package mcmc.mc3;

import java.util.List;

import parameter.AbstractParameter;

/**
 * Model specifying how the chains in an MC3 run are to be 'heated'. Right now the only implementation of this is 
 * the 'ExpChainHeats' model, in which chain i has temperature exp( -l *i ), where i is a parameter
 * @author brendano
 *
 */
public interface ChainHeats {

	public static final String XML_CHAINNUMBER = "numberOfChains";

	/**
	 * Get the heat for chain 'chainNumber'
	 * @param chainNumber
	 * @return
	 */
	public double getHeat(int chainNumber);
	
	/**
	 * Return the number of heats stored in this ChainHeats object
	 * @return
	 */
	public int getHeatCount();
	
	/**
	 * Tell the heating model that a proposed swap was rejected
	 */
	public void tallyRejection();
	
	/**
	 * Tell the heating model that a proposed swap was accepted
	 */
	public void tallyAcceptance();
	
	/**
	 * In a typical MC3 run we call clearObjectMap() on the XML loader so we can create now instances of all the objects. 
	 * However, this can potentially interfere with ChainHeating stuff, since we DONT want to create multiple versions of
	 * these things. They're 'global' with respect to MC objects. So here we can return a list of all parameters we depend
	 * on (for instance, the lambda chain heating parameter) to prevent these from being cleared. 
	 * @return
	 */
	List<AbstractParameter<?>> getGlobalParameters();  
	
}
