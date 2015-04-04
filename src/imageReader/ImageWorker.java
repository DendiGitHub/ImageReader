package imageReader;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/*	Author :Dendi
 * 	Email  :CoderDendi@163.com
 * 	Data   :2015.01.23
 */

public class ImageWorker {

	// to be determined;
	static int STANDARD_HEIGHT = 50;
	static int STANDARD_WIDTH = 50;
	static int BLUE_WEIGH = 29;
	static int GREEN_WEIGH = 77;
	static int RED_WEIGH = 150;
	static int DIVIDER = 20;

	private BufferedImage bufferImage;
	private int height;
	private int width;
	private int[][] grayTable;
	private boolean[][] binaryTable;
	private String fileName;

	public boolean[][] getBinaryTable() {
		return binaryTable;
	}

	public void setBinaryTable(boolean[][] binaryTable) {
		this.binaryTable = binaryTable;
	}

	public ImageWorker(File imageFile) {
		try {
			fileName = imageFile.getName();
			bufferImage = ImageIO.read(imageFile);
			width = bufferImage.getWidth();
			height = bufferImage.getHeight();
			grayTable = initGrayTable();
			binaryTable = initBinaryTable();
//			bufferImage = binary();
//			grayTable = initGrayTable();
//			modifyBinaryTable();
			// getStandard();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void modifyBinaryTable(){
		binaryTable = new boolean[width][height];
		int threshold = getThreshold(grayTable);

		// init
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (grayTable[i][j] > threshold) {
					binaryTable[i][j] = true;
					// grayTable[i][j] |= 0x00FFFF;
				} else {
					binaryTable[i][j] = false;
					// grayTable[i][j] &= 0xFF0000;
				}
			}
		}
	}
	public void getStandard() {
		ImageResizer a = new ImageResizer(grayTable, height, width,
				STANDARD_HEIGHT, STANDARD_WIDTH);
		grayTable = a.getImageResizer();
		height = STANDARD_HEIGHT;
		width = STANDARD_WIDTH;

		binaryTable = new boolean[width][height];
		int threshold = getThreshold(grayTable);

		// init
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (grayTable[i][j] > threshold) {
					binaryTable[i][j] = true;
					// grayTable[i][j] |= 0x00FFFF;
				} else {
					binaryTable[i][j] = false;
					// grayTable[i][j] &= 0xFF0000;
				}
			}
		}

		return;
	}

	public void getBinary() {
		imageOut(binary());
	}

	// 灰度化
	public int[][] initGrayTable() {
		int[][] grayTable = new int[width][height];
		int rgb, r, g, b, grayPixel;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				rgb = bufferImage.getRGB(j, i);
				r = (rgb >> 16) & 0xFF;
				g = (rgb >> 8) & 0xFF;
				b = (rgb >> 0) & 0xFF;
				// toBeDetermined
				grayPixel = (int) ((b * BLUE_WEIGH + g * GREEN_WEIGH
						+ RED_WEIGH * r + 128) >> 8);
				grayTable[j][i] = grayPixel;
			}
		}
		return grayTable;
	}

	private boolean[][] initBinaryTable() {
		boolean[][] binaryTable = new boolean[width][height];
		int threshold = getThreshold(grayTable);
		int heightFlag = 0;
		int heightStart = 0;
		int widthFlag = 0;
		int widthStart = 0;

		// init
		for (int i = widthStart; i < width; i++) {
			for (int j = heightStart; j < height; j++) {
				if (grayTable[i][j] > threshold) {
					binaryTable[i][j] = true;
					// grayTable[i][j] |= 0x00FFFF;
				} else {
					binaryTable[i][j] = false;
					// grayTable[i][j] &= 0xFF0000;
				}
			}
		}

		// left to right scanning
//		boolean flag = true;
//		for (int i = 0; i < width / 2 && flag; i++) {
//			int signal = 0;
//			for (int j = 0; j < height; j++) {
//				if (binaryTable[i][j] == true) {
//					signal++;
//				}
//			}
//			if (signal <= width / DIVIDER) {
//				widthFlag++;
//				widthStart++;
//			} else {
//				flag = false;
//			}
//		}
		// right to left scanning
//		flag = true;
//		for (int i = width - 1; i > width / 2 && flag; i--) {
//			int signal = 0;
//			for (int j = 0; j < height - heightFlag; j++) {
//				if (binaryTable[i][j] == true) {
//					signal++;
//				}
//			}
//			if (signal <= width / DIVIDER) {
//				widthFlag++;
//			} else {
//				flag = false;
//			}
//		}
		// top to bottom scanning
//		flag = true;
//		for (int i = 0; i < height / 2 && flag; i++) {
//			int signal = 0;
//			for (int j = 0; j < width; j++) {
//				if (binaryTable[j][i] == true) {
//					signal++;
//				}
//			}
//			if (signal <= height / DIVIDER) {
//				heightFlag++;
//				heightStart++;
//			} else {
//				flag = false;
//			}
//		}
		// bottom to top scanning
//		flag = true;
//		for (int i = height - 1; i > height / 2 && flag; i--) {
//			int signal = 0;
//			for (int j = 0; j < width - widthFlag; j++) {
//				if (binaryTable[j][i] == true) {
//					signal++;
//				}
//			}
//			if (signal <= height / DIVIDER) {
//				heightFlag++;
//			} else {
//				flag = false;
//			}
//		}
		// new INIT
		boolean[][] result = new boolean[width - widthFlag][height - heightFlag];
		for (int i = 0; i < width - widthFlag; i++) {
			for (int j = 0; j < height - heightFlag; j++) {
				result[i][j] = binaryTable[i + widthStart][j + heightStart];
			}
		}
		height = height - heightFlag;
		width = width - widthFlag;
		return result;
	}

	// getThreshold
	private int getThreshold(int[][] grayTable) {
		int threshold = 0;

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				threshold = threshold + grayTable[i][j];
			}
		}
		threshold = threshold / (width * height);
		threshold *= 1.15;
		//正态分布 

		return threshold;
	}

	// 二值化
	public BufferedImage binary() {
		BufferedImage binaryImage = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_BINARY);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (binaryTable[j][i]) {
					binaryImage.setRGB(j, i, Color.white.getRGB());
				} else {
					binaryImage.setRGB(j, i, Color.BLACK.getRGB());
				}
			}
		}
