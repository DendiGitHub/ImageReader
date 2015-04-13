package PDollar;

import java.util.ArrayList;

public class ListSimplify {

	public static ArrayList<Point> simplify(ArrayList<Point> before) {

		int length = before.size();
		for (int i = 0; i < length - 2;) {
//			if(before.get(i).ID!=before.get(i+1).ID && before.get(i+1).ID!=before.get(i+2).ID){
//				before.remove(i+1);
//				length--;
//				continue;
//			}
			if (before.get(i + 1).ID == before.get(i).ID
					&& before.get(i + 1).ID == before.get(i + 2).ID) {
				if (Point.inOneLine(before.get(i), before.get(i + 1),
						before.get(i + 2))) {
					before.remove(i + 1);
					length--;
					continue;
				}
			}
			i++;
		}
		
		

		return before;
	}
}
