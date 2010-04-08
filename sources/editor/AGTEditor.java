package AGTEdit;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class AGTEditor {

	AGTEditor() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() { public void run() { startUI(); } });
	}

	public void startUI() {
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { }
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame("AGT editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		AGTComponents acs = new AGTComponents();
		frame.getContentPane().add(acs.createComponents(), BorderLayout.CENTER);
		frame.setJMenuBar(acs.mainMenu());
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		acs.addActions();
	}

}

class AGTComponents {

	private AGTable doc = new AGTable();

	private BufferedImage tableImage;
	private AGTablePaint imageView;

	private JTextField tableName = new JTextField("Untitled", 17);

	private JList objectList = new JList(doc.getObjects());
	private JButton newObject = new JButton("New");
	private JButton deleteObject = new JButton("Delete");
	private JButton objectUp = new JButton("/\\");
	private JButton objectDown = new JButton("\\/");

	private boolean updateEnabled = false;
	private JTextField renameObject = new JTextField(17);
	private JTextField objectParam = new JTextField(5);
	private JTextField objectX = new JTextField(3);
	private JTextField objectY = new JTextField(3);
	private JTextField objectW = new JTextField(3);
	private JTextField objectH = new JTextField(3);
	private JTextField objectVal = new JTextField(3);
	private JTextField objectFX = new JTextField(3);
	private JTextField objectFY = new JTextField(3);
	private JButton objectDir = new JButton();

	private JCheckBox spriteActive = new JCheckBox("Start as active");
	private JCheckBox spritePresent = new JCheckBox("Sprite present");
	private JCheckBox spriteVisible = new JCheckBox("Start as visible");
	private JButton spriteFit = new JButton("Fit size");
	private AGSpriteEditor spriteEditor;
	private AGEventEditor eventEditor = new AGEventEditor(doc);
	private JPanel dummyProperty = new JPanel();
	private JPanel renamePanel = new JPanel();
	private JPanel dimensionProperty = new JPanel();
	private JPanel spritePanel = new JPanel(new BorderLayout());
	private JPanel valueProperty = new JPanel();
	private JPanel flipperPanel = new JPanel();

	private JPanel objectPropertyPanel = new JPanel(new CardLayout());
	private JPanel gizmoProperty = new JPanel();
	private JPanel timerProperty = new JPanel();
	private JPanel counterProperty = new JPanel();
	private JPanel bounceProperty = new JPanel();
	private JPanel flipperProperty = new JPanel();
	private JPanel commentProperty = new JPanel();

	private JMenuItem fileNew = new JMenuItem("New");
	private JMenuItem fileOpen = new JMenuItem("Open");
	private JMenuItem fileSave = new JMenuItem("Save");
	private JMenuItem fileExport = new JMenuItem("Export");

	private JMenuItem zoom1x = new JMenuItem("Zoom 100%");
	private JMenuItem zoom15x = new JMenuItem("Zoom 150%");
	private JMenuItem zoom2x = new JMenuItem("Zoom 200%");
	private JMenuItem zoom4x = new JMenuItem("Zoom 400%");

	private JMenuItem tableLayers = new JMenuItem("Deco -> Wall");
	private JMenuItem tableImport = new JMenuItem("Import image");

	private JFileChooser jfc = new JFileChooser("tables");
	private JFileChooser ifc = new JFileChooser(".");

	private class AGObjectActions implements ActionListener {

		private class AGObjectChooser extends JDialog implements ActionListener {

			private JButton actionOK = new JButton("OK");
			private JButton actionCancel = new JButton("Cancel");
			private JRadioButton[] typeButtons = new JRadioButton[7];
			private String[] typeNames = { "[NONE]", "Gizmo", "Timer", "Counter", "Bounce", "Flipper", "Comment" };
			private int type = AGObject.OBJECT;

