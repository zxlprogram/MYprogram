// 學號:11327227, 11327261 姓名:謝博丞, 周孝倫

#include <iomanip>
#include <iostream>
#include <fstream>
#include <sstream>
#include <algorithm>
#include <limits>
#include <vector>
#include <string>
using namespace std;

// 先把畢業生資訊 11個欄位 + 編號存在這
struct Record {
    int serial;
    string schoolCode;
    string schoolName;
    string deptCode;
    string deptName;
    string dayNight;
    string level;
    string students;
    string teachers;
    string lastYear;
    string city;
    string systemType;
};

// 超過千位數的數字轉換
int toIntStrict(const string& s) {
    // 如果字串是空的，回傳錯誤
    if (s.empty()) return -1;

    int n = 0;              // 最後轉換出的整數
    bool hasDigit = false;  // 確保字串裡至少有一個數字

    for (char c : s) {
        // 忽略逗號或雙引號
        if (c == ',' || c == '"') continue;
        if (!isdigit((unsigned char)c)) return -1;
        hasDigit = true;

        // 把字元數字轉換為整數
        n = n * 10 + (c - '0');
    }

    if (!hasDigit) return -1;
    return n;
}

bool readRecords(const string& filename, vector<Record>& out) {
    ifstream fin(filename);
    if (!fin) return false;

    out.clear();
    string line;

    // 跳過前三行
    getline(fin, line);
    getline(fin, line);
    getline(fin, line);

    int serial = 0;

    while (getline(fin, line)) {
        if (line.empty()) continue;

        stringstream ss(line);
        Record r;
        r.serial = ++serial;

        getline(ss, r.schoolCode, '\t');
        getline(ss, r.schoolName, '\t');
        getline(ss, r.deptCode, '\t');
        getline(ss, r.deptName, '\t');
        getline(ss, r.dayNight, '\t');
        getline(ss, r.level, '\t');
        getline(ss, r.students, '\t');
        getline(ss, r.teachers, '\t');
        getline(ss, r.lastYear, '\t');
        getline(ss, r.city, '\t');
        getline(ss, r.systemType, '\t');

        out.push_back(r);
    }
    return true;
}

class TwoThreeTree {
private:
    // 一個 key 可能對應到多筆資料(同樣學生數)，所以 serials 用 vector 存所有編號
    struct Entry {
        int key;
        vector<int> serials;
    };

    // 2-3 樹節點：
    // - entries 最多放 2 個鍵值(2-node 或 3-node)
    // - child 依照鍵值切分子樹，葉節點沒有 child
    struct Node {
        vector<Entry> entries;
        vector<Node*> child;
        bool leaf;
    };

    Node* root;

    // 後序刪除整棵樹，避免記憶體洩漏
    void clear(Node* n) {
        if (!n) return;
        for (Node* c : n->child) clear(c);
        delete n;
    }

    // 找到 key 應該插入(或比較)的位置：第一個 >= key 的索引
    int findPos(Node* n, int key) {
        int i = 0;
        while (i < (int)n->entries.size() && n->entries[i].key < key) i++;
        return i;
    }

    // 假設 split 函數會處理節點的分裂邏輯
    bool split(Node* n, int pos, Entry& promoted, Node*& newRight) {
        Node* right = new Node; // 分裂後的右邊節點
        promoted = n->entries[1]; // 取得中間鍵

        // 如果是葉節點，分裂後的右邊節點只會有一個元素
        if (n->child.empty()) {
            right->entries.push_back(n->entries[2]);
            n->entries.resize(1); // 留下最小鍵
        } else {
            // 如果是內部節點，分裂後的右邊節點有多個鍵和子樹
            right->entries.push_back(n->entries[2]);
            right->child.push_back(n->child[2]);
            right->child.push_back(n->child[3]);
            n->entries.resize(1);  // 留下最小鍵
            n->child.resize(2);    // 留下兩個子樹
        }

        newRight = right;
        return true;
    }

