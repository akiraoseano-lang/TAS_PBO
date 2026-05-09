package com.project.tas_pbo.model;

public class User {

    private String username;
    private String password;
    private String noHp;

    public User(String username, String password, String noHp) {
        this.username = username;
        this.password = password;
        this.noHp = noHp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNoHp() {
        return noHp;
    }

    public void setNoHp(String noHp) {
        this.noHp = noHp;
    }

}
