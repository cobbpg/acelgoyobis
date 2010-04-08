package AGTEdit;

public interface AGDimension {

	public void setX(int n);
	public int getX();

	public void setY(int n);
	public int getY();

	public void setW(int n);
	public int getW();

	public void setH(int n);
	public int getH();

	public void setPos(int nx, int ny);
	public void setDim(int nw, int nh);

	public void addPos(int dx, int dy);

}

