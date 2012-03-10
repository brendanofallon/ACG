package newgui.gui.alignmentViewer;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A frame that displays a few options regarding selecting columns of a sequence group
 * @author brendan
 */
public class ColumnSelectionFrame extends javax.swing.JFrame {

    SGContentPanelDisplay parentDisplay;
	
	
    public ColumnSelectionFrame(SGContentPanelDisplay display) {
    	super("Select columns");
    	
    	parentDisplay = display;
        initComponents();
        setLocationRelativeTo(null);
        this.getRootPane().setDefaultButton(doneButton);
    }

  
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        positionOne = new javax.swing.JCheckBox();
        positionTwo = new javax.swing.JCheckBox();
        positionThree = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        rangeField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        startPosBox = new javax.swing.JComboBox();
        cancelButton = new javax.swing.JButton();
        selectButton = new javax.swing.JButton();
        doneButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Positions :");

        positionOne.setSelected(true);
        positionOne.setText(" 1");

        positionTwo.setSelected(true);
        positionTwo.setText("2");

        positionThree.setSelected(true);
        positionThree.setText("3");

        jLabel2.setText("In range");

        rangeField.setText("begin-end");
        rangeField.setMinimumSize(new Dimension(100, 20));
        rangeField.setPreferredSize(new Dimension(100, 26));
        jLabel3.setFont(new java.awt.Font("DejaVu Sans", 2, 13)); // NOI18N
        jLabel3.setText("eg. 10-24, 80-end");

        jLabel4.setText("Starting from");

        startPosBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "First column", "Zero marker" }));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        selectButton.setText("Select");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        doneButton.setText("Done");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        
        JPanel mainPanel = new JPanel();
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(positionOne)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(positionTwo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(positionThree))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(rangeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(startPosBox, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cancelButton)
                                .addGap(79, 79, 79)
                                .addComponent(selectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(doneButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(23, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(positionOne)
                    .addComponent(positionTwo)
                    .addComponent(positionThree))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(rangeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(startPosBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(doneButton)
                    .addComponent(selectButton)))
        );

        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 4, 3, 4));
        add(mainPanel);
        pack();
    }// </editor-fold>

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {
        selectButtonActionPerformed(evt);
        cancelButtonActionPerformed(evt);
    }

    IntegerRange parseRange(String str) throws NumberFormatException {
    	IntegerRange range = new IntegerRange();
    	str = str.trim();
    	if (str.contains("-")) {
    		String[] parts = str.split("-");
    		
    		if (parts.length==1) {
    			//We probably had a 9- or a -10
    			int pos = str.indexOf("-");
    			if (pos==-1)
    				return null; //This should never happen (since str contains '-')
    			if (pos==0) {
    				range.start = 0;
    				range.end = Integer.parseInt(str);
    			}
    			else {
    				range.start = Integer.parseInt(str); 
    				range.end = Integer.valueOf((int) 1e9);  //This can't be Integer.max_value since we may add zeroColumn to it later... but it must be bigger than the longest sequence we're going to consider
    			}
    		}
    		
    		if (parts.length==2) {
    			if (parts[0].contains("begin") || parts[0].contains("Begin") || parts[0].contains("start") || parts[0].contains("Start"))
    				range.start = 0;
    			else {
    				range.start = Integer.parseInt(parts[0]);
    			}
    			
    			if (parts[1].contains("end") || parts[1].contains("End") )
    				range.end = Integer.valueOf((int) 1e9); //This can't be Integer.max_value since we may add zeroColumn to it later... but it must be bigger than the longest sequence we're going to consider
    			else {
    				range.end = Integer.parseInt(parts[1]);
    			}
    		}
    		
    	}//str contained a '-'
    	else {
    		//str doesn't contain a '-', so just try to parse the first int from it
    		range.start = Integer.parseInt(str);
    		range.end = range.start;
    	}
    	
    	
    	return range;
    }
    
    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	//First parse the range field
    	String[] groups = rangeField.getText().split(",");
    	ArrayList<IntegerRange> ranges = new ArrayList<IntegerRange>();
    	for(String str : groups) {
    		try {
    			//System.out.println("Parsing range : " + str);
    			IntegerRange range = parseRange(str);
    			//System.out.println("got begin: " + range.start + ".." + range.end);
    			ranges.add(range);
    		}
    		catch (NumberFormatException nfe) {
    			//I don't think we need to worry about this, but don't add the range to the list
    		}
    	}
    	
    	//If we're starting from the zero marker, we need to adjust the ranges here
    	int zeroCol = parentDisplay.getZeroColumn();
    	for (IntegerRange range : ranges) {
    		range.start += zeroCol;
    		range.end += zeroCol;
    		//System.out.println("range start: " + range.start + " end: " + range.end);
    	}
    	
    	parentDisplay.selectColumns(ranges, positionOne.isSelected(), positionTwo.isSelected(), positionThree.isSelected());
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.setVisible(false);
    }


    class IntegerRange {
    	int start;
    	int end;
    }
    
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton doneButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JCheckBox positionOne;
    private javax.swing.JCheckBox positionThree;
    private javax.swing.JCheckBox positionTwo;
    private javax.swing.JTextField rangeField;
    private javax.swing.JButton selectButton;
    private javax.swing.JComboBox startPosBox;

}
