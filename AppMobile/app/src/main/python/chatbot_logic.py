import json
import requests

# BẮT BUỘC: API Key từ https://aistudio.google.com/
GEMINI_API_KEY = "AIzaSyCT2BwvVwyeWxRqNKCBs_NnagBXKkpFh-s"

# Ánh xạ mã ngành từ ERD.json để AI hiểu
NGANH_MAP = {
    "Nganh-001": "Công nghệ thông tin",
    "Nganh-002": "Kỹ thuật & Công nghệ",
    "Nganh-003": "Kinh tế & Tài chính & Luật",
    "Nganh-004": "Sáng tạo & Nghệ thuật",
    "Nganh-005": "Y tế & Giáo dục",
    "Nganh-006": "Khoa học xã hội & Truyền thông",
    "Nganh-007": "Dịch vụ & Du lịch"
}

def call_gemini_api(contents, system_instruction):
    headers = {'Content-Type': 'application/json'}
    # ĐÃ CẬP NHẬT: Sử dụng model gemini-2.5-flash theo yêu cầu
    model_name = "models/gemini-2.5-flash"
    url = f"https://generativelanguage.googleapis.com/v1beta/{model_name}:generateContent?key={GEMINI_API_KEY}"

    payload = {
        "contents": contents,
        "system_instruction": {
            "role": "user",
            "parts": [{"text": system_instruction}]
        },
        "generationConfig": {
            "temperature": 0.7,
            "maxOutputTokens": 2048,
            "topP": 0.95,
        }
    }

    try:
        response = requests.post(url, headers=headers, json=payload, timeout=30)
        if response.status_code == 200:
            result = response.json()
            return result['candidates'][0]['content']['parts'][0]['text']
        return f"Lỗi AI ({response.status_code}): {response.text}"
    except Exception as e:
        return f"Lỗi kết nối AI: {str(e)}"

def get_bot_response(user_input, user_data_json, chat_history_json):
    try:
        data = json.loads(user_data_json) if user_data_json else {}
        history = json.loads(chat_history_json) if chat_history_json else []

        # 1. Trích xuất thông tin người dùng từ collection "NguoiDung" theo ERD.json
        profile = data.get("profile", {})
        last_name = profile.get("Ho", "")
        first_name = profile.get("Ten", "bạn")
        full_name = f"{last_name} {first_name}".strip()

        # 2. Xử lý kết quả dự đoán ngành nghề
        ma_nganh = data.get("predicted_career_id", "chưa xác định")
        ten_nganh = NGANH_MAP.get(ma_nganh, ma_nganh)

        # 3. Xử lý điểm số trắc nghiệm từ "KetQuaPhanTich"
        test_results = data.get("test_results", {})
        scores_info = ""
        if test_results:
            scores_info = " Điểm trắc nghiệm: " + ", ".join([f"{k}: {v}" for k, v in test_results.items()])

        # 4. Tạo System Prompt cá nhân hóa theo dữ liệu ERD
        context = f"Người dùng: {full_name}. Ngành phù hợp nhất: {ten_nganh}.{scores_info}"

        system_prompt = (
            f"Bạn là trợ lý AI tư vấn hướng nghiệp chuyên sâu. "
            f"Thông tin người dùng từ Firestore: {context}. "
            f"Nhiệm vụ: Dựa vào điểm trắc nghiệm (Holland, MBTI, Big5, DISC) và ngành gợi ý để giải thích tại sao ngành đó phù hợp. "
            f"Hãy chào người dùng bằng tên thật của họ ({full_name}). "
            f"Gợi ý thêm về lộ trình, sách và trò chơi mô phỏng (như trong ERD.json) nếu cần. "
            f"Ngôn ngữ: Tiếng Việt, thân thiện, sử dụng Markdown."
        )

        formatted_contents = []
        for msg in history:
            formatted_contents.append(msg)
        formatted_contents.append({"role": "user", "parts": [{"text": user_input}]})

        return call_gemini_api(formatted_contents, system_prompt)

    except Exception as e:
        return f"Lỗi xử lý dữ liệu: {str(e)}"
