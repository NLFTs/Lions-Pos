import { cn } from '@/lib/utils'

export function avatarVariant({ size = 'default', variant = 'default' } = {}) {
  return cn(
    'relative flex h-10 w-10 shrink-0 overflow-hidden rounded-full',
    size === 'sm' && 'h-8 w-8',
    size === 'lg' && 'h-12 w-12',
  )
}
