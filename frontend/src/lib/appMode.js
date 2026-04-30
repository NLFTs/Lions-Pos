export const appMode = import.meta.env.VITE_APP_MODE
  || (import.meta.env.VITE_MOCK_API === 'true' ? 'mock' : 'api')

export const isMockMode = appMode === 'mock'
export const isEmptyMode = appMode === 'empty'

export const FULL_PERMISSIONS = [
  'user.index', 'user.store', 'user.update', 'user.destroy',
  'post.index', 'post.store', 'post.update', 'post.destroy',
  'category.index', 'category.store', 'category.update', 'category.destroy',
  'role.index', 'role.store', 'role.update', 'role.destroy',
  'permission.index', 'permission.store', 'permission.update', 'permission.destroy',
  'module.index', 'module.store', 'module.update', 'module.destroy',
  'log.index',
]
