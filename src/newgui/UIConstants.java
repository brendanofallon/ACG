package newgui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;

import newgui.gui.ViewerWindow;

/**
 * Stores some oft-used ui values, background colors, icons, fonts, etc.  
 * @author brendan
 *
 */
public class UIConstants {

	
	
	
	//Component background colors
	public static final Color componentBackground = new Color(0.92f, 0.92f, 0.93f);
	
	public static final Color lightBackground = new Color(0.97f, 0.97f, 0.99f);
	
	public static final Color darkBackground = new Color(0.90f, 0.90f, 0.92f);

	//Some oft-used icons
	public static final ImageIcon blueRightArrow = getIcon("gui/icons/blueRightArrow.png");
	public static final ImageIcon grayRightArrow = getIcon("gui/icons/grayRightArrow.png");
	
	public static final ImageIcon blueLeftArrow = getIcon("gui/icons/blueLeftArrow.png");
	
	public static final ImageIcon redCloseButton = getIcon("gui/icons/redClose.png");
	public static final ImageIcon grayCloseButton = getIcon("gui/icons/grayClose.png");

	
	public static final ImageIcon startButton = getIcon("gui/icons/startArrow.png");
	
	public static final ImageIcon pauseButton = getIcon("gui/icons/pauseButton.png");
	
	public static final ImageIcon stopButton = getIcon("gui/icons/stopButton.png");
	
	public static final ImageIcon addButton = getIcon("gui/icons/addButton.png");
	
	public static final ImageIcon saveGrayButton = getIcon("gui/icons/saveGray.png");
	public static final ImageIcon saveBlueButton = getIcon("gui/icons/saveBlue.png");
	
	public static final ImageIcon clearButton = getIcon("gui/icons/zeroIcon.png");
	
	public static final ImageIcon grayHistogram = getIcon("gui/icons/grayHistogram.png");
	public static final ImageIcon blueHistogram = getIcon("gui/icons/blueHistogram.png");
	
	//Fonts
	public static final Font sansFont = getFont("gui/fonts/Trebuchet_MS.ttf");
	
	//Some other helpful properties:
	
	/**
	 * Returns true if we're on a mac, false if windows or linux
	 * @return
	 */
	public static boolean isMac() {
		return System.getProperty("os.name").contains("Mac");
	}

	/**
	 * Returns true if we're on a Windows machine (any version), false if mac or linux
	 * @return
	 */
	public static boolean isWindows() {
		return System.getProperty("os.name").contains("Windows");
	}
	
	/**
	 * Return an icon associated with the given url. For instance, if the url is icons/folder.png, we look in the
	 * package icons for the image folder.png, and create and return an icon from it. 
	 * @param url
	 * @return
	 */
	public static ImageIcon getIcon(String url) {
		ImageIcon icon = null;
		try {
			java.net.URL imageURL = UIConstants.class.getResource(url);
			icon = new ImageIcon(imageURL);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return icon;
	}
	
	public static Font getFont(String url) {
		Font font = null;
		try {
			InputStream fontStream = UIConstants.class.getResourceAsStream(url);
			font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
			font = font.deriveFont(12f);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return font;
	}
	
}
