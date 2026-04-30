import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
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
    path: '/dashboard/posts',
    name: 'posts',
    component: () => import('@/pages/PostsPage.vue'),
    meta: {
      requiresAuth: true,
      permission: 'post.index',
      pageTitle: 'Manajemen Post',
      pageSubtitle: 'Kelola konten post, status publish, dan kategori.',
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

  // Catch-all
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login',
  },
]

// Dev mode: base '/' (no prefix)
// Prod build: base '/_/' (Spring Boot prefix)
const isProd = import.meta.env.PROD
const base = isProd ? '/_/' : '/'

const router = createRouter({
  history: createWebHistory(base),
  routes,
})

router.beforeEach((to, _from) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login' }
  }

  if (to.meta.guest && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }

  if (to.meta.permission && !auth.permissions.includes(to.meta.permission)) {
    return { name: 'dashboard' }
  }
})

export default router
