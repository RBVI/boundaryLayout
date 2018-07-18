package edu.ucsf.rbvi.boundaryLayout.internal.model;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.view.model.CyNetworkView;

public class Template {
	private String name;
	private List<String> annotationsString;
	private Image thumbnail;
	private List<CyNetworkView> activeViews;
	private List<String> annotationUUIDs;
	
	public Template(String name, List<String> annotationsString, Image thumbnail) {
		this.name = name;
		this.annotationsString = annotationsString;
		this.thumbnail = thumbnail;
		this.activeViews = new ArrayList<>();
		this.annotationUUIDs = new ArrayList<>();
		this.initAnnotationUUIDs();
	}
	
	public Template(String name) {
		this(name, null, null);
	}
	
	public Template(String name, List<String> annotationsString) {
		this(name, annotationsString, null);
	}
	
	private void initAnnotationUUIDs() {
		if(annotationUUIDs == null)
			annotationUUIDs = new ArrayList<>();
		else
			annotationUUIDs.clear();
		if(annotationsString != null && !annotationsString.isEmpty())
			for(String annotation : annotationsString) {
				String subAnnotation = annotation.substring(annotation.indexOf("uuid"));
				String uuidEquality = subAnnotation.substring(0, subAnnotation.indexOf(", "));
				String uuid = uuidEquality.substring(uuidEquality.indexOf("=") + 1);
				annotationUUIDs.add(uuid);
			}
		for(String uuid : annotationUUIDs)
			System.out.println(uuid);
	}
	
	public boolean isContainedUUIDs(List<String> containingUUIDs) {
		for(String uuid : this.annotationUUIDs) 
			if(!containingUUIDs.contains(uuid))
				return false;
		return true;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setAnnotations(List<String> annotationsString) {
		this.annotationsString = annotationsString;
		this.initAnnotationUUIDs();
	}
	
	public List<String> getAnnotations() {
		return annotationsString;
	}
	
	public void setThumbnail(Image thumbnail) {
		this.thumbnail = thumbnail;
	}
	
	public Image getThumbnail() {
		return thumbnail;
	}
	
	public void addActiveView(CyNetworkView view) {
		if(!activeViews.contains(view))
			activeViews.add(view);
	}
	
	public List<CyNetworkView> getActiveViews() {
		return activeViews;
	}
	
	public boolean hasActiveViews() {
		return !activeViews.isEmpty();
	}
	
	public boolean removeActiveView(CyNetworkView view) {
		if(activeViews.contains(view)) {
			activeViews.remove(view);
			return true;
		}
		return false;
	}
	
	public void removeAllActiveViews() {
		while(!activeViews.isEmpty())
			activeViews.remove(0);
	}
}
