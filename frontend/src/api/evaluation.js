import request from './request'

export const getEvaluationsByTaskId = (taskId) => request.get(`/evaluation/task/${taskId}`)
export const getTaskEvaluationSummary = (taskId) => request.get(`/evaluation/task/${taskId}/summary`)
