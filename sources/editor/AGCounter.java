package AGTEdit; import javax.swing.*; 
public class AGCounter extends AGObject { 
	private DefaultListModel events = new DefaultListModel();
	
	public DefaultListModel getEvents() { return events; }
	private int val;

	AGCounter() { super(); name = "Counter-" + id; }

	public void setVal(int n) { val = (n > 0 && n < 256) ? n : 0; }
	public int getVal() { return val; }

	public int getType() { return COUNTER; }

	public String toString() { return name + " [" + val + "]"; }
} 
