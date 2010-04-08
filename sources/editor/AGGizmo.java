package AGTEdit; import javax.swing.*; 
public class AGGizmo extends AGObject implements AGDimension { 
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
	private DefaultListModel events = new DefaultListModel();
	
	public DefaultListModel getEvents() { return events; }
	private int flags = 0;
	AGSprite sprite = new AGSprite();

	AGGizmo() { super(); name = "Gizmo-" + id; setW(sprite.getW()); setH(sprite.getH()); }

	public void setFlags(int n) { flags = n; }
	public int getFlags() { return flags; }

	public void setSpriteFlag(boolean f) { flags = flags & 6 | ((f) ? 1: 0); }
	public void setVisibleFlag(boolean f) { flags = flags & 5 | ((f) ? 2: 0); }
	public void setActiveFlag(boolean f) { flags = flags & 3 | ((f) ? 4: 0); }

	public boolean getSpriteFlag() { return (flags & 1) != 0; }
	public boolean getVisibleFlag() { return (flags & 2) != 0; }
	public boolean getActiveFlag() { return (flags & 4) != 0; }

	public void toggleSpriteFlag() { flags ^= 1; }
	public void toggleVisibleFlag() { flags ^= 2; }
	public void toggleActiveFlag() { flags ^= 4; }

	public void setSprite(AGSprite o) { sprite = o; }
	public AGSprite getSprite() { return sprite; }

	public void reFit() { setDim(sprite.getW(), sprite.getH()); }

	public int getType() { return GIZMO; }
} 
