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

public class Node<T> {

	private T data = null;
	 
	private List<Node<T>> children = new ArrayList<>();
	 
	private Node<T> parent = null;
	 
	public Node(T data) {
		this.data = data;
	}
	 
	public Node<T> addChild(Node<T> child) {
		child.setParent(this);
		this.children.add(child);
	 	return child;
	}
	 
	public void addChildren(List<Node<T>> children) {
		children.forEach(each -> each.setParent(this));
		this.children.addAll(children);
	}
	 
	public List<Node<T>> getChildren() {
		return children;
	}
	 
	public T getData() {
		return data;
	}
	 
	public void setData(T data) {
		this.data = data;
	}
	 
	private void setParent(Node<T> parent) {
		this.parent = parent;
	}
	 
	public Node<T> getParent() {
		return parent;
	}
	
	public String toString () {
		if (data != null) {
			return data.toString() ;
		}
		return "" ;
	}
}
