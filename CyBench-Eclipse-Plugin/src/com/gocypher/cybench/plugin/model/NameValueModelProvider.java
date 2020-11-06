package com.gocypher.cybench.plugin.model;

import java.util.ArrayList;
import java.util.List;

public enum NameValueModelProvider {
	INSTANCE ;
	
	private List<NameValueEntry>entries ;
	
	private NameValueModelProvider () {
		this.entries = new ArrayList<>() ;
		
		/*entries.add(new NameValueEntry ("name", "My report")) ;
		entries.add(new NameValueEntry ("timestamp", "2020-11-06")) ;
		entries.add(new NameValueEntry ("score", "5,500.23")) ;
		*/
		
	}

	public List<NameValueEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<NameValueEntry> entries) {
		this.entries = entries;
	}
	
	
}
