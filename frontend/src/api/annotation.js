import request from './request';

export const getAllAnnotations = () => request.get('/annotations');
export const getAnnotationById = (id) => request.get(`/annotations/${id}`);
export const getAnnotationsByAudioId = (audioId) => request.get(`/annotations/audio/${audioId}`);
export const getAnnotationsByLabelId = (labelId) => request.get(`/annotations/label/${labelId}`);
export const getAnnotationsByStatus = (status) => request.get(`/annotations/status/${status}`);
export const getAnnotationsByAnnotatorId = (annotatorId) => request.get(`/annotations/annotator/${annotatorId}`);
export const createAnnotation = (data) => request.post('/annotations', data);
export const updateAnnotation = (id, data) => request.put(`/annotations/${id}`, data);
export const deleteAnnotation = (id) => request.delete(`/annotations/${id}`);
export const submitAnnotation = (id) => request.post(`/annotations/${id}/submit`);
export const saveDraft = (id) => request.post(`/annotations/${id}/save-draft`);
export const reviewAnnotation = (id, data) => request.post(`/annotations/${id}/review`, data);
export const countByLabelId = (labelId) => request.get(`/annotations/count/label/${labelId}`);
export const countByStatus = (status) => request.get(`/annotations/count/status/${status}`);