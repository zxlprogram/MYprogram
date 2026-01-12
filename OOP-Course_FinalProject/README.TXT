專案名稱
Moodle伺服器架構設計與實現
專案內容
本專案設計並實現類似 Moodle 的伺服器架構。該系統被劃分為多個層級，以提高可擴展性、安全性和程式碼多用性。

整體而言，我們使用has-a組合的方式，將Moodle類別賦予密碼、作業提交系統與伺服器的功能。

我們使用了繼承的方式，讓題目能夠有多樣變化

下面將會展示專題的類別架構圖

組員: 
周孝倫 侯易辰 陳彥儒 黃昱翔

==============================

0. PPT: 專案報告說明與展示+口試+zip (每組一份) (20%)

各組員反思內容: (10%)

1. 本專案系統的架構設計有何好處? (10%)
可擴展性與可維護性
1.如果新的伺服器系統不需要密碼功能，可以直接重複使用根抽象結構。這減少了不必要的耦合。
2.系統允許輕鬆添加新的任務類型（例如，QATask），而無需修改現有核心邏輯。
3.使用了抽象、介面等概念，提升了系統的彈性

BaseUser保護機制
透過靜態Set類別管理ID，以及拋出例外的方式防止ID重複

基於權限的安全控制
Moodle 伺服器使用枚舉類型 PERMISSION 來驗證使用者請求。每個伺服器操作在執行前都會檢查權限，防止未經授權的存取。

穩定的密碼彈性
Password類別要求子類別必須覆寫hashCode與equals，以此維護密碼的可靠性，同時又能夠自訂Password資料內容的型別，維持高度的密碼彈性。

2. 本次練習是否有使用AI輔助? (YES/NO) +4
YES
   若有， AI 的回答如何改變我的理解？
最初，我透過直接從用戶類別呼叫伺服器方法來實現某些伺服器行為。人工智慧建議這些操作應該在伺服器類別中處理，使用者則作為客戶端發送請求。這明確了實際伺服器架構中職責的正確劃分。
人工智慧也建議將架構拆分為兩個獨立的抽象結構：一個包含密碼功能，另一個不包含。我沒有採用這種方法，因為我認為密碼處理並非所有系統的基本需求，而應該作為可選功能。

此外，人工智慧也幫助我識別出最初忽略的潛在空指標異常問題。

3.若有使用AI輔助，有下列兩項目以上者，依論述品質，最多可加2/5分(依照比例) (附上對話紀錄畫面):

  3.1 主動提出 2 個可能的 bug 原因
Bug 1：隨機 ID 產生問題我們曾嘗試在二級抽象結構中使用 Random 產生使用者 ID。儘管建構函式已正確初始化，但仍出現了 NullPointerException 異常。 AI 指出，由於所有物件共用同一個隨機產生器，因此應將其宣告為靜態的。此操作解決了該問題。

Bug 2：資料外洩風險AI 提醒我們，getter 方法不應傳回原始的可變資料結構，因為這可能允許客戶端篡改伺服器端資料。為了維護系統安全，應該傳回不可變的副本或防禦性副本。

  3.2 自己測試三種輸入並解釋行為
測試案例 1：重複 ID 檢測
MoodleUser stu=new MoodleUser(s.getBaseServer(),"student",123456,newHashSet<PERMISSION>(Arrays.asList(
	PERMISSION.READ_TASK
)));
MoodleUser prof=new MoodleUser(s.getBaseServer(),"professor",123456,newHashSet<PERMISSION>(Arrays.asList(
	PERMISSION.READ_TASK,
	PERMISSION.EDIT_TASK
)));

結果：Exception in thread "main" IdRepeatitiveException: the ID is repeatitive (ID:123456)

解釋：兩個用戶共享相同的 ID。 BaseUser 中的靜態集合偵測到了重複項，從而避免了系統不一致。
測試案例 2：權限檢查失敗
Task t = new TFTask(s, "question 1", "1+1=2", true);
MoodleUser stu=new MoodleUser(s.getBaseServer(),"student",12346,new HashSet<PERMISSION>(Arrays.asList(
	PERMISSION.READ_TASK
)));
MoodleUser prof=new MoodleUser(s.getBaseServer(),"professor",123456,new HashSet<PERMISSION>(Arrays.asList(
	PERMISSION.READ_TASK,
	PERMISSION.EDIT_TASK
))); 
//省略註冊登入過程
s.addTask(t,prof);
stu.doTask(t, true);
結果：[moodle] student/12346, you didn't sign in or have no permission
解釋：該學生缺少 SUBMIT_ANS 權限。伺服器在 submitAnswer() 期間驗證權限，並記錄權限違規。
測試案例 3：線上狀態驗證

場景：教授已從線上onlinePool中移除，並嘗試新增任務。

結果：
[moodle] name/123456, you didn't sign in or have no permission

說明：伺服器在處理請求前會檢查使用者是否存在於線上使用者池中。

  3.3 在 AI 回答後寫「採納或不採納 

  3.4 其他佐證主動式對話的證據畫面

(本作業所謂AI對話是指使用CHATGPT+老師的AI網站對話(系統會紀錄完整對話紀錄供驗證)，而非提供ChatGPT片斷的對話。)