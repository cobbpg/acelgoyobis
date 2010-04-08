package AGTEdit;

public class AGBrush {

	public static final int SIZE = 5;
	public static final int NONE = 0;
	public static final int BRUSH1 = 1;
	public static final int BRUSH2 = 2;
	public static final int BRUSH3 = 3;
	public static final int BRUSH4 = 4;

	private static final byte[][] brushes = {
		{ 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0 },
		{ 0,0,0,0,0, 0,0,0,0,0, 0,0,1,0,0, 0,0,0,0,0, 0,0,0,0,0 },
		{ 0,0,0,0,0, 0,0,1,0,0, 0,1,1,1,0, 0,0,1,0,0, 0,0,0,0,0 },
		{ 0,0,1,0,0, 0,1,1,1,0, 1,1,1,1,1, 0,1,1,1,0, 0,0,1,0,0 },
		{ 1,1,1,1,1, 1,1,1,1,1, 1,1,1,1,1, 1,1,1,1,1, 1,1,1,1,1 }
	};

	public static byte[] getBrush(int n) { return brushes[n]; }

}

