// 學號:11327227, 11327261 姓名:謝博丞, 周孝倫
#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <sstream>
#include <iomanip>
#include <cmath>
#include <cstring>

using namespace std;
struct StudentData {
    char sid[10];
    char name[10];
    unsigned char score[6];
    float average;
};
vector<StudentData> dataList;   // 目前載入的資料
string currentFileNum = "";     // 最後一次任務一的檔案編號
bool hasTask1Run = false;       // 是否已執行過任務一
struct HashNode {
    bool used = false;
    long long hvalue;
    char sid[10];
    char name[10];
    float average;
};

// 判斷質數
bool isPrime(int x) {
    if (x < 2) return false;
    for (int i = 2; i * i <= x; ++i)
        if (x % i == 0) return false;
    return true;
}

// 大於等於 x 的最小質數
int nextPrime(int x) {
    while (!isPrime(x)) ++x;
    return x;
}

// 雜湊函式：學號每個字元 ASCII 累乘後取 mod
long long hashKey(const char sid[], int mod) {
    long long res = 1;
    for (int i = 0; sid[i] != '\0'; ++i) {
        res = (res * (unsigned char)sid[i]) % mod;
    }
    return res % mod;
}

// 輔助：文字檔轉二進位檔
bool convertTxtToBin(const string& num) {
    string txtName = "input" + num + ".txt";
    string binName = "input" + num + ".bin";
    ifstream fin(txtName.c_str());
    if (!fin) {
        cout << endl << "### " << txtName << " does not exist! ###" << endl << endl;
        hasTask1Run=false;
        return false;
    }

    ofstream fout(binName.c_str(), ios::binary);
    string line;
    while (getline(fin, line)) {
        if (line.empty()) continue;
        istringstream iss(line);
        string token;
        StudentData s;
        memset(&s, 0, sizeof(s));

        getline(iss, token, '\t');
        strncpy(s.sid, token.c_str(), 9);
        s.sid[9] = '\0';

        getline(iss, token, '\t');
        strncpy(s.name, token.c_str(), 9);
        s.name[9] = '\0';

        for (int i = 0; i < 6; ++i) {
            getline(iss, token, '\t');
            s.score[i] = (unsigned char)stoi(token);
        }

        getline(iss, token, '\t');
        s.average = stof(token);

        fout.write((char*)&s, sizeof(s));
    }
    fin.close();
    fout.close();
    return true;
}

// 載入二進位檔到 dataList
bool loadBinary(const string& num) {
    string binName = "input" + num + ".bin";
    ifstream fin(binName.c_str(), ios::binary);
    if (!fin) return false;

    dataList.clear();
    StudentData s;
    while (fin.read((char*)&s, sizeof(s))) {
        dataList.push_back(s);
    }
    fin.close();
    return true;
}

// 準備資料：先嘗試讀 .bin，若無則轉換 .txt
bool prepareData(const string& num) {
    if (loadBinary(num)) return true;
    cout << endl << "### input" << num << ".bin does not exist! ###" << endl;
    if (convertTxtToBin(num)) {
        return loadBinary(num);
    }
    return false;
}

// =========================================================
// 任務一：平方探測雜湊表 class
// =========================================================
class QuadraticHashTable {
public:
    int m;                    // 表格大小（質數）
    vector<HashNode> table;
    int actualStored = 0;

    QuadraticHashTable(int n)
        : m(nextPrime((int)ceil(n * 1.15))), table(nextPrime((int)ceil(n * 1.15))) {}

    // 插入一筆資料
    void insert(const StudentData& s) {
        long long h = hashKey(s.sid, m);
        for (int i = 0; i < m; ++i) {
            int pos = (h + (long long)i * i) % m;  // Only +i², no negative direction
            if (!table[pos].used) {
                setNode(table[pos], h, s);
                ++actualStored;
                return;
            }
        }
        cout << "Cannot insert: " << s.sid << endl;
    }

