# DataTable Component - shadcn-vue Standards

## Overview

Sistem DataTable di Spravel dibangun mengikuti prinsip **shadcn-vue**:
- **Open Code**: Kode komponen terbuka untuk modifikasi
- **Composition**: Interface yang konsisten dan dapat digabungkan
- **Beautiful Defaults**: Desain default yang bersih dan modern

---

## Komponen yang Tersedia

### 1. DataTable (Main Component)

File: `src/components/ui/DataTable.vue`

Komponen utama untuk menampilkan data tabel dengan fitur lengkap.

#### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `data` | Array | required | Data array untuk ditampilkan |
| `columns` | Array | required | Definisi kolom (key, label, sortable, width) |
| `page` | Number | 1 | Halaman aktif (1-indexed) |
| `pageSize` | Number | 10 | Jumlah baris per halaman |
| `total` | Number | 0 | Total data untuk pagination |
| `loading` | Boolean | false | Loading state |
| `emptyMessage` | String | 'No data...' | Pesan saat data kosong |
| `sortable` | Boolean | true | Enable column sorting |
| `selectable` | Boolean | false | Enable row selection (checkbox) |
| `rowKey` | String | 'id' | Field untuk row key |

#### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `update:page` | Number | Page berubah |
| `update:pageSize` | Number | Page size berubah |
| `sort` | { field, order } | Column sort diklik |
| `selection-change` | Array | Selection berubah |

#### Slots

| Slot | Props | Description |
|------|-------|-------------|
| `cell-{key}` | { item, value, index } | Custom cell rendering per kolom |
| `actions` | { item } | Kolom actions (kanan) |
| `select-all` | - | Header checkbox |
| `select` | { item } | Row checkbox |
| `empty-icon` | - | Icon untuk empty state |
| `empty-action` | - | Action button untuk empty state |

#### Contoh Penggunaan

```vue
<template>
  <DataTable
    :data="users"
    :columns="columns"
    :page="page"
    :page-size="pageSize"
    :total="total"
    :loading="loading"
    @update:page="page = $event"
    @update:page-size="pageSize = $event"
  >
    <!-- Custom cell rendering -->
    <template #cell-name="{ item }">
      <div class="flex items-center gap-2">
        <Avatar :src="item.avatar" />
        <span>{{ item.name }}</span>
      </div>
    </template>

    <!-- Actions column -->
    <template #actions="{ item }">
      <Button variant="ghost" size="icon" @click="edit(item)">
        <Pencil class="h-4 w-4" />
      </Button>
    </template>

    <!-- Empty state -->
    <template #empty-icon>
      <UserCog class="h-8 w-8" />
    </template>
    <template #empty-action>
      <Button @click="openCreate">Create your first user</Button>
    </template>
  </DataTable>
</template>

<script setup>
const columns = [
  { key: 'name', label: 'Name', sortable: true, width: '200px' },
  { key: 'email', label: 'Email', sortable: true },
  { key: 'role', label: 'Role', sortable: false },
  { key: 'createdAt', label: 'Joined', sortable: true, width: '150px' },
]
</script>
```

---

### 2. DataTableSearch

File: `src/components/ui/DataTableSearch.vue`

Search input dengan debounce untuk filtering data.

#### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `modelValue` | String | '' | Search query (v-model) |
| `placeholder` | String | 'Search...' | Placeholder text |
| `debounce` | Number | 300 | Debounce delay (ms) |
| `class` | String | '' | Custom class |

#### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `update:modelValue` | String | Value berubah |
| `search` | String | Search query (setelah debounce) |

#### Contoh

```vue
<DataTableSearch
  v-model="searchQuery"
  placeholder="Search users..."
  :debounce="500"
  class="w-full max-w-sm"
/>
```

---

### 3. DataTableActions

File: `src/components/ui/DataTableActions.vue`

Dropdown menu untuk row actions (three dots menu).

#### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `label` | String | 'Actions' | Accessibility label |
| `items` | Array | required | Menu items |
| `class` | String | '' | Custom class |

#### Item Structure

```javascript
{
  label: 'Edit',
  icon: Pencil,          // Lucide icon component
  onClick: () => {...},  // Click handler
  variant: 'destructive', // 'default' | 'destructive'
  disabled: false,
  separator: false,      // Show separator above
}
```

#### Contoh

```vue
<DataTableActions
  :items="[
    {
      label: 'Edit',
      icon: Pencil,
      onClick: () => openEdit(item),
    },
    {
      label: 'Delete',
      icon: Trash2,
      variant: 'destructive',
      onClick: () => doDelete(item),
    },
  ]"
/>
```

---

## Column Definition

Struktur definisi kolom:

```javascript
{
  key: 'username',       // Field name di data
  label: 'Name',         // Header text
  sortable: true,        // Enable sorting
  width: '200px',        // Column width (optional)
  minWidth: '150px',     // Min width (optional)
}
```

---

## Pagination

