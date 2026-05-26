// 學號:11327227, 11327261 姓名:謝博丞, 周孝倫

// 標準函式庫
#include <algorithm>
#include <climits>
#include <cstring>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <queue>
#include <sstream>
#include <string>
#include <unordered_map>
#include <vector>
#include <chrono>
#include <cctype>
using namespace std;

// 以 1-byte 對齊，確保讀寫二進位欄位與檔案一致
#pragma pack(push, 1)
struct EdgeRecord {
    char putID[12];
    char getID[12];
    float weight;
};
#pragma pack(pop)

// 相鄰串列的邊
struct Edge {
    string to;
    float weight; // 權重
};

// 主陣列的節點
struct Node {
    string id;
    vector<Edge> edges; // 發訊者的串列
};

// 連通數結果
struct CountEntry {
    string id;
    vector<string> reachable; // 可達學號列表
};

// 檢查檔案是否存在
static bool fileExists(const string& path) {
    ifstream fin(path, ios::binary);
    return fin.good();
}

// 固定長度字元陣列轉成字串
static string fixedToString(const char* src, size_t len) {
    string out;
    out.reserve(len);
    for (size_t i = 0; i < len; ++i) {
        if (src[i] == '\0') break;
        out.push_back(src[i]);
    }
    if (out.empty()) {
        out.assign(src, src + len);
    }
    return out;
}

// 權重輸出格式，去除多餘 0
static string formatWeight(float value) {
    ostringstream oss;
    oss.setf(ios::fixed);
    oss << setprecision(2) << value;
    string s = oss.str();

    // 去除小數點後多餘的 0 和可能的小數點
    while (!s.empty() && s.back() == '0') s.pop_back();
    if (!s.empty() && s.back() == '.') s.pop_back();
    return s;
}

// 相鄰串列與連通數的封裝類別
class AdjacencyGraph {
public:
    // 任務一：建立相鄰串列並輸出 .adj
    void taskBuildAdjacency() {
        built_ = false;
        cout << "\nInput a file number ([0] Quit): ";
        string num;
        cin >> num;
        if (num == "0") return;

        string baseName = "pairs" + num;
        string binPath = baseName + ".bin";
        if (!fileExists(binPath)) {
            cout << "\n### " << binPath << " does not exist! ###\n";
            return;
        }

        // 讀取二進位資料
        vector<EdgeRecord> records;
        if (!readBin(binPath, records)) {
            cout << "\n### " << binPath << " does not exist! ###\n";
            return;
        }

        // 建立相鄰串列
        buildAdjacency(records);
        lastBaseName_ = baseName;
        built_ = true;

        // 輸出相鄰串列
        string outPath = baseName + ".adj";
        writeAdjacency(outPath);

        cout << "\n<<< There are " << nodes_.size() << " IDs in total. >>>\n";
        cout << "\n<<< There are " << totalEdges_ << " nodes in total. >>>\n";
    }

    // 任務二：計算連通數並輸出 .cnt
    void taskComputeCounts() {
        if (!built_) {
            cout << "### There is no graph and choose 1 first. ###\n";
            return;
        }

        // 計算所有節點的連通數
        vector<CountEntry> counts = computeConnectionCounts();
        string outPath = lastBaseName_ + ".cnt";
        writeCounts(outPath, counts);

        cout << "\n<<< There are " << nodes_.size() << " IDs in total. >>>\n";
    }

    // 任務四：固定門檻(0.9)找出前K名估計影響力 top-k estimated influence
    void taskTopKEstimatedInfluence() {
        if (!built_) {
            cout << "### There is no graph and choose 1 first. ###\n";
            return;
        }

        using clock = chrono::high_resolution_clock;
        const float threshold = 0.9f;

        auto t0 = clock::now();
        vector<InfluenceEntry> all = computeInfluenceAll(threshold);
        auto t1 = clock::now();

        auto elapsedMs = chrono::duration_cast<chrono::milliseconds>(t1 - t0).count();
        cout << "\n[Elapsed time] " << elapsedMs << " ms\n\n";

        vector<InfluenceEntry> positive;
        positive.reserve(all.size());
        for (auto &e : all) {
            if (e.influence > 0) positive.push_back(move(e));
        }
        if (positive.empty()) return;

        int K = 0;
        while (true) {
            cout << "Input an integer to show top-K in [1," << positive.size() << "]: ";
            string s;
            cin >> s;

            if (s.empty() || !all_of(s.begin(), s.end(), [](unsigned char ch) { return isdigit(ch); })) {
                cout << "\n";
                continue;
            }

            long long v = 0;
            try {
                v = stoll(s);
            } catch (...) {
                cout << "\n";
                continue;
            }

            if (v < 1 || v > (long long)positive.size()) {
                cout << "\n### " << v << " is NOT in [1," << positive.size() << "] ###\n\n";
                continue;
            }

            K = (int)v;
            break;
        }

        cout << "\n";

        sort(positive.begin(), positive.end(), [](const InfluenceEntry& a, const InfluenceEntry& b) {
            if (a.influence != b.influence) return a.influence > b.influence;
            return a.id < b.id;
        });

        int cutoffInfluence = 0;
        if ((int)positive.size() >= K) cutoffInfluence = positive[K - 1].influence;
        else cutoffInfluence = positive.back().influence;

        int rank = 1;
        for (const auto& e : positive) {
            if (rank > K && e.influence < cutoffInfluence) break;
            cout << "<" << rank << "> " << e.id << ": " << e.influence << "\n";
            ++rank;
        }
    }

