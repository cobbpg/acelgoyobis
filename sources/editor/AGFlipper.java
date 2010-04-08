package AGTEdit; 
public class AGFlipper extends AGObject implements AGDimension { 
	private int x, y, w, h;

	public void setX(int n) { x = (n + w >= AGTable.XSIZE) ? x : n; x = (x >= 0) ? x : 0; }
	public int getX() { return x; }

	public void setY(int n) { y = (n + h >= AGTable.YSIZE) ? y : n; y = (y >= 0) ? y : 0; }
	public int getY() { return y; }

	public void setW(int n) { w = (n + x >= AGTable.XSIZE) ? w : n; w = (w >= 0) ? w : 0; }
	public int getW() { return w; }

	public void setH(int n) { h = (n + y >= AGTable.YSIZE) ? h : n; h = (h >= 0) ? h : 0; }
	public int getH() { return h; }
	
	public void setPos(int nx, int ny) { setX(nx); setY(ny); }
	public void setDim(int nw, int nh) { setW(nw); setH(nh); }

	public void addPos(int dx, int dy) { setX(x + dx); setY(y + dy); }

	public String toString() { return name + " [" + x + " " + y + " "+ w + " "+ h + "]"; }
	public static final int LEFT = 0;
	public static final int RIGHT = 1;

	private int dir;

	AGFlipper() { super(); name = "Flipper-" + id; setW(15); setH(7); }

	public void setDir(int n) { dir = n & 1; }
	public void toggleDir() { dir ^= 1; }

	public int getDir() { return dir; }
	public String getDirString() {
		switch(dir) {
			case LEFT: return "Left";
			case RIGHT: return "Right";
		}
		return "ERROR";
	}

	public int getType() { return FLIPPER; }
} 
