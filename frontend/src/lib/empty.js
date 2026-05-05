/**
 * Empty data layer.
 * Use this for a clean local UI without sample mock data and without backend calls.
 */

import { FULL_PERMISSIONS } from './appMode'

const emptyUser = {
  id: 'empty-user',
  username: 'admin',
  fullname: 'Empty Admin',
  roles: ['ADMIN'],
  permissions: FULL_PERMISSIONS,
}

function paginatedData(config) {
  const url = config.url || ''
  const params = new URLSearchParams(url.split('?')[1] || '')
  const page = parseInt(params.get('page') || '0')
  const size = parseInt(params.get('size') || '10')

  return {
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: page,
    size,
  }
}

function emptyResponse(config) {
  const url = config.url || ''
  const method = (config.method || 'get').toLowerCase()
  const path = url.split('?')[0]

  if (path === '/api/v1/auth/login') {
    return {
      status: 200,
      data: {
        data: {
          accessToken: 'empty-access-token',
          refreshToken: 'empty-refresh-token',
        },
      },
    }
  }

  if (path === '/api/v1/auth/me') {
    return {
      status: 200,
      data: {
        data: emptyUser,
      },
    }
  }

  if (path === '/api/v1/auth/logout') {
    return { status: 204, data: null }
  }

  if (method === 'get') {
    const isPaginated = url.includes('page=') || ['/api/v1/posts', '/api/v1/logs', '/api/v1/users'].includes(path)
    return {
      status: 200,
      data: {
        data: isPaginated ? paginatedData(config) : [],
      },
    }
  }

  if (method === 'delete') {
    return { status: 204, data: null }
  }

  return {
    status: 200,
    data: {
      data: {},
    },
  }
}

export function setupEmptyData(api) {
  api.interceptors.request.use((config) => {
    return Promise.reject({ __isEmptyData: true, response: emptyResponse(config) })
  })

  api.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.__isEmptyData) {
        return Promise.resolve(error.response)
      }
      return Promise.reject(error)
    }
  )
}