    // 任務三：自訂門檻(0.9~1.0)估計影響力並輸出 .inf
    void taskEstimateInfluenceValues() {
        if (!built_) {
            cout << "### There is no graph and choose 1 first. ###\n";
            return;
        }

        float threshold = 0.0f;
        while (true) {
            cout << "\nInput a real number in [0.9,1.0]: ";
            string s;
            cin >> s;

            bool hasDigit = false;
            bool validFormat = true;
            for (char ch : s) {
                if (isdigit(static_cast<unsigned char>(ch))) {
                    hasDigit = true;
                    continue;
                }
                if (ch == '.') continue;
                validFormat = false;
                break;
            }

            if (!validFormat || !hasDigit) {
                continue;
            }

            float value = 0.0f;
            try {
                value = stof(s);
            } catch (...) {
                continue;
            }

            if (value <= 0.9f || value > 1.0f) {
                cout << "\n### It is NOT in [0.9,1.0] ###\n";
                continue;
            }

            threshold = value;
            break;
        }

        vector<InfluenceEntry> all = computeInfluenceAll(threshold);
        vector<InfluenceEntry> positive;
        positive.reserve(all.size());
        for (auto &e : all) {
            if (e.influence > 0) positive.push_back(move(e));
        }

        sort(positive.begin(), positive.end(), [](const InfluenceEntry& a, const InfluenceEntry& b) {
            if (a.influence != b.influence) return a.influence > b.influence;
            return a.id < b.id;
        });

        cout << "\n<<< There are " << positive.size() << " IDs in total. >>>\n";
        string outPath = lastBaseName_ + ".inf";
        writeInfluence(outPath, positive);
    }

private:
    vector<Node> nodes_; // 相鄰串列主陣列
    unordered_map<string, size_t> idToIndex_; // 學號到索引的對照表
    size_t totalEdges_ = 0; // 總邊數
    bool built_ = false; // 是否已建立相鄰串列
    string lastBaseName_; // 上一次輸入的檔名

    struct InfluenceEntry {
        string id;
        int influence = 0;
        vector<string> reachable;
    };

    // 讀取二進位檔的所有紀錄
    static bool readBin(const string& path, vector<EdgeRecord>& out) {
        ifstream fin(path, ios::binary);
        if (!fin) return false;

        out.clear();
        EdgeRecord rec;
        while (fin.read(reinterpret_cast<char*>(&rec), sizeof(EdgeRecord))) {
            out.push_back(rec);
        }
        return true;
    }

