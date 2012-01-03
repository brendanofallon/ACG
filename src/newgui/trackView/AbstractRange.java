package newgui.trackView;


public class AbstractRange implements Range {

	final int begin;
	final int end;
	
	public AbstractRange(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
	
	public boolean intersects(Range otherRange) {
		return intersects(otherRange.getBegin(), otherRange.getEnd());
	}
	
	public boolean intersects(int begin, int end) {
		return end <= getBegin() || begin >= getEnd() ? false : true;
	}
	
	public boolean contains(int pos) {
		return pos >= getBegin() && pos < getEnd();
	}
	
	@Override
	public int compare(Range o1, Range o2) {
		return o1.getBegin() - o2.getBegin();
	}

	@Override
	public int getBegin() {
		return begin;
	}

	@Override
	public int getEnd() {
		return end;
	}
	
	public String toString() {
		return "[" + begin + " - " + end + "]";
	}
	
	public int hashCode() {
		return begin;
	}
}