    // 遞迴插入：先走到葉節點插入，再檢查是否需要分裂
    // 回傳 true 代表「此節點分裂了」，並透過 promoted/newRight 把分裂結果往上回報
    bool insertInternal(Node* n, int key, int serial, Entry& promoted, Node*& newRight) {
        int pos = findPos(n, key);

        // 若 key 已存在(同樣學生數)，只要加 serial 進去就好，不需要新增鍵也不會分裂
        if (pos < (int)n->entries.size() && n->entries[pos].key == key) {
            n->entries[pos].serials.push_back(serial);
            return false;
        }

        // 走到葉節點：直接插入新鍵，不用遞迴(BASE CASE)，然後檢查是否需要分裂
        if (n->child.empty()) {
            Entry e;
            e.key = key;
            e.serials.push_back(serial);

            n->entries.insert(n->entries.begin() + pos, e);

            // 葉節點最多容納 2 個鍵，未超過就結束
            if ((int)n->entries.size() <= 2) return false;

            // 超過(變 3 個鍵)就分裂：
            return split(n, pos, promoted, newRight);
        }

        // 插入在內部節點：往子樹遞迴，就可以知道子節點是否分裂了
        Entry childPromoted;
        Node* childRight = nullptr;
        // 先往對應子樹插入，可能會得到子節點分裂資訊
        bool childSplit = insertInternal(n->child[pos], key, serial, childPromoted, childRight);

        if (!childSplit) return false;

        // 子節點分裂時，把升上來的鍵插入目前節點，並掛上新右子樹
        n->entries.insert(n->entries.begin() + pos, childPromoted);
        n->child.insert(n->child.begin() + pos + 1, childRight);

        // 目前節點若仍 <= 2 鍵，不需再分裂
        if ((int)n->entries.size() <= 2) return false;

        // 子節點分裂後，把中間鍵往上丟給父節點，結果父節點自己也爆掉了，就要繼續分裂：
        return split(n, pos, promoted, newRight);
    }

    // 2-3 樹是平衡樹，任一路徑高度相同；取最左路徑即可
    int height(Node* n) const {
        if (!n) return 0;
        if (n->child.empty()) return 1;
        return 1 + height(n->child[0]);
    }

    // 計算整棵樹節點總數
    int countNodes(Node* n) const {
        if (!n) return 0;
        int total = 1;
        for (Node* c : n->child) total += countNodes(c);
        return total;
    }

public:
    TwoThreeTree() : root(nullptr) {}

    ~TwoThreeTree() {
        clear(root);
    }

    void insert(int key, int serial) {
        // 空樹時先建立根(葉節點)
        if (!root) {
            root = new Node;
            Entry e;
            e.key = key;
            e.serials.push_back(serial);
            root->entries.push_back(e);
            return;
        }

        Entry promoted;
        Node* newRight = nullptr;
        bool split = insertInternal(root, key, serial, promoted, newRight);

        if (split) {
            // 若根分裂，直接建立新的 root
            Node* newRoot = new Node;
            newRoot->entries.push_back(promoted);
            newRoot->child.push_back(root);
            newRoot->child.push_back(newRight);
            root = newRoot;
        }
    }

    int getHeight() const {
        return height(root);
    }

    int getNodeCount() const {
        return countNodes(root);
    }

    vector<int> getRootView() const {
        vector<int> out;
        if (!root) return out;

        // 只輸出根節點的所有 key 對應資料，serial 由小到大顯示
        for (const Entry& e : root->entries) {
            vector<int> s = e.serials;
            sort(s.begin(), s.end());
            for (int serial : s) {
                out.push_back(serial);
            }
        }
        return out;
    }
};

class AVLTree {
private:
    // AVL 節點：以科系名稱作為 key，同 key 的所有序號集中在 serials
    struct Node {
        string key;
        vector<int> serials;
        Node* left;
        Node* right;
        int height;

        Node(const string& k, int serial)
            : key(k), left(nullptr), right(nullptr), height(1) {
            serials.push_back(serial);
        }
    };

    // 根節點
    Node* root;

    // 取得節點高度(空節點高度為 0)
    int nodeHeight(Node* n) const {
        if (n) return n->height;
        else return 0;
    }

