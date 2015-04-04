package PDollar;

public class RecognizerResults {
	public String mName;
	public double mScore;
	public String mOtherInfo;

	public RecognizerResults(String name, double score)
		{ mName = name; mScore = score; }
	public RecognizerResults(String name, double score, String otherInfo) 
		{ mName = name; mScore = score; mOtherInfo = otherInfo; }
}
