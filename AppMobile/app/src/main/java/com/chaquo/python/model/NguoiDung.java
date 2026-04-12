package com.chaquo.python.model;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.Timestamp;

public class NguoiDung {
    private String MaNguoiDung;
    private String Ho;
    private String Ten;
    private String Email;
    private String MatKhau;
    private Object NgaySinh;
    private Object NgayTao;
    private String SDT;
    private String AnhDaiDien;
    private String VaiTro;
    private String TrangThai;

    public NguoiDung() {}


    @PropertyName("MaNguoiDung")
    public String getMaNguoiDung() { return MaNguoiDung; }
    @PropertyName("MaNguoiDung")
    public void setMaNguoiDung(String MaNguoiDung) { this.MaNguoiDung = MaNguoiDung; }

    @PropertyName("Ho")
    public String getHo() { return Ho; }
    @PropertyName("Ho")
    public void setHo(String Ho) { this.Ho = Ho; }

    @PropertyName("Ten")
    public String getTen() { return Ten; }
    @PropertyName("Ten")
    public void setTen(String Ten) { this.Ten = Ten; }

    @PropertyName("Email")
    public String getEmail() { return Email; }
    @PropertyName("Email")
    public void setEmail(String Email) { this.Email = Email; }

    @PropertyName("MatKhau")
    public String getMatKhau() { return MatKhau; }
    @PropertyName("MatKhau")
    public void setMatKhau(String MatKhau) { this.MatKhau = MatKhau; }

    @PropertyName("NgaySinh")
    public Object getNgaySinh() { return NgaySinh; }
    @PropertyName("NgaySinh")
    public void setNgaySinh(Object NgaySinh) { this.NgaySinh = NgaySinh; }

    @PropertyName("NgayTao")
    public Object getNgayTao() { return NgayTao; }
    @PropertyName("NgayTao")
    public void setNgayTao(Object NgayTao) { this.NgayTao = NgayTao; }

    @PropertyName("SDT")
    public String getSDT() { return SDT; }
    @PropertyName("SDT")
    public void setSDT(String SDT) { this.SDT = SDT; }

    @PropertyName("AnhDaiDien")
    public String getAnhDaiDien() { return AnhDaiDien; }
    @PropertyName("AnhDaiDien")
    public void setAnhDaiDien(String AnhDaiDien) { this.AnhDaiDien = AnhDaiDien; }

    @PropertyName("VaiTro")
    public String getVaiTro() { return VaiTro; }
    @PropertyName("VaiTro")
    public void setVaiTro(String VaiTro) { this.VaiTro = VaiTro; }

    @PropertyName("TrangThai")
    public String getTrangThai() { return TrangThai; }
    @PropertyName("TrangThai")
    public void setTrangThai(String TrangThai) { this.TrangThai = TrangThai; }

    public String getHoTen() {
        return (Ho != null ? Ho : "") + " " + (Ten != null ? Ten : "");
    }

    // Helper methods to handle potential String or Timestamp values
    public String getNgayTaoAsString() {
        if (NgayTao instanceof String) return (String) NgayTao;
        if (NgayTao instanceof Timestamp) return ((Timestamp) NgayTao).toDate().toString();
        return "";
    }

    public String getNgaySinhAsString() {
        if (NgaySinh instanceof String) return (String) NgaySinh;
        if (NgaySinh instanceof Timestamp) return ((Timestamp) NgaySinh).toDate().toString();
        return "";
    }
}