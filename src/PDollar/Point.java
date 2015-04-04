package PDollar;

public class Point {
	public double X;
	public double Y;
	public int ID;

	public Point(double x, double y, int id)
		{ X = x; Y = y; ID = id; }

	@Override
	public boolean equals(Object arg0) {
		
		if(arg0 instanceof Point){
			Point right = (Point)arg0;
			if(this.X==right.X&&this.Y==right.Y&&this.ID==right.ID){
				return true;
			}
		}
		return false;
	}
	
	public static boolean inOneLine(Point a,Point b,Point c){
		if((c.Y-b.Y)*(c.X-a.X)-(c.Y-a.Y)*(c.X-b.X)==0){
			return true;
		}
		return false;
	}
	
	
}
