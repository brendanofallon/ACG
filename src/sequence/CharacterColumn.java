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

public interface CharacterColumn {
	
	/**
	 * Return the number of symbols in this row. 
	 * @return
	 */
	public int size();
	/**
	 * Return the symbol (i.e. A,C,T,G) associated with the state at the given row
	 */
	public char getSymbol(int row);
	
	/**
	 * Returns true if this character column has identical character states to the given col
	 * @param col
	 * @return
	 */
	public boolean isEqual(CharacterColumn col);
	
	/**
	 * Returns true if this character col has identical characters to the other col from start..end, in a HALF-OPEN
	 * interval which includes all sites from start..end-1 
	 * @param col
	 * @param start
	 * @param end
	 * @return
	 */
	public boolean isEqualBetween(CharacterColumn col, int start, int end);
	
	/**
	 * Returns true if this column is identical to the other at the given row
	 * @param col
	 * @param row
	 * @return
	 */
	public boolean isEqualAt(CharacterColumn col, int row);
	
}
