# Layout Standards - Spravel

## Overview

Layout Spravel menggunakan desain modern dengan:
- **Sidebar**: Navigasi utama + menu pengaturan di bawah
- **Topbar**: Minimalis (breadcrumb + language + role)

---

## Struktur Layout

```
┌─────────────────────────────────────────────────────────────┐
│ TOPBAR (h-12)                                               │
│ [Breadcrumb]                    [ID | EN]  [Role ▼]        │
├──────────┬──────────────────────────────────────────────────┤
│          │                                                  │
│ SIDEBAR  │              MAIN CONTENT                        │
│ (w-70px) │              (flex-1)                            │
│   or     │                                                  │
│ (w-280px)│                                                  │
│          │                                                  │
│ [Menu]   │  [Page Title]                                    │
│ [⋮]      │  [Subtitle]                                      │
│          │                                                  │
│ [Settings]                                                │
│ [⋮]      │  [Content Area]                                  │
│ [Keluar] │                                                  │
└──────────┴──────────────────────────────────────────────────┘
```

---

## Topbar Components

### 1. Language Switcher

**Lokasi**: Topbar kanan, sebelah kiri role dropdown

**Format**: `ID | EN`

- Active language: font-semibold
- English active: text-emerald-600
- Separator: `|` (text-zinc-300)

```vue
<div class="flex items-center gap-2 text-sm">
  <button @click="switchLang('ID')" :class="active ? 'font-semibold' : 'text-zinc-400'">
    ID
  </button>
  <span class="text-zinc-300">|</span>
  <button @click="switchLang('EN')" :class="active ? 'text-emerald-600 font-semibold' : 'text-zinc-400'">
    EN
  </button>
</div>
```

### 2. Role Dropdown

**Lokasi**: Topbar kanan, paling kanan

**Isi**:
- Header: "Logged in as" + username
- Menu: Profil Saya
- Footer: Keluar (merah)

```vue
<button @click="showRoleDropdown = !showRoleDropdown">
  {{ displayRole }}
  <ChevronDown class="w-3.5 h-3.5" />
</button>

<div v-if="showRoleDropdown" class="dropdown">
  <div class="header">Logged in as<br><strong>{{ displayName }}</strong></div>
  <RouterLink to="/dashboard/profile">Profil Saya</RouterLink>
  <button @click="auth.logout()" class="text-red-600">Keluar</button>
</div>
```

---

## Sidebar Components

### 1. Main Navigation

**Lokasi**: Bagian atas sidebar

**Item**:
- Dashboard (icon: `LayoutDashboard`)
- Konten (children: Posts, Kategori)
- Manajemen User
- Keamanan (children: Role, Permission, Modul)
- Audit Log

**State**:
- Active: `bg-primary/10 text-primary`
- Hover: `bg-zinc-100 dark:bg-zinc-900`
- Collapsed: hanya icon

### 2. Settings Menu

**Lokasi**: Bagian bawah sidebar (dipisah divider)

**Item**:
- Profil (`UserCircle`)
- Pengaturan (`Settings`)
- Tema (`Palette`) → children: Primary, Neutral
- Tampilan (`Monitor`) → children: Light, Dark

**Nested Submenu**:
```vue
<button @click="toggleExpand('Tema')">
  <Palette class="w-4 h-4" />
  <span>Tema</span>
  <ChevronRight class="w-3.5 h-3.5" />
</button>

<div v-if="isExpanded('Tema')" class="submenu">
  <button @click="setTheme('primary')">
    <ChevronRight class="w-3.5 h-3.5" />
    <span>Primary</span>
  </button>
</div>
```

### 3. Logout Button

**Lokasi**: Paling bawah sidebar

**Style**:
- Text: `text-red-600 dark:text-red-400`
- Hover: `bg-red-50 dark:bg-red-950/30`
- Icon: `LogOut`

---

## Dimensions

