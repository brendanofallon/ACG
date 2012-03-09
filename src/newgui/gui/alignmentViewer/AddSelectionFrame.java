package newgui.gui.alignmentViewer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AddSelectionFrame.java
 *
 * Created on 22-Nov-2009, 8:39:06 PM
 */

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;

import displayPane.DisplayPane;
import display.Display;

/**
 *
 * @author brendan
 */
public class AddSelectionFrame extends javax.swing.JFrame {

	DisplayPane displayPane;
	SGContentPanelDisplay sgDisplay;

    public AddSelectionFrame(DisplayPane displayPane, SGContentPanelDisplay sgDisplay) {
    	this.displayPane = displayPane;
    	this.sgDisplay = sgDisplay;
        initComponents();
        setLocationRelativeTo(null);
    }

    public void setVisible(boolean visible) {
    	
    	if (visible) {
    		//Returns a list of all SequenceGroupDisplays currently being displayed
    		//Use it to make a new ListModel so a user can pick one 
    		List<Display> displays = displayPane.getDisplaysOfClass(SGContentPanelDisplay.class);
    		jList1.setModel( new DisplayListModel(displays) );

    		jScrollPane1.setViewportView(jList1);

    		if (sgDisplay.getNumSelectedColumns()>0 && sgDisplay.getNumSelectedRows()==0) {
    			if (sgDisplay.getNumSelectedColumns()==1) 
    				selectionInfoLabel.setText(" 1 column selected");
    			else 
    				selectionInfoLabel.setText(sgDisplay.getNumSelectedColumns() + " columns selected");
    		}
    		if (sgDisplay.getNumSelectedRows()>0) {
    			if (sgDisplay.getNumSelectedRows()==1)
    				selectionInfoLabel.setText(" 1 sequence selected");
    			else 
    				selectionInfoLabel.setText(sgDisplay.getNumSelectedRows() + " sequences selected");
    		}
    	}
    	pack();
    	super.setVisible(visible);
    }

    private void initComponents() {
        cancelButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        selectionInfoLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        addButton.setText("Add Selection");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(cancelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addComponent(addButton))
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
            .addGroup(layout.createSequentialGroup()
                .addComponent(selectionInfoLabel)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectionInfoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(addButton)))
        );

        pack();
    }

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	try {
    		DisplayListModel model = (DisplayListModel)jList1.getModel();
    		SGContentPanelDisplay targetDisplay = (SGContentPanelDisplay)model.getElementAt( jList1.getSelectedIndex() );
    		
    		if (sgDisplay.getNumSelectedRows()>0 ) { 
    		//	.displayData( new DisplayData(null, currentSG.getRows(table.getSelectedRows())), "Selected Sequences");
    		}
    		else {
    		//	sunfishParent.displayData( new DisplayData(null, currentSG.getCols(table.getSelectedColumns())), "Selected Columns");    				
    		}
    		
    	}
    	catch (ClassCastException cce ) {
    		
    	}
    	
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	this.setVisible(false);
    }


    private class DisplayListModel extends AbstractListModel {

    	List<Display> displays;
    	
    	public DisplayListModel(List<Display> displays2) {
    		this.displays = displays2;
    	}
		
		public Object getElementAt(int index) {
    		return displays.get(index);
		}


		public int getSize() {
			return displays.size();			
		}
    	
    }

    private javax.swing.JButton addButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel selectionInfoLabel;


}