    // 重新計算節點高度
    void updateHeight(Node* n) {
        if (!n) return;
        n->height = 1 + max(nodeHeight(n->left), nodeHeight(n->right));
    }

    // 平衡因子 = 左子樹高 - 右子樹高
    int balanceFactor(Node* n) const {
        if (!n) return 0;
        return nodeHeight(n->left) - nodeHeight(n->right);
    }

    // 右旋：處理 LL 型失衡
    Node* rotateRight(Node* y) {
        Node* x = y->left;
        Node* t2 = x->right;

        x->right = y;
        y->left = t2;

        updateHeight(y);
        updateHeight(x);
        return x;
    }

    // 左旋：處理 RR 型失衡
    Node* rotateLeft(Node* x) {
        Node* y = x->right;
        Node* t2 = y->left;

        y->left = x;
        x->right = t2;

        updateHeight(x);
        updateHeight(y);
        return y;
    }

    // 遞迴插入並在回溯時維持 AVL 平衡
    Node* insertNode(Node* n, const string& key, int serial) {
        if (!n) return new Node(key, serial);

        // 依字串比較決定往左或往右
        if (key < n->key) {
            n->left = insertNode(n->left, key, serial);
        }
        else if (key > n->key) {
            n->right = insertNode(n->right, key, serial);
        }
        else {
            // 相同科系名稱的資料必須集中在同一個節點
            n->serials.push_back(serial);
            return n;
        }

        // 插入後更新高度並檢查是否失衡
        updateHeight(n);
        int bf = balanceFactor(n);

        // LL
        if (bf > 1 && key < n->left->key) {
            return rotateRight(n);
        }
        // RR
        if (bf < -1 && key > n->right->key) {
            return rotateLeft(n);
        }
        // LR
        if (bf > 1 && key > n->left->key) {
            n->left = rotateLeft(n->left);
            return rotateRight(n);
        }
        // RL
        if (bf < -1 && key < n->right->key) {
            n->right = rotateRight(n->right);
            return rotateLeft(n);
        }

        return n;
    }

    // 計算整棵 AVL 的節點總數
    int countNodes(Node* n) const {
        if (!n) return 0;
        return 1 + countNodes(n->left) + countNodes(n->right);
    }

    // 釋放整棵樹
    void clear(Node* n) {
        if (!n) return;
        clear(n->left);
        clear(n->right);
        delete n;
    }

public:
    AVLTree() : root(nullptr) {}

    ~AVLTree() {
        clear(root);
    }

    // 外部插入介面
    void insert(const string& key, int serial) {
        root = insertNode(root, key, serial);
    }

    int getHeight() const {
        return nodeHeight(root);
    }

    int getNodeCount() const {
        return countNodes(root);
    }

    // 只輸出根節點資料，序號需由小到大
    vector<int> getRootView() const {
        vector<int> out;
        if (!root) return out;

        vector<int> s = root->serials;
        sort(s.begin(), s.end());
        for (int serial : s) {
            out.push_back(serial);
        }
        return out;
    }
};

class RedBlackTree {
private:
    // 紅黑樹顏色(定義在「父 -> 子」這條連線上)
    enum Color { RED, BLACK };

    // 節點：用科系名稱當 key，重複 key 的序號集中在 serials
    // incomingColor 代表「父節點連到此節點」那條線的顏色
    // 根節點沒有父節點，慣例視為 BLACK
    struct Node {
        string key;
        vector<int> serials;
        Color incomingColor;
        Node* left;
        Node* right;
        Node* parent;

        Node(const string& k, int serial)
            : key(k), incomingColor(RED), left(nullptr), right(nullptr), parent(nullptr) {
            serials.push_back(serial);
        }
    };

    Node* root;

    // nullptr 視為黑色連線，方便修正流程統一處理
    Color getColor(Node* n) const {
        if (!n) return BLACK;
        return n->incomingColor;
    }

