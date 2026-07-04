import request from './request';

export const getAllTasks = () => request.get('/train/tasks');
export const getTaskById = (id) => request.get(`/train/tasks/${id}`);
export const getTasksByStatus = (status) => request.get(`/train/tasks/status/${status}`);
export const getTaskProgress = (id) => request.get(`/train/tasks/${id}/progress`);
export const createTask = (data) => request.post('/train/tasks', data);
export const startTask = (id) => request.post(`/train/tasks/${id}/start`);
export const updateTaskProgress = (id, epoch, valMetric) => 
  request.put(`/train/tasks/${id}/progress?epoch=${epoch}&valMetric=${valMetric}`);
export const completeTask = (id, modelPath) => 
  request.post(`/train/tasks/${id}/complete?modelPath=${modelPath}`);
export const failTask = (id, errorMsg) => 
  request.post(`/train/tasks/${id}/fail?errorMsg=${errorMsg}`);
export const deleteTask = (id) => request.delete(`/train/tasks/${id}`);
export const generateModelPath = (id) => request.get(`/train/tasks/${id}/model-path`);