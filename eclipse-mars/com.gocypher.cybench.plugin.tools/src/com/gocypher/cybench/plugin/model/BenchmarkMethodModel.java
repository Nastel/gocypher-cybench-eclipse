package com.gocypher.cybench.plugin.model;

public class BenchmarkMethodModel {


	private String methodName;
	private Class<?> methodType;
	private String methodBenchmarkMode;
	private String methodHint;
	
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

}
