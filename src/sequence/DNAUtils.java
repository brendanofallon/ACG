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

public class DNAUtils implements Alphabet {

	//Define these in alphabetical order
	public static final int T = 3;
	public static final int C = 1;
	public static final int A = 0;
	public static final int G = 2;
	public static final int N = 4;
	public static final int GAP = 5;
	
	public static int intForBase(char base) {
		switch(base) {
		case 'A' : return A;
		case 'C' : return C;
		case 'T' : return T;
		case 'G' : return G;
		case '-' : return GAP;
		case '?' : return GAP;
		case 'N' : return N;
		}
		throw new IllegalArgumentException("Invalid base : '" + base + "'");
	}
	
	public static char baseForInt(int i) {
		if (i==A) return 'A';
		if (i==C) return 'C';
		if (i==T) return 'T';
		if (i==G) return 'G';
		if (i==N) return 'N';
		if (i==GAP) return '-';
		throw new IllegalArgumentException("Invalid state number : " + i);
	}


	public int symbolToInt(char base) {
		return intForBase(base);
	}


	public char intToSymbol(int i) {
		return baseForInt(i);
	}


	public int getSymbolCount() {
		return 5;
	}

	public static char[] basesForVals(int[] vals) {
		char[] chars = new char[vals.length];
		for(int i=0; i<vals.length; i++)
			chars[i] = baseForInt(vals[i]);
		return chars;
	}
}
