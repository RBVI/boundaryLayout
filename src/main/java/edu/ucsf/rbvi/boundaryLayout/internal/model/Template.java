package edu.ucsf.rbvi.boundaryLayout.internal.model;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.view.model.CyNetworkView;

/**
 * A template is a collection of boundaries, which are grouped by some functionality,
 * i.e. Cellular location or taxonomy
 */
public class Template {
	private String name;
	private List<String> annotationsString;
	private Image thumbnail;
	private List<CyNetworkView> activeViews;
	private List<String> annotationUUIDs;
	
	/**
	 * Construct a template with its various properties A template is an integral part of Template
	 * Mode, which allows user manipulation of templates, which are a collection of boundaries.
	 * @param name is the name of the template defined by the user
	 * @param annotationsString is the list of annotation information corresponding to this template
	 * @param thumbnail is the image corresponding to the template which is visible to the
	 * user via the Boundaries Tab
	 */
	public Template(String name, List<String> annotationsString, Image thumbnail) {
		this.name = name;
		this.annotationsString = annotationsString;
		this.thumbnail = thumbnail;
		this.activeViews = new ArrayList<>();
		this.annotationUUIDs = new ArrayList<>();
		this.initAnnotationUUIDs();
	}
	
	/**
	 * Construct a general template only given its name 
	 */
	public Template(String name) {
		this(name, null, null);
	}
	
	/**
	 * Construct a template with its name and annotation information
	 */
	public Template(String name, List<String> annotationsString) {
		this(name, annotationsString, null);
	}
	
	/** Private Method
	 * Initialize the annotation UUIDs of the template
	 */
	private void initAnnotationUUIDs() {
		if(annotationUUIDs == null)
			annotationUUIDs = new ArrayList<>();
		else
			annotationUUIDs.clear();
		if(annotationsString != null && !annotationsString.isEmpty()) {
			for(String annotation : annotationsString) {
				String subAnnotation = annotation.substring(annotation.indexOf("uuid"));
				String uuidEquality = subAnnotation.substring(0, subAnnotation.indexOf(", "));
				String uuid = uuidEquality.substring(uuidEquality.indexOf("=") + 1);
				annotationUUIDs.add(uuid);
			}
		}
	}
	
	/**
	 * If the parameter containingUUIDs contains all the UUIDs in this
	 * template, return true. Otherwise return false. This is used when checking
	 * for active templates and the UUIDs correspond to annotations identification.
	 * @param containingUUIDs is a list of strings on which to compare this template's UUIDs. 
	 */
	public boolean isContainedUUIDs(List<String> containingUUIDs) {
		for(String uuid : this.annotationUUIDs) 
			if(!containingUUIDs.contains(uuid))
				return false;
		return true;
	}
	
	/**
	 * Set the user-defined name of this template. This is used to name or rename a template.
	 * @param name is the name to be changed
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the name of this template
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the annotations of this template and then re-initializes the 
	 * UUIDs of this template
	 * @param annotationsString is the annotation information which is to be 
	 * associated with this template
	 */
	public void setAnnotations(List<String> annotationsString) {
		this.annotationsString = annotationsString;
		this.initAnnotationUUIDs();
	}
	
	/**
	 * Gets the annotation information of this template, in the form 
	 * of a list of strings where the strings are a string representation of 
	 * the argument map of the annotation
	 */
	public List<String> getAnnotations() {
		return annotationsString;
	}
	
	/**
	 * Sets the thumbnail of this template
	 * @param thumbnail
	 */
	public void setThumbnail(Image thumbnail) {
		this.thumbnail = thumbnail;
	}
	
	/**
	 * Gets the thumbnail of this template
	 * @return thumbnail
	 */
	public Image getThumbnail() {
		return thumbnail;
	}
	
	/**
	 * Adds a network view to the list of active views of this
	 * template. An active view is a CyNetworkView which the template
	 * has been applied on
	 * @param view that has just registered this as its template
	 */
	public void addActiveView(CyNetworkView view) {
		if(!activeViews.contains(view))
			activeViews.add(view);
	}
	
	/**
	 * @return a list of CyNetworkViews which this template is applied on
	 */
	public List<CyNetworkView> getActiveViews() {
		return activeViews;
	}
	
	/**
	 * Check to see if this template is currently applied on any network view
	 * @return true if this template has active views
	 */
	public boolean hasActiveViews() {
		return activeViews != null && !activeViews.isEmpty();
	}
	
	/**
	 * Remove a certain CyNetworkView from the list of this template's active views. This
	 * network view should be currently using this template. This is the case when the applied
	 * template is switched from this to another template or CyNetworkView has been removed from
	 * the user's session
	 * @param view is the CyNetworkView to remove from this template's list of active views
	 * @return true if view was an active view and was successfully removed from this template's
	 * list of active views
	 */
	public boolean removeActiveView(CyNetworkView view) {
		if(activeViews.contains(view)) {
			activeViews.remove(view);
			if(!activeViews.contains(view))
				return true;
		}
		return false;
	}
	
	/**
	 * Remove all of the active views from this template. This is used primarily when the user has 
	 * deleted this template.
	 */
	public void removeAllActiveViews() {
		if(activeViews == null)
			activeViews = new ArrayList<>();
		while(!activeViews.isEmpty())
			activeViews.remove(0);
	}
}
