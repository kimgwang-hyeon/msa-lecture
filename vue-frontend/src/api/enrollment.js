import api from './index.js'

export const enrollmentApi = {
  getMyEnrollments() {
    return api.get('/api/enrollments/my')
  },
  enroll(courseId, reason) {
    return api.post('/api/enrollments', { courseId, reason })
  },
  requestPurchase(data) {
    return api.post('/api/enrollments/purchases', data)
  },
  getPending(requestType) {
    return api.get('/api/enrollments/pending', { params: { requestType } })
  },
  approve(enrollmentId) {
    return api.post(`/api/enrollments/${enrollmentId}/approve`)
  },
  reject(enrollmentId, reviewComment) {
    return api.post(`/api/enrollments/${enrollmentId}/reject`, { reviewComment })
  },
  getRecommendations(userId) {
    return api.get(`/api/recommend/${userId}`)
  },
  getAlternatives(category) {
    return api.get('/api/recommend/alternatives', { params: { category } })
  }
}
