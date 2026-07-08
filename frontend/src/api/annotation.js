import request from "./request";

export const getAllAnnotations = () => request.get("/annotations");
export const createAnnotation = (data) => request.post("/annotations", data);
export const updateAnnotation = (id, data) =>
  request.put(`/annotations/${id}`, data);
export const deleteAnnotation = (id) => request.delete(`/annotations/${id}`);
export const submitAnnotation = (id) =>
  request.post(`/annotations/${id}/submit`);
export const reviewAnnotation = (id, data) =>
  request.post(`/annotations/${id}/review`, data);