    // 左旋：把 x 的右子節點 y 拉上來，x 往左下掉
    void rotateLeft(Node* x) {
        Node* y = x->right;  // y 是 x 的右子（旋轉的主角）
        x->right = y->left;  // 把 y 的左子樹（T2）搬到 x 的右邊

        // 如果 T2 存在，要更新它的 parent（不然會斷掉）
        if (y->left) y->left->parent = x;

        // y 準備接手 x 的位置（往上升）
        y->parent = x->parent;

        if (!x->parent) {
            // 如果 x 是 root → y 變新的 root
            root = y;
        } else if (x == x->parent->left) {
            // 如果 x 是左子 → 更新父節點向左指 y
            x->parent->left = y;
        } else {
            // 如果 x 是右子 → 更新父節點向右指 y
            x->parent->right = y;
        }

        y->left = x; // 把 x 掛到 y 的左邊（核心步驟）
        x->parent = y; // 更新 x 的 parent
    }

    // 右旋：把 x 的左子節點 y 拉上來，x 往右下掉
    void rotateRight(Node* x) {
        Node* y = x->left;  // y 是 x 的左子（旋轉的主角）
        x->left = y->right; // 把 y 的右子樹（T2）搬到 x 的左邊

        // 如果 T2 存在，要更新它的 parent（不然會斷掉）
        if (y->right) y->right->parent = x;

        // y 準備接手 x 的位置（往上升）
        y->parent = x->parent;

        if (!x->parent) {
            // 如果 x 是 root → y 變新的 root
            root = y;
        } else if (x == x->parent->left) {
            // 如果 x 是左子 → 更新父節點向左指 y
            x->parent->left = y;
        } else {
            // 如果 x 是右子 → 更新父節點向右指 y
            x->parent->right = y;
        }

        y->right = x; // 把 x 掛到 y 的右邊（核心步驟）
        x->parent = y; // 更新 x 的 parent
    }

    // 插入後修正：處理紅紅衝突，維持紅黑樹性質
    // 判斷的是「父->子連線顏色」
    void fixInsert(Node* z) {
        while (z->parent && z->parent->incomingColor == RED) {
            Node* gp = z->parent->parent;
            if (z->parent == gp->left) {
                Node* uncle = gp->right;

                // 已經排好的，分裂完(改顏色)直接插入
                if (getColor(uncle) == RED) {
                    z->parent->incomingColor = BLACK;
                    uncle->incomingColor = BLACK;
                    gp->incomingColor = RED;
                    z = gp;
                }
                else { // LL or LR
                    if (z == z->parent->right) {
                        z = z->parent;
                        rotateLeft(z);
                    }
                    z->parent->incomingColor = BLACK;
                    gp->incomingColor = RED;
                    rotateRight(gp);
                }
            }
            else {
                Node* uncle = gp->left;

                // 已經排好的，分裂完(改顏色)直接插入
                if (getColor(uncle) == RED) {
                    z->parent->incomingColor = BLACK;
                    uncle->incomingColor = BLACK;
                    gp->incomingColor = RED;
                    z = gp;
                }
                else { // RR or RL
                    if (z == z->parent->left) {
                        z = z->parent;
                        rotateRight(z);
                    }
                    z->parent->incomingColor = BLACK;
                    gp->incomingColor = RED;
                    rotateLeft(gp);
                }
            }
        }

        // 根節點沒有父連線，固定視為 BLACK
        root->incomingColor = BLACK;
    }

    // 找 key 對應節點
    Node* findNode(const string& key) const {
        Node* cur = root;
        while (cur) {
            if (key < cur->key) cur = cur->left;
            else if (key > cur->key) cur = cur->right;
            else return cur;
        }
        return nullptr;
    }

    // 取得子樹最小節點(右子樹的最左節點)
    Node* treeMinimum(Node* n) const {
        while (n && n->left) n = n->left;
        return n;
    }

    // 用 v 取代 u 在樹中的位置
    void transplant(Node* u, Node* v) {
        if (!u->parent) {
            // 如果 u 是 root → v 變成新的 root
            root = v;
        } else if (u == u->parent->left) {
            // 如果 u 是左子 → 父節點改指向 v
            u->parent->left = v;
        } else {
            // 如果 u 是右子 → 父節點改指向 v
            u->parent->right = v;
        }

        // 更新 v 的 parent，不然樹會斷掉
        if (v) v->parent = u->parent;
    }

