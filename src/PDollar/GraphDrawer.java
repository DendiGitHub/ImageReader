package PDollar;

import java.util.ArrayList;
import java.util.Stack;

public class GraphDrawer {
	private boolean[][] binaryTable;
	private boolean[][] border;
	private int width;
	private int height;
	private ArrayList<Point> pointList;
	private Stack<Point> routeStack;

	public GraphDrawer(boolean[][] binaryTable, int width, int height) {
		this.binaryTable = binaryTable;
		this.width = width;
		this.height = height;
		routeStack = new Stack<Point>();
		pointList = new ArrayList<Point>();
		
		
		border = findBorder();
		
	}
	

	public ArrayList<Point> getPointList(){
		getStroke();
		pointList = ListSimplify.simplify(pointList);
		return pointList;
	}


	//get border stored in boolean[][] border
	public boolean[][] findBorder() {
		// add border
		border = new boolean[width][height];
		//boundary
		for(int i=0;i<width;i++){
			if(binaryTable[i][0]){
				border[i][0] = true;
			}
			if(binaryTable[i][height-1]){
				border[i][height-1] = true;
			}
		}
		for(int j=0;j<height;j++){
			if(binaryTable[0][j]){
				border[0][j]=true;
			}
			if(binaryTable[width-1][j]){
				border[width-1][j]=true;
			}
		}
		
		//inside
		for (int i = 1; i < width - 1; i++) {
			for (int j = 1; j < height - 1; j++) {
				border[i][j] = false;
				if (binaryTable[i][j]) {
					if (binaryTable[i + 1][j] && binaryTable[i - 1][j]
							&& binaryTable[i][j - 1] && binaryTable[i][j + 1]) {
					} else {
						border[i][j] = true;
					}
				}
			}
		}
		return border;
	}

	
//	get stroke stored in pointList
	public void getStroke() {
		int flag = 1;
		while (flag > 0) {
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (border[i][j]) {
						pointList.add(new Point(i,j,flag));
						border[i][j] = false;
						
						getLinkedLine(i,j,flag);
						flag++;
						continue;
					}
				}
			}
			flag=0;
		}
	}
	
	
	
	//add route to Stack routeStack
	private void getLinkedLine(int i,int j,int flag){
		int primitiveI = i;
		int primitiveJ = j;
		
		boolean whileFlag = true;
		while(whileFlag){
			whileFlag = false;
		while((j+1)<height&&border[i][j+1]){
			routeStack.add(new Point(i,j+1,flag));
			border[i][j+1] = false;
			j=j+1;
			whileFlag = true;
		}
		while((j-1)>0&&border[i][j-1]){
			routeStack.add(new Point(i,j-1,flag));
			border[i][j-1] = false;
			j=j-1;
			whileFlag = true;
		}
		while((i-1)>0&&border[i-1][j]){
			routeStack.add(new Point(i-1,j,flag));
			border[i-1][j] = false;
			i=i-1;
			whileFlag = true;
		}
		while((i+1)<width&&border[i+1][j]){
			routeStack.add(new Point(i+1,j,flag));
			border[i+1][j] = false;
			i=i+1;
			whileFlag = true;
		}
		while((i+1)<width&&(j+1)<height&&border[i+1][j+1]){
			routeStack.add(new Point(i+1,j+1,flag));
			border[i+1][j+1] = false;
			i=i+1;
			j=j+1;
			whileFlag = true;
		}
		while((i+1)<width&&(j-1)>0&&border[i+1][j-1]){
			routeStack.add(new Point(i+1,j-1,flag));
			border[i+1][j-1] = false;
			i=i+1;
			j=j-1;
			whileFlag = true;
		}
		while((i-1)>0&&(j-1)>0&&border[i-1][j-1]){
			routeStack.add(new Point(i-1,j-1,flag));
			border[i-1][j-1] = false;
			i=i-1;
			j=j-1;
			whileFlag = true;
		}
		while((i-1)>0&&(j+1)<height&&border[i-1][j+1]){
			routeStack.add(new Point(i-1,j+1,flag));
			border[i-1][j+1] = false;
			i=i-1;
			j=j+1;
			whileFlag = true;
		}
		}
		popStack();
		findLinkedPoint(primitiveI,primitiveJ,flag);
	}
	
//	pop the whole stack;
	private void popStack(){
		while(!routeStack.empty()){
			pointList.add(routeStack.pop());
		}
	}

//	find if point beside (i,j) is a border,then add them into the list
	private void findLinkedPoint(int i, int j, int flag) {
		if((j+1)<height&&border[i][j+1]){
			pointList.add(new Point(i,j+1,flag));
			border[i][j+1] = false;
			findLinkedPoint(i,j+1,flag);
			return;
		}
		if((j-1)>0&&border[i][j-1]){
			pointList.add(new Point(i,j-1,flag));
			border[i][j-1] = false;
			findLinkedPoint(i,j-1,flag);
			return;
		}
		if((i-1)>0&&border[i-1][j]){
			pointList.add(new Point(i-1,j,flag));
			border[i-1][j] = false;
			findLinkedPoint(i-1,j,flag);
			return;
		}
		if((i+1)<width&&border[i+1][j]){
			pointList.add(new Point(i+1,j,flag));
			border[i+1][j] = false;
			findLinkedPoint(i+1,j,flag);
			return;
		}
		if((i+1)<width&&(j+1)<height&&border[i+1][j+1]){
			pointList.add(new Point(i+1,j+1,flag));
			border[i+1][j+1] = false;
			findLinkedPoint(i+1,j+1,flag);
			return;
		}
		if((i+1)<width&&(j-1)>0&&border[i+1][j-1]){
			pointList.add(new Point(i+1,j-1,flag));
			border[i+1][j-1] = false;
			findLinkedPoint(i+1,j-1,flag);
			return;
		}
		if((i-1)>0&&(j-1)>0&&border[i-1][j-1]){
			pointList.add(new Point(i-1,j-1,flag));
			border[i-1][j-1] = false;
			findLinkedPoint(i-1,j-1,flag);
			return;
		}
		if((i-1)>0&&(j+1)<height&&border[i-1][j+1]){
			pointList.add(new Point(i-1,j+1,flag));
			border[i-1][j+1] = false;
			findLinkedPoint(i-1,j+1,flag);
			return;
		}
		return;
	}
	
	public boolean[][] getBorder(){
		return border;
	}

}
