// 學號:11327227, 11327261 姓名:謝博丞, 周孝倫

#include <algorithm>
#include <chrono>
#include <cstdio>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

// 緩衝區上限，固定為 300 筆
static const size_t kBufferSize = 300; 

// 以 1-byte 對齊，確保與二進位檔格式完全一致
#pragma pack(push, 1)
struct Record {
    char  putID[10]; // 發訊者學號（10 字元）
    char  getID[10]; // 收訊者學號（10 字元）
    float weight;    // 量化權重，介於 (0, 1]
};
#pragma pack(pop)

static const size_t kRecSize = sizeof(Record); // 每筆紀錄的 byte 大小

// 主索引條目：key（量化權重）+ 該 key 第一筆的紀錄筆數 offset
struct IndexEntry {
    float     key;    // 量化權重
    long long offset; // 單位：筆數（第幾筆，從 0 開始）
};

// ═════════════════════════════════════════════════════════
// B-Tree 實作（主索引用，存在記憶體中）
// 階數 t = 3（每個節點最多 2t-1 = 5 個 key，最少 t-1 = 2 個 key）
// ═════════════════════════════════════════════════════════

static const int BT_T = 3; // B-Tree 最小度數（minimum degree）

// B-Tree 節點
struct BTreeNode {
    vector<IndexEntry> keys;     // 節點內的主索引條目，依 key 由大到小排列
    vector<BTreeNode*> children; // 子節點指標（leaf 節點的 children 為空）
    bool isLeaf;                 // 是否為葉節點

    BTreeNode(bool leaf) : isLeaf(leaf) {}

    ~BTreeNode() {
        // 遞迴釋放所有子節點記憶體
        for (auto* c : children) delete c;
    }
};

// B-Tree 本體
class BTree {
public:
    BTree() : root_(new BTreeNode(true)) {}
    ~BTree() { delete root_; }

    // 插入一筆主索引條目（key 由大到小排列）
    void insert(const IndexEntry& entry) {
        BTreeNode* r = root_;

        // 若根節點已滿（2t-1 個 key），先分裂再插入
        if ((int)r->keys.size() == 2 * BT_T - 1) {
            // 建立新根，舊根變成新根的第一個子節點
            BTreeNode* newRoot = new BTreeNode(false);
            newRoot->children.push_back(r);
            splitChild(newRoot, 0); // 分裂新根的第 0 個子節點（即舊根）
            root_ = newRoot;
        }
        insertNonFull(root_, entry);
    }

    // 中序走訪（由大到小），將所有條目依序存入 out
    void inorder(vector<IndexEntry>& out) const {
        inorderNode(root_, out);
    }

private:
    BTreeNode* root_;

    // 分裂節點 parent->children[idx]（必須已滿，即有 2t-1 個 key）
    // 將中間 key 提升到 parent，左右各保留 t-1 個 key
    void splitChild(BTreeNode* parent, int idx) {
        BTreeNode* full  = parent->children[idx]; // 要被分裂的子節點
        BTreeNode* right = new BTreeNode(full->isLeaf); // 分裂出的右半邊

        // 中間 key 的索引（0-based），提升到 parent
        int mid = BT_T - 1;

        // 右節點取 full 的後半段 keys（mid+1 ~ 2t-2）
        right->keys.assign(full->keys.begin() + mid + 1, full->keys.end());
        // 若 full 不是葉節點，右節點也取對應的後半段子節點
        if (!full->isLeaf) {
            right->children.assign(full->children.begin() + mid + 1,
                                   full->children.end());
            full->children.resize(mid + 1); // full 只保留前半段子節點
        }
        IndexEntry midKey = full->keys[mid]; // 中間 key 要提升到 parent
        full->keys.resize(mid);              // full 只保留前半段 keys

        // 將中間 key 插入 parent 的適當位置（保持由大到小）
        parent->keys.insert(parent->keys.begin() + idx, midKey);
        // 將右節點插入 parent->children 的對應位置
        parent->children.insert(parent->children.begin() + idx + 1, right);
    }

