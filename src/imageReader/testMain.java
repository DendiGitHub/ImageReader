package imageReader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.imageio.ImageIO;

import PDollar.*;

public class testMain {
	public static void main(String[] args) {

		long a = System.currentTimeMillis();
		File file = new File("testDemo");
//		File file = new File("testDemo//engine");
//		file = new File("StandardDatabase//airBag.png");
		batchTest(file);
		
		
//		System.out.println("Time : " + (System.currentTimeMillis() - a)
//				+ "ms");
		
		// File testFile = new File("testDemo\\XYtest.png");
		// WorkedImage a = new WorkedImage(testFile);
		// boolean[][] result = a.getBinaryTable();
		// for(int i=0;i<a.getWidth();i++){
		// for(int j=0;j<a.getHeight();j++){
		// System.out.print(" "+ result[i][j] + ",");
		// }
		// System.out.println();
		// }

		// File testFile = new File("testDemo\\QQ½ØÍ¼20150306152132.jpg");
		// singleTest(testFile);

		// WorkedImage a = new WorkedImage(new
		// File("StandardDatabase\\ABS-2.png"));
		// testTool.getBorder(new File("StandardDatabase\\ABS-2.png"));
		// testTool.borderOut(new File("StandardDatabase\\ABS-2.png"));
	}

	public static void singleRotate(File testFile, int angle) {
		BufferedImage src;
		try {
			src = ImageIO.read(testFile);
			BufferedImage des = ImageRotate.Rotate(src, angle);
			ImageIO.write(des, "jpg",
					new File("rotateDemo\\" + testFile.getName()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void batchRotate(File file, int angle) {
		ArrayList<File> result = new ArrayList<File>();
		result = getFiles(file, result);

		for (int i = 0; i < result.size(); i++) {
			singleRotate(result.get(i), angle);
		}

	}

	public static void singleTest(File testFile) {
		testTool.borderOut(testFile);
		testTool.binaryOut(testFile);
		RecognizerResults result = Recognizer.getResult(testFile);

		System.out.println("============================");
		System.out.println(result.mName);
		System.out.println(result.mScore);
	}

	public static boolean sinTest(File testFile) {
		testTool.borderOut(testFile);
		testTool.binaryOut(testFile);
		RecognizerResults result = Recognizer.getResult(testFile);

		if (result.mName.startsWith(testFile.getName().split("[-,.]")[0])) {
			return true;
		}
		System.out.println("\t" + testFile.getName() + "\t"
				+ testFile.getName().split("-")[0] + "\t" + result.mName);
		return false;
	}

	public static boolean sinTest(String fileString) {
		File testFile = new File(fileString);
		return sinTest(testFile);
	}

	public static void batchTest(File file) {
		int passFlag = 0;
		int failFlag = 0;
		ArrayList<File> result = new ArrayList<File>();
		result = getFiles(file, result);

		for (int i = 0; i < result.size(); i++) {
			if (sinTest(result.get(i))) {
				passFlag++;
			} else {
				failFlag++;
			}
		}

		System.out.println("tested " + (passFlag + failFlag) + " demo,"
				+ passFlag + " passed");
		System.out.println("pass rate:" + ((double) passFlag)
				/ (passFlag + failFlag));
	}

	public static ArrayList<File> getFiles(File root, ArrayList<File> result) {
		if (root.isDirectory()) {
			File[] files = root.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					result = getFiles(file, result);
				} else {
					result.add(file);
				}
			}
		} else {
			result.add(root);
		}
		return result;
	}
}
