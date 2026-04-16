package com.chaquo.python.model;

import com.google.firebase.firestore.PropertyName;

public class BaiTest {
    private String TieuDe;
    private String LoaiTest;
    private String MoTaLoai;
    private String HinhAnh;
    private int ThoiGian;
    private String TrangThai;
    private int SoLuongCauHoi;

    public BaiTest() {}

    @PropertyName("TieuDe")
    public String getTieuDe() { return TieuDe; }
    @PropertyName("TieuDe")
    public void setTieuDe(String TieuDe) { this.TieuDe = TieuDe; }

    @PropertyName("LoaiTest")
    public String getLoaiTest() { return LoaiTest; }
    @PropertyName("LoaiTest")
    public void setLoaiTest(String LoaiTest) { this.LoaiTest = LoaiTest; }

    @PropertyName("MoTaLoai")
    public String getMoTaLoai() { return MoTaLoai; }
    @PropertyName("MoTaLoai")
    public void setMoTaLoai(String MoTaLoai) { this.MoTaLoai = MoTaLoai; }

    @PropertyName("HinhAnh")
    public String getHinhAnh() { return HinhAnh; }
    @PropertyName("HinhAnh")
    public void setHinhAnh(String HinhAnh) { this.HinhAnh = HinhAnh; }

    @PropertyName("ThoiGian")
    public int getThoiGian() { return ThoiGian; }
    @PropertyName("ThoiGian")
    public void setThoiGian(int ThoiGian) { this.ThoiGian = ThoiGian; }

    @PropertyName("TrangThai")
    public String getTrangThai() { return TrangThai; }
    @PropertyName("TrangThai")
    public void setTrangThai(String TrangThai) { this.TrangThai = TrangThai; }

    @PropertyName("SoLuongCauHoi")
    public int getSoLuongCauHoi() { return SoLuongCauHoi; }
    @PropertyName("SoLuongCauHoi")
    public void setSoLuongCauHoi(int SoLuongCauHoi) { this.SoLuongCauHoi = SoLuongCauHoi; }
}