    // 在保證節點未滿的前提下插入 entry（遞迴下降）
    void insertNonFull(BTreeNode* node, const IndexEntry& entry) {
        int i = (int)node->keys.size() - 1;

        if (node->isLeaf) {
            // 葉節點：找到插入位置（由大到小），向右移位後插入
            node->keys.push_back({}); // 先佔位
            while (i >= 0 && entry.key > node->keys[i].key) {
                node->keys[i + 1] = node->keys[i];
                --i;
            }
            node->keys[i + 1] = entry;
        } else {
            // 內部節點：找到要往下走的子節點（由大到小，找第一個 key < entry.key）
            while (i >= 0 && entry.key > node->keys[i].key) --i;
            int childIdx = i + 1; // 應走的子節點索引

            // 若該子節點已滿，先分裂
            if ((int)node->children[childIdx]->keys.size() == 2 * BT_T - 1) {
                splitChild(node, childIdx);
                // 分裂後中間 key 提升到 node->keys[childIdx]，重新判斷走哪邊
                if (entry.key < node->keys[childIdx].key)
                    ++childIdx;
            }
            insertNonFull(node->children[childIdx], entry);
        }
    }

    // 中序走訪輔助函式（由大到小）
    // B-Tree 由大到小的中序：先走最左子節點，再輸出 key[0]，再走 children[1]，
    // 輸出 key[1]，走 children[2]… 以此類推
    void inorderNode(const BTreeNode* node, vector<IndexEntry>& out) const {
        if (!node) return;
        int n = (int)node->keys.size();
        for (int i = 0; i < n; ++i) {
            // 先遞迴走第 i 個子樹（比 keys[i] 大的那側）
            if (!node->isLeaf) inorderNode(node->children[i], out);
            out.push_back(node->keys[i]);
        }
        // 走最右側子樹
        if (!node->isLeaf) inorderNode(node->children[n], out);
    }
};

// 檢查檔案是否存在
static bool fileExists(const string& path) {
    ifstream fin(path, ios::binary);
    return fin.good();
}

// 格式化浮點數輸出：去除結尾的零（如 1.00→1, 0.90→0.9）
static string formatWeight(float v) {
    ostringstream oss;
    oss << fixed << setprecision(2) << v;
    string s = oss.str();
    while (!s.empty() && s.back() == '0') s.pop_back();
    if (!s.empty() && s.back() == '.')  s.pop_back();
    return s;
}

// ═════════════════════════════════════════════════════════
// 任務一：外部合併排序（Two-Way External Merge Sort）
// 規則：依【量化權重】由大到小；相等時保持原始順序（stable sort）
// ═════════════════════════════════════════════════════════

// 第一階段：內部排序，分批讀取排序後寫成初始 run 檔
static vector<string> createInitialRuns(ifstream& fin) {
    vector<string> runPaths;
    // 預先分配固定大小 buffer，整個階段重複使用，避免反覆配置記憶體
    vector<Record> buf(kBufferSize);
    size_t runIdx = 0;

    while (true) {
        // 逐筆讀取，直到 buffer 滿或檔案結束
        size_t cnt = 0;
        while (cnt < kBufferSize &&
               fin.read(reinterpret_cast<char*>(&buf[cnt]), kRecSize))
            ++cnt;
        if (cnt == 0) break;

        // 穩定排序：weight 由大到小（相等維持原始讀入順序）
        stable_sort(buf.begin(), buf.begin() + cnt,
                    [](const Record& a, const Record& b) {
                        return a.weight > b.weight;
                    });

        // 寫出到暫存 run 檔（一次性寫入，減少系統呼叫次數）
        string path = "run_" + to_string(runIdx++) + ".bin";
        ofstream fout(path, ios::binary);
        fout.write(reinterpret_cast<const char*>(buf.data()),
                   static_cast<streamsize>(cnt * kRecSize));
        runPaths.push_back(path);
    }
    return runPaths; // 回傳 run 檔名列表
}

