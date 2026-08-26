package com.lecture.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type")
    @Builder.Default
    private RequestType requestType = RequestType.LOAN;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "review_comment", length = 500)
    private String reviewComment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING,
        ACTIVE,
        REJECTED,
        CANCELLED
    }

    public enum RequestType {
        LOAN,
        PURCHASE
    }

    public void activate() {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("대기 중인 신청만 승인할 수 있습니다");
        }
        this.status = Status.ACTIVE;
    }

    public void reject(String reviewComment) {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("대기 중인 신청만 반려할 수 있습니다");
        }
        this.status = Status.REJECTED;
        this.reviewComment = reviewComment;
    }

    public void cancel() {
        this.status = Status.CANCELLED;
    }

    @PrePersist
    @PostLoad
    private void applyGearHubDefaults() {
        if (requestType == null) requestType = RequestType.LOAN;
    }
}
