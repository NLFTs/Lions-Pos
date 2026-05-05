import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

export const routes = [
  // Root redirect
  {
    path: '/',
    name: 'landing',
    component: () => import('@/pages/LandingPage.vue'),
  },

  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/LoginPage.vue'), 
    meta: { guest: true },
  },

  // Authenticated area — all under /dashboard
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('@/pages/DashboardPage.vue'),
    meta: {
      requiresAuth: true,
      pageTitle: 'Dashboard',
      pageSubtitle: 'Ringkasan data dan aktivitas aplikasi.',
    },
  },
  {
    path: '/dashboard/products',
    name: 'products',
    component: () => import('@/pages/ProductPage.vue'),
    meta: {
      requiresAuth: true,
      permission: 'post.index',
      pageTitle: 'Manajemen Produk',
      pageSubtitle: 'Kelola produk, status, dan kategori.',
    },
  },
  {
    path: '/dashboard/categories',
    name: 'categories',
    component: () => import('@/pages/CategoriesPage.vue'),
    meta: {
      requiresAuth: true,
      permission: 'category.index',
      pageTitle: 'Manajemen Kategori',
      pageSubtitle: 'Atur kategori untuk klasifikasi konten post.',
    },
  },
  {
    path: '/dashboard/stock-mutations',
    name: 'stock-mutations',
    component: () => import('@/pages/StockMutationsPage.vue'),
    meta: {
      requiresAuth: true,
      permission: null,
      pageTitle: 'Mutasi Stok',
      pageSubtitle: 'Pantau riwayat pergerakan stok barang.',
    },
  },
  {
    path: '/dashboard/partners',
    name: 'partners',
    component: () => import('@/pages/PartnersPage.vue'),
    meta: {
      requiresAuth: true,
      permission: null,
      pageTitle: 'Manajemen Partner',
      pageSubtitle: 'Kelola data supplier dan customer.',
    },
  },
  {
    path: '/dashboard/locations',
    name: 'locations',
    component: () => import('@/pages/LocationsPage.vue'),
    meta: {
      requiresAuth: true,
      permission: null,
      pageTitle: 'Manajemen Lokasi',
      pageSubtitle: 'Atur data gudang dan cabang.',
    },
  },
  {
    path: '/dashboard/roles',
    name: 'roles',
    component: () => import('@/pages/RolesPage.vue'),
    meta: {
      requiresAuth: true,
      permission: 'role.index',
      pageTitle: 'Manajemen Role',
      pageSubtitle: 'Kelola role dan matriks hak akses pengguna.',
    },
  },
  {
    path: '/dashboard/permissions',
    name: 'permissions',
    component: () => import('@/pages/PermissionsPage.vue'),
    meta: {
      requiresAuth: true,
      permission: 'permission.index',
      pageTitle: 'Manajemen Permission',
      pageSubtitle: 'Kelola slug permission yang dipakai di otorisasi.',
    },
  },
  {
    path: '/dashboard/modules',
    name: 'modules',
    component: () => import('@/pages/ModulesPage.vue'),
    meta: {
      requiresAuth: true,
      permission: 'module.index',
      pageTitle: 'Manajemen Modul',
      pageSubtitle: 'Atur grup modul sebagai header untuk permission.',
    },
  },
  {
    path: '/dashboard/users',
    name: 'users',
    component: () => import('@/pages/UsersPage.vue'),
    meta: {
      requiresAuth: true,
      permission: 'user.index',
      pageTitle: 'Manajemen User',
      pageSubtitle: 'Kelola data user dan assign role.',
    },
  },
  {
    path: '/dashboard/profile',
    name: 'profile',
    component: () => import('@/pages/ProfilePage.vue'),
    meta: {
      requiresAuth: true,
      pageTitle: 'Profil Saya',
      pageSubtitle: 'Lihat profil dan ganti password.',
    },
  },
  {
    path: '/dashboard/logs',
    name: 'logs',
    component: () => import('@/pages/LogsPage.vue'),
    meta: {
      requiresAuth: true,
      permission: 'log.index',
      pageTitle: 'Audit Logs',
      pageSubtitle: 'Log aktivitas HTTP pada sistem.',
    },
  },
  {
    path: '/dashboard/kasir',
    name: 'kasir',
    component: () => import('@/pages/KasirPage.vue'),
    meta: {
      requiresAuth: true,
      pageTitle: 'Kasir',
      pageSubtitle: 'Sistem Point of Sale',
    },
  },

  {
    path: '/about',
    name: 'about',
    component: () => import('@/pages/AboutPage.vue'),
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login',
  },
]

const isProd = import.meta.env.PROD
const base = isProd ? '/_/' : '/'


export const setupRouterGuards = (router) => {
  router.beforeEach((to, from) => {
    const auth = useAuthStore()


    if (to.meta.requiresAuth && !auth.isAuthenticated) {
      return { name: 'login' }
    }

    if (to.meta.permission && !auth.permissions.includes(to.meta.permission)) {
      return { name: 'dashboard' }
    }

    if (to.meta.guest && auth.isAuthenticated) {
  return { name: 'dashboard' }
}

  })
}

const router = createRouter({
  history: createWebHistory(base),
  routes,
})

setupRouterGuards(router)

export default router