package com.project.tas_pbo.model;

import java.sql.Timestamp;

public class Member {
    private int idMember;
    private String kodeMember;
    private String namaMember;
    private String noTelepon;
    private String alamat;
    private int poin;
    private double totalBelanja;
    private Timestamp createdAt;

    public Member() {}

    public Member(int idMember, String kodeMember, String namaMember, String noTelepon, String alamat, int poin, double totalBelanja, Timestamp createdAt) {
        this.idMember = idMember;
        this.kodeMember = kodeMember;
        this.namaMember = namaMember;
        this.noTelepon = noTelepon;
        this.alamat = alamat;
        this.poin = poin;
        this.totalBelanja = totalBelanja;
        this.createdAt = createdAt;
    }

    public int getIdMember() { return idMember; }
    public void setIdMember(int idMember) { this.idMember = idMember; }

    public String getKodeMember() { return kodeMember; }
    public void setKodeMember(String kodeMember) { this.kodeMember = kodeMember; }

    public String getNamaMember() { return namaMember; }
    public void setNamaMember(String namaMember) { this.namaMember = namaMember; }

    public String getNoTelepon() { return noTelepon; }
    public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public int getPoin() { return poin; }
    public void setPoin(int poin) { this.poin = poin; }

    public double getTotalBelanja() { return totalBelanja; }
    public void setTotalBelanja(double totalBelanja) { this.totalBelanja = totalBelanja; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
