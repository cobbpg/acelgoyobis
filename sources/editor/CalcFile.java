package AGTEdit;

import java.io.*;

public class CalcFile {

	public static final int TI82 = 0;
	public static final int TI83 = 1;
	public static final int TI83P = 2;

	public static boolean save(byte[] data, int type, String name, String cname) {
		byte[] buf;
		int ofs = 0;
		int size = data.length;

		switch(type) {
			case TI82: ofs = 72; break;
			case TI83: ofs = 72; break;
			case TI83P: ofs = 74; break;
		}
		buf = new byte[size + ofs + 2];
		System.arraycopy(data, 0, buf, ofs, size);

		try {
			System.arraycopy(new String("**TI83**").getBytes("iso-8859-1"), 0, buf, 0, 8);
			if (type == TI82) buf[5] = '2';
			if (type == TI83P) buf[6] = 'F';
			buf[8] = 26;
			buf[9] = 10;
			System.arraycopy(new String("File created with AGTEdit").getBytes("iso-8859-1"), 0, buf, 11, 25);
			buf[53] = (byte)(size + ofs - 55);
			buf[54] = (byte)((size + ofs - 55) >> 8);
			buf[55] = (byte)(ofs - 61);	// Header length
			buf[57] = (byte)(size + 2);
			buf[58] = (byte)((size + 2) >> 8);
			buf[59] = 6;	// Protected program
			System.arraycopy(cname.getBytes("iso-8859-1"), 0, buf, 60, cname.length());
			buf[68] = 1;	// Flag for the 83+ (overwritten by others)
			buf[ofs - 4] = (byte)(size + 2);
			buf[ofs - 3] = (byte)((size + 2) >> 8);
			buf[ofs - 2] = (byte)size;
			buf[ofs - 1] = (byte)(size >> 8);
	
			// Checksum (from the header length to the end of the data)
			int chksum = 0;
			for(int i = 55; i < size + ofs; i++)
				if(buf[i] >= 0) chksum += buf[i];
				else chksum += buf[i] + 256;
			buf[size + ofs] = (byte)chksum;
			buf[size + ofs + 1] = (byte)(chksum >> 8);

		    	OutputStream fout = new FileOutputStream(name);
			fout.write(buf);
			fout.close();
		} catch (Exception e) { return false; }
		return true;
	}

	public static String convertName(String name) {
		String ret = "CALCFILE";
		try {
			byte[] nbuf = name.getBytes("iso-8859-1");
			int ns = 0;
			for(int i = 0; i < nbuf.length; i++) if (nbuf[i] > 32) ns++;
			if (ns > 8) ns = 8;
			byte[] nbuf2 = new byte[ns];
			int j = 0;
			for(int i = 0; j < nbuf2.length; i++) if (nbuf[i] > 32) nbuf2[j++] = nbuf[i];
			ret = new String(nbuf2, "iso-8859-1");
		} catch (Exception e) { }
		return ret;
	}

}

