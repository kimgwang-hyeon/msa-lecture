package com.lecture.enrollment.service;

import com.lecture.enrollment.dto.EnrollmentDto;
import com.lecture.enrollment.entity.Enrollment;
import com.lecture.enrollment.kafka.EnrollmentKafkaProducer;
import com.lecture.enrollment.kafka.KafkaEvent;
import com.lecture.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseServiceClient courseServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final EnrollmentKafkaProducer kafkaProducer;
    private final EnrollmentWriteService enrollmentWriteService;

    /** 기존 수강신청 API를 교보재 대여 신청으로 사용한다. */
    public EnrollmentDto.EnrollmentResponse enroll(Long userId, Long courseId, String reason) {
        if (!courseServiceClient.existsCourse(courseId)) {
            throw new IllegalArgumentException("존재하지 않는 교보재입니다: " + courseId);
        }

        Map<String, Object> courseInfo = courseServiceClient.getCourse(courseId);
        if (!"OWNED".equals(stringValue(courseInfo.get("itemType")))
                || !"ACTIVE".equals(stringValue(courseInfo.get("status")))) {
            throw new IllegalArgumentException("대여할 수 있는 교보재가 아닙니다");
        }
        if (toInteger(courseInfo.get("availableQuantity")) <= 0) {
            throw new IllegalStateException("현재 대여 가능한 수량이 없습니다");
        }
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new IllegalArgumentException("이미 신청한 교보재입니다");
        }

        Enrollment enrollment = enrollmentWriteService.createPendingEnrollment(
                userId,
                courseId,
                Enrollment.RequestType.LOAN,
                reason
        );

        log.info("[EnrollmentService] 교보재 대여 신청 완료 - enrollmentId: {}", enrollment.getId());
        return EnrollmentDto.EnrollmentResponse.from(enrollment, toCourseSummary(courseInfo));
    }

    /** 신규 교보재 구매요청: 비공개 Course + Enrollment + PENDING Payment를 만든다. */
    public EnrollmentDto.EnrollmentResponse requestPurchase(
            Long userId,
            EnrollmentDto.PurchaseRequest request) {
        Map<String, Object> courseInfo = courseServiceClient.createPurchaseCourse(userId, request);
        Long courseId = toLong(courseInfo.get("id"));

        Enrollment enrollment = enrollmentWriteService.createPendingEnrollment(
                userId,
                courseId,
                Enrollment.RequestType.PURCHASE,
                request.getReason()
        );

        BigDecimal totalAmount = request.getUnitPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity().longValue()));
        paymentServiceClient.requestPayment(userId, courseId, totalAmount);

        log.info("[EnrollmentService] 신규 교보재 구매요청 완료 - enrollmentId: {}, totalAmount: {}",
                enrollment.getId(), totalAmount);
        return EnrollmentDto.EnrollmentResponse.from(enrollment, toCourseSummary(courseInfo));
    }

    /** 운영진의 대여 승인. 구매요청 승인은 Payment Service에서 처리한다. */
    @Transactional
    public EnrollmentDto.EnrollmentResponse approveLoan(Long enrollmentId) {
        Enrollment enrollment = findEnrollment(enrollmentId);
        if (enrollment.getRequestType() != Enrollment.RequestType.LOAN) {
            throw new IllegalArgumentException("대여 신청만 이 API에서 승인할 수 있습니다");
        }
        if (enrollment.getStatus() == Enrollment.Status.ACTIVE) {
            return toResponseWithCourse(enrollment);
        }

        courseServiceClient.borrowCourse(enrollment.getCourseId());
        enrollment.activate();
        publishCompleted(enrollment);
        return toResponseWithCourse(enrollment);
    }

    @Transactional
    public EnrollmentDto.EnrollmentResponse rejectLoan(Long enrollmentId, String reviewComment) {
        if (reviewComment == null || reviewComment.isBlank()) {
            throw new IllegalArgumentException("반려 사유를 입력해 주세요");
        }

        Enrollment enrollment = findEnrollment(enrollmentId);
        if (enrollment.getRequestType() != Enrollment.RequestType.LOAN) {
            throw new IllegalArgumentException("대여 신청만 이 API에서 반려할 수 있습니다");
        }
        enrollment.reject(reviewComment);
        return toResponseWithCourse(enrollment);
    }

    /** payment.completed 이벤트를 예산 검토 완료 이벤트로 해석한다. */
    @Transactional
    public void handleBudgetReview(Long userId, Long courseId, String status) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "구매요청 정보를 찾을 수 없습니다 - userId: " + userId + ", courseId: " + courseId));

        if (enrollment.getRequestType() != Enrollment.RequestType.PURCHASE) {
            throw new IllegalArgumentException("구매요청이 아닌 신청입니다");
        }

        if ("COMPLETED".equals(status)) {
            if (enrollment.getStatus() == Enrollment.Status.ACTIVE) {
                return;
            }
            enrollment.activate();
            publishCompleted(enrollment);
            log.info("[EnrollmentService] 구매요청 예산 승인 - enrollmentId: {}", enrollment.getId());
            return;
        }

        if ("FAILED".equals(status)) {
            if (enrollment.getStatus() == Enrollment.Status.REJECTED) {
                return;
            }
            enrollment.reject("운영진 예산 검토에서 반려되었습니다");
            log.info("[EnrollmentService] 구매요청 예산 반려 - enrollmentId: {}", enrollment.getId());
        }
    }

    public List<EnrollmentDto.EnrollmentResponse> getEnrollmentsByUser(Long userId) {
        return enrollmentRepository.findByUserId(userId).stream()
                .map(this::toResponseWithCourse)
                .collect(Collectors.toList());
    }

    public List<EnrollmentDto.EnrollmentResponse> getPendingEnrollments(
            Enrollment.RequestType requestType) {
        return enrollmentRepository
                .findByStatusAndRequestType(Enrollment.Status.PENDING, requestType)
                .stream()
                .map(this::toResponseWithCourse)
                .collect(Collectors.toList());
    }

    public EnrollmentDto.EnrollmentHistoryResponse getEnrollmentHistory(Long userId) {
        List<Long> activeCourseIds = enrollmentRepository
                .findByUserIdAndStatus(userId, Enrollment.Status.ACTIVE)
                .stream()
                .filter(enrollment -> enrollment.getRequestType() == Enrollment.RequestType.LOAN)
                .map(Enrollment::getCourseId)
                .collect(Collectors.toList());

        return EnrollmentDto.EnrollmentHistoryResponse.builder()
                .userId(userId)
                .activeCourseIds(activeCourseIds)
                .build();
    }

    private EnrollmentDto.EnrollmentResponse toResponseWithCourse(Enrollment enrollment) {
        Map<String, Object> courseInfo = courseServiceClient.getCourse(enrollment.getCourseId());
        return EnrollmentDto.EnrollmentResponse.from(enrollment, toCourseSummary(courseInfo));
    }

    private EnrollmentDto.CourseSummary toCourseSummary(Map<String, Object> courseInfo) {
        return EnrollmentDto.CourseSummary.builder()
                .id(toLong(courseInfo.get("id")))
                .title(stringValue(courseInfo.get("title")))
                .description(stringValue(courseInfo.get("description")))
                .category(normalizeCategory(stringValue(courseInfo.get("category"))))
                .price(toBigDecimal(courseInfo.get("price")))
                .thumbnail(stringValue(courseInfo.get("thumbnail")))
                .instructorName(firstNonBlank(
                        stringValue(courseInfo.get("instructorName")),
                        stringValue(courseInfo.get("teacherName")),
                        stringValue(courseInfo.get("instructor_name"))
                ))
                .enrollmentCount(toInteger(courseInfo.get("enrollmentCount")))
                .itemType(stringValue(courseInfo.get("itemType")))
                .totalQuantity(toInteger(courseInfo.get("totalQuantity")))
                .availableQuantity(toInteger(courseInfo.get("availableQuantity")))
                .purchaseUrl(stringValue(courseInfo.get("purchaseUrl")))
                .build();
    }

    private void publishCompleted(Enrollment enrollment) {
        kafkaProducer.publishEnrollmentCompleted(
                KafkaEvent.EnrollmentCompletedEvent.builder()
                        .enrollmentId(enrollment.getId())
                        .userId(enrollment.getUserId())
                        .courseId(enrollment.getCourseId())
                        .build()
        );
    }

    private Enrollment findEnrollment(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다: " + enrollmentId));
    }

    private String normalizeCategory(String category) {
        if (category == null) return null;
        return switch (category) {
            case "DEVICE", "MOBILE" -> "스마트기기";
            case "COMPUTER" -> "컴퓨터";
            case "SERVER_CLOUD", "DEVOPS" -> "서버·클라우드";
            case "ELECTRONICS_IOT" -> "전자·IoT";
            case "MAKER" -> "메이커·건축";
            case "CAMERA_AUDIO" -> "촬영·음향";
            case "BACKEND" -> "개발장비";
            case "FRONTEND" -> "디자인장비";
            case "DATA_SCIENCE" -> "데이터장비";
            case "SECURITY" -> "보안장비";
            default -> "기타";
        };
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(value.toString());
    }

    private Integer toInteger(Object value) {
        if (value == null) return 0;
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal decimal) return decimal;
        return new BigDecimal(value.toString());
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
