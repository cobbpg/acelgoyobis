package AGTEdit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class AGBrushBar extends JPanel implements ActionListener {

	private JToggleButton brushDeco = new JToggleButton("Deco", true);
	private JToggleButton brushWall = new JToggleButton("Wall");
	private JToggleButton brushHole = new JToggleButton("Hole");
	private JToggleButton maskDeco = new JToggleButton("Deco", true);
	private JToggleButton maskWall = new JToggleButton("Wall", true);
	private JToggleButton maskHole = new JToggleButton("Hole", true);
	private JToggleButton toolBrush1 = new JToggleButton("Br1");
	private JToggleButton toolBrush2 = new JToggleButton("Br2");
	private JToggleButton toolBrush3 = new JToggleButton("Br3");
	private JToggleButton toolBrush4 = new JToggleButton("Br4");
	private JToggleButton toolObjects = new JToggleButton("Obj");

	private AGTablePaint imageView;

	AGBrushBar(AGTablePaint atp) {
		super(new GridLayout(14, 1));
		imageView = atp;

		brushDeco.setForeground(Color.BLACK);
		brushWall.setForeground(Color.RED);
		brushHole.setForeground(Color.BLUE);
		maskDeco.setForeground(Color.BLACK);
		maskWall.setForeground(Color.RED);
		maskHole.setForeground(Color.BLUE);
		add(new JLabel("Colour", SwingConstants.CENTER));
		add(brushDeco);
		add(brushWall);
		add(brushHole);
		add(new JLabel("Mask", SwingConstants.CENTER));
		add(maskDeco);
		add(maskWall);
		add(maskHole);
		add(new JLabel("Tools", SwingConstants.CENTER));
		add(toolBrush1);
		add(toolBrush2);
		add(toolBrush3);
		add(toolBrush4);
		add(toolObjects);

		ButtonGroup brushGroup = new ButtonGroup();
		brushGroup.add(toolBrush1);
		brushGroup.add(toolBrush2);
		brushGroup.add(toolBrush3);
		brushGroup.add(toolBrush4);
		brushGroup.add(toolObjects);

		brushDeco.addActionListener(this);
		brushWall.addActionListener(this);
		brushHole.addActionListener(this);
		maskDeco.addActionListener(this);
		maskWall.addActionListener(this);
		maskHole.addActionListener(this);
		toolBrush1.addActionListener(this);
		toolBrush2.addActionListener(this);
		toolBrush3.addActionListener(this);
		toolBrush4.addActionListener(this);
		toolObjects.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		JToggleButton s = (JToggleButton)e.getSource();
		if (s.equals(brushDeco)) {
			imageView.setBrushColor(imageView.getBrushColor() & 6 | ((s.isSelected()) ? 1 : 0));
		} else
		if (s.equals(brushWall)) {
			imageView.setBrushColor(imageView.getBrushColor() & 5 | ((s.isSelected()) ? 2 : 0));
		} else
		if (s.equals(brushHole)) {
			imageView.setBrushColor(imageView.getBrushColor() & 3 | ((s.isSelected()) ? 4 : 0));
		} else
		if (s.equals(maskDeco)) {
			imageView.setMask(imageView.getMask() & 6 | ((s.isSelected()) ? 1 : 0));
			imageView.repaint();
		} else
		if (s.equals(maskWall)) {
			imageView.setMask(imageView.getMask() & 5 | ((s.isSelected()) ? 2 : 0));
			imageView.repaint();
		} else
		if (s.equals(maskHole)) {
			imageView.setMask(imageView.getMask() & 3 | ((s.isSelected()) ? 4 : 0));
			imageView.repaint();
		} else
		if (s.equals(toolBrush1)) {
			imageView.setMode(AGTablePaint.PAINT);
			imageView.setBrush(AGBrush.BRUSH1);
		} else
		if (s.equals(toolBrush2)) {
			imageView.setMode(AGTablePaint.PAINT);
			imageView.setBrush(AGBrush.BRUSH2);
		} else
		if (s.equals(toolBrush3)) {
			imageView.setMode(AGTablePaint.PAINT);
			imageView.setBrush(AGBrush.BRUSH3);
		} else
		if (s.equals(toolBrush4)) {
			imageView.setMode(AGTablePaint.PAINT);
			imageView.setBrush(AGBrush.BRUSH4);
		} else
		if (s.equals(toolObjects)) {
			imageView.setMode(AGTablePaint.OBJECTS);
		}
	}

}

