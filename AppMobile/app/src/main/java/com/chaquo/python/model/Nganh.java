package com.chaquo.python.model;

import com.google.firebase.firestore.PropertyName;

public class Nganh {
    private String MaNganh;
    private String TenNganh;
    private String MoTa;

    public Nganh() {}

    @PropertyName("MaNganh")
    public String getMaNganh() { return MaNganh; }
    @PropertyName("MaNganh")
    public void setMaNganh(String MaNganh) { this.MaNganh = MaNganh; }

    @PropertyName("TenNganh")
    public String getTenNganh() { return TenNganh; }
    @PropertyName("TenNganh")
    public void setTenNganh(String TenNganh) { this.TenNganh = TenNganh; }

    @PropertyName("MoTa")
    public String getMoTa() { return MoTa; }
    @PropertyName("MoTa")
    public void setMoTa(String MoTa) { this.MoTa = MoTa; }
}