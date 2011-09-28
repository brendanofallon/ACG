/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package sequence;

import java.util.BitSet;

/**
 * A column of nucleotides. Each character has only 4 possible states. These are immutable.  
 * This version doesn't allow for gap or unknown characters, and is thus deprecated. Use GappedNucColumn instead
 * @author brendan
 *
 */
public class NucColumn implements CharacterColumn {

	private final BitSet bits;
	
	private final int size;
	
	//First bit is 0..chars.length-1, next bit is in chars.length..2*chars.length-1
	//A = 0, 0
	//G = 0, 1
	//C = 1, 0
	//T = 1, 1
	
	public NucColumn(char[] chars) {
		bits = new BitSet(2*chars.length);
		size = chars.length;
		for(int i=0; i<chars.length; i++) {
			if (chars[i]=='A') {
				//dont actually do anything - both are zero
				continue;
			}
			if (chars[i]=='G') {
				bits.set(i+size);
			}
			if (chars[i]=='C') {
				bits.set(i);
			}
			if (chars[i]=='T') {
				bits.set(i);
				bits.set(i+size);
			}
		}
	}
	
	public char getSymbol(int which) {
		boolean first = bits.get(which);
		boolean second = bits.get(which+size);
		if (!first && !second) {
			return 'A';
		}
		if (!first && second) {
			return 'G';
		}
		if (first && !second) {
			return 'C';
		}
		if (first && second) {
			return 'T';
		}
		return '?';
	}
	

	public boolean isEqual(CharacterColumn col) {
		return isEqualBetween(col, 0, 2*size);	
	}


	public boolean isEqualBetween(CharacterColumn col, int start, int end) {
		for(int i=start; i<end; i++) {
			if (! isEqualAt(col, i)) {
				return false;
			}
		}
		return true;
	}

	public boolean isEqualAt(CharacterColumn col, int row) {
		return (getSymbol(row) == col.getSymbol(row) ); 
	}

	@Override
	public int size() {
		return size;
	}
	

}
