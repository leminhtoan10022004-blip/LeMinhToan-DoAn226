import json
import requests

# BẮT BUỘC: API Key từ https://aistudio.google.com/
GEMINI_API_KEY = "AIzaSyCT2BwvVwyeWxRqNKCBs_NnagBXKkpFh-s"

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
    # GIỮ NGUYÊN MODEL GEMINI-2.5-FLASH THEO YÊU CẦU CỦA BẠN
    model_name = "gemini-2.5-flash"
    url = f"https://generativelanguage.googleapis.com/v1beta/models/{model_name}:generateContent?key={GEMINI_API_KEY}"

    payload = {
        "contents": contents,
        "system_instruction": {
            "role": "user",
            "parts": [{"text": system_instruction}]
        },
        "generationConfig": {
            "temperature": 0.4,
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
    """Hàm tư vấn chung"""
    try:
        data = json.loads(user_data_json) if user_data_json else {}
        history = json.loads(chat_history_json) if chat_history_json else []
        profile = data.get("profile", {})
        full_name = f"{profile.get('Ho', '')} {profile.get('Ten', 'bạn')}".strip()
        ma_nganh = data.get("predicted_career_id", "chưa xác định")
        ten_nganh = NGANH_MAP.get(ma_nganh, ma_nganh)

        system_prompt = (
            f"Bạn là trợ lý AI hướng nghiệp cho {full_name}. Ngành phù hợp là {ten_nganh}. "
            "Hãy chào người dùng và tư vấn dựa trên trắc nghiệm. Dùng Markdown."
        )

        formatted_contents = history + [{"role": "user", "parts": [{"text": user_input}]}]
        return call_gemini_api(formatted_contents, system_prompt)
    except Exception as e:
        return f"Lỗi: {str(e)}"

def get_roadmap_advice(user_input, job_detail_json, roadmap_steps_json, resources_json, user_data_json):
    """Tư vấn lộ trình chuyên sâu"""
    try:
        job = json.loads(job_detail_json)
        steps = json.loads(roadmap_steps_json)
        resources = json.loads(resources_json)
        user_data = json.loads(user_data_json) if user_data_json else {}

        user_name = user_data.get("profile", {}).get("Ten", "bạn")
        job_name = job.get("TenCongViec", "nghề nghiệp này")
        job_desc = job.get("MoTa", "")

        system_instruction = (
            f"Bạn là chuyên gia 'Cố vấn Lộ trình AI' cho nghề {job_name}. "
            f"DỮ LIỆU NGÀNH: {job_desc}. "
            f"LỘ TRÌNH CHI TIẾT: {json.dumps(steps, ensure_ascii=False)}. "
            f"TÀI NGUYÊN: {json.dumps(resources, ensure_ascii=False)}. "
            "\nNHIỆM VỤ: Tư vấn lộ trình học tập và đưa ra 3 câu hỏi gợi ý trong ngoặc vuông [...]."
        )

        query = user_input if user_input.strip() else f"Chào tôi và giới thiệu tổng quan về nghề {job_name}."
        contents = [{"role": "user", "parts": [{"text": f"Người dùng {user_name} hỏi: {query}"}]}]
        return call_gemini_api(contents, system_instruction)
    except Exception as e:
        return f"Lỗi AI Roadmap: {str(e)}"

def get_trend_prediction(trend_data_json, news_data_json):
    """Phân tích xu hướng nghề nghiệp"""
    try:
        system_instruction = (
            "Bạn là chuyên gia phân tích thị trường lao động. Hãy dựa trên dữ liệu xu hướng "
            "và tin tức được cung cấp để đưa ra dự báo về các ngành nghề triển vọng nhất hiện nay. "
            "Dùng Markdown để trình bày đẹp mắt."
        )

        user_prompt = (
            f"DỮ LIỆU XU HƯỚNG: {trend_data_json}\n\n"
            f"TIN TỨC THỊ TRƯỜNG: {news_data_json}"
        )

        contents = [{"role": "user", "parts": [{"text": user_prompt}]}]
        return call_gemini_api(contents, system_instruction)
    except Exception as e:
        return f"Lỗi AI Trend: {str(e)}"
