package com.chaquo.python.model;

import com.google.firebase.firestore.PropertyName;

public class RoadmapStep {
    private int BuocSo;
    private String TenBuoc;
    private String MoTa;

    public RoadmapStep() {}

    @PropertyName("BuocSo")
    public int getBuocSo() { return BuocSo; }
    @PropertyName("BuocSo")
    public void setBuocSo(int BuocSo) { this.BuocSo = BuocSo; }

    @PropertyName("TenBuoc")
    public String getTenBuoc() { return TenBuoc; }
    @PropertyName("TenBuoc")
    public void setTenBuoc(String TenBuoc) { this.TenBuoc = TenBuoc; }

    @PropertyName("MoTa")
    public String getMoTa() { return MoTa; }
    @PropertyName("MoTa")
    public void setMoTa(String MoTa) { this.MoTa = MoTa; }
}