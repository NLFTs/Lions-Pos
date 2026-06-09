<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import AppLayout from '@/components/AppLayout.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import api from '@/lib/api'
import { User, Paintbrush, ShieldCheck, Pencil, Eye, EyeOff, Loader2, Check, X, Upload } from 'lucide-vue-next'

const auth = useAuthStore()
const { toast } = useToast()
const { confirm } = useConfirm()

// ─── Active Tab ───────────────────────────────────────────────────────────────
const activeTab = ref('general')
const tabs = [
  { key: 'general', label: 'General',  icon: User },
  { key: 'style',   label: 'Style',    icon: Paintbrush },
  { key: 'security',label: 'Security', icon: ShieldCheck },
]

// ─── Avatar State ─────────────────────────────────────────────────────────────
const uploadingAvatar = ref(false)
const avatarInputRef = ref(null)

const avatarUrl = computed(() => auth.user?.avatar || null)
const displayName = computed(() => auth.user?.fullname || auth.user?.username || 'User')
const userInitial = computed(() => displayName.value.charAt(0).toUpperCase())

const AVATAR_PALETTE = [
  { bg: '#dbeafe', color: '#1d4ed8' },
  { bg: '#dcfce7', color: '#15803d' },
  { bg: '#fef9c3', color: '#a16207' },
  { bg: '#fce7f3', color: '#be185d' },
  { bg: '#ede9fe', color: '#6d28d9' },
  { bg: '#ffedd5', color: '#c2410c' },
  { bg: '#cffafe', color: '#0e7490' },
  { bg: '#f1f5f9', color: '#475569' },
]
const avatarStyle = computed(() => {
  const hash = displayName.value.split('').reduce((a, c) => a + c.charCodeAt(0), 0)
  const p = AVATAR_PALETTE[hash % AVATAR_PALETTE.length]
  return { backgroundColor: p.bg, color: p.color }
})

async function onAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) { toast.error('Ukuran file maksimal 2MB.'); return }

  uploadingAvatar.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await api.post('/api/v1/upload/avatar', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    const url = res.data?.data?.url
    await api.put(`/api/v1/users/${auth.user.id}`, { avatar: url })
    await auth.fetchMe()
    toast.success('Avatar berhasil diperbarui!')
  } catch (err) {
    toast.error(err.response?.data?.data?.message || 'Gagal mengunggah avatar.')
  } finally {
    uploadingAvatar.value = false
    e.target.value = ''
  }
}

// ─── Inline Edit State ────────────────────────────────────────────────────────
// Each field: { editing, value, saving }
const nameEdit  = ref({ editing: false, value: '', saving: false })
const emailEdit = ref({ editing: false, value: '', saving: false })

function startEdit(field) {
  if (field === 'name') {
    nameEdit.value = { editing: true, value: auth.user?.fullname || '', saving: false }
  } else {
    emailEdit.value = { editing: true, value: auth.user?.email || '', saving: false }
  }
}

function cancelEdit(field) {
  if (field === 'name') nameEdit.value.editing = false
  else emailEdit.value.editing = false
}

async function saveEdit(field) {
  const edit = field === 'name' ? nameEdit.value : emailEdit.value
  if (!edit.value.trim()) return
  edit.saving = true
  try {
    const payload = field === 'name'
      ? { fullname: edit.value.trim() }
      : { email: edit.value.trim() }
    await api.put(`/api/v1/users/${auth.user.id}`, payload)
    await auth.fetchMe()
    edit.editing = false
    toast.success(`${field === 'name' ? 'Nama' : 'Email'} berhasil diperbarui!`)
  } catch (err) {
    toast.error(err.response?.data?.data?.message || 'Gagal menyimpan.')
  } finally {
    edit.saving = false
  }
}

// ─── Password Change ──────────────────────────────────────────────────────────
const pwEdit = ref({ editing: false, saving: false })
const pwForm = ref({ current: '', newPw: '', confirm: '' })
const pwError = ref(null)
const showCurrent = ref(false)
const showNew = ref(false)
const showConfirm = ref(false)

function startPwEdit() {
  pwEdit.value = { editing: true, saving: false }
  pwForm.value = { current: '', newPw: '', confirm: '' }
  pwError.value = null
}

function cancelPwEdit() {
  pwEdit.value.editing = false
  pwError.value = null
}

