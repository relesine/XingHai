import request from './request'

export const apiLogin = data => request.post('/auth/login', data)

export const apiMyVideos = (pageNum = 1) => request.get('/video/mine', { params: { pageNum, pageSize: 20 } })
export const apiUpdateVideo = (id, data) => request.put(`/video/${id}`, data)
export const apiDeleteVideo = id => request.delete(`/video/${id}`)
export const apiSetVisibility = (id, isPublic) => request.put(`/video/visibility/${id}`, { isPublic })

export const apiDashboard = () => request.get('/creator/dashboard')

export const apiMyShop = () => request.get('/shop/mine')
export const apiCreateShop = data => request.post('/shop/create', data)
export const apiUpdateShop = (id, data) => request.put(`/shop/${id}`, data)

export const apiShopProducts = (shopId, pageNum = 1) => request.get(`/product/shop/${shopId}`, { params: { pageNum, pageSize: 50 } })
export const apiAddProduct = data => request.post('/product', data)
export const apiUpdateProduct = (id, data) => request.put(`/product/${id}`, data)
export const apiProductStatus = (id, status) => request.put(`/product/status/${id}`, { status })
export const apiDeleteProduct = id => request.delete(`/product/${id}`)

export const apiShopOrders = (status, pageNum = 1) => request.get('/order/shop', { params: { status, pageNum } })
export const apiShipOrder = id => request.post(`/order/ship/${id}`)
export const apiCompleteOrder = id => request.post(`/order/complete/${id}`)

export const apiLiveList = () => request.get('/live/list')
export const apiLiveStart = title => request.post('/live/start', { title })
export const apiLiveStop = id => request.post(`/live/stop/${id}`)

export const apiProfile = () => request.get('/user/profile')
export const apiUpdateProfile = data => request.put('/user/profile', data)
