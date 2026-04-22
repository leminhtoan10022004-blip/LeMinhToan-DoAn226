package com.chaquo.python.model;

import com.google.firebase.firestore.PropertyName;
import java.util.Map;

public class KetQuaTest {
    private String MaNguoiDung;
    private String MaTest;
    private Map<String, Integer> KetQuaChiTiet;
    private long NgayLam;

    public KetQuaTest() {}

    @PropertyName("MaNguoiDung")
    public String getMaNguoiDung() { return MaNguoiDung; }
    @PropertyName("MaNguoiDung")
    public void setMaNguoiDung(String MaNguoiDung) { this.MaNguoiDung = MaNguoiDung; }

    @PropertyName("MaTest")
    public String getMaTest() { return MaTest; }
    @PropertyName("MaTest")
    public void setMaTest(String MaTest) { this.MaTest = MaTest; }

    @PropertyName("KetQuaChiTiet")
    public Map<String, Integer> getKetQuaChiTiet() { return KetQuaChiTiet; }
    @PropertyName("KetQuaChiTiet")
    public void setKetQuaChiTiet(Map<String, Integer> KetQuaChiTiet) { this.KetQuaChiTiet = KetQuaChiTiet; }

    @PropertyName("NgayLam")
    public long getNgayLam() { return NgayLam; }
    @PropertyName("NgayLam")
    public void setNgayLam(long NgayLam) { this.NgayLam = NgayLam; }
}