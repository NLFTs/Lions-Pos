<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import Alert from '@/components/ui/Alert.vue'
import Table from '@/components/ui/Table.vue'
import TableHeader from '@/components/ui/TableHeader.vue'
import TableBody from '@/components/ui/TableBody.vue'
import TableRow from '@/components/ui/TableRow.vue'
import TableHead from '@/components/ui/TableHead.vue'
import TableCell from '@/components/ui/TableCell.vue'
import api from '@/lib/api'
import { Plus, Pencil, Trash2, Loader2, X, ChevronLeft, ChevronRight, LayoutGrid } from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()

// ─── State ───────────────────────────────────────────────────────────────────
const posts = ref([])
const categories = ref([])
const pagination = ref({ page: 0, size: 10, totalPages: 0, totalElements: 0 })
const loading = ref(false)
const error = ref(null)
const searchQuery = ref('')

const filteredPosts = computed(() => {
  if (!searchQuery.value) return posts.value
  const q = searchQuery.value.toLowerCase()
  return posts.value.filter(p => 
    p.title.toLowerCase().includes(q) || 
    (p.categoryName && p.categoryName.toLowerCase().includes(q)) ||
    (p.createdBy && p.createdBy.toLowerCase().includes(q))
  )
})

// Modal
const showModal = ref(false)
const modalMode = ref('create') // 'create' | 'edit'
const saving = ref(false)
const formError = ref(null)
const form = ref({ id: null, title: '', content: '', status: 'DRAFT', categoryId: null })

