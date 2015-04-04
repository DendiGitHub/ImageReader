package imageReader;

import java.io.File;

public class ImageCompare {
	
	
	public static double compare(WorkedImage leftImage, WorkedImage rightImage) {
		double result = 0;
		int height = Math.min(leftImage.getHeight(), rightImage.getHeight());
		int width = Math.min(leftImage.getWidth(), rightImage.getWidth());
		boolean[][] leftBinaryTable = leftImage.getBinaryTable();
		boolean[][] rightBinaryTable = rightImage.getBinaryTable();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (leftBinaryTable[i][j] == rightBinaryTable[i][j]) {
					result = result + 1;
				}
				else{
//					
					
				}
			}
		}
		
		
		result = result / (height * width);

		return result;
	}
	
	public static double[] compare(WorkedImage leftImage) {
		double[] result = null;
		
		String filepath = "StandardDatabase";
		File file = new File(filepath);
		if(file.isDirectory()){
			String[] filelist = file.list();
			result = new double[filelist.length];
            for (int i = 0; i < filelist.length; i++) {
                    File readfile = new File(filepath + "\\" + filelist[i]);
                    result[i] = compare(leftImage,new WorkedImage(readfile));
                    }
            }
		return result;
	}
	
	public static void showResult(double[] result){
		for(int i=1;i<=result.length;i++){
			System.out.println(i+" : "+result[i-1]);
		}
	}
	
}
