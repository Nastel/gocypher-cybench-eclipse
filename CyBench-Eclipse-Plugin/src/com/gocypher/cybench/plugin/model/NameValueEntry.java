package com.gocypher.cybench.plugin.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


public class NameValueEntry {
	
	private String name ;
	private String value ;
	
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
            this);
	
	public NameValueEntry() {
		
	}
	public NameValueEntry(String name, String value) {
		this.name = name;
		this.value = value ;
	}
	
	public boolean hasValue () {
		return value != null && !value.trim().isEmpty() ;
	}
	public void addPropertyChangeListener(String propertyName,
	            PropertyChangeListener listener) {
	        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	    }

	public void removePropertyChangeListener(PropertyChangeListener listener) {
	        propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {	
		propertyChangeSupport.firePropertyChange("name", this.name,
	                this.name = name);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		propertyChangeSupport.firePropertyChange("value", this.value,
                this.value = value);		
	}
	
	@Override
	public String toString () {
		return name+" "+value ;
	}
	

}
