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


package testing;

import dlCalculation.substitutionModels.BaseFrequencies;
import dlCalculation.substitutionModels.TN93Matrix;

public class SubstModelTest {

	public static double[] getBEASTTN93(double distance, double kappa1, double kappa2, double piA, double piC, double piG, double piT) {
		double [] matrix = new double[16];
		double pIR, piY;
		double p1a;
		double p0a;
		double p3b;
		double p2b;
		double a;
		double b;
		double p1aa;
		double p0aa;
		double p3bb;
		double p2bb;
		double p1aIsa;
		double p0aIsa;
		double p3bIsb;
		double p2bIsb;
		double k1g;
		double k1a;
		double k2t;
		double k2c;
		double subrateScale;
		pIR = piA + piG;
		piY = piC + piT;


		double k1 = kappa1;
		double k2 = kappa2;

		//		        System.out.println(getModelName() + " Using " + k1 + " " + k2);
		// A hack until I get right this boundary case. gives results accurate to 1e-8 in the P matrix
		// so should be OK even like this.
		if (k1 == 1) {
			k1 += 1E-10;
		}
		if (k2 == 1) {
			k2 += 1e-10;
		}

		double l1 = k1 * k1 * pIR + k1 * (2 * piY - 1) - piY;
		double l2 = k2 * k2 * piY + k2 * (2 * pIR - 1) - pIR;

		p1a = piG * l1;
		p0a = piA * l1;
		p3b = piT * l2;
		p2b = piC * l2;

		//a = -(k1 * pIR + piY);
		//b = -(k2 * piY + pIR);

		p1aa = p1a / -(k1 * pIR + piY);
		p0aa = p0a / -(k1 * pIR + piY);
		p3bb = p3b / -(k2 * piY + pIR);
		p2bb = p2b / -(k2 * piY + pIR);

		p1aIsa = p1a / (1 + -(k1 * pIR + piY));
		p0aIsa = p0a / (1 + -(k1 * pIR + piY));
		p3bIsb = p3b / (1 + -(k2 * piY + pIR));
		p2bIsb = p2b / (1 + -(k2 * piY + pIR));

		k1g = k1 * piG;
		k1a = k1 * piA;
		k2t = k2 * piT;
		k2c = k2 * piC;

		subrateScale = 2 * (k1 * piA * piG + k2 * piC * piT + pIR * piY);

		System.out.println("Rate scaling factor: " + subrateScale);
		distance /= subrateScale;
		
	        double[] q = {
	                0, k1g, piC, piT,
	                k1a, 0, piC, piT,
	                piA, piG, 0, k2t,
	                piA, piG, k2c, 0
	        };

	        q[0] = -(q[1] + q[2] + q[3]);
	        q[5] = -(q[4] + q[6] + q[7]);
	        q[10] = -(q[8] + q[9] + q[11]);
	        q[15] = -(q[12] + q[13] + q[14]);

	        double[] fa0 = {
	                1 + q[0] - p1aa, q[1] + p1aa, q[2], q[3],
	                q[4] + p0aa, 1 + q[5] - p0aa, q[6], q[7],
	                q[8], q[9], 1 + q[10] - p3bb, q[11] + p3bb,
	                q[12], q[13], q[14] + p2bb, 1 + q[15] - p2bb
	        };


	        double[] fa1 = {
	                -q[0] + p1aIsa, -q[1] - p1aIsa, -q[2], -q[3],
	                -q[4] - p0aIsa, -q[5] + p0aIsa, -q[6], -q[7],
	                -q[8], -q[9], -q[10] + p3bIsb, -q[11] - p3bIsb,
	                -q[12], -q[13], -q[14] - p2bIsb, -q[15] + p2bIsb};

	        double et = Math.exp(-distance);

	        for (int k = 0; k < 16; ++k) {
	            fa1[k] = fa1[k] * et + fa0[k];
	        }

	        final double eta = Math.exp(distance * -(k1 * pIR + piY));
	        final double etb = Math.exp(distance * -(k2 * piY + pIR));

	        double za = eta / (-(k1 * pIR + piY) * (1 + -(k1 * pIR + piY)));
	        double zb = etb / (-(k2 * piY + pIR) * (1 + -(k2 * piY + pIR)));
	        double u0 = p1a * za;
	        double u1 = p0a * za;
	        double u2 = p3b * zb;
	        double u3 = p2b * zb;

	        fa1[0] += u0;
	        fa1[1] -= u0;
	        fa1[4] -= u1;
	        fa1[5] += u1;

	        fa1[10] += u2;
	        fa1[11] -= u2;
	        fa1[14] -= u3;
	        fa1[15] += u3;

	        // transpose 2 middle rows and columns
	        matrix[0] = fa1[0];
	        matrix[1] = fa1[2];
	        matrix[2] = fa1[1];
	        matrix[3] = fa1[3];
//	        matrix[4] = fa1[8];
//	        matrix[5] = fa1[10];
//	        matrix[6] = fa1[9];
//	        matrix[7] = fa1[11];
//	        matrix[8] = fa1[4];
//	        matrix[9] = fa1[6];
//	        matrix[10] = fa1[5];
//	        matrix[11] = fa1[7];
//	        matrix[12] = fa1[12];
//	        matrix[13] = fa1[14];
//	        matrix[14] = fa1[13];
//	        matrix[15] = fa1[15];

	        return matrix;
	}
	
	
	

	public static void main(String[] args) {
		double[] beastTN93 = getBEASTTN93(0.01, 1.000000001, 1.000000001, 0.25, 0.25, 0.25, 0.25);
		
		int tot = 0;
		for(int i=0; i<1; i++) {
			for(int j=0; j<4; j++) {
				System.out.println(beastTN93[tot] + "\t");
				tot++;
			}
			System.out.println();
		}
		
		TN93Matrix myTN93 = new TN93Matrix(new BaseFrequencies(new double[]{0.3, 0.1, 0.2, 0.4}), 1.00001, 1.000001);
		myTN93.setBranchLength(100);
		double[][] myMat = myTN93.getMatrix();
		
		System.out.println("My matrix : ");
		for(int i=0; i<1; i++) {
			double sum =0;
			for(int j=0; j<4; j++) {
				System.out.println(myMat[i][j] + "\t");
				sum += myMat[i][j];
			}
			System.out.println("Sum : " + sum);
		}
		
	}
}
