package com.chaquo.python.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.util.List;

public class BaiTest {
    private String MaTest; // Trường lưu Document ID
    private String TieuDe;
    private String LoaiTest;
    private String MoTaLoai;
    private String HinhAnh;
    private int ThoiGian;
    private String TrangThai;
    private int SoLuongCauHoi;
    private List<CauHoi> DanhSachCauHoi;

    public BaiTest() {}

    @Exclude
    public String getMaTest() { return MaTest; }
    @Exclude
    public void setMaTest(String MaTest) { this.MaTest = MaTest; }

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

    @PropertyName("DanhSachCauHoi")
    public List<CauHoi> getDanhSachCauHoi() { return DanhSachCauHoi; }
    @PropertyName("DanhSachCauHoi")
    public void setDanhSachCauHoi(List<CauHoi> DanhSachCauHoi) { this.DanhSachCauHoi = DanhSachCauHoi; }
}