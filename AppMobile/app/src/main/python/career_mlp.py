import pandas as pd
import numpy as np
from sklearn.neural_network import MLPClassifier
from sklearn.preprocessing import LabelEncoder
from os.path import dirname, join

# --- KHỞI TẠO VÀ HUẤN LUYỆN (Chỉ chạy 1 lần khi load module) ---
print("Đang khởi tạo mô hình AI...")
csv_path = join(dirname(__file__), "data_train.csv")
df = pd.read_csv(csv_path)

# Tiền xử lý dữ liệu
le_mbti = LabelEncoder()
df['MBTI_enc'] = le_mbti.fit_transform(df['MBTI_code'])

le_holland = LabelEncoder()
df['Holland_enc'] = le_holland.fit_transform(df['Holland_code'])

le_disc = LabelEncoder()
df['DISC_enc'] = le_disc.fit_transform(df['DISC_code'])

feature_cols = ['MBTI_enc', 'Holland_enc', 'Big5_O', 'Big5_C', 'Big5_E', 'Big5_A', 'Big5_N', 'DISC_enc',
                'Toan', 'Ly', 'Hoa', 'Sinh', 'Van', 'Anh', 'Tin', 'Dia', 'Su']

X = df[feature_cols].values
y = df['nghe_nghiep'].values

# Huấn luyện mô hình (Chỉ huấn luyện 1 lần duy nhất)
mlp = MLPClassifier(hidden_layer_sizes=(128, 64), max_iter=1500, random_state=42)
mlp.fit(X, y)
print("Mô hình đã sẵn sàng!")

def predict_career(mbti_code, holland_code, big5_o, big5_c, big5_e, big5_a, big5_n, disc_code,
                   toan, ly, hoa, sinh, van, anh, tin, dia, su):
    try:
        # Sử dụng các LabelEncoder và mô hình mlp đã được huấn luyện sẵn ở trên
        try:
            mbti_val = le_mbti.transform([mbti_code])[0]
        except: mbti_val = 0

        try:
            holland_val = le_holland.transform([holland_code])[0]
        except: holland_val = 0

        try:
            disc_val = le_disc.transform([disc_code])[0]
        except: disc_val = 0

        user_input = np.array([[mbti_val, holland_val, big5_o, big5_c, big5_e, big5_a, big5_n, disc_val,
                               toan, ly, hoa, sinh, van, anh, tin, dia, su]])

        # Dự đoán (Lúc này cực nhanh vì không phải huấn luyện lại)
        prediction = mlp.predict(user_input)
        return str(prediction[0])

    except Exception as e:
        return "Lỗi xử lý mô hình: " + str(e)
