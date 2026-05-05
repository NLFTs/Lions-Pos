import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'

/**
 * Composable for checking user permissions.
 *
 * Usage:
 *   const { can, canAny, canAll } = usePermission()
 *   if (can('post.store')) { ... }
 *   v-if="can('post.update')"
 */
export function usePermission() {
  const { permissions } = storeToRefs(useAuthStore())

  /**
   * Check if the user has a single permission slug.
   * @param {string} slug  e.g. 'post.index'
   */
  function can(slug) {
    return permissions.value.includes(slug)
  }

  /**
   * Check if the user has ANY of the given permission slugs.
   * @param {...string} slugs
   */
  function canAny(...slugs) {
    return slugs.some((slug) => permissions.value.includes(slug))
  }

  /**
   * Check if the user has ALL of the given permission slugs.
   * @param {...string} slugs
   */
  function canAll(...slugs) {
    return slugs.every((slug) => permissions.value.includes(slug))
  }

  return { can, canAny, canAll }
}
