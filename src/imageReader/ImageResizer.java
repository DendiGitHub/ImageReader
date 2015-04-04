package imageReader;

public class ImageResizer {
	int originalHeight;
	int originalWidth;
	private int[][] grayTable;
	int height;
	int width;

	public ImageResizer(int[][] grayTable, int originalHeight,
			int originalWidth, int height, int width) {
		this.originalHeight = originalHeight;
		this.originalWidth = originalWidth;
		this.grayTable = grayTable;
		this.height = height;
		this.width = width;
	}

	public int[][] getImageResizer() {
		int[][] result = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				
				result [i][j] = getPoint(i,j) ;
			}
		}

		return result;
	}

	private int getPoint(int x, int y) {
		int result = 0;
		
		int X = (int)((double)x)*originalWidth/width;
		int Y = (int)((double)y)*originalHeight/height;
		double X_ = ((double)x)*originalWidth/width-X;
		double Y_ = ((double)y)*originalHeight/height-Y;
		
		int XP1Y;
		int XYP1;
		int XP1YP1;
		
		if(X==originalWidth-1||Y==originalHeight-1){
//			XP1YP1 = grayTable[X][Y];
			XP1Y = grayTable[Math.min(originalWidth-1, X+1)][Y];
			XYP1 = grayTable[X][Math.min(originalHeight-1, Y+1)];
			XP1YP1 = grayTable[Math.min(originalWidth-1, X+1)][Math.min(originalHeight-1, Y+1)];
		}else{
			XP1Y = grayTable[X+1][Y];
			XYP1 = grayTable[X][Y+1];
			XP1YP1 = grayTable[X+1][Y+1];
			
		}
//		if(X==originalWidth-1){
//			XP1Y = grayTable[X][Y];
//		}else{
//			XP1Y = grayTable[X+1][Y];
//		}
//		if(Y==originalHeight-1){
//			XYP1 = grayTable[X][Y];
//		}else{
//			XYP1 = grayTable[X][Y+1];
//		}
		
		
//		System.out.println("("+x+","+y+")"+"|X="+X + "|Y=" + Y + "|X_="+X_ + " |y_=" + Y_ );
		
//		result = (int)((1-X_-Y_)*grayTable[X][Y]+
//				(Y_-2*X_*Y_)*grayTable[X][Y+1]+
//				(X_-2*X_*Y_)*grayTable[X+1][Y]+
//				2*X_*Y_*grayTable[X+1][Y+1]);
		result = (int)((1-X_)*(1-Y_)*grayTable[X][Y]
				+(1-X_)*Y_*XYP1
						+X_*(1-Y_)*XP1Y
								+X_*Y_*XP1YP1);
//		System.out.println("result"+result);
		return result;
	}

}
