package PDollar;

import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Double;

//
// PDollarRecognizer class constants
//
public class PDollarRecognizer {

	static int mNumPoints = 80;
	static Point mPointOrig = new Point(0.0, 0.0, 0);
	static ArrayList<PointCloud> mPntClouds = new ArrayList<PointCloud>();

	public PDollarRecognizer() {
		initializePointCloudTable();
	}

	public RecognizerResults Recognize(ArrayList<Point> points) {
		PointCloud foundPointCloud = null;
		points = Resample(points, mNumPoints);
		points = Scale(points);
		points = TranslateTo(points, mPointOrig);

		double score = Double.POSITIVE_INFINITY;
		for (int i = 0; i < mPntClouds.size(); i++) // for each point-cloud
													// template
		{
			double distScore = GreedyCloudMatch(points, mPntClouds.get(i));
			
//			System.out.println(mPntClouds.get(i).mName);
//			System.out.println(distScore);
//			System.out.println("===========================");
			
			if (distScore < score) {
				score = distScore; // best (least) distance
				foundPointCloud = mPntClouds.get(i); // point-cloud
			}
		}
//		return (foundPointCloud == null) ? new RecognizerResults("No match.",
//				0.0) : new RecognizerResults(foundPointCloud.mName, Math.max(
//				(score - 2.0) / -2.0, 0.0), String.format("score %f\n", score));
		return (foundPointCloud == null) ? new RecognizerResults("No match.",
				0.0) : new RecognizerResults(foundPointCloud.mName,
				score, String.format("score %f\n", score));
	}

	public int addGesture(String name, ArrayList<Point> points) {
		mPntClouds.add(new PointCloud(name, points, mNumPoints));
		int num = 0;
		for (int i = 0; i < mPntClouds.size(); i++) {
			if (mPntClouds.get(i).mName.equals(name))
				num++;
		}
		return num;
	}

	private static double GreedyCloudMatch(ArrayList<Point> points,
			PointCloud pntCloud) {
		double e = 0.50;
		double step = Math.floor(Math.pow(points.size(), 1 - e));

		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < points.size(); i += step) {
			double d1 = CloudDistance(points, pntCloud.mPoints, i);
			double d2 = CloudDistance(pntCloud.mPoints, points, i);
			min = Math.min(min, Math.min(d1, d2)); // min3
		}
		return min;
	}

	private static double CloudDistance(ArrayList<Point> pts1,
			ArrayList<Point> pts2, int start) {
		// pts1.size() == pts2.size()
		boolean[] matched = new boolean[pts1.size()];
		for (int k = 0; k < pts1.size(); k++)
			matched[k] = false;
		double sum = 0;
		int i = start;
		do {
			int index = -1;
			double min = Double.POSITIVE_INFINITY;
			for (int j = 0; j < matched.length; j++) {
				if (!matched[j]) {
					double d = EuclideanDistance(pts1.get(i), pts2.get(j));
					if (d < min) {
						min = d;
						index = j;
					}
				}
			}
			matched[index] = true;
//			double weight = 1 - ((i - start + pts1.size()) % pts1.size())
//					/ pts1.size();
			double weight = 1+4*(i - start + pts1.size()) % pts1.size()
					/ pts1.size();
//			sum += weight * min;
			sum += weight * getModifiedLength(min);
			i = (i + 1) % pts1.size();
		} while (i != start);
		return sum;
	}
	
	public static double getModifiedLength(double x){
		return  2*x*x+x;
	}

	private static ArrayList<Point> Resample(ArrayList<Point> points, int n) {
		double I = PathLength(points) / (n - 1); // interval length
		double D = 0.0;

		ArrayList<Point> newpoints = new ArrayList<Point>();
		newpoints.add(points.get(0));

		for (int i = 1; i < points.size(); i++) {
			if (points.get(i).ID == points.get(i - 1).ID) {
				double d = EuclideanDistance(points.get(i - 1), points.get(i));
				if ((D + d) >= I) {
					double qx = points.get(i - 1).X + ((I - D) / d)
							* (points.get(i).X - points.get(i - 1).X);
					double qy = points.get(i - 1).Y + ((I - D) / d)
							* (points.get(i).Y - points.get(i - 1).Y);
					Point q = new Point(qx, qy, points.get(i).ID);
					newpoints.add(q); // append new point 'q'
					points.add(i, q); // insert 'q' at position i in points s.t.
										// 'q' will be the next i
					D = 0.0;
				} else {
					D += d;
				}
			}else{
				//add
//				points.add(points.get(0));
//				D = 0.0;
			}
		}

		// sometimes we fall a rounding-error short of
		// adding the last point, so add it if so
		if (newpoints.size() == n - 1)
			newpoints
					.add(new Point(points.get(points.size() - 1).X, points
							.get(points.size() - 1).Y,
							points.get(points.size() - 1).ID));
		return newpoints;
	}

	private static ArrayList<Point> Scale(ArrayList<Point> points) {
		double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < points.size(); i++) {
			minX = Math.min(minX, points.get(i).X);
			minY = Math.min(minY, points.get(i).Y);
			maxX = Math.max(maxX, points.get(i).X);
			maxY = Math.max(maxY, points.get(i).Y);
		}

		double size = Math.max(maxX - minX, maxY - minY);
		ArrayList<Point> newpoints = new ArrayList<Point>();

		for (int i = 0; i < points.size(); i++) {
			double qx = (points.get(i).X - minX) / size;
			double qy = (points.get(i).Y - minY) / size;
			newpoints.add(new Point(qx, qy, points.get(i).ID));
		}
		
//		for(int i=0;i<newpoints.size();i++){
//			System.out.println(newpoints.get(i).X+","+newpoints.get(i).Y);
//		}
		
		return newpoints;
	}

	private static ArrayList<Point> TranslateTo(ArrayList<Point> points,
			Point pt) // translates points' centroid
	{
		Point c = Centroid(points);
		ArrayList<Point> newpoints = new ArrayList<Point>();
		for (int i = 0; i < points.size(); i++) {
			double qx = points.get(i).X + pt.X - c.X;
			double qy = points.get(i).Y + pt.Y - c.Y;
			newpoints.add(new Point(qx, qy, points.get(i).ID));
		}
		return newpoints;
	}

	private static Point Centroid(ArrayList<Point> points) {
		double x = 0.0;
		double y = 0.0;
		for (int i = 0; i < points.size(); i++) {
			x += points.get(i).X;
			y += points.get(i).Y;
		}
		x /= points.size();
		y /= points.size();
		return new Point(x, y, 0);
	}

	// average distance between corresponding points in two paths
