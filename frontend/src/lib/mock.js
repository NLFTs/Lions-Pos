/**
 * Mocking layer for GAPETK Frontend
 * This allows the frontend to run without a backend.
 */

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
          'user.index', 'user.store', 'user.update', 'user.destroy',
          'post.index', 'post.store', 'post.update', 'post.destroy',
          'category.index', 'category.store', 'category.update', 'category.destroy',
          'role.index', 'role.store', 'role.update', 'role.destroy',
          'permission.index', 'permission.store', 'permission.update', 'permission.destroy',
          'module.index', 'module.store', 'module.update', 'module.destroy',
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
        { id: 'r1', name: 'ADMIN', description: 'Full access' },
        { id: 'r2', name: 'EDITOR', description: 'Can edit posts' },
      ]
    }
  },
  '/api/v1/permissions': {
    status: 200,
    data: {
      data: [
        { id: 'perm1', name: 'User Index', slug: 'user.index' },
        { id: 'perm2', name: 'Post Index', slug: 'post.index' },
      ]
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
    // Check if we have mock data for this URL
    // Strip base URL if present
    const url = config.url.replace(config.baseURL, '')
    
    if (mockData[url]) {
      console.log(`[Mock API] Intercepting ${config.method.toUpperCase()} ${url}`)
      
      // Return a resolved promise with the mock data
      // We throw an object that looks like an Axios error or success
      // But actually, we want to return a response object.
      // Axios interceptors allow returning a response directly to skip the network.
      return Promise.reject({
        __isMock: true,
        response: mockData[url]
      })
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