    // 計算成功搜尋平均比較次數（只對實際存入的資料）
    double calcSuccessfulAvg() const {
        double sucSum = 0.0;
        for (int idx = 0; idx < m; ++idx) {
            if (!table[idx].used) continue;
            long long h = hashKey(table[idx].sid, m);
            for (int i = 0; i < m; ++i) {
                ++sucSum;
                int pos = (h + (long long)i * i) % m;  // Only +i², match insertion direction
                if (table[pos].used && strncmp(table[pos].sid, table[idx].sid, 10) == 0) break;
            }
        }
        return sucSum / actualStored;
    }

    // 計算不成功搜尋平均比較次數（對每個可能的 hash 值 0 ~ m-1）
    double calcUnsuccessfulAvg() const {
        double unsucSum = 0.0;
        for (int h = 0; h < m; ++h) {
            for (int i = 0; i < m; ++i) {
                int pos = (h + (long long)i * i) % m;
                if (!table[pos].used) break;  // Stop, do NOT count this empty slot
                ++unsucSum;                   // Only count occupied slots
            }
        }
        return unsucSum / m;
    }

    // 輸出表格到檔案
void saveToFile(const string& outName) const {
    ofstream fout(outName.c_str());
    fout << "--- Hash table created by Quadratic probing ---" << endl;
    for (int i = 0; i < m; ++i) {
        fout << "[" << setw(3) << i << "]";
        if (table[i].used) {
            // average: 去掉 fixed，讓它自動省略尾零
            // 但要右對齊11格
            ostringstream avgSS;
            // 去除尾部多餘的零
            float avg = table[i].average;
            if (avg == (int)avg)
                avgSS << (int)avg;
            else {
                avgSS << fixed << setprecision(2) << avg;
                // 去掉尾零
                string s = avgSS.str();
                s.erase(s.find_last_not_of('0') + 1);
                avgSS.str(""); avgSS << s;
            }
            fout << setw(11) << table[i].hvalue << ","
                 << setw(11) << table[i].sid << ","
                 << setw(11) << table[i].name << ","
                 << setw(11) << avgSS.str();
        }
        else
            fout<<" ";
        fout << endl;
    }
    fout << " -----------------------------------------------------" << endl;
    fout.close();
}

private:
    void setNode(HashNode& node, long long h, const StudentData& s) {
        node.used = true;
        node.hvalue = h;
        memcpy(node.sid, s.sid, 10);
        memcpy(node.name, s.name, 10);
        node.average = s.average;
    }
};

// =========================================================
// 任務二：雙重雜湊表 class
// =========================================================
class DoubleHashTable {
public:
    int m;                    // 表格大小（質數）
    int R;                    // 最高步距（質數）
    vector<HashNode> table;
    int actualStored = 0;

    DoubleHashTable(int n)
        : m(nextPrime((int)ceil(n * 1.15))),
          R(nextPrime((int)ceil(n / 5.0))),
          table(nextPrime((int)ceil(n * 1.15))) {}

    // 插入一筆資料
    void insert(const StudentData& s) {
        long long h = hashKey(s.sid, m);
        long long step = R - hashKey(s.sid, R);
        if (step == 0) step = 1;   // 避免步距為 0
        bool placed = false;
        for (int i = 0; i < m; ++i) {
            int pos = (h + i * step) % m;
            if (!table[pos].used) {
                setNode(table[pos], h, s);
                placed = true;
                ++actualStored;
                break;
            }
        }
        if (!placed) {
            cout << "Cannot insert: " << s.sid << endl;
        }
    }

