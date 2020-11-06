package com.gocypher.cybench.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtils {
	 private static ObjectMapper mapper = new ObjectMapper();

	    public static Map<?,?> parseJsonIntoMap(String jsonString) {
	        try {
	            return mapper.readValue(jsonString, HashMap.class);
	        } catch (Exception e) {
	            //LOG.error("Error on parsing json into map", e);
	        	e.printStackTrace();
	            return new HashMap<>();
	        }
	    }
	    public static List<?> parseJsonIntoList(String jsonString) {
	        try {
	            return mapper.readValue(jsonString, ArrayList.class);
	        } catch (Exception e) {
	            //LOG.error("Error on parsing json into map", e);
	        	e.printStackTrace();
	            return new ArrayList<>();
	        }
	    }
	    public static String marshalToJson(Object item) {
	        try {
	            return mapper.writeValueAsString(item);
	        } catch (Exception e) {
	            //LOG.error ("Error on marshaling to json",e) ;
	        	e.printStackTrace();
	            return "";
	        }
	    }
	    public static String marshalToPrettyJson(Object item) {
	        try {
	            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(item);
	        } catch (Exception e) {
	           //LOG.error ("Error on marshal to pretty json",e) ;
	        	e.printStackTrace();
	           return "";
	        }
	    }
}