    // 刪除後fix：消除多出來的黑，維持黑高度一致
    void fixDelete(Node* x, Node* parent) {
        // 只要 x 不是 root，且還有「多一層黑」，就要繼續修
        while (x != root && getColor(x) == BLACK) {

            // ===== x 是左子 =====
            if (x == (parent ? parent->left : nullptr)) {

                Node* w = parent ? parent->right : nullptr;
                // w = x 的兄弟

                // Case 1：兄弟是紅，先把兄弟變黑、再檢查 Case 2/3
                if (getColor(w) == RED) {
                    w->incomingColor = BLACK;     // 兄弟變黑
                    parent->incomingColor = RED;  // 父變紅
                    rotateLeft(parent);           // 左旋
                    w = parent->right;            // 更新兄弟
                }

                // Case 2：兄弟黑 + 兩個子都是黑
                if (getColor(w ? w->left : nullptr) == BLACK &&
                    getColor(w ? w->right : nullptr) == BLACK) {
                    if (w) w->incomingColor = RED;  // 兄弟變紅（補黑）
                    x = parent;                    // 往上檢查
                    parent = x ? x->parent : nullptr;
                }

                // Case 3/4：兄弟黑 + 至少一個紅（先處理紅色在內側，轉成靠外側）
                else {
                    // case 3：紅色在內側，轉成靠外側
                    if (getColor(w ? w->right : nullptr) == BLACK) {
                        if (w && w->left) w->left->incomingColor = BLACK; // 內側子變黑

                        if (w) {
                            w->incomingColor = RED; // 兄弟變紅
                            rotateRight(w);         // 右旋 → 轉成 Case 4
                        }

                        // 更新兄弟（旋轉後變了）
                        w = parent ? parent->right : nullptr;
                    }

                    // case 4：兄弟黑 + 右子紅 → 左旋父節點
                    // 兄弟的顏色設為父節點的顏色
                    if (w) w->incomingColor = parent ? parent->incomingColor : BLACK;
                    // 父節點變黑
                    if (parent) parent->incomingColor = BLACK;
                    // 兄弟的右子變黑（不論原本是紅是黑都變黑）
                    if (w && w->right) w->right->incomingColor = BLACK;

                    // 旋轉解決問題，變一紅接兩黑
                    if (parent) rotateLeft(parent);

                    x = root;      // 結束 while
                    parent = nullptr;
                }
            }

            // ===== x 是右子（完全鏡像）=====
            else {
                Node* w = parent ? parent->left : nullptr;
                // Case 1
                if (getColor(w) == RED) {
                    w->incomingColor = BLACK;
                    parent->incomingColor = RED;
                    rotateRight(parent);
                    w = parent->left;
                }
                // Case 2
                if (getColor(w ? w->left : nullptr) == BLACK &&
                    getColor(w ? w->right : nullptr) == BLACK) {
                    if (w) w->incomingColor = RED;
                    x = parent;
                    parent = x ? x->parent : nullptr;
                }
                else {
                    // Case 3
                    if (getColor(w ? w->left : nullptr) == BLACK) {
                        if (w && w->right) w->right->incomingColor = BLACK;
                        if (w) {
                            w->incomingColor = RED;
                            rotateLeft(w);
                        }
                        w = parent ? parent->left : nullptr;
                    }
                    // Case 4
                    if (w) w->incomingColor = parent ? parent->incomingColor : BLACK;
                    if (parent) parent->incomingColor = BLACK;
                    if (w && w->left) w->left->incomingColor = BLACK;
                    if (parent) rotateRight(parent);
                    x = root;
                    parent = nullptr;
                }
            }
        }

        // root 沒有父連線，固定視為 BLACK
        if (x) x->incomingColor = BLACK;
    }

