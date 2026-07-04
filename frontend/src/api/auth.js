import request from './request'

export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const changePassword = (data) => request.post('/auth/change-password', data)
export const getCurrentUser = () => request.get('/auth/me')
export const getCaptcha = () => request.get('/auth/captcha')
