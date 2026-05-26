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
    float weight;
};

// 主陣列的節點
struct Node {
    string id;
    vector<Edge> edges;
};

// 連通數結果
struct CountEntry {
    string id;
    vector<string> reachable;
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

private:
    vector<Node> nodes_; // 相鄰串列主陣列
    unordered_map<string, size_t> idToIndex_; // 學號到索引的對照表
    size_t totalEdges_ = 0; // 總邊數
    bool built_ = false; // 是否已建立相鄰串列
    string lastBaseName_; // 上一次輸入的檔名

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
        allIds.reserve(records.size() * 2);

        // 蒐集所有學號
        for (const auto& rec : records) {
            string putId = fixedToString(rec.putID, sizeof(rec.putID));
            string getId = fixedToString(rec.getID, sizeof(rec.getID));
            allIds.push_back(putId);
            allIds.push_back(getId);
        }

        // 主陣列依學號排序
        sort(allIds.begin(), allIds.end());
        allIds.erase(unique(allIds.begin(), allIds.end()), allIds.end());

        // 建立主陣列與索引
        nodes_.clear();
        nodes_.reserve(allIds.size());
        idToIndex_.clear();
        idToIndex_.reserve(allIds.size());

        for (size_t i = 0; i < allIds.size(); ++i) {
            Node node;
            node.id = allIds[i];
            nodes_.push_back(node);
            idToIndex_[allIds[i]] = i;
        }

        // 將邊加入發訊者的串列
        for (const auto& rec : records) {
            string putId = fixedToString(rec.putID, sizeof(rec.putID));
            string getId = fixedToString(rec.getID, sizeof(rec.getID));
            size_t idx = idToIndex_[putId];
            nodes_[idx].edges.push_back(Edge{getId, rec.weight});
        }

        // 串列內依收訊者排序
        totalEdges_ = 0;
        for (auto& node : nodes_) {
            sort(node.edges.begin(), node.edges.end(), [](const Edge& a, const Edge& b) {
                return a.to < b.to;
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

        for (size_t start = 0; start < n; ++start) {
            if (stamp == INT_MAX) {
                fill(visited.begin(), visited.end(), 0);
                stamp = 1;
            }

            // 使用 queue 進行 BFS
            queue<size_t> q;
            visited[start] = stamp;
            q.push(start);

            vector<string> reachable;
            while (!q.empty()) {
                size_t u = q.front();
                q.pop();
                for (const auto& e : nodes_[u].edges) {
                    auto it = idToIndex_.find(e.to);
                    if (it == idToIndex_.end()) continue;
                    size_t v = it->second;
                    if (visited[v] == stamp) continue;
                    visited[v] = stamp;
                    q.push(v);
                    if (v != start) reachable.push_back(nodes_[v].id);
                }
            }

            // 收集並排序可達學號
            sort(reachable.begin(), reachable.end());
            CountEntry entry;
            entry.id = nodes_[start].id;
            entry.reachable = move(reachable);
            results.push_back(move(entry));
            ++stamp;
        }

        // 依連通數由大到小排序，連通數相同時依學號排序
        sort(results.begin(), results.end(), [](const CountEntry& a, const CountEntry& b) {
            if (a.reachable.size() != b.reachable.size()) {
                return a.reachable.size() > b.reachable.size();
            }
            return a.id < b.id;
        });

        return results;
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
    cout << "**********************************\n";
    cout << "Input a choice(0, 1, 2): ";
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
        } else {
            cout << "\nCommand does not exist!\n";
        }
    }

    return 0;
}
