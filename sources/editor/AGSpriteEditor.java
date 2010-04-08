package AGTEdit;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class AGSpriteEditor extends JPanel implements MouseListener, MouseMotionListener {

	private static final int zoom = 8;
	private BufferedImage spriteImage = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY);
	private byte[] spriteData = ((DataBufferByte)spriteImage.getRaster().getDataBuffer()).getData();
	private int mousebut;
	private AGSprite sprite;
	private AGTablePaint painter;

	AGSpriteEditor(AGTablePaint atp) {
		super();
		setPreferredSize(new Dimension(68, 68));
		setBorder(new EtchedBorder());
		painter = atp;
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.drawRenderedImage(spriteImage, new AffineTransform(zoom, 0, 0, zoom, 2, 2));
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX() / zoom;
		int y = e.getY() / zoom;
		if (x >= 0 && x < 8 && y >= 0 && y < 8)
			spriteData[y * 8 + x] = (byte)((mousebut == MouseEvent.BUTTON1) ? 0 : -1);
		paintImmediately(2, 2, 8 * zoom, 8 * zoom);
	}

	public void mouseClicked(MouseEvent e) { }

	public void mouseMoved(MouseEvent e) { }

	public void mouseEntered(MouseEvent e) { }

	public void mouseExited(MouseEvent e) { }

	public void mousePressed(MouseEvent e) {
		int x = e.getX() / zoom;
		int y = e.getY() / zoom;
		mousebut = e.getButton();
		if (x >= 0 && x < 8 && y >= 0 && y < 8)
			spriteData[y * 8 + x] = (byte)((mousebut == MouseEvent.BUTTON1) ? 0 : -1);
		paintImmediately(2, 2, 8 * zoom, 8 * zoom);
	}

	public void mouseReleased(MouseEvent e) {
		byte[] data = sprite.get();
		for(int i = 0; i < 64; i++)
			data[i] = (byte)(spriteData[i] + 1);
		painter.repaint();
	}

	public void setSprite(AGSprite s) {
		sprite = s;
		byte[] data = sprite.get();
		for(int i = 0; i < 64; i++)
			spriteData[i] = (byte)(data[i] - 1);
		paintImmediately(2, 2, 8 * zoom, 8 * zoom);
	}

}

