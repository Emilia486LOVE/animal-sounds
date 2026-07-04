import request from './request'

export const predict = (data) => {
  return request({
    url: '/prediction/single',
    method: 'post',
    data
  })
}

export const batchPredict = (data) => {
  return request({
    url: '/prediction/batch',
    method: 'post',
    data
  })
}