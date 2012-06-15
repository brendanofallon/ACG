package newgui.gui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import newgui.UIConstants;

/**
 * A small panel that displays memory usage. It starts a swing timer that updates
 * a label periodically
 * @author brendan
 *
 */
public class MemoryGauge extends JPanel implements ActionListener {
	
	private Timer updater;
	
	public MemoryGauge() {
		initComponents();
		updater = new Timer(1000, this);
		updater.start();
	}

	private void initComponents() {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		label = new JLabel("Memory usage:");
		label.setFont(UIConstants.sansFont.deriveFont(12f));
		this.add(label);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		updateLabel();
	}

	private void updateLabel() {
		Runtime rt = Runtime.getRuntime();
		long free = rt.freeMemory();
		long tot = rt.totalMemory();
		int freeMBs = (int)Math.round( free / 1048576l);
		int totMBs = (int)Math.round(tot / 1048576l);
		int used = totMBs - freeMBs;
		label.setText("Memory: " + used + " / " + totMBs + " MB");
		label.revalidate();
		label.repaint();
	}
	
	private JLabel label;
}