// 第二階段：兩兩合併（Two-Way Merge）
static void mergeTwoRuns(const string& leftPath,
                         const string& rightPath,
                         const string& outPath) {
    // 合併兩個已排序 run，輸出到 outPath
    ifstream finL(leftPath,  ios::binary);
    ifstream finR(rightPath, ios::binary);
    ofstream fout(outPath,   ios::binary);

    // 緩衝區三等分：左輸入 / 右輸入 / 輸出，各佔 kBufferSize/3 筆
    const size_t half   = kBufferSize / 3;        // 每個輸入緩衝區大小
    const size_t outCap = kBufferSize - half * 2; // 輸出緩衝區大小

    // 預先配置三塊緩衝區，合併過程中重複使用
    vector<Record> bufL(half), bufR(half), bufOut;
    bufOut.reserve(outCap);

    size_t cntL = 0, cntR = 0; // 各緩衝區有效筆數
    size_t idxL = 0, idxR = 0; // 各緩衝區目前讀取位置
    bool   eofL = false, eofR = false; // 各輸入檔是否已讀到結尾

    // 以下為合併過程中使用的輔助 lambda 函式：
    auto refillL = [&]() { // 填充左輸入緩衝區
        cntL = 0; idxL = 0;
        while (!eofL && cntL < half) {
            if (!finL.read(reinterpret_cast<char*>(&bufL[cntL]), kRecSize))
                { eofL = true; break; }
            ++cntL;
        }
    };

    auto refillR = [&]() { // 填充右輸入緩衝區
        cntR = 0; idxR = 0;
        while (!eofR && cntR < half) {
            if (!finR.read(reinterpret_cast<char*>(&bufR[cntR]), kRecSize))
                { eofR = true; break; }
            ++cntR;
        }
    };

    auto flushOut = [&]() { // 輸出緩衝區批次寫入磁碟，減少系統呼叫次數
        if (!bufOut.empty()) {
            fout.write(reinterpret_cast<const char*>(bufOut.data()),
                       static_cast<streamsize>(bufOut.size() * kRecSize));
            bufOut.clear();
        }
    };

    // 初次填充兩個輸入緩衝區
    refillL();
    refillR();

    while (true) {
        bool hasL = (idxL < cntL);
        bool hasR = (idxR < cntR);

        if (!hasL && eofL && !hasR && eofR) break; // 兩側都耗盡，結束

        // 緩衝區耗盡但檔案未結束 → 重新填充
        if (!hasL && !eofL) { refillL(); hasL = (idxL < cntL); }
        if (!hasR && !eofR) { refillR(); hasR = (idxR < cntR); }

        // 選出 weight 較大的那筆（相等時優先取左側，保持 stable）
        if (hasL && (!hasR || bufL[idxL].weight >= bufR[idxR].weight)) {
            bufOut.push_back(bufL[idxL++]);
        } else if (hasR) {
            bufOut.push_back(bufR[idxR++]);
        }

        if (bufOut.size() >= outCap) flushOut(); // 輸出緩衝區滿時批次寫出
    }
    flushOut(); // 合併結束後若輸出緩衝區仍有資料，直接寫出剩餘部分
}

// 外部排序主流程 (第一階段內部排序 + 第二階段兩兩合併)
static string runExternalSort() {
    cout << "##################################\n";
    cout << "Mission 1: External merge sort \n";
    cout << "##################################\n\n";

    // 輸入防呆：檔案不存在時重新詢問
    string num, inputFile;
    while (true) {
        cout << "Input the file name: [0]Quit\n";
        cin >> num;
        if (num == "0") return ""; // 使用者輸入 0 時直接回傳空字串

        inputFile = "pairs" + num + ".bin";
        if (fileExists(inputFile)) break;
        cout << "\n" << inputFile << " does not exist!!!\n\n";
    }

    auto t0 = chrono::high_resolution_clock::now();

    // 第一階段：內部排序，產生初始 runs
    ifstream fin(inputFile, ios::binary);
    vector<string> runs = createInitialRuns(fin);
    fin.close();

    auto t1 = chrono::high_resolution_clock::now();
    double msInternal = chrono::duration<double, milli>(t1 - t0).count();

    cout << "\nThe internal sort is completed. Check the initial sorted runs! \n\n";

    // 第二階段：反覆兩兩合併，直到只剩一個 run
    size_t mergeIdx = 0; // 合併暫存檔流水號，避免檔名衝突
    while (true) {
        cout << "Now there are " << runs.size() << " runs.\n\n";
        if (runs.size() == 1) break;

        vector<string> nextRuns;
        nextRuns.reserve((runs.size() + 1) / 2);

        for (size_t i = 0; i < runs.size(); i += 2) {
            if (i + 1 >= runs.size()) {
                // 奇數個 run：最後一個直接帶到下一輪
                nextRuns.push_back(runs[i]);
                continue;
            }
            string merged = "mrg_" + to_string(mergeIdx++) + ".bin";
            mergeTwoRuns(runs[i], runs[i + 1], merged);

            // 刪除已合併的暫存 run 檔，及時釋放磁碟空間
            remove(runs[i].c_str());
            remove(runs[i + 1].c_str());
            nextRuns.push_back(merged);
        }
        runs.swap(nextRuns);
    }

    auto t2 = chrono::high_resolution_clock::now();
    double msExternal = chrono::duration<double, milli>(t2 - t1).count();
    double msTotal    = msInternal + msExternal;

    // 直接 rename，不複製資料（最快）
    string orderFile = "order" + num + ".bin";
    rename(runs.front().c_str(), orderFile.c_str());

    cout << "The execution time ...\n";
    cout << "Internal Sort = " << fixed << setprecision(3) << msInternal << " ms\n";
    cout << "External Sort = " << fixed << setprecision(3) << msExternal << " ms\n";
    cout << "Total Execution Time = " << fixed << setprecision(3) << msTotal << " ms\n\n";

    return orderFile; // 回傳排序後輸出檔名（供任務二使用）
}

