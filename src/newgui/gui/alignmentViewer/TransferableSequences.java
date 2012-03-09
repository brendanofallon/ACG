package newgui.gui.alignmentViewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import element.sequence.*;

/**
 * A transferable wrapper for a sequence group. When parts of sequence groups are cut or pasted, this is what
 * is transferred. 
 * @author brendan
 *
 */
public class TransferableSequences implements Transferable {

	SequenceGroup seqs;

	public TransferableSequences(SequenceGroup seqs) {
		this.seqs = seqs;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] flavs = {new DataFlavor(SequenceGroup.class, "SequenceGroup"), DataFlavor.stringFlavor};
		return flavs;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor.getRepresentationClass() == SequenceGroup.class || flavor.isFlavorTextType()) {
			return true;
		}
		else
			return false;
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		
		if (flavor.getRepresentationClass() == SequenceGroup.class)
			return seqs;
		
		if (flavor.isFlavorTextType()) {
			StringBuffer strb = new StringBuffer();
			for(int i=0; i<seqs.size(); i++) {
				strb.append(">" +seqs.get(i).getName() + "\n");
				strb.append( seqs.get(i).toString()+"\n");
			}
			return strb.toString();
		
		}
			
		return null;
	}

	
}