    // 建立相鄰串列與主陣列
    void buildAdjacency(const vector<EdgeRecord>& records) {
        vector<string> allIds;
        allIds.reserve(records.size() * 2); // 每筆紀錄有兩個學號

        // 蒐集所有學號
        for (const auto& rec : records) {
            string putId = fixedToString(rec.putID, sizeof(rec.putID));
            string getId = fixedToString(rec.getID, sizeof(rec.getID));
            allIds.push_back(putId);
            allIds.push_back(getId);
        }

        // 主陣列依學號排序
        sort(allIds.begin(), allIds.end());
        // unique 移動重複學號到結尾，erase 刪除重複學號
        allIds.erase(unique(allIds.begin(), allIds.end()), allIds.end());

        // 建立主陣列與索引
        nodes_.clear();
        nodes_.reserve(allIds.size());
        idToIndex_.clear();
        idToIndex_.reserve(allIds.size());

        // 將學號加入主陣列並建立索引
        for (size_t i = 0; i < allIds.size(); ++i) {
            Node node;
            node.id = allIds[i];
            nodes_.push_back(node); // 把學號加入主陣列
            idToIndex_[allIds[i]] = i; // 建立學號到索引的對照表
        }

        // 將邊加入發訊者的串列
        for (const auto& rec : records) {
            string putId = fixedToString(rec.putID, sizeof(rec.putID));
            string getId = fixedToString(rec.getID, sizeof(rec.getID));

            size_t idx = idToIndex_[putId]; // 找到發送者索引在哪
            // 將收訊者和權重加入發訊者的串列
            nodes_[idx].edges.push_back(Edge{getId, rec.weight});
        }

        // 串列內依收訊者排序
        totalEdges_ = 0;
        for (auto& node : nodes_) {
            sort(node.edges.begin(), node.edges.end(), [](const Edge& a, const Edge& b) {
                return a.to < b.to; // 如果 a.to 比 b.to 小，則 a 要排在前面。
            });
            totalEdges_ += node.edges.size();
        }
    }

    // 輸出相鄰串列到文字檔
    void writeAdjacency(const string& outPath) const {
        ofstream fout(outPath);
        if (!fout) return;

        fout << "<<< There are " << nodes_.size() << " IDs in total. >>>\n";
        for (size_t i = 0; i < nodes_.size(); ++i) {
            fout << "[" << setw(3) << (i + 1) << "] " << nodes_[i].id << ": ";
            if (nodes_[i].edges.empty()) {
                fout << "\n";
                continue;
            }

            fout << "\n";
            for (size_t j = 0; j < nodes_[i].edges.size(); ++j) {
                const Edge& e = nodes_[i].edges[j];
                fout << "\t(" << setw(2) << (j + 1) << ") "
                     << e.to << ", "
                     << setw(6) << formatWeight(e.weight);

                if (j % 12 == 11) fout << "\n"; // 每 12 個換行
            }
            fout << "\n";
        }
        fout << "<<< There are " << totalEdges_ << " nodes in total. >>>\n";
    }

    // 以 BFS 計算每個學號的連通數
    vector<CountEntry> computeConnectionCounts() const {
        vector<CountEntry> results;
        size_t n = nodes_.size();
        results.reserve(n);

        // 用時間戳避免每次清空 visited
        vector<int> visited(n, 0);
        int stamp = 1;

        // 從每個節點出發計算「可達節點」
        for (size_t start = 0; start < n; ++start) {
            // 如果時間戳達到 INT_MAX，重置 visited 並從 1 開始
            if (stamp == INT_MAX) {
                fill(visited.begin(), visited.end(), 0);
                stamp = 1;
            }

            // 使用 queue 進行 BFS
            queue<size_t> q;
            visited[start] = stamp;
            q.push(start);

            // 收集「可達節點」的學號列表
            vector<string> reachable;
            while (!q.empty()) {
                // 取出隊伍第一個節點 u
                size_t u = q.front();
                q.pop();

                // 遍歷 u 的所有邊，找到收訊者節點
                for (const auto& e : nodes_[u].edges) {
                    auto it = idToIndex_.find(e.to); // 找到收訊者的索引
                    if (it == idToIndex_.end()) continue; // 如果找不到收訊者，跳過

                    size_t v = it->second; // 找到收訊者節點 v
                    if (visited[v] == stamp) continue; // 如果收訊者已訪問過，跳過

                    // 標記收訊者為已訪問並加入 BFS 隊伍
                    visited[v] = stamp;
                    q.push(v);

                    // 只有當 v 不是起點時才加入可達列表
                    if (v != start) reachable.push_back(nodes_[v].id);
                }
            }

            // 收集並排序可達學號
            sort(reachable.begin(), reachable.end());
            CountEntry entry;
            entry.id = nodes_[start].id;
            entry.reachable = move(reachable); // 使用 move 避免不必要的複製
            results.push_back(move(entry));
            ++stamp; // 增加時間戳以區分下一輪 BFS
        }

        // 依連通數由大到小排序，連通數相同時依學號排序
        sort(results.begin(), results.end(), [](const CountEntry& a, const CountEntry& b) {
            if (a.reachable.size() != b.reachable.size()) {
                // 依「連通數」（可達節點數量）由大到小排序
                return a.reachable.size() > b.reachable.size();
            }
            // 若連通數相同，則依學號由小到大排序
            return a.id < b.id;
        });

        return results;
    }