//		Image result = binaryImage.getScaledInstance(STANDARD_WIDTH,
//				STANDARD_HEIGHT, Image.SCALE_REPLICATE);
//
//		BufferedImage bi = new BufferedImage(STANDARD_WIDTH, STANDARD_HEIGHT,
//				BufferedImage.TYPE_INT_RGB);
//		Graphics2D biContext = bi.createGraphics();
//		biContext.drawImage(result, 0, 0, null);
//		height = STANDARD_HEIGHT;
//		width = STANDARD_WIDTH;
//		return bi;
		 return binaryImage;
	}

	// 二值化
		public BufferedImage binary(boolean[][] binaryTable,int width,int height) {
			BufferedImage binaryImage = new BufferedImage(width, height,
					BufferedImage.TYPE_BYTE_BINARY);
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if (binaryTable[j][i]) {
						binaryImage.setRGB(j, i, Color.white.getRGB());
					} else {
						binaryImage.setRGB(j, i, Color.BLACK.getRGB());
					}
				}
			}
//			Image result = binaryImage.getScaledInstance(STANDARD_WIDTH,
//					STANDARD_HEIGHT, Image.SCALE_REPLICATE);
	//
//			BufferedImage bi = new BufferedImage(STANDARD_WIDTH, STANDARD_HEIGHT,
//					BufferedImage.TYPE_INT_RGB);
//			Graphics2D biContext = bi.createGraphics();
//			biContext.drawImage(result, 0, 0, null);
//			height = STANDARD_HEIGHT;
//			width = STANDARD_WIDTH;
//			return bi;
			 return binaryImage;
		}
		
	public void imageOut(BufferedImage binaryImage) {
		try {
			String dir = "BinaryImage//" + fileName;
			ImageIO.write(binaryImage, "jpg", new File(dir));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
	
	public void imageOut(BufferedImage binaryImage,String route) {
		try {
			String dir = route+"//" + fileName;
			ImageIO.write(binaryImage, "jpg", new File(dir));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	// public BufferedImage standard(BufferedImage primitiveImage) {
	// BufferedImage result = new BufferedImage(STANDARD_WIDTH,
	// STANDARD_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
	// int width = primitiveImage.getWidth();
	// int height = primitiveImage.getHeight();
	//
	// int currentRGB;
	// for (int i = 0; i < STANDARD_WIDTH; i++) {
	// for (int j = 0; j < STANDARD_HEIGHT; j++) {
	// currentRGB = getRGB(bufferImage, width, height, i, j);
	// result.setRGB(i, j, currentRGB);
	// }
	// }
	// return result;
	// }
	//
	// public int getRGB(BufferedImage bufferImage, int width, int height, int
	// x,
	// int y) {
	// // double a = width*x/STANDARD_WIDTH;
	// // double b = height*y/STANDARD_HEIGHT;
	// // int x_ = (int) (a / 1);
	// // int y_ = (int) (b/1);
	// // double dx = a - x_;
	// // double dy = b-y_;
	// // System.out.println("x = "+ x_ + "----y = "+y);
	// // System.out.println("dx = " +dx+"_____dy = "+dy);
	// // //to be determined
	// // double rgb = ((2-dx-dy)*bufferImage.getRGB(x_, y_) +
	// // (1+dy-dx)*bufferImage.getRGB(x_, y_+1)
	// // +(1+dx-dy)*bufferImage.getRGB(x_+1,
	// // y_)+(dx+dy)*bufferImage.getRGB(x_+1, y_+1));
	// // rgb = rgb / 4;
	// int rgb = bufferImage.getRGB(width * x / STANDARD_WIDTH, y * height
	// / STANDARD_HEIGHT);
	// return (int) rgb;
	// }

	public BufferedImage getBufferImage() {
		return bufferImage;
	}

	public void setBufferImage(BufferedImage bufferImage) {
		this.bufferImage = bufferImage;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void setGrayTable(int[][] grayTable) {
		this.grayTable = grayTable;
	}

	public int[][] getGrayTable() {
		return grayTable;
	}
}
