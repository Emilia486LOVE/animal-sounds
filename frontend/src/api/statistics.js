import request from "./request";

export const getOverview = () => request.get("/statistics/overview");
export const getDatasetStatistics = () => request.get("/statistics/dataset");
export const getAnnotationStatusStatistics = () =>
  request.get("/statistics/annotation/status");
export const getLabelStatistics = () => request.get("/statistics/label");
export const getNoiseLevelStatistics = () => request.get("/statistics/noise");
