package com.gocypher.cybench.plugin.tools.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import com.gocypher.cybench.plugin.Activator;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class CyBenchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public CyBenchPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("CyBench authentication and identification properties.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new StringFieldEditor(PreferenceConstants.P_AUTH_TOKEN, "A &repository bench token:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_QUERY_TOKEN, "A $repository query token:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_EMAIL, "&Email address:", getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
	}
	
}