<script setup>
const features = [
  {
    title: 'Kasir Modern',
    desc: 'Transaksi cepat dengan dukungan pembayaran digital dan cetak struk otomatis.',
  },
  {
    title: 'Manajemen Inventaris',
    desc: 'Pantau stok secara real-time dan terima notifikasi saat stok menipis.',
  },
  {
    title: 'Dashboard Analitik',
    desc: 'Visualisasi penjualan, laba, produk terlaris, dan performa bisnis.',
  },
  {
    title: 'Pembelian & Distributor',
    desc: 'Kelola supplier dan proses pengadaan barang dalam satu tempat.',
  },
  {
    title: 'Laporan Lengkap',
    desc: 'Laporan harian, mingguan, bulanan hingga tahunan secara otomatis.',
  },
  {
    title: 'Monitoring Bisnis',
    desc: 'Pantau seluruh aktivitas operasional dari satu dashboard terpadu.',
  },
]

 // Implementasi dalam komponen Vue Anda:
  import { useGsap } from '@/hooks/useGsap'

  useGsap((gsap) => {
    // Animasi muncul untuk teks utama (jalankan sekali)
    gsap.from('.animate-target > *', {
      y: 20,
      opacity: 0,
      duration: 0.8,
      stagger: 0.2,
      ease: 'power2.out'
    })

    // Animasi looping untuk kotak notifikasi
    gsap.fromTo('.notify-item', 
      { x: -10, opacity: 0 }, 
      { 
        x: 0, 
        opacity: 1, 
        duration: 0.8, 
        stagger: 0.3, 
        repeat: -1, 
        repeatDelay: 1,
        ease: 'power2.out' 
      }
    )
  })
</script>

