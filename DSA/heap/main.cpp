#include <iostream>
#include <vector>
#include <unordered_map>
using namespace std;

string toString(vector<int>&v) {
    string s = "[";
    for(int i = 0; i < v.size(); i++) {
        s += to_string(v[i]);
        if(i != v.size()-1) s += " ";
    }
    s += "]";
    return s;
}

int main()
{
    vector<int> minHeap, maxHeap;
    unordered_map<int,int> mapMinToMax, mapMaxToMin; // map[minHeap idx] = maxHeap idx
    int n, code;

    while(cin>>code) {
        switch(code) {
        case 0: // 插入
            cin>>n;
            minHeap.push_back(n);
            maxHeap.push_back(n);
            {
                int i=minHeap.size()-1;
                int j=maxHeap.size()-1;
                mapMinToMax[i]=j;
                mapMaxToMin[j]=i;

                while(i>0 && minHeap[(i-1)/2]>minHeap[i]){
                    swap(minHeap[(i-1)/2], minHeap[i]);
                    swap(mapMinToMax[(i-1)/2], mapMinToMax[i]);
                    i=(i-1)/2;
                }

                while(j>0 && maxHeap[(j-1)/2]<maxHeap[j]){
                    swap(maxHeap[(j-1)/2], maxHeap[j]);
                    swap(mapMaxToMin[(j-1)/2], mapMaxToMin[j]);
                    j=(j-1)/2;
                }
            }
            cout<<toString(minHeap)<<endl;
            break;
        case 1: // 移除最小值
            if(minHeap.empty()) break;
            {
                int i=0;
                int j=mapMinToMax[i];

                swap(minHeap[i], minHeap[minHeap.size()-1]);
                swap(maxHeap[j], maxHeap[maxHeap.size()-1]);
                cout<<minHeap.back()<<endl;

                minHeap.pop_back();
                maxHeap.pop_back();

                while(i*2+1<minHeap.size()) {
                    if(i*2+2<minHeap.size()) {
                        if(minHeap[i]>min(minHeap[i*2+1],minHeap[i*2+2])) {
                            if(minHeap[i*2+1]>minHeap[i*2+2]) {
                                swap(minHeap[i*2+2],minHeap[i]);
                                i=i*2+2;
                            }
                            else {
                                swap(minHeap[i*2+1],minHeap[i]);
                                i=i*2+1;
                            }
                        }
                        else break;
                    }
                    else {
                        if(minHeap[i]>minHeap[i*2+1]) {
                            swap(minHeap[i],minHeap[i*2+1]);
                            i=i*2+1;
                        }
                        else break;
                    }
                }

                // 對 maxHeap 做相同位置 heapify-down
                i=j;
                while(i*2+1<maxHeap.size()) {
                    if(i*2+2<maxHeap.size()) {
                        if(maxHeap[i]<max(maxHeap[i*2+1],maxHeap[i*2+2])) {
                            if(maxHeap[i*2+1]<maxHeap[i*2+2]) {
                                swap(maxHeap[i*2+2],maxHeap[i]);
                                i=i*2+2;
                            }
                            else {
                                swap(maxHeap[i*2+1],maxHeap[i]);
                                i=i*2+1;
                            }
                        }
                        else break;
                    }
                    else {
                        if(maxHeap[i]<maxHeap[i*2+1]) {
                            swap(maxHeap[i],maxHeap[i*2+1]);
                            i=i*2+1;
                        }
                        else break;
                    }
                }
            }
            cout<<toString(minHeap)<<endl;
            break;
        case 2: // 移除最大值
            if(maxHeap.empty()) break;
            {
                int i=0;
                int j=mapMaxToMin[i];

                swap(maxHeap[i], maxHeap[maxHeap.size()-1]);
                swap(minHeap[j], minHeap[minHeap.size()-1]);
                cout<<maxHeap.back()<<endl;

                maxHeap.pop_back();
                minHeap.pop_back();

                // heapify-down maxHeap
                i=0;
                while(i*2+1<maxHeap.size()) {
                    if(i*2+2<maxHeap.size()) {
                        if(maxHeap[i]<max(maxHeap[i*2+1],maxHeap[i*2+2])) {
                            if(maxHeap[i*2+1]<maxHeap[i*2+2]) {
                                swap(maxHeap[i*2+2],maxHeap[i]);
                                i=i*2+2;
                            }
                            else {
                                swap(maxHeap[i*2+1],maxHeap[i]);
                                i=i*2+1;
                            }
                        }
                        else break;
                    }
                    else {
                        if(maxHeap[i]<maxHeap[i*2+1]) {
                            swap(maxHeap[i],maxHeap[i*2+1]);
                            i=i*2+1;
                        }
                        else break;
                    }
                }

                // heapify-down minHeap
                i=j;
                while(i*2+1<minHeap.size()) {
                    if(i*2+2<minHeap.size()) {
                        if(minHeap[i]>min(minHeap[i*2+1],minHeap[i*2+2])) {
                            if(minHeap[i*2+1]>minHeap[i*2+2]) {
                                swap(minHeap[i*2+2],minHeap[i]);
                                i=i*2+2;
                            }
                            else {
                                swap(minHeap[i*2+1],minHeap[i]);
                                i=i*2+1;
                            }
                        }
                        else break;
                    }
                    else {
                        if(minHeap[i]>minHeap[i*2+1]) {
                            swap(minHeap[i],minHeap[i*2+1]);
                            i=i*2+1;
                        }
                        else break;
                    }
                }
            }
            cout<<toString(minHeap)<<endl;
            break;
        }
    }
}
