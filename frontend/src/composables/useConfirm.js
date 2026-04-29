import { useConfirmStore } from '@/stores/confirm'

/**
 * Composable for showing a reusable confirmation dialog.
 *
 * Usage:
 *   const { confirm } = useConfirm()
 *   const ok = await confirm({ title: 'Delete?', description: 'This cannot be undone.' })
 *   if (ok) await api.delete(`/items/${id}`)
 */
export function useConfirm() {
  const store = useConfirmStore()
  return { confirm: store.confirm }
}
