package com.gocypher.cybench.plugin.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class RunSelectionEntry {

	    private String outputPath;
		private String projectPath;
		Set<String> classPaths = new LinkedHashSet<>();
		private String projectReportsPath;
		
		public String getProjectReportsPath() {
			return projectReportsPath;
		}
		public void setProjectReportsPath(String projectReportsPath) {
			this.projectReportsPath = projectReportsPath;
		}
		public String getProjectPath() {
			return projectPath;
		}
		public void setProjectPath(String projectPath) {
			this.projectPath = projectPath;
		}
		public void addClassPaths(String classPath) {
			this.classPaths.add(classPath);
		}
		public void removeClassPaths(String classPath) {
			this.classPaths.remove(classPath);
		}
		public Set<String> getClassPaths() {
			return classPaths;
		}
		public void setClassPaths(Set<String> classPaths) {
			this.classPaths = classPaths;
		}
		public String getOutputPath() {
			return outputPath;
		}
		public void setOutputPath(String outputPath) {
			this.outputPath = outputPath;
		}
}