async function savePw() {
  pwError.value = null
  if (!pwForm.value.current) { pwError.value = 'Password saat ini wajib diisi.'; return }
  if (!pwForm.value.newPw || pwForm.value.newPw.length < 6) { pwError.value = 'Password baru minimal 6 karakter.'; return }
  if (pwForm.value.newPw !== pwForm.value.confirm) { pwError.value = 'Konfirmasi password tidak cocok.'; return }

  pwEdit.value.saving = true
  try {
    await api.put('/api/v1/users/me/password', {
      currentPassword: pwForm.value.current,
      newPassword: pwForm.value.newPw,
    })
    toast.success('Password berhasil diubah!')
    pwEdit.value.editing = false
  } catch (err) {
    pwError.value = err.response?.data?.data?.message || err.response?.data?.message || 'Password saat ini salah.'
  } finally {
    pwEdit.value.saving = false
  }
}

// ─── Delete Account ───────────────────────────────────────────────────────────
const deletingAccount = ref(false)

async function deleteAccount() {
  const ok = await confirm({
    title: 'Hapus Akun',
    description: 'Tindakan ini tidak dapat dibatalkan. Seluruh data akun Anda akan dihapus permanen. Ketik konfirmasi untuk melanjutkan.',
    confirmLabel: 'Hapus Akun',
    cancelLabel: 'Batal',
    variant: 'destructive',
  })
  if (!ok) return

  deletingAccount.value = true
  try {
    await api.delete(`/api/v1/users/${auth.user.id}`)
    toast.success('Akun berhasil dihapus.')
    await auth.logout()
  } catch (err) {
    toast.error(err.response?.data?.data?.message || 'Gagal menghapus akun.')
  } finally {
    deletingAccount.value = false
  }
}
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-8 p-4 sm:p-6 pb-20 max-w-4xl">

      <!-- Page Title -->
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-foreground">Settings</h1>
      </div>

      <div class="flex flex-col sm:flex-row gap-8 items-start">

        <!-- ─── Left Nav ─── -->
        <nav class="w-full sm:w-44 shrink-0 flex sm:flex-col gap-1">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            @click="activeTab = tab.key"
            class="flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors w-full text-left"
            :class="activeTab === tab.key
              ? 'bg-zinc-200 dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100'
              : 'text-zinc-500 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-900 hover:text-zinc-900 dark:hover:text-zinc-100'"
          >
            <component :is="tab.icon" class="w-4 h-4 shrink-0" />
            {{ tab.label }}
          </button>
        </nav>

        <!-- ─── Right Content ─── -->
        <div class="flex-1 min-w-0">

          <!-- ══ GENERAL TAB ══ -->
          <div v-if="activeTab === 'general'" class="rounded-xl border border-zinc-200 dark:border-zinc-800 overflow-hidden divide-y divide-zinc-200 dark:divide-zinc-800">

            <!-- Avatar Row -->
            <div class="flex items-center justify-between px-5 py-4 bg-card hover:bg-zinc-50/50 dark:hover:bg-zinc-900/30 transition-colors">
              <div>
                <p class="text-sm font-semibold text-foreground">Avatar</p>
                <p class="text-xs text-muted-foreground mt-0.5">Upload your public avatar</p>
              </div>
              <div class="flex items-center gap-3">
                <!-- Avatar Preview -->
                <div class="relative group cursor-pointer" @click="avatarInputRef?.click()">
                  <div class="w-12 h-12 rounded-full overflow-hidden border-2 border-zinc-200 dark:border-zinc-700 relative">
                    <img v-if="avatarUrl" :src="avatarUrl" :alt="displayName" class="w-full h-full object-cover" />
                    <div v-else class="w-full h-full flex items-center justify-center text-lg font-bold select-none" :style="avatarStyle">
                      {{ userInitial }}
                    </div>
                    <!-- Upload overlay -->
                    <div class="absolute inset-0 bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity rounded-full">
                      <Loader2 v-if="uploadingAvatar" class="w-4 h-4 text-white animate-spin" />
                      <Upload v-else class="w-4 h-4 text-white" />
                    </div>
                  </div>
                  <input ref="avatarInputRef" type="file" accept="image/*" class="hidden" @change="onAvatarChange" />
                </div>
              </div>
            </div>

            <!-- Name Row -->
            <div class="px-5 py-4 bg-card">
              <div v-if="!nameEdit.editing" class="flex items-center justify-between">
                <div>
                  <p class="text-sm font-semibold text-foreground">Name</p>
                  <p class="text-xs text-muted-foreground mt-0.5">Change your name</p>
                </div>
                <div class="flex items-center gap-3">
                  <span class="text-sm text-zinc-500 dark:text-zinc-400">{{ auth.user?.fullname || '—' }}</span>
                  <button @click="startEdit('name')" class="p-1.5 rounded-md hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors">
                    <Pencil class="w-4 h-4" />
                  </button>
                </div>
              </div>

              <!-- Inline edit Name -->
              <div v-else class="flex flex-col gap-3">
                <div>
                  <p class="text-sm font-semibold text-foreground">Name</p>
                  <p class="text-xs text-muted-foreground mt-0.5">Change your name</p>
                </div>
                <div class="flex items-center gap-2">
                  <Input
                    v-model="nameEdit.value"
                    placeholder="Nairha"
                    class="h-9 text-sm flex-1"
                    :disabled="nameEdit.saving"
                    @keyup.enter="saveEdit('name')"
                    @keyup.esc="cancelEdit('name')"
                    autofocus
                  />
                  <button @click="saveEdit('name')" :disabled="nameEdit.saving" class="p-2 rounded-md bg-primary text-primary-foreground hover:bg-primary/90 transition-colors disabled:opacity-50">
                    <Loader2 v-if="nameEdit.saving" class="w-4 h-4 animate-spin" />
                    <Check v-else class="w-4 h-4" />
                  </button>
                  <button @click="cancelEdit('name')" class="p-2 rounded-md hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-400 transition-colors">
                    <X class="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>

            <!-- Email Row -->
            <div class="px-5 py-4 bg-card">
              <div v-if="!emailEdit.editing" class="flex items-center justify-between">
                <div>
                  <p class="text-sm font-semibold text-foreground">Email</p>
                  <p class="text-xs text-muted-foreground mt-0.5">Change your email address</p>
                </div>
                <div class="flex items-center gap-3">
                  <span class="text-sm text-zinc-500 dark:text-zinc-400">{{ auth.user?.email || auth.user?.username || '—' }}</span>
                  <button @click="startEdit('email')" class="p-1.5 rounded-md hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors">
                    <Pencil class="w-4 h-4" />
                  </button>
                </div>
              </div>

              <!-- Inline edit Email -->
              <div v-else class="flex flex-col gap-3">
                <div>
                  <p class="text-sm font-semibold text-foreground">Email</p>
                  <p class="text-xs text-muted-foreground mt-0.5">Change your email address</p>
                </div>
                <div class="flex items-center gap-2">
                  <Input
                    v-model="emailEdit.value"
                    type="email"
                    placeholder="nairha@nlfts.dev"
                    class="h-9 text-sm flex-1"
                    :disabled="emailEdit.saving"
                    @keyup.enter="saveEdit('email')"
                    @keyup.esc="cancelEdit('email')"
                    autofocus
                  />
                  <button @click="saveEdit('email')" :disabled="emailEdit.saving" class="p-2 rounded-md bg-primary text-primary-foreground hover:bg-primary/90 transition-colors disabled:opacity-50">
                    <Loader2 v-if="emailEdit.saving" class="w-4 h-4 animate-spin" />
                    <Check v-else class="w-4 h-4" />
                  </button>
                  <button @click="cancelEdit('email')" class="p-2 rounded-md hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-400 transition-colors">
                    <X class="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>

            <!-- Password Row -->
            <div class="px-5 py-4 bg-card">
              <div v-if="!pwEdit.editing" class="flex items-center justify-between">
                <div>
                  <p class="text-sm font-semibold text-foreground">Password</p>
                  <p class="text-xs text-muted-foreground mt-0.5">Change your password</p>
                </div>
                <button @click="startPwEdit" class="p-1.5 rounded-md hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors">
                  <Pencil class="w-4 h-4" />
                </button>
              </div>

              <!-- Inline edit Password -->
              <div v-else class="flex flex-col gap-3">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-semibold text-foreground">Password</p>
                    <p class="text-xs text-muted-foreground mt-0.5">Change your password</p>
                  </div>
                  <button @click="cancelPwEdit" class="p-1.5 rounded-md hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-400 transition-colors">
                    <X class="w-4 h-4" />
                  </button>
                </div>

                <p v-if="pwError" class="text-xs text-destructive font-semibold bg-destructive/10 px-3 py-2 rounded-lg">{{ pwError }}</p>

                <div class="flex flex-col gap-2">
                  <!-- Current password -->
                  <div class="relative">
                    <Input
                      v-model="pwForm.current"
                      :type="showCurrent ? 'text' : 'password'"
                      placeholder="Password saat ini"
                      class="h-9 text-sm pr-10"
                      :disabled="pwEdit.saving"
                    />
                    <button type="button" @click="showCurrent = !showCurrent" class="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-600 dark:hover:text-zinc-300">
                      <EyeOff v-if="showCurrent" class="w-4 h-4" />
                      <Eye v-else class="w-4 h-4" />
                    </button>
                  </div>
                  <!-- New password -->
                  <div class="relative">
                    <Input
                      v-model="pwForm.newPw"
                      :type="showNew ? 'text' : 'password'"
                      placeholder="Password baru (min 6 karakter)"
                      class="h-9 text-sm pr-10"
                      :disabled="pwEdit.saving"
                    />
                    <button type="button" @click="showNew = !showNew" class="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-600 dark:hover:text-zinc-300">
                      <EyeOff v-if="showNew" class="w-4 h-4" />
                      <Eye v-else class="w-4 h-4" />
                    </button>
                  </div>
                  <!-- Confirm password -->
                  <div class="relative">
                    <Input
                      v-model="pwForm.confirm"
                      :type="showConfirm ? 'text' : 'password'"
                      placeholder="Konfirmasi password baru"
                      class="h-9 text-sm pr-10"
                      :disabled="pwEdit.saving"
                      @keyup.enter="savePw"
                    />
                    <button type="button" @click="showConfirm = !showConfirm" class="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-600 dark:hover:text-zinc-300">
                      <EyeOff v-if="showConfirm" class="w-4 h-4" />
                      <Eye v-else class="w-4 h-4" />
                    </button>
                  </div>
                </div>

                <div class="flex justify-end">
                  <button @click="savePw" :disabled="pwEdit.saving" class="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-primary-foreground text-sm font-semibold hover:bg-primary/90 transition-colors disabled:opacity-50">
                    <Loader2 v-if="pwEdit.saving" class="w-4 h-4 animate-spin" />
                    <span>{{ pwEdit.saving ? 'Menyimpan...' : 'Simpan Password' }}</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- Delete Account Row -->
            <div class="flex items-center justify-between px-5 py-4 bg-destructive/5 dark:bg-destructive/10">
              <div>
                <p class="text-sm font-semibold text-foreground">Delete account</p>
                <p class="text-xs text-destructive mt-0.5">Permanently delete your account</p>
              </div>
              <button
                @click="deleteAccount"
                :disabled="deletingAccount"
                class="flex items-center gap-2 px-4 py-2 rounded-lg bg-destructive text-white text-sm font-semibold hover:bg-destructive/90 transition-colors disabled:opacity-50"
              >
                <Loader2 v-if="deletingAccount" class="w-4 h-4 animate-spin" />
                <span>Delete</span>
              </button>
            </div>

          </div>

          <!-- ══ STYLE TAB ══ -->
          <div v-else-if="activeTab === 'style'" class="rounded-xl border border-zinc-200 dark:border-zinc-800 overflow-hidden divide-y divide-zinc-200 dark:divide-zinc-800">
            <div class="px-5 py-4 bg-card">
              <p class="text-sm font-semibold text-foreground mb-4">Tema Warna</p>
              <div class="flex flex-wrap gap-2">
                <!-- Theme color pills — reuse from AppLayout logic via import -->
                <button
                  v-for="theme in [
                    { key: 'zinc',   color: '#71717a', label: 'Zinc' },
                    { key: 'rose',   color: '#f43f5e', label: 'Rose' },
                    { key: 'blue',   color: '#3b82f6', label: 'Blue' },
                    { key: 'green',  color: '#22c55e', label: 'Green' },
                    { key: 'orange', color: '#f97316', label: 'Orange' },
                    { key: 'violet', color: '#8b5cf6', label: 'Violet' },
                  ]"
                  :key="theme.key"
                  @click="$themeStore?.setTheme?.(theme.key)"
                  class="flex items-center gap-2 px-3 py-1.5 rounded-full border text-xs font-medium transition-colors border-zinc-200 dark:border-zinc-700 hover:border-zinc-400 dark:hover:border-zinc-500 bg-card"
                >
                  <span class="w-3 h-3 rounded-full" :style="{ backgroundColor: theme.color }" />
                  {{ theme.label }}
                </button>
              </div>
            </div>

            <div class="px-5 py-4 bg-card">
              <p class="text-sm font-semibold text-foreground mb-3">Mode Tampilan</p>
              <div class="flex items-center gap-2">
                <button
                  v-for="mode in [{ key: 'light', label: 'Terang' }, { key: 'dark', label: 'Gelap' }, { key: 'system', label: 'Sistem' }]"
                  :key="mode.key"
                  class="px-4 py-2 rounded-lg border border-zinc-200 dark:border-zinc-700 text-sm font-medium hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors"
                >
                  {{ mode.label }}
                </button>
              </div>
            </div>
          </div>

          <!-- ══ SECURITY TAB ══ -->
          <div v-else-if="activeTab === 'security'" class="rounded-xl border border-zinc-200 dark:border-zinc-800 overflow-hidden divide-y divide-zinc-200 dark:divide-zinc-800">

            <!-- User info summary -->
            <div class="px-5 py-4 bg-card flex items-center gap-4">
              <div class="w-12 h-12 rounded-full overflow-hidden border-2 border-zinc-200 dark:border-zinc-700 shrink-0">
                <img v-if="avatarUrl" :src="avatarUrl" :alt="displayName" class="w-full h-full object-cover" />
                <div v-else class="w-full h-full flex items-center justify-center text-lg font-bold" :style="avatarStyle">{{ userInitial }}</div>
              </div>
              <div>
                <p class="font-semibold text-sm text-foreground">{{ displayName }}</p>
                <p class="text-xs text-muted-foreground">{{ auth.user?.username }}</p>
              </div>
            </div>

            <!-- Roles -->
            <div class="px-5 py-4 bg-card">
              <p class="text-sm font-semibold text-foreground mb-2">Roles</p>
              <div class="flex flex-wrap gap-1.5">
                <span
                  v-for="role in (auth.user?.roles || [])"
                  :key="typeof role === 'string' ? role : role.name"
                  class="inline-flex items-center gap-1 text-xs font-medium px-2.5 py-1 rounded-full bg-primary/10 text-primary border border-primary/20"
                >
                  <ShieldCheck class="w-3 h-3" />
                  {{ typeof role === 'string' ? role : (role.name || role.slug) }}
                </span>
                <span v-if="!(auth.user?.roles?.length)" class="text-xs text-muted-foreground">Tidak ada role</span>
              </div>
            </div>

            <!-- Account Info -->
            <div class="px-5 py-4 bg-card">
              <p class="text-sm font-semibold text-foreground mb-3">Informasi Akun</p>
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
                <div class="rounded-lg bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-100 dark:border-zinc-800 p-3">
                  <p class="text-[10px] font-semibold text-muted-foreground uppercase tracking-wide">Username</p>
                  <p class="font-medium text-foreground mt-1">{{ auth.user?.username || '—' }}</p>
                </div>
                <div class="rounded-lg bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-100 dark:border-zinc-800 p-3">
                  <p class="text-[10px] font-semibold text-muted-foreground uppercase tracking-wide">ID Akun</p>
                  <p class="font-mono font-medium text-foreground mt-1">{{ auth.user?.id || '—' }}</p>
                </div>
                <div class="rounded-lg bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-100 dark:border-zinc-800 p-3">
                  <p class="text-[10px] font-semibold text-muted-foreground uppercase tracking-wide">Cabang</p>
                  <p class="font-medium text-foreground mt-1">{{ auth.user?.branchName || '—' }}</p>
                </div>
                <div class="rounded-lg bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-100 dark:border-zinc-800 p-3">
                  <p class="text-[10px] font-semibold text-muted-foreground uppercase tracking-wide">Plan</p>
                  <p class="font-medium text-foreground mt-1 capitalize">{{ auth.user?.plan || 'basic' }}</p>
                </div>
              </div>
            </div>

          </div>

        </div>
      </div>
    </div>
  </AppLayout>
</template>
