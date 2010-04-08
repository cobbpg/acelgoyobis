package AGTEdit;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class AGEventEditor extends JPanel implements ActionListener {

	private JButton newEvent = new JButton("New");
	private JButton deleteEvent = new JButton("Delete");
	private JButton editEvent = new JButton("Edit");
	private JList eventList = new JList();
	private AGTable table;

	private class AGEventDialog extends JDialog implements ActionListener {

		private AGEvent event;
		private JButton actionOK = new JButton("OK");
		private JButton actionCancel = new JButton("Cancel");
		private JRadioButton[] typeButtons = new JRadioButton[AGEvent.count()];
		private int objectListType = AGObject.OBJECT;
		private JComboBox objectList = new JComboBox(table.getObjects(objectListType));
		private JTextField param1 = new JTextField(5);
		private JTextField param2 = new JTextField(5);
		private boolean isNew;

		AGEventDialog(AGEvent e, boolean n) {
			super();
			event = e;
			isNew = n;
			if(isNew) event.setType(AGEvent.SCORE);
			else {
				newList(event.getObjectType());
				objectList.setSelectedItem(event.getObject());
			}
			param1.setText(Integer.toString(event.getVal()));
			param2.setText(Integer.toString(event.getBonus()));

			Container p = getContentPane();
			setModal(true);
			setResizable(false);
			setLocationRelativeTo(null);
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			setTitle("Event properties");

			ButtonGroup choice = new ButtonGroup();
			JPanel types = new JPanel(new GridLayout(typeButtons.length / 2 + 1, 2));
			for(int i = 1; i < AGEvent.count(); i++) {
				typeButtons[i] = new JRadioButton(AGEvent.getName(i), (i == ((isNew) ? AGEvent.SCORE : event.getType())));
				choice.add(typeButtons[i]);
				types.add(typeButtons[i]);
				typeButtons[i].addActionListener(this);
			}
			actionOK.addActionListener(this);
			actionCancel.addActionListener(this);

			JPanel params = new JPanel();
			params.add(new JLabel("Par1: "));
			params.add(param1);
			params.add(new JLabel("Par2: "));
			params.add(param2);

			JPanel obj = new JPanel();
			obj.add(new JLabel("Object: "));
			obj.add(objectList);

			JPanel allparams = new JPanel(new BorderLayout());
			allparams.add(params, BorderLayout.NORTH);
			allparams.add(objectList, BorderLayout.SOUTH);

			JPanel actions = new JPanel();
			actions.add(actionOK);
			actions.add(actionCancel);

			p.setLayout(new BorderLayout());
			p.add(types, BorderLayout.NORTH);
			p.add(allparams, BorderLayout.CENTER);
			p.add(actions, BorderLayout.SOUTH);

			pack();
			show();
		}

		private int parseInt(String s) {
			int res;
			try { res = Integer.parseInt(s); }
			catch (Exception e) { res = 0; }
			return res;
		}

		private void newList(int t) {
			if (t != objectListType) {
				objectList.setModel(new DefaultComboBoxModel(table.getObjects(t)));
				objectListType = t;
			}
		}

		public void actionPerformed(ActionEvent e) {
			Object s = e.getSource();

			if (s.equals(actionOK)) {
				for(int i = 1; i < typeButtons.length; i++)
					if (typeButtons[i].isSelected()) event.setType(i);
				event.setObject((AGObject)objectList.getSelectedItem());
				event.setVal(parseInt(param1.getText()));
				event.setBonus(parseInt(param2.getText()));
				dispose();
			} else
			if (s.equals(actionCancel)) {
				if (isNew) event.setType(AGEvent.INVALID);
				dispose();
			} else for(int i = 1; i < typeButtons.length; i++)
				if (s.equals(typeButtons[i])) newList(AGEvent.getObjectType(i));

		}

	}

	AGEventEditor(AGTable t) {
		super(new BorderLayout());
		table = t;

		JPanel controlPanel = new JPanel();
		controlPanel.add(newEvent);
		controlPanel.add(deleteEvent);
		controlPanel.add(editEvent);

		add(controlPanel, BorderLayout.NORTH);
		add(new JScrollPane(eventList), BorderLayout.CENTER);
		setBorder(BorderFactory.createTitledBorder("Events"));
		setPreferredSize(new Dimension(200, 200));

		newEvent.addActionListener(this);
		deleteEvent.addActionListener(this);
		editEvent.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		DefaultListModel l = (DefaultListModel)eventList.getModel();

		if (s.equals(newEvent)) {
			AGEvent ne = createEvent();
			if (ne.getType() != AGEvent.INVALID) {
				if (eventList.getSelectedIndex() != -1)
					l.insertElementAt(ne, eventList.getSelectedIndex());
				else l.addElement(ne);
			}
		} else

		if (s.equals(deleteEvent)) {
			while (eventList.getSelectedIndex() != -1)
				l.removeElementAt(eventList.getSelectedIndex());
		} else

		if (s.equals(editEvent)) {
			if (eventList.getSelectedIndex() != -1)
				editEvent((AGEvent)eventList.getSelectedValue());
			eventList.paint(eventList.getGraphics());
		}

	}

	public void setContents(DefaultListModel el) { eventList.setModel(el); }

	private AGEvent createEvent() {
		AGEvent e = new AGEvent();
		new AGEventDialog(e, true);
		return e;
	}

	private void editEvent(AGEvent e) {
		new AGEventDialog(e, false);
	}

}

