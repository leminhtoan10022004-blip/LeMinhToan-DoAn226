import json
import os

def process_transcript_image(image_path):
    """
    Mock OCR: Vì PaddleOCR và Transformers quá nặng cho Mobile, 
    chúng ta sẽ trả về kết quả giả lập để test luồng MLP.
    """
    try:
        # Giả lập thời gian xử lý
        import time
        time.sleep(1) 

        # Trả về kết quả bảng điểm mẫu để có thể chạy MLP
        ket_qua = {
            "hoc_sinh": "Học sinh mẫu",
            "nam_hoc": "2023-2024",
            "bang_diem": [
                {"mon_hoc": "Toán", "diem_tb": 8.5},
                {"mon_hoc": "Lý", "diem_tb": 8.0},
                {"mon_hoc": "Hóa", "diem_tb": 7.5},
                {"mon_hoc": "Văn", "diem_tb": 7.0},
                {"mon_hoc": "Anh", "diem_tb": 8.5},
                {"mon_hoc": "Sinh", "diem_tb": 6.5},
                {"mon_hoc": "Tin", "diem_tb": 9.0},
                {"mon_hoc": "Địa", "diem_tb": 7.0},
                {"mon_hoc": "Sử", "diem_tb": 6.0}
            ]
        }
        return json.dumps(ket_qua, ensure_ascii=False)

    except Exception as e:
        return json.dumps({"error": f"Lỗi hệ thống: {str(e)}"})
