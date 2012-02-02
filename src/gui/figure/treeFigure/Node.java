package gui.figure.treeFigure;

import java.util.ArrayList;

public class Node {

	ArrayList<Node> offspring;
	Node parent = null;
	double distToParent = Double.NaN;
	String label;
	
	public Node() {
		distToParent = Double.NaN;
		offspring = new ArrayList<Node>();
	}
	
	public ArrayList<Node> getOffspring() {
		return offspring;
	}
	
	public Node getOffspring(int which) {
		return offspring.get(which);
	}
	
	public Node removeOffspring(int which) {
		Node n = offspring.remove(which);
		return n;
	}
	
	public void clearOffspring() {
		offspring.clear();
	}
	
	public boolean removeOffspring(Node kidToRemove) {
		return offspring.remove(kidToRemove);
	}
	
	
	
	public void addOffspring(Node kid) {
		offspring.add(kid);
	}
	
	public int numOffspring() {
		if (offspring==null)
			return 0;
		else 
			return offspring.size();
	}
	
	
	public void setParent(Node par) {
		parent = par;
	}
	
	public Node getParent() {
		return parent;
	}
	
	public boolean isLeaf() {
		return (offspring == null || offspring.size()==0);
	}
	
	public double getDistToParent() {
		return distToParent;
	}
	
	public double getDistToRoot() {
		if (! hasDistToParent())
			return Double.NaN;
		
		double sum = this.getDistToParent();
		Node node = this.getParent();
		
		while (node != null) {
			sum += node.getDistToParent();
			node = node.getParent();
		}
		
		return sum;
	}
	
	public void setDistToParent(double dist) {
		distToParent = dist;
	}
	
	public boolean hasDistToParent() {
		return ! Double.isNaN(distToParent);
	}
	
	public boolean hasLabel() {
		return (label != null);
	}
	
	public String getLabel() {
		if (label==null)
			return "";
		else
			return label;
	}
	
	public void setLabel(String lab) {
		label = lab;
	}
}
