package com.chaquo.python.model;

import com.google.firebase.Timestamp;

public class NguoiDungBanTin {
    private String MaNguoiDung;
    private String MaBanTin;
    private String TrangThai;
    private Timestamp NgayDocLanCuoi;
    private boolean YeuThich;

    private String TenCongViec; 

    public NguoiDungBanTin() {}

    public String getMaNguoiDung() { return MaNguoiDung; }
    public void setMaNguoiDung(String MaNguoiDung) { this.MaNguoiDung = MaNguoiDung; }

    public String getMaBanTin() { return MaBanTin; }
    public void setMaBanTin(String MaBanTin) { this.MaBanTin = MaBanTin; }

    public String getTrangThai() { return TrangThai; }
    public void setTrangThai(String TrangThai) { this.TrangThai = TrangThai; }

    public Timestamp getNgayDocLanCuoi() { return NgayDocLanCuoi; }
    public void setNgayDocLanCuoi(Timestamp NgayDocLanCuoi) { this.NgayDocLanCuoi = NgayDocLanCuoi; }

    public boolean isYeuThich() { return YeuThich; }
    public void setYeuThich(boolean YeuThich) { this.YeuThich = YeuThich; }

    public String getTenCongViec() { return TenCongViec; }
    public void setTenCongViec(String TenCongViec) { this.TenCongViec = TenCongViec; }
}
