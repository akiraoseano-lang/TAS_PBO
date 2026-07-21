package com.project.tas_pbo.DAO;

import com.project.tas_pbo.database.DBconnection;
import com.project.tas_pbo.model.LaporanHarian;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO untuk operasi database laporan
public class LaporanDAO {

    /**
     * Mengambil ringkasan penjualan harian untuk N hari terakhir.
     * Mengembalikan daftar LaporanHarian yang diurutkan berdasarkan tanggal.
     */
    public static List<LaporanHarian> getDailySales(int days) {
        List<LaporanHarian> list = new ArrayList<>();

        String sql = """
                SELECT
                    DATE(waktu_transaksi) AS tanggal,
                    COUNT(*) AS jumlah_transaksi,
                    SUM(total_belanja) AS total_penjualan
                FROM penjualan
                WHERE waktu_transaksi >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
                GROUP BY DATE(waktu_transaksi)
                ORDER BY tanggal ASC
                """;

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, days);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LaporanHarian l = new LaporanHarian();
                    l.setTanggal(rs.getDate("tanggal").toLocalDate());
                    l.setJumlahTransaksi(rs.getInt("jumlah_transaksi"));
                    l.setTotalPenjualan(rs.getDouble("total_penjualan"));
                    list.add(l);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Mengambil total ringkasan untuk suatu periode.
     */
    public static LaporanHarian getSummary(int days) {
        String sql = """
                SELECT
                    COUNT(*) AS jumlah_transaksi,
                    COALESCE(SUM(total_belanja), 0) AS total_penjualan,
                    COALESCE(AVG(total_belanja), 0) AS rata_rata
                FROM penjualan
                WHERE waktu_transaksi >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
                """;

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, days);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LaporanHarian l = new LaporanHarian();
                    l.setJumlahTransaksi(rs.getInt("jumlah_transaksi"));
                    l.setTotalPenjualan(rs.getDouble("total_penjualan"));
                    l.setRataRata(rs.getDouble("rata_rata"));
                    return l;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new LaporanHarian();
    }

    /**
     * Mengambil produk terlaris untuk suatu periode.
     */
    public static List<String[]> getTopProduk(int days, int limit) {
        List<String[]> list = new ArrayList<>();

        String sql = """
                SELECT
                    pd.nama_produk,
                    SUM(pd.jumlah) AS total_terjual,
                    SUM(pd.subtotal) AS total_pendapatan
                FROM penjualan_detail pd
                JOIN penjualan p ON pd.id_penjualan = p.id_penjualan
                WHERE p.waktu_transaksi >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
                GROUP BY pd.nama_produk
                ORDER BY total_terjual DESC
                LIMIT ?
                """;

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, days);
            stmt.setInt(2, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                            rs.getString("nama_produk"),
                            String.valueOf(rs.getInt("total_terjual")),
                            String.valueOf(rs.getDouble("total_pendapatan"))
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}