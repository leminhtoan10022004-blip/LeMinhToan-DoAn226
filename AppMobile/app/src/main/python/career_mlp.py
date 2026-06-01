import pandas as pd
import numpy as np
from sklearn.neural_network import MLPClassifier
from sklearn.preprocessing import LabelEncoder
from os.path import dirname, join


_mlp_model = None
_le_mbti = None
_le_holland = None
_le_disc = None

def init_model():
    global _mlp_model, _le_mbti, _le_holland, _le_disc
    if _mlp_model is not None:
        return True
        
    try:
        csv_path = join(dirname(__file__), "data_train.csv")
        df = pd.read_csv(csv_path)

        _le_mbti = LabelEncoder()
        df['MBTI_enc'] = _le_mbti.fit_transform(df['MBTI_code'])

        _le_holland = LabelEncoder()
        df['Holland_enc'] = _le_holland.fit_transform(df['Holland_code'])

        _le_disc = LabelEncoder()
        df['DISC_enc'] = _le_disc.fit_transform(df['DISC_code'])

        feature_cols = ['MBTI_enc', 'Holland_enc', 'Big5_O', 'Big5_C', 'Big5_E', 'Big5_A', 'Big5_N', 'DISC_enc',
                        'Toan', 'Ly', 'Hoa', 'Sinh', 'Van', 'Anh', 'Tin', 'Dia', 'Su']

        X = df[feature_cols].values
        y = df['nghe_nghiep'].values

        # Khởi tạo và huấn luyện mô hình MLP
        _mlp_model = MLPClassifier(hidden_layer_sizes=(128, 64), max_iter=1000, random_state=42)
        _mlp_model.fit(X, y)
        return True
    except Exception as e:
        print(f"Lỗi khởi tạo MLP: {e}")
        return False

def predict_career_top_5(mbti_code, holland_code, big5_o, big5_c, big5_e, big5_a, big5_n, disc_code,
                        toan, ly, hoa, sinh, van, anh, tin, dia, su):
    if not init_model():
        return [{"error": "Lỗi: Không thể khởi tạo mô hình AI."}]

    try:
        # Encode các giá trị phân loại
        try: mbti_val = _le_mbti.transform([mbti_code])[0]
        except: mbti_val = 0

        try: holland_val = _le_holland.transform([holland_code])[0]
        except: holland_val = 0

        try: disc_val = _le_disc.transform([disc_code])[0]
        except: disc_val = 0

        user_input = np.array([[mbti_val, holland_val, big5_o, big5_c, big5_e, big5_a, big5_n, disc_val,
                               toan, ly, hoa, sinh, van, anh, tin, dia, su]])

        # Dự đoán xác suất cho tất cả các lớp
        probabilities = _mlp_model.predict_proba(user_input)[0]
        classes = _mlp_model.classes_

        # Lấy top 5 kết quả có xác suất cao nhất
        top_indices = np.argsort(probabilities)[::-1][:5]
        
        results = []
        for i in top_indices:
            results.append({
                "career": str(classes[i]),
                "probability": float(probabilities[i] * 100)
            })
            
        return results

    except Exception as e:
        return [{"error": str(e)}]

def predict_career(mbti_code, holland_code, big5_o, big5_c, big5_e, big5_a, big5_n, disc_code,
                   toan, ly, hoa, sinh, van, anh, tin, dia, su):
    # Giữ lại hàm cũ để không làm gãy code cũ, nhưng gọi hàm mới lấy top 1
    res = predict_career_top_5(mbti_code, holland_code, big5_o, big5_c, big5_e, big5_a, big5_n, disc_code,
                              toan, ly, hoa, sinh, van, anh, tin, dia, su)
    if "career" in res[0]:
        return res[0]["career"]
    return "Lỗi"
