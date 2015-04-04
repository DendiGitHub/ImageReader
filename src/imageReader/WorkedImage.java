package imageReader;

import java.io.File;

public class WorkedImage {
	
	
	private int height;
	private int width;
	private boolean[][] binaryTable;
	 
	public WorkedImage(File imageFile){
		ImageWorker casual = new ImageWorker(imageFile);
		height = casual.getHeight();
		width = casual.getWidth();
//		grayTable = casual.getGrayTable();
		binaryTable = casual.getBinaryTable();
	}
	
	
	
	public WorkedImage(String FileRoute){
		this(new File(FileRoute));
	}


	public int getHeight() {
		return height;
	}


	public void setHeight(int height) {
		this.height = height;
	}


	public int getWidth() {
		return width;
	}


	public void setWidth(int width) {
		this.width = width;
	}


	public boolean[][] getBinaryTable() {
		return binaryTable;
	}


	public void setBinaryTable(boolean[][] binaryTable) {
		this.binaryTable = binaryTable;
	}

}
