import request from "./request";

export const getAllAudioFiles = () => request.get("/audio");
export const getAudioFilesByDataset = (datasetId) =>
  request.get(`/audio/dataset/${datasetId}`);
export const uploadAudioFiles = (data) => request.post("/audio/upload", data);
export const updateAudioFile = (id, data) => request.put(`/audio/${id}`, data);
export const deleteAudioFile = (id) => request.delete(`/audio/${id}`);
export const moveAudioToDataset = (audioId, targetDatasetId) =>
  request.put(`/audio/${audioId}/move`, null, { params: { targetDatasetId } });
export const batchMoveAudio = (audioIds, targetDatasetId) =>
  request.put("/audio/batch-move", null, {
    params: { audioIds: audioIds.join(","), targetDatasetId },
  });
export const batchDeleteAudio = (audioIds) =>
  request.delete("/audio/batch", { params: { audioIds: audioIds.join(",") } });