    int height(Node* n) const {
        if (!n) return 0;
        int lh = height(n->left);
        int rh = height(n->right);
        return 1 + max(lh, rh);
    }

    int countNodes(Node* n) const {
        if (!n) return 0;
        return 1 + countNodes(n->left) + countNodes(n->right);
    }

    void clear(Node* n) {
        if (!n) return;
        clear(n->left);
        clear(n->right);
        delete n;
    }

public:
    RedBlackTree() : root(nullptr) {}

    ~RedBlackTree() {
        clear(root);
    }

    void insert(const string& key, int serial) {
        // 空樹時先建立根節點
        if (!root) {
            root = new Node(key, serial);
            root->incomingColor = BLACK;
            return;
        }

        Node* cur = root;
        Node* parent = nullptr;

        // 找到插入位置
        while (cur) {
            parent = cur;
            if (key < cur->key) cur = cur->left;
            else if (key > cur->key) cur = cur->right;
            else {
                // 相同 key 直接放在同一個節點
                cur->serials.push_back(serial);
                return;
            }
        }

        // 插入新節點，預設為紅色
        Node* z = new Node(key, serial);
        z->parent = parent;
        if (key < parent->key) parent->left = z;
        else parent->right = z;

        // 插入後修正：消除紅紅衝突，維持紅黑樹性質
        fixInsert(z);
    }

    // 刪除指定 key 的節點；成功回傳 true
    bool erase(const string& key) {
        Node* z = findNode(key);  // 找要刪的節點
        if (!z) return false;

        Node* y = z;  // y = 真正被刪掉的節點（可能會換）
        Color yOriginalColor = y->incomingColor; // 記錄原本顏色

        Node* x = nullptr;       // x = 補上來的位置（可能變 double black）
        Node* xParent = nullptr; // x 的 parent（x 可能是 null）

        // ===== Case 1：沒有左子 =====
        if (!z->left) {
            x = z->right;
            xParent = z->parent;
            transplant(z, z->right);  // 右子補上
        }

        // ===== Case 2：沒有右子 =====
        else if (!z->right) {
            x = z->left;
            xParent = z->parent;
            transplant(z, z->left);   // 左子補上
        }

        // ===== Case 3：有兩個子 =====
        else {
            y = treeMinimum(z->right);  // 找中序後繼
            yOriginalColor = y->incomingColor;

            x = y->right;  // y 會被搬走，所以這裡會空出來

            if (y->parent == z) {
                // y 就是 z 的右子
                xParent = y;
                if (x) x->parent = y;
            }
            else {
                xParent = y->parent;

                transplant(y, y->right);  // 把 y 拔掉
                y->right = z->right;      // 接上 z 的右子
                y->right->parent = y;
            }

            transplant(z, y);  // y 取代 z
            y->left = z->left; // 接上左子
            y->left->parent = y;

            y->incomingColor = z->incomingColor; // 顏色繼承
        }

        delete z;  // 真正刪除節點

        // 如果刪掉的是黑色 → 需要修復
        if (yOriginalColor == BLACK) {
            fixDelete(x, xParent);
        }

        return true;
    }

    int getHeight() const {
        return height(root);
    }

    int getNodeCount() const {
        return countNodes(root);
    }

    vector<int> getRootView() const {
        vector<int> out;
        if (!root) return out;

        vector<int> s = root->serials;
        sort(s.begin(), s.end());
        for (int serial : s) {
            out.push_back(serial);
        }
        return out;
    }
};

void menu() {
    cout << "\n* Data Structures and Algorithms *\n";
    cout << "****** Balanced Search Tree ******\n";
    cout << "* 0. QUIT                        *\n";
    cout << "* 1. Build 23 tree               *\n";
    cout << "* 2. Build AVL tree              *\n";
    cout << "* 3. Build Red-Black tree        *\n";
    cout << "* 4. Delete from Red-Black tree  *\n";
    cout << "**********************************\n";
}

