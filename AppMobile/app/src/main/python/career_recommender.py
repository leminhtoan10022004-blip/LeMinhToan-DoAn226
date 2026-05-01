import json

def recommend_jobs(user_scales_json, erd_data_json):
    """
    user_scales_json: JSON string chứa danh sách mã thang đo người dùng đạt được.
    erd_data_json: Nội dung của file ERD.json được truyền từ Java.
    """
    try:
        # 1. Load dữ liệu từ string được truyền vào
        data = json.loads(erd_data_json)
        user_scales = json.loads(user_scales_json)
        
        job_scores = {} # {MaCongViec: TongDiem}

        # Lấy bảng ánh xạ CongViec_ThangDo và dữ liệu CongViec
        mapping = data.get("CongViec_ThangDo", {})
        jobs = data.get("CongViec", {})

        # 2. Tính điểm cho từng công việc dựa trên thang đo của người dùng
        for i, scale_id in enumerate(user_scales):
            # Điểm ưu tiên: người dùng đạt thang đo này ở vị trí càng cao thì trọng số càng lớn
            user_priority_weight = 1.0 - (i * 0.1) 
            if user_priority_weight < 0.2: user_priority_weight = 0.2

            for m_id, m_data in mapping.items():
                if m_data["MaThangDo"] == scale_id:
                    job_id = m_data["MaCongViec"]
                    # Trọng số của thang đo đối với công việc này trong ERD
                    db_weight = m_data.get("TrongSo", 1.0)
                    
                    # Điểm tích lũy = Trọng số DB * Trọng số ưu tiên người dùng
                    match_score = db_weight * user_priority_weight
                    
                    job_scores[job_id] = job_scores.get(job_id, 0) + match_score

        # 3. Sắp xếp công việc theo điểm số giảm dần
        sorted_jobs = sorted(job_scores.items(), key=lambda x: x[1], reverse=True)

        # 4. Trả về kết quả (Top 5)
        results = []
        for job_id, score in sorted_jobs[:5]:
            job_info = jobs.get(job_id, {})
            results.append({
                "MaCongViec": job_id,
                "TenCongViec": job_info.get("TenCongViec"),
                "Score": round(score, 3),
                "MaNganh": job_info.get("MaNganh")
            })

        return json.dumps(results, ensure_ascii=False)

    except Exception as e:
        return json.dumps({"error": str(e)})
