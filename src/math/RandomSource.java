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

import java.util.Map;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

/**
 * A wrapper for random number generation with a convenient static getters
 * @author brendan
 *
 */
public class RandomSource {

	public static RandomSource globalSource = null;
	
	static RandomEngine rng;
	static int seed;
	Uniform uniGenerator;
	
	/**
	 * Create a new random number generator with a seed taken from the current time,
	 *  as if by call to new RandomSource()
	 */
	public static void initialize() {
		new RandomSource();
	}
	
	public static void initializeSilently() {
		new RandomSource(false);
	}

	/**
	 * Create a new random number generator with seed as given,
	 *  as if by call to new RandomSource(seed)
	 */
	public static void initialize(int seed) {
		new RandomSource(seed);
	}
	
	public RandomSource(Map<String, String> attrMap) {
		String seedStr = attrMap.get("seed");
		
		if (seedStr != null) {
			if (globalSource != null) {
				throw new IllegalArgumentException("Global RandomSource has already been created, cannot create a new one with a different seed");
			}
			try {
				int seedAttr = Integer.valueOf(seedStr);
				initiateFromSeed(seedAttr, true);
			}
			catch (NumberFormatException nfe) {
				System.err.println("Could not parse seed from attribute with value: " + seedStr);
			}
		}
		else {
			if (globalSource == null)
				initiateFromSeed( (int)System.currentTimeMillis(), true);
		}
	}
	
	public RandomSource(int seed) {
		initiateFromSeed(seed, true);
	}
	
	public RandomSource(int seed, boolean verbose) {
		initiateFromSeed(seed, verbose);
	}
	
	public RandomSource() {
		this( (int)System.currentTimeMillis() );
	}
	
	public RandomSource(boolean verbose) {
		this( (int)System.currentTimeMillis(), verbose);
	}
	
	private void initiateFromSeed(int seed, boolean verbose) {
		if (globalSource != null) {
			System.out.println("Warning: Multiple initializations of random number generator. Initializing again with seed: " + seed);
		}
		globalSource = this;
		RandomSource.seed = seed;
		if (verbose)
			System.out.println("Initiating random source with seed: " + seed);
		
		rng = new MersenneTwister(seed);
		uniGenerator = new Uniform(rng);
	}
	
	public synchronized static Double getNextUniform() {
		return globalSource.nextUniformDouble();
	}
	
	public static RandomEngine getEngine() {
		return rng;
	}
	
	public static int getSeed() {
		return seed;
	}
	
	/**
	 * Return a new random integer between lower and upper, inclusive of both values
	 * @param lower Lower bound (inclusive)
	 * @param upper Upper bound (inclusive)
	 * @return A uniformly distributed random number 
	 */
	public static synchronized Integer getNextIntFromTo(int lower, int upper) {
		return globalSource.nextIntFromTo(lower, upper);
	}
	
	public synchronized Double nextUniformDouble() {
		return uniGenerator.nextDouble();
	}
	
	/**
	 * Return a new uniformly distributed random value from lower..upper, inclusive of both values. 
	 * @param lower Lower bound (inclusive)
	 * @param upper Upper bound (inclusive)
	 * @return A new uniformly distributed random value from lower..upper
	 */
	public synchronized Integer nextIntFromTo(int lower, int upper) {
		return uniGenerator.nextIntFromTo(lower, upper);
	}
	
}
