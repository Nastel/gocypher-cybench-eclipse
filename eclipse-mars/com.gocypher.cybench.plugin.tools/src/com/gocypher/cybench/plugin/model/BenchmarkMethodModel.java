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

public class BenchmarkMethodModel {
	
	private String methodName;
	private Class<?> methodType;
	private String methodBenchmarkMode;
	private String methodHint;
	private String[] exceptionTypes;
	private String[] parameterTypes;
	
	public BenchmarkMethodModel(){
		
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Class<?> getMethodType() {
		return methodType;
	}
	public void setMethodType(Class<?> methodType) {
		this.methodType = methodType;
	}
	public String getMethodBenchmarkMode() {
		return methodBenchmarkMode;
	}
	public void setMethodBenchmarkMode(String methodBenchmarkMode) {
		this.methodBenchmarkMode = methodBenchmarkMode;
	}
	public String getMethodHint() {
		return methodHint;
	}
	public void setMethodHint(String methodHint) {
		this.methodHint = methodHint;
	}
	public String[] getExceptionTypes() {
		return exceptionTypes;
	}
	public void setExceptionTypes(String[] exceptionTypes) {
		this.exceptionTypes = exceptionTypes;
	}
	public String[] getParameterTypes() {
		return parameterTypes;
	}
	public void setParameterTypes(String[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

}
