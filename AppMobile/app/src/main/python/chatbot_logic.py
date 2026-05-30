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
    # SỬ DỤNG MODEL GEMINI-2.5-FLASH THEO YÊU CẦU
    model_name = "gemini-2.5-flash"
    url = f"https://generativelanguage.googleapis.com/v1beta/models/{model_name}:generateContent?key={GEMINI_API_KEY}"

    payload = {
        "contents": contents,
        "system_instruction": {
            "role": "user",
            "parts": [{"text": system_instruction}]
        },
        "generationConfig": {
            "temperature": 0.3, # Giảm xuống để AI trả lời thực tế, bám sát dữ liệu
            "maxOutputTokens": 2048,
            "topP": 0.95,
        }
    }

    try:
        print(f"Calling Gemini API with model: {model_name}")
        response = requests.post(url, headers=headers, json=payload, timeout=30)
        if response.status_code == 200:
            result = response.json()
            return result['candidates'][0]['content']['parts'][0]['text']
        
        error_msg = f"Lỗi AI ({response.status_code}): {response.text}"
        print(error_msg)
        return error_msg
    except Exception as e:
        error_msg = f"Lỗi kết nối AI: {str(e)}"
        print(error_msg)
        return error_msg

def get_bot_response(user_input, user_data_json, chat_history_json):
    try:
        data = json.loads(user_data_json) if user_data_json else {}
        history = json.loads(chat_history_json) if chat_history_json else []

        profile = data.get("profile", {})
        last_name = profile.get("Ho", "")
        first_name = profile.get("Ten", "bạn")
        full_name = f"{last_name} {first_name}".strip()

        ma_nganh = data.get("predicted_career_id", "chưa xác định")
        ten_nganh = NGANH_MAP.get(ma_nganh, ma_nganh)

        test_results = data.get("test_results", {})
        scores_info = ""
        if test_results:
            scores_info = " Điểm trắc nghiệm: " + ", ".join([f"{k}: {v}" for k, v in test_results.items()])

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

def get_trend_prediction(trend_data_json, news_data_json):
    """Hàm mới: Dự báo xu hướng thị trường lao động bằng AI"""
    try:
        print("Starting trend prediction analysis...")
        trends = json.loads(trend_data_json)
        news = json.loads(news_data_json)
        
        system_instruction = (
            "Bạn là chuyên gia phân tích dữ liệu thị trường lao động thực dụng. "
            "NHIỆM VỤ: Dựa trên dữ liệu cung cấp, hãy đưa ra phân tích xu hướng cụ thể, sắc bén. "
            "QUY TẮC BẮT BUỘC: "
            "1. KHÔNG chào hỏi, KHÔNG giới thiệu bản thân (Tuyệt đối không có 'Chào bạn', 'Tôi là...'). "
            "2. ĐI THẲNG VÀO PHÂN TÍCH. Không giải thích cách làm. "
            "3. Sử dụng Markdown (###) và danh sách gạch đầu dòng rõ ràng. "
            "4. Phân tích cụ thể dựa trên số liệu lượt quan tâm từ Firebase và tin tức. "
            "5. Tập trung vào các từ khóa: AI, Bán dẫn, Logistic, Năng lượng xanh, Chuyển đổi số. "
            "\nCấu trúc phản hồi: \n"
            "### 🚀 Top Ngành Nghề Bùng Nổ (2025-2026)\n"
            "### 📈 Phân Tích Xu Hướng Thị Trường\n"
            "### 🛠️ Kỹ Năng 'Vàng' Cần Trang Bị\n"
            "### 💡 Lời Khuyên Định Hướng"
        )
        
        prompt = (
            f"DỮ LIỆU THỰC TẾ TỪ FIREBASE:\n{json.dumps(trends, ensure_ascii=False)}\n\n"
            f"TIN TỨC THỊ TRƯỜNG:\n{json.dumps(news, ensure_ascii=False)}\n\n"
            "HÃY THỰC HIỆN PHÂN TÍCH VÀ TRẢ LỜI NGAY LẬP TỨC THEO CẤU TRÚC."
        )
        
        contents = [{"role": "user", "parts": [{"text": prompt}]}]
        return call_gemini_api(contents, system_instruction)
    except Exception as e:
        error_msg = f"Lỗi AI Trend: {str(e)}"
        print(error_msg)
        return error_msg
