package com.project.tas_pbo.DAO;

import com.project.tas_pbo.model.Member;
import com.project.tas_pbo.database.DBconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    public List<Member> getAllMember() {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM member ORDER BY id_member ASC";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Member m = new Member();
                m.setIdMember(rs.getInt("id_member"));
                m.setKodeMember(rs.getString("kode_member"));
                m.setNamaMember(rs.getString("nama_member"));
                m.setNoTelepon(rs.getString("no_telepon"));
                m.setAlamat(rs.getString("alamat"));
                m.setPoin(rs.getInt("poin"));
                m.setTotalBelanja(rs.getDouble("total_belanja"));
                m.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