<template>
  <section class="bg-black py-32">
    <div class="mx-auto max-w-7xl px-6">

      <!-- Heading -->
      <div class="mb-16 max-w-2xl">
        <h2
          class="text-5xl md:text-6xl font-light leading-tight text-white"
        >
          Kelola bisnis Anda
          <span class="text-violet-500">
            tanpa ribet
          </span>
        </h2>
      </div>

      <!-- TABLE GRID -->
      <div
        class="border border-zinc-800"
      >

        <!-- ROW 1 -->
        <div class="grid lg:grid-cols-2">

          <!-- Cell -->
           <div class="relative flex h-full items-start overflow-hidden px-12 py-16 pt-20">
            <div
                class="max-w-[720px] text-[42px] leading-[1.1] font-normal tracking-[-0.03em]"
            >
                <span class="text-white">Kelola seluruh operasional bisnis dengan</span>
                <span class="text-violet-400"> satu platform</span>,
                <span class="text-cyan-400"> kasir digital</span>,
                <span class="text-emerald-400"> inventaris</span>,
                <span class="text-orange-300"> pengadaan</span>,
                <span class="text-fuchsia-400"> produk</span>,
                <span class="text-sky-400"> kategori</span>,
                <span class="text-amber-300"> laporan</span>,
                <span class="text-red-400"> analitik</span>,
                <span class="text-green-400"> supplier</span>,
                <span class="text-violet-400"> dashboard</span>,
                <span class="text-emerald-500"> multi cabang</span>,
                <span class="text-cyan-500"> pelanggan</span>,
                <span class="text-yellow-400"> retail</span>,
                <span class="text-purple-500"> F&B</span>,
                <span class="text-sky-300"> POS</span>.
                <span class="text-white"> Semua fitur terintegrasi sempurna untuk kemudahan Anda.</span>
            </div>
            <!-- Text fade -->
            <div class="absolute inset-x-0 bottom-0 h-[65%] bg-gradient-to-t from-black via-black/95 via-black/70 to-transparent z-10 pointer-events-none"></div>
            </div>

          <!-- Cell -->
           <div class="relative h-full overflow-hidden px-16 py-16 pb-80">

  <!-- Content -->
  <div class="relative z-20 max-w-[580px]">

    <h3
      class="text-[38px] leading-[0.95] tracking-[-0.04em] font-light text-white"
    >
      Pantau bisnis secara real-time
    </h3>

    <p
      class="mt-6 max-w-[520px] text-[17px] leading-relaxed text-zinc-300"
    >
      Lihat transaksi, inventaris, penjualan, dan performa bisnis
      dari seluruh cabang dalam satu dashboard terintegrasi.
    </p>

    <a
      href="#"
      class="mt-8 inline-flex text-violet-400 hover:text-violet-300 transition-colors"
    >
      Lihat dashboard →
    </a>

  </div>

  <!-- Glow -->
  <div
    class="absolute bottom-20 left-1/2 h-[280px] w-[500px] -translate-x-1/2 rounded-full bg-emerald-900/35 blur-3xl"
  ></div>

  <!-- Grid -->
  <div
    class="absolute bottom-20 left-1/2 h-[220px] w-[420px] -translate-x-1/2 opacity-40"
    style="
      background-image:
        linear-gradient(rgba(16,185,129,.3) 1px, transparent 1px),
        linear-gradient(90deg, rgba(16,185,129,.3) 1px, transparent 1px);
      background-size: 20px 20px;
    "
  ></div>

  <!-- Bottom UI -->
  <div
    class="absolute bottom-32 left-1/2 z-20 flex -translate-x-1/2 items-center gap-6"
  >

    <!-- Left Panel -->
    <div
      class="w-[180px] border border-zinc-800 bg-[#0B0B0B]/90 backdrop-blur-sm"
    >

      <div
        class="flex items-center justify-between border-b border-zinc-800 px-4 py-3"
      >
        <span class="text-sm text-zinc-300">
          Dashboard Hari Ini
        </span>       
      </div>

      <div class="space-y-3 px-4 py-4">

        <div class="text-sm text-zinc-300">
          Penjualan Harian
        </div>

        <div class="flex items-center gap-2">
          <div
            class="flex h-5 w-5 items-center justify-center rounded-full bg-emerald-500 text-[10px] text-black"
          >
            ✓
          </div>

          <span class="text-sm text-zinc-400">
            Target tercapai
          </span>
        </div>

      </div>

    </div>

    <!-- Connector -->
     <div class="relative w-12">
        <div class="absolute top-1/2 left-[-50%] h-[2px] w-[250%] bg-gradient-to-r from-gray-900 via-black to-gray-900 shadow-[0_0_10px_rgba(0,0,0,0.8)] overflow-hidden">
            <div class="absolute inset-0 w-full h-full bg-[linear-gradient(90deg,transparent_0%,#22c55e_50%,transparent_100%)] animate-pulse-line"></div>
        </div>
    </div>

    <!-- Right Panel -->
    <div
      class="w-[200px] border border-zinc-800 bg-[#0B0B0B]/90 backdrop-blur-sm"
    >

      <div
        class="border-b border-zinc-800 px-4 py-3 text-sm text-zinc-300"
      >
        Status Sistem
      </div>

      <div class="space-y-3 px-4 py-4">

        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
                <div class="h-4 w-4 rounded-full bg-emerald-500 flex items-center justify-center">
                    <span class="text-[10px] text-black font-bold leading-none">✓</span>
                </div>
            <span class="text-sm text-zinc-300">
              Kasir
            </span>
          </div>

          <span class="text-xs text-zinc-500">
            Online
          </span>
        </div>

        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
                <div class="h-4 w-4 rounded-full bg-emerald-500 flex items-center justify-center">
                    <span class="text-[10px] text-black font-bold leading-none">✓</span>
                </div>
            <span class="text-sm text-zinc-300">
              Inventaris
            </span>
          </div>

          <span class="text-xs text-zinc-500">
            Online
          </span>
        </div>

        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
                <div class="h-4 w-4 rounded-full bg-emerald-500 flex items-center justify-center">
                    <span class="text-[10px] text-black font-bold leading-none">✓</span>
                </div>
            <span class="text-sm text-zinc-300">
              Produk
            </span>
          </div>

          <span class="text-xs text-zinc-500">
            Online
          </span>
        </div>

        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
                <div class="h-4 w-4 rounded-full bg-emerald-500 flex items-center justify-center">
                    <span class="text-[10px] text-black font-bold leading-none">✓</span>
                </div>
            <span class="text-sm text-zinc-300">
              Laporan
            </span>
          </div>

          <span class="text-xs text-zinc-500">
            Online
          </span>
        </div>
      </div>
    </div>
  </div>
