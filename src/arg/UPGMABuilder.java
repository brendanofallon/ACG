package arg;

import java.util.ArrayList;
import java.util.List;

import arg.DistanceMatrix.DistResult;
import sequence.Alignment;

/**
 * Tool for building a UPGMA tree from a group of sequences. May be useful for generating starting trees.
 * @author brendano
 *
 */
public class UPGMABuilder {

	Alignment aln = null;
	
	public UPGMABuilder(Alignment aln) {
		this.aln = aln;
	}

	/**
	 * Generate a UPGMA tree from the alignment this object was created with, and return the root as a coal node
	 * @param owner
	 * @return
	 */
	public CoalNode getRoot(ARG owner) {
		DistanceMatrix mat = new DistanceMatrix(aln);

		List<TipNode> tips = new ArrayList<TipNode>();
		List<ARGNode> nodes = new ArrayList<ARGNode>();
		
		List<ARGNode> allNodes = new ArrayList<ARGNode>();
		
		for(int j=0; j<aln.getSequenceCount(); j++) {
			TipNode tip = new TipNode(owner);
			tip.proposeHeight(0.0);
			tip.setLabel(aln.getSequenceLabel(j));
			tips.add(tip);
			nodes.add(tip);
			allNodes.add(tip);
		}
		
		double totHeight = 0;
		while(mat.size()>1) {
			DistResult res = mat.findLowestDist();
			
			ARGNode ci = nodes.get(res.i);
			ARGNode cj = nodes.get(res.j);
			if (res.dist==0)
				res.dist += 1e-8; //Prevents 0-length branches
			
			CoalNode newParent = new CoalNode(owner);
			allNodes.add(newParent);
			newParent.proposeHeight(totHeight + res.dist);
			newParent.proposeOffspring(0, ci);
			newParent.proposeOffspring(1, cj);
			ci.proposeParent(0, newParent);
			cj.proposeParent(0, newParent);
			
			//Make sure we adjust node list in exactly the same way as the distance matrix
			mat.merge(res.i, res.j);
			nodes.set(Math.max(res.i, res.j), newParent); //Be sure to set this one before we remove the other
			nodes.remove(Math.min(res.i, res.j));
			
			
			//System.out.println("Matrix :\n " + mat);
			
			totHeight += res.dist;
		}
		
		return (CoalNode)nodes.get(0);
	}
	
//	public ARG getTree() {
//		ARG arg = new ARG();
//	
//		arg.initializeFromNodeList(allNodes, aln.getSiteCount(), false);
//		return arg;
//	}
	
}
