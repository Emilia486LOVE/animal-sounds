import request from './request'

export const getAllLabels = () => request.get('/labels')
export const createLabel = (data) => request.post('/labels', data)
export const updateLabel = (id, data) => request.put(`/labels/${id}`, data)
export const deleteLabel = (id) => request.delete(`/labels/${id}`)
