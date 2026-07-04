import request from './request';

export const getAllAudioFiles = () => request.get('/audio');
export const getAudioFileById = (id) => request.get(`/audio/${id}`);
export const getAudioFilesByDatasetId = (datasetId) => request.get(`/audio/dataset/${datasetId}`);
export const getAudioFilesByNoiseLevel = (noiseLevel) => request.get(`/audio/noise/${noiseLevel}`);
export const searchAudioFilesByLocation = (location) => request.get(`/audio/location?location=${location}`);
export const uploadAudioFiles = (datasetId, files) => {
  const formData = new FormData();
  files.forEach(file => formData.append('files', file));
  return request.post(`/audio/upload/${datasetId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};
export const updateAudioFile = (id, data) => request.put(`/audio/${id}`, data);
export const deleteAudioFile = (id) => request.delete(`/audio/${id}`);