import request from './request';

export const getEvaluationsByTaskId = (taskId) => request.get(`/evaluation/task/${taskId}`);
export const getEvaluationById = (id) => request.get(`/evaluation/${id}`);
export const getTaskEvaluationSummary = (taskId) => request.get(`/evaluation/task/${taskId}/summary`);
export const saveEvaluations = (taskId, data) => request.post(`/evaluation/task/${taskId}`, data);
export const deleteEvaluations = (taskId) => request.delete(`/evaluation/task/${taskId}`);