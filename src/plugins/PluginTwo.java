package plugins;

import java.util.Map;

public class PluginTwo {

	public PluginTwo(Map<String, String> attributes, PluginOne p1) {
		System.out.println("Plugin Two created with plugin one as arg");
		
		for(String key : attributes.keySet()) {
			System.out.println("Found key: " + key + " = " + attributes.get(key));
		}
	}
}
