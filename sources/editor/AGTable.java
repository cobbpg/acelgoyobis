package AGTEdit;

import java.io.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class AGTable {

	public static final int XSIZE = 96;
	public static final int YSIZE = 160;
	private String name;
	private byte[] table;
	private DefaultListModel objects = new DefaultListModel();

	public void setName(String s) { name = s; }
	public String getName() { return name; }

	public void add(AGObject o) { objects.addElement(o); }
	public void remove(AGObject o) { objects.removeElement(o); }
	public void remove(int n) { objects.removeElementAt(n); }
	public void removeAllObjects() { objects.removeAllElements(); }
	public void insert(AGObject o, int n) { objects.insertElementAt(o, n); }
	public int getCount() { return objects.size(); }
	public AGObject get(int n) { return (AGObject) objects.elementAt(n); }
	public DefaultListModel getObjects() { return objects; }

	public Vector getObjects(int t) {
		Vector vec = new Vector(10, 10);
		int i;

		vec.add(AGObject.getDummy());
		for(i = 0; i < objects.size(); i++)
			if (((AGObject)objects.elementAt(i)).getType() == t) vec.add(objects.elementAt(i));

		return vec;
	}

	public Vector getCleanObjects(int t) {
		Vector vec = new Vector(10, 10);
		int i;

		for(i = 0; i < objects.size(); i++)
			if (((AGObject)objects.elementAt(i)).getType() == t) vec.add(objects.elementAt(i));

		return vec;
	}

	public String[] getNames() {
		AGObject[] os = (AGObject[]) objects.toArray();
		String[] ons = new String[os.length];
		for (int i = 0; i < os.length; i++) ons[i] = os[i].getName();
		return ons;
	}

	public void setTable(byte[] data) { table = data; }
	public byte[] getTable() { return table; }

	public boolean open(String s) {

		class ObjectIDPair { public int id = 0; public AGObject o = AGObject.getDummy(); public Element e = null; }

		try {
			String newName;
			String newTable;
			Vector newObjects = new Vector(10, 10);

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			Element in = dbf.newDocumentBuilder().parse(s).getDocumentElement();
			if (!in.getTagName().equals("AGTable")) return false;
			newName = in.getElementsByTagName("Name").item(0).getFirstChild().getNodeValue();
			newTable = in.getElementsByTagName("TableData").item(0).getFirstChild().getNodeValue();
			NodeList objectData = in.getElementsByTagName("ObjectData").item(0).getChildNodes();
			newObjects.add(new ObjectIDPair());
			for(int i = 0; i < objectData.getLength(); i++) {
				if (objectData.item(i) instanceof Element) {
					Element objectNode = (Element)objectData.item(i);
					String objectType = objectNode.getNodeName();
					ObjectIDPair oip = new ObjectIDPair();
					oip.id = Integer.parseInt(objectNode.getAttributeNode("id").getValue());
					if (objectType.equals("Gizmo")) {
						AGGizmo o = new AGGizmo();
						oip.o = o;
						o.setName(objectNode.getAttributeNode("name").getValue());
						o.setX(Integer.parseInt(objectNode.getAttributeNode("x").getValue()));
						o.setY(Integer.parseInt(objectNode.getAttributeNode("y").getValue()));
						o.setW(Integer.parseInt(objectNode.getAttributeNode("w").getValue()));
						o.setH(Integer.parseInt(objectNode.getAttributeNode("h").getValue()));
						o.setFlags(Integer.parseInt(objectNode.getAttributeNode("flags").getValue()));
						o.getSprite().set(stringArray(objectNode.getElementsByTagName("Sprite").item(0).getFirstChild().getNodeValue()));
						o.getEvents().removeAllElements();
						oip.e = (Element)objectNode.getElementsByTagName("Events").item(0);
						newObjects.add(oip);
					} else
					if (objectType.equals("Timer")) {
						AGTimer o = new AGTimer();
						oip.o = o;
						o.setName(objectNode.getAttributeNode("name").getValue());
						o.setVal(Integer.parseInt(objectNode.getAttributeNode("value").getValue()));
						o.getEvents().removeAllElements();
						oip.e = (Element)objectNode.getElementsByTagName("Events").item(0);
						newObjects.add(oip);
					} else
					if (objectType.equals("Counter")) {
						AGCounter o = new AGCounter();
						oip.o = o;
						o.setName(objectNode.getAttributeNode("name").getValue());
						o.setVal(Integer.parseInt(objectNode.getAttributeNode("value").getValue()));
						o.getEvents().removeAllElements();
						oip.e = (Element)objectNode.getElementsByTagName("Events").item(0);
						newObjects.add(oip);
					} else
					if (objectType.equals("Bounce")) {
						AGBounce o = new AGBounce();
						oip.o = o;
						o.setName(objectNode.getAttributeNode("name").getValue());
						o.setX(Integer.parseInt(objectNode.getAttributeNode("x").getValue()));
						o.setY(Integer.parseInt(objectNode.getAttributeNode("y").getValue()));
						o.setW(Integer.parseInt(objectNode.getAttributeNode("w").getValue()));
						o.setH(Integer.parseInt(objectNode.getAttributeNode("h").getValue()));
						newObjects.add(oip);
					} else
					if (objectType.equals("Flipper")) {
						AGFlipper o = new AGFlipper();
						oip.o = o;
						o.setName(objectNode.getAttributeNode("name").getValue());
						o.setX(Integer.parseInt(objectNode.getAttributeNode("x").getValue()));
						o.setY(Integer.parseInt(objectNode.getAttributeNode("y").getValue()));
						o.setDir(Integer.parseInt(objectNode.getAttributeNode("dir").getValue()));
						newObjects.add(oip);
					}
					if (objectType.equals("Comment")) {
						AGComment o = new AGComment();
						oip.o = o;
						o.setName(objectNode.getAttributeNode("name").getValue());
						newObjects.add(oip);
					}
				}
			}
			for(int i = 0; i < newObjects.size(); i++) {
				ObjectIDPair oip = (ObjectIDPair)newObjects.elementAt(i);
				DefaultListModel oe = null;
				switch(oip.o.getType()) {
					case AGObject.GIZMO: oe = ((AGGizmo)oip.o).getEvents(); break;
					case AGObject.TIMER: oe = ((AGTimer)oip.o).getEvents(); break;
					case AGObject.COUNTER: oe = ((AGCounter)oip.o).getEvents(); break;
				}
				if (oe != null && oip.e.hasChildNodes()) {
					NodeList eventData = oip.e.getElementsByTagName("Event");
					for(int j = 0; j < eventData.getLength(); j++) {
						Element eventNode = (Element)eventData.item(j);
						AGEvent ae = new AGEvent();
						ae.setType(AGEvent.getType(eventNode.getAttributeNode("type").getValue()));
						ae.setVal(Integer.parseInt(eventNode.getAttributeNode("val").getValue()));
						ae.setBonus(Integer.parseInt(eventNode.getAttributeNode("bonus").getValue()));
						ae.setObject(AGObject.getDummy());
						for(int k = 0; k < newObjects.size(); k++) {
							ObjectIDPair ooip = (ObjectIDPair)newObjects.elementAt(k);
							if (ooip.id == Integer.parseInt(eventNode.getAttributeNode("object").getValue()))
								ae.setObject(ooip.o);
						}
						oe.addElement(ae);
					}
				}
			}

			name = newName;
			stringArray(newTable, table);
			objects.removeAllElements();
			for(int i = 1; i < newObjects.size(); i++) objects.addElement(((ObjectIDPair)newObjects.elementAt(i)).o);

		} catch (Exception e) { return false; }
		return true;
	}

	public boolean save(String s) {
		try {
		    OutputStream fout = new FileOutputStream(s);
			OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(fout), "UTF-8");
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
			out.write("<AGTable>\r\n");
			out.write("<Name>" + name + "</Name>\r\n");
			out.write("<ObjectData>\r\n");
			for(int i = 0; i < getCount(); i++) {
				AGObject o = get(i);
				switch(o.getType()) {
					case AGObject.GIZMO: {
						AGGizmo o2 = (AGGizmo)o;
						out.write("<Gizmo id=\"" + o.getID() + "\" name=\"" + o.getName() + "\"" +
							" x=\"" + o2.getX() + "\" y=\"" + o2.getY() + "\" w=\"" + o2.getW() + "\" h=\"" + o2.getH() + "\"" +
							" flags=\"" + o2.getFlags() + "\">\r\n");
						out.write("<Sprite>");
						byte[] spr = o2.getSprite().get();
						for(int j = 0; j < spr.length; j++) out.write(spr[j] + '0');
						out.write("</Sprite>\r\n");
						writeEventList(out, o2.getEvents());
						out.write("</Gizmo>\r\n");
					}
					break;
					case AGObject.TIMER: {
						AGTimer o2 = (AGTimer)o;
						out.write("<Timer id=\"" + o.getID() + "\" name=\"" + o.getName() + "\" value=\"" + o2.getVal() + "\">\r\n");
						writeEventList(out, o2.getEvents());
						out.write("</Timer>\r\n");
					}
					break;
					case AGObject.COUNTER: {
						AGCounter o2 = (AGCounter)o;
						out.write("<Counter id=\"" + o.getID() + "\" name=\"" + o.getName() + "\" value=\"" + o2.getVal() + "\">\r\n");
						writeEventList(out, o2.getEvents());
						out.write("</Counter>\r\n");
					}
					break;
					case AGObject.BOUNCE: {
						AGBounce o2 = (AGBounce)o;
						out.write("<Bounce id=\"" + o.getID() + "\" name=\"" + o.getName() + "\"" +
							" x=\"" + o2.getX() + "\" y=\"" + o2.getY() + "\" w=\"" + o2.getW() + "\" h=\"" + o2.getH() + "\"/>\r\n");
					}
					break;
					case AGObject.FLIPPER: {
						AGFlipper o2 = (AGFlipper)o;
						out.write("<Flipper id=\"" + o.getID() + "\" name=\"" + o.getName() + "\"" +
							" x=\"" + o2.getX() + "\" y=\"" + o2.getY() + "\" dir=\"" + o2.getDir() + "\"/>\r\n");
					}
					break;
					case AGObject.COMMENT: {
						AGComment o2 = (AGComment)o;
						out.write("<Comment id=\"" + o.getID() + "\" name=\"" + o.getName() + "\"/>\r\n");
					}
					break;
				}
			}
			out.write("</ObjectData>\r\n");
			out.write("<TableData>");
			for(int i = 0; i < table.length; i++) out.write(table[i] + '0');
			out.write("</TableData>\r\n");
			out.write("</AGTable>\r\n");
			out.flush();
			out.close();
		} catch (IOException e) { return false; }
		return true;
	}

	private void writeEventList(OutputStreamWriter out, DefaultListModel el) throws IOException {
		if (el.size() > 0) {
			out.write("<Events>\r\n");
			for(int i = 0; i < el.size(); i++) {
				AGEvent e = (AGEvent)el.elementAt(i);
				out.write("<Event type=\"" + e.getName().trim() + "\" " +
					"val=\"" + e.getVal() + "\" bonus=\"" + e.getBonus() + "\" " +
					"object=\"" + e.getObject().getID() + "\"/>\r\n");
			}
			out.write("</Events>\r\n");
		} else out.write("<Events/>\r\n");
	}

	private byte[] stringArray(String s) {
		byte[] arr = new byte[s.length()];
		for(int i = 0; i < s.length(); i++) arr[i] = (byte)Character.digit(s.charAt(i), 10);
		return arr;
	}

	private void stringArray(String s, byte[] arr) {
		for(int i = 0; i < s.length(); i++) arr[i] = (byte)Character.digit(s.charAt(i), 10);
	}

	public byte[] export() {
		byte[] sbuf = new byte[8192];
		byte[] rbuf;
		int i, j, k, l, so;
		Vector gizmoList, timerList, counterList, bounceList, flipperList;

		gizmoList = getCleanObjects(AGObject.GIZMO);
		timerList = getCleanObjects(AGObject.TIMER);
		counterList = getCleanObjects(AGObject.COUNTER);
		bounceList = getCleanObjects(AGObject.BOUNCE);
		flipperList = getCleanObjects(AGObject.FLIPPER);

		fillBinID(gizmoList);
		fillBinID(timerList);
		fillBinID(counterList);

		sbuf[0] = 83; sbuf[1] = 12; sbuf[2] = (byte)217; sbuf[3] = (byte)154;
		byte[] bname = new byte[1];
		try { bname = name.getBytes("iso-8859-1"); } catch (Exception e) { }
		for(i = 0; i < bname.length; i++) sbuf[i + 6] = bname[i];
		so = bname.length + 11;
		sbuf[so++] = (byte)gizmoList.size();
		sbuf[so++] = (byte)timerList.size();
		sbuf[so++] = (byte)counterList.size();
		sbuf[so++] = (byte)bounceList.size();
		sbuf[so++] = (byte)flipperList.size();
		for(i = 0; i < flipperList.size(); i++) {
			AGFlipper flp = (AGFlipper)flipperList.elementAt(i);
			sbuf[so++] = (byte)(flp.getX() + ((flp.getDir() == AGFlipper.RIGHT) ? 128 : 0));
			sbuf[so++] = (byte)flp.getY();
		}
		for(i = 0; i < AGTable.YSIZE; i++) {
			for (j = 0; j < AGTable.XSIZE >> 3; j++) {
				l = 0;
				for (k = 0; k < 8; k++) l |= (table[i * AGTable.XSIZE + (j << 3) + k] & 1) << (7 - k);
				sbuf[so++] = (byte)l;
			}
		}
		int[] slopes = generateSlopes();
		byte[] compressedSlopes = compressSlopes(slopes);
		sbuf[so++] = (byte)compressedSlopes.length;
		sbuf[so++] = (byte)(compressedSlopes.length >> 8);
		for(i = 0; i < compressedSlopes.length; i++) sbuf[so++] = compressedSlopes[i];
		l = so;
		so += 2;
		for(i = 0; i < gizmoList.size(); i++) {
			AGGizmo g = (AGGizmo)gizmoList.elementAt(i);
			sbuf[so++] = (byte)g.getFlags();
			sbuf[so++] = (byte)g.getX();
			sbuf[so++] = (byte)g.getY();
			sbuf[so++] = (byte)(g.getX() + g.getW());
			sbuf[so++] = (byte)(g.getY() + g.getH());
			if (g.getSpriteFlag()) {
				byte[] sd = g.getSprite().get();
				for(j = 0; j < 8; j++) {
					int sb = 0;
					for(k = 0; k < 8; k++)
						sb += sd[j * 8 + k] << (7 - k);
					sbuf[so++] = (byte)sb;
				}
			}
			byte[] binEvents = generateBinEvents(g.getEvents());
			for(j = 0; j < binEvents.length; j++) sbuf[so++] = binEvents[j];
			sbuf[so++] = 0;
		}
		for(i = 0; i < timerList.size(); i++) {
			AGTimer g = (AGTimer)timerList.elementAt(i);
			sbuf[so++] = (byte)g.getVal();
			byte[] binEvents = generateBinEvents(g.getEvents());
			for(j = 0; j < binEvents.length; j++) sbuf[so++] = binEvents[j];
			sbuf[so++] = 0;
		}
		for(i = 0; i < counterList.size(); i++) {
			AGCounter g = (AGCounter)counterList.elementAt(i);
			sbuf[so++] = (byte)g.getVal();
			byte[] binEvents = generateBinEvents(g.getEvents());
			for(j = 0; j < binEvents.length; j++) sbuf[so++] = binEvents[j];
			sbuf[so++] = 0;
		}
		for(i = 0; i < bounceList.size(); i++) {
			AGBounce g = (AGBounce)bounceList.elementAt(i);
			sbuf[so++] = (byte)g.getX();
			sbuf[so++] = (byte)g.getY();
			sbuf[so++] = (byte)(g.getX() + g.getW());
			sbuf[so++] = (byte)(g.getY() + g.getH());
		}
		k = so - l - 2;
		sbuf[l++] = (byte)k;
		sbuf[l] = (byte)(k >> 8);
		// Checking for a limitation of the game
		if (k + gizmoList.size() * 2 + 1 + timerList.size() * 2 + 1 + counterList.size() * 2 + 1 > 1000) sbuf[0] = 0;
		k = so - 6;
		sbuf[4] = (byte)k;
		sbuf[5] = (byte)(k >> 8);
		k = 0;
		for(i = 6; i < so; i++)
			if (sbuf[i] >= 0) k += sbuf[i];
			else k += sbuf[i] + 256;
		sbuf[so++] = (byte)k;
		sbuf[so++] = (byte)(k >> 8);
		rbuf = new byte[so];
		for(i = 0; i < so; i++) rbuf[i] = sbuf[i];
		return rbuf;
	}

	private int[] generateSlopes() {
		int[] slope = new int[XSIZE * YSIZE];
		int[] slopeRet = new int[XSIZE * YSIZE];
		int i, j, k, l;
		double xs, ys;
		double[] st = new double[256];

		for(i = 0; i < 256; i++) st[i] = Math.sin(Math.PI * i / 128) * 256;
		for(i = 0; i < YSIZE; i++)
			for(j = 0; j < XSIZE; j++)
				if ((table[i * XSIZE + j] & 2) != 0) {
					xs = ys = 0;
					for(k = i - 1; k <= i + 1; k++)
						for(l = j - 1; l <= j + 1; l++)
							if ((tableByte(l, k) & 2) == 0) {
								xs += l - j;
								ys += k - i;
							}
					slope[i * XSIZE + j] = vectorAngle(xs, ys);
				} else slope[i * XSIZE + j] = 0;
		System.out.println();
		for(i = 0; i < YSIZE; i++)
			for(j = 0; j < XSIZE; j++)
				if ((tableByte(j, i) & 2) == 0 || (tableByte(j, i) & 4) == 4 ||
					((tableByte(j - 1, i - 1) & 2) + (tableByte(j, i - 1) & 2) +
					(tableByte(j + 1, i - 1) & 2) + (tableByte(j - 1, i) & 2) +
					(tableByte(j + 1, i) & 2) + (tableByte(j - 1, i + 1) & 2) +
					(tableByte(j, i + 1) & 2) + (tableByte(j + 1, i + 1) & 2)) == 16) {
					slopeRet[i * XSIZE + j] = 0;
				} else {
					xs = ys = 0;
					for(k = i - 1; k <= i + 1; k++)
						for(l = j - 1; l <= j + 1; l++)
							if (slopeByte(l, k, slope) != 0) {
								xs += st[slopeByte(l, k, slope)];
								ys += st[(slopeByte(l, k, slope) - 64) & 255];
							}
					slopeRet[i * XSIZE + j] = vectorAngle(xs, ys);
				}
		return slopeRet;
	}

	private int vectorAngle(double x, double y) {
		if (x == 0 && y == 0) return 0;
		int a = (int)(Math.atan2(x, -y) * 128 / Math.PI) & 255;
		return (a == 0) ? 1 : a;
	}

	private byte tableByte(int x, int y) {
		if (x >= 0 && x < XSIZE && y >= 0 && y < YSIZE) return table[y * XSIZE + x];
		else return 3;
	}

	private int slopeByte(int x, int y, int[] slope) {
		if (x >= 0 && x < XSIZE && y >= 0 && y < YSIZE) return slope[y * XSIZE + x];
		else return 0;
	}

	private byte[] compressSlopes(int[] slope) {
		int[] cslope = new int[XSIZE * YSIZE >> 1];
		int[] cslope2 = new int[XSIZE * YSIZE >> 1];
		byte[] slopeRet;
		int i, j, k, b;

		for(i = 0; i < YSIZE >> 1; i++)
			for(j = 0; j < XSIZE >> 1; j++) {
				b = slope[(i << 1) * XSIZE + (j << 1)];
				if (b == 0) b = slope[(i << 1) * XSIZE + (j << 1) + 1];
				if (b == 0) b = slope[((i << 1) + 1) * XSIZE + (j << 1)];
				if (b == 0) b = slope[((i << 1) + 1) * XSIZE + (j << 1) + 1];
				cslope[i * (XSIZE >> 1) + j] = b;
			}

		j = XSIZE * YSIZE >> 2;
		i = k = 0;
		while(j > 0) {
			if (cslope[i] == 0) {
				i++;
				cslope2[k++] = 0;
				for(b = 0; b < 255 && j > 0; b++, j--)
					if (cslope[i++] != 0) { cslope2[k++] = b; cslope2[k++] = cslope[i - 1]; b = 256; }
				if (b == 255) cslope2[k++] = 255;
				if (j <= 0) cslope2[k++] = b - 1;
			}
			if (cslope[i] != 0 ) { cslope2[k++] = cslope[i++]; j--; }
		}

		slopeRet = new byte[k];
		for(i = 0; i < k; i++) slopeRet[i] = (byte)cslope2[i];
		return slopeRet;
	}

	private byte[] generateBinEvents(DefaultListModel el) {
		byte[] raw = new byte[2048];
		byte[] ret;
		int s = 0;
		for(int i = 0; i < el.size(); i++) {
			AGEvent e = (AGEvent)el.elementAt(i);
			raw[s++] = (byte)e.getType();
			switch(e.getType()) {
				case AGEvent.SCORE:
					raw[s++] = (byte)e.getVal();
					raw[s++] = (byte)(e.getVal() >> 8);
					raw[s++] = (byte)e.getBonus();
					raw[s++] = (byte)(e.getBonus() >> 8);
				break;
				case AGEvent.SETFLAGS:
					raw[s++] = (byte)e.getObject().getBinID();
					raw[s++] = (byte)(e.getVal() & 254);
				break;
				case AGEvent.RESETFLAGS:
					raw[s++] = (byte)e.getObject().getBinID();
					raw[s++] = (byte)(e.getVal() | 1);
				break;
				case AGEvent.SETCOUNTER:
				case AGEvent.SETTIMER:
					raw[s++] = (byte)e.getObject().getBinID();
					raw[s++] = (byte)e.getVal();
				break;
				case AGEvent.ACTIVATE:
				case AGEvent.HIDE:
				case AGEvent.DECREASE:
					raw[s++] = (byte)e.getObject().getBinID();
				break;
			}
		}
		ret = new byte[s];
		for(int i = 0; i < s; i++) ret[i] = raw[i];
		return ret;
	}

	private void fillBinID(Vector l) {
		for(int i = 0; i < l.size(); i++) ((AGObject)l.elementAt(i)).setBinID(i);
	}

	public void copyDeco() {
		for(int i = 0; i < table.length; i++) {
			if ((table[i] & 1) == 1) table[i] |= 2;
			else table[i] &= ~2;
		}
	}

}

