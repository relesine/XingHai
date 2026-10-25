import request from './request'

export const apiLogin = data => request.post('/auth/login', data)

export const apiStats = () => request.get('/admin/stats')

export const apiUsers = (pageNum = 1, pageSize = 10, keyword = '') =>
  request.get('/admin/users', { params: { pageNum, pageSize, keyword: keyword || undefined } })
export const apiUserStatus = (userId, status) => request.put(`/admin/user/status/${userId}`, { status })
export const apiAssignRole = (userId, roleId) => request.post('/admin/user/role', { userId, roleId })
export const apiRoles = () => request.get('/admin/roles')
export const apiPermissions = () => request.get('/admin/permissions')
export const apiAssignPermissions = (roleId, permissionIds) =>
  request.post('/admin/role/permissions', { roleId, permissionIds })

export const apiAdminVideos = (pageNum = 1, pageSize = 10, status) =>
  request.get('/video/admin/list', { params: { pageNum, pageSize, status: status === undefined ? undefined : status } })
export const apiAudit = (videoId, action, rejectReason) =>
  request.post('/video/audit', { videoId, action, rejectReason })

export const apiReports = (pageNum = 1, pageSize = 10, status) =>
  request.get('/report/list', { params: { pageNum, pageSize, status: status === undefined ? undefined : status } })
export const apiHandleReport = (reportId, action) => request.post(`/report/handle/${reportId}`, { action })

export const apiNotices = () => request.get('/notice/list')
export const apiSaveNotice = (title, content) => request.post('/notice', { title, content })
export const apiUpdateNotice = (id, body) => request.put(`/notice/${id}`, body)
export const apiDeleteNotice = id => request.delete(`/notice/${id}`)

export const apiAdverts = () => request.get('/advert/list')
export const apiSaveAdvert = data => request.post('/advert', data)
export const apiUpdateAdvert = data => request.put('/advert', data)
export const apiDeleteAdvert = id => request.delete(`/advert/${id}`)

export const apiLiveList = () => request.get('/live/list')
export const apiLiveStop = roomId => request.post(`/live/stop/${roomId}`)
