/*
 * Copyright (C) 2020, K2N.IO.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

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
