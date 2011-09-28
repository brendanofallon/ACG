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


package math;

public class FastExp {

	double max = 0.0;
	double min = -20.0;
	
	int bins = 5000;
	double stepSize = (max-min)/(double)bins;
	double[] vals;
	
	double binsOverMaxMin = (double)bins/(max-min);
	
	public FastExp() {
		vals = new double[bins];
		for(int i=0; i<bins; i++) {
			double x = min+i*stepSize;
			double y = Math.exp(x);
			vals[i] = y;
		}
	}
	
	public static double exp(double x) {
		double x2 = x*x;
		return 1.0+x*(1.0+x*(0.5 + x*0.166666667+x2*0.0416666667));
//		if (x2 < 0.01)
//			return 1.0+x+x2*0.5 + x2*x*0.166666667;
//		else
//			return Math.exp(x);
//		if (x>=(max-stepSize) || x<=min) {
//			return Math.exp(x);
//		}
//		
//		double xm = x-min;
//		double xmb = xm * binsOverMaxMin;
//		int bin = (int)  ( binsOverMaxMin*xm );
//		
//		double frac = xmb-bin;
//		
//		return (1.0-frac)*vals[bin]+frac*vals[bin+1];
	}
	
	public static void main(String[] args) {
		FastExp exp = new FastExp();
		for(double x=-1; x<0; x+=0.0005) {
			double actual = Math.exp(x);
			double calc = FastExp.exp(x);
			double error = Math.abs( (actual-calc) / actual);
			System.out.println("x: " + x + "\t" + actual + "\t" + calc + "\t" + error);
		}
		
		int steps = 10000000;
		long start = System.currentTimeMillis();
		double res = 0.00000001;
		for(int i=0; i<steps; i++) {
			res += 1.000452;
		}
		long end = System.currentTimeMillis();
		System.out.println("Final " + res + " Time to compute sum : " + (end-start)/1000.0);
		
		start = System.currentTimeMillis();
		int prod = 1;
		for(int i=0; i<steps; i++) {
			prod += 3;
		}
		end = System.currentTimeMillis();
		System.out.println("Final " + prod + " Time to compute product : " + (end-start)/1000.0);
//		
//		start = System.currentTimeMillis();
//		double div = 1000;
//		for(int i=0; i<steps; i++) {
//			div /= 1.00028983;
//		}
//		end = System.currentTimeMillis();
//		System.out.println("Final " + div +  " Time to compute div : " + (end-start)/1000.0);
		
		
		
	}
	
}
