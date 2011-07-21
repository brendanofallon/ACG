package mcmc.mc3;

public interface ChainHeats {

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
	
}
