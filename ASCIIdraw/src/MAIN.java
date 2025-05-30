import javax.imageio.*;
import java.awt.image.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
public class MAIN {
	public static ArrayList<ascii>ascArr=new ArrayList<>();
	public static BufferedImage resizeImage(String inputPath, int targetSize) throws Exception {
        BufferedImage originalImage = ImageIO.read(new File(inputPath));
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        double scale = (double) targetSize / Math.min(originalWidth, originalHeight);
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }
	public static char closer(double [][]Arr,int subI,int subJ) {
		double mindis=Double.MAX_VALUE;
		char minchar=' ';
		for(ascii arr:ascArr) {
			double me=0;
			for(int i=0;i<10;i++)
			for(int j=0;j<10&&subI+i<Arr.length&&subJ+j<Arr[0].length;j++)
				me+=(arr.getArr()[i][j]-Arr[subI+i][subJ+j])*(arr.getArr()[i][j]-Arr[subI+i][subJ+j]);
			if(mindis>me) {
				mindis=me;
				minchar=arr.getChar();
			}
		}
		return minchar;
	}
	public static void buildMap() {
    	ascArr.add(ascii.SPACE);
    	ascArr.add(ascii.SHARP);
    	ascArr.add(ascii.AND);
    	ascArr.add(ascii.TIME);
    	ascArr.add(ascii.LEFTSMA);
    	ascArr.add(ascii.RIGHTSMA);
    	ascArr.add(ascii.FLOOR);
    	ascArr.add(ascii.PLUS);
    	ascArr.add(ascii.LEFTBIG);
    	ascArr.add(ascii.RIGHTBIG);
    	ascArr.add(ascii.OR);
    	ascArr.add(ascii.SAY);
    	ascArr.add(ascii.STRING);
    	ascArr.add(ascii.SMALLER);
    	ascArr.add(ascii.BIGGER);
    	ascArr.add(ascii.QUESTION);
    	ascArr.add(ascii.MINUS);
    	ascArr.add(ascii.EQUAL);
    	ascArr.add(ascii.LEFTMID);
    	ascArr.add(ascii.RIGHTMID);
    	ascArr.add(ascii.DOWNFILe);
    	ascArr.add(ascii.ENDMARK);
    	ascArr.add(ascii.UPPERSPOT);
    	ascArr.add(ascii.CONTSPOT);
    	ascArr.add(ascii.SPOT);
    	ascArr.add(ascii.DEVIVE);
	}
    public static double[][] convertArray(String imagePath,int size) throws Exception {
    	buildMap();
        BufferedImage image = resizeImage(imagePath,size*10);
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] arr = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = new Color(image.getRGB(x, y));
                float r = pixel.getRed() / 255.0f;
                float g = pixel.getGreen() / 255.0f;
                float b = pixel.getBlue() / 255.0f;
                float f = 0.299f * r + 0.587f * g + 0.114f * b;
                arr[y][x] = f;
            }
        }
        return arr;
    }
    public static void main(String[] args) throws Exception {
    	Scanner sc=new Scanner(System.in);
    	System.out.println("drag your picture here");
        String path=sc.nextLine();
        if(path.charAt(0)=='"'&&path.charAt(path.length()-1)=='"')
        	path=path.substring(1,path.length()-1);
        System.out.println("input the size");
        int size=sc.nextInt();
        double[][]array=convertArray(path,size);
        try (FileWriter writer=new FileWriter("output.txt")) {
            for(int i=0;i<array.length;i+=10) {
            	for(int j=0;j<array[0].length;j+=10)
            		writer.write(closer(array,i,j));
            	writer.write("\n");
            }
        }
        Desktop.getDesktop().open(new File("output.txt"));
        sc.close();
    }
}
enum ascii{
//#&*()_+{}|:"<>?-=[]\;',./
SHARP(new double[][] {
	{1,1,1,0,1,1,0,1,1,1},
	{1,1,1,0,1,1,0,1,1,1},
	{1,1,1,0,1,1,0,1,1,1},
	{1,0,0,0,0,0,0,0,0,1},
	{1,1,1,0,1,1,0,1,1,1},
	{1,1,1,0,1,1,0,1,1,1},
	{1,0,0,0,0,0,0,0,0,1},
	{1,0,1,0,1,0,0,1,0,1},
	{1,1,1,0,1,1,0,1,1,1},
	{1,1,1,0,1,1,0,1,1,1},
},'#'),
AND(new double[][] {
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,0,1,1,1,0,1,1,1},
	{1,1,0,1,1,1,0,1,1,1},
	{1,1,1,0,1,0,1,1,1,1},
	{1,1,1,1,0,1,1,1,1,1},
	{1,1,1,0,1,0,1,1,0,1},
	{1,1,0,1,1,0,1,0,1,1},
	{1,1,0,1,1,1,0,1,1,1},
	{1,1,1,0,0,0,1,0,1,1},
	{1,1,1,1,1,1,1,1,1,1},
},'&'),
TIME(new double[][] {
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,0,0,0,0,1,1,1},
	{1,1,1,0,0,0,0,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
},'*'),
LEFTSMA(new double[][] {
	{1,1,1,1,1,0,0,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,0,1,1,1,1,1},
	{1,1,1,0,0,1,1,1,1,1},
	{1,1,1,0,1,1,1,1,1,1},
	{1,1,0,0,1,1,1,1,1,1},
	{1,1,1,0,0,1,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
},'('),
RIGHTSMA(new double[][] {
	{1,1,0,0,0,1,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
	{1,1,1,1,1,1,0,1,1,1},
	{1,1,1,1,1,1,0,0,1,1},
	{1,1,1,1,1,1,0,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,0,0,1,1,1,1,1},
	{1,1,0,0,1,1,1,1,1,1},
},')'),
FLOOR(new double[][] {
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{0,0,0,0,0,0,0,0,0,0},
	{0,0,0,0,0,0,0,0,0,0},
},'_'),
PLUS(new double[][] {
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,0,0,0,0,0,0,0,0,1},
	{1,0,0,0,0,0,0,0,0,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
},'+'),
LEFTBIG(new double[][] {
	{1,1,1,1,1,0,0,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,0,0,1,1,1,1,1},
	{1,1,1,0,1,1,1,1,1,1},
	{1,1,0,0,1,1,1,1,1,1},
	{1,1,0,0,1,1,1,1,1,1},
	{1,1,1,0,0,1,1,1,1,1},
	{1,1,1,0,0,1,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
},'{'),
RIGHTBIG(new double[][] {
	{1,1,1,0,0,1,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
	{1,1,1,1,1,1,0,1,1,1},
	{1,1,1,1,1,1,0,0,0,1},
	{1,1,1,1,1,1,0,0,0,1},
	{1,1,1,1,1,1,0,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,0,0,1,1,1,1,1},
},'}'),
OR(new double[][] {
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
},'|'),
SAY(new double[][] {
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,0,0,0,0,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,0,0,0,0,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
},':'),
STRING(new double[][] {
	{1,1,0,0,1,1,0,0,1,1},
	{1,1,0,0,1,1,0,0,1,1},
	{1,1,0,0,1,1,0,0,1,1},
	{1,1,0,0,1,1,0,0,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
},'"'),
SMALLER(new double[][] {
	{1,1,1,1,1,1,0,0,1,1},
	{1,1,1,1,1,0,0,0,1,1},
	{1,1,1,1,0,0,0,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,0,0,0,1,1,1,1,1},
	{1,0,0,0,1,1,1,1,1,1},
	{1,1,0,0,0,1,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,1,0,0,0,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
},'<'),
BIGGER(new double[][] {
	{1,1,1,1,1,1,1,1,1,1},
	{1,0,0,0,1,1,1,1,1,1},
	{1,1,0,0,0,1,1,1,1,1},
	{1,1,1,0,0,0,0,1,1,1},
	{1,1,1,1,1,0,0,0,1,1},
	{1,1,1,1,1,1,0,0,0,1},
	{1,1,1,1,1,0,0,0,1,1},
	{1,1,1,1,0,0,0,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,0,0,0,1,1,1,1,1},
},'>'),
QUESTION(new double[][] {
	{1,1,0,0,0,0,1,1,1,1},
	{1,1,0,0,0,0,0,1,1,1},
	{1,0,0,0,1,1,0,0,1,1},
	{1,0,0,1,1,0,0,0,1,1},
	{1,1,0,1,0,0,0,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,0,0,1,1,1,1,1},
	{1,1,1,1,0,1,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
},'?'),
MINUS(new double[][] {
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,0,0,0,0,0,0,0,0,1},
	{1,0,0,0,0,0,0,0,0,1},
	{1,0,0,0,0,0,0,0,0,1},
	{1,0,0,0,0,0,0,0,0,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
},'-'),
EQUAL(new double[][] {
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,0,0,0,0,0,0,1,1},
	{1,1,0,0,0,0,0,0,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,0,0,0,0,0,0,1,1},
	{1,1,0,0,0,0,0,0,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
},'='),
LEFTMID(new double[][] {
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,0,0,0,0,0,1,1,1},
	{1,1,0,0,0,0,0,1,1,1},
	{1,1,0,0,1,1,1,1,1,1},
	{1,1,0,0,1,1,1,1,1,1},
	{1,1,0,0,1,1,1,1,1,1},
	{1,1,0,0,1,1,1,1,1,1},
	{1,1,0,0,1,1,1,1,1,1},
	{1,1,0,0,0,0,0,1,1,1},
	{1,1,0,0,0,0,0,1,1,1},
},'['),
RIGHTMID(new double[][] {
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,0,0,0,0,0,1,1,1},
	{1,1,0,0,0,0,0,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
	{1,1,0,0,0,0,0,1,1,1},
	{1,1,0,0,0,0,0,1,1,1},
},']'),
DOWNFILe(new double[][]{
	{1,1,0,0,1,1,1,1,1,1},
	{1,1,0,0,1,1,1,1,1,1},
	{1,1,0,0,1,1,1,1,1,1},
	{1,1,0,0,0,1,1,1,1,1},
	{1,1,1,0,0,1,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,1,0,0,0,1,1,1},
	{1,1,1,1,1,0,0,1,1,1},
	{1,1,1,1,1,0,0,0,1,1},
},'\\'),
ENDMARK(new double[][] {
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,0,0,0,1,1,1,1,1},
},';'),
UPPERSPOT(new double[][] {
	{1,1,1,0,0,0,0,1,1,1},
	{1,1,1,0,0,0,0,1,1,1},
	{1,1,1,0,0,0,0,1,1,1},
	{1,1,1,0,0,0,0,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
},(char)'\''),
CONTSPOT(new double[][] {
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,0,1,1,1,1,1},
	{1,1,1,0,0,0,0,1,1,1},
	{1,1,1,1,0,0,0,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,1,0,0,1,1,1,1,1},	
},','),
SPOT(new double[][] {
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,0,0,0,0,0,1,1,1},
	{1,1,0,0,0,0,0,1,1,1},
},'.'),
DEVIVE(new double[][] {
	{1,1,1,1,1,1,1,0,0,1},
	{1,1,1,1,1,1,0,0,0,1},
	{1,1,1,1,1,1,0,0,1,1},
	{1,1,1,1,1,0,0,0,1,1},
	{1,1,1,1,0,0,0,1,1,1},
	{1,1,1,1,0,0,1,1,1,1},
	{1,1,1,0,0,0,1,1,1,1},
	{1,1,0,0,0,1,1,1,1,1},
	{1,0,0,0,1,1,1,1,1,1},
	{1,0,0,1,1,1,1,1,1,1},
},'/'),
SPACE(new double[][]{
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	{1,1,1,1,1,1,1,1,1,1},
	},' ');
	private final double [][]arr;
	private final char c;
	ascii(double [][]arr,char c){
		this.arr=arr;
		this.c=c;
	}
	public char getChar() {
		return c;
	}
	public double[][]getArr() {
		return arr;
	}
}