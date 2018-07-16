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
	
	public Template(String name, List<String> annotationsString, Image thumbnail) {
		this.name = name;
		this.annotationsString = annotationsString;
		this.thumbnail = thumbnail;
		this.activeViews = new ArrayList<>();
	}
	
	public Template(String name) {
		this(name, null, null);
	}
	
	public Template(String name, List<String> annotationsString) {
		this(name, annotationsString, null);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setAnnotations(List<String> annotationsString) {
		this.annotationsString = annotationsString;
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
