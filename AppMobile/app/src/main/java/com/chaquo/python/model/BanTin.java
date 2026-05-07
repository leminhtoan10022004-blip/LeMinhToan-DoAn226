package com.chaquo.python.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class BanTin {
    private String TieuDe;
    private String TomTat;
    private String LoaiTin;
    private String HinhAnh;
    private Object NgayDang;

    public BanTin() {}

    @PropertyName("TieuDe")
    public String getTieuDe() { return TieuDe; }
    @PropertyName("TieuDe")
    public void setTieuDe(String TieuDe) { this.TieuDe = TieuDe; }

    @PropertyName("TomTat")
    public String getTomTat() { return TomTat; }
    @PropertyName("TomTat")
    public void setTomTat(String TomTat) { this.TomTat = TomTat; }

    @PropertyName("LoaiTin")
    public String getLoaiTin() { return LoaiTin; }
    @PropertyName("LoaiTin")
    public void setLoaiTin(String LoaiTin) { this.LoaiTin = LoaiTin; }

    @PropertyName("HinhAnh")
    public String getHinhAnh() { return HinhAnh; }
    @PropertyName("HinhAnh")
    public void setHinhAnh(String HinhAnh) { this.HinhAnh = HinhAnh; }

    @PropertyName("NgayDang")
    public Object getNgayDang() { return NgayDang; }
    @PropertyName("NgayDang")
    public void setNgayDang(Object NgayDang) { this.NgayDang = NgayDang; }

    public String getNgayDangAsString() {
        if (NgayDang instanceof String) return (String) NgayDang;
        if (NgayDang instanceof Timestamp) return ((Timestamp) NgayDang).toDate().toString();
        return "";
    }
}
