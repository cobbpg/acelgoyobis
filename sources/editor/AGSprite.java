package AGTEdit;

public class AGSprite {

	private byte[] pic = new byte[64];

	AGSprite() {
		// A little x
		pic[0] = pic[2] = pic[9] = pic[16] = pic[18] = 1;
	}

	public void set(int x, int y, byte c) { pic[8 * (y & 7) + (x & 7)] = c; }
	public void set(byte[] p) { pic = p; }
	public byte[] get() { return pic; }

	public int getW() {
		int i, j;

		for (i = 7; i >= 0; i--)
			for (j = 0; j < 8; j++)
				if (pic[8 * j + i] != 0) return i;

		return 0;
	}

	public int getH() {
		int i, j;

		for (i = 7; i >= 0; i--)
			for (j = 0; j < 8; j++)
				if (pic[8 * i + j] != 0) return i;

		return 0;
	}

}

