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


package arg;

import sequence.BasicSequenceAlignment;
import sequence.DataMatrix;
import sequence.Sequence;

/**
 * Matrix of pairwise distances of a group of sequences. Currently this is just used for building UPGMA trees 
 * for starting trees. 
 * @author brendano
 *
 */
public class DistanceMatrix {

	double[][] mat;
	String[] taxLabels;
	
	public DistanceMatrix(DataMatrix alignmentData) {
		createMatrix(alignmentData);
		taxLabels = new String[alignmentData.getSequenceCount()];
		for(int i=0; i<alignmentData.getSequenceCount(); i++) {
			taxLabels[i] = new String( alignmentData.getSequenceLabel(i) );
		}		
	}
	
	public DistanceMatrix(BasicSequenceAlignment aln) {
		createMatrix(aln);
		taxLabels = new String[aln.getSequenceCount()];
		for(int i=0; i<aln.getSequenceCount(); i++) {
			taxLabels[i] = new String( aln.getSequenceLabel(i) );
		}
	}
	
	/**
	 * Obtain distance of element i from element j
	 * @param i
	 * @param j
	 * @return
	 */
	public double getDistance(int i, int j) {
		return mat[i][j];
	}
	
	/**
	 * Combine row i and j, setting as distance to element k (dist[i,k]+dist[j,k)/2.0
	 * @param i
	 * @param j
	 */
	public void merge(int i, int j) {
		int min = Math.min(i, j); //We need to know which one is the big & small one to keep the indices straight
		int max = Math.max(i, j);
		
		double[] newRow = new double[mat.length];
		for(int k=0; k<min; k++) {
			newRow[k] = (mat[i][k] + mat[j][k])/2.0;
		}
		for(int k=min+1; k<mat.length; k++) {
			newRow[k-1] = (mat[i][k] + mat[j][k])/2.0;
		}
		newRow[max-1] = 0;
		
		mat = cloneRemoveIndex(min, mat);
		
		//Row/column min has been removed from matrix, so we now replace row max-1
		for(int k=0; k<mat.length; k++) {
			mat[k][max-1] = newRow[k];
			mat[max-1][k] = newRow[k];
		}
		
	}
	
	/**
	 * Clones the given matrix but removes both column and row given by indexToRemove
	 * @param indexToRemove
	 * @return
	 */
	public static double[][] cloneRemoveIndex(int indexToRemove, double[][] src) {
		double[][] newMat = new double[src.length-1][src.length-1];
		int i = 0;
		int newRow = 0;
		int newCol = 0;
		for(i=0; i<indexToRemove; i++) {
			newCol = 0;
			for(int j=0; j<indexToRemove; j++) {
				newMat[newRow][newCol] = src[i][j];
				newCol++;
			}
			for(int j=indexToRemove+1; j<src.length; j++) {
				newMat[newRow][newCol] = src[i][j];
				newCol++;
			}
			
			newRow++;
		}
		
		for(i=indexToRemove+1; i<src.length; i++) {
			newCol = 0;
			for(int j=0; j<indexToRemove; j++) {
				newMat[newRow][newCol] = src[i][j];
				newCol++;
			}
			for(int j=indexToRemove+1; j<src.length; j++) {
				newMat[newRow][newCol] = src[i][j];
				newCol++;
			}
			newRow++;
		}
		
		return newMat;
	}
	
	/**
	 * Create the distance matrix from the given alignment
	 * @param aln
	 */
	private void createMatrix(BasicSequenceAlignment aln) {
		mat = new double[aln.getSequenceCount()][aln.getSequenceCount()];
		
		for(int i=0; i<mat.length; i++) {
			for(int j=i+1; j<mat.length; j++) {
				double dist = dist(aln.getSequence(i), aln.getSequence(j));
				mat[i][j] = dist;
				mat[j][i] = dist;
			}
		}
	}
	
	/**
	 * Create the initial distance matrix from the supplied DataMatrix
	 * @param matrix
	 */
	private void createMatrix(DataMatrix matrix) {
		mat = new double[matrix.getSequenceCount()][matrix.getSequenceCount()];
		
		for(int i=0; i<mat.length; i++) {
			for(int j=i+1; j<mat.length; j++) {
				double dist = dist(matrix, i, j); 
				mat[i][j] = dist;
				mat[j][i] = dist;
			}
		}
	}
	
	/**
	 * Number of rows / columns in this (square) matrix
	 * @return
	 */
	public int size() {
		return mat.length;
	}
	
	
	public DistResult findLowestDist() {
		int lowI = -1;
		int lowJ = -1;
		double low = Double.POSITIVE_INFINITY;
		for(int i=0; i<mat.length; i++) {
			for(int j=i+1; j<mat.length; j++) {
				if (mat[i][j] < low) {
					lowI = i;
					lowJ = j;
					low = mat[i][j];
				}
			}
		}
		
		DistResult res = new DistResult();
		res.i = lowI;
		res.j = lowJ;
		res.dist = low;
		return res;
	}
	
	/**
	 * Returns mean number of sites at which strings differ
	 * @param a
	 * @param b
	 * @return
	 */
	private static double dist(Sequence a, Sequence b) {
		int difs =0;
		for(int i=0; i<a.getLength(); i++) {
			if (a.getCharAt(i) != b.getCharAt(i))
				difs++;
		}

		return (double)difs / (double)a.getLength();
	}
	
	private static double dist(DataMatrix data, int i, int j) {
		int difs =0;
		for(int col=0; col<data.getTotalColumnCount(); col++) {
			if (data.sequencesDiffer(col, i, j))
				difs++;
		}

		return (double)difs / (double)data.getTotalColumnCount();
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		for(int i=0; i<mat.length; i++) {
			for(int j=0; j<mat.length; j++) {
				str.append(mat[i][j] + "\t");
			}
			str.append("\n");
		}
		return str.toString();
	}
	
	/**
	 * Structure to store results of finding lowest distance between taxa
	 * @author brendano
	 *
	 */
	class DistResult {
		int i;
		int j;
		double dist;
	}
	
	
}
