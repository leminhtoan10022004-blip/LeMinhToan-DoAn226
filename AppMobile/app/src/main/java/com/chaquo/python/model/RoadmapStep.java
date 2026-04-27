package com.chaquo.python.model;

import com.google.firebase.firestore.PropertyName;
import java.util.List;

public class RoadmapStep {
    private int BuocSo;
    private String TenBuoc;
    private String MoTa;
    private String ThoiGian; // Thêm thời gian dự kiến
    private List<String> KyNang; // Thêm danh sách kỹ năng cần đạt
    private boolean isCompleted; // Trạng thái hoàn thành (để người dùng tương tác)

    public RoadmapStep() {}

    @PropertyName("BuocSo")
    public int getBuocSo() { return BuocSo; }
    @PropertyName("BuocSo")
    public void setBuocSo(int BuocSo) { this.BuocSo = BuocSo; }

    @PropertyName("TenBuoc")
    public String getTenBuoc() { return TenBuoc; }
    @PropertyName("TenBuoc")
    public void setTenBuoc(String TenBuoc) { this.TenBuoc = TenBuoc; }

    @PropertyName("MoTa")
    public String getMoTa() { return MoTa; }
    @PropertyName("MoTa")
    public void setMoTa(String MoTa) { this.MoTa = MoTa; }

    @PropertyName("ThoiGian")
    public String getThoiGian() { return ThoiGian; }
    @PropertyName("ThoiGian")
    public void setThoiGian(String ThoiGian) { this.ThoiGian = ThoiGian; }

    @PropertyName("KyNang")
    public List<String> getKyNang() { return KyNang; }
    @PropertyName("KyNang")
    public void setKyNang(List<String> KyNang) { this.KyNang = KyNang; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
