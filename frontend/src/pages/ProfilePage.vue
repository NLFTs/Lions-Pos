<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Alert from '@/components/ui/Alert.vue'
import Badge from '@/components/ui/Badge.vue'
import api from '@/lib/api'
import { Loader2, ShieldCheck } from 'lucide-vue-next'

const auth = useAuthStore()
const { toast } = useToast()

// Password change form
const saving = ref(false)
const formError = ref(null)
const form = ref({ currentPassword: '', newPassword: '', confirmNewPassword: '' })

async function changePassword() {
  formError.value = null

  if (!form.value.currentPassword) {
    formError.value = 'Current password is required'
    return
  }
  if (!form.value.newPassword || form.value.newPassword.length < 6) {
    formError.value = 'New password must be at least 6 characters'
    return
  }
  if (form.value.newPassword !== form.value.confirmNewPassword) {
    formError.value = 'New password and confirmation do not match'
    return
  }

  saving.value = true
  try {
    await api.put('/api/v1/users/me/password', {
      currentPassword: form.value.currentPassword,
      newPassword: form.value.newPassword,
    })
    toast.success('Password changed successfully!')
    form.value = { currentPassword: '', newPassword: '', confirmNewPassword: '' }
  } catch (err) {
    formError.value = err.response?.data?.data?.message
      || err.response?.data?.message
      || 'Failed to change password. Check your current password.'
  } finally {
    saving.value = false
  }
}

function getRoleBadges() {
  return auth.user?.roles || []
}
</script>

<template>
  <AppLayout>
    <Alert v-if="formError" variant="destructive" class="mb-4">{{ formError }}</Alert>

    <div class="grid gap-6 max-w-2xl">
      <!-- Profile Info -->
      <Card>
        <CardHeader>
          <CardTitle>Profil Saya</CardTitle>
        </CardHeader>
        <CardContent class="space-y-4">
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <Label class="text-muted-foreground">Username</Label>
              <p class="font-medium mt-1">{{ auth.user?.username || '-' }}</p>
            </div>
            <div>
              <Label class="text-muted-foreground">Full Name</Label>
              <p class="font-medium mt-1">{{ auth.user?.fullname || '-' }}</p>
            </div>
          </div>

          <div>
            <Label class="text-muted-foreground">Roles</Label>
            <div class="flex flex-wrap gap-1 mt-1">
              <Badge v-for="role in getRoleBadges()" :key="role" variant="secondary" class="text-xs">
                <ShieldCheck class="h-3 w-3 mr-1" />
                {{ role }}
              </Badge>
              <span v-if="!getRoleBadges().length" class="text-muted-foreground">-</span>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Change Password -->
      <Card>
        <CardHeader>
          <CardTitle>Ganti Password</CardTitle>
        </CardHeader>
        <CardContent class="space-y-4">
          <div class="space-y-2">
            <Label for="p-current">Current Password <span class="text-destructive">*</span></Label>
            <Input id="p-current" v-model="form.currentPassword" type="password" placeholder="Current password" :disabled="saving" />
          </div>

          <div class="space-y-2">
            <Label for="p-new">New Password <span class="text-destructive">*</span></Label>
            <Input id="p-new" v-model="form.newPassword" type="password" placeholder="New password (min 6 chars)" :disabled="saving" />
          </div>

          <div class="space-y-2">
            <Label for="p-confirm">Confirm New Password <span class="text-destructive">*</span></Label>
            <Input id="p-confirm" v-model="form.confirmNewPassword" type="password" placeholder="Confirm new password" :disabled="saving" />
          </div>

          <div class="flex justify-end pt-2">
            <Button @click="changePassword" :disabled="saving">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              {{ saving ? 'Saving...' : 'Change Password' }}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  </AppLayout>
</template>