| Element | Size |
|---------|------|
| Topbar height | `h-12` (48px) |
| Sidebar width (expanded) | `w-[280px]` |
| Sidebar width (collapsed) | `w-[70px]` |
| Icon size (main menu) | `w-4 h-4` |
| Icon size (submenu) | `w-3.5 h-3.5` |
| Font size (menu) | `text-sm` (14px) |
| Font size (submenu) | `text-sm` (14px) |

---

## Colors

### Light Mode
- Background: `bg-white` (sidebar), `bg-zinc-50` (main)
- Text: `text-zinc-900` (primary), `text-zinc-600` (secondary)
- Border: `border-zinc-200`
- Hover: `bg-zinc-100`

### Dark Mode
- Background: `dark:bg-zinc-950` (sidebar), `dark:bg-zinc-950` (main)
- Text: `dark:text-zinc-100` (primary), `dark:text-zinc-400` (secondary)
- Border: `dark:border-zinc-800`
- Hover: `dark:hover:bg-zinc-900`

### Active State
- Main menu: `bg-primary/10 text-primary`
- Submenu: `text-primary` (text only)

---

## Behavior

### Sidebar Toggle
- State disimpan di localStorage: `sidebarCollapsed`
- Transition: `duration-300 ease-in-out`
- Collapsed: hanya icon yang tampil

### Submenu Expand
- State: `expandedMenus` (Set)
- Animation: Chevron rotate 90°
- Nested submenu: indent `ml-3`, border-left

### Outside Click
- Role dropdown close on outside click
- Handler: `handleOutsideClick`

---

## Icons (Lucide Vue)

```javascript
import {
  LayoutDashboard,  // Dashboard
  FileText,         // Konten
  Users,            // User Management
  ShieldCheck,      // Keamanan
  Activity,         // Audit Log
  UserCircle,       // Profil
  Settings,         // Pengaturan
  Palette,          // Tema
  Monitor,          // Tampilan
  LogOut,           // Keluar
  ChevronRight,     // Submenu indicator
  ChevronDown,      // Dropdown indicator
  PanelLeftClose,   // Collapse sidebar
  PanelLeftOpen,    // Expand sidebar
} from 'lucide-vue-next'
```

---

## Responsive

Layout ini **tidak responsive** untuk mobile. Sidebar selalu visible.

Untuk mobile support, perlu tambahan:
- Overlay saat sidebar expanded di mobile
- Hamburger menu di topbar
- Sidebar slide-in dari kiri

---

## Contoh Penggunaan di Page

```vue
<script setup>
import AppLayout from '@/components/AppLayout.vue'
</script>

<template>
  <AppLayout>
    <template #header-actions>
      <button>Tambah Data</button>
    </template>

    <!-- Konten halaman -->
    <div class="grid grid-cols-1 gap-4">
      <!-- Cards, tables, etc -->
    </div>
  </AppLayout>
</template>
```

---

## Migration Notes

### Dari Layout Lama ke Baru

| Old | New |
|-----|-----|
| Topbar: search, theme, notifications, profile | Topbar: language, role only |
| Sidebar: profile avatar di bawah | Sidebar: settings menu + logout |
| Profile dropdown di topbar | Role dropdown (minimal) |
| Theme switcher di topbar | Theme submenu di sidebar |
| Breadcrumb besar | Breadcrumb minimal |

### Breaking Changes
- ❌ Search bar di topbar **dihapus**
- ❌ Notification bell **dihapus**
- ❌ Theme color picker **dipindah** ke sidebar submenu
- ✅ Profile link tetap ada di role dropdown
- ✅ Logout tetap ada, dipindah ke sidebar bawah

---

## TODO

- [ ] Implement actual language switching (i18n)
- [ ] Add mobile responsive sidebar
- [ ] Add settings page (route: `/dashboard/settings`)
- [ ] Persist language preference to localStorage
- [ ] Add keyboard shortcuts (Ctrl+K for search?)
