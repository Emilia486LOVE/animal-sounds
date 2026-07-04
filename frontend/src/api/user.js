import request from './request';

export const getAllUsers = () => request.get('/admin/users');
export const getUserById = (id) => request.get(`/admin/users/${id}`);
export const createUser = (data) => request.post('/admin/users', data);
export const updateUser = (id, data) => request.put(`/admin/users/${id}`, data);
export const deleteUser = (id) => request.delete(`/admin/users/${id}`);
export const getUsersByRole = (role) => request.get(`/admin/users/role/${role}`);