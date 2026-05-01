from paddleocr import PaddleOCR
from transformers import pipeline
import json
import cv2
import os

# Khởi tạo OCR (nên để global để tránh khởi tạo lại nhiều lần)
_ocr = None
_llm = None

def get_ocr():
    global _ocr
    if _ocr is None:
        # Tắt enable_mkldnn vì không hỗ trợ trên Android/ARM thông thường qua pip
        _ocr = PaddleOCR(lang='vi', use_angle_cls=True, show_log=False)
    return _ocr

def get_llm():
    global _llm
    if _llm is None:
        # Lưu ý: google/flan-t5-base rất nặng (~1GB). 
        # Trên mobile có thể gặp lỗi bộ nhớ (OOM).
        _llm = pipeline("text2text-generation", model="google/flan-t5-base")
    return _llm

def process_transcript_image(image_path):
    """
    Quy trình: Ảnh -> PaddleOCR -> LLM -> JSON
    """
    try:
        if not os.path.exists(image_path):
            return json.dumps({"error": f"File không tồn tại: {image_path}"})

        # 1. OCR
        ocr = get_ocr()
        result = ocr.ocr(image_path, cls=True)

        if not result or not result[0]:
            return json.dumps({"error": "Không nhận dạng được chữ nào trong ảnh."})

        # Gom text từ kết quả OCR
        ocr_text = "\n".join([line[1][0] for line in result[0]])

        # 2. LLM (Chuyển đổi sang JSON)
        llm = get_llm()
        
        prompt = f"""
        Chuyển bảng điểm sau thành JSON với cấu trúc chính xác:
        {{
            "hoc_sinh": "tên học sinh",
            "nam_hoc": "năm học",
            "bang_diem": [
                {{"mon_hoc": "tên môn", "diem_hk1": số, "diem_hk2": số}}
            ]
        }}

        Chỉ trả về JSON, không thêm text giải thích.

        Nội dung bảng điểm:
        {ocr_text}

        JSON:
        """

        response = llm(prompt, max_length=1000)[0]['generated_text']

        # 3. Parse JSON từ response của LLM
        try:
            start_idx = response.find('{')
            end_idx = response.rfind('}') + 1
            if start_idx != -1 and end_idx != 0:
                json_str = response[start_idx:end_idx]
                ket_qua = json.loads(json_str)
                # Trả về JSON string để Java/Kotlin dễ xử lý
                return json.dumps(ket_qua, ensure_ascii=False)
            else:
                return json.dumps({"error": "LLM không trả về định dạng JSON hợp lệ", "raw_response": response})
        except Exception as e:
            return json.dumps({"error": f"Lỗi parse JSON: {str(e)}", "raw_response": response})

    except Exception as e:
        return json.dumps({"error": f"Lỗi hệ thống: {str(e)}"})
