package sequence;

/**
 * A list of symbols that can be mapped to numbers, and vice-versa
 * @author brendan
 *
 */
public interface Alphabet {

	public int symbolToInt(char symbol);
	
	public char intToSymbol(int i);
	
	public int getSymbolCount();
	
}
