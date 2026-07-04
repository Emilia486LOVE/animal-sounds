import request from './request'

export const getAllTasks = () => request.get('/train/tasks')
export const createTask = (data) => request.post('/train/tasks', data)
export const startTask = (id) => request.post(`/train/tasks/${id}/start`)
export const deleteTask = (id) => request.delete(`/train/tasks/${id}`)
