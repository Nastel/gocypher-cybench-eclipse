package com.gocypher.cybench.plugin.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Base64;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.gocypher.cybench.plugin.model.ICybenchPartView;
import com.gocypher.cybench.plugin.views.CyBenchExplorerView;
import com.gocypher.cybench.plugin.views.ReportsDisplayView;

public class GuiUtils {

	public static ResourceBundle titles = ResourceBundle.getBundle("titles");
	
	public static String getKeyName(String key) {
		if (titles.containsKey(key)) {
			return titles.getString(key);
	    } 
		return key;
	}
	public static String convertNumToStringByLength(String value) {
        try {


            double v = Double.parseDouble(value);
            if (value != null) {
                if (value.indexOf(".") < 1) {
                    return value;
                }
                if (Math.abs(v) > 1) {
                    return convertNumToStringFrac(v, 2, 2);
                }
                if (Math.abs(v) > 0.1) {
                    return convertNumToStringFrac(v, 2, 2);
                }
                if (Math.abs(v) > 0.01) {
                    return convertNumToStringFrac(v, 3, 3);
                }
                if (Math.abs(v) > 0.001) {
                    return convertNumToStringFrac(v, 4, 4);
                }
                if (Math.abs(v) > 0.0001) {
                    return convertNumToStringFrac(v, 5, 5);
                }
                if (Math.abs(v) > 0.00001) {
                    return convertNumToStringFrac(v, 6, 6);
                }
                if (v == 0) {
                    return convertNumToStringFrac(v, 0, 0);
                }
                return convertNumToStringFrac(v, 6, 8);
            }
        } catch (NumberFormatException e) {
            return value;
        }


        return value;
    }

    private static String convertNumToStringFrac(Object value, int minFractionDigits, int maxFractionDigits) {

        DecimalFormat decimalFormat = new DecimalFormat();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        decimalFormat.setMinimumFractionDigits(minFractionDigits);
        decimalFormat.setMinimumFractionDigits(maxFractionDigits);
        decimalFormat.setDecimalFormatSymbols(symbols);
        return decimalFormat.format(value);
    }
    public static String encodeBase64 (String plainString) {
    	if (plainString != null) {
    		return new String (Base64.getEncoder().encode(plainString.getBytes())) ;
    	}
    	return null ;
    }
    public static String decodeBase64 (String base64String) {
    	if (base64String != null) {
    		return new String (Base64.getDecoder().decode(base64String.getBytes())) ;
    	}
    	return null ;
    }
    
    public static void refreshCybenchExplorer () {
    	Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
		    	try {		    	
		    		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();		    	
					IViewPart view = page.findView(CyBenchExplorerView.ID) ;
		    		if (view instanceof ICybenchPartView) {
		    			((ICybenchPartView)view).refreshView();
		    		}
//			    	PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ReportsDisplayView.ID) ; 
		    	}catch (Exception e) {
		    		e.printStackTrace();
		    	}
		    }
		});	
    }
    public static void openReportDisplayView (String fullPathToReport) {
    	if (fullPathToReport != null && !fullPathToReport.isEmpty()) {
	    	Display.getDefault().asyncExec(new Runnable() {
			    public void run() {
			    	try {
			    		System.out.println("Will open part for reports");
			    		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			    		
			    		String reportIdentifier = encodeBase64(fullPathToReport) ;
			    		page.showView(ReportsDisplayView.ID,reportIdentifier , IWorkbenchPage.VIEW_ACTIVATE);			
		    		
	//			    	PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ReportsDisplayView.ID) ; 
			    	}catch (Exception e) {
			    		e.printStackTrace();
			    	}
			    }
			});	
    	}
    	else {
    		System.err.println("Error: full path to report is null or empty, view can't be opened!");
    	}
    }
	 

}
