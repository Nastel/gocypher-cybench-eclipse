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
