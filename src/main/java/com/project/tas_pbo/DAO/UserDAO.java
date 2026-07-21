package com.project.tas_pbo.DAO;

import com.project.tas_pbo.database.DBconnection;
import com.project.tas_pbo.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// DAO untuk operasi database tabel users
public class UserDAO {

    // Login user dengan username dan password
    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE status = 1 AND username = ? AND password = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Mengambil semua user aktif
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 1 ORDER BY id_user ASC";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Mencari user berdasarkan username atau nama lengkap
    public List<User> searchUser(String keyword) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 1 AND (username LIKE ? OR nama_lengkap LIKE ?) ORDER BY id_user ASC";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            stmt.setString(1, like);
            stmt.setString(2, like);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Menambahkan user baru
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, nama_lengkap, role, status) VALUES (?, ?, ?, ?, 1)";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getNamaLengkap());
            stmt.setString(4, user.getRole());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mengupdate data user
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, nama_lengkap = ?, role = ? WHERE id_user = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getNamaLengkap());
            stmt.setString(3, user.getRole());
            stmt.setInt(4, user.getIdUser());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mengganti password user
    public boolean updateUserPassword(int idUser, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id_user = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setInt(2, idUser);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Menghapus user (soft delete - set status = 0)
    public boolean deleteUser(int idUser) {
        String sql = "UPDATE users SET status = 0 WHERE id_user = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUser);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mengambil semua user yang dihapus
    public List<User> getDeletedUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 0 ORDER BY id_user ASC";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Memulihkan user yang dihapus (set status = 1)
    public boolean restoreUser(int idUser) {
        String sql = "UPDATE users SET status = 1 WHERE id_user = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUser);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Memetakan hasil query ke objek User
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setIdUser(rs.getInt("id_user"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setNamaLengkap(rs.getString("nama_lengkap"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getInt("status"));
        return user;
    }
}