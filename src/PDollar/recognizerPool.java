package PDollar;

public class recognizerPool {
	
	static PDollarRecognizer recognizer;
	
	recognizerPool(){
		recognizer = new PDollarRecognizer();
	}
	
	public static PDollarRecognizer getRecognizer(){
		return recognizer;
	}
}
