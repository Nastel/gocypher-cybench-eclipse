package com.gocypher.cybench.plugin.model;

import java.util.Comparator;

public class ReportFileEntryComparator implements Comparator<ReportFileEntry>{

	@Override
	public int compare(ReportFileEntry o1, ReportFileEntry o2) {
		try {
			if (o1 != null && o2 != null) {
				return ((Long)o2.getTimestamp()).compareTo(o1.getTimestamp()) ;
			}
		}catch (Exception e) {
			
		}
		return 0;
	}
	

}
