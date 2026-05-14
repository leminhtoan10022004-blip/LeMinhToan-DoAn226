package com.chaquo.python.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import java.util.Date;
import java.util.Map;

public class KetQuaPhanTich {
    private String MaKetQua;
    private String MaNguoiDung;
    private String MaTest;
    private Map<String, Object> DuLieuChiTiet;
    private Map<String, Integer> KetQuaChiTiet;
    private String MaNganhPhuHop;
    private Timestamp NgayThucHien;

    public KetQuaPhanTich() {}

    @PropertyName("MaKetQua")
    public String getMaKetQua() { return MaKetQua; }
    @PropertyName("MaKetQua")
    public void setMaKetQua(String MaKetQua) { this.MaKetQua = MaKetQua; }

    @PropertyName("MaNguoiDung")
    public String getMaNguoiDung() { return MaNguoiDung; }
    @PropertyName("MaNguoiDung")
    public void setMaNguoiDung(String MaNguoiDung) { this.MaNguoiDung = MaNguoiDung; }

    @PropertyName("MaTest")
    public String getMaTest() { return MaTest; }
    @PropertyName("MaTest")
    public void setMaTest(String MaTest) { this.MaTest = MaTest; }

    @PropertyName("DuLieuChiTiet")
    public Map<String, Object> getDuLieuChiTiet() { return DuLieuChiTiet; }
    @PropertyName("DuLieuChiTiet")
    public void setDuLieuChiTiet(Map<String, Object> DuLieuChiTiet) { this.DuLieuChiTiet = DuLieuChiTiet; }

    @PropertyName("KetQuaChiTiet")
    public Map<String, Integer> getKetQuaChiTiet() { return KetQuaChiTiet; }
    @PropertyName("KetQuaChiTiet")
    public void setKetQuaChiTiet(Map<String, Integer> KetQuaChiTiet) { this.KetQuaChiTiet = KetQuaChiTiet; }

    @PropertyName("MaNganhPhuHop")
    public String getMaNganhPhuHop() { return MaNganhPhuHop; }
    @PropertyName("MaNganhPhuHop")
    public void setMaNganhPhuHop(String MaNganhPhuHop) { this.MaNganhPhuHop = MaNganhPhuHop; }

    @PropertyName("NgayThucHien")
    public Timestamp getNgayThucHien() { return NgayThucHien; }
    
    @PropertyName("NgayThucHien")
    public void setNgayThucHien(Object value) {
        if (value instanceof Timestamp) {
            this.NgayThucHien = (Timestamp) value;
        } else if (value instanceof Long) {
            this.NgayThucHien = new Timestamp(new Date((Long) value));
        }
    }
}
