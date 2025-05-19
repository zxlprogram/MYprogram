import pandas as pd
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt

# 讀取 CSV 檔案
df = pd.read_csv("C:\\Users\\user\\desktop\\school.csv", encoding="big5")

# 定義輸入與目標
X = df[["x", "y", "z"]]
y = df["n"]

# 切分訓練與測試集（80% 訓練，20% 測試）
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# 建立與訓練模型
model = LinearRegression()
model.fit(X_train, y_train)

# 預測測試集
y_pred = model.predict(X_test)

# 顯示模型參數
print("m：", model.coef_)
print("intercept：", model.intercept_)
coef = model.coef_
intercept = model.intercept_
print(f"linearfunc：n = {coef[0]:.2f}x + {coef[1]:.2f}y + {coef[2]:.2f}z + {intercept:.2f}")

# 繪圖：實際 vs 預測
plt.scatter(y_test, y_pred)
plt.xlabel("real pr")
plt.ylabel("AI pr")
plt.title("lineareg(real:AI)")
plt.grid(True)
plt.show()
