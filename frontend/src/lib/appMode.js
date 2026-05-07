export const appMode = import.meta.env.VITE_APP_MODE
  || (import.meta.env.VITE_MOCK_API === 'true' ? 'mock' : 'api')

export const isMockMode = appMode === 'mock'
export const isEmptyMode = appMode === 'empty'

export const FULL_PERMISSIONS = [
  'user.index', 'user.store', 'user.update', 'user.delete', 'user.destroy',
  'produk.index', 'produk.store', 'produk.update', 'produk.delete', 'produk.destroy',
  'category.index', 'category.store', 'category.update', 'category.delete', 'category.destroy',
  'stock-mutation.index', 'stock-mutation.store', 'stock-mutation.update', 'stock-mutation.delete', 'stock-mutation.destroy',
  'partner.index', 'partner.store', 'partner.update', 'partner.delete', 'partner.destroy',
  'location.index', 'location.store', 'location.update', 'location.delete', 'location.destroy',
  'voucher.index', 'voucher.store', 'voucher.update', 'voucher.delete', 'voucher.destroy',
  'role.index', 'role.store', 'role.update', 'role.delete', 'role.destroy',
  'permission.index', 'permission.store', 'permission.update', 'permission.delete', 'permission.destroy',
  'module.index', 'module.store', 'module.update', 'module.delete', 'module.destroy',
  'supplier.index', 'supplier.store', 'supplier.update', 'supplier.delete', 'supplier.destroy',
  'order.index', 'order.store', 'order.update', 'order.delete', 'order.destroy',
  'purchase-order.index', 'purchase-order.store', 'purchase-order.update', 'purchase-order.delete', 'purchase-order.destroy',
  'transfer-request.index', 'transfer-request.store', 'transfer-request.update', 'transfer-request.delete', 'transfer-request.destroy',
  'stock-opname.index', 'stock-opname.store', 'stock-opname.update', 'stock-opname.delete', 'stock-opname.destroy',
  'log.index',
]
