//11327261 周孝倫

#include <iostream>
#include <vector>
#include <fstream>

using namespace std;
class Department {
public:
    static int stid;
    int graduate,node_id;
    string school_name,department_name,daynight,level,city,system,school_id,department_id,student,teacher;
    Department(string schid,string schnm,string dpid,string dpnm,string dn,string level,string stu,string tch,int grad,string city,string sys){
        this->node_id=stid;
        stid++;
        this->school_id=schid;
        this->school_name=schnm;
        this->department_id=dpid;
        this->department_name=dpnm;
        this->daynight=dn;
        this->level=level;
        this->student=stu;
        this->teacher=tch;
        this->graduate=grad;
        this->city=city;
        this->system=sys;
    }
    Department(int grad) {
        this->node_id=stid;
        stid++;
        this->graduate=grad;
    }
};
int Department::stid=1;
int father(int cur) {
    return (cur-1)/2;
}
int level(int n) {
    return n==0?0:level(father(n))+1;
}
int partner(int i) {
    return i?(i+(1<<(level(i)-1))*((i<=(((((1<<level(i)))+(1<<(level(i)+1))-4)/2)))?1:-1)):0;
}
int whereAmI(int i) {
    return partner(i)<i; //1=max side, 0=min side
}
bool operator<(const Department& a,const Department& b) {
    return a.graduate<b.graduate;
}
bool operator>(const Department& a,const Department& b) {
    return a.graduate>b.graduate;
}
bool operator>=(const Department& a,const Department& b) {
    return a.graduate>=b.graduate;
}
bool operator<=(const Department& a,const Department& b) {
    return a.graduate<=b.graduate;
}
bool operator==(const Department& a,const Department& b) {
    return a.graduate==b.graduate;
}
ostream& operator<<(ostream& os,const Department& d) {
    os<<"["<<d.node_id<<"] "<<d.graduate;
    return os;
}
ostream& operator>>(ostream&os, const Department& d) {
    os<<d.node_id<<"\t"<<d.school_name<<"\t"<<d.department_name<<"\t"<<d.daynight<<"\t"<<d.level<<"\t"<<d.graduate;
    return os;
}
vector<string> split(string& s,char tab) {
    vector<string>ret;
    size_t start=0;
    size_t end;
    while((end=s.find(tab,start))!=string::npos) {
        ret.push_back(s.substr(start,end-start));
        start=end+1;
    }
    ret.push_back(s.substr(start));
    return ret;
}
vector<Department>& operator+=(vector<Department>&heap, const Department &d) {
    heap.push_back(d);
    int cur=heap.size()-1;
    if(cur>0&&((level(cur)&1)==heap[cur]<heap[father(cur)])) {
        swap(heap[cur],heap[father(cur)]);
        cur=father(cur);
    }
    while(cur>2&&((level(cur)&1)==heap[cur]>=heap[father(father(cur))])) {
        swap(heap[cur],heap[father(father(cur))]);
        cur=father(father(cur));
    }
    return heap;
}
vector<Department>& operator-=(vector<Department>&heap, const Department &d) {
    heap.push_back(d);
    int cur=heap.size()-1;
    while(cur>0&&heap[cur]<heap[father(cur)]) {
        swap(heap[cur],heap[father(cur)]);
        cur=father(cur);
    }
    return heap;
}
vector<Department>& operator*=(vector<Department>&heap,const Department &d) {
    heap.push_back(d);
    int cur=heap.size()-1;
    int pk;
    if(partner(cur)>=heap.size())
        pk=father(partner(cur));
    else
        pk=partner(cur);
    if(pk<1)
        return heap;
    if(whereAmI(pk)==(heap[pk]<=heap[cur])){
        swap(heap[pk],heap[cur]);
        cur=pk;
    }
    while(father(cur)>0&&(whereAmI(cur)==(heap[father(cur)]<heap[cur]))) {
        swap(heap[father(cur)],heap[cur]);
        cur=father(cur);
    }
    return heap;
}
void operator/=(vector<Department>&heap, int i) {
    for (int cnt = 0; cnt < i; ++cnt) {
        if (heap.size() < 2) {
            break;
        }
        Department ans = heap[2];
        swap(heap[2], heap.back());
        heap.pop_back();
        int cur = 2;
        while (true) {
            int child = -1;
            if (whereAmI(cur)) {
                int left = cur*2+1;
                int right = cur*2+2;
                if (left < heap.size() && (child == -1 || heap[left] > heap[child])) child = left;
                if (right < heap.size() && (child == -1 || heap[right] > heap[child])) child = right;
                if (child != -1 && heap[child] > heap[cur]) {
                    swap(heap[child], heap[cur]);
                    cur = child;
                    continue;
                }
                int p = partner(cur);
                if (p < heap.size() && heap[p] > heap[cur]) {
                    swap(heap[p], heap[cur]);
                    int up = p;
                    while (up > 1 && heap[up] < heap[father(up)]) {
                        swap(heap[up], heap[father(up)]);
                        up = father(up);
                    }
                }
                break;
            } else {
                break;
            }
        }
        cout >> ans << endl;
    }
}

