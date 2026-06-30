package com.project.tas_pbo.util;

import com.project.tas_pbo.model.User;

public class Session {

    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getIdUser() : 1;
    }

    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getNamaLengkap() : "Unknown";
    }

    public static String getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : "";
    }

    public static void clear() {
        currentUser = null;
    }
}