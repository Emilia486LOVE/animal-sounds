import request from "./request";

export const getAllDatasets = () => request.get("/datasets");
export const createDataset = (data) => request.post("/datasets", data);
export const updateDataset = (id, data) => request.put(`/datasets/${id}`, data);
export const deleteDataset = (id) => request.delete(`/datasets/${id}`);
export const refreshAudioCount = (id) =>
  request.post(`/datasets/${id}/refresh-count`);
