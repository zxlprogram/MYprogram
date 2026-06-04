#include <iostream>
#include <stack>
using namespace std;
int full(int*arr,int ptr) {
    if(arr[ptr]!=3)
        return false;
    for(int i=1;i<5;i++)
        if(arr[ptr*4+i]<1||arr[ptr*4+i]>3)
            return false;
    return true;
}
//1=e,2=f,3=p
//every movement promise the final pointer will point to correct space, so just write before move the pointer
void format(int*arr,string str){
    stack<int>stk;
    int ptr=0;
    for(char c:str) {
        switch(c) {
        case 'p':
            arr[ptr]=3;
            stk.push(ptr);
            ptr=ptr*4+1;//children index in the quad tree array
            break;
        case 'e':
            arr[ptr]=1;
            if(full(arr,(ptr-1)/4)) {//if father have exactly 4 child
                while(stk.size()&&full(arr,(ptr-1)/4)){
                    ptr=stk.top();
                    stk.pop();
                }
                ptr++;//the pointer will point to the ancestor that which is not full, pointer should point to it's child because we are going to keep filling it's child
                for(int i=0;i<4;i++)//find with the void child
                    if(arr[ptr])
                        ptr++;
            }
            else
                ptr++;
            break;
        case 'f'://same as 'e'
            arr[ptr]=2;
            if(full(arr,(ptr-1)/4)) {
                while(stk.size()&&full(arr,(ptr-1)/4)){
                    ptr=stk.top();
                    stk.pop();
                }
                ptr++;
                for(int i=0;i<4;i++)
                    if(arr[ptr])
                        ptr++;
            }
            else
                ptr++;
            break;
        }
    }
}
int mergeDFS(int* a,int* b){//the DFS logic is similar with format function
    int *ret=(int*)calloc(100000,sizeof(int));//the merged tree
    stack<int>stk;
    int ptr=0,total=0;
    do {
        if(a[ptr]==2||b[ptr]==2) {//the black always covered everything
            ret[ptr]=2;
            total+=1024/(1<<2*stk.size());//the stack size equals to pointed space's deep, and 1<<n can easily calculate for power of 2
            if(full(ret,(ptr-1)/4)) {
                while(stk.size()&&full(ret,(ptr-1)/4)){
                    ptr=stk.top();
                    stk.pop();
                }
                ptr++;
                for(int i=0;i<4;i++)
                    if(ret[ptr])
                        ptr++;
            }
            else
                ptr++;
        }
        else if(a[ptr]==3||b[ptr]==3) {//except the black node, the parent node must calculated at first because it possible have black pixels, it is possibly covered white and void node
            stk.push(ptr);
            ret[ptr]=3;
            ptr=ptr*4+1;
        }
        else if(a[ptr]==1||b[ptr]==1) {//this part also similar with black node logic
            ret[ptr]=1;
            if(full(ret,(ptr-1)/4)) {
                while(stk.size()&&full(ret,(ptr-1)/4)){
                    ptr=stk.top();
                    stk.pop();
                }
                ptr++;
                for(int i=0;i<4;i++)
                    if(ret[ptr])
                        ptr++;
            }
            else
                ptr++;
        }
    }while(stk.size()>0);//at the first iteration, the stack size is 0, if the root is p-node, the stack size will became 1 and the loop keep going
    return total;
}
int main() {
    int T;cin>>T;
    while(T--) {
        string a,b;cin>>a>>b;
        int *TreeA=(int*)calloc(100000,sizeof(int));
        int *TreeB=(int*)calloc(100000,sizeof(int));
        //but the online judge just gave me accept
        format(TreeA,a);
        format(TreeB,b);
        cout<<"There are "<<mergeDFS(TreeA,TreeB)<<" black pixels."<<endl;
    }
}
