import { useToastStore } from '@/stores/toast'

/**
 * Composable for showing toast notifications.
 *
 * Usage:
 *   const { toast } = useToast()
 *   toast.success('Item created!')
 *   toast.error('Something went wrong')
 *   toast.info('Loading data…')
 */
export function useToast() {
  const store = useToastStore()
  return { toast: { success: store.success, error: store.error, info: store.info } }
}
