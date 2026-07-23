package com.project.tas_pbo.DAO;

import com.project.tas_pbo.model.Produk;
import com.project.tas_pbo.database.DBconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// DAO untuk operasi database tabel produk
public class ProdukDAO implements ICrudDAO<Produk> {

    // Mengambil semua produk aktif
    public List<Produk> getAllProduk() {
        List<Produk> list = new ArrayList<>();
        String sql = "SELECT * FROM produk WHERE status = 1 ORDER BY id_produk ASC";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToProduk(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Mencari produk berdasarkan nama atau ID
    public List<Produk> searchProduk(String keyword) {
        List<Produk> list = new ArrayList<>();
        String sql = "SELECT * FROM produk WHERE status = 1 AND (nama_produk LIKE ? OR id_produk = ?) ORDER BY nama_produk ASC LIMIT 20";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            try {
                stmt.setInt(2, Integer.parseInt(keyword));
            } catch (NumberFormatException e) {
                stmt.setInt(2, -1); // tidak cocok jika bukan angka
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduk(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Mencari produk berdasarkan nama saja
    public List<Produk> searchByName(String name) {
        List<Produk> list = new ArrayList<>();
        String sql = "SELECT * FROM produk WHERE status = 1 AND nama_produk LIKE ? ORDER BY nama_produk ASC LIMIT 20";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduk(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Mencari produk berdasarkan barcode atau ID
    public Produk findByBarcodeOrId(String keyword) {

        String sqlByBarcode = "SELECT * FROM produk WHERE status = 1 AND barcode = ?";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sqlByBarcode)) {

            stmt.setString(1, keyword);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduk(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (keyword.matches("\\d+")) {
            String sqlById = "SELECT * FROM produk WHERE status = 1 AND id_produk = ?";
            try (Connection conn = DBconnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(sqlById)) {

                stmt.setInt(1, Integer.parseInt(keyword));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToProduk(rs);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // Mengambil produk berdasarkan ID
    public Produk getProdukById(int id) {
        String sql = "SELECT * FROM produk WHERE status = 1 AND id_produk = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduk(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Menambahkan produk baru
    public boolean addProduk(Produk produk) {
        String sql = "INSERT INTO produk (nama_produk, barcode, kategori, harga, stok, satuan, stok_minimum, status) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, produk.getNamaProduk());
            stmt.setString(2, produk.getBarcode());
            stmt.setString(3, produk.getKategori());
            stmt.setDouble(4, produk.getHarga());
            stmt.setInt(5, produk.getStok());
            stmt.setString(6, produk.getSatuan());
            stmt.setInt(7, produk.getStokMinimum());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mengupdate data produk
    public boolean updateProduk(Produk produk) {
        String sql = "UPDATE produk SET nama_produk = ?, barcode = ?, kategori = ?, harga = ?, stok = ?, satuan = ?, stok_minimum = ? WHERE id_produk = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, produk.getNamaProduk());
            stmt.setString(2, produk.getBarcode());
            stmt.setString(3, produk.getKategori());
            stmt.setDouble(4, produk.getHarga());
            stmt.setInt(5, produk.getStok());
            stmt.setString(6, produk.getSatuan());
            stmt.setInt(7, produk.getStokMinimum());
            stmt.setInt(8, produk.getIdProduk());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Menghapus produk (soft delete - set stok = 0, status = 0)
    public boolean deleteProduk(int id) {
        String sql = "UPDATE produk SET stok = 0, status = 0 WHERE id_produk = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // Memulihkan produk yang dihapus (set status = 1, stok = stokBaru)
    public boolean restoreProduk(int idProduk, int stokBaru) {
        String sql = "UPDATE produk SET status = 1, stok = ? WHERE id_produk = ?";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stokBaru);
            stmt.setInt(2, idProduk);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Produk> getAll() { return getAllProduk(); }

    @Override
    public Produk getById(int id) { return getProdukById(id); }

    @Override
    public boolean add(Produk entity) { return addProduk(entity); }

    @Override
    public boolean update(Produk entity) { return updateProduk(entity); }

    @Override
    public boolean delete(int id) { return deleteProduk(id); }

    // Memetakan hasil query ke objek Produk
    private Produk mapResultSetToProduk(ResultSet rs) throws SQLException {
        Produk produk = new Produk();
        produk.setIdProduk(rs.getInt("id_produk"));
        produk.setBarcode(rs.getString("barcode"));
        produk.setNamaProduk(rs.getString("nama_produk"));
        produk.setKategori(rs.getString("kategori"));
        produk.setHarga(rs.getDouble("harga"));
        produk.setStok(rs.getInt("stok"));
        produk.setSatuan(rs.getString("satuan"));
        produk.setStokMinimum(rs.getInt("stok_minimum"));
        produk.setStatus(rs.getInt("status"));
        return produk;
    }

    // Mengambil total jumlah produk
    public int getTotalProduk() {
        String sql = "SELECT COUNT(*) AS total FROM produk WHERE status = 1";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Mengambil total stok semua produk
    public int getTotalStok() {
        String sql = "SELECT SUM(stok) AS total FROM produk WHERE status = 1";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Mengambil produk yang telah dihapus
    public List<Produk> getDeletedProduk() {
        List<Produk> list = new ArrayList<>();
        String sql = "SELECT * FROM produk WHERE status = 0 ORDER BY id_produk ASC";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToProduk(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Mengambil produk dengan stok menipis
    public List<Produk> getProdukMenipis() {
        List<Produk> list = new ArrayList<>();
        String sql = "SELECT * FROM produk WHERE status = 1 AND stok <= stok_minimum ORDER BY stok ASC";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToProduk(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Mengambil daftar kategori unik
    public List<String> getAllKategori() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT kategori FROM produk WHERE status = 1 ORDER BY kategori ASC";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("kategori"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Mencari produk berdasarkan kategori
    public List<Produk> getProdukByKategori(String kategori) {
        List<Produk> list = new ArrayList<>();
        String sql = "SELECT * FROM produk WHERE status = 1 AND kategori = ? ORDER BY nama_produk ASC";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, kategori);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduk(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Cek apakah barcode sudah ada di database
    public boolean isBarcodeExists(String barcode) {
        String sql = "SELECT COUNT(*) FROM produk WHERE barcode = ?";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, barcode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Mengambil produk terlaris berdasarkan jumlah penjualan
    public List<com.project.tas_pbo.model.ProdukTerlaris> getProdukTerlaris(int limit) {
        List<com.project.tas_pbo.model.ProdukTerlaris> list = new ArrayList<>();
        String sql = "SELECT pd.nama_produk, SUM(pd.jumlah) AS total_terjual, p.satuan " +
                     "FROM penjualan_detail pd " +
                     "JOIN produk p ON pd.id_produk = p.id_produk " +
                     "GROUP BY pd.id_produk, pd.nama_produk, p.satuan " +
                     "ORDER BY total_terjual DESC LIMIT ?";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new com.project.tas_pbo.model.ProdukTerlaris(
                        rs.getString("nama_produk"),
                        rs.getInt("total_terjual"),
                        rs.getString("satuan")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}