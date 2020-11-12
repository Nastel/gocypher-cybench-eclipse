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
		//System.out.println("Works using dependency injection");		
			
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
		if (rawReport != null) {
			Map<String, Object> reportMap = (Map<String,Object>)JSONUtils.parseJsonIntoMap(rawReport ) ;
			
			this.extractBaseProperties(model, reportMap);
			this.extractOverviewProperties(model, reportMap);
			
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
			
		}
		return model ;
		
	}
	private void extractOverviewProperties (ReportUIModel model,Map<String,Object>reportProperties) {
		Map<String,Object> benchmarkSettings = (Map<String,Object>)reportProperties.get("benchmarkSettings") ;
		if (benchmarkSettings != null) {
			model.addToListOfOverview(GuiUtils.getKeyName("benchReportName"),(String)benchmarkSettings.get("benchReportName")) ;
		}
		Long timestamp = (Long)reportProperties.get("timestamp") ;
		if (timestamp != null) {
			model.addToListOfOverview(GuiUtils.getKeyName("timestamp"), CybenchUtils.formatTimestamp(timestamp)); 
		}
		Double totalScore = (Double)reportProperties.get("totalScore") ;
		if (totalScore != null) {
			model.addToListOfOverview(GuiUtils.getKeyName("totalScore"), GuiUtils.convertNumToStringByLength(totalScore.toString()) ); 
		}
		model.addToListOfOverview(GuiUtils.getKeyName("uploadStatus"), (String)reportProperties.get("uploadStatus") ); 
		
		if (benchmarkSettings != null) {
			benchmarkSettings.keySet().forEach(key->{
				if (!"benchReportName".equals(key) && benchmarkSettings.get(key)!= null) {
					model.addToListOfOverview(GuiUtils.getKeyName(key), benchmarkSettings.get(key).toString() ); 
				}
			});
		}
		
		Map<String,Object>environmentSettings = (Map<String,Object>)reportProperties.get("environmentSettings") ;
		if (environmentSettings != null) {
			Map<String,Object> unclassifiedProperties = (Map<String,Object>)environmentSettings.get("unclassifiedProperties") ;	
			if (unclassifiedProperties != null) {
				unclassifiedProperties.keySet().forEach(key->{
					if (unclassifiedProperties.get(key) != null) {
						if ("performanceGarbageCollectors".equals(key)) {							
							String gc = "" ;
							List<String> arr = (List<String>)unclassifiedProperties.get(key) ;
							for (String item:arr) {
								gc+=item+", " ;
							}
							if (!gc.isEmpty()) {
								model.addToListOfOverview(GuiUtils.getKeyName(key), gc.substring(0,gc.lastIndexOf(",")));
							}							
						}
						else if ("performanceJvmRuntimeParameters".equals(key)) {
							List<String> arr = (List<String>)unclassifiedProperties.get(key) ;
							String other = "" ;
							for (String item:arr) {
								if (item.startsWith("-D")) {
									String[] kv = item.split("=") ;
									if (kv != null) {
										if (kv.length == 2) {
											model.addToListOfOverview (kv[0],kv[1]) ;
										}
										else if (kv.length == 1) {
											other += item +", " ;
										}
									}
								}
								else {
									other+=item+", " ;
								}
							}
							if (!other.isEmpty()) {
								model.addToListOfOverview(GuiUtils.getKeyName(key), other.substring(0,other.lastIndexOf(",")));
							}	
						}
						else {
							model.addToListOfOverview(GuiUtils.getKeyName(key), unclassifiedProperties.get(key).toString());
						}
					}
				});
			}
			Map<String,Object> userDefinedProperties = (Map<String,Object>)environmentSettings.get("userDefinedProperties") ;	
			if (userDefinedProperties != null) {
				userDefinedProperties.keySet().forEach(key->{
					if (userDefinedProperties.get(key)!= null) {
						model.addToListOfOverview(GuiUtils.getKeyName(key), userDefinedProperties.get(key).toString());
					}
				});
			}
			
		}
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
		model.addBaseProperty("reportURL", (String)reportProperties.get("reportURL"));
		
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
