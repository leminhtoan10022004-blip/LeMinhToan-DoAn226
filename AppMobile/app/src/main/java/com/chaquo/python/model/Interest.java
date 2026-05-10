package com.chaquo.python.model;

public class Interest {
    private String MaCongViec;
    private String TenCongViec;
    private long ThoiGian;

    public Interest() {}

    public Interest(String maCongViec, String tenCongViec, long thoiGian) {
        this.MaCongViec = maCongViec;
        this.TenCongViec = tenCongViec;
        this.ThoiGian = thoiGian;
    }

    public String getMaCongViec() {
        return MaCongViec;
    }

    public void setMaCongViec(String maCongViec) {
        MaCongViec = maCongViec;
    }

    public String getTenCongViec() {
        return TenCongViec;
    }

    public void setTenCongViec(String tenCongViec) {
        TenCongViec = tenCongViec;
    }

    public long getThoiGian() {
        return ThoiGian;
    }

    public void setThoiGian(long thoiGian) {
        ThoiGian = thoiGian;
    }
}
