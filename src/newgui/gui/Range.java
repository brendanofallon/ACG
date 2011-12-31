package newgui.gui;

import java.util.Comparator;

public interface Range extends Comparator<Range> {

	public int getBegin();
	
	public int getEnd();
	
	public boolean contains(int pos);
	
	public boolean intersects(int begin, int end);
	
}
