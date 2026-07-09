package com.project.tas_pbo.DAO;

import com.project.tas_pbo.database.DBconnection;
import com.project.tas_pbo.model.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    public List<Member> getAllMember() {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM member ORDER BY id_member ASC";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Member getByKode(String kodeMember) {

        String sql = "SELECT * FROM member WHERE kode_member = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.println("Kode yang dicari = " + kodeMember);

            stmt.setString(1, kodeMember);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    System.out.println("Ketemu member!");
                    return map(rs);
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Tidak ketemu member");
        return null;
    }

    public List<Member> searchMember(String keyword) {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE nama_member LIKE ? OR kode_member LIKE ? OR no_telepon LIKE ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String kw = "%" + keyword + "%";
            stmt.setString(1, kw);
            stmt.setString(2, kw);
            stmt.setString(3, kw);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addMember(Member member) {
        String sql = "INSERT INTO member (kode_member, nama_member, no_telepon, alamat, poin, total_belanja) VALUES (?,?,?,?,?,?)";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, member.getKodeMember());
            stmt.setString(2, member.getNamaMember());
            stmt.setString(3, member.getNoTelepon());
            stmt.setString(4, member.getAlamat());
            stmt.setInt(5, member.getPoin());
            stmt.setDouble(6, member.getTotalBelanja());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMember(Member member) {
        String sql = "UPDATE member SET kode_member=?, nama_member=?, no_telepon=?, alamat=?, poin=? WHERE id_member=?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, member.getKodeMember());
            stmt.setString(2, member.getNamaMember());
            stmt.setString(3, member.getNoTelepon());
            stmt.setString(4, member.getAlamat());
            stmt.setInt(5, member.getPoin());
            stmt.setInt(6, member.getIdMember());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMember(int id) {
        String sql = "DELETE FROM member WHERE id_member = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countMember() {
        String sql = "SELECT COUNT(*) FROM member";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Generates next member code e.g. MBR-0006
     */
    public String generateKodeMember() {
        String sql = "SELECT COUNT(*) FROM member";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return "MBR-" + String.format("%04d", count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "MBR-0001";
    }

    private Member map(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setIdMember(rs.getInt("id_member"));
        m.setKodeMember(rs.getString("kode_member"));
        m.setNamaMember(rs.getString("nama_member"));
        m.setNoTelepon(rs.getString("no_telepon"));
        m.setAlamat(rs.getString("alamat"));
        m.setPoin(rs.getInt("poin"));
        m.setTotalBelanja(rs.getDouble("total_belanja"));
        return m;
    }
}