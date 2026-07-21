package com.project.tas_pbo.DAO;

import com.project.tas_pbo.database.DBconnection;
import com.project.tas_pbo.model.Penjualan;
import com.project.tas_pbo.model.PenjualanDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

// DAO untuk operasi database tabel penjualan dan penjualan_detail
public class PenjualanDAO {

    // Menyimpan transaksi penjualan beserta detailnya (menggunakan transaksi database)
    public int saveTransaction(Penjualan penjualan, List<PenjualanDetail> items) {
        String sqlHeader = "INSERT INTO penjualan (no_transaksi, id_user, total_belanja, bayar, kembalian) " +
                "VALUES (?, ?, ?, ?, ?)";

        String sqlDetail = "INSERT INTO penjualan_detail (id_penjualan, id_produk, nama_produk, harga_satuan, jumlah, subtotal) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        String sqlUpdateStok = "UPDATE produk SET stok = stok - ? WHERE id_produk = ?";

        Connection conn = null;

        try {
            conn = DBconnection.connect();
            conn.setAutoCommit(false);

            int generatedId;


            try (PreparedStatement stmt = conn.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, penjualan.getNoTransaksi());
                stmt.setInt(2, penjualan.getIdUser());
                stmt.setDouble(3, penjualan.getTotalBelanja());
                stmt.setDouble(4, penjualan.getBayar());
                stmt.setDouble(5, penjualan.getKembalian());

                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedId = keys.getInt(1);
                    } else {
                        conn.rollback();
                        return -1;
                    }
                }
            }

            try (PreparedStatement stmtDetail = conn.prepareStatement(sqlDetail);
                 PreparedStatement stmtStok = conn.prepareStatement(sqlUpdateStok)) {

                for (PenjualanDetail item : items) {
                    stmtDetail.setInt(1, generatedId);
                    stmtDetail.setInt(2, item.getIdProduk());
                    stmtDetail.setString(3, item.getNamaProduk());
                    stmtDetail.setDouble(4, item.getHargaSatuan());
                    stmtDetail.setInt(5, item.getJumlah());
                    stmtDetail.setDouble(6, item.getSubtotal());
                    stmtDetail.addBatch();

                    stmtStok.setInt(1, item.getJumlah());
                    stmtStok.setInt(2, item.getIdProduk());
                    stmtStok.addBatch();
                }

                stmtDetail.executeBatch();
                stmtStok.executeBatch();
            }

            conn.commit();
            return generatedId;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            return -1;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Menghasilkan nomor transaksi otomatis dengan format TRX-YYYYMMDD-XXXX
    public String generateNoTransaksi() {
        String today = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String prefix = "TRX-" + today + "-";

        String sql = "SELECT COUNT(*) AS jumlah FROM penjualan WHERE no_transaksi LIKE ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("jumlah") + 1;
                    return prefix + String.format("%04d", count);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prefix + "0001";
    }

    // Mengambil total penjualan
    public double getTotalPenjualan() {
        String sql = "SELECT SUM(total_belanja) AS total FROM penjualan";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Mengambil semua data penjualan
    public List<Penjualan> getAllPenjualan() {
        List<Penjualan> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM penjualan ORDER BY waktu_transaksi DESC, id_penjualan DESC";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Penjualan p = new Penjualan();
                p.setIdPenjualan(rs.getInt("id_penjualan"));
                p.setNoTransaksi(rs.getString("no_transaksi"));
                p.setIdUser(rs.getInt("id_user"));
                p.setTotalBelanja(rs.getDouble("total_belanja"));
                p.setBayar(rs.getDouble("bayar"));
                p.setKembalian(rs.getDouble("kembalian"));
                p.setWaktuTransaksi(rs.getTimestamp("waktu_transaksi"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Mengambil penjualan terbaru dengan jumlah terbatas
    public List<Penjualan> getLatestPenjualan(int limit) {
        List<Penjualan> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM penjualan ORDER BY waktu_transaksi DESC, id_penjualan DESC LIMIT ?";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Penjualan p = new Penjualan();
                    p.setIdPenjualan(rs.getInt("id_penjualan"));
                    p.setNoTransaksi(rs.getString("no_transaksi"));
                    p.setIdUser(rs.getInt("id_user"));
                    p.setTotalBelanja(rs.getDouble("total_belanja"));
                    p.setBayar(rs.getDouble("bayar"));
                    p.setKembalian(rs.getDouble("kembalian"));
                    p.setWaktuTransaksi(rs.getTimestamp("waktu_transaksi"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}