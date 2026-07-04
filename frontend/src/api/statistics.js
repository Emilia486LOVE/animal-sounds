import request from './request'

export const getOverview = () => request.get('/statistics/overview')
