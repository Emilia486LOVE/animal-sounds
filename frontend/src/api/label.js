import request from './request';

export const getAllLabels = () => request.get('/labels');
export const getLabelTree = () => request.get('/labels/tree');
export const getLabelById = (id) => request.get(`/labels/${id}`);
export const getLabelsByParentId = (parentId) => request.get(`/labels/parent/${parentId}`);
export const getLabelsByTaxonRank = (taxonRank) => request.get(`/labels/rank/${taxonRank}`);
export const getAncestors = (id) => request.get(`/labels/${id}/ancestors`);
export const getDescendants = (id) => request.get(`/labels/${id}/descendants`);
export const createLabel = (data) => request.post('/labels', data);
export const updateLabel = (id, data) => request.put(`/labels/${id}`, data);
export const deleteLabel = (id) => request.delete(`/labels/${id}`);