package com.gocypher.cybench.plugin.views;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;

public class TabGroup extends AbstractLaunchConfigurationTabGroup {

    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
    	ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
    			new CybenchTab(),
    			new JavaArgumentsTab(),
    			new JavaJRETab(),
    			new JavaClasspathTab(), 
    			new CommonTab()
    	};
        setTabs(tabs);
    }

}