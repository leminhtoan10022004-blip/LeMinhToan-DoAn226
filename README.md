# AppDinhHuongNgheNghiep

- Career Orientation AI System là một hệ thống hỗ trợ định hướng nghề nghiệp thông minh, kết hợp giữa các bài kiểm tra tâm lý chuẩn quốc tế và sức mạnh của Machine Learning. Ứng dụng phân tích dữ liệu đa chiều (năng lực học tập, tính cách, sở thích) để đưa ra dự đoán nghề nghiệp và lộ trình phát triển cá nhân hóa cho học sinh, sinh viên.

- Hệ thống được thiết kế với cấu trúc dữ liệu chặt chẽ trên Firebase, tối ưu hóa cho việc lưu trữ thông tin người dùng, kết quả trắc nghiệm và dữ liệu huấn luyện cho mô hình AI.

- Các thực thể chính:
  Hệ thống Người dùng:

* NguoiDung: Lưu trữ thông tin cá nhân, tài khoản và vai trò.

* VaiTro: Quản lý phân quyền (Admin, SinhVien, ChuyenGia).

Đánh giá Năng lực & Tính cách:

- BangDiem: Lưu trữ điểm số các môn học (Toán, Lý, Hóa, Anh...) theo từng học kỳ/năm học.

- BaiTest & CauTraLoi: Chứa dữ liệu của các bộ trắc nghiệm định hướng (MBTI, Holland, Big Five).

- LichSuLamBai: Ghi lại kết quả và thời gian làm test của người dùng.

Hệ thống Nghề nghiệp & Lộ trình:

- CongViec: Thông tin chi tiết về hơn 25 ngành nghề phổ biến, bao gồm mô tả, độ hot, mức lương và yêu cầu đào tạo.

- LoTrinh: Lưu trữ lộ trình phát triển chi tiết (Roadmap) dạng JSON, được phân chia theo từng giai đoạn cụ thể (Nền tảng, Chuyên môn, Nâng cao).

- Nganh & KyNang: Phân loại nghề nghiệp theo lĩnh vực và các kỹ năng cốt lõi cần thiết.

Dữ liệu AI:

- DuLieuHuanLuyen: Tập hợp dữ liệu tổng hợp (Profile + MBTI + Holland + Labels nghề nghiệp) dùng để tối ưu hóa mô hình gợi ý.
