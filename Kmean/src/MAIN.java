import java.util.*;
import AIlib.*;
public class MAIN {
	public static void main(String[]args) {
		        double[][] inputs = {
		            {0, 0},
		            {0, 1},
		            {1, 0},
		            {1, 1}
		        };
		        double[][] targets = {
		            {0},
		            {1},
		            {1},
		            {0}
		        };
		        int[] hiddenLayers = {100};
		        ANN ann=new ANN(inputs,hiddenLayers,targets);
		        ann.RunMod(5000,0.999,true);
		        ann.RunMod(50000, 0.999, true);
	}
	public static void KM() {
		@SuppressWarnings("resource")
		Scanner sc=new Scanner(System.in);
		myKmean myk=new myKmean();
		System.out.println("where is your excel file?");
		String path=sc.next();
		System.out.println("and how much is your features?");
		int feat=sc.nextInt();
		System.out.println("tell me how much group do you want");
		int group=sc.nextInt();
		double[][]M=myk.k_mean(path,feat,group);
        for(int i=0;i<M.length;i++) 
        	System.out.println("group "+(i+1)+" : "+Arrays.toString(M[i]));
	}
}
class myKmean extends K_mean {
	@Override
	public int StringToNum(String s) {//這裡實作將字串轉為數字的算法
		return (int)s.charAt(0)-'A';
	}
}