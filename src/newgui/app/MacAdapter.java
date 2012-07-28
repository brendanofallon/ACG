package newgui.app;

import com.apple.eawt.*;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;

public class MacAdapter implements QuitHandler, AboutHandler, PreferencesHandler {

	public MacAdapter() {
		Application.getApplication().setQuitHandler(this);
		Application.getApplication().setAboutHandler(this);
		Application.getApplication().setPreferencesHandler(this);
		
	}
	@Override
	public void handleQuitRequestWith(QuitEvent evt, QuitResponse resp) {
		ACGApp.shutdown();
		resp.performQuit();
	}

	@Override
	public void handleAbout(AboutEvent arg0) {
		
	}

	@Override
	public void handlePreferences(PreferencesEvent arg0) {
		
	}

	
}
