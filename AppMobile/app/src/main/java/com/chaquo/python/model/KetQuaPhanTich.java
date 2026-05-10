package com.chaquo.python.model;

import com.google.firebase.firestore.PropertyName;
import java.util.Map;

public class KetQuaPhanTich {
    private String MaKetQua;
    private Map<String, Object> DuLieuChiTiet;
    private Map<String, Integer> KetQuaChiTiet;
    private String MaNganhPhuHop;

    public KetQuaPhanTich() {}

    @PropertyName("MaKetQua")
    public String getMaKetQua() { return MaKetQua; }
    @PropertyName("MaKetQua")
    public void setMaKetQua(String MaKetQua) { this.MaKetQua = MaKetQua; }

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
}