//	private static double PathDistance(ArrayList<Point> pts1,
//			ArrayList<Point> pts2) {
//		double d = 0.0;
//		for (int i = 0; i < pts1.size(); i++)
//			// assumes pts1.size() == pts2.size()
//			d += EuclideanDistance(pts1.get(i), pts2.get(i));
//		return d / pts1.size();
//	}

	// length traversed by a point path
	private static double PathLength(ArrayList<Point> points) {
		double d = 0.0;
		for (int i = 1; i < points.size(); i++) {
			if (points.get(i).ID == points.get(i - 1).ID)
				d += EuclideanDistance(points.get(i - 1), points.get(i));
		}
		return d;
	}

	// Euclidean distance between two points
	private static double EuclideanDistance(Point p1, Point p2) {
		double dx = p2.X - p1.X;
		double dy = p2.Y - p1.Y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	//
	// PointCloud class: a point-cloud template
	//
	public class PointCloud {
		public ArrayList<Point> mPoints;
		public String mName;

		PointCloud(String name, ArrayList<Point> points, int numPoints) {

			mName = name;
			mPoints = points;

			mPoints = PDollarRecognizer.Resample(mPoints, numPoints);
			mPoints = PDollarRecognizer.Scale(mPoints);
			mPoints = PDollarRecognizer.TranslateTo(mPoints, mPointOrig);
		}
	}

	private void initializePointCloudTable() {

//		mPntClouds.add(new PointCloud("handbreak", new ArrayList<Point>(Arrays
//				.asList(new Point(0.0,8.0,1),new Point(5.0,23.0,1),new Point(4.0,22.0,1),new Point(4.0,20.0,1),new Point(5.0,19.0,1),new Point(6.0,20.0,1),new Point(6.0,21.0,1),new Point(7.0,22.0,1),new Point(7.0,23.0,1),new Point(6.0,24.0,1),new Point(6.0,25.0,1),new Point(7.0,26.0,1),new Point(7.0,29.0,1),new Point(5.0,29.0,1),new Point(3.0,27.0,1),new Point(3.0,26.0,1),new Point(2.0,25.0,1),new Point(2.0,24.0,1),new Point(1.0,23.0,1),new Point(1.0,21.0,1),new Point(0.0,20.0,1),new Point(0.0,9.0,1),new Point(1.0,7.0,1),new Point(1.0,6.0,1),new Point(2.0,5.0,1),new Point(2.0,4.0,1),new Point(3.0,3.0,1),new Point(3.0,2.0,1),new Point(4.0,1.0,1),new Point(3.0,10.0,2),new Point(3.0,11.0,2),new Point(4.0,9.0,2),new Point(4.0,7.0,2),new Point(5.0,6.0,2),new Point(5.0,5.0,2),new Point(7.0,3.0,2),new Point(7.0,1.0,2),new Point(5.0,0.0,3),new Point(6.0,0.0,3),new Point(5.0,11.0,4),new Point(5.0,12.0,4),new Point(6.0,10.0,4),new Point(6.0,8.0,4),new Point(7.0,7.0,4),new Point(7.0,6.0,4),new Point(10.0,3.0,4),new Point(11.0,3.0,4),new Point(13.0,1.0,4),new Point(14.0,1.0,4),new Point(8.0,12.0,5),new Point(9.0,11.0,5),new Point(9.0,10.0,5),new Point(10.0,9.0,5),new Point(10.0,8.0,5),new Point(14.0,4.0,5),new Point(16.0,4.0,5),new Point(17.0,3.0,5),new Point(23.0,3.0,5),new Point(24.0,4.0,5),new Point(25.0,4.0,5),new Point(26.0,5.0,5),new Point(27.0,5.0,5),new Point(30.0,8.0,5),new Point(30.0,9.0,5),new Point(31.0,10.0,5),new Point(31.0,11.0,5),new Point(32.0,12.0,5),new Point(32.0,18.0,5),new Point(31.0,19.0,5),new Point(31.0,20.0,5),new Point(30.0,21.0,5),new Point(30.0,22.0,5),new Point(28.0,24.0,5),new Point(27.0,24.0,5),new Point(25.0,26.0,5),new Point(23.0,26.0,5),new Point(22.0,27.0,5),new Point(18.0,27.0,5),new Point(17.0,26.0,5),new Point(15.0,26.0,5),new Point(14.0,25.0,5),new Point(13.0,25.0,5),new Point(11.0,23.0,5),new Point(11.0,22.0,5),new Point(9.0,20.0,5),new Point(9.0,19.0,5),new Point(8.0,18.0,5),new Point(8.0,13.0,5),new Point(8.0,24.0,6),new Point(33.0,7.0,6),new Point(32.0,6.0,6),new Point(32.0,5.0,6),new Point(33.0,4.0,6),new Point(35.0,6.0,6),new Point(35.0,7.0,6),new Point(34.0,8.0,6),new Point(34.0,10.0,6),new Point(35.0,11.0,6),new Point(35.0,19.0,6),new Point(34.0,20.0,6),new Point(34.0,22.0,6),new Point(27.0,29.0,6),new Point(25.0,29.0,6),new Point(24.0,30.0,6),new Point(17.0,30.0,6),new Point(16.0,29.0,6),new Point(13.0,29.0,6),new Point(9.0,25.0,6),new Point(15.0,0.0,7),new Point(31.0,4.0,7),new Point(29.0,2.0,7),new Point(28.0,2.0,7),new Point(27.0,1.0,7),new Point(26.0,1.0,7),new Point(25.0,0.0,7),new Point(16.0,0.0,7),new Point(17.0,22.0,8),new Point(18.0,21.0,8),new Point(21.0,21.0,8),new Point(22.0,22.0,8),new Point(22.0,23.0,8),new Point(21.0,24.0,8),new Point(20.0,24.0,8),new Point(19.0,25.0,8),new Point(17.0,23.0,8),new Point(18.0,5.0,9),new Point(21.0,5.0,9),new Point(21.0,18.0,9),new Point(18.0,18.0,9),new Point(18.0,6.0,9),new Point(32.0,1.0,10),new Point(33.0,3.0,10),new Point(32.0,2.0,10),new Point(33.0,1.0,10),new Point(32.0,25.0,11),new Point(36.0,8.0,11),new Point(36.0,9.0,11),new Point(37.0,10.0,11),new Point(37.0,14.0,11),new Point(38.0,15.0,11),new Point(37.0,16.0,11),new Point(37.0,19.0,11),new Point(36.0,20.0,11),new Point(36.0,22.0,11),new Point(35.0,23.0,11),new Point(35.0,24.0,11),new Point(33.0,26.0,11),new Point(32.0,27.0,12),new Point(35.0,1.0,12),new Point(37.0,3.0,12),new Point(37.0,4.0,12),new Point(39.0,6.0,12),new Point(39.0,7.0,12),new Point(40.0,8.0,12),new Point(40.0,12.0,12),new Point(41.0,13.0,12),new Point(41.0,15.0,12),new Point(40.0,16.0,12),new Point(40.0,21.0,12),new Point(39.0,22.0,12),new Point(39.0,23.0,12),new Point(38.0,24.0,12),new Point(38.0,25.0,12),new Point(36.0,27.0,12),new Point(36.0,28.0,12),new Point(35.0,29.0,12),new Point(32.0,29.0,12),new Point(32.0,28.0,12),new Point(34.0,0.0,13))), mNumPoints));

		mPntClouds.add(new PointCloud("battery", new ArrayList<Point>(Arrays
				.asList(new Point(0.0,2.0,1),new Point(35.0,1.0,1),new Point(36.0,2.0,1),new Point(39.0,2.0,1),new Point(39.0,26.0,1),new Point(33.0,26.0,1),new Point(32.0,25.0,1),new Point(31.0,26.0,1),new Point(30.0,25.0,1),new Point(29.0,26.0,1),new Point(28.0,26.0,1),new Point(27.0,25.0,1),new Point(26.0,26.0,1),new Point(25.0,25.0,1),new Point(24.0,25.0,1),new Point(23.0,26.0,1),new Point(22.0,25.0,1),new Point(21.0,26.0,1),new Point(20.0,25.0,1),new Point(19.0,26.0,1),new Point(18.0,25.0,1),new Point(7.0,25.0,1),new Point(6.0,26.0,1),new Point(5.0,25.0,1),new Point(2.0,25.0,1),new Point(1.0,26.0,1),new Point(0.0,25.0,1),new Point(0.0,3.0,1),new Point(1.0,2.0,1),new Point(6.0,2.0,1),new Point(7.0,1.0,1),new Point(3.0,7.0,2),new Point(33.0,11.0,2),new Point(32.0,12.0,2),new Point(30.0,10.0,2),new Point(29.0,10.0,2),new Point(32.0,7.0,2),new Point(32.0,8.0,2),new Point(34.0,10.0,2),new Point(34.0,9.0,2),new Point(36.0,11.0,2),new Point(36.0,21.0,2),new Point(35.0,22.0,2),new Point(4.0,22.0,2),new Point(3.0,21.0,2),new Point(3.0,8.0,2),new Point(4.0,6.0,2),new Point(5.0,5.0,2),new Point(35.0,5.0,2),new Point(36.0,6.0,2),new Point(36.0,9.0,2),new Point(6.0,9.0,3),new Point(10.0,9.0,3),new Point(11.0,10.0,3),new Point(6.0,10.0,3),new Point(7.0,0.0,4),new Point(31.0,1.0,4),new Point(30.0,2.0,4),new Point(12.0,2.0,4),new Point(11.0,1.0,4),new Point(11.0,0.0,4),new Point(8.0,0.0,4),new Point(11.0,9.0,5),new Point(31.0,0.0,6),new Point(32.0,0.0,6),new Point(31.0,12.0,7))), mNumPoints));
		mPntClouds.add(new PointCloud("breakDisks", new ArrayList<Point>(Arrays
				.asList(new Point(0.0,13.0,1),new Point(2.0,11.0,1),new Point(4.0,13.0,1),new Point(4.0,17.0,1),new Point(3.0,18.0,1),new Point(4.0,19.0,1),new Point(4.0,20.0,1),new Point(3.0,21.0,1),new Point(3.0,22.0,1),new Point(2.0,23.0,1),new Point(0.0,21.0,1),new Point(0.0,14.0,1),new Point(1.0,9.0,2),new Point(9.0,1.0,2),new Point(9.0,3.0,2),new Point(7.0,5.0,2),new Point(7.0,6.0,2),new Point(5.0,8.0,2),new Point(5.0,9.0,2),new Point(3.0,11.0,2),new Point(2.0,10.0,2),new Point(2.0,6.0,2),new Point(4.0,4.0,2),new Point(4.0,3.0,2),new Point(6.0,1.0,2),new Point(1.0,24.0,3),new Point(4.0,23.0,3),new Point(5.0,24.0,3),new Point(5.0,25.0,3),new Point(6.0,26.0,3),new Point(6.0,27.0,3),new Point(7.0,28.0,3),new Point(7.0,29.0,3),new Point(9.0,31.0,3),new Point(9.0,32.0,3),new Point(7.0,34.0,3),new Point(4.0,31.0,3),new Point(4.0,30.0,3),new Point(3.0,29.0,3),new Point(3.0,28.0,3),new Point(2.0,27.0,3),new Point(2.0,26.0,3),new Point(1.0,25.0,3),new Point(6.0,14.0,4),new Point(26.0,1.0,4),new Point(28.0,1.0,4),new Point(29.0,2.0,4),new Point(30.0,2.0,4),new Point(36.0,8.0,4),new Point(36.0,9.0,4),new Point(37.0,10.0,4),new Point(37.0,11.0,4),new Point(38.0,12.0,4),new Point(38.0,22.0,4),new Point(37.0,23.0,4),new Point(37.0,25.0,4),new Point(35.0,27.0,4),new Point(35.0,28.0,4),new Point(32.0,31.0,4),new Point(31.0,31.0,4),new Point(29.0,33.0,4),new Point(27.0,33.0,4),new Point(26.0,34.0,4),new Point(18.0,34.0,4),new Point(17.0,33.0,4),new Point(15.0,33.0,4),new Point(13.0,31.0,4),new Point(12.0,31.0,4),new Point(9.0,28.0,4),new Point(9.0,27.0,4),new Point(8.0,26.0,4),new Point(8.0,25.0,4),new Point(7.0,24.0,4),new Point(7.0,22.0,4),new Point(6.0,21.0,4),new Point(6.0,15.0,4),new Point(7.0,13.0,4),new Point(7.0,11.0,4),new Point(8.0,10.0,4),new Point(8.0,9.0,4),new Point(9.0,8.0,4),new Point(9.0,7.0,4),new Point(13.0,3.0,4),new Point(14.0,3.0,4),new Point(15.0,2.0,4),new Point(16.0,2.0,4),new Point(17.0,1.0,4),new Point(19.0,1.0,4),new Point(7.0,0.0,5),new Point(8.0,0.0,5),new Point(10.0,13.0,6),new Point(11.0,12.0,6),new Point(11.0,11.0,6),new Point(12.0,10.0,6),new Point(12.0,9.0,6),new Point(16.0,5.0,6),new Point(17.0,5.0,6),new Point(18.0,4.0,6),new Point(26.0,4.0,6),new Point(27.0,5.0,6),new Point(28.0,5.0,6),new Point(33.0,10.0,6),new Point(33.0,11.0,6),new Point(34.0,12.0,6),new Point(34.0,13.0,6),new Point(35.0,14.0,6),new Point(35.0,20.0,6),new Point(34.0,21.0,6),new Point(34.0,23.0,6),new Point(33.0,24.0,6),new Point(33.0,25.0,6),new Point(30.0,28.0,6),new Point(29.0,28.0,6),new Point(27.0,30.0,6),new Point(25.0,30.0,6),new Point(24.0,31.0,6),new Point(20.0,31.0,6),new Point(19.0,30.0,6),new Point(17.0,30.0,6),new Point(16.0,29.0,6),new Point(15.0,29.0,6),new Point(14.0,28.0,6),new Point(14.0,27.0,6),new Point(11.0,24.0,6),new Point(11.0,23.0,6),new Point(10.0,22.0,6),new Point(10.0,14.0,6),new Point(20.0,0.0,7),new Point(21.0,0.0,7),new Point(34.0,1.0,8),new Point(40.0,23.0,8),new Point(39.0,24.0,8),new Point(39.0,26.0,8),new Point(38.0,27.0,8),new Point(38.0,28.0,8),new Point(36.0,30.0,8),new Point(36.0,33.0,8),new Point(37.0,34.0,8),new Point(38.0,34.0,8),new Point(40.0,32.0,8),new Point(40.0,30.0,8),new Point(41.0,29.0,8),new Point(41.0,28.0,8),new Point(42.0,27.0,8),new Point(42.0,26.0,8),new Point(43.0,25.0,8),new Point(43.0,24.0,8),new Point(41.0,22.0,8),new Point(41.0,21.0,8),new Point(40.0,20.0,8),new Point(40.0,11.0,8),new Point(39.0,10.0,8),new Point(39.0,9.0,8),new Point(38.0,8.0,8),new Point(38.0,7.0,8),new Point(34.0,3.0,8),new Point(34.0,2.0,8),new Point(35.0,1.0,8),new Point(37.0,1.0,8),new Point(40.0,4.0,8),new Point(40.0,5.0,8),new Point(42.0,7.0,8),new Point(42.0,11.0,8),new Point(43.0,12.0,8),new Point(43.0,13.0,8),new Point(44.0,14.0,8),new Point(44.0,21.0,8),new Point(43.0,22.0,8),new Point(34.0,5.0,9))), mNumPoints));
		mPntClouds.add(new PointCloud("engineOil",
				new ArrayList<Point>(Arrays.asList(new Point(0.0,5.0,1),new Point(58.0,9.0,1),new Point(58.0,8.0,1),new Point(57.0,9.0,1),new Point(54.0,6.0,1),new Point(52.0,6.0,1),new Point(51.0,7.0,1),new Point(50.0,7.0,1),new Point(44.0,13.0,1),new Point(44.0,14.0,1),new Point(41.0,17.0,1),new Point(41.0,18.0,1),new Point(36.0,23.0,1),new Point(10.0,23.0,1),new Point(10.0,13.0,1),new Point(8.0,11.0,1),new Point(6.0,11.0,1),new Point(5.0,10.0,1),new Point(3.0,10.0,1),new Point(2.0,9.0,1),new Point(0.0,9.0,1),new Point(0.0,6.0,1),new Point(1.0,4.0,1),new Point(1.0,1.0,1),new Point(6.0,1.0,1),new Point(7.0,2.0,1),new Point(9.0,2.0,1),new Point(10.0,3.0,1),new Point(12.0,3.0,1),new Point(12.0,4.0,1),new Point(13.0,5.0,1),new Point(20.0,5.0,1),new Point(21.0,4.0,1),new Point(21.0,1.0,1),new Point(3.0,5.0,2),new Point(4.0,4.0,2),new Point(6.0,4.0,2),new Point(7.0,5.0,2),new Point(8.0,5.0,2),new Point(10.0,7.0,2),new Point(9.0,8.0,2),new Point(6.0,8.0,2),new Point(5.0,7.0,2),new Point(4.0,7.0,2),new Point(3.0,6.0,2),new Point(12.0,8.0,3),new Point(13.0,7.0,3),new Point(28.0,7.0,3),new Point(32.0,11.0,3),new Point(35.0,11.0,3),new Point(36.0,10.0,3),new Point(39.0,10.0,3),new Point(40.0,9.0,3),new Point(42.0,9.0,3),new Point(43.0,10.0,3),new Point(41.0,12.0,3),new Point(41.0,13.0,3),new Point(37.0,17.0,3),new Point(37.0,18.0,3),new Point(35.0,20.0,3),new Point(13.0,20.0,3),new Point(12.0,19.0,3),new Point(12.0,9.0,3),new Point(21.0,0.0,4),new Point(58.0,7.0,4),new Point(58.0,6.0,4),new Point(57.0,5.0,4),new Point(57.0,4.0,4),new Point(56.0,3.0,4),new Point(52.0,3.0,4),new Point(51.0,4.0,4),new Point(48.0,4.0,4),new Point(47.0,5.0,4),new Point(45.0,5.0,4),new Point(44.0,6.0,4),new Point(41.0,6.0,4),new Point(40.0,7.0,4),new Point(36.0,7.0,4),new Point(35.0,8.0,4),new Point(34.0,8.0,4),new Point(31.0,5.0,4),new Point(25.0,5.0,4),new Point(24.0,4.0,4),new Point(24.0,3.0,4),new Point(23.0,2.0,4),new Point(24.0,1.0,4),new Point(23.0,0.0,4),new Point(22.0,0.0,4),new Point(55.0,16.0,5),new Point(56.0,15.0,5),new Point(56.0,14.0,5),new Point(57.0,13.0,5),new Point(58.0,14.0,5),new Point(58.0,19.0,5),new Point(57.0,20.0,5),new Point(55.0,18.0,5),new Point(55.0,17.0,5),new Point(56.0,9.0,6),new Point(56.0,13.0,7),new Point(57.0,12.0,7))), mNumPoints));
		mPntClouds.add(new PointCloud("waterTempurature", new ArrayList<Point>(Arrays
				.asList(new Point(0.0,25.0,1),new Point(48.0,29.0,1),new Point(48.0,28.0,1),new Point(47.0,29.0,1),new Point(45.0,27.0,1),new Point(44.0,27.0,1),new Point(43.0,26.0,1),new Point(42.0,27.0,1),new Point(40.0,27.0,1),new Point(39.0,28.0,1),new Point(37.0,28.0,1),new Point(36.0,29.0,1),new Point(35.0,29.0,1),new Point(34.0,28.0,1),new Point(31.0,28.0,1),new Point(29.0,26.0,1),new Point(28.0,27.0,1),new Point(28.0,28.0,1),new Point(27.0,29.0,1),new Point(26.0,29.0,1),new Point(25.0,30.0,1),new Point(25.0,31.0,1),new Point(26.0,32.0,1),new Point(27.0,32.0,1),new Point(28.0,31.0,1),new Point(29.0,31.0,1),new Point(30.0,30.0,1),new Point(34.0,30.0,1),new Point(36.0,32.0,1),new Point(39.0,32.0,1),new Point(40.0,31.0,1),new Point(41.0,31.0,1),new Point(42.0,32.0,1),new Point(42.0,33.0,1),new Point(41.0,34.0,1),new Point(40.0,34.0,1),new Point(39.0,35.0,1),new Point(35.0,35.0,1),new Point(33.0,33.0,1),new Point(31.0,33.0,1),new Point(30.0,34.0,1),new Point(29.0,34.0,1),new Point(28.0,35.0,1),new Point(25.0,35.0,1),new Point(24.0,34.0,1),new Point(23.0,34.0,1),new Point(22.0,33.0,1),new Point(21.0,33.0,1),new Point(20.0,34.0,1),new Point(19.0,34.0,1),new Point(18.0,35.0,1),new Point(15.0,35.0,1),new Point(14.0,34.0,1),new Point(13.0,34.0,1),new Point(12.0,33.0,1),new Point(9.0,33.0,1),new Point(8.0,34.0,1),new Point(7.0,34.0,1),new Point(6.0,33.0,1),new Point(6.0,32.0,1),new Point(7.0,31.0,1),new Point(8.0,31.0,1),new Point(9.0,30.0,1),new Point(13.0,30.0,1),new Point(14.0,31.0,1),new Point(15.0,31.0,1),new Point(16.0,32.0,1),new Point(17.0,32.0,1),new Point(18.0,31.0,1),new Point(19.0,31.0,1),new Point(20.0,30.0,1),new Point(21.0,30.0,1),new Point(22.0,29.0,1),new Point(19.0,26.0,1),new Point(17.0,28.0,1),new Point(11.0,28.0,1),new Point(10.0,27.0,1),new Point(9.0,27.0,1),new Point(8.0,26.0,1),new Point(3.0,26.0,1),new Point(1.0,28.0,1),new Point(0.0,27.0,1),new Point(0.0,26.0,1),new Point(1.0,24.0,1),new Point(2.0,24.0,1),new Point(3.0,23.0,1),new Point(9.0,23.0,1),new Point(10.0,24.0,1),new Point(11.0,24.0,1),new Point(12.0,25.0,1),new Point(16.0,25.0,1),new Point(18.0,23.0,1),new Point(19.0,23.0,1),new Point(20.0,24.0,1),new Point(21.0,24.0,1),new Point(22.0,23.0,1),new Point(22.0,1.0,1),new Point(23.0,0.0,2),new Point(48.0,27.0,2),new Point(48.0,26.0,2),new Point(46.0,24.0,2),new Point(39.0,24.0,2),new Point(38.0,25.0,2),new Point(37.0,25.0,2),new Point(36.0,26.0,2),new Point(34.0,26.0,2),new Point(32.0,24.0,2),new Point(31.0,24.0,2),new Point(30.0,23.0,2),new Point(29.0,23.0,2),new Point(28.0,24.0,2),new Point(27.0,23.0,2),new Point(27.0,21.0,2),new Point(28.0,20.0,2),new Point(33.0,20.0,2),new Point(35.0,18.0,2),new Point(34.0,17.0,2),new Point(28.0,17.0,2),new Point(27.0,16.0,2),new Point(27.0,14.0,2),new Point(28.0,13.0,2),new Point(34.0,13.0,2),new Point(35.0,12.0,2),new Point(34.0,11.0,2),new Point(34.0,10.0,2),new Point(28.0,10.0,2),new Point(27.0,9.0,2),new Point(27.0,7.0,2),new Point(28.0,6.0,2),new Point(34.0,6.0,2),new Point(35.0,5.0,2),new Point(33.0,3.0,2),new Point(28.0,3.0,2),new Point(27.0,2.0,2),new Point(27.0,1.0,2),new Point(26.0,0.0,2),new Point(24.0,0.0,2),new Point(35.0,19.0,3))),
				mNumPoints));
		mPntClouds.add(new PointCloud("airBag", new ArrayList<Point>(Arrays
				.asList(new Point(0.0,33.0,1),new Point(24.0,37.0,1),new Point(25.0,37.0,1),new Point(23.0,35.0,1),new Point(23.0,34.0,1),new Point(22.0,33.0,1),new Point(22.0,32.0,1),new Point(21.0,31.0,1),new Point(21.0,30.0,1),new Point(19.0,28.0,1),new Point(12.0,28.0,1),new Point(10.0,30.0,1),new Point(10.0,31.0,1),new Point(8.0,33.0,1),new Point(8.0,34.0,1),new Point(6.0,36.0,1),new Point(6.0,37.0,1),new Point(5.0,38.0,1),new Point(1.0,38.0,1),new Point(0.0,37.0,1),new Point(0.0,34.0,1),new Point(1.0,32.0,1),new Point(2.0,31.0,1),new Point(2.0,30.0,1),new Point(4.0,28.0,1),new Point(4.0,27.0,1),new Point(5.0,26.0,1),new Point(5.0,25.0,1),new Point(7.0,23.0,1),new Point(7.0,22.0,1),new Point(8.0,21.0,1),new Point(14.0,21.0,1),new Point(16.0,23.0,1),new Point(16.0,24.0,1),new Point(17.0,25.0,1),new Point(17.0,26.0,1),new Point(18.0,27.0,1),new Point(19.0,26.0,1),new Point(18.0,25.0,1),new Point(18.0,24.0,1),new Point(17.0,23.0,1),new Point(17.0,20.0,1),new Point(19.0,20.0,1),new Point(20.0,19.0,1),new Point(20.0,18.0,1),new Point(22.0,16.0,1),new Point(22.0,15.0,1),new Point(24.0,13.0,1),new Point(24.0,12.0,1),new Point(26.0,10.0,1),new Point(27.0,10.0,1),new Point(27.0,11.0,1),new Point(30.0,14.0,1),new Point(31.0,14.0,1),new Point(31.0,15.0,1),new Point(32.0,16.0,1),new Point(31.0,17.0,1),new Point(31.0,18.0,1),new Point(30.0,19.0,1),new Point(30.0,20.0,1),new Point(29.0,21.0,1),new Point(29.0,22.0,1),new Point(28.0,23.0,1),new Point(28.0,24.0,1),new Point(27.0,25.0,1),new Point(27.0,26.0,1),new Point(26.0,27.0,1),new Point(26.0,28.0,1),new Point(24.0,30.0,1),new Point(26.0,32.0,1),new Point(26.0,33.0,1),new Point(27.0,34.0,1),new Point(27.0,36.0,1),new Point(26.0,37.0,1),new Point(2.0,9.0,2),new Point(24.0,9.0,2),new Point(23.0,8.0,2),new Point(23.0,7.0,2),new Point(22.0,6.0,2),new Point(21.0,6.0,2),new Point(21.0,13.0,2),new Point(16.0,18.0,2),new Point(14.0,18.0,2),new Point(13.0,19.0,2),new Point(12.0,19.0,2),new Point(11.0,18.0,2),new Point(8.0,18.0,2),new Point(7.0,17.0,2),new Point(6.0,17.0,2),new Point(4.0,15.0,2),new Point(4.0,14.0,2),new Point(3.0,13.0,2),new Point(3.0,5.0,2),new Point(6.0,2.0,2),new Point(7.0,2.0,2),new Point(8.0,1.0,2),new Point(9.0,1.0,2),new Point(10.0,0.0,3),new Point(20.0,5.0,3),new Point(19.0,4.0,3),new Point(19.0,3.0,3),new Point(18.0,2.0,3),new Point(17.0,2.0,3),new Point(16.0,1.0,3),new Point(14.0,1.0,3),new Point(13.0,0.0,3),new Point(11.0,0.0,3),new Point(22.0,3.0,4),new Point(22.0,4.0,4),new Point(23.0,2.0,4),new Point(24.0,1.0,4),new Point(26.0,1.0,4),new Point(27.0,0.0,5),new Point(28.0,9.0,5),new Point(30.0,9.0,5),new Point(31.0,8.0,5),new Point(31.0,7.0,5),new Point(32.0,6.0,5),new Point(32.0,4.0,5),new Point(31.0,3.0,5),new Point(31.0,2.0,5),new Point(30.0,1.0,5),new Point(29.0,1.0,5),new Point(28.0,0.0,5),new Point(31.0,10.0,6),new Point(32.0,13.0,6),new Point(33.0,13.0,6),new Point(31.0,11.0,6),new Point(35.0,7.0,6),new Point(36.0,7.0,6),new Point(37.0,6.0,6),new Point(38.0,6.0,6),new Point(39.0,7.0,6),new Point(39.0,10.0,6),new Point(38.0,11.0,6),new Point(37.0,11.0,6),new Point(36.0,12.0,6),new Point(35.0,12.0,6),new Point(34.0,13.0,6))), mNumPoints));
		mPntClouds.add(new PointCloud("ABS", new ArrayList<Point>(Arrays
				.asList(new Point(0.0,12.0,1),new Point(6.0,24.0,1),new Point(5.0,23.0,1),new Point(5.0,21.0,1),new Point(6.0,20.0,1),new Point(7.0,21.0,1),new Point(7.0,22.0,1),new Point(8.0,23.0,1),new Point(8.0,24.0,1),new Point(7.0,25.0,1),new Point(7.0,26.0,1),new Point(8.0,27.0,1),new Point(8.0,29.0,1),new Point(7.0,30.0,1),new Point(6.0,30.0,1),new Point(5.0,29.0,1),new Point(5.0,28.0,1),new Point(3.0,26.0,1),new Point(3.0,24.0,1),new Point(2.0,23.0,1),new Point(2.0,22.0,1),new Point(1.0,21.0,1),new Point(1.0,18.0,1),new Point(0.0,17.0,1),new Point(0.0,13.0,1),new Point(1.0,11.0,1),new Point(1.0,8.0,1),new Point(2.0,7.0,1),new Point(2.0,6.0,1),new Point(3.0,5.0,1),new Point(3.0,4.0,1),new Point(4.0,3.0,1),new Point(4.0,2.0,1),new Point(5.0,1.0,1),new Point(8.0,1.0,1),new Point(8.0,3.0,1),new Point(6.0,5.0,1),new Point(6.0,6.0,1),new Point(5.0,7.0,1),new Point(5.0,8.0,1),new Point(4.0,9.0,1),new Point(4.0,12.0,1),new Point(3.0,13.0,1),new Point(3.0,17.0,1),new Point(4.0,18.0,1),new Point(4.0,20.0,1),new Point(6.0,11.0,2),new Point(6.0,12.0,2),new Point(7.0,10.0,2),new Point(7.0,8.0,2),new Point(10.0,5.0,2),new Point(10.0,4.0,2),new Point(11.0,3.0,2),new Point(12.0,3.0,2),new Point(13.0,2.0,2),new Point(14.0,2.0,2),new Point(15.0,1.0,2),new Point(17.0,1.0,2),new Point(9.0,11.0,3),new Point(11.0,9.0,3),new Point(11.0,8.0,3),new Point(14.0,5.0,3),new Point(15.0,5.0,3),new Point(16.0,4.0,3),new Point(17.0,4.0,3),new Point(18.0,3.0,3),new Point(23.0,3.0,3),new Point(24.0,4.0,3),new Point(26.0,4.0,3),new Point(31.0,9.0,3),new Point(31.0,10.0,3),new Point(32.0,11.0,3),new Point(32.0,12.0,3),new Point(33.0,13.0,3),new Point(33.0,17.0,3),new Point(32.0,18.0,3),new Point(32.0,20.0,3),new Point(31.0,21.0,3),new Point(31.0,22.0,3),new Point(28.0,25.0,3),new Point(27.0,25.0,3),new Point(25.0,27.0,3),new Point(21.0,27.0,3),new Point(20.0,28.0,3),new Point(19.0,27.0,3),new Point(17.0,27.0,3),new Point(16.0,26.0,3),new Point(15.0,26.0,3),new Point(10.0,21.0,3),new Point(10.0,20.0,3),new Point(9.0,19.0,3),new Point(9.0,12.0,3),new Point(9.0,25.0,4),new Point(25.0,1.0,4),new Point(27.0,1.0,4),new Point(28.0,2.0,4),new Point(29.0,2.0,4),new Point(33.0,6.0,4),new Point(33.0,7.0,4),new Point(35.0,9.0,4),new Point(35.0,11.0,4),new Point(36.0,12.0,4),new Point(36.0,19.0,4),new Point(35.0,20.0,4),new Point(35.0,21.0,4),new Point(34.0,22.0,4),new Point(34.0,23.0,4),new Point(32.0,25.0,4),new Point(32.0,26.0,4),new Point(30.0,28.0,4),new Point(29.0,28.0,4),new Point(28.0,29.0,4),new Point(27.0,29.0,4),new Point(26.0,30.0,4),new Point(23.0,30.0,4),new Point(22.0,31.0,4),new Point(19.0,31.0,4),new Point(18.0,30.0,4),new Point(15.0,30.0,4),new Point(13.0,28.0,4),new Point(12.0,28.0,4),new Point(10.0,26.0,4),new Point(11.0,18.0,5),new Point(15.0,13.0,5),new Point(13.0,15.0,5),new Point(13.0,16.0,5),new Point(12.0,17.0,5),new Point(12.0,15.0,5),new Point(14.0,13.0,5),new Point(16.0,15.0,5),new Point(12.0,19.0,5),new Point(13.0,13.0,6),new Point(15.0,15.0,7),new Point(15.0,18.0,7),new Point(16.0,18.0,7),new Point(16.0,16.0,7),new Point(17.0,18.0,8),new Point(18.0,0.0,9),new Point(19.0,0.0,9),new Point(19.0,13.0,10),new Point(20.0,13.0,10),new Point(20.0,14.0,10),new Point(21.0,13.0,10),new Point(24.0,16.0,10),new Point(24.0,18.0,10),new Point(21.0,15.0,10),new Point(21.0,16.0,10),new Point(20.0,17.0,10),new Point(21.0,18.0,10),new Point(20.0,19.0,10),new Point(19.0,18.0,10),new Point(19.0,14.0,10),new Point(21.0,19.0,11),new Point(22.0,18.0,11),new Point(23.0,18.0,11),new Point(22.0,19.0,11),new Point(22.0,13.0,12),new Point(24.0,14.0,12),new Point(23.0,13.0,12),new Point(26.0,13.0,13),new Point(27.0,14.0,13),new Point(27.0,13.0,13),new Point(29.0,13.0,13),new Point(30.0,14.0,13),new Point(30.0,13.0,13),new Point(27.0,16.0,13),new Point(26.0,15.0,13),new Point(26.0,14.0,13),new Point(26.0,17.0,14),new Point(30.0,17.0,14),new Point(29.0,18.0,14),new Point(29.0,19.0,14),new Point(31.0,17.0,14),new Point(30.0,16.0,14),new Point(29.0,16.0,14),new Point(29.0,15.0,14),new Point(27.0,17.0,14),new Point(28.0,18.0,14),new Point(27.0,19.0,14),new Point(26.0,18.0,14),new Point(28.0,19.0,15),new Point(33.0,2.0,16),new Point(34.0,1.0,16),new Point(35.0,1.0,16),new Point(38.0,4.0,16),new Point(38.0,5.0,16),new Point(39.0,6.0,16),new Point(39.0,7.0,16),new Point(40.0,8.0,16),new Point(40.0,9.0,16),new Point(41.0,10.0,16),new Point(41.0,20.0,16),new Point(40.0,21.0,16),new Point(40.0,23.0,16),new Point(39.0,24.0,16),new Point(39.0,25.0,16),new Point(37.0,27.0,16),new Point(37.0,28.0,16),new Point(35.0,30.0,16),new Point(34.0,30.0,16),new Point(33.0,29.0,16),new Point(33.0,27.0,16),new Point(35.0,25.0,16),new Point(35.0,24.0,16),new Point(36.0,23.0,16),new Point(36.0,22.0,16),new Point(37.0,21.0,16),new Point(37.0,19.0,16),new Point(38.0,18.0,16),new Point(38.0,11.0,16),new Point(37.0,10.0,16),new Point(37.0,8.0,16),new Point(35.0,6.0,16),new Point(35.0,5.0,16),new Point(33.0,3.0,16),new Point(36.0,1.0,17))), mNumPoints));
		mPntClouds.add(new PointCloud("engine", new ArrayList<Point>(Arrays
				.asList(new Point(0.0,11.0,1),new Point(14.0,22.0,1),new Point(15.0,21.0,1),new Point(16.0,22.0,1),new Point(15.0,23.0,1),new Point(13.0,23.0,1),new Point(13.0,24.0,1),new Point(14.0,25.0,1),new Point(15.0,25.0,1),new Point(16.0,26.0,1),new Point(17.0,26.0,1),new Point(22.0,31.0,1),new Point(35.0,31.0,1),new Point(37.0,29.0,1),new Point(36.0,28.0,1),new Point(37.0,27.0,1),new Point(37.0,26.0,1),new Point(39.0,26.0,1),new Point(39.0,32.0,1),new Point(38.0,33.0,1),new Point(21.0,33.0,1),new Point(19.0,31.0,1),new Point(18.0,31.0,1),new Point(15.0,28.0,1),new Point(5.0,28.0,1),new Point(4.0,27.0,1),new Point(5.0,26.0,1),new Point(5.0,21.0,1),new Point(4.0,20.0,1),new Point(3.0,20.0,1),new Point(2.0,21.0,1),new Point(2.0,26.0,1),new Point(0.0,26.0,1),new Point(0.0,12.0,1),new Point(1.0,11.0,1),new Point(2.0,11.0,1),new Point(2.0,16.0,1),new Point(3.0,17.0,1),new Point(4.0,17.0,1),new Point(5.0,16.0,1),new Point(5.0,11.0,1),new Point(4.0,10.0,1),new Point(5.0,9.0,1),new Point(10.0,9.0,1),new Point(11.0,8.0,1),new Point(11.0,4.0,1),new Point(21.0,4.0,1),new Point(22.0,3.0,1),new Point(21.0,2.0,1),new Point(16.0,2.0,1),new Point(16.0,1.0,1),new Point(7.0,12.0,2),new Point(13.0,26.0,2),new Point(12.0,26.0,2),new Point(11.0,25.0,2),new Point(9.0,25.0,2),new Point(8.0,26.0,2),new Point(7.0,25.0,2),new Point(7.0,13.0,2),new Point(8.0,11.0,2),new Point(13.0,11.0,2),new Point(13.0,7.0,2),new Point(14.0,6.0,2),new Point(15.0,7.0,2),new Point(19.0,7.0,2),new Point(20.0,6.0,2),new Point(21.0,6.0,2),new Point(22.0,7.0,2),new Point(24.0,7.0,2),new Point(25.0,6.0,2),new Point(26.0,7.0,2),new Point(29.0,7.0,2),new Point(30.0,6.0,2),new Point(32.0,8.0,2),new Point(32.0,10.0,2),new Point(36.0,10.0,2),new Point(38.0,12.0,2),new Point(38.0,16.0,2),new Point(40.0,18.0,2),new Point(40.0,19.0,2),new Point(39.0,19.0,2),new Point(35.0,15.0,2),new Point(36.0,14.0,2),new Point(37.0,14.0,2),new Point(9.0,18.0,3),new Point(12.0,24.0,3),new Point(10.0,22.0,3),new Point(10.0,19.0,3),new Point(9.0,19.0,3),new Point(10.0,18.0,3),new Point(10.0,15.0,3),new Point(11.0,15.0,3),new Point(11.0,14.0,3),new Point(12.0,14.0,3),new Point(12.0,13.0,3),new Point(13.0,13.0,3),new Point(13.0,14.0,3),new Point(14.0,14.0,3),new Point(14.0,13.0,3),new Point(15.0,13.0,3),new Point(15.0,15.0,3),new Point(16.0,15.0,3),new Point(16.0,16.0,3),new Point(17.0,17.0,3),new Point(17.0,19.0,3),new Point(18.0,19.0,3),new Point(18.0,23.0,3),new Point(17.0,23.0,3),new Point(11.0,21.0,4),new Point(12.0,22.0,4),new Point(16.0,0.0,5),new Point(39.0,12.0,5),new Point(40.0,11.0,5),new Point(40.0,8.0,5),new Point(38.0,8.0,5),new Point(37.0,7.0,5),new Point(35.0,7.0,5),new Point(34.0,6.0,5),new Point(34.0,4.0,5),new Point(25.0,4.0,5),new Point(24.0,3.0,5),new Point(25.0,2.0,5),new Point(29.0,2.0,5),new Point(30.0,1.0,5),new Point(29.0,0.0,5),new Point(17.0,0.0,5),new Point(16.0,20.0,6),new Point(17.0,13.0,7),new Point(26.0,24.0,7),new Point(28.0,24.0,7),new Point(29.0,23.0,7),new Point(22.0,23.0,7),new Point(22.0,19.0,7),new Point(21.0,18.0,7),new Point(20.0,18.0,7),new Point(20.0,17.0,7),new Point(19.0,18.0,7),new Point(18.0,17.0,7),new Point(18.0,14.0,7),new Point(17.0,14.0,7),new Point(22.0,14.0,8),new Point(22.0,15.0,8),new Point(23.0,13.0,8),new Point(26.0,13.0,8),new Point(26.0,14.0,8),new Point(27.0,14.0,8),new Point(27.0,13.0,8),new Point(28.0,13.0,8),new Point(28.0,14.0,8),new Point(29.0,14.0,8),new Point(29.0,13.0,8),new Point(25.0,15.0,9),new Point(36.0,21.0,9),new Point(37.0,20.0,9),new Point(38.0,21.0,9),new Point(36.0,23.0,9),new Point(35.0,22.0,9),new Point(35.0,23.0,9),new Point(34.0,24.0,9),new Point(29.0,19.0,9),new Point(28.0,19.0,9),new Point(28.0,18.0,9),new Point(27.0,19.0,9),new Point(25.0,17.0,9),new Point(25.0,16.0,9),new Point(25.0,20.0,10),new Point(25.0,21.0,10),new Point(26.0,19.0,10),new Point(27.0,18.0,10),new Point(29.0,18.0,11),new Point(35.0,13.0,11),new Point(34.0,14.0,11),new Point(34.0,13.0,11),new Point(30.0,17.0,11),new Point(29.0,24.0,12),new Point(30.0,16.0,13),new Point(34.0,23.0,13),new Point(33.0,24.0,13),new Point(30.0,21.0,13),new Point(31.0,20.0,13),new Point(31.0,14.0,13),new Point(32.0,13.0,13),new Point(33.0,13.0,13),new Point(32.0,24.0,14),new Point(37.0,19.0,15),new Point(38.0,20.0,15),new Point(38.0,22.0,16),new Point(38.0,23.0,16),new Point(40.0,16.0,17),new Point(42.0,12.0,17),new Point(42.0,9.0,17),new Point(49.0,9.0,17),new Point(49.0,29.0,17),new Point(42.0,29.0,17),new Point(42.0,26.0,17),new Point(43.0,25.0,17),new Point(45.0,27.0,17),new Point(46.0,27.0,17),new Point(47.0,26.0,17),new Point(47.0,12.0,17),new Point(46.0,11.0,17),new Point(45.0,11.0,17),new Point(42.0,14.0,17),new Point(42.0,13.0,17),new Point(43.0,14.0,17),new Point(41.0,16.0,17),new Point(41.0,17.0,17),new Point(41.0,15.0,18),new Point(41.0,19.0,19),new Point(44.0,25.0,19),new Point(43.0,24.0,19),new Point(43.0,23.0,19),new Point(42.0,22.0,19),new Point(42.0,23.0,19),new Point(43.0,22.0,19),new Point(42.0,21.0,19),new Point(41.0,21.0,19),new Point(41.0,20.0,19),new Point(42.0,20.0,19))), mNumPoints));
		mPntClouds.add(new PointCloud("fuel", new ArrayList<Point>(Arrays
				.asList(new Point(0.0,38.0,1),new Point(20.0,1.0,1),new Point(21.0,1.0,1),new Point(22.0,2.0,1),new Point(22.0,16.0,1),new Point(27.0,21.0,1),new Point(27.0,34.0,1),new Point(28.0,35.0,1),new Point(30.0,35.0,1),new Point(31.0,36.0,1),new Point(33.0,36.0,1),new Point(35.0,34.0,1),new Point(35.0,9.0,1),new Point(34.0,8.0,1),new Point(34.0,7.0,1),new Point(32.0,5.0,1),new Point(31.0,5.0,1),new Point(29.0,3.0,1),new Point(28.0,3.0,1),new Point(26.0,1.0,1),new Point(25.0,2.0,1),new Point(25.0,3.0,1),new Point(26.0,4.0,1),new Point(27.0,4.0,1),new Point(30.0,7.0,1),new Point(31.0,7.0,1),new Point(32.0,8.0,1),new Point(32.0,9.0,1),new Point(33.0,10.0,1),new Point(33.0,33.0,1),new Point(29.0,37.0,1),new Point(28.0,37.0,1),new Point(27.0,36.0,1),new Point(26.0,36.0,1),new Point(25.0,35.0,1),new Point(25.0,22.0,1),new Point(23.0,20.0,1),new Point(22.0,21.0,1),new Point(22.0,36.0,1),new Point(24.0,38.0,1),new Point(24.0,40.0,1),new Point(0.0,40.0,1),new Point(0.0,39.0,1),new Point(1.0,37.0,1),new Point(1.0,36.0,1),new Point(2.0,35.0,1),new Point(2.0,34.0,1),new Point(1.0,33.0,1),new Point(2.0,32.0,1),new Point(1.0,31.0,1),new Point(2.0,30.0,1),new Point(2.0,2.0,1),new Point(3.0,1.0,1),new Point(4.0,0.0,2),new Point(8.0,1.0,2),new Point(7.0,0.0,2),new Point(5.0,0.0,2),new Point(4.0,5.0,3),new Point(5.0,4.0,3),new Point(18.0,4.0,3),new Point(20.0,6.0,3),new Point(19.0,7.0,3),new Point(20.0,8.0,3),new Point(20.0,9.0,3),new Point(19.0,10.0,3),new Point(19.0,12.0,3),new Point(18.0,13.0,3),new Point(17.0,13.0,3),new Point(16.0,14.0,3),new Point(6.0,14.0,3),new Point(4.0,12.0,3),new Point(4.0,6.0,3),new Point(9.0,0.0,4),new Point(11.0,1.0,4),new Point(10.0,0.0,4),new Point(12.0,0.0,5),new Point(13.0,0.0,5),new Point(25.0,18.0,6),new Point(34.0,36.0,7))),
				mNumPoints));
		mPntClouds.add(new PointCloud("door", new ArrayList<Point>(Arrays
				.asList(new Point(0.0,27.0,1),new Point(27.0,1.0,1),new Point(34.0,1.0,1),new Point(35.0,2.0,1),new Point(36.0,2.0,1),new Point(37.0,3.0,1),new Point(37.0,6.0,1),new Point(38.0,7.0,1),new Point(37.0,8.0,1),new Point(38.0,9.0,1),new Point(38.0,11.0,1),new Point(37.0,12.0,1),new Point(38.0,13.0,1),new Point(38.0,15.0,1),new Point(47.0,24.0,1),new Point(47.0,25.0,1),new Point(49.0,27.0,1),new Point(49.0,29.0,1),new Point(48.0,30.0,1),new Point(47.0,30.0,1),new Point(41.0,24.0,1),new Point(41.0,23.0,1),new Point(39.0,21.0,1),new Point(38.0,21.0,1),new Point(37.0,22.0,1),new Point(37.0,50.0,1),new Point(35.0,52.0,1),new Point(15.0,52.0,1),new Point(12.0,49.0,1),new Point(12.0,22.0,1),new Point(11.0,21.0,1),new Point(8.0,24.0,1),new Point(8.0,25.0,1),new Point(3.0,30.0,1),new Point(2.0,30.0,1),new Point(1.0,31.0,1),new Point(0.0,30.0,1),new Point(0.0,28.0,1),new Point(1.0,26.0,1),new Point(12.0,15.0,1),new Point(12.0,3.0,1),new Point(13.0,2.0,1),new Point(14.0,2.0,1),new Point(15.0,1.0,1),new Point(18.0,1.0,1),new Point(10.0,23.0,2),new Point(13.0,51.0,3),new Point(15.0,21.0,4),new Point(16.0,20.0,4),new Point(17.0,21.0,4),new Point(17.0,32.0,4),new Point(16.0,33.0,4),new Point(15.0,32.0,4),new Point(15.0,22.0,4),new Point(16.0,38.0,5),new Point(17.0,37.0,5),new Point(17.0,36.0,5),new Point(19.0,34.0,5),new Point(30.0,34.0,5),new Point(34.0,38.0,5),new Point(32.0,40.0,5),new Point(30.0,40.0,5),new Point(29.0,41.0,5),new Point(22.0,41.0,5),new Point(21.0,40.0,5),new Point(18.0,40.0,5),new Point(17.0,39.0,5),new Point(18.0,18.0,6),new Point(19.0,17.0,6),new Point(20.0,17.0,6),new Point(21.0,16.0,6),new Point(29.0,16.0,6),new Point(31.0,18.0,6),new Point(31.0,19.0,6),new Point(30.0,20.0,6),new Point(19.0,20.0,6),new Point(18.0,19.0,6),new Point(19.0,0.0,7),new Point(20.0,1.0,7),new Point(21.0,0.0,8),new Point(22.0,1.0,8),new Point(23.0,0.0,9),new Point(25.0,1.0,9),new Point(24.0,1.0,9),new Point(26.0,0.0,10),new Point(31.0,22.0,11),new Point(33.0,20.0,11),new Point(35.0,22.0,11),new Point(35.0,32.0,11),new Point(34.0,33.0,11),new Point(33.0,33.0,11),new Point(31.0,31.0,11),new Point(31.0,23.0,11),new Point(34.0,19.0,12),new Point(35.0,20.0,12))), mNumPoints));
//		mPntClouds.add(new PointCloud("cleaningAgent", new ArrayList<Point>(Arrays
//				.asList(new Point(0.0,14.0,1),new Point(8.0,1.0,1),new Point(10.0,3.0,1),new Point(12.0,3.0,1),new Point(13.0,2.0,1),new Point(13.0,1.0,1),new Point(15.0,3.0,1),new Point(17.0,3.0,1),new Point(18.0,2.0,1),new Point(19.0,3.0,1),new Point(18.0,4.0,1),new Point(18.0,7.0,1),new Point(19.0,8.0,1),new Point(20.0,8.0,1),new Point(21.0,9.0,1),new Point(25.0,9.0,1),new Point(26.0,10.0,1),new Point(29.0,10.0,1),new Point(30.0,11.0,1),new Point(31.0,11.0,1),new Point(32.0,12.0,1),new Point(33.0,12.0,1),new Point(34.0,13.0,1),new Point(35.0,13.0,1),new Point(36.0,14.0,1),new Point(37.0,14.0,1),new Point(38.0,15.0,1),new Point(38.0,19.0,1),new Point(36.0,21.0,1),new Point(36.0,22.0,1),new Point(33.0,25.0,1),new Point(33.0,26.0,1),new Point(32.0,27.0,1),new Point(32.0,28.0,1),new Point(30.0,30.0,1),new Point(30.0,31.0,1),new Point(29.0,32.0,1),new Point(25.0,32.0,1),new Point(24.0,31.0,1),new Point(21.0,31.0,1),new Point(20.0,32.0,1),new Point(20.0,33.0,1),new Point(18.0,33.0,1),new Point(16.0,31.0,1),new Point(13.0,31.0,1),new Point(12.0,32.0,1),new Point(10.0,32.0,1),new Point(8.0,30.0,1),new Point(8.0,29.0,1),new Point(6.0,27.0,1),new Point(6.0,26.0,1),new Point(5.0,25.0,1),new Point(5.0,24.0,1),new Point(3.0,22.0,1),new Point(3.0,21.0,1),new Point(1.0,19.0,1),new Point(1.0,18.0,1),new Point(0.0,17.0,1),new Point(0.0,15.0,1),new Point(1.0,13.0,1),new Point(2.0,13.0,1),new Point(3.0,12.0,1),new Point(4.0,12.0,1),new Point(5.0,11.0,1),new Point(8.0,11.0,1),new Point(9.0,10.0,1),new Point(13.0,10.0,1),new Point(14.0,9.0,1),new Point(18.0,9.0,1),new Point(2.0,16.0,2),new Point(20.0,22.0,2),new Point(21.0,23.0,2),new Point(26.0,23.0,2),new Point(27.0,22.0,2),new Point(29.0,22.0,2),new Point(30.0,21.0,2),new Point(31.0,22.0,2),new Point(32.0,22.0,2),new Point(35.0,19.0,2),new Point(35.0,18.0,2),new Point(36.0,17.0,2),new Point(34.0,15.0,2),new Point(33.0,15.0,2),new Point(32.0,14.0,2),new Point(31.0,14.0,2),new Point(30.0,13.0,2),new Point(28.0,13.0,2),new Point(27.0,12.0,2),new Point(24.0,12.0,2),new Point(23.0,11.0,2),new Point(22.0,11.0,2),new Point(21.0,12.0,2),new Point(21.0,15.0,2),new Point(20.0,16.0,2),new Point(20.0,17.0,2),new Point(21.0,18.0,2),new Point(21.0,20.0,2),new Point(18.0,23.0,2),new Point(13.0,23.0,2),new Point(12.0,22.0,2),new Point(10.0,22.0,2),new Point(9.0,21.0,2),new Point(8.0,22.0,2),new Point(7.0,22.0,2),new Point(5.0,20.0,2),new Point(5.0,19.0,2),new Point(3.0,17.0,2),new Point(3.0,15.0,2),new Point(4.0,15.0,2),new Point(5.0,14.0,2),new Point(6.0,14.0,2),new Point(7.0,13.0,2),new Point(9.0,13.0,2),new Point(10.0,12.0,2),new Point(14.0,12.0,2),new Point(15.0,11.0,2),new Point(16.0,11.0,2),new Point(18.0,13.0,2),new Point(18.0,21.0,2),new Point(6.0,4.0,3),new Point(7.0,3.0,3),new Point(8.0,3.0,3),new Point(9.0,4.0,3),new Point(9.0,7.0,3),new Point(8.0,8.0,3),new Point(7.0,8.0,3),new Point(6.0,7.0,3),new Point(6.0,5.0,3),new Point(9.0,0.0,4),new Point(10.0,0.0,4),new Point(14.0,0.0,5),new Point(18.0,1.0,5),new Point(18.0,0.0,5),new Point(15.0,0.0,5),new Point(20.0,3.0,6),new Point(21.0,7.0,6),new Point(21.0,5.0,6),new Point(20.0,4.0,6),new Point(21.0,2.0,6),new Point(21.0,1.0,6),new Point(21.0,0.0,7),new Point(22.0,3.0,7),new Point(24.0,3.0,7),new Point(25.0,2.0,7),new Point(26.0,2.0,7),new Point(27.0,3.0,7),new Point(29.0,3.0,7),new Point(31.0,1.0,7),new Point(30.0,0.0,7),new Point(22.0,0.0,7),new Point(30.0,3.0,8),new Point(31.0,5.0,8),new Point(31.0,7.0,8),new Point(32.0,8.0,8),new Point(33.0,8.0,8),new Point(34.0,7.0,8),new Point(34.0,4.0,8),new Point(33.0,3.0,8),new Point(32.0,3.0,8),new Point(31.0,4.0,8))), mNumPoints));
		 mPntClouds.add(new PointCloud("seatBelt", new
		 ArrayList<Point>(Arrays
		 .asList(
				 new Point(0.0,39.0,1),new Point(12.0,27.0,1),new Point(12.0,28.0,1),new Point(11.0,29.0,1),new Point(11.0,30.0,1),new Point(12.0,31.0,1),new Point(13.0,30.0,1),new Point(13.0,29.0,1),new Point(24.0,18.0,1),new Point(24.0,17.0,1),new Point(26.0,15.0,1),new Point(25.0,14.0,1),new Point(25.0,13.0,1),new Point(33.0,5.0,1),new Point(34.0,5.0,1),new Point(35.0,6.0,1),new Point(35.0,7.0,1),new Point(30.0,12.0,1),new Point(30.0,14.0,1),new Point(31.0,15.0,1),new Point(31.0,20.0,1),new Point(33.0,22.0,1),new Point(33.0,34.0,1),new Point(34.0,35.0,1),new Point(37.0,35.0,1),new Point(40.0,38.0,1),new Point(40.0,41.0,1),new Point(39.0,42.0,1),new Point(37.0,42.0,1),new Point(35.0,40.0,1),new Point(34.0,40.0,1),new Point(33.0,41.0,1),new Point(33.0,42.0,1),new Point(32.0,43.0,1),new Point(32.0,44.0,1),new Point(30.0,46.0,1),new Point(30.0,47.0,1),new Point(29.0,48.0,1),new Point(29.0,49.0,1),new Point(26.0,52.0,1),new Point(23.0,52.0,1),new Point(22.0,51.0,1),new Point(22.0,48.0,1),new Point(23.0,47.0,1),new Point(23.0,46.0,1),new Point(25.0,44.0,1),new Point(25.0,43.0,1),new Point(26.0,42.0,1),new Point(24.0,40.0,1),new Point(16.0,40.0,1),new Point(15.0,41.0,1),new Point(15.0,42.0,1),new Point(16.0,43.0,1),new Point(16.0,44.0,1),new Point(17.0,45.0,1),new Point(17.0,46.0,1),new Point(18.0,47.0,1),new Point(18.0,48.0,1),new Point(19.0,49.0,1),new Point(19.0,50.0,1),new Point(18.0,51.0,1),new Point(18.0,52.0,1),new Point(15.0,52.0,1),new Point(12.0,49.0,1),new Point(12.0,48.0,1),new Point(10.0,46.0,1),new Point(10.0,45.0,1),new Point(9.0,44.0,1),new Point(9.0,43.0,1),new Point(8.0,42.0,1),new Point(8.0,41.0,1),new Point(7.0,40.0,1),new Point(4.0,40.0,1),new Point(2.0,42.0,1),new Point(0.0,42.0,1),new Point(0.0,40.0,1),new Point(1.0,38.0,1),new Point(8.0,31.0,1),new Point(8.0,22.0,1),new Point(10.0,20.0,1),new Point(10.0,15.0,1),new Point(11.0,14.0,1),new Point(15.0,14.0,1),new Point(17.0,12.0,1),new Point(14.0,9.0,1),new Point(14.0,4.0,1),new Point(17.0,1.0,1),new Point(9.0,35.0,2),new Point(11.0,33.0,2),new Point(13.0,33.0,2),new Point(14.0,34.0,2),new Point(12.0,36.0,2),new Point(10.0,36.0,2),new Point(18.0,0.0,3),new Point(24.0,12.0,3),new Point(24.0,11.0,3),new Point(26.0,9.0,3),new Point(26.0,4.0,3),new Point(22.0,0.0,3),new Point(19.0,0.0,3),new Point(21.0,34.0,4),new Point(28.0,35.0,4),new Point(29.0,34.0,4),new Point(28.0,33.0,4),new Point(26.0,35.0,4),new Point(24.0,33.0,4),new Point(22.0,35.0,4),new Point(22.0,33.0,4),new Point(24.0,35.0,5),new Point(26.0,33.0,6)
		 )), mNumPoints));
		 mPntClouds.add(new PointCloud("engine-2", new
		 ArrayList<Point>(Arrays
		 .asList(
				 new Point(0.0,14.0,1),new Point(1.0,19.0,1),new Point(1.0,18.0,1),new Point(0.0,17.0,1),new Point(0.0,15.0,1),new Point(1.0,13.0,1),new Point(1.0,11.0,1),new Point(3.0,11.0,1),new Point(4.0,12.0,1),new Point(5.0,11.0,1),new Point(5.0,10.0,1),new Point(10.0,10.0,1),new Point(11.0,9.0,1),new Point(10.0,8.0,1),new Point(11.0,7.0,1),new Point(11.0,5.0,1),new Point(14.0,5.0,1),new Point(15.0,4.0,1),new Point(16.0,5.0,1),new Point(17.0,4.0,1),new Point(16.0,3.0,1),new Point(16.0,2.0,1),new Point(17.0,1.0,1),new Point(20.0,1.0,1),new Point(0.0,20.0,2),new Point(26.0,1.0,2),new Point(31.0,1.0,2),new Point(31.0,4.0,2),new Point(32.0,5.0,2),new Point(36.0,5.0,2),new Point(36.0,7.0,2),new Point(37.0,8.0,2),new Point(41.0,8.0,2),new Point(42.0,9.0,2),new Point(50.0,9.0,2),new Point(51.0,10.0,2),new Point(51.0,30.0,2),new Point(50.0,31.0,2),new Point(44.0,31.0,2),new Point(43.0,30.0,2),new Point(42.0,30.0,2),new Point(40.0,32.0,2),new Point(40.0,34.0,2),new Point(36.0,34.0,2),new Point(35.0,35.0,2),new Point(34.0,34.0,2),new Point(32.0,34.0,2),new Point(31.0,35.0,2),new Point(30.0,34.0,2),new Point(29.0,35.0,2),new Point(28.0,34.0,2),new Point(27.0,35.0,2),new Point(26.0,34.0,2),new Point(25.0,34.0,2),new Point(24.0,35.0,2),new Point(23.0,34.0,2),new Point(21.0,34.0,2),new Point(17.0,30.0,2),new Point(16.0,30.0,2),new Point(15.0,29.0,2),new Point(5.0,29.0,2),new Point(4.0,28.0,2),new Point(2.0,28.0,2),new Point(1.0,27.0,2),new Point(1.0,22.0,2),new Point(0.0,21.0,2),new Point(8.0,19.0,3),new Point(41.0,15.0,3),new Point(41.0,14.0,3),new Point(42.0,13.0,3),new Point(43.0,14.0,3),new Point(45.0,14.0,3),new Point(46.0,13.0,3),new Point(47.0,14.0,3),new Point(47.0,16.0,3),new Point(48.0,17.0,3),new Point(47.0,18.0,3),new Point(47.0,22.0,3),new Point(48.0,23.0,3),new Point(47.0,24.0,3),new Point(47.0,25.0,3),new Point(46.0,26.0,3),new Point(45.0,25.0,3),new Point(43.0,25.0,3),new Point(40.0,28.0,3),new Point(39.0,28.0,3),new Point(37.0,26.0,3),new Point(37.0,30.0,3),new Point(36.0,31.0,3),new Point(23.0,31.0,3),new Point(19.0,27.0,3),new Point(18.0,27.0,3),new Point(17.0,26.0,3),new Point(9.0,26.0,3),new Point(8.0,25.0,3),new Point(8.0,24.0,3),new Point(9.0,23.0,3),new Point(8.0,22.0,3),new Point(8.0,20.0,3),new Point(9.0,18.0,3),new Point(9.0,16.0,3),new Point(10.0,15.0,3),new Point(10.0,14.0,3),new Point(11.0,13.0,3),new Point(15.0,13.0,3),new Point(15.0,12.0,3),new Point(14.0,11.0,3),new Point(15.0,10.0,3),new Point(14.0,9.0,3),new Point(15.0,8.0,3),new Point(29.0,8.0,3),new Point(30.0,9.0,3),new Point(31.0,8.0,3),new Point(32.0,9.0,3),new Point(32.0,10.0,3),new Point(33.0,11.0,3),new Point(34.0,11.0,3),new Point(35.0,12.0,3),new Point(36.0,12.0,3),new Point(37.0,13.0,3),new Point(38.0,13.0,3),new Point(38.0,12.0,3),new Point(39.0,13.0,3),new Point(40.0,13.0,3),new Point(21.0,0.0,4),new Point(24.0,1.0,4),new Point(22.0,1.0,4),new Point(25.0,0.0,5),new Point(37.0,11.0,6),new Point(41.0,11.0,7),new Point(42.0,10.0,7),new Point(43.0,11.0,7),new Point(42.0,12.0,7)
		 )), mNumPoints));
		 mPntClouds.add(new PointCloud("door-1", new
		 ArrayList<Point>(Arrays
		 .asList(
				 new Point(1.0,30.0,1),new Point(39.0,52.0,1),new Point(39.0,51.0,1),new Point(37.0,53.0,1),new Point(34.0,53.0,1),new Point(33.0,54.0,1),new Point(21.0,54.0,1),new Point(20.0,53.0,1),new Point(17.0,53.0,1),new Point(16.0,52.0,1),new Point(15.0,52.0,1),new Point(14.0,51.0,1),new Point(14.0,24.0,1),new Point(13.0,23.0,1),new Point(4.0,32.0,1),new Point(3.0,32.0,1),new Point(2.0,31.0,1),new Point(2.0,28.0,1),new Point(13.0,17.0,1),new Point(13.0,16.0,1),new Point(14.0,15.0,1),new Point(14.0,4.0,1),new Point(15.0,3.0,1),new Point(16.0,3.0,1),new Point(17.0,2.0,1),new Point(19.0,2.0,1),new Point(20.0,1.0,1),new Point(33.0,1.0,1),new Point(34.0,2.0,1),new Point(37.0,2.0,1),new Point(40.0,5.0,1),new Point(40.0,15.0,1),new Point(42.0,17.0,1),new Point(42.0,18.0,1),new Point(51.0,27.0,1),new Point(51.0,31.0,1),new Point(49.0,31.0,1),new Point(40.0,22.0,1),new Point(39.0,23.0,1),new Point(39.0,50.0,1)
		 )), mNumPoints));
		 mPntClouds.add(new PointCloud("seatBelt-2", new
		 ArrayList<Point>(Arrays
		 .asList(
				 new Point(0.0,55.0,1),new Point(1.0,41.0,2),new Point(38.0,43.0,2),new Point(39.0,43.0,2),new Point(37.0,41.0,2),new Point(36.0,41.0,2),new Point(35.0,42.0,2),new Point(35.0,43.0,2),new Point(34.0,44.0,2),new Point(34.0,45.0,2),new Point(33.0,46.0,2),new Point(33.0,47.0,2),new Point(31.0,49.0,2),new Point(31.0,50.0,2),new Point(30.0,51.0,2),new Point(30.0,52.0,2),new Point(29.0,53.0,2),new Point(25.0,53.0,2),new Point(24.0,52.0,2),new Point(24.0,49.0,2),new Point(25.0,48.0,2),new Point(25.0,47.0,2),new Point(26.0,46.0,2),new Point(26.0,45.0,2),new Point(28.0,43.0,2),new Point(26.0,41.0,2),new Point(18.0,41.0,2),new Point(17.0,42.0,2),new Point(17.0,43.0,2),new Point(18.0,44.0,2),new Point(18.0,45.0,2),new Point(20.0,47.0,2),new Point(20.0,48.0,2),new Point(21.0,49.0,2),new Point(21.0,52.0,2),new Point(20.0,53.0,2),new Point(16.0,53.0,2),new Point(15.0,52.0,2),new Point(15.0,51.0,2),new Point(13.0,49.0,2),new Point(13.0,48.0,2),new Point(12.0,47.0,2),new Point(12.0,46.0,2),new Point(10.0,44.0,2),new Point(10.0,43.0,2),new Point(9.0,42.0,2),new Point(8.0,42.0,2),new Point(7.0,41.0,2),new Point(6.0,41.0,2),new Point(4.0,43.0,2),new Point(2.0,43.0,2),new Point(1.0,42.0,2),new Point(2.0,40.0,2),new Point(10.0,32.0,2),new Point(10.0,22.0,2),new Point(11.0,22.0,2),new Point(12.0,21.0,2),new Point(12.0,15.0,2),new Point(13.0,15.0,2),new Point(14.0,14.0,2),new Point(17.0,14.0,2),new Point(18.0,13.0,2),new Point(18.0,12.0,2),new Point(17.0,11.0,2),new Point(16.0,11.0,2),new Point(16.0,5.0,2),new Point(17.0,4.0,2),new Point(17.0,3.0,2),new Point(18.0,3.0,2),new Point(20.0,1.0,2),new Point(24.0,1.0,2),new Point(28.0,5.0,2),new Point(28.0,7.0,2),new Point(29.0,8.0,2),new Point(28.0,9.0,2),new Point(28.0,11.0,2),new Point(27.0,11.0,2),new Point(26.0,12.0,2),new Point(26.0,13.0,2),new Point(27.0,14.0,2),new Point(31.0,10.0,2),new Point(31.0,9.0,2),new Point(32.0,9.0,2),new Point(33.0,8.0,2),new Point(33.0,7.0,2),new Point(34.0,7.0,2),new Point(35.0,6.0,2),new Point(36.0,6.0,2),new Point(37.0,7.0,2),new Point(37.0,8.0,2),new Point(34.0,11.0,2),new Point(34.0,12.0,2),new Point(32.0,14.0,2),new Point(32.0,15.0,2),new Point(33.0,16.0,2),new Point(33.0,21.0,2),new Point(34.0,22.0,2),new Point(35.0,22.0,2),new Point(35.0,35.0,2),new Point(36.0,36.0,2),new Point(39.0,36.0,2),new Point(42.0,39.0,2),new Point(43.0,39.0,2),new Point(43.0,41.0,2),new Point(41.0,43.0,2),new Point(40.0,43.0,2)
		 )), mNumPoints));
		 
		 mPntClouds.add(new PointCloud("handbreak-2", new
			 ArrayList<Point>(Arrays
			 .asList(
					 new Point(3.0,10.0,1),new Point(7.0,23.0,1),new Point(7.0,22.0,1),new Point(8.0,21.0,1),new Point(9.0,22.0,1),new Point(9.0,23.0,1),new Point(8.0,24.0,1),new Point(8.0,25.0,1),new Point(9.0,24.0,1),new Point(10.0,25.0,1),new Point(9.0,26.0,1),new Point(9.0,27.0,1),new Point(10.0,28.0,1),new Point(10.0,29.0,1),new Point(11.0,30.0,1),new Point(10.0,31.0,1),new Point(8.0,31.0,1),new Point(6.0,29.0,1),new Point(6.0,28.0,1),new Point(5.0,27.0,1),new Point(5.0,26.0,1),new Point(4.0,25.0,1),new Point(4.0,23.0,1),new Point(3.0,22.0,1),new Point(3.0,11.0,1),new Point(4.0,9.0,1),new Point(4.0,7.0,1),new Point(6.0,5.0,1),new Point(6.0,4.0,1),new Point(8.0,2.0,1),new Point(9.0,2.0,1),new Point(10.0,3.0,1),new Point(10.0,5.0,1),new Point(8.0,7.0,1),new Point(8.0,8.0,1),new Point(7.0,9.0,1),new Point(7.0,11.0,1),new Point(6.0,12.0,1),new Point(6.0,20.0,1),new Point(7.0,21.0,1),new Point(8.0,20.0,1),new Point(8.0,13.0,1),new Point(9.0,12.0,1),new Point(9.0,10.0,1),new Point(10.0,9.0,1),new Point(10.0,8.0,1),new Point(14.0,4.0,1),new Point(15.0,4.0,1),new Point(16.0,3.0,1),new Point(17.0,3.0,1),new Point(18.0,2.0,1),new Point(22.0,2.0,1),new Point(23.0,1.0,1),new Point(24.0,2.0,1),new Point(28.0,2.0,1),new Point(29.0,3.0,1),new Point(30.0,3.0,1),new Point(31.0,4.0,1),new Point(32.0,4.0,1),new Point(36.0,8.0,1),new Point(36.0,9.0,1),new Point(37.0,10.0,1),new Point(37.0,11.0,1),new Point(38.0,12.0,1),new Point(38.0,21.0,1),new Point(39.0,22.0,1),new Point(39.0,23.0,1),new Point(38.0,24.0,1),new Point(38.0,25.0,1),new Point(37.0,24.0,1),new Point(37.0,22.0,1),new Point(11.0,14.0,2),new Point(12.0,13.0,2),new Point(12.0,12.0,2),new Point(13.0,11.0,2),new Point(13.0,10.0,2),new Point(16.0,7.0,2),new Point(17.0,7.0,2),new Point(18.0,6.0,2),new Point(19.0,6.0,2),new Point(20.0,5.0,2),new Point(26.0,5.0,2),new Point(27.0,6.0,2),new Point(28.0,6.0,2),new Point(29.0,7.0,2),new Point(30.0,7.0,2),new Point(33.0,10.0,2),new Point(33.0,11.0,2),new Point(34.0,12.0,2),new Point(34.0,13.0,2),new Point(35.0,14.0,2),new Point(35.0,19.0,2),new Point(34.0,20.0,2),new Point(34.0,22.0,2),new Point(33.0,23.0,2),new Point(33.0,24.0,2),new Point(31.0,26.0,2),new Point(30.0,26.0,2),new Point(28.0,28.0,2),new Point(26.0,28.0,2),new Point(25.0,29.0,2),new Point(21.0,29.0,2),new Point(20.0,28.0,2),new Point(18.0,28.0,2),new Point(16.0,26.0,2),new Point(15.0,26.0,2),new Point(14.0,25.0,2),new Point(14.0,24.0,2),new Point(12.0,22.0,2),new Point(12.0,21.0,2),new Point(11.0,20.0,2),new Point(11.0,15.0,2),new Point(11.0,26.0,3),new Point(35.0,3.0,3),new Point(35.0,4.0,3),new Point(37.0,2.0,3),new Point(41.0,6.0,3),new Point(41.0,7.0,3),new Point(42.0,8.0,3),new Point(42.0,9.0,3),new Point(43.0,10.0,3),new Point(43.0,14.0,3),new Point(44.0,15.0,3),new Point(44.0,18.0,3),new Point(43.0,19.0,3),new Point(43.0,23.0,3),new Point(42.0,24.0,3),new Point(42.0,25.0,3),new Point(41.0,26.0,3),new Point(41.0,27.0,3),new Point(39.0,29.0,3),new Point(39.0,30.0,3),new Point(38.0,31.0,3),new Point(35.0,31.0,3),new Point(35.0,29.0,3),new Point(37.0,27.0,3),new Point(37.0,26.0,3),new Point(36.0,25.0,3),new Point(36.0,26.0,3),new Point(33.0,29.0,3),new Point(32.0,29.0,3),new Point(30.0,31.0,3),new Point(28.0,31.0,3),new Point(27.0,32.0,3),new Point(19.0,32.0,3),new Point(18.0,31.0,3),new Point(16.0,31.0,3),new Point(14.0,29.0,3),new Point(13.0,29.0,3),new Point(12.0,28.0,3),new Point(12.0,27.0,3),new Point(19.0,10.0,4),new Point(20.0,9.0,4),new Point(26.0,9.0,4),new Point(28.0,11.0,4),new Point(28.0,12.0,4),new Point(29.0,13.0,4),new Point(29.0,17.0,4),new Point(28.0,18.0,4),new Point(26.0,18.0,4),new Point(25.0,19.0,4),new Point(22.0,19.0,4),new Point(21.0,20.0,4),new Point(21.0,24.0,4),new Point(20.0,25.0,4),new Point(19.0,25.0,4),new Point(19.0,11.0,4),new Point(21.0,12.0,5),new Point(22.0,11.0,5),new Point(25.0,11.0,5),new Point(26.0,12.0,5),new Point(26.0,13.0,5),new Point(27.0,14.0,5),new Point(27.0,15.0,5),new Point(26.0,16.0,5),new Point(25.0,16.0,5),new Point(24.0,17.0,5),new Point(22.0,17.0,5),new Point(21.0,16.0,5),new Point(21.0,13.0,5),new Point(36.0,5.0,6),new Point(40.0,21.0,6),new Point(40.0,13.0,6),new Point(39.0,12.0,6),new Point(39.0,10.0,6),new Point(38.0,9.0,6),new Point(38.0,8.0,6),new Point(36.0,6.0,6)
			 )), mNumPoints));
		 
		 mPntClouds.add(new PointCloud("handbreak-1", new
			 ArrayList<Point>(Arrays
			 .asList(
					 new Point(3.0,10.0,1),new Point(35.0,31.0,1),new Point(36.0,31.0,1),new Point(33.0,28.0,1),new Point(30.0,31.0,1),new Point(28.0,31.0,1),new Point(27.0,32.0,1),new Point(20.0,32.0,1),new Point(19.0,31.0,1),new Point(17.0,31.0,1),new Point(16.0,30.0,1),new Point(15.0,30.0,1),new Point(13.0,28.0,1),new Point(12.0,28.0,1),new Point(10.0,30.0,1),new Point(10.0,31.0,1),new Point(8.0,31.0,1),new Point(6.0,29.0,1),new Point(6.0,28.0,1),new Point(5.0,27.0,1),new Point(5.0,26.0,1),new Point(4.0,25.0,1),new Point(4.0,23.0,1),new Point(3.0,22.0,1),new Point(3.0,11.0,1),new Point(4.0,9.0,1),new Point(4.0,8.0,1),new Point(5.0,7.0,1),new Point(5.0,6.0,1),new Point(6.0,5.0,1),new Point(6.0,4.0,1),new Point(8.0,2.0,1),new Point(9.0,2.0,1),new Point(12.0,5.0,1),new Point(14.0,5.0,1),new Point(16.0,3.0,1),new Point(17.0,3.0,1),new Point(18.0,2.0,1),new Point(28.0,2.0,1),new Point(29.0,3.0,1),new Point(30.0,3.0,1),new Point(31.0,4.0,1),new Point(32.0,4.0,1),new Point(33.0,5.0,1),new Point(34.0,5.0,1),new Point(35.0,4.0,1),new Point(35.0,3.0,1),new Point(36.0,3.0,1),new Point(37.0,2.0,1),new Point(40.0,5.0,1),new Point(40.0,6.0,1),new Point(42.0,8.0,1),new Point(42.0,9.0,1),new Point(43.0,10.0,1),new Point(43.0,14.0,1),new Point(44.0,15.0,1),new Point(44.0,17.0,1),new Point(43.0,18.0,1),new Point(43.0,23.0,1),new Point(42.0,24.0,1),new Point(42.0,25.0,1),new Point(41.0,26.0,1),new Point(41.0,27.0,1),new Point(39.0,29.0,1),new Point(39.0,30.0,1),new Point(38.0,31.0,1),new Point(37.0,31.0,1),new Point(11.0,14.0,2),new Point(12.0,13.0,2),new Point(12.0,12.0,2),new Point(13.0,11.0,2),new Point(13.0,10.0,2),new Point(17.0,6.0,2),new Point(19.0,6.0,2),new Point(20.0,5.0,2),new Point(27.0,5.0,2),new Point(29.0,7.0,2),new Point(30.0,7.0,2),new Point(33.0,10.0,2),new Point(33.0,11.0,2),new Point(34.0,12.0,2),new Point(34.0,13.0,2),new Point(35.0,14.0,2),new Point(35.0,20.0,2),new Point(34.0,21.0,2),new Point(34.0,22.0,2),new Point(33.0,23.0,2),new Point(33.0,24.0,2),new Point(30.0,27.0,2),new Point(29.0,27.0,2),new Point(28.0,28.0,2),new Point(26.0,28.0,2),new Point(25.0,29.0,2),new Point(21.0,29.0,2),new Point(20.0,28.0,2),new Point(18.0,28.0,2),new Point(17.0,27.0,2),new Point(16.0,27.0,2),new Point(14.0,25.0,2),new Point(14.0,24.0,2),new Point(12.0,22.0,2),new Point(12.0,21.0,2),new Point(11.0,20.0,2),new Point(11.0,15.0,2),new Point(20.0,24.0,3),new Point(21.0,23.0,3),new Point(24.0,23.0,3),new Point(25.0,24.0,3),new Point(25.0,25.0,3),new Point(24.0,26.0,3),new Point(23.0,26.0,3),new Point(22.0,27.0,3),new Point(20.0,25.0,3),new Point(21.0,7.0,4),new Point(24.0,7.0,4),new Point(24.0,20.0,4),new Point(21.0,20.0,4),new Point(21.0,8.0,4)
			 )), mNumPoints));
		 
		 mPntClouds.add(new PointCloud("ABS-1", new
		 ArrayList<Point>(Arrays
		 .asList(
				 new Point(2.0,14.0,1),new Point(3.0,13.0,1),new Point(3.0,9.0,1),new Point(4.0,8.0,1),new Point(4.0,7.0,1),new Point(5.0,6.0,1),new Point(5.0,5.0,1),new Point(7.0,3.0,1),new Point(7.0,2.0,1),new Point(9.0,2.0,1),new Point(12.0,5.0,1),new Point(13.0,5.0,1),new Point(15.0,3.0,1),new Point(16.0,3.0,1),new Point(17.0,2.0,1),new Point(19.0,2.0,1),new Point(20.0,1.0,1),new Point(25.0,1.0,1),new Point(26.0,2.0,1),new Point(29.0,2.0,1),new Point(30.0,3.0,1),new Point(31.0,3.0,1),new Point(33.0,5.0,1),new Point(34.0,5.0,1),new Point(35.0,4.0,1),new Point(35.0,3.0,1),new Point(36.0,2.0,1),new Point(37.0,2.0,1),new Point(40.0,5.0,1),new Point(40.0,6.0,1),new Point(41.0,7.0,1),new Point(41.0,8.0,1),new Point(42.0,9.0,1),new Point(42.0,11.0,1),new Point(43.0,12.0,1),new Point(43.0,21.0,1),new Point(42.0,22.0,1),new Point(42.0,24.0,1),new Point(40.0,26.0,1),new Point(40.0,27.0,1),new Point(39.0,28.0,1),new Point(39.0,29.0,1),new Point(37.0,31.0,1),new Point(36.0,31.0,1),new Point(35.0,30.0,1),new Point(35.0,29.0,1),new Point(34.0,28.0,1),new Point(33.0,28.0,1),new Point(32.0,29.0,1),new Point(31.0,29.0,1),new Point(30.0,30.0,1),new Point(29.0,30.0,1),new Point(28.0,31.0,1),new Point(25.0,31.0,1),new Point(24.0,32.0,1),new Point(22.0,32.0,1),new Point(21.0,31.0,1),new Point(18.0,31.0,1),new Point(17.0,30.0,1),new Point(16.0,30.0,1),new Point(15.0,29.0,1),new Point(14.0,29.0,1),new Point(13.0,28.0,1),new Point(12.0,29.0,1),new Point(11.0,29.0,1),new Point(9.0,31.0,1),new Point(5.0,27.0,1),new Point(5.0,25.0,1),new Point(4.0,24.0,1),new Point(4.0,23.0,1),new Point(3.0,22.0,1),new Point(3.0,19.0,1),new Point(2.0,18.0,1),new Point(2.0,15.0,1),new Point(7.0,30.0,2),new Point(11.0,12.0,3),new Point(12.0,11.0,3),new Point(12.0,10.0,3),new Point(16.0,6.0,3),new Point(17.0,6.0,3),new Point(18.0,5.0,3),new Point(19.0,5.0,3),new Point(20.0,4.0,3),new Point(25.0,4.0,3),new Point(26.0,5.0,3),new Point(28.0,5.0,3),new Point(33.0,10.0,3),new Point(33.0,11.0,3),new Point(34.0,12.0,3),new Point(34.0,13.0,3),new Point(35.0,14.0,3),new Point(35.0,18.0,3),new Point(34.0,19.0,3),new Point(34.0,21.0,3),new Point(33.0,22.0,3),new Point(33.0,23.0,3),new Point(29.0,27.0,3),new Point(28.0,27.0,3),new Point(27.0,28.0,3),new Point(23.0,28.0,3),new Point(22.0,29.0,3),new Point(21.0,29.0,3),new Point(20.0,28.0,3),new Point(18.0,28.0,3),new Point(12.0,22.0,3),new Point(12.0,21.0,3),new Point(11.0,20.0,3),new Point(11.0,13.0,3),new Point(13.0,19.0,4),new Point(17.0,14.0,4),new Point(15.0,16.0,4),new Point(15.0,17.0,4),new Point(14.0,18.0,4),new Point(14.0,16.0,4),new Point(16.0,14.0,4),new Point(18.0,16.0,4),new Point(14.0,20.0,4),new Point(15.0,14.0,5),new Point(17.0,16.0,6),new Point(17.0,19.0,6),new Point(18.0,19.0,6),new Point(18.0,17.0,6),new Point(19.0,19.0,7),new Point(21.0,14.0,8),new Point(22.0,14.0,8),new Point(22.0,15.0,8),new Point(23.0,14.0,8),new Point(26.0,17.0,8),new Point(26.0,19.0,8),new Point(23.0,16.0,8),new Point(23.0,17.0,8),new Point(22.0,18.0,8),new Point(23.0,19.0,8),new Point(22.0,20.0,8),new Point(21.0,19.0,8),new Point(21.0,15.0,8),new Point(23.0,20.0,9),new Point(24.0,19.0,9),new Point(25.0,19.0,9),new Point(24.0,20.0,9),new Point(24.0,14.0,10),new Point(26.0,15.0,10),new Point(25.0,14.0,10),new Point(28.0,14.0,11),new Point(29.0,15.0,11),new Point(29.0,14.0,11),new Point(31.0,14.0,11),new Point(32.0,15.0,11),new Point(32.0,14.0,11),new Point(29.0,17.0,11),new Point(28.0,16.0,11),new Point(28.0,15.0,11),new Point(28.0,18.0,12),new Point(32.0,18.0,12),new Point(31.0,19.0,12),new Point(31.0,20.0,12),new Point(33.0,18.0,12),new Point(32.0,17.0,12),new Point(31.0,17.0,12),new Point(31.0,16.0,12),new Point(29.0,18.0,12),new Point(30.0,19.0,12),new Point(29.0,20.0,12),new Point(28.0,19.0,12)
		 )), mNumPoints));
		 
		// mPntClouds.add(new PointCloud("database16", new
			// ArrayList<Point>(Arrays
			// .asList(
			//
			// )), mNumPoints));
		 
		// mPntClouds.add(new PointCloud("database16", new
			// ArrayList<Point>(Arrays
			// .asList(
			//
			// )), mNumPoints));

	}
}
