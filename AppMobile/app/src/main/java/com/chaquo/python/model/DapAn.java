package com.chaquo.python.model;

import com.google.firebase.firestore.PropertyName;

public class DapAn {
    private String MaDapAn;
    private String NoiDung;
    private String MaThangDo;
    private int GiaTri;

    public DapAn() {}

    @PropertyName("MaDapAn")
    public String getMaDapAn() { return MaDapAn; }
    @PropertyName("MaDapAn")
    public void setMaDapAn(String MaDapAn) { this.MaDapAn = MaDapAn; }

    @PropertyName("NoiDung")
    public String getNoiDung() { return NoiDung; }
    @PropertyName("NoiDung")
    public void setNoiDung(String NoiDung) { this.NoiDung = NoiDung; }

    @PropertyName("MaThangDo")
    public String getMaThangDo() { return MaThangDo; }
    @PropertyName("MaThangDo")
    public void setMaThangDo(String MaThangDo) { this.MaThangDo = MaThangDo; }

    @PropertyName("GiaTri")
    public int getGiaTri() { return GiaTri; }
    @PropertyName("GiaTri")
    public void setGiaTri(int GiaTri) { this.GiaTri = GiaTri; }
}