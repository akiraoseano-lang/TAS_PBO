package com.project.tas_pbo.model;

// Model untuk tabel users
public class User extends BaseModel {

    private int idUser;
    private String username;
    private String password;
    private String namaLengkap;
    private String role;
    private int status = 1;

    public User() {}

    public User(int idUser, String username, String password, String namaLengkap, String role) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
        this.namaLengkap = namaLengkap;
        this.role = role;
    }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNamaLengkap() { return namaLengkap; }
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    @Override
    public int getId() { return idUser; }

    @Override
    public String getDisplayInfo() { return "User: " + namaLengkap + " (" + role + ")"; }
}