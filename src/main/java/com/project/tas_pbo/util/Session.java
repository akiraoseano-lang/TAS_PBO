package com.project.tas_pbo.util;

import com.project.tas_pbo.model.User;

// Menyimpan informasi user yang sedang login (session)
public class Session {

    private static User currentUser;

    // Menyimpan user yang login
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Mengambil user yang sedang login
    public static User getCurrentUser() {
        return currentUser;
    }

    // Mengambil ID user yang login
    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getIdUser() : 1;
    }

    // Mengambil nama lengkap user yang login
    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getNamaLengkap() : "Unknown";
    }

    // Mengambil role user yang login
    public static String getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : "";
    }

    // Menghapus session (logout)
    public static void clear() {
        currentUser = null;
    }
}