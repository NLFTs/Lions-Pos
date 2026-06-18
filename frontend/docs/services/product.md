# Product

Modul **Product** adalah inti dari sistem manajemen inventaris Gaptek. Semua data produk, harga, dan stok dikelola di sini.

## Fitur Utama

- **Katalog Produk** — Tambah, edit, dan hapus produk dengan mudah.
- **Kategori & Tag** — Organisasikan produk menggunakan kategori dan label kustom.
- **Multi-Harga** — Dukung harga grosir, eceran, dan harga khusus pelanggan.
- **Manajemen Stok** — Pantau stok real-time di setiap gudang dan cabang.
- **Foto Produk** — Upload gambar produk langsung dari panel admin.

## Menambahkan Produk Baru

1. Masuk ke menu **Produk → Daftar Produk**.
2. Klik tombol **+ Tambah Produk**.
3. Isi formulir: nama, SKU, kategori, harga, dan stok awal.
4. Upload foto produk (opsional).
5. Klik **Simpan**.

## Impor Produk Massal

Anda dapat mengunggah produk secara massal menggunakan file Excel:

```bash
Format file: .xlsx
Template: Unduh dari menu Produk → Impor → Unduh Template
```

::: tip
Pastikan SKU produk bersifat **unik** untuk menghindari duplikasi data saat impor massal.
:::

## Manajemen Stok

Stok dapat dikelola melalui:

| Menu              | Fungsi                                     |
|-------------------|--------------------------------------------|
| Penerimaan Stok   | Tambah stok dari pembelian atau produksi   |
| Transfer Stok     | Pindahkan stok antar gudang / cabang       |
| Penyesuaian Stok  | Koreksi stok akibat selisih fisik          |
| Mutasi Stok       | Lihat riwayat pergerakan stok              |