// ─── Fetch posts ─────────────────────────────────────────────────────────────
async function fetchPosts(page = 0) {
  loading.value = true
  error.value = null
  try {
    const res = await api.get(`/api/v1/posts?page=${page}&size=${pagination.value.size}`)
    const data = res.data.data
    posts.value = data.content
    pagination.value = {
      ...pagination.value,
      page: data.number,
      totalPages: data.totalPages,
      totalElements: data.totalElements,
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load posts.'
  } finally {
    loading.value = false
  }
}

function updatePostsSize(newSize) {
  pagination.value.size = newSize
  fetchPosts(0)
}

async function fetchCategories() {
  try {
    const res = await api.get('/api/v1/categories')
    categories.value = res.data.data
  } catch (_) {
    // non-critical, category dropdown will just be empty
  }
}

onMounted(() => {
  fetchPosts()
  fetchCategories()
})

// ─── Create / Edit modal ──────────────────────────────────────────────────────
function openCreate() {
  form.value = { id: null, title: '', content: '', status: 'DRAFT', categoryId: null }
  formError.value = null
  modalMode.value = 'create'
  showModal.value = true
}

function openEdit(post) {
  form.value = { id: post.id, title: post.title, content: post.content, status: post.status, categoryId: post.categoryId || null }
  formError.value = null
  modalMode.value = 'edit'
  showModal.value = true
}

async function savePost() {
  formError.value = null
  saving.value = true
  try {
    if (modalMode.value === 'create') {
      await api.post('/api/v1/posts', {
        title: form.value.title,
        content: form.value.content,
        status: form.value.status,
        categoryId: form.value.categoryId || undefined,
      })
      toast.success('Post created!')
    } else {
      await api.put(`/api/v1/posts/${form.value.id}`, {
        title: form.value.title,
        content: form.value.content,
        status: form.value.status,
        categoryId: form.value.categoryId || undefined,
      })
      toast.success('Post updated!')
    }
    showModal.value = false
    fetchPosts(pagination.value.page)
  } catch (err) {
    formError.value = err.response?.data?.data?.message || err.response?.data?.message || 'Failed to save post.'
  } finally {
    saving.value = false
  }
}

// ─── Delete ───────────────────────────────────────────────────────────────────
async function doDelete(post) {
  const ok = await confirm({
    title: 'Delete Post',
    description: `Are you sure you want to delete "${post.title}"? This action cannot be undone.`,
  })
  if (!ok) return

  try {
    await api.delete(`/api/v1/posts/${post.id}`)
    toast.success('Post deleted!')
    fetchPosts(pagination.value.page)
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to delete post.')
  }
}

// ─── Utils ────────────────────────────────────────────────────────────────────
function badgeVariant(status) {
  return status === 'PUBLISHED' ? 'default' : 'secondary'
}

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' })
}
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-6 pb-20">
      <!-- ─── Page Header ─── -->
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Posts</h1>
          <p class="text-muted-foreground text-sm mt-1">
            Manage your blog posts and articles.
          </p>
        </div>
        
        <div class="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-72">
            <DataTableSearch v-model="searchQuery" placeholder="Search posts..." />
          </div>
          <div class="flex items-center gap-2 w-full sm:w-auto">
            <Button variant="outline" class="w-full sm:w-auto hidden sm:flex">
              <LayoutGrid class="mr-2 h-4 w-4" />
              Customize Columns
            </Button>
            <Button v-if="can('post.store')" class="w-full sm:w-auto" @click="openCreate">
              <Plus class="h-4 w-4 mr-2" />
              New Post
            </Button>
          </div>
        </div>
      </div>

      <Alert v-if="error" variant="destructive" class="mb-4">
        {{ error }}
      </Alert>

      <!-- Table card -->
      <Card class="border-zinc-200 shadow-sm overflow-hidden">
        <CardContent class="p-0">
        <!-- Loading -->
        <div v-if="loading" class="flex items-center justify-center py-20">
          <Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
        </div>

        <!-- Empty -->
        <div v-else-if="filteredPosts.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
          <p class="text-sm">No posts found.</p>
          <Button v-if="can('post.store') && !searchQuery" variant="outline" class="mt-3" @click="openCreate">Create your first post</Button>
        </div>

        <!-- Table -->
        <div v-else class="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow class="hover:bg-transparent">
                <TableHead class="w-[200px]">Title</TableHead>
                <TableHead>Category</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created By</TableHead>
                <TableHead>Date</TableHead>
                <TableHead class="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow
                v-for="post in filteredPosts"
                :key="post.id"
              >
                <TableCell class="font-medium max-w-[240px] truncate">{{ post.title }}</TableCell>
                <TableCell>
                  <span v-if="post.categoryName" class="inline-flex items-center rounded-md bg-muted px-2 py-1 text-xs font-medium">{{ post.categoryName }}</span>
                  <span v-else class="text-muted-foreground/50">—</span>
                </TableCell>
                <TableCell>
                  <Badge :variant="badgeVariant(post.status)">{{ post.status }}</Badge>
                </TableCell>
                <TableCell class="text-muted-foreground">{{ post.createdBy || '-' }}</TableCell>
                <TableCell class="text-muted-foreground">{{ formatDate(post.createdAt) }}</TableCell>
                <TableCell class="text-right">
                  <div class="flex justify-end gap-1.5">
                    <Button v-if="can('post.update')" variant="ghost" size="icon" class="h-8 w-8" @click="openEdit(post)">
                      <Pencil class="h-3.5 w-3.5" />
                    </Button>
                    <Button v-if="can('post.delete')" variant="ghost" size="icon" class="h-8 w-8 text-destructive hover:text-destructive" @click="doDelete(post)">
                      <Trash2 class="h-3.5 w-3.5" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>

        <DataTablePagination
          v-if="pagination.totalElements > 0 && !loading"
          :page="pagination.page + 1"
          :page-size="pagination.size"
          :total="pagination.totalElements"
          @update:page="fetchPosts($event - 1)"
          @update:page-size="updatePostsSize($event)"
        />
      </CardContent>
    </Card>
    </div>

    <!-- ─── Create / Edit Modal ─── -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/50" @click="showModal = false" />

        <!-- Dialog -->
        <div class="relative z-10 w-full max-w-md mx-4 bg-card rounded-lg shadow-xl border">
          <div class="flex items-center justify-between p-6 border-b">
            <h3 class="font-semibold text-lg">{{ modalMode === 'create' ? 'New Post' : 'Edit Post' }}</h3>
            <Button variant="ghost" size="icon" @click="showModal = false">
              <X class="h-4 w-4" />
            </Button>
          </div>

          <div class="p-6 space-y-4">
            <Alert v-if="formError" variant="destructive">
              <p class="text-sm">{{ formError }}</p>
            </Alert>

            <div class="space-y-2">
              <Label for="f-title">Title <span class="text-destructive">*</span></Label>
              <Input id="f-title" v-model="form.title" placeholder="Post title" :disabled="saving" />
            </div>

            <div class="space-y-2">
              <Label for="f-content">Content <span class="text-destructive">*</span></Label>
              <textarea
                id="f-content"
                v-model="form.content"
                rows="4"
                :disabled="saving"
                placeholder="Write your content..."
                class="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 resize-none"
              />
            </div>

            <div class="space-y-2">
              <Label for="f-status">Status</Label>
              <select
                id="f-status"
                v-model="form.status"
                :disabled="saving"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
              >
                <option value="DRAFT">DRAFT</option>
                <option value="PUBLISHED">PUBLISHED</option>
              </select>
            </div>

            <div class="space-y-2">
              <Label for="f-category">Category</Label>
              <select
                id="f-category"
                v-model="form.categoryId"
                :disabled="saving"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
              >
                <option :value="null">— No category —</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
              </select>
            </div>
          </div>

          <div class="flex justify-end gap-3 p-6 border-t">
            <Button variant="outline" @click="showModal = false" :disabled="saving">Cancel</Button>
            <Button @click="savePost" :disabled="saving">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              {{ modalMode === 'create' ? 'Create' : 'Save Changes' }}
            </Button>
          </div>
        </div>
      </div>
    </Teleport>
  </AppLayout>
</template>