</div>
        </div>

        <!-- ROW 2 -->
         <div class="border-t border-b border-zinc-800 p-10 flex flex-col md:flex-row items-stretch gap-10 bg-black min-h-[380px]">     
                <div class="flex-1 flex flex-col justify-center py-6">
                    <h3 class="text-3xl text-white font-semibold mb-4 max-w-sm">
                        Load-based autoscaling that handles 100x traffic bursts and beyond
                    </h3>
                    <p class="text-zinc-400 max-w-md mb-6">
                        Keep your workloads running smoothly through viral moments, 
                        seasonal spikes, and launch days.
                    </p>
                    <a href="#" class="text-purple-400 hover:text-purple-300 transition-colors flex items-center gap-2">
                        Autoscaling docs <span>&rarr;</span>
                    </a>
                </div>

                <div class="relative w-full md:w-1/2 overflow-hidden flex items-center justify-end" 
                    style="-webkit-mask-image: linear-gradient(to left, black 80%, transparent 100%); mask-image: linear-gradient(to left, black 80%, transparent 100%);">
                    
                    <div class="absolute -top-10 -right-10 w-96 h-96 bg-purple-600/30 blur-[120px] rounded-full z-0"></div>

                    <img 
                        src="/components/grafik.png" 
                        alt="Grafik Autoscaling" 
                        class="relative z-10 w-full h-full object-contain object-right opacity-100"
                    />

                </div>
            </div>

        <!-- ROW 3 -->
        <div class="grid lg:grid-cols-2">
          <div
            class="border-r border-b border-zinc-800 flex flex-col workflow-container relative overflow-hidden"
            style="padding-bottom: 0px;"
          >
            <div class="animate-target p-10 min-h-[280px]">
              <h3 class="text-3xl text-white mb-4">
                Sistem Kasir Pintar<br />
                untuk Bisnis Anda
              </h3>

              <p class="text-zinc-400 max-w-md mb-6">
                Kelola transaksi, inventaris, dan laporan penjualan secara real-time dalam satu dasbor yang responsif dan efisien.
              </p>

              <a href="#" class="text-indigo-400 hover:text-indigo-300 block mb-4">
                Lihat fitur POS →
              </a>
            </div>

            <div class="border-l border-r border-t border-zinc-800 bg-zinc-950 p-6 font-mono text-sm text-zinc-300 flex flex-col gap-3 notification-list relative">
              <!-- Kotak Notifikasi -->
              <div class="flex items-center gap-3 border border-zinc-700 bg-zinc-900 p-3 notify-item">
                <div class="w-3 h-3 bg-emerald-500 rounded-sm"></div>
                <span class="text-white">Transaksi: Pembayaran Berhasil</span>
              </div>
              <div class="flex items-center gap-3 border border-zinc-700 bg-zinc-900 p-3 notify-item">
                <div class="w-3 h-3 bg-amber-500 rounded-sm"></div>
                <span class="text-white">Stok: Produk mulai menipis</span>
              </div>
              <div class="flex items-center gap-3 border border-zinc-700 bg-zinc-900 p-3 notify-item">
                <div class="w-3 h-3 bg-blue-500 rounded-sm"></div>
                <span class="text-white">Laporan: Harian siap diunduh</span>
              </div>

              <!-- Efek Gradien Jatuh ke Kegelapan -->
              <div class="absolute bottom-0 left-0 w-full h-20 bg-gradient-to-t from-zinc-950 via-zinc-950/80 to-transparent pointer-events-none z-10"></div>
            </div>
          </div>

          <!-- col 2 -->
           <div class="border-b border-zinc-800 p-10 flex flex-col bg-black">
  <!-- Header Konten -->
  <div class="animate-target mb-10">
    <h3 class="text-4xl font-semibold text-white mb-4">
      Laporan Lengkap
    </h3>

    <p class="text-zinc-400 text-lg max-w-lg mb-6">
      Laporan otomatis yang siap diunduh kapan saja. Analisis data penjualan akurat dengan metrik performa bisnis yang mendalam.
    </p>

    <a href="#" class="text-indigo-400 hover:text-indigo-300 block">
      Lihat laporan →
    </a>
  </div>

  <!-- Blok Data Visual (Menggunakan Gambar Laporan dengan Proteksi) -->
  <div class="border border-zinc-800 bg-[#0d0d0d] p-6 max-w-2xl shadow-2xl">
    <img 
      src="/components/lapor.webp" 
      alt="Laporan Lengkap" 
      class="w-full h-auto object-cover select-none pointer-events-none"
      oncontextmenu="return false;"
      draggable="false"
      onerror="this.style.display='none'; this.parentNode.innerHTML='<div class=\'text-zinc-500 font-mono\'>Gambar laporan tidak ditemukan</div>'"
    />
  </div>
</div>

        </div>

        <!-- ROW 4 -->
        <div
          class="p-10 min-h-[340px]"
        >
          <h3 class="text-3xl text-white mb-4">
            Monitoring Bisnis
          </h3>

          <p class="text-zinc-400 max-w-md">
            Pantau aktivitas toko, transaksi, stok, dan performa bisnis.
          </p>

          <div
            class="mt-12 rounded-xl border border-zinc-800 bg-violet-950/20 p-8"
          >
            <div class="grid md:grid-cols-4 gap-6">

              <div>
                <div class="text-zinc-500 text-sm">Penjualan</div>
                <div class="text-3xl text-white">1.245</div>
              </div>

              <div>
                <div class="text-zinc-500 text-sm">Produk</div>
                <div class="text-3xl text-white">342</div>
              </div>

              <div>
                <div class="text-zinc-500 text-sm">Pelanggan</div>
                <div class="text-3xl text-white">879</div>
              </div>

              <div>
                <div class="text-zinc-500 text-sm">Laba</div>
                <div class="text-3xl text-violet-400">
                  +24%
                </div>
              </div>

            </div>
          </div>
        </div>

      </div>

    </div>
  </section>
</template>

<style scoped>
@keyframes move-line {
    0% { transform: translateX(-100%); }
    100% { transform: translateX(100%); }
  }
  .animate-pulse-line {
    animation: move-line 2s linear infinite;
    opacity: 0.6;
  } 
</style>