    // 以 DFS(堆疊) 計算單一節點的 influence：只走 weight >= threshold 的有效邊
    InfluenceEntry computeInfluenceOne(size_t start, float threshold) const {
        InfluenceEntry out;
        out.id = nodes_[start].id;

        const size_t n = nodes_.size();
        vector<char> visited(n, 0);
        vector<size_t> st;
        st.reserve(64);

        visited[start] = 1;
        st.push_back(start);

        while (!st.empty()) {
            size_t u = st.back();
            st.pop_back();

            for (const auto& e : nodes_[u].edges) {
                if (e.weight + 1e-6f < threshold) continue;

                auto it = idToIndex_.find(e.to);
                if (it == idToIndex_.end()) continue;

                size_t v = it->second;
                if (visited[v]) continue;

                visited[v] = 1;
                st.push_back(v);
                if (v != start) out.reachable.push_back(nodes_[v].id);
            }
        }

        sort(out.reachable.begin(), out.reachable.end());
        out.influence = (int)out.reachable.size();
        return out;
    }

    // 計算所有節點 influence
    vector<InfluenceEntry> computeInfluenceAll(float threshold) const {
        vector<InfluenceEntry> res;
        res.reserve(nodes_.size());
        for (size_t i = 0; i < nodes_.size(); ++i) {
            res.push_back(computeInfluenceOne(i, threshold));
        }
        return res;
    }

    // 輸出影響力結果到文字檔(.inf)
    void writeInfluence(const string& outPath, const vector<InfluenceEntry>& entries) const {
        ofstream fout(outPath);
        if (!fout) return;

        fout << "<<< There are " << entries.size() << " IDs in total. >>>\n";
        for (size_t i = 0; i < entries.size(); ++i) {
            const InfluenceEntry& entry = entries[i];
            fout << "[" << setw(3) << (i + 1) << "] " << entry.id
                 << "(" << entry.influence << "): ";

            if (entry.reachable.empty()) {
                fout << "\n";
                continue;
            }

            for (size_t j = 0; j < entry.reachable.size(); ++j) {
                if (j % 12 == 0) fout << "\n";
                fout << "\t(" << setw(2) << (j + 1) << ") " << entry.reachable[j];
            }

            if (entry.reachable.size() % 12 == 0) fout << "\n";
            fout << "\n";
        }
    }

    // 輸出連通數結果到文字檔
    void writeCounts(const string& outPath, const vector<CountEntry>& counts) const {
        ofstream fout(outPath);
        if (!fout) return;

        fout << "<<< There are " << nodes_.size() << " IDs in total. >>>\n";
        for (size_t i = 0; i < counts.size(); ++i) {
            const CountEntry& entry = counts[i];
            fout << "[" << setw(3) << (i + 1) << "] " << entry.id
                 << "(" << entry.reachable.size() << "): ";

            if (entry.reachable.empty()) {
                fout << "\n";
                continue;
            }

            for (size_t j = 0; j < entry.reachable.size(); ++j) {
                if (j % 12 == 0) fout << "\n";
                fout << "\t(" << setw(2) << (j + 1) << ") " << entry.reachable[j];
            }

            // 若剛好最後一行滿 12 個，補上換行
            if (entry.reachable.size() % 12 == 0) fout << "\n";
            fout << "\n";
        }
    }
};

// 輸出選單
static void printMenu() {
    cout << "\n* Data Structures and Algorithms *\n";
    cout << "**** Graph data manipulation *****\n";
    cout << "* 0. QUIT                        *\n";
    cout << "* 1. Build adjacency lists       *\n";
    cout << "* 2. Compute connection counts   *\n";
    cout << "* 3. Estimate influence values   *\n";
    cout << "* 4. Find top-k influence values *\n";
    cout << "**********************************\n";
    cout << "Input a choice(0, 1, 2, 3, 4): ";
}

int main() {
    // 建立圖物件
    AdjacencyGraph graph;

    // 互動式選單
    while (true) {
        printMenu();
        string choice = "0";
        if (!(cin >> choice) || choice == "0") break;

        // 依選項執行任務
        if (choice == "1") {
            graph.taskBuildAdjacency();
        } else if (choice == "2") {
            graph.taskComputeCounts();
        } else if (choice == "3") {
            graph.taskEstimateInfluenceValues();
        } else if (choice == "4") {
            graph.taskTopKEstimatedInfluence();
        } else {
            cout << "\nCommand does not exist!\n";
        }
    }

    return 0;
}
