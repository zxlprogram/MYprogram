// 學號:11327227, 11327261 姓名:謝博丞, 周孝倫

#include <iomanip>
#include <iostream>
#include <fstream>
#include <sstream>
#include <algorithm>
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

// 轉換成只包含編號和畢業生數的 Item 結構
struct Item {
    int serial;
    int grads;
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

// Record -> Item
vector<Item> buildItems(const vector<Record>& recs) {
    vector<Item> items;
    items.reserve(recs.size());

    for (auto& r : recs) {
        int g = toIntStrict(r.lastYear);
        items.push_back(Item{ r.serial, g });
    }
    return items;
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
        return n ? n->height : 0;
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

void show(const string& s, const Item& it) {
    cout << s << "[" << it.serial << "] " << it.grads << "\n";
}

void menu() {
    cout << "\n* Data Structures and Algorithms *\n";
    cout << "****** Balanced Search Tree ******\n";
    cout << "* 0. QUIT                        *\n";
    cout << "* 1. Build 23 tree               *\n";
    cout << "* 2. Build AVL tree              *\n";
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
    bool avlBuilt = false;

    while (true) {
        menu();
        cout << "Input a choice(0, 1, 2): ";
        int choice;
        if (!(cin >> choice) || choice == 0) break;

        if (choice == 1) {
            recs.clear();
            // 先清除舊的 2-3 樹和 AVL 樹
            t23 = TwoThreeTree();
            avl = AVLTree();
            avlBuilt = false;

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
        else {
            cout << "\nCommand does not exist!\n";
        }
    }
    return 0;
}