DataTable sudah include pagination component dengan fitur:
- Rows per page selector (10, 20, 30, 50, 100)
- Page info ("Page 1 of 10")
- First/Previous/Next/Last buttons
- Page number buttons (max 5 visible)
- Responsive layout

### Server-side Pagination

```vue
<template>
  <DataTable
    :data="users"
    :page="page"
    :page-size="pageSize"
    :total="total"
    @update:page="handlePageChange"
    @update:page-size="handlePageSizeChange"
  />
</template>

<script setup>
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

watch([page, pageSize], async () => {
  const response = await api.get(`/users?page=${page.value - 1}&size=${pageSize.value}`)
  users.value = response.data.content
  total.value = response.data.totalElements
})
</script>
```

---

## Sorting

### Client-side Sorting

DataTable handle sorting otomatis jika `sortable={true}`.

### Server-side Sorting

```vue
<DataTable
  :columns="columns"
  :data="data"
  :sortable="true"
  @sort="handleSort"
/>

<script setup>
async function handleSort({ field, order }) {
  const response = await api.get(`/users?sort=${field},${order}`)
  data.value = response.data.content
}
</script>
```

---

## Styling

### Custom Classes

Semua komponen menerima `class` prop untuk custom styling:

```vue
<DataTable class="shadow-lg" />
<DataTableSearch class="w-64" />
<DataTableActions class="hover:bg-muted" />
```

### Dark Mode

Semua komponen sudah support dark mode menggunakan CSS variables:
- `bg-card`, `text-card-foreground`
- `border-border`
- `text-muted-foreground`
- `bg-muted`, `hover:bg-muted/30`

---

## Best Practices

### 1. Column Order

Urutkan kolom dari yang paling penting ke kiri:
```javascript
const columns = [
  { key: 'name', label: 'Name', width: '250px' },    // Most important
  { key: 'email', label: 'Email' },
  { key: 'role', label: 'Role' },
  { key: 'createdAt', label: 'Joined', width: '150px' }, // Date di kanan
]
```

### 2. Cell Rendering

Gunakan slot untuk custom rendering:
```vue
<template #cell-name="{ item }">
  <div class="flex items-center gap-2">
    <Avatar class="h-8 w-8" />
    <div>
      <p class="font-medium">{{ item.name }}</p>
      <p class="text-xs text-muted-foreground">{{ item.email }}</p>
    </div>
  </div>
</template>
```

### 3. Actions Pattern

Gunakan DataTableActions untuk konsistensi:
```vue
<template #actions="{ item }">
  <DataTableActions
    :items="[
      { label: 'Edit', icon: Pencil, onClick: () => edit(item) },
      { label: 'Delete', icon: Trash2, variant: 'destructive', onClick: () => del(item) },
    ]"
  />
</template>
```

### 4. Empty State

Selalu sediakan empty state yang informatif:
```vue
<template #empty-icon>
  <UserCog class="h-8 w-8 text-muted-foreground" />
</template>
<template #empty-action>
  <Button @click="openCreate">Create your first user</Button>
</template>
```

---

## Migration Guide

### Dari Table Biasa ke DataTable

**Before:**
```vue
<table>
  <thead>
    <tr>
      <th>Name</th>
      <th>Email</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    <tr v-for="user in users" :key="user.id">
      <td>{{ user.name }}</td>
      <td>{{ user.email }}</td>
      <td>
        <Button @click="edit(user)">Edit</Button>
      </td>
    </tr>
  </tbody>
</table>
```

**After:**
```vue
<DataTable
  :data="users"
  :columns="[
    { key: 'name', label: 'Name' },
    { key: 'email', label: 'Email' },
  ]"
>
  <template #actions="{ item }">
    <Button @click="edit(item)">Edit</Button>
  </template>
</DataTable>
```

**Benefits:**
- ✅ Pagination built-in
- ✅ Sorting support
- ✅ Loading state
- ✅ Empty state
- ✅ Responsive
- ✅ Consistent styling

---

## Troubleshooting

### Issue: Pagination tidak update

**Solution:** Pastikan `total` prop diupdate dari API response:
```javascript
total.value = response.data.totalElements // Spring Page
```

### Issue: Sort icon tidak muncul

**Solution:** Set `sortable: true` di column definition:
```javascript
{ key: 'name', label: 'Name', sortable: true }
```

### Issue: Custom cell tidak render

**Solution:** Pastikan nama slot sesuai dengan column key:
```vue
<!-- Column key: 'createdAt' -->
<template #cell-createdAt="{ item }">
  {{ formatDate(item.createdAt) }}
</template>
```

---

## Referensi

- [shadcn-vue Documentation](https://www.shadcn-vue.com)
- [shadcn Table](https://ui.shadcn.com/docs/components/table)
- [TanStack Table](https://tanstack.com/table) (untuk advanced features)

---

## TODO

- [ ] Column visibility toggle
- [ ] Column pinning (fixed columns)
- [ ] Row selection (checkbox)
- [ ] Bulk actions
- [ ] Column resize
- [ ] Export to CSV/Excel
- [ ] Advanced filtering
- [ ] Virtual scrolling untuk large dataset
