package AGTEdit;

public class AGEvent {

	public static final int INVALID = 0;
	public static final int SCORE = 1;
	public static final int BONUS = 2;
	public static final int SETFLAGS = 3;
	public static final int RESETFLAGS = 4;
	public static final int ACTIVATE = 5;
	public static final int HIDE = 6;
	public static final int SETCOUNTER = 7;
	public static final int DECREASE = 8;
	public static final int SETTIMER = 9;

	private static final String[] names = {
		"[NONE]", "Score ", "Bonus", "SetFlags ", "ResetFlags ", "Activate ", "Hide ", "SetCounter ", "Decrease ", "SetTimer "
	};

	private static final int[] types = {
		AGObject.OBJECT, AGObject.OBJECT, AGObject.OBJECT,
		AGObject.GIZMO, AGObject.GIZMO, AGObject.GIZMO, AGObject.GIZMO,
		AGObject.COUNTER, AGObject.COUNTER, AGObject.TIMER
	};

	private int type = INVALID;
	private int val1;
	private int val2;
	private AGObject obj;

	public void setType(int n) { type = n; if (type != SCORE) val1 &= 255; }
	public int getType() { return type; }

	public void setVal(int n) { val1 = n & ((type == SCORE) ? 65535 : 255); }
	public int getVal() { return val1; }

	public void setBonus(int n) { val2 = n & 65535; }
	public int getBonus() { return val2; }

	public void setObject(AGObject o) { obj = o; }
	public AGObject getObject() { return obj; }

	public int getObjectType() { return types[type]; }
	public static int getObjectType(int t) { return types[t]; }

	public String getName() { return names[type]; }
	public String toString() {
		String oname = "[NO OBJECT SET]";
		if (obj != null) oname = obj.getName();

		switch (type) {
			case SCORE: return names[type] + val1 + ", " + val2;
			case BONUS: return names[type];
			case ACTIVATE:
			case HIDE:
			case DECREASE: return names[type] + oname;
			case SETFLAGS:
			case RESETFLAGS:
			case SETCOUNTER:
			case SETTIMER: return names[type] + oname + ", " + val1;
		}
		return "[PLEASE EDIT OR DELETE]";
	}

	public static String getName(int t) { return names[t]; }
	public static int getType(String n) {
		int t = 0;
		for(int i = 1; i < names.length; i++)
			if(n.trim().equals(names[i].trim())) t = i;
		return t;
	}
	public static int count() { return names.length; }

}

