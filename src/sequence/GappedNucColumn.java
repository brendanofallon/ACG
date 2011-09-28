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
