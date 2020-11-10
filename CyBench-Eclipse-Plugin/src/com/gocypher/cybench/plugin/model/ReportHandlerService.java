package com.gocypher.cybench.plugin.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.gocypher.cybench.core.utils.JSONUtils;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.utils.GuiUtils;

@Creatable
@Singleton
public class ReportHandlerService {

	
	
	public ReportUIModel prepareReportDisplayModel (ReportFileEntry reportFile) {
		System.out.println("Works using dependency injection");		
			
		ReportUIModel model = this.deserializaReportIntoUIModel(CybenchUtils.loadFile(reportFile.getFullPathToFile())) ;
		
		return model ;
	}

	private void extractReportProperties (String reportJSON, List<NameValueEntry>listOfProperties) {
		listOfProperties.clear();
		//NameValueModelProvider.INSTANCE.getEntries().add(new NameValueEntry("custom1","custom2")) ;
		Map<String, Object> reportMap = (Map<String,Object>)JSONUtils.parseJsonIntoMap(reportJSON ) ;
		
		reportMap.keySet().forEach(key ->{
			listOfProperties.add(new  NameValueEntry (key,reportMap.get(key) != null ?reportMap.get(key).toString():"")) ;
		});
		
		
	}
	
	private ReportUIModel deserializaReportIntoUIModel (String rawReport) {
		ReportUIModel model = new ReportUIModel() ;
		Map<String, Object> reportMap = (Map<String,Object>)JSONUtils.parseJsonIntoMap(rawReport ) ;
		
		this.extractBaseProperties(model, reportMap);
		
		Map<String,Object>benchmarksByCategories= (Map<String,Object>) reportMap.get("benchmarks") ;
		if (benchmarksByCategories != null) {
			Iterator<String>it = benchmarksByCategories.keySet().iterator() ;
			while (it.hasNext()) {
				String categoryName = it.next() ;
				List<Map<String,Object>> rawBenchmarks = (List<Map<String,Object>>)benchmarksByCategories.get(categoryName) ;
				for (Map<String,Object>benchmarkItem:rawBenchmarks) {
					String benchmarkName = (String)benchmarkItem.get("name") ;
					String score = "" ;
					if (benchmarkItem.get("score") != null) {
						score = addParentheses(GuiUtils.convertNumToStringByLength(benchmarkItem.get("score").toString())) ;
					}
					model.addToListOfBenchmarks(benchmarkName,score );
					
					this.extractBenchmarkProperties(model, benchmarkName, benchmarkItem);
					
				}
			}
		}
		
		Map<String,Object>environmentSettings = (Map<String,Object>)reportMap.get("environmentSettings") ;
		if (environmentSettings != null) {
			Map<String,Object>jvmEnvironment = (Map<String,Object>)environmentSettings.get("jvmEnvironment") ;
			if (jvmEnvironment != null) {
				extractProperties (jvmEnvironment, model.getListOfJVMProperties()) ;
			}
			Map<String,Object>environment = (Map<String,Object>)environmentSettings.get("environment") ;
			if (environment != null) {
				extractProperties (environment, model.getListOfHwProperties()) ;
			}
		}
		
		
		return model ;
		
	}
	private void extractBaseProperties (ReportUIModel model,Map<String,Object>reportProperties) {
		Long timestamp = (Long)reportProperties.get("timestamp") ;
		if (timestamp != null) {
			model.addBaseProperty("timestamp", CybenchUtils.formatTimestamp(timestamp)); 
		}
		Double totalScore = (Double)reportProperties.get("totalScore") ;
		if (totalScore != null) {
			model.addBaseProperty("totalScore", GuiUtils.convertNumToStringByLength(totalScore.toString()) ); 
		}
		Map<String,Object> benchmarkSettings = (Map<String,Object>)reportProperties.get("benchmarkSettings") ;
		if (benchmarkSettings != null) {
			model.addBaseProperty ("benchReportName",(String)benchmarkSettings.get("benchReportName")) ;
		}
		
	}
	
	private void extractBenchmarkProperties(ReportUIModel model, String benchmarkName, Map<String,Object>benchmarkProperties) {
		
		benchmarkProperties.keySet().forEach(key ->{
			model.addToBenchmarksAttributes(benchmarkName, GuiUtils.getKeyName(key), convertValueToString(benchmarkProperties.get(key)));		
		});
	}
	private void extractProperties (Map<String,Object>properties, List<NameValueEntry> listOfProperties) {
		properties.keySet().forEach(key ->{			
			listOfProperties.add( new NameValueEntry (GuiUtils.getKeyName(key), convertValueToString(properties.get(key))));		
		});
	}
	private String addParentheses(String value) {
		if (value != null && !value.isEmpty()) {
			return "("+value+")" ;
		}
		return "" ;
	}
	private String convertValueToString (Object value) {
		if (value != null) {
			if (value instanceof String) {
				return value.toString() ;
			}
			if (value instanceof Number) {
				return GuiUtils.convertNumToStringByLength(value.toString()) ;
			}
		}
		
		return "" ;
	}

}
