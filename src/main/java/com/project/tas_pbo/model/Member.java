package com.project.tas_pbo.model;

public class Member {

    private int idMember;
    private String kodeMember;
    private String namaMember;
    private String noTelepon;
    private String alamat;
    private int poin;
    private double totalBelanja;

    public Member() {}

    public Member(String kodeMember, String namaMember, String noTelepon, String alamat) {
        this.kodeMember = kodeMember;
        this.namaMember = namaMember;
        this.noTelepon = noTelepon;
        this.alamat = alamat;
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

    @Override
    public String toString() { return kodeMember + " - " + namaMember; }
}