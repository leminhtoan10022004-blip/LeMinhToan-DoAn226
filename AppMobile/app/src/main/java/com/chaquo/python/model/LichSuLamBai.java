package com.chaquo.python.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import java.util.Date;

public class LichSuLamBai {
    private String MaLichSu;
    private String MaNguoiDung;
    private String MaTest;
    private Timestamp ThoiGianBD;
    private Timestamp ThoiGianKT;
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
    public Timestamp getThoiGianBD() { return ThoiGianBD; }

    @PropertyName("ThoiGianBD")
    public void setThoiGianBD(Object value) {
        if (value instanceof Timestamp) {
            this.ThoiGianBD = (Timestamp) value;
        } else if (value instanceof Long) {
            this.ThoiGianBD = new Timestamp(new Date((Long) value));
        }
    }

    @PropertyName("ThoiGianKT")
    public Timestamp getThoiGianKT() { return ThoiGianKT; }
    
    @PropertyName("ThoiGianKT")
    public void setThoiGianKT(Object value) {
        if (value instanceof Timestamp) {
            this.ThoiGianKT = (Timestamp) value;
        } else if (value instanceof Long) {
            this.ThoiGianKT = new Timestamp(new Date((Long) value));
        }
    }

    @PropertyName("MaKetQua")
    public String getMaKetQua() { return MaKetQua; }
    @PropertyName("MaKetQua")
    public void setMaKetQua(String MaKetQua) { this.MaKetQua = MaKetQua; }

    @PropertyName("TrangThai")
    public String getTrangThai() { return TrangThai; }
    @PropertyName("TrangThai")
    public void setTrangThai(String TrangThai) { this.TrangThai = TrangThai; }
}
