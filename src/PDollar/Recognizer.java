package PDollar;

import imageReader.WorkedImage;

import java.io.File;
import java.util.ArrayList;

public class Recognizer {
	public static PDollarRecognizer recognizer = new PDollarRecognizer();
	
	public static RecognizerResults getResult(File imageFile){
		WorkedImage a = new WorkedImage(imageFile);
		GraphDrawer drawer = new GraphDrawer(a.getBinaryTable(), a.getWidth(),
				a.getHeight());
		ArrayList<Point> pointList = drawer.getPointList();
		
		
//		PDollarRecognizer apd = new PDollarRecognizer();
		RecognizerResults result = recognizer.Recognize(pointList);
		return result;
	}
}