bool inputFileNumber(vector<Record>& recs) {
    while (true) {
        cout << "\nInput a file number ([0] Quit): ";
        string num;
        cin >> num;
        if (num == "0") return false;
        string fileName = "input" + num + ".txt";
        if (!readRecords(fileName, recs)) {
            cout << "\n### "+ fileName + " does not exist! ###\n";
            continue;
        }
        return true;
    }
}

void showRootRecords(const vector<int>& serials, const vector<Record>& recs) {
    for (int i = 0; i < (int)serials.size(); ++i) {
        int serial = serials[i];
        if (serial <= 0 || serial > (int)recs.size()) continue;

        const Record& r = recs[serial - 1];
        cout << (i + 1) << ": [" << r.serial << "] "
             << r.schoolName << ", "
             << r.deptName << ", "
             << r.dayNight << ", "
             << r.level << ", "
             << r.students << ", "
             << r.lastYear << "\n";
    }
    cout << "\n";
}

int main() {
    vector<Record> recs;
    TwoThreeTree t23;
    AVLTree avl;
    RedBlackTree rbt;
    bool avlBuilt = false;
    bool rbtBuilt = false;

    while (true) {
        menu();
        cout << "Input a choice(0, 1, 2, 3, 4): ";
        int choice;
        if (!(cin >> choice) || choice == 0) break;

        if (choice == 1) {
            recs.clear();
            // 先清除舊的 2-3 樹、AVL 樹和紅黑樹
            t23 = TwoThreeTree();
            avl = AVLTree();
            rbt = RedBlackTree();
            avlBuilt = false;
            rbtBuilt = false;

            if(!inputFileNumber(recs)) continue;

            for (const Record& r : recs) {
                int stu = toIntStrict(r.students);
                t23.insert(stu, r.serial);
            }

            cout << "Tree height = " << t23.getHeight() << "\n";
            cout << "Number of nodes = " << t23.getNodeCount() << "\n";

            vector<int> rootSerials = t23.getRootView();
            showRootRecords(rootSerials, recs);
        }
        else if (choice == 2) {
            if (recs.empty()) {
                cout << "### Choose 1 first. ###\n";
                continue;
            }

            if (!avlBuilt) {
                // 依序號由小到大逐筆插入，key 為科系名稱
                for (const Record& r : recs) avl.insert(r.deptName, r.serial);
                avlBuilt = true;
            } else {
                cout << "### AVL tree has been built. ###\n";
            }

            cout << "Tree height = " << avl.getHeight() << "\n";
            cout << "Number of nodes = " << avl.getNodeCount() << "\n";

            vector<int> rootSerials = avl.getRootView();
            showRootRecords(rootSerials, recs);
        }
        else if (choice == 3) {
            if (recs.empty()) {
                cout << "### Choose 1 first. ###\n";
                continue;
            }

            if (!rbtBuilt) {
                // 依序號由小到大逐筆插入，key 為科系名稱
                for (const Record& r : recs) rbt.insert(r.deptName, r.serial);
                rbtBuilt = true;
            } else {
                cout << "### Red-Black tree has been built. ###\n";
            }

            cout << "Tree height = " << rbt.getHeight() << "\n";
            cout << "Number of nodes = " << rbt.getNodeCount() << "\n";

            vector<int> rootSerials = rbt.getRootView();
            showRootRecords(rootSerials, recs);
        }
        else if (choice == 4) {
            if (recs.empty()) {
                cout << "### Choose 1 first. ###\n";
                continue;
            }

            if (!rbtBuilt) {
                for (const Record& r : recs) rbt.insert(r.deptName, r.serial);
                rbtBuilt = true;
            }

            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cout << "Input a dept name to delete ([0] Quit): ";
            string dept;
            getline(cin, dept);

            if (dept == "0") continue;

            if (!rbt.erase(dept)) {
                cout << "### Dept not found. ###\n";
            }

            cout << "Tree height = " << rbt.getHeight() << "\n";
            cout << "Number of nodes = " << rbt.getNodeCount() << "\n";

            vector<int> rootSerials = rbt.getRootView();
            showRootRecords(rootSerials, recs);
        }
        else {
            cout << "\nCommand does not exist!\n";
        }
    }
    return 0;
}
