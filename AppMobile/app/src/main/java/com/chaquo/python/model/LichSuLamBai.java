package com.chaquo.python.model;

import com.google.firebase.firestore.PropertyName;

public class LichSuLamBai {
    private String MaLichSu;
    private String MaNguoiDung;
    private String MaTest;
    private long ThoiGianBD;
    private long ThoiGianKT;
    private String MaKetQua;
    private String TrangThai;

    public LichSuLamBai() {}

    @PropertyName("MaLichSu")
    public String getMaLichSu() { return MaLichSu; }
    @PropertyName("MaLichSu")
    public void setMaLichSu(String MaLichSu) { this.MaLichSu = MaLichSu; }

    @PropertyName("MaNguoiDung")
    public String getMaNguoiDung() { return MaNguoiDung; }
    @PropertyName("MaNguoiDung")
    public void setMaNguoiDung(String MaNguoiDung) { this.MaNguoiDung = MaNguoiDung; }

    @PropertyName("MaTest")
    public String getMaTest() { return MaTest; }
    @PropertyName("MaTest")
    public void setMaTest(String MaTest) { this.MaTest = MaTest; }

    @PropertyName("ThoiGianBD")
    public long getThoiGianBD() { return ThoiGianBD; }
    @PropertyName("ThoiGianBD")
    public void setThoiGianBD(long ThoiGianBD) { this.ThoiGianBD = ThoiGianBD; }

    @PropertyName("ThoiGianKT")
    public long getThoiGianKT() { return ThoiGianKT; }
    @PropertyName("ThoiGianKT")
    public void setThoiGianKT(long ThoiGianKT) { this.ThoiGianKT = ThoiGianKT; }

    @PropertyName("MaKetQua")
    public String getMaKetQua() { return MaKetQua; }
    @PropertyName("MaKetQua")
    public void setMaKetQua(String MaKetQua) { this.MaKetQua = MaKetQua; }

    @PropertyName("TrangThai")
    public String getTrangThai() { return TrangThai; }
    @PropertyName("TrangThai")
    public void setTrangThai(String TrangThai) { this.TrangThai = TrangThai; }
}
