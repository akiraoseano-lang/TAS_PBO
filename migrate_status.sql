-- ==========================================================
-- Migrasi: Menambahkan kolom `status` untuk soft delete
-- Semua tabel kecuali penjualan & penjualan_detail
-- ==========================================================

-- 1. Produk: ganti is_deleted -> status (jika is_deleted sudah ada)
ALTER TABLE produk CHANGE COLUMN is_deleted status TINYINT(1) NOT NULL DEFAULT 1;

-- 2. Users: tambah kolom status
ALTER TABLE users ADD COLUMN status TINYINT(1) NOT NULL DEFAULT 1 AFTER role;

-- Note: penjualan & penjualan_detail tidak ditambahkan status
-- agar relasi data penjualan tidak rusak.