int main() {
    int work;
    vector<Department>heap;
    while(true) {
        string d,fileName;
        do {
            cout<<"\n* Data Structures and Algorithms *\n*** Heap Construction and Use ****\n* 0. QUIT                        *\n* 1. Build a min heap            *\n* 2. Build a min-max heap        *\n* 3. Build a DEAP                *\n* 4: Top-K maximum from DEAP     *\n**********************************\nInput a choice(0, 1, 2, 3, 4): ";
            cin>>work;
            switch(work) {
                case 0: return 0;
                case 1: break;
                case 2: break;
                case 3: break;
                case 4: break;
                default:
                cout<<"\nCommand does not exist!\n";break;
            }
        }while(work<0||work>4);
        if(work==4) {
            if(heap.size()<=1)cout<<"\nheap is empty\n";
            else {
                int k;
                do {
                    cout<<"\nenter a number between [1] and ["<<(heap.size()-1)<<"]: ";
                    cin>>k;
                    if(k>=heap.size()||k<=0)
                        cout<<endl<<k<<" is not in correct range\n";
                }while(k>=heap.size()||k<=0);
                heap/=k;
            }
        }
        else
        while(true) {
            cout<<"\nInput a file number ([0] Quit): ";
            cin>>fileName;
            ifstream file("input"+fileName+".txt");
            if(fileName!="0") {
                if(!file) {
                    cout<<"\n### input"<<fileName<<".txt does not exist! ###\n";
                    continue;
                }
                else {
                    heap.clear();
                    Department::stid=1;
                    for(int i=0;i<3;i++)
                        getline(file,d);
                    switch(work) {
                    case 2:
                        cout<<"<min-max heap>";
                        while(getline(file,d)) {
                            size_t pos=0;
                            for(int i=0;i<8;i++)
                                pos=d.find('\t',pos)+1;
                            size_t end=d.find('\t',pos);
                            heap+=Department(stoi(d.substr(pos,end-pos)));
                        }
                        cout<<"\nroot: "<<heap[0]<<"\nbottom: "<<heap[heap.size()-1]<<"\nleftmost bottom: "<<heap[int(1<<(level(heap.size()-1)))-1]<<endl;
                        break;
                    case 1:
                        cout<<"<min heap>";
                        while(getline(file,d)) {
                            size_t pos=0;
                            for(int i=0;i<8;i++)
                                pos=d.find('\t',pos)+1;
                            size_t end=d.find('\t',pos);
                            heap-=Department(stoi(d.substr(pos,end-pos)));
                        }
                        cout<<"\nroot: "<<heap[0]<<"\nbottom: "<<heap[heap.size()-1]<<"\nleftmost bottom: "<<heap[int(1<<(level(heap.size()-1)))-1]<<endl;
                        break;
                    case 3:
                        cout<<"<DEAP>";
                        heap.push_back(Department(-1));
                        Department::stid-=1;
                        while(getline(file,d)) {
                            vector<string> data=split(d,'\t');
                            heap*=Department(data[0],data[1],data[2],data[3],data[4],data[5],data[6],data[7],stoi(data[8]),data[9],data[10]);
                        }
                        cout<<"\nbottom: "<<heap[heap.size()-1]<<"\nleftmost bottom: "<<heap[int(1<<(level(heap.size()-1)))-1]<<endl;
                        break;
                }
                break;
            }
        }
        else break;
    }
}
}
