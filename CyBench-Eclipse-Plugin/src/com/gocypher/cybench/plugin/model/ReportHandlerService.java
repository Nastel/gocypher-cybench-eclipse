package com.gocypher.cybench.plugin.model;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

@Creatable
@Singleton
public class ReportHandlerService {

	
	
	public void prepareReportDisplayModel () {
		System.out.println("Works using dependency injection");
	}

}
