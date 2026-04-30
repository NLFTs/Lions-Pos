/**
 * Mocking layer for GAPETK Frontend
 * This allows the frontend to run without a backend.
 */

const permissionModules = ['user', 'post', 'category', 'role', 'permission', 'module']
const permissionActions = ['index', 'show', 'store', 'update', 'delete']

const mockPermissionsMap = permissionModules.reduce((acc, module) => {
  acc[module] = permissionActions.map((action) => ({
    id: `perm-${module}-${action}`,
    name: `${action.charAt(0).toUpperCase()}${action.slice(1)} ${module}`,
    slug: `${module}.${action}`,
    module,
    action,
  }))
  return acc
}, {
  log: [
    { id: 'perm-log-index', name: 'Index log', slug: 'log.index', module: 'log', action: 'index' },
  ],
})

const allMockPermissions = Object.values(mockPermissionsMap).flat()
const editorPermissions = allMockPermissions.filter((permission) => (
  ['post', 'category'].includes(permission.module)
  || permission.slug === 'log.index'
))

function parseBody(data) {
  if (!data) return {}
  if (typeof data === 'string') {
    try {
      return JSON.parse(data)
    } catch {
      return {}
    }
  }
  return data
}

function mockRoleMutation(config, url) {
  const method = (config.method || 'get').toLowerCase()
  const body = parseBody(config.data)

  if (method === 'post' && url === '/api/v1/roles') {
    const permissions = allMockPermissions.filter((permission) => body.permissionIds?.includes(permission.id))
    return {
      status: 200,
      data: {
        data: {
          id: `r-${Date.now()}`,
          name: body.name,
          slug: body.slug,
          permissions,
        },
      },
    }
  }

  const permissionMatch = url.match(/^\/api\/v1\/roles\/([^/]+)\/permissions$/)
  if (method === 'put' && permissionMatch) {
    const permissions = allMockPermissions.filter((permission) => body.permissionIds?.includes(permission.id))
    return {
      status: 200,
      data: {
        data: {
          id: permissionMatch[1],
          permissions,
        },
      },
    }
  }

  const roleMatch = url.match(/^\/api\/v1\/roles\/([^/]+)$/)
  if (roleMatch && ['put', 'delete'].includes(method)) {
    if (method === 'delete') {
      return { status: 204, data: null }
    }

    return {
      status: 200,
      data: {
        data: {
          id: roleMatch[1],
          name: body.name,
          slug: body.slug,
        },
      },
    }
  }

  return null
}

