import api from './index.js'

export const paymentApi = {
  getPending() {
    return api.get('/api/payments', { params: { status: 'PENDING' } })
  },
  approve(paymentId) {
    return api.post(`/api/payments/${paymentId}/approve`)
  },
  reject(paymentId) {
    return api.post(`/api/payments/${paymentId}/reject`)
  }
}