    // 計算成功搜尋平均比較次數
    double calcSuccessfulAvg() const {
        double sucSum = 0.0;
        for (int idx = 0; idx < m; ++idx) {
            if (!table[idx].used) continue;
            long long h = hashKey(table[idx].sid, m);
            long long step = R - hashKey(table[idx].sid, R);
            if (step == 0) step = 1;
            for (int i = 0; i < m; ++i) {
                ++sucSum;
                int pos = (h + i * step) % m;
                if (table[pos].used && strncmp(table[pos].sid, table[idx].sid, 10) == 0) break;
            }
        }
        return sucSum / actualStored;
    }

    // 輸出表格到檔案
void saveToFile(const string& outName) const {
    ofstream fout(outName.c_str());
    fout << "--- Hash table created by Double hashing    ---" << endl;
    for (int i = 0; i < m; ++i) {
        fout << "[" << setw(3) << i << "]";
        if (table[i].used) {
            // 處理 average 去除尾零
            ostringstream avgSS;
            float avg = table[i].average;
            if (avg == (int)avg) {
                avgSS << (int)avg;
            } else {
                avgSS << fixed << setprecision(2) << avg;
                string s = avgSS.str();
                s.erase(s.find_last_not_of('0') + 1);
                avgSS.str(""); avgSS << s;
            }
            fout << setw(11) << table[i].hvalue << ","
                 << setw(11) << table[i].sid << ","
                 << setw(11) << table[i].name << ","
                 << setw(11) << avgSS.str();
        }
        else
            fout<<" ";
        fout << endl;
    }
    fout << " -----------------------------------------------------" << endl;
    fout.close();
}

private:
    void setNode(HashNode& node, long long h, const StudentData& s) {
        node.used = true;
        node.hvalue = h;
        memcpy(node.sid, s.sid, 10);
        memcpy(node.name, s.name, 10);
        node.average = s.average;
    }
};

// =========================================================
// 任務一：平方探測
// =========================================================
void task1(const string& num) {
    if (!prepareData(num)) return;

    currentFileNum = num;
    hasTask1Run = true;

    QuadraticHashTable ht(dataList.size());

    for (const auto& s : dataList)
        ht.insert(s);

    double sucAvg   = ht.calcSuccessfulAvg();
    double unsucAvg = ht.calcUnsuccessfulAvg();

    ht.saveToFile("quadratic" + num + ".txt");

    cout << "\nHash table has been successfully created by Quadratic probing" << endl;
    cout << "unsuccessful search: " << fixed << setprecision(4) << unsucAvg << " comparisons on average" << endl;
    cout << "successful search: "   << fixed << setprecision(4) << sucAvg   << " comparisons on average" << endl;
}

// =========================================================
// 任務二：雙重雜湊
// =========================================================
void task2() {
    if (!hasTask1Run) {
        cout << "### Command 1 first. ###" << endl << endl;
        return;
    }
    if (dataList.empty()) return;

    DoubleHashTable ht(dataList.size());

    for (const auto& s : dataList)
        ht.insert(s);

    double sucAvg = ht.calcSuccessfulAvg();

    ht.saveToFile("double" + currentFileNum + ".txt");

    cout << "\nHash table has been successfully created by Double hashing   " << endl;
    cout << "successful search: " << fixed << setprecision(4) << sucAvg << " comparisons on average" << endl;
}

// 主程式
int main() {
    string choice;
    while (true) {
        cout << "\n* Data Structures and Algorithms *\n"
                "************ Hash Table **********\n"
                "* 0. QUIT                        *\n"
                "* 1. Quadratic probing           *\n"
                "* 2. Double hashing              *\n"
                "**********************************\n"
                "Input a choice(0, 1, 2): ";
        cin >> choice;
        if (choice == "0") {
            break;
        }
        else if (choice == "1") {
            cout << "\nInput a file number ([0] Quit): ";
            string num;
            cin >> num;
            if (num != "0") task1(num);
            else{
                cout<<endl;
                hasTask1Run=false;
            }
        }
        else if (choice == "2") {
            task2();
        }
        else {
            cout << endl << "Command does not exist!" << endl << endl;
        }
    }
    return 0;
}
