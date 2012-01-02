package newgui.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtils {

	/**
	 * Returns the context of the text node that is the immediate child of the given element,
	 * or null if there is no text-node child
	 * @param el
	 * @return
	 */
	public static String getChildText(Element el) {
		NodeList children = el.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				return child.getNodeValue();
			}
		}
		return null;
	}
	
	
}
