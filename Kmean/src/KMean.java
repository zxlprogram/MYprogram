import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import java.io.*;
import java.util.*;
class K_mean {
	protected int StringToNum(String s) {
		return 0;
	}
    public double[][] k_mean(String filePath,int feat,int group) {
    	ArrayList<data>database=new ArrayList<>();
    	double [][]rewrite=new double[10000000][feat];
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int rowIndex = 0; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
            	data d=new data(new double[feat]);
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    for (int colIndex = 0; colIndex < row.getPhysicalNumberOfCells(); colIndex++) {
                    	Cell cell = row.getCell(colIndex);
                        if (cell != null) {
                            switch (cell.getCellType()) {
                               case NUMERIC:
                                    d.feature[colIndex]=cell.getNumericCellValue();
                                    rewrite[rowIndex][colIndex]=cell.getNumericCellValue();
                                    break;
                                case BOOLEAN:
                                	d.feature[colIndex]=cell.getBooleanCellValue()?1:0;
                                    rewrite[rowIndex][colIndex]=cell.getBooleanCellValue()?1:0;
                                    break;
                                default:
                                    d.feature[colIndex]=StringToNum(cell.getStringCellValue());
                                    rewrite[rowIndex][colIndex]=StringToNum(cell.getStringCellValue());
                                    break;
                            }
                        }
                    }
                    database.add(d);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        data[] dataBaseArr=new data[database.size()];
        {//Trans ArrayList to Array
        	int i=0;
        	for(data d:database) {
        		dataBaseArr[i]=d;
        		i++;
        	}
        }
        //K-mean
        Map<data,ArrayList<data>>saveGroup=new HashMap<>();
        data[]midpoint=new data[group];
        for(int i=0;i<group;i++) 
        	midpoint[i]=new data(dataBaseArr[i].feature);
        //loop start from here
        boolean keep=true;
        for(int looptime=0;keep&&looptime<=600000;looptime++) {
        	keep=false;
        for(data d:dataBaseArr) {
    		if(d.closerData==null)
    			d.closerData=midpoint[0];
        	for(int i=0;i<group;i++) 
        		if(d.distance(midpoint[i])<=d.distance(d.closerData)) {
        			d.closerData=midpoint[i];
        		}
        }//set all data closest point
        for(data d:midpoint)
        	saveGroup.put(d,new ArrayList<data>());
        for(data d:dataBaseArr) 
        	saveGroup.get(d.closerData).add(d);
        //refrash the group
        for(data d:midpoint) {
        	double[]total=new double[feat];
        	for(data ele:saveGroup.get(d)) {
        		for(int i=0;i<feat;i++)
        			total[i]+=ele.feature[i];
        	}
        	for(int i=0;i<feat;i++) {
        		total[i]/=(double)feat;
        		if(d.feature[i]!=total[i])
        			keep=true;
        		d.feature[i]=total[i];
        	}
        }
        //refrash the group middle point
        }
        {
        int i=1;
        Map<data,Integer>giveGroupName=new HashMap<>();
        for(data d:midpoint) {
        	giveGroupName.put(d,i);
        	i++;
        }
        i=0;
        for(data d:dataBaseArr) {
        	System.out.println(Arrays.toString(rewrite[i])+" is belong to group:"+giveGroupName.get(d.closerData));
        	i++;
        }
        }
        double [][]arr=new double[group][feat];
        for(int i=0;i<group;i++)
        	arr[i]=midpoint[i].feature;
        return arr;
    }
}
final class data {
	double [] feature;
	protected data closerData;
	public double distance(data d) {
		double dis=0;
		for(int i=0;i<d.feature.length;i++) {
			dis+=(d.feature[i]-this.feature[i])*(d.feature[i]-this.feature[i]);
		}
		return Math.sqrt(dis);
	}
	public data(double []arr) {
		this.feature=arr;
	}
}