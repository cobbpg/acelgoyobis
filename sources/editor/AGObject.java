package AGTEdit;

public class AGObject {

	public static final int OBJECT = 0;
	public static final int GIZMO = 1;
	public static final int TIMER = 2;
	public static final int COUNTER = 3;
	public static final int BOUNCE = 4;
	public static final int FLIPPER = 5;
	public static final int COMMENT = 6;

	private static int newid = 0;
	private static AGObject dummy;
	protected String name;
	protected int id;
	protected int binid;

	AGObject() { id = getNewID(); }

	public void setName(String s) { name = s; }
	public String getName() { return name; }
	public String toString() { return name; }

	public int getID() { return id; }
	public int getType() { return OBJECT; }

	public void setBinID(int n) { binid = n; }
	public int getBinID() { return binid; }

	protected static int getNewID() { return newid++; }

	public static void setDummy(AGObject o) { dummy = o; }
	public static AGObject getDummy() { return dummy; }

}

