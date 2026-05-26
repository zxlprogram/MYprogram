#include <iostream>
#include <vector>
#include<cmath>
using namespace std;
int father(int n) {
    return (n-1)/2;
}
int level(int n) {
    int two=1,c=-1;
    while(two<=n+1) {
        two*=2;
        c++;
    }
    return c;
}
void printv(vector<int>v) {
            cout<<"[";
            for(int i=0;i<v.size();i++)
                cout<<(i?", ":"")<<v[i];
            cout<<"]"<<endl;
}
int main()
{
    vector<int>heap;
    int code,n;
    while(cin>>code) {
        switch(code) {
        case 0: {//push back a node to heap, and swap with father if the father is bigger than current(if current max level) or smaller than current(if current min level), if don't need to swap, consider the grandfather, if the grandfather is bigger/smaller(same logic with father) than current node than swap with it
            cin>>n;
            heap.push_back(n);
            {
            int cur=heap.size()-1;
            if(cur>0&&(bool(level(cur)%2)==(heap[cur]<heap[father(cur)]))) {
                swap(heap[cur],heap[father(cur)]);
                cur=father(cur);
            }
            while(cur>2&&(bool(level(cur)%2)==(heap[cur]>=heap[father(father(cur))]))) {
                swap(heap[cur],heap[father(father(cur))]);
                cur=father(father(cur));
            }
            printv(heap);
            }
            break;
        }
        case 1: {//find all grandchild, and select the maximum(if current max level) or minimum(if current min level), if no grandchild can swap, try to find the child, and select the maximum(if current max level) or minimum(if current min level)
            if (heap.empty()) break;
            swap(heap[0], heap[heap.size() - 1]);
            cout << "minimum: " << heap[heap.size() - 1] << endl;
            heap.pop_back();
            bool cont = true;
            int cur = 0;
            while (cont) {
                int maxminp = cur;
                cont = false;
                for(int i=3;i<=6;i++)//grandchild
                    if(cur*4+i<heap.size()&&((heap[cur*4+i]>heap[maxminp])==(level(cur)%2))) maxminp=cur*4+i;
                if (maxminp!=cur) {
                    swap(heap[maxminp],heap[cur]);
                    if ((heap[maxminp]<heap[father(maxminp)])==(level(cur)%2)) swap(heap[maxminp],heap[father(maxminp)]);
                    cur=maxminp;
                    cont=true;
                }
                else {//child
                    for(int i=1;i<=2;i++)
                        if(cur*2+i<heap.size()&&((heap[cur*2+i]>heap[maxminp])==(level(cur)%2))) maxminp=cur*2+i;
                    if(maxminp != cur) swap(heap[maxminp],heap[cur]);
                }
            }
            printv(heap);
            break;
        }
        case 2: {
            int index;
            if(heap.empty())break;
            else if(heap.size()==1)index=0;
            else if(heap.size()==2)index=1;
            else if(heap[1]>heap[2])index=1;
            else index=2;
            swap(heap[index],heap[heap.size()-1]);
            cout<<"maximum: "<<heap[heap.size()-1]<<endl;
            heap.pop_back();
            while(true){
                int maxp=index,maxq=index;

                for(int i=2*index+1;i<=2*index+2 && i<heap.size();i++)
                    if(heap[i]>heap[maxp])maxp=i;

                for(int i=4*index+3;i<=4*index+6 && i<heap.size();i++)
                    if(heap[i]>heap[maxq])maxq=i;

                if(maxq!=index && heap[maxq]>heap[maxp]){ // grandchild
                    swap(heap[index],heap[maxq]);
                    index=maxq;
                    if(heap[index] < heap[father(index)])
                        swap(heap[index],heap[father(index)]);
                }
                else if(maxp!=index){ // child
                    swap(heap[index],heap[maxp]);
                    break;
                }
                else break;
            }
            printv(heap);
        }
        }
    }
}
