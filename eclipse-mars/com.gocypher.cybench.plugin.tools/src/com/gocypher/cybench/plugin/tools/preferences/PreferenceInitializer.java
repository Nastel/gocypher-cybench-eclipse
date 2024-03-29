package com.gocypher.cybench.plugin.tools.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.gocypher.cybench.plugin.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_AUTH_TOKEN,"");
		store.setDefault(PreferenceConstants.P_QUERY_TOKEN,"");
		store.setDefault(PreferenceConstants.P_EMAIL,"");
	}

}
