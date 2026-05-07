package com.chaquo.python.model;

import com.google.firebase.firestore.PropertyName;

public class CongViec {
    private String MaCongViec;
    private String MaNganh;
    private String TenCongViec;
    private String MoTa;
    private String DoHot;
    private long LuongToiThieu;
    private long LuongToiDa;
    private String YeuCauDaoTao;
    private String HinhAnh;

    public CongViec() {}

    @PropertyName("MaCongViec")
    public String getMaCongViec() { return MaCongViec; }
    @PropertyName("MaCongViec")
    public void setMaCongViec(String MaCongViec) { this.MaCongViec = MaCongViec; }

    @PropertyName("MaNganh")
    public String getMaNganh() { return MaNganh; }
    @PropertyName("MaNganh")
    public void setMaNganh(String MaNganh) { this.MaNganh = MaNganh; }

    @PropertyName("TenCongViec")
    public String getTenCongViec() { return TenCongViec; }
    @PropertyName("TenCongViec")
    public void setTenCongViec(String TenCongViec) { this.TenCongViec = TenCongViec; }

    @PropertyName("MoTa")
    public String getMoTa() { return MoTa; }
    @PropertyName("MoTa")
    public void setMoTa(String MoTa) { this.MoTa = MoTa; }

    @PropertyName("DoHot")
    public String getDoHot() { return DoHot; }
    @PropertyName("DoHot")
    public void setDoHot(String DoHot) { this.DoHot = DoHot; }

    @PropertyName("LuongToiThieu")
    public long getLuongToiThieu() { return LuongToiThieu; }
    @PropertyName("LuongToiThieu")
    public void setLuongToiThieu(long LuongToiThieu) { this.LuongToiThieu = LuongToiThieu; }

    @PropertyName("LuongToiDa")
    public long getLuongToiDa() { return LuongToiDa; }
    @PropertyName("LuongToiDa")
    public void setLuongToiDa(long LuongToiDa) { this.LuongToiDa = LuongToiDa; }

    @PropertyName("YeuCauDaoTao")
    public String getYeuCauDaoTao() { return YeuCauDaoTao; }
    @PropertyName("YeuCauDaoTao")
    public void setYeuCauDaoTao(String YeuCauDaoTao) { this.YeuCauDaoTao = YeuCauDaoTao; }

    @PropertyName("HinhAnh")
    public String getHinhAnh() { return HinhAnh; }
    @PropertyName("HinhAnh")
    public void setHinhAnh(String HinhAnh) { this.HinhAnh = HinhAnh; }
}