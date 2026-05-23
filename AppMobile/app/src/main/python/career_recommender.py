import json

def recommend_jobs(user_scales_json, erd_data_json):

    try:
        data = json.loads(erd_data_json)
        user_scales = json.loads(user_scales_json)
        
        job_scores = {}

        mapping = data.get("CongViec_ThangDo", {})
        jobs = data.get("CongViec", {})

        for i, scale_id in enumerate(user_scales):
            user_priority_weight = 1.0 - (i * 0.1) 
            if user_priority_weight < 0.2: user_priority_weight = 0.2

            for m_id, m_data in mapping.items():
                if m_data["MaThangDo"] == scale_id:
                    job_id = m_data["MaCongViec"]
                    db_weight = m_data.get("TrongSo", 1.0)

                    match_score = db_weight * user_priority_weight
                    
                    job_scores[job_id] = job_scores.get(job_id, 0) + match_score

        sorted_jobs = sorted(job_scores.items(), key=lambda x: x[1], reverse=True)

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
