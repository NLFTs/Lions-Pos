import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { setupMocks } from './mock'
import { setupEmptyData } from './empty'
import { FULL_PERMISSIONS, isEmptyMode, isMockMode } from './appMode'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  headers: { 'Content-Type': 'application/json' },
})

if (isEmptyMode) {
  setupEmptyData(api)
} else if (isMockMode) {
  setupMocks(api)
}

// ─── Helper: decode permissions from JWT payload (no extra lib needed) ────────
function parseJwtPerms(token) {
  if ((isMockMode && token === 'mock-access-token') || (isEmptyMode && token === 'empty-access-token')) {
    return FULL_PERMISSIONS
  }
  try {
    const payload = JSON.parse(
      atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))
    )
    return Array.isArray(payload.perms) ? payload.perms : []
  } catch {
    return []
  }
}

// ─── Request: attach access token ────────────────────────────────────────────
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// ─── Response: handle 401 → refresh token ────────────────────────────────────
let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error)
    else prom.resolve(token)
  })
  failedQueue = []
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Skip refresh logic untuk auth endpoints agar tidak loop
    const skipUrls = ['/auth/login', '/auth/refresh', '/auth/logout', '/auth/force-logout-all']
    const isAuthPath = skipUrls.some(u => originalRequest?.url?.includes(u))

    // Cek apakah ini 401 yang bisa di-refresh
    const is401 = error.response?.status === 401
    const isTokenExpired = is401 || error.response?.data?.error === 'TOKEN_EXPIRED'

    if (isTokenExpired && !originalRequest?._retry && !isAuthPath) {
      if (isRefreshing) {
        // Antri request yang gagal, tunggu refresh selesai
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return api(originalRequest)
          })
          .catch((err) => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      const storedRefreshToken = localStorage.getItem('refresh_token')
      if (!storedRefreshToken) {
        isRefreshing = false
        useAuthStore().logout()
        return Promise.reject(error)
      }

      try {
        console.log('[API] Token expired, attempting refresh...')

        // Pakai axios bersih — hindari interceptor recursive
        const baseURL = import.meta.env.VITE_API_URL || window.location.origin
        const refreshRes = await axios.post(
          `${baseURL}/api/v1/auth/refresh?refreshToken=${encodeURIComponent(storedRefreshToken)}`
        )

        const newAccessToken = refreshRes.data?.data?.accessToken
        if (!newAccessToken) throw new Error('No access token in refresh response')

        console.log('[API] Token refresh successful, syncing session...')

        // Simpan token baru
        localStorage.setItem('access_token', newAccessToken)
        const auth = useAuthStore()
        auth.accessToken = newAccessToken

        // Sync ulang permissions dari server (lebih akurat dari JWT claim)
        try {
          await auth.fetchMe()
        } catch (_) {
          // fetchMe gagal tidak critical — pakai permissions dari JWT
          const newPerms = parseJwtPerms(newAccessToken)
          auth.permissions = newPerms
          localStorage.setItem('auth_permissions', JSON.stringify(newPerms))
        }

        processQueue(null, newAccessToken)

        // Retry original request dengan token baru
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        return api(originalRequest)
      } catch (refreshErr) {
        console.warn('[API] Refresh token failed, logging out.', refreshErr.message)
        processQueue(refreshErr, null)
        useAuthStore().logout()
        return Promise.reject(refreshErr)
      } finally {
        isRefreshing = false
      }
    }

    if (!error.response) {
      console.warn('[API] Network Error / Backend unreachable.')
      return Promise.reject({ ...error, isNetworkError: true })
    }

    return Promise.reject(error)
  }
)

export default api
