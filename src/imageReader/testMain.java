package imageReader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import PDollar.*;

public class testMain {
	public static void main(String[] args) {

		
		 File file = new File("testDemo\\");
		batchTest(file);
		
//		File testFile = new File("testDemo\\XYtest.png");
//		WorkedImage a = new WorkedImage(testFile);
//		boolean[][] result = a.getBinaryTable();
//		for(int i=0;i<a.getWidth();i++){
//			for(int j=0;j<a.getHeight();j++){
//			    System.out.print(" "+ result[i][j] + ",");
//			}
//			System.out.println();
//		}
		
		

		// File testFile = new File("testDemo\\QQ½ØÍ¼20150306152132.jpg");
		// singleTest(testFile);

//		 WorkedImage a = new WorkedImage(new
//		 File("StandardDatabase\\ABS-2.png"));
//		 testTool.getBorder(new File("StandardDatabase\\ABS-2.png"));
//		 testTool.borderOut(new File("StandardDatabase\\ABS-2.png"));
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


		System.out.println("tested "+(passFlag+failFlag)+" demo,"+passFlag+" passed");
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