			AGObjectChooser() {
				super();

				Container p = getContentPane();
				setModal(true);
				setResizable(false);
				setLocationRelativeTo(null);
				setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
				setTitle("Object type");

				ButtonGroup choice = new ButtonGroup();
				JPanel types = new JPanel(new GridLayout(typeButtons.length, 1));
				for(int i = 1; i < typeButtons.length; i++) {
					typeButtons[i] = new JRadioButton(typeNames[i]);
					choice.add(typeButtons[i]);
					types.add(typeButtons[i]);
				}
				actionOK.addActionListener(this);
				actionCancel.addActionListener(this);

				JPanel actions = new JPanel();
				actions.add(actionOK);
				actions.add(actionCancel);

				p.setLayout(new BorderLayout());
				p.add(types, BorderLayout.CENTER);
				p.add(actions, BorderLayout.SOUTH);

				pack();
				show();
			}

			public int getType() { return type; }

			public void actionPerformed(ActionEvent e) {
				Object s = e.getSource();

				if (s.equals(actionOK)) {
					for(int i = 1; i < typeButtons.length; i++)
						if (typeButtons[i].isSelected()) type = i;
					dispose();
				} else
				if (s.equals(actionCancel)) {
					type = AGObject.OBJECT;
					dispose();
				}
			}

		}

		public void actionPerformed(ActionEvent e) {
			Object s = e.getSource();

			if (s.equals(newObject)) {
				int i = objectList.getSelectedIndex();
				AGObject o = null;
				AGObjectChooser oc = new AGObjectChooser();
				switch(oc.getType()) {
					case AGObject.GIZMO: o = new AGGizmo(); break;
					case AGObject.TIMER: o = new AGTimer(); break;
					case AGObject.COUNTER: o = new AGCounter(); break;
					case AGObject.BOUNCE: o = new AGBounce(); break;
					case AGObject.FLIPPER: o = new AGFlipper(); break;
					case AGObject.COMMENT: o = new AGComment(); break;
				}
				if (o != null) {
					objectList.clearSelection();
					if (i != -1) doc.insert(o, i);
					else doc.add(o);
					objectList.setSelectedValue(o, true);
				}
			} else

			if (s.equals(deleteObject)) {
				while (objectList.getSelectedIndex() != -1)
					doc.remove(objectList.getSelectedIndex());
			} else

			if (s.equals(objectUp)) {
				if (objectList.getSelectedIndices().length == 1) {
					int i = objectList.getSelectedIndex();
					if (i > 0) {
						AGObject o = doc.get(i);
						doc.remove(i);
						doc.insert(o, i - 1);
						objectList.setSelectedValue(o, true);
					}
				}
			} else

			if (s.equals(objectDown)) {
				if (objectList.getSelectedIndices().length == 1) {
					int i = objectList.getSelectedIndex();
					if (i < doc.getCount() - 1) {
						AGObject o = doc.get(i);
						doc.remove(i);
						doc.insert(o, i + 1);
						objectList.setSelectedValue(o, true);
					}
				}
			} else

			if (s.equals(renameObject)) {
				((AGObject)objectList.getSelectedValue()).setName(renameObject.getText());
				objectList.paint(objectList.getGraphics());
			} else

			if (s.equals(spriteActive)) {
				((AGGizmo)objectList.getSelectedValue()).toggleActiveFlag();
			} else

			if (s.equals(spritePresent)) {
				((AGGizmo)objectList.getSelectedValue()).toggleSpriteFlag();
			} else

			if (s.equals(spriteVisible)) {
				((AGGizmo)objectList.getSelectedValue()).toggleVisibleFlag();
			} else

			if (s.equals(spriteFit)) {
				AGGizmo g = (AGGizmo)objectList.getSelectedValue();
				g.reFit();
				objectW.setText(Integer.toString(g.getW()));
				objectH.setText(Integer.toString(g.getH()));
			}

			if (s.equals(objectDir)) {
				AGFlipper f = (AGFlipper)objectList.getSelectedValue();
				f.toggleDir();
				objectDir.setText(f.getDirString());
			}

			imageView.repaint();
		}

	}

	private class AGMenuActions implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			Object s = e.getSource();

			if (s.equals(fileNew)) {
				tableName.setText("Untitled");
				doc.setName("Untitled");
				doc.removeAllObjects();
				Arrays.fill(doc.getTable(), (byte)0);
			} else

