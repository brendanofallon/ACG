package figure.series;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import topLevelGUI.SunFishFrame;

import errorHandling.ErrorWindow;
import guiWidgets.ColorSwatchButton;

/**
 * A configuration tool for figure legends, this actually allows all series currently in the figure to
 * be configured and/or removed from the figure. 
 * @author brendan
 *
 */
public class LegendConfigFrame extends JFrame {

	JPanel contentPanel;
	XYSeriesFigure figParent;
	List<JPanel> seriesPanels = new ArrayList<JPanel>();
	
	Font panelFont = new Font("Sans", Font.PLAIN, 12);
	
	public LegendConfigFrame(XYSeriesFigure parent, LegendElement legend) {
		super("Configure legend");
		this.figParent = parent;
		Font bFont = new Font("Sans", Font.BOLD, 12);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		this.add(mainPanel);
		mainPanel.setPreferredSize(new Dimension(600, 200));
		setPreferredSize(new Dimension(600, 200) );
		
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
		headerPanel.setAlignmentX(RIGHT_ALIGNMENT);
		headerPanel.setMinimumSize(new Dimension(600, 10));
		headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		JLabel area = new JLabel("Color");
		area.setFont(bFont);
		headerPanel.add(area);
		headerPanel.add(Box.createHorizontalStrut(12));
		JLabel mode = new JLabel("Mode");
		mode.setFont(bFont);
		headerPanel.add(mode);
		headerPanel.add(Box.createHorizontalStrut(100));
		JLabel marker = new JLabel("Marker");
		marker.setFont(bFont);
		headerPanel.add(marker);
		headerPanel.add(Box.createHorizontalStrut(50));
		JLabel name = new JLabel("Series Name");
		name.setFont(bFont);
		headerPanel.add(name);
		headerPanel.add(Box.createGlue());
		mainPanel.add(headerPanel);
		
		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		mainPanel.add(contentPanel);
		mainPanel.add(Box.createVerticalGlue());
		
		JPanel panel4 = new JPanel();
		panel4.setLayout(new BorderLayout());
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	cancel();
            }
        });
		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	done();
            }
        } );
		panel4.add(cancelButton, BorderLayout.WEST);
		panel4.add(doneButton, BorderLayout.EAST);
		panel4.setMaximumSize(new Dimension(1000, 30));
		mainPanel.add(panel4);
		
		setLocationRelativeTo(null);
		this.getRootPane().setDefaultButton(doneButton);
		pack();
		setVisible(false);
	}

	/**
	 * Removes everything from contentPanel and then generates new info for each passed in 
	 * series and adds it all to the content panel, and finally makes this frame visible.
	 * @param seriesList
	 */
	public void display(List<SeriesElement> seriesList) {
		contentPanel.removeAll();
		for(SeriesElement series : seriesList) {
			if (series instanceof XYSeriesElement) {
				JPanel panel = addPanelForSeries( (XYSeriesElement)series);
				contentPanel.add(panel);
				
				contentPanel.add(Box.createVerticalGlue());
			}
			else {
				ErrorWindow.showErrorWindow(new IllegalArgumentException("The default legend config frame cannot currently handle series that aren't XY series."), SunFishFrame.getSunFishFrame().getLogger());
			}
		}
		
		setVisible(true);
		invalidate();
		validate();
		repaint();
	}
	
	protected void done() {
		setVisible(false);
	}

	protected void cancel() {
		setVisible(false);
	}
	
	/**
	 * Construct a new JPanel that contains all the layout options for the series in question, these
	 * panels are then added to 'contentPanel' to display info for all series
	 * @param series
	 * @return
	 */
	private JPanel addPanelForSeries(final XYSeriesElement series) {
		final JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		panel.add(Box.createHorizontalStrut(5));
		final ColorSwatchButton lineColorButton = new ColorSwatchButton(series.getLineColor());
		lineColorButton.setPreferredSize(new Dimension(26, 25));
		panel.add(lineColorButton);
		lineColorButton.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(ColorSwatchButton.SWATCH_COLOR_CHANGED)) {
					series.setLineColor(lineColorButton.getColor());
					figParent.repaint();
				}
			}
		});
		panel.add(Box.createHorizontalStrut(12));
		
        final JComboBox styleBox = new JComboBox();
        styleBox.setModel(new javax.swing.DefaultComboBoxModel(XYSeriesElement.styleTypes));
        styleBox.setSelectedIndex(series.indexForMode());
        styleBox.setFont(panelFont);
        styleBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				series.setMode((String)(styleBox.getSelectedItem()));
				figParent.repaint();
			}
        });
		panel.add(styleBox);
		panel.add(Box.createHorizontalStrut(3));
		
		final JComboBox markerBox = new JComboBox(new javax.swing.DefaultComboBoxModel(XYSeriesElement.markerTypes) );
		markerBox.setSelectedIndex(series.indexForMarkerType());
		markerBox.setPreferredSize(new Dimension(100, 25));
		markerBox.setFont(panelFont);
		markerBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				series.setMarker((String)markerBox.getSelectedItem());
				figParent.repaint();
			}
        });
		panel.add(markerBox);
		panel.add(Box.createHorizontalStrut(3));
		
		final JTextField nameField = new JTextField();
		nameField.setText(series.getName());
		nameField.setFont(panelFont);
		nameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				series.setName(nameField.getText());
				figParent.repaint();
			}			
		});
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				series.getSeries().setName(nameField.getText());
				figParent.repaint();
			}

			public void insertUpdate(DocumentEvent e) {
				series.getSeries().setName(nameField.getText());
				figParent.repaint();
			}

			public void removeUpdate(DocumentEvent e) {
				series.getSeries().setName(nameField.getText());
				figParent.repaint();				
			}
		});
		nameField.setPreferredSize(new Dimension(200, 25));
		nameField.setMinimumSize(new Dimension(150, 10));
		panel.add(nameField);
		panel.add(Box.createHorizontalStrut(3));
		
		JButton remove = new JButton("Remove");
		remove.setFont(panelFont);
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removePanel(panel);
				figParent.removeSeries(series.getSeries());
				figParent.repaint();
			}	
		});
		panel.add(remove);
		panel.add(Box.createHorizontalGlue());
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		seriesPanels.add(panel);
		return panel;
	}


	protected void removePanel(JPanel panel) {
		contentPanel.remove(panel);
		contentPanel.revalidate();
		repaint();
	}
}
