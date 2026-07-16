package com.project.tas_pbo.DAO;

import com.project.tas_pbo.model.Produk;
import com.project.tas_pbo.database.DBconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProdukDAO {

    public List<Produk> getAllProduk() {
        List<Produk> list = new ArrayList<>();
        String sql = "SELECT * FROM produk ORDER BY id_produk ASC";

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

    public List<Produk> searchProduk(String keyword) {
        List<Produk> list = new ArrayList<>();
        String sql = "SELECT * FROM produk WHERE nama_produk LIKE ? OR id_produk = ? ORDER BY nama_produk ASC LIMIT 20";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            try {
                stmt.setInt(2, Integer.parseInt(keyword));
            } catch (NumberFormatException e) {
                stmt.setInt(2, -1); // no match if not numeric
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

    public Produk findByBarcodeOrId(String keyword) {

        String sqlByBarcode = "SELECT * FROM produk WHERE barcode = ?";
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
            String sqlById = "SELECT * FROM produk WHERE id_produk = ?";
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

    public Produk getProdukById(int id) {
        String sql = "SELECT * FROM produk WHERE id_produk = ?";

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

    public boolean addProduk(Produk produk) {
        String sql = "INSERT INTO produk (nama_produk, barcode, kategori, harga, stok, satuan, stok_minimum) VALUES (?, ?, ?, ?, ?, ?, ?)";

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

    public boolean deleteProduk(int id) {
        String sql = "DELETE FROM produk WHERE id_produk = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Produk mapResultSetToProduk(ResultSet rs) throws SQLException {
        Produk produk = new Produk();
        produk.setIdProduk(rs.getInt("id_produk")); // ADD THIS LINE
        produk.setBarcode(rs.getString("barcode"));
        produk.setNamaProduk(rs.getString("nama_produk"));
        produk.setKategori(rs.getString("kategori"));
        produk.setHarga(rs.getDouble("harga"));
        produk.setStok(rs.getInt("stok"));
        produk.setSatuan(rs.getString("satuan"));
        produk.setStokMinimum(rs.getInt("stok_minimum"));
        return produk;
    }

    public int getTotalProduk() {
        String sql = "SELECT COUNT(*) AS total FROM produk";
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

    public int getTotalStok() {
        String sql = "SELECT SUM(stok) AS total FROM produk";
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

    public List<Produk> getProdukMenipis() {
        List<Produk> list = new ArrayList<>();
        String sql = "SELECT * FROM produk WHERE stok <= stok_minimum ORDER BY stok ASC";
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