			if (s.equals(fileOpen)) {
				if(jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
					if (!doc.open(jfc.getSelectedFile().getPath()))
						JOptionPane.showMessageDialog(null, "The file could not be opened", "Error!",
							JOptionPane.ERROR_MESSAGE);
				tableName.setText(doc.getName());
			} else

			if (s.equals(fileSave)) {
				doc.setName(tableName.getText());
				if(jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
					if (!doc.save(jfc.getSelectedFile().getPath()))
						JOptionPane.showMessageDialog(null, "The file could not be saved", "Error!",
							JOptionPane.ERROR_MESSAGE);
			} else

			if (s.equals(fileExport)) {
				doc.setName(tableName.getText());
				byte[] binDoc = doc.export();
				if (binDoc[0] == 0) JOptionPane.showMessageDialog(null, "There are too many objects on the table",
					"Error!", JOptionPane.ERROR_MESSAGE);
				else if(jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					String fname = jfc.getSelectedFile().getPath();
					if (fname.endsWith(".xagt")) fname = fname.substring(0, fname.length() - 5);
					String cname = CalcFile.convertName("ag" + doc.getName());;
					if (!CalcFile.save(binDoc, CalcFile.TI82, fname + ".82p", cname))
						JOptionPane.showMessageDialog(null, "The TI-82 binary could not be saved", "Error!",
							JOptionPane.ERROR_MESSAGE);
					if (!CalcFile.save(binDoc, CalcFile.TI83, fname + ".83p", cname))
						JOptionPane.showMessageDialog(null, "The TI-83 binary could not be saved", "Error!",
							JOptionPane.ERROR_MESSAGE);
					if (!CalcFile.save(binDoc, CalcFile.TI83P, fname + ".8xp", cname))
						JOptionPane.showMessageDialog(null, "The TI-83+ binary could not be saved", "Error!",
							JOptionPane.ERROR_MESSAGE);
				}

			} else

			if (s.equals(zoom1x)) { imageView.setZoom(2); imageView.repaint(); } else
			if (s.equals(zoom15x)) { imageView.setZoom(3); imageView.repaint(); } else
			if (s.equals(zoom2x)) { imageView.setZoom(4); imageView.repaint(); } else
			if (s.equals(zoom4x)) { imageView.setZoom(8); imageView.repaint(); } else

			if (s.equals(tableLayers)) { doc.copyDeco(); imageView.repaint(); } else
			if (s.equals(tableImport)) {
				if (ifc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					try {
						BufferedImage bi = ImageIO.read(ifc.getSelectedFile());
						int xs = bi.getWidth();
						int ys = bi.getHeight();
						if (xs != AGTable.XSIZE || ys != AGTable.YSIZE)
							JOptionPane.showMessageDialog(null, "The image dimensions must be " + AGTable.XSIZE + "x" + AGTable.YSIZE + " pixels.", "Error!",
								JOptionPane.ERROR_MESSAGE);
						else {
							BufferedImage tmpi = new BufferedImage(AGTable.XSIZE, AGTable.YSIZE, BufferedImage.TYPE_BYTE_GRAY);
							Graphics2D gc = tmpi.createGraphics();
							gc.drawRenderedImage(bi, new AffineTransform());
							byte[] tmpd = ((DataBufferByte)tmpi.getRaster().getDataBuffer()).getData();
							byte[] tmpt = doc.getTable();
							for(int i = 0; i < tmpd.length; i++) tmpt[i] = (byte)((tmpd[i] < 0) ? 0 : 1);
							imageView.repaint();
						}
					} catch (Exception xe) {
						JOptionPane.showMessageDialog(null, "An error occurred while trying to read the image.", "Error!",
							JOptionPane.ERROR_MESSAGE);
					}
				}
			}

		}

	}

