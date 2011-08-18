package sequence;

public class GappedNucColumn implements CharacterColumn {

	private final char[] chars;
	
	public GappedNucColumn(char[] chars) {
		this.chars = chars;
	}
	
	public char getSymbol(int which) {
		return chars[which];
	}
	

	public boolean isEqual(CharacterColumn col) {
		return isEqualBetween(col, 0, chars.length);	
	}


	public boolean isEqualBetween(CharacterColumn col, int start, int end) {
		for(int i=start; i<end; i++) {
			if (col.getSymbol(i) != chars[i]) {
				return false;
			}
		}
		return true;
	}

	public boolean isEqualAt(CharacterColumn col, int row) {
		return (col.getSymbol(row) == chars[row]); 
	}
	

	@Override
	public int size() {
		return chars.length;
	}
	
}
