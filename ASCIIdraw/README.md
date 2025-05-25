---
title: "\U0001F3A8 ASCII 圖畫產生器（ASCIIdraw）"

---

# 🎨 ASCII 圖畫產生器（ASCIIdraw）

## 🧐 這是什麼？

這是一個使用**符號字元生成 ASCII 藝術圖**的小工具。
使用者只需要把圖片拖進去、輸入圖片解析度，就可以自動產出 `.txt` 的 ASCII 藝術圖！

* 數字越大，輸出圖片越清楚，但生成速度會變慢。
* **不建議輸入超過 100**，除非你電腦夠強，因為檔案會變很大。

---

## 🧠 能做什麼？

沒幹啥，純粹娛樂。
適合：

* 打發時間
* 做點好看的終端機藝術圖
* 拿來做專案彩蛋

---

## 🛠️ 安裝與執行方式

### ✅ 系統需求

* **Java 執行環境 JDK 23.0.1** 或以上版本
  檢查方式（Windows）：

  ```sh
  win + R → 輸入 `cmd`
  → 在命令提示字元中輸入：
  java -version
  ```

如果顯示版本號，表示你已安裝。
若沒有，請參考下方步驟。

---

### 🔧 Java 安裝方法（Windows）

1. 前往 Oracle JDK 下載頁面：
   👉 [https://www.oracle.com/java/technologies/javase/jdk23-archive-downloads.html](https://www.oracle.com/java/technologies/javase/jdk23-archive-downloads.html)
2. 下載並安裝 JDK（建議用 JDK 23.0.1 或以上版本）
3. 設定環境變數（重要）：

   * 打開 `sysdm.cpl` → 「進階」→「環境變數」
   * 新增系統變數：

     * **變數名稱：** `JAVA_HOME`
     * **變數值：** JDK 安裝路徑
   * 編輯 `Path` → 新增 `%JAVA_HOME%\bin`
   * 確定後重啟命令提示字元再試一次 `java -version`

---

### 🚀 執行方法

1. 前往 GitHub 頁面：
   👉 `https://github.com/zxlprogram/MYprogram`
2. 找到並下載 `ASCIIdraw.jar`
3. 打開命令提示字元，導向 jar 檔所在資料夾：

   ```sh
   cd C:\你下載的位置
   ```
4. 執行程式：

   ```sh
   java -jar ASCIIdraw.jar
   ```

---

## 📸 支援的圖片格式

目前支援以下格式（基於 Java ImageIO）：

* `.jpeg`, `.jpg`
* `.png`
* `.bmp`
* `.wbmp`
* `.gif`（僅讀取第一幀，非動畫）

---

## 🧑‍💻 使用方式

1. 執行程式後，會跳出一個視窗
2. 將你想轉換的圖片拖進去
3. 輸入一個大於 1 的整數（推薦值為 50\~80）
4. 程式會生成一個 `.txt` 檔案（ASCII 圖）

如果 `.txt` 看起來是亂碼，請**縮小字體觀看**或使用支援等寬字型的編輯器（如 Notepad++）。

---