	private class AGObjectListSelectionHandler implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() == false) {
				updateEnabled = false;
				CardLayout cl = (CardLayout)(objectPropertyPanel.getLayout());
				if (objectList.getSelectedIndices().length == 1) {
					AGObject o = (AGObject)objectList.getSelectedValue();
					renameObject.setText(o.getName());
					switch (o.getType()) {
						case AGObject.GIZMO: {
							AGGizmo o2 = (AGGizmo)o;
							objectX.setText(Integer.toString(o2.getX()));
							objectY.setText(Integer.toString(o2.getY()));
							objectW.setText(Integer.toString(o2.getW()));
							objectH.setText(Integer.toString(o2.getH()));
							spriteEditor.setSprite(o2.getSprite());
							eventEditor.setContents(o2.getEvents());
							spriteActive.setSelected(o2.getActiveFlag());
							spritePresent.setSelected(o2.getSpriteFlag());
							spriteVisible.setSelected(o2.getVisibleFlag());
							gizmoProperty.add(renamePanel);
							gizmoProperty.add(dimensionProperty);
							gizmoProperty.add(spritePanel);
							gizmoProperty.add(eventEditor);
							cl.show(objectPropertyPanel, "Gizmo");
						}
						break;
						case AGObject.TIMER: {
							AGTimer o2 = (AGTimer)o;
							objectVal.setText(Integer.toString(o2.getVal()));
							eventEditor.setContents(o2.getEvents());
							timerProperty.add(renamePanel);
							timerProperty.add(valueProperty);
							timerProperty.add(eventEditor);
							cl.show(objectPropertyPanel, "Timer");
						}
						break;
						case AGObject.COUNTER: {
							AGCounter o2 = (AGCounter)o;
							objectVal.setText(Integer.toString(o2.getVal()));
							eventEditor.setContents(o2.getEvents());
							counterProperty.add(renamePanel);
							counterProperty.add(valueProperty);
							counterProperty.add(eventEditor);
							cl.show(objectPropertyPanel, "Counter");
						}
						break;
						case AGObject.BOUNCE: {
							AGBounce o2 = (AGBounce)o;
							objectX.setText(Integer.toString(o2.getX()));
							objectY.setText(Integer.toString(o2.getY()));
							objectW.setText(Integer.toString(o2.getW()));
							objectH.setText(Integer.toString(o2.getH()));
							bounceProperty.add(renamePanel);
							bounceProperty.add(dimensionProperty);
							cl.show(objectPropertyPanel, "Bounce");
						}
						break;
						case AGObject.FLIPPER: {
							AGFlipper o2 = (AGFlipper)o;
							objectFX.setText(Integer.toString(o2.getX()));
							objectFY.setText(Integer.toString(o2.getY()));
							objectDir.setText(o2.getDirString());
							flipperProperty.add(renamePanel);
							flipperProperty.add(flipperPanel);
							cl.show(objectPropertyPanel, "Flipper");
						}
						break;
						case AGObject.COMMENT: {
							commentProperty.add(renamePanel);
							cl.show(objectPropertyPanel, "Comment");
						}
					}
					updateEnabled = true;
				} else { cl.show(objectPropertyPanel, "None"); }
				imageView.repaint();
			}
		}

	}

	private class AGDocumentListener implements DocumentListener {

		public static final int X = 0;
		public static final int Y = 1;
		public static final int W = 2;
		public static final int H = 3;
		public static final int VAL = 4;
		public static final int FX = 5;
		public static final int FY = 6;
		private int obj;

		AGDocumentListener(int o) { super(); obj = o; }

		public void insertUpdate(DocumentEvent e) { if (updateEnabled) performUpdate(); }

		public void removeUpdate(DocumentEvent e) { if (updateEnabled) performUpdate(); }

		public void changedUpdate(DocumentEvent e) { }

		private int parseInt(String s) {
			int res;
			try { res = Integer.parseInt(s); }
			catch (Exception e) { res = 0; }
			return res;
		}

		private void performUpdate() {
			AGObject o = (AGObject)objectList.getSelectedValue();
			switch(obj) {
				case X:
					if (o.getType() == AGObject.GIZMO) ((AGGizmo)o).setX(parseInt(objectX.getText()));
					if (o.getType() == AGObject.BOUNCE) ((AGBounce)o).setX(parseInt(objectX.getText()));
				break;
				case Y:
					if (o.getType() == AGObject.GIZMO) ((AGGizmo)o).setY(parseInt(objectY.getText()));
					if (o.getType() == AGObject.BOUNCE) ((AGBounce)o).setY(parseInt(objectY.getText()));
				break;
				case W:
					if (o.getType() == AGObject.GIZMO) ((AGGizmo)o).setW(parseInt(objectW.getText()));
					if (o.getType() == AGObject.BOUNCE) ((AGBounce)o).setW(parseInt(objectW.getText()));
				break;
				case H:
					if (o.getType() == AGObject.GIZMO) ((AGGizmo)o).setH(parseInt(objectH.getText()));
					if (o.getType() == AGObject.BOUNCE) ((AGBounce)o).setH(parseInt(objectH.getText()));
				break;
				case VAL:
					if (o.getType() == AGObject.TIMER) ((AGTimer)o).setVal(parseInt(objectVal.getText()));
					if (o.getType() == AGObject.COUNTER) ((AGCounter)o).setVal(parseInt(objectVal.getText()));
				break;
				case FX:
					((AGFlipper)o).setX(parseInt(objectFX.getText()));
				break;
				case FY:
					((AGFlipper)o).setY(parseInt(objectFY.getText()));
				break;
			}
			imageView.repaint();
		}

	}

	public JComponent createComponents() {
		AGObject dummy = new AGObject();
		dummy.setName("[NO OBJECT SET]");
		AGObject.setDummy(dummy);

		jfc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) { return true; }
				String ext = ".";
				String s = f.getName();
				int i = s.lastIndexOf('.');
				if (i > 0 && i < s.length() - 1) ext = s.substring(i + 1).toLowerCase();
				return ext.equals("xagt");
			}
			public String getDescription() { return "Table source files"; }
		});

		ifc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) { return true; }
				String ext = ".";
				String s = f.getName();
				int i = s.lastIndexOf('.');
				if (i > 0 && i < s.length() - 1) ext = s.substring(i + 1).toLowerCase();
				return ext.equals("jpg") || ext.equals("gif") || ext.equals("png") || ext.equals("bmp");
			}
			public String getDescription() { return "Image files"; }
		});

		// Table image view
		byte[] icmR = { -1, 0,  -1, -96,  0,   0, -1, -96 };
		byte[] icmG = { -1, 0,   0,   0,  0,   0,  0,   0 };
		byte[] icmB = { -1, 0,   0,   0, -1, -96, -1, -96 };
		int zoom = 4;
		tableImage = new BufferedImage(AGTable.XSIZE, AGTable.YSIZE, BufferedImage.TYPE_BYTE_INDEXED, new IndexColorModel(8, 8, icmR, icmG, icmB));
		imageView = new AGTablePaint(tableImage, objectList);
		imageView.setZoom(zoom);
		byte[] tableData = ((DataBufferByte)tableImage.getRaster().getDataBuffer()).getData();
		doc.setTable(tableData);
		spriteEditor = new AGSpriteEditor(imageView);

		// Full list
		JScrollPane objectListScroller = new JScrollPane(objectList);
		objectListScroller.setPreferredSize(new Dimension(200, 250));

		JPanel objectListControls = new JPanel();
		objectListControls.add(newObject);
		objectListControls.add(deleteObject);
		objectListControls.add(objectUp);
		objectListControls.add(objectDown);

		JPanel objectListPanel = new JPanel(new BorderLayout());
		objectListPanel.add(objectListControls, BorderLayout.NORTH);
		objectListPanel.add(objectListScroller, BorderLayout.CENTER);
		objectListPanel.setBorder(BorderFactory.createTitledBorder("Objects"));

		// Properties
		dummyProperty.add(new JLabel("Table name:"));
		dummyProperty.add(tableName);
		objectPropertyPanel.add(dummyProperty, "None");

		renamePanel.add(new JLabel("Name:"));
		renamePanel.add(renameObject);

		Insets textInsets = new Insets(1, 1, 1, 1);
		objectX.setMargin(textInsets);
		objectY.setMargin(textInsets);
		objectW.setMargin(textInsets);
		objectH.setMargin(textInsets);
		dimensionProperty.add(new JLabel("X:"));
		dimensionProperty.add(objectX);
		dimensionProperty.add(new JLabel("Y:"));
		dimensionProperty.add(objectY);
		dimensionProperty.add(new JLabel("W:"));
		dimensionProperty.add(objectW);
		dimensionProperty.add(new JLabel("H:"));
		dimensionProperty.add(objectH);

		JPanel spriteProperties = new JPanel(new GridLayout(3, 1));
		spriteProperties.add(spriteActive);
		spriteProperties.add(spritePresent);
		spriteProperties.add(spriteVisible);
		spritePanel.add(spriteProperties, BorderLayout.CENTER);
		spritePanel.add(spriteEditor, BorderLayout.EAST);
		spritePanel.add(spriteFit, BorderLayout.SOUTH);

		valueProperty.add(new JLabel("Initial value:"));
		valueProperty.add(objectVal);

		objectDir.setPreferredSize(new Dimension(70, 25));
		flipperPanel.add(new JLabel("X:"));
		flipperPanel.add(objectFX);
		flipperPanel.add(new JLabel("Y:"));
		flipperPanel.add(objectFY);
		flipperPanel.add(objectDir);

		gizmoProperty.setPreferredSize(new Dimension(200, 300));
		timerProperty.setPreferredSize(new Dimension(200, 300));
		counterProperty.setPreferredSize(new Dimension(200, 300));
		bounceProperty.setPreferredSize(new Dimension(200, 300));
		flipperProperty.setPreferredSize(new Dimension(200, 300));
		commentProperty.setPreferredSize(new Dimension(200, 300));
		objectPropertyPanel.add(gizmoProperty, "Gizmo");
		objectPropertyPanel.add(timerProperty, "Timer");
		objectPropertyPanel.add(counterProperty, "Counter");
		objectPropertyPanel.add(bounceProperty, "Bounce");
		objectPropertyPanel.add(flipperProperty, "Flipper");
		objectPropertyPanel.add(commentProperty, "Comment");

		// Assembling object bar
		JSplitPane objectBar = new JSplitPane(JSplitPane.VERTICAL_SPLIT, objectListPanel, objectPropertyPanel);
		objectBar.setOneTouchExpandable(true);

		// Everything
		JPanel all = new JPanel(new BorderLayout());
		all.add(new AGBrushBar(imageView), BorderLayout.WEST);
		all.add(new JScrollPane(imageView), BorderLayout.CENTER);
		all.add(objectBar, BorderLayout.EAST);

		return all;
	}

	public JMenuBar mainMenu() {
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(fileNew);
		fileMenu.add(fileOpen);
		fileMenu.add(fileSave);
		fileMenu.add(fileExport);

		JMenu zoomMenu = new JMenu("Zoom");
		zoomMenu.add(zoom1x);
		zoomMenu.add(zoom15x);
		zoomMenu.add(zoom2x);
		zoomMenu.add(zoom4x);

		JMenu tableMenu = new JMenu("Table");
		tableMenu.add(tableLayers);
		tableMenu.add(tableImport);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(zoomMenu);
		menuBar.add(tableMenu);

		return menuBar;
	}

	public void addActions() {
		AGObjectActions oa = new AGObjectActions();
		AGMenuActions ma = new AGMenuActions();
		// Menu
		fileNew.addActionListener(ma);
		fileOpen.addActionListener(ma);
		fileSave.addActionListener(ma);
		fileExport.addActionListener(ma);
		zoom1x.addActionListener(ma);
		zoom15x.addActionListener(ma);
		zoom2x.addActionListener(ma);
		zoom4x.addActionListener(ma);
		tableLayers.addActionListener(ma);
		tableImport.addActionListener(ma);

		// Objects
		newObject.addActionListener(oa);
		deleteObject.addActionListener(oa);
		objectUp.addActionListener(oa);
		objectDown.addActionListener(oa);
		renameObject.addActionListener(oa);
		spriteFit.addActionListener(oa);
		spriteActive.addActionListener(oa);
		spritePresent.addActionListener(oa);
		spriteVisible.addActionListener(oa);
		objectDir.addActionListener(oa);

		ListSelectionModel objectListSelectionModel = objectList.getSelectionModel();
		objectListSelectionModel.addListSelectionListener(new AGObjectListSelectionHandler());

		objectX.getDocument().addDocumentListener(new AGDocumentListener(AGDocumentListener.X));
		objectY.getDocument().addDocumentListener(new AGDocumentListener(AGDocumentListener.Y));
		objectW.getDocument().addDocumentListener(new AGDocumentListener(AGDocumentListener.W));
		objectH.getDocument().addDocumentListener(new AGDocumentListener(AGDocumentListener.H));
		objectVal.getDocument().addDocumentListener(new AGDocumentListener(AGDocumentListener.VAL));
		objectFX.getDocument().addDocumentListener(new AGDocumentListener(AGDocumentListener.FX));
		objectFY.getDocument().addDocumentListener(new AGDocumentListener(AGDocumentListener.FY));
	}

}
