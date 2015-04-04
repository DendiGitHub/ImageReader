package imageReader;

import java.io.File;
import java.util.ArrayList;

import PDollar.GraphDrawer;
import PDollar.ListSimplify;
import PDollar.Point;

public class testTool {
	public static void getBorder(ImageWorker a){
//		ImageWorker a = new ImageWorker(new File(
//				"StandardDatabase\\database16.png"));
		GraphDrawer drawer = new GraphDrawer(a.getBinaryTable(), a.getWidth(),
				a.getHeight());
		ArrayList<Point> pointList = drawer.getPointList();
		 pointList = ListSimplify.simplify(pointList);
		 for(int i=0;i<pointList.size();i++){
		 System.out.print("new Point("+pointList.get(i).X+","+pointList.get(i).Y+","
		 +pointList.get(i).ID+"),");
		 }
	}
	
	public static void getBorder(WorkedImage a){
//		ImageWorker a = new ImageWorker(new File(
//				"StandardDatabase\\database16.png"));
		GraphDrawer drawer = new GraphDrawer(a.getBinaryTable(), a.getWidth(),
				a.getHeight());
		ArrayList<Point> pointList = drawer.getPointList();
		 pointList = ListSimplify.simplify(pointList);
		 for(int i=0;i<pointList.size();i++){
		 System.out.print("new Point("+pointList.get(i).X+","+pointList.get(i).Y+","
		 +pointList.get(i).ID+"),");
		 }
	}
	
	public static void getBorder(File file){
		WorkedImage a = new WorkedImage(file);
		getBorder(a);
	}
	
	public static void borderOut(File file){
		ImageWorker a = new ImageWorker(file);
		GraphDrawer drawer = new GraphDrawer(a.getBinaryTable(), a.getWidth(),
				a.getHeight());

		a.imageOut(a.binary(drawer.getBorder(), a.getWidth(), a.getHeight()),"BorderImage");
	}
	
	
	public static void binaryOut(File file){
		ImageWorker a = new ImageWorker(file);
		a.imageOut(a.binary());
	}
}
