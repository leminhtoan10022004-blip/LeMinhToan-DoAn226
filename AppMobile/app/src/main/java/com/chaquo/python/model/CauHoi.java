package com.chaquo.python.model;

import com.google.firebase.firestore.PropertyName;
import java.util.List;

public class CauHoi {
    private String MaCauHoi;
    private String NoiDung;
    private int ThuTu;
    private List<DapAn> DapAn;

    public CauHoi() {}

    @PropertyName("MaCauHoi")
    public String getMaCauHoi() { return MaCauHoi; }
    @PropertyName("MaCauHoi")
    public void setMaCauHoi(String MaCauHoi) { this.MaCauHoi = MaCauHoi; }

    @PropertyName("NoiDung")
    public String getNoiDung() { return NoiDung; }
    @PropertyName("NoiDung")
    public void setNoiDung(String NoiDung) { this.NoiDung = NoiDung; }

    @PropertyName("ThuTu")
    public int getThuTu() { return ThuTu; }
    @PropertyName("ThuTu")
    public void setThuTu(int ThuTu) { this.ThuTu = ThuTu; }

    @PropertyName("DapAn")
    public List<DapAn> getDapAn() { return DapAn; }
    @PropertyName("DapAn")
    public void setDapAn(List<DapAn> DapAn) { this.DapAn = DapAn; }
}