// ═════════════════════════════════════════════════════════
// 任務二：用 B-Tree 建立主索引（Primary Index）
// 輸入：任務一產生的已排序 orderXXX.bin
// 規則：
//   - 主索引存在記憶體的 B-Tree 中，與排序檔分開
//   - offset 單位為「筆數」（第幾筆紀錄，從 0 開始）
//   - 同一 key 只保留第一筆出現的 offset
//   - 分批讀檔，不整批載入
//   - 建完後中序走訪 B-Tree，由大到小輸出所有條目
// ═════════════════════════════════════════════════════════
static void runPrimaryIndex(const string& orderFile) {
    cout << "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n";
    cout << "Mission 2: Build the primary index \n";
    cout << "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n\n";
    cout << "<Primary index>: (key, offset)\n";

    ifstream fin(orderFile, ios::binary);
    if (!fin) return;

    BTree btree; // 主索引 B-Tree，存在記憶體中

    // 預先分配 buffer，分批讀取，不整批載入
    vector<Record> buf(kBufferSize);
    long long recordIdx = 0;  // 目前讀到的筆數（offset 單位）
    float     prevKey   = -1.f; // 上一個看到的 weight（初始值為不可能出現的值）

    while (true) {
        // 一次讀入最多 kBufferSize 筆
        size_t cnt = 0;
        while (cnt < kBufferSize &&
               fin.read(reinterpret_cast<char*>(&buf[cnt]), kRecSize))
            ++cnt;
        if (cnt == 0) break;

        for (size_t i = 0; i < cnt; ++i) {
            float w = buf[i].weight;
            // 遇到新的 key → 插入 B-Tree（只記錄第一筆的筆數 offset）
            if (w != prevKey) {
                btree.insert({w, recordIdx});
                prevKey = w;
            }
            ++recordIdx;
        }
    }

    // 中序走訪 B-Tree，取得由大到小排列的所有主索引條目
    vector<IndexEntry> result;
    btree.inorder(result);

    // 依序輸出（序號從 1 開始）
    for (size_t i = 0; i < result.size(); ++i) {
        cout << "[" << (i + 1) << "] ("
             << formatWeight(result[i].key) << ", "
             << result[i].offset << ")\n";
    }
}

static void printMenu() {
    cout << "\n* Data Structures and Algorithms *\n";
    cout << "**********************************\n";
    cout << "* 1. External merge sort on file *\n";
    cout << "* 2: Construct the primary index *\n";
    cout << "**********************************\n";
    cout << "*** The buffer size is 300\n";
}

int main() {
    while (true) {
        printMenu();

        // 任務一排序 → 任務二建索引（連續執行，不回選單）
        string orderFile = runExternalSort();
        if (!orderFile.empty()) {
            runPrimaryIndex(orderFile);
        }

        cout << "\n[0]Quit or [Any other key]continue?\n";
        string cont;
        cin >> cont;
        if (cont == "0") break;
    }
    return 0;
}