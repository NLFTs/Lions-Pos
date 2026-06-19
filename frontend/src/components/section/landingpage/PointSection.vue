<script setup>
const steps = [
  {
    number: '1',
    title: 'Pilih Modul',
    description:
      'Pilih fitur yang ingin digunakan mulai dari kasir, inventaris, pembelian, hingga pengelolaan produk dalam satu sistem terintegrasi.',
  },
  {
    number: '2',
    title: 'Kelola Stok',
    description:
      'Pantau ketersediaan barang secara real-time, atur minimum stok, dan lakukan pengelolaan inventaris dengan lebih efisien.',
  },
  {
    number: '3',
    title: 'Pantau Dashboard',
    description:
      'Lihat performa bisnis, penjualan, transaksi, dan kondisi inventaris melalui dashboard yang mudah dipahami.',
  },
]
</script>

<template>
  <section class="relative bg-black py-32 overflow-hidden">
    <div class="mx-auto max-w-7xl px-6 lg:px-10">

      <div class="grid gap-20 lg:grid-cols-3">

        <!-- STEP 1 -->
        <div class="space-y-8">

          <div
            class="flex h-10 w-10 items-center justify-center bg-[#6D28D9] text-white text-xl font-medium"
          >
            {{ steps[0].number }}
          </div>

          <div class="space-y-5">
            <h3
              class="text-4xl md:text-5xl font-light tracking-tight text-white"
            >
              {{ steps[0].title }}
            </h3>

            <p
              class="max-w-md text-lg leading-relaxed text-zinc-300"
            >
              {{ steps[0].description }}
            </p>
          </div>

          <div
            class="mt-10 border border-zinc-800 bg-gradient-to-br from-zinc-900 to-black overflow-hidden"
          >

            <div class="border-b border-zinc-800 p-4">
              <div
                class="flex items-center justify-between rounded-lg border border-zinc-700 bg-zinc-900 px-4 py-3 text-sm text-zinc-400"
              >
                <span>Cari...</span>
                <span>Esc</span>
              </div>
            </div>

            <div class="divide-y divide-zinc-800">

              <div class="px-5 py-4">
                <div class="text-white">Dashboard</div>
                <div class="text-sm text-zinc-500">Utama</div>
              </div>

              <div class="bg-emerald-400 px-5 py-4">
                <div class="font-medium text-black">Kasir</div>
                <div class="text-sm text-black/70">Transaksi</div>
              </div>

              <div class="px-5 py-4">
                <div class="text-white">Riwayat Pesanan</div>
                <div class="text-sm text-zinc-500">Transaksi</div>
              </div>

              <div class="px-5 py-4">
                <div class="text-white">Produk</div>
                <div class="text-sm text-zinc-500">Inventaris</div>
              </div>

              <div class="px-5 py-4">
                <div class="text-white">Data Inventaris</div>
                <div class="text-sm text-zinc-500">Inventaris</div>
              </div>

              <div class="px-5 py-4">
                <div class="text-white">Kategori</div>
                <div class="text-sm text-zinc-500">Inventaris</div>
              </div>

              <div class="px-5 py-4">
                <div class="text-white">Distributor</div>
                <div class="text-sm text-zinc-500">Pengadaan</div>
              </div>

              <div class="px-5 py-4">
                <div class="text-white">Pembelian</div>
                <div class="text-sm text-zinc-500">Pengadaan</div>
              </div>

            </div>

          </div>

        </div>

        <!-- STEP 2 -->
        <div class="space-y-8">

          <div
            class="flex h-10 w-10 items-center justify-center bg-[#6D28D9] text-white text-xl font-medium"
          >
            {{ steps[1].number }}
          </div>

          <div class="space-y-5">
            <h3
              class="text-4xl md:text-5xl font-light tracking-tight text-white"
            >
              {{ steps[1].title }}
            </h3>

            <p
              class="max-w-md text-lg leading-relaxed text-zinc-300"
            >
              {{ steps[1].description }}
            </p>
          </div>

          <div
            class="border border-zinc-800 bg-gradient-to-br from-zinc-900 to-black p-6"
          >

            <div class="space-y-5">

              <div class="grid grid-cols-[140px_1fr] gap-4">
                <span class="text-zinc-400">Produk</span>
                <div class="border border-zinc-700 bg-zinc-900 p-3 text-white">
                  Kopi Arabica
                </div>
              </div>

              <div class="grid grid-cols-[140px_1fr] gap-4">
                <span class="text-zinc-400">Stok Saat Ini</span>
                <div class="border border-zinc-700 bg-zinc-900 p-3 text-white">
                  248 Unit
                </div>
              </div>

              <div class="grid grid-cols-[140px_1fr] gap-4">
                <span class="text-zinc-400">Minimum Stok</span>
                <div class="border border-zinc-700 bg-zinc-900 p-3 text-white">
                  50 Unit
                </div>
              </div>

              <div class="grid grid-cols-[140px_1fr] gap-4">
                <span class="text-zinc-400">Status</span>
                <div class="border border-zinc-700 bg-zinc-900 p-3 text-emerald-400">
                  Aman
                </div>
              </div>

            </div>

            <button
              class="mt-8 bg-white px-6 py-3 text-black"
            >
              Update Stok
            </button>

          </div>

        </div>

        <!-- STEP 3 -->
        <div class="space-y-8">

          <div
            class="flex h-10 w-10 items-center justify-center bg-[#6D28D9] text-white text-xl font-medium"
          >
            {{ steps[2].number }}
          </div>

          <div class="space-y-5">
            <h3
              class="text-4xl md:text-5xl font-light tracking-tight text-white"
            >
              {{ steps[2].title }}
            </h3>

            <p
              class="max-w-md text-lg leading-relaxed text-zinc-300"
            >
              {{ steps[2].description }}
            </p>
          </div>

          <div
            class="border border-zinc-800 bg-gradient-to-br from-zinc-900 to-black"
          >

            <div class="border-b border-zinc-800 p-5 text-zinc-400">
              Ringkasan Hari Ini
            </div>

            <div class="divide-y divide-zinc-800">

              <div class="flex items-center justify-between p-5">
                <span class="text-zinc-400">Penjualan</span>
                <span class="text-white font-medium">
                  Rp 12.500.000
                </span>
              </div>

              <div class="flex items-center justify-between p-5">
                <span class="text-zinc-400">Pesanan</span>
                <span class="text-white font-medium">
                  152
                </span>
              </div>

              <div class="flex items-center justify-between p-5">
                <span class="text-zinc-400">Produk Terjual</span>
                <span class="text-white font-medium">
                  487
                </span>
              </div>

              <div class="flex items-center justify-between p-5">
                <span class="text-zinc-400">Stok Menipis</span>
                <span class="font-medium text-amber-400">
                  8 Item
                </span>
              </div>

            </div>

          </div>

        </div>

      </div>

    </div>
  </section>
</template>

<style scoped>
</style>