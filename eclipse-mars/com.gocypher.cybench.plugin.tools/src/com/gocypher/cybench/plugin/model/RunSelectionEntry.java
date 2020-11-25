package com.gocypher.cybench.plugin.model;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;

public class RunSelectionEntry {

		private String projectName;
	    private String outputPath;
		private String projectPath;
		private List<String> sourcePathsWithClasses = new LinkedList<String>();
		private Set<String> classPaths = new LinkedHashSet<>();
		private String projectReportsPath;
		private IProject projectSelected;
		
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
		public List<String> getSourcePathsWithClasses() {
			return sourcePathsWithClasses;
		}
		public void setSourcePathsWithClasses(List<String> sourcePathsWithClasses) {
			this.sourcePathsWithClasses = sourcePathsWithClasses;
		}
		public void addSourcePathsWithClasses(String sourcePathClass) {
			this.sourcePathsWithClasses.add(sourcePathClass);
		}
		public void removeSourcePathsWithClasses(String sourcePathClass) {
			this.sourcePathsWithClasses.remove(sourcePathClass);
		}
		public String getProjectName() {
			return projectName;
		}
		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}
		public IProject getProjectSelected() {
			return projectSelected;
		}
		public void setProjectSelected(IProject projectSelected) {
			this.projectSelected = projectSelected;
		}
}

