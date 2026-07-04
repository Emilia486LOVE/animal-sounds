import request from './request'

export const getAllAudioFiles = () => request.get('/audio')
export const uploadAudioFiles = (data) => request.post('/audio/upload', data)
export const updateAudioFile = (id, data) => request.put(`/audio/${id}`, data)
export const deleteAudioFile = (id) => request.delete(`/audio/${id}`)
