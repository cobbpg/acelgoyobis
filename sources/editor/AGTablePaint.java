package AGTEdit;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class AGTablePaint extends JPanel implements MouseListener, MouseMotionListener {

	public static final int PAINT = 0;
	public static final int OBJECTS = 1;
	private static byte[] leftFlipper = {
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
		1,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,
		1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,
		0,1,1,0,0,0,1,1,1,1,0,0,0,0,0,0,
		0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,
		0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,
	};
	private static final int extraSize = 7;
	private static final byte[] icmR = { 0, -1, 127, -1,  0, -1, -1, -1 };
	private static final byte[] icmG = { 0,  0, 127,  0,  0,  0,  0,  0 };
	private static final byte[] icmB = { 0, -1, 127,  0, -1, -1,  0,  0 };
	private static final byte[] icmA = { 0, -1,  64, -1, 64, -1, 64, -1 };
	private BufferedImage drawArea, maskedLayer;
	private BufferedImage spriteLayer = new BufferedImage(AGTable.XSIZE, AGTable.YSIZE,
		BufferedImage.TYPE_BYTE_INDEXED, new IndexColorModel(8, 8, icmR, icmG, icmB, icmA));;
	private TexturePaint extraArea;
	private byte[] tableData, maskedData;
	private byte[] spriteData = ((DataBufferByte)spriteLayer.getRaster().getDataBuffer()).getData();
	private int zoom = 4, xp, yp;
	private JList objects;
	private DefaultListModel objectList;
	private int mode = PAINT;
	private int lastx, lasty;
	private int mousex, mousey, mousebut;
	private int brush = AGBrush.NONE;
	private int brushcol = 1;
	private int mask = 7;

	AGTablePaint(BufferedImage da, JList os) {
		// Creating texture for the unused paint area
		BufferedImage txtr = new BufferedImage(extraSize, extraSize, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster wr = txtr.getRaster();
		for (int i = 0; i < extraSize; i++)
			for (int j = 0; j < extraSize; j++)
				wr.setSample(i, j, 0, (j == i || extraSize - j - 1 == i) ? 192 : 255);
		extraArea = new TexturePaint(txtr, new Rectangle(0, 0, extraSize, extraSize));
		drawArea = da;
		tableData = ((DataBufferByte)da.getRaster().getDataBuffer()).getData();
		objects = os;
		objectList = (DefaultListModel)os.getModel();
		maskedLayer = new BufferedImage(AGTable.XSIZE, AGTable.YSIZE,
			BufferedImage.TYPE_BYTE_INDEXED, (IndexColorModel)da.getColorModel());
		maskedData = ((DataBufferByte)maskedLayer.getRaster().getDataBuffer()).getData();
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int i, j, k;

		for(i = 0; i < AGTable.XSIZE * AGTable.YSIZE; i++) {
			spriteData[i] = 0;
			maskedData[i] = (byte)(tableData[i] & mask);
		}

		for(i = 0; i < objectList.getSize(); i++) {
			AGObject o = (AGObject)objectList.elementAt(i);
			switch(o.getType()) {
				case AGObject.GIZMO: {
					AGGizmo o2 = (AGGizmo)o;
					byte[] spr = o2.getSprite().get();
					int x = o2.getX();
					int y = o2.getY();
					if (o2.getSpriteFlag()) {
						for(j = 0; j < 8; j++)
							for(k = 0; k < 8; k++)
								if (x + k < AGTable.XSIZE && y + j < AGTable.YSIZE)
									spriteData[(y + j) * AGTable.XSIZE + x + k] |= spr[j * 8 + k];
					} else {
						for(j = 0; j <= o2.getH(); j++)
							for(k = 0; k <= o2.getW(); k++)
								spriteData[(o2.getY() + j) * AGTable.XSIZE + o2.getX() + k] |= 4;
					}			
				}
				break;
				case AGObject.BOUNCE: {
					AGBounce o2 = (AGBounce)o;
					for(j = 0; j <= o2.getH(); j++)
						for(k = 0; k <= o2.getW(); k++)
							spriteData[(o2.getY() + j) * AGTable.XSIZE + o2.getX() + k] |= 4;
				}
				break;
				case AGObject.FLIPPER: {
					AGFlipper o2 = (AGFlipper)o;
					for(j = 0; j < 8; j++)
						for(k = 0; k < 16; k++)
							spriteData[(o2.getY() + j) * AGTable.XSIZE + o2.getX() + k] |=
								leftFlipper[j * 16 + ((o2.getDir() == AGFlipper.LEFT) ? k : 15 - k)];
				}
				break;
			}
		}

		Object[] sel = objects.getSelectedValues();

		for(i = 0; i < sel.length; i++) {
			AGObject o = (AGObject)sel[i];
			switch(o.getType()) {
				case AGObject.GIZMO:
				case AGObject.BOUNCE:
				case AGObject.FLIPPER: {
					AGDimension o2 = (AGDimension)sel[i];
					for(j = 0; j <= o2.getH(); j++)
						for(k = 0; k <= o2.getW(); k++)
							spriteData[(o2.getY() + j) * AGTable.XSIZE + o2.getX() + k] |= 2;
				}
				break;
			}
		}

		if (mode == PAINT) {
			byte[] bimg = AGBrush.getBrush(brush);
			for(i = 0; i < AGBrush.SIZE; i++)
				for(j = 0; j < AGBrush.SIZE; j++) {
					int bx = i + mousex - 2;
					int by = j + mousey - 2;
					int bc = brushcol & mask;
					if (bx >= 0 && bx < AGTable.XSIZE && by >= 0 && by < AGTable.YSIZE) {
						if (mousebut == MouseEvent.BUTTON1 || mousebut == MouseEvent.NOBUTTON)
							maskedData[by * AGTable.XSIZE + bx] |= bimg[j * AGBrush.SIZE + i] * bc;
						else
							maskedData[by * AGTable.XSIZE + bx] &= ~(bimg[j * AGBrush.SIZE + i] * bc);
					}
				}
		}

		xp = (getWidth() - AGTable.XSIZE * zoom) / 2;
		yp = (getHeight() - AGTable.YSIZE * zoom) / 2;
		xp = (xp > 0) ? xp : 0;
		yp = (yp > 0) ? yp : 0;
		g2.setPaint(extraArea);
		g2.fill(g2.getClipBounds());
		g2.drawRenderedImage(maskedLayer, new AffineTransform(zoom, 0, 0, zoom, xp, yp));
		g2.drawRenderedImage(spriteLayer, new AffineTransform(zoom, 0, 0, zoom, xp, yp));
	}

	public void mouseDragged(MouseEvent e) {
		int x = (e.getX() - xp) / zoom;
		int y = (e.getY() - yp) / zoom;
		mousex = x;
		mousey = y;
		if (lastx != x || lasty != y) switch(mode) {
			case PAINT: {
				byte[] bimg = AGBrush.getBrush(brush);
				int i, j, bx, by, bc = brushcol & mask;
				for(i = 0; i < AGBrush.SIZE; i++)
					for(j = 0; j < AGBrush.SIZE; j++) {
						bx = i + x - 2;
						by = j + y - 2;
						if (bx >= 0 && bx < AGTable.XSIZE && by >= 0 && by < AGTable.YSIZE) {
							if (mousebut == MouseEvent.BUTTON1) tableData[by * AGTable.XSIZE + bx] |= bimg[j * AGBrush.SIZE + i] * bc;
							else tableData[by * AGTable.XSIZE + bx] &= ~(bimg[j * AGBrush.SIZE + i] * bc);
						}
					}
				paintImmediately(xp + (x - 2) * zoom, yp + (y - 2) * zoom, zoom * AGBrush.SIZE, zoom * AGBrush.SIZE);
			}
			break;
			case OBJECTS: {
				Object[] sel = objects.getSelectedValues();
				for(int i = 0; i < sel.length; i++) {
					AGObject o = (AGObject)sel[i];
						switch(o.getType()) {
							case AGObject.GIZMO:
							case AGObject.BOUNCE:
							case AGObject.FLIPPER: {
								AGDimension o2 = (AGDimension)sel[i];
								o2.addPos(x - lastx, y - lasty);
							}
							break;
					}
				}
				paintImmediately(xp, yp, AGTable.XSIZE * zoom, AGTable.YSIZE * zoom);
			}
			break;
		}
		lastx = x;
		lasty = y;
	}

	public void mouseClicked(MouseEvent e) { }

	public void mouseMoved(MouseEvent e) {
		mousebut = e.getButton();
		mousex = (e.getX() - xp) / zoom;
		mousey = (e.getY() - yp) / zoom;
		if (mousebut == MouseEvent.NOBUTTON) paintImmediately(xp, yp, AGTable.XSIZE * zoom, AGTable.YSIZE * zoom);
	}

	public void mouseEntered(MouseEvent e) { }

	public void mouseExited(MouseEvent e) { }

	public void mousePressed(MouseEvent e) {
		int x = (e.getX() - xp) / zoom;
		int y = (e.getY() - yp) / zoom;
		mousebut = e.getButton();
		lastx = x;
		lasty = y;
		switch(mode) {
			case PAINT: {
				byte[] bimg = AGBrush.getBrush(brush);
				int i, j, bx, by, bc = brushcol & mask;
				for(i = 0; i < AGBrush.SIZE; i++)
					for(j = 0; j < AGBrush.SIZE; j++) {
						bx = i + x - 2;
						by = j + y - 2;
						if (bx >= 0 && bx < AGTable.XSIZE && by >= 0 && by < AGTable.YSIZE) {
							if (mousebut == MouseEvent.BUTTON1) tableData[by * AGTable.XSIZE + bx] |= bimg[j * AGBrush.SIZE + i] * bc;
							else tableData[by * AGTable.XSIZE + bx] &= ~(bimg[j * AGBrush.SIZE + i] * bc);
						}
					}
				paintImmediately(xp + (x - 2) * zoom, yp + (y - 2) * zoom, zoom * AGBrush.SIZE, zoom * AGBrush.SIZE);
			}
			break;
			case OBJECTS: {
				boolean found = false;
				for(int i = 0; i < objectList.getSize(); i++) {
					AGObject o = (AGObject)objectList.elementAt(i);
					switch(o.getType()) {
						case AGObject.GIZMO:
						case AGObject.FLIPPER:
						case AGObject.BOUNCE: {
							AGDimension o2 = (AGDimension)o;
							if (x >= o2.getX() && x <= o2.getX() + o2.getW() && y >= o2.getY() && y <= o2.getY() + o2.getH()) {
								found = true;
								if (e.isControlDown()) objects.addSelectionInterval(i, i);
								else if (!objects.isSelectedIndex(i)) objects.setSelectedIndex(i);
							}
						}
						break;
					}
				}
				if (!found) objects.clearSelection();
			}
			break;
		}
	}

	public void mouseReleased(MouseEvent e) { }

	public void setZoom(int z) {
		zoom = z;
		setPreferredSize(new Dimension(AGTable.XSIZE * zoom, AGTable.YSIZE * zoom));
		if (getParent() != null) ((JScrollPane)getParent().getParent()).revalidate();
	}
	public int getZoom() { return zoom; }

	public void setMode(int m) { mode = m; }
	public int getMode() { return mode; }

	public void setBrush(int b) { brush = b; }
	public int getBrush() { return brush; }

	public void setBrushColor(int c) { brushcol = c; }
	public int getBrushColor() { return brushcol; }

	public void setMask(int m) { mask = m; }
	public int getMask() { return mask; }

}