const mockData = {
  '/api/v1/auth/me': {
    status: 200,
    data: {
      data: {
        id: 'mock-user-1',
        username: 'admin',
        fullname: 'Super Admin Mock',
        roles: ['ADMIN'],
        permissions: [
          'user.index', 'user.store', 'user.update', 'user.delete', 'user.destroy',
          'post.index', 'post.store', 'post.update', 'post.delete', 'post.destroy',
          'category.index', 'category.store', 'category.update', 'category.delete', 'category.destroy',
          'role.index', 'role.store', 'role.update', 'role.delete', 'role.destroy',
          'permission.index', 'permission.store', 'permission.update', 'permission.delete', 'permission.destroy',
          'module.index', 'module.store', 'module.update', 'module.delete', 'module.destroy',
          'log.index'
        ]
      }
    }
  },
  '/api/v1/auth/login': {
    status: 200,
    data: {
      data: {
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token'
      }
    }
  },
  '/api/v1/users': {
    status: 200,
    data: {
      data: [
        { id: '1', username: 'john_doe', fullname: 'John Doe', createdAt: new Date().toISOString() },
        { id: '2', username: 'jane_smith', fullname: 'Jane Smith', createdAt: new Date(Date.now() - 86400000).toISOString() },
        { id: '3', username: 'bob_builder', fullname: 'Bob Builder', createdAt: new Date(Date.now() - 172800000).toISOString() },
      ]
    }
  },
  '/api/v1/posts?page=0&size=5': {
    status: 200,
    data: {
      data: {
        totalElements: 12,
        content: [
          { id: 'p1', title: 'Cara Menggunakan Spravel', status: 'published', createdAt: new Date().toISOString() },
          { id: 'p2', title: 'Tips Desain UI Modern', status: 'draft', createdAt: new Date(Date.now() - 3600000).toISOString() },
          { id: 'p3', title: 'Panduan Vue 3 Composition API', status: 'published', createdAt: new Date(Date.now() - 7200000).toISOString() },
        ]
      }
    }
  },
  '/api/v1/categories': {
    status: 200,
    data: {
      data: [
        { id: 'c1', name: 'Tutorial', slug: 'tutorial' },
        { id: 'c2', name: 'Design', slug: 'design' },
        { id: 'c3', name: 'News', slug: 'news' },
      ]
    }
  },
  '/api/v1/roles': {
    status: 200,
    data: {
      data: [
        { id: 'r1', name: 'ADMIN', slug: 'admin', description: 'Full access', permissions: allMockPermissions },
        { id: 'r2', name: 'EDITOR', slug: 'editor', description: 'Can edit posts', permissions: editorPermissions },
      ]
    }
  },
  '/api/v1/roles/permissions': {
    status: 200,
    data: {
      data: mockPermissionsMap
    }
  },
  '/api/v1/permissions': {
    status: 200,
    data: {
      data: allMockPermissions
    }
  },
  '/api/v1/modules': {
    status: 200,
    data: {
      data: [
        { id: 'm1', name: 'User Management' },
        { id: 'm2', name: 'Content Management' },
      ]
    }
  },
  '/api/v1/logs?page=0&size=20': {
    status: 200,
    data: {
      data: {
        totalElements: 4,
        totalPages: 1,
        number: 0,
        size: 20,
        content: [
          {
            id: 'log1',
            method: 'POST',
            url: '/api/v1/auth/login',
            responseStatus: 200,
            userFullname: 'Super Admin Mock',
            userId: 'mock-user-1',
            durationMs: 45,
            requestAt: new Date().toISOString(),
            responseAt: new Date().toISOString(),
            userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/115.0.0.0 Safari/537.36'
          },
          {
            id: 'log2',
            method: 'GET',
            url: '/api/v1/users',
            responseStatus: 200,
            userFullname: 'Super Admin Mock',
            userId: 'mock-user-1',
            durationMs: 120,
            requestAt: new Date(Date.now() - 3600000).toISOString(),
            responseAt: new Date(Date.now() - 3600000 + 120).toISOString(),
            userAgent: 'Mozilla/5.0'
          },
          {
            id: 'log3',
            method: 'PUT',
            url: '/api/v1/settings/theme',
            responseStatus: 204,
            userFullname: 'Jane Smith',
            userId: '2',
            durationMs: 80,
            requestAt: new Date(Date.now() - 7200000).toISOString(),
            responseAt: new Date(Date.now() - 7200000 + 80).toISOString(),
            userAgent: 'PostmanRuntime/7.28.4'
          },
          {
            id: 'log4',
            method: 'DELETE',
            url: '/api/v1/posts/p1',
            responseStatus: 403,
            userFullname: 'Bob Builder',
            userId: '3',
            durationMs: 35,
            requestAt: new Date(Date.now() - 86400000).toISOString(),
            responseAt: new Date(Date.now() - 86400000 + 35).toISOString(),
            userAgent: 'curl/7.68.0'
          }
        ]
      }
    }
  }
}

export function setupMocks(api) {
  api.interceptors.request.use(async (config) => {
    // Strip base URL if present
    const url = config.url.replace(config.baseURL, '')
    const roleMutationResponse = mockRoleMutation(config, url)

    if (roleMutationResponse) {
      console.log(`[Mock API] Intercepting ${config.method.toUpperCase()} ${url}`)
      return Promise.reject({ __isMock: true, response: roleMutationResponse })
    }

    // Exact match first
    if (mockData[url]) {
      console.log(`[Mock API] Intercepting ${config.method.toUpperCase()} ${url}`)
      return Promise.reject({ __isMock: true, response: mockData[url] })
    }

    // Prefix / pattern match for paginated endpoints
    // e.g. /api/v1/users?page=0&size=10  → use /api/v1/users mock
    const baseUrl = url.split('?')[0]
    if (mockData[baseUrl]) {
      const mockResponse = mockData[baseUrl]

      // If the mock data is a flat array, wrap it in a paginated envelope
      const rawData = mockResponse.data?.data
      if (Array.isArray(rawData)) {
        const params = new URLSearchParams(url.split('?')[1] || '')
        const page = parseInt(params.get('page') || '0')
        const size = parseInt(params.get('size') || '10')
        const sliced = rawData.slice(page * size, page * size + size)
        const paginatedResponse = {
          ...mockResponse,
          data: {
            data: {
              content: sliced,
              totalElements: rawData.length,
              totalPages: Math.ceil(rawData.length / size),
              number: page,
              size,
            }
          }
        }
        console.log(`[Mock API] Intercepting (paginated) ${config.method.toUpperCase()} ${url}`)
        return Promise.reject({ __isMock: true, response: paginatedResponse })
      }

      console.log(`[Mock API] Intercepting (prefix) ${config.method.toUpperCase()} ${url}`)
      return Promise.reject({ __isMock: true, response: mockResponse })
    }

    return config
  })

  api.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.__isMock) {
        return Promise.resolve(error.response)
      }
      return Promise.reject(error)
    }
  )
}
