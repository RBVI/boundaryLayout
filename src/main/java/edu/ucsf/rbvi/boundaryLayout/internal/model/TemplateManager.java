package edu.ucsf.rbvi.boundaryLayout.internal.model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.view.presentation.annotations.BoundedTextAnnotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/* 
 * This is the manager of the Templates tab called Boundaries feature
 * provided by boundary layout. Capabilities include saving, loading, deleting,
 * overwriting, importing, exporting, and applying the template to the view
 * */
public class TemplateManager {
	private Map<String, List<String>> templates;
	private Map<String, Image> templateThumbnails;
	private final CyServiceRegistrar registrar;
	private final AnnotationManager annotationManager;
	private final CyNetworkFactory networkFactory;
	private final CyNetworkViewFactory networkViewFactory;
	private final RenderingEngineFactory<CyNetwork> renderingEngineFactory;
	public static final String NETWORK_TEMPLATES = "Templates Applied";
	static double PADDING = 10.0; // Make sure we have some room around our annotations

	/*
	 * Initialize a template manager, which manages the entire Boundaries tab, corresponding
	 * to the Template Mode feature of boundaryLayout.  
	 * */
	public TemplateManager(CyServiceRegistrar registrar) {
		templates = new HashMap<>();
		templateThumbnails = new HashMap<>();
		this.registrar = registrar;
		annotationManager = registrar.getService(AnnotationManager.class);	
		networkFactory = registrar.getService(CyNetworkFactory.class);	
		networkViewFactory = registrar.getService(CyNetworkViewFactory.class);	
		renderingEngineFactory = registrar.getService(RenderingEngineFactory.class);	
	}

	/*
	 * Adds template to the templates map, with the mapping:
	 * (template name - list of annotations information), by calling addTemplateStrings()
	 * 
	 * @param templateName is the name of the added template 
	 * @param annotations is a list of annotations that make up the template 
	 * @return true if the template has been successfully added
	 */
	public boolean addTemplate(String templateName, List<Annotation> annotations) {
		List<String> annotationsInfo = getAnnotationInformation(annotations);
		return addTemplateStrings(templateName, annotationsInfo);
	}

	/*
	 * Adds the mapping (template name - list of annotations information) to templates map. 
	 * If the mapping already exists, overwrite the template mapping.
	 * 
	 * @param templateName is the name of the added template 
	 * @param annotations is a list of information of the annotations that comprise the template 
	 * @return true if the template has been successfully added to the templates map
	 * */
	public boolean addTemplateStrings(String templateName, List<String> annotations) {
		if(templates.containsKey(templateName)) 
			return overwriteTemplateStrings(templateName, annotations);
		templates.put(templateName, annotations);
		if(templates.containsKey(templateName)) 
			return true;
		return false;
	}

	/*
	 * Removes the mapping (template name - list of annotations information) from templates map. 
	 * 
	 * @param templateName is the name of the added template 
	 * @return true if the template has been successfully removed from the templates map
	 * */
	public boolean deleteTemplate(String templateName) {
		if(!templates.containsKey(templateName))
			return false;
		templates.remove(templateName);
		if(!templates.containsKey(templateName))
			return true;
		return false;
	}

	/*
	 * Overwrites template mapping of key template name with the new value consisting
	 * of a new list of annotation information, by calling overwriteTemplateStrings()
	 * 
	 * @param templateName is the name of the overwritten template 
	 * @param annotations is a list of annotations that make up the template 
	 * @return true if the template has been successfully overwritten
	 */
	public boolean overwriteTemplate(String templateName, List<Annotation> annotations) {
		List<String> annotationsInfo = getAnnotationInformation(annotations);
		return overwriteTemplateStrings(templateName, annotationsInfo);
	}

	/*
	 * Overwrites the mapping (template name - list of annotations information) to templates map. 
	 * 
	 * @precondition It is assumed that templates map already has the key templateName.
	 * @param templateName is the name of the overwritten template 
	 * @param annotations is a list of information of the annotations that comprise the template 
	 * @return true if the template has been successfully added to the templates map
	 */
	public boolean overwriteTemplateStrings(String templateName, List<String> annotations) {
		if(!templates.containsKey(templateName))
			return false;
		templates.replace(templateName, annotations);
		return true;
	}

	/*
	 * This method applies the template corresponding to the templateName to the given 
	 * network view, meaning all the annotations and their properties are added to the view.
	 * 
	 * @param templateName is the name of the applied template 
	 * @param networkView is the network view on which to apply the template
	 * @return true if the template has been applied correctly
	 */
	public boolean useTemplate(String templateName, CyNetworkView networkView) {
		if(!templates.containsKey(templateName)) 
			return false;
		List<String> templateInformation = templates.get(templateName);

		for(String annotationInformation : templateInformation) {
			String[] argsArray = annotationInformation.substring(
					annotationInformation.indexOf(')') + 3, 
					annotationInformation.length() - 1).split(", ");
			Map<String, String> argMap = new HashMap<>();
			for(String arg : argsArray) {
				String[] keyValuePair = arg.split("=");
				argMap.put(keyValuePair[0], keyValuePair[1]);
			}
			Annotation addedAnnotation = getCreatedAnnotation(registrar, networkView, argMap);
			addedAnnotation.setName(argMap.get("name"));
			annotationManager.addAnnotation(addedAnnotation);
			addedAnnotation.update();
		}
		appendTemplatesActive(networkView, templateName);

		networkView.updateView();
		return true;
	}

	/*
	 * Imports the template information given in the template file and creates the mapping
	 * (template name - list of annotation information)
	 * 
	 * @precondition templateFile exists 
	 * @param templateName is what the user chooses to call the template after importing that 
	 * template's annotations from the file
	 * @param templateFile is the file form which the user wishes to read the annotation information
	 * @return true if the import was successful
	 */
	public boolean importTemplate(String templateName, File templateFile) throws IOException {
		if(!templateFile.exists())
			return false;
		BufferedReader templateReader = new BufferedReader(new FileReader(templateFile.getAbsolutePath()));
		List<String> templateInformation = new ArrayList<>();
		String annotationInformation = "";
		while((annotationInformation = templateReader.readLine()) != null)
			templateInformation.add(annotationInformation);
		if(!templates.containsKey(templateName))
			templates.put(templateName, templateInformation);
		else
			templates.replace(templateName, templateInformation);
		try {
			templateReader.close();
		} catch (IOException e) {
			throw new IOException("Problems writing to stream: " + 
					templateReader.toString() + "[" + e.getMessage()+ "]");
		}
		if(templates.containsKey(templateName))
			return true;
		return false;
	}

	/*
	 * Exports the template information associated with the template name to a file in the given
	 * path. If the file does not exist, a file is created. The information of the annotations in the
	 * template are written line by line to this file.
	 * 
	 * @param templateName is name of the template that the user wishes to export
	 * @param absoluteFilePath is the path of the file that the template information is written to
	 * @precondition templateName is valid and is in the templates map 
	 * @return true if the import was successful
	 */
	public boolean exportTemplate(String templateName, String absoluteFilePath) throws IOException {
		if(!templates.containsKey(templateName))
			return false;
		File exportedFile = new File(absoluteFilePath);
		if(!exportedFile.exists())
			exportedFile.createNewFile();
		BufferedWriter templateWriter = new BufferedWriter(new FileWriter(exportedFile));
		for(String annotationInformation : templates.get(templateName)) {
			templateWriter.write(annotationInformation);
			templateWriter.newLine();
		}
		try {
			templateWriter.close();
		} catch (IOException e) {
			throw new IOException("Problems writing to stream: " + 
					templateWriter.toString() + "[" + e.getMessage()+ "]");
		}
		return true;
	}

	/*
	 * This method removes all the templates from the current view. This includes removing
	 * all of the annotations corresponding to each template only from the current network view, achieved
	 * by calling clearNetworkofTemplates(CyNetworkView)
	 * 
	 * @return the resultant network view with the removed templates
	 */
	public CyNetworkView clearCurrentNetworkOfTemplates() {
		CyNetworkView netView = registrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		this.clearNetworkofTemplates(netView);
		return netView;
	}

	/*
	 * This method removes all the templates from the given network view. This method
	 * uses networkRemoveTemplates(), passing the list of templates active in the view.
	 * 
	 * @param networkView is the network view whose templates will be removed
	 */
	public void clearNetworkofTemplates(CyNetworkView networkView) { 
		CyRow networkRow = getNetworkRow(networkView);
		networkRemoveTemplates(networkView, (List<String>) networkRow.getRaw(NETWORK_TEMPLATES));
	}

	/*
	 * Given a list of template names to remove from the passed network view, this method
	 * removes every template name in the list from the network view.
	 * 
	 * @param networkView is the network view from which to remove the templates
	 * @param templateRemoveNames is the list of template names corresponding to templates and 
	 * their annotations to remove from the view
	 * @precondition templateRemoveNames != null & templateRemoveNames is not empty
	 * Given @param networkView and @param templateRemoveNames, remove the templates from networkView
	 * corresponding to these names 
	 */
	public void networkRemoveTemplates(CyNetworkView networkView, List<String> templateRemoveNames) {
		List<Annotation> annotations = annotationManager.getAnnotations(networkView);
		List<String> uuidsToRemove = new ArrayList<>();
		if(templateRemoveNames != null && !templateRemoveNames.isEmpty())
			for(String templateRemoveName : templateRemoveNames) {
				if(templates.containsKey(templateRemoveName)) {
					List<String> templateInformation =
							templates.get(templateRemoveName);
					for(String annotationInformation : templateInformation) {
						String[] argsArray = annotationInformation.substring(
								annotationInformation.indexOf(')') + 3, 
								annotationInformation.length() - 1).split(", ");
						Map<String, String> argMap = new HashMap<>();
						for(String arg : argsArray) {
							String[] keyValuePair = arg.split("=");
							argMap.put(keyValuePair[0], keyValuePair[1]);
						}
						uuidsToRemove.add(argMap.get("uuid"));
					}
				}
			}

		//make sure that the annotation is only deleted in the specific network view, not all views
		if(annotations != null) {
			for(Annotation annotation : annotations)  
				if(uuidsToRemove.contains(annotation.getUUID().toString())) {
					annotationManager.removeAnnotation(annotation);
					annotation.removeAnnotation();
				}
			removeTemplatesActive(networkView, templateRemoveNames);
			networkView.updateView();
		}
	}

	/* Private method
	 * Creates a list of the annotation information corresponding to the given list
	 * of annotations. The annotation information is simply a String representing the
	 * respective argument maps, argMap, of each annotation
	 * 
	 * @return that list of annotation information
	 */
	private static List<String> getAnnotationInformation(List<Annotation> annotations) {
		List<String> annotationsInfo = new ArrayList<>();
		if(annotations != null)
			for(Annotation annotation : annotations)
				annotationsInfo.add(annotation.getArgMap().toString());
		return annotationsInfo;
	}

	/*
	 * @return a list of template names
	 */
	public List<String> getTemplateNames() {
		return new ArrayList<>(templates.keySet());
	}

	/* Private method
	 * Apply the template corresponding to @param templateName to @param networkView
	 */
	private void appendTemplatesActive(CyNetworkView networkView, String templateName) {
		CyRow networkRow = getNetworkRow(networkView);
		List<String> activeTemplates = (List<String>) networkRow.getRaw(NETWORK_TEMPLATES);
		if(activeTemplates == null)
			activeTemplates = new ArrayList<>();
		activeTemplates.add(templateName);
		networkRow.set(NETWORK_TEMPLATES, activeTemplates);		
	}

	/* Private method
	 * Removes the templates in @param templateRemoveNames from the @param networkView
	 */
	private void removeTemplatesActive(CyNetworkView networkView, List<String> templateRemoveNames) {
		CyRow networkRow = getNetworkRow(networkView);
		List<String> activeTemplates = (List<String>) networkRow.getRaw(NETWORK_TEMPLATES);
		List<String> templatesToRemove = new ArrayList<>();
		if(templateRemoveNames != null && !templateRemoveNames.isEmpty()) {
			for(String templateRemoveName : templateRemoveNames)
				if(activeTemplates.contains(templateRemoveName))
					templatesToRemove.add(templateRemoveName);
			activeTemplates.removeAll(templatesToRemove);
			networkRow.set(NETWORK_TEMPLATES, activeTemplates);		
		}
	}

	/* 
	 * Gets the network CyRow of the given network view and if the templates column
	 * is null, initialize it.
	 * 
	 * @param networkView is the view whose requested CyRow corresponds to 
	 * @return the CyRow of the networkView, containing information about the view
	 */
	public CyRow getNetworkRow(CyNetworkView networkView) {
		return getNetworkRow(networkView.getModel());
	}
	
	public CyRow getNetworkRow(CyNetwork network) {
		CyTable networkTable = network.getDefaultNetworkTable();
		if(!columnAlreadyExists(networkTable, NETWORK_TEMPLATES)) 
			networkTable.createListColumn(NETWORK_TEMPLATES, String.class, true);
		List<String> templatesActive = networkTable.getRow(network.getSUID()).
				getList(NETWORK_TEMPLATES, String.class);
		if(templatesActive == null)
			templatesActive = new ArrayList<>();
		return networkTable.getRow(network.getSUID());
	}

	/*
	 * Checks if a certain column name already exists in the given network table. This method
	 * is used to avoid any null pointer errors.
	 * 
	 * @param networkTable is the table containing all the network information in the session
	 * @param columnName is the name of the column on which to check whether or not it exists
	 * @return true if the column already exists in the networkTable
	 */
	public static boolean columnAlreadyExists(CyTable networkTable, String columnName) {
		for(CyColumn networkColumn : networkTable.getColumns())
			if(networkColumn.getName().equals(columnName))
				return true;
		return false;
	}

	/*
	 * Creates an annotation with arguments @param argMap, in the @param networkView, and with the
	 * information @argMap
	 */
	public Annotation getCreatedAnnotation(CyServiceRegistrar registrar, 
			CyNetworkView networkView, Map<String, String> argMap) {
		String annotationType = argMap.get("type");
		Annotation addedShape = null;
		if(networkView.getModel().getDefaultNetworkTable().getColumn("__Annotations") == null) {
			networkView.getModel().getDefaultNetworkTable().createListColumn("__Annotations", 
					String.class, false, Collections.EMPTY_LIST);
		}
		if(annotationType.contains("ShapeAnnotation")) {
			AnnotationFactory<ShapeAnnotation> annotationFactory = registrar.getService(
					AnnotationFactory.class, "(type=ShapeAnnotation.class)");
			addedShape = annotationFactory.createAnnotation(ShapeAnnotation.class, networkView, argMap);
		} else if(annotationType.contains("TextAnnotation")) {
			AnnotationFactory<TextAnnotation> annotationFactory = registrar.getService(
					AnnotationFactory.class, "(type=TextAnnotation.class)");
			addedShape = annotationFactory.createAnnotation(TextAnnotation.class, networkView, argMap);
		} else if(annotationType.contains("ImageAnnotation")) {
			AnnotationFactory<ImageAnnotation> annotationFactory = registrar.getService(
					AnnotationFactory.class, "(type=ImageAnnotation.class)");
			addedShape = annotationFactory.createAnnotation(ImageAnnotation.class, networkView, argMap);
		} else if(annotationType.contains("GroupAnnotation")) {
			AnnotationFactory<GroupAnnotation> annotationFactory = registrar.getService(
					AnnotationFactory.class, "(type=GroupAnnotation.class)");
			addedShape = annotationFactory.createAnnotation(GroupAnnotation.class, networkView, argMap);
		} else if(annotationType.contains("ArrowAnnotation")) {
			AnnotationFactory<ArrowAnnotation> annotationFactory = registrar.getService(
					AnnotationFactory.class, "(type=ArrowAnnotation.class)");
			addedShape = annotationFactory.createAnnotation(ArrowAnnotation.class, networkView, argMap);
		} else if(annotationType.contains("BoundedTextAnnotation")) {
			AnnotationFactory<BoundedTextAnnotation> annotationFactory = registrar.getService(
					AnnotationFactory.class, "(type=BoundedTextAnnotation.class)");
			addedShape = annotationFactory.createAnnotation(BoundedTextAnnotation.class, networkView, argMap);
		}
		return addedShape;
	}

	/* Private Method
	 * Rename the template corresponding to oldName with a new name, without changing any properties 
	 * of the template. Also rename all instances of the active template in the network views.
	 * 
	 * @param oldName is the name of the template to be changed 
	 * @param newName is the new name of the template
	 * @param networkView is the network view on which to check for the active template
	 * 
	 * @precondition oldName must exist in the templates map as a template
	 * @return true if the renaming was successful
	 */
	private boolean renameTemplateHelper(String oldName, String newName, CyNetwork network) {
		if(!templates.containsKey(oldName))
			return false;
		templates.put(newName, templates.get(oldName));
		templates.remove(oldName);
		if(network != null) {
			CyRow networkRow = getNetworkRow(network);
			List<String> templatesActive = (List<String>) networkRow.getRaw(NETWORK_TEMPLATES);
			if(templatesActive != null && templatesActive.contains(oldName)) {
				templatesActive.add(newName);
				templatesActive.remove(oldName);
				networkRow.set(NETWORK_TEMPLATES, templatesActive);		
			}
		}
		if(templates.containsKey(newName) && !templates.containsKey(oldName))
			return true;
		return false;
	}

	/*
	 * Rename the template corresponding to oldName with a new name, without changing any properties 
	 * of the template, by calling the helper function. Also, rename all instances of the active 
	 * template in the network views found in the manager.
	 * 
	 * @param oldName is the name of the template to be changed 
	 * @param newName is the new name of the template
	 * 
	 * @precondition oldName must exist in the templates map as a template
	 * @return true if the renaming was successful
	 */
	public boolean renameTemplate(String oldName, String newName) {
		boolean rename = true;
		Set<CyNetwork> networks = registrar.getService(CyNetworkManager.class).getNetworkSet();
		for(CyNetwork network : networks) {
			boolean temp = renameTemplateHelper(oldName, newName, network);
			if(rename && !temp)
				rename = temp;
		}
		return rename;
	}

	/*
	 * @return the template map
	 */
	public Map<String, List<String>> getTemplateMap() {
		return templates;
	}

	/*
	 * This method returns the annotation information corresponding to the given template.
	 * 
	 * @return annotation information corresponding to the template as a list of strings
	 * @precondition template exists in the templates map
	 */
	public List<String> getTemplate(String template) {
		if (templates.containsKey(template))
			return templates.get(template);
		return null;
	}

	/*
	 * This method handles encoding the thumbnail of the given template
	 * 
	 * @param template is the template in which to create a thumbnail of
	 * @return encoded thumbnail in the form of a string
	 */
	public String getEncodedThumbnail(String template) {
		Image thumb = getThumbnail(template);
		if (thumb == null) return null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write((BufferedImage) thumb, "png", baos);
			baos.flush();
			byte[] imageBytes = Base64.getEncoder().encode(baos.toByteArray());
			baos.close();
			return new String(imageBytes);
		} catch (Exception e) {
			return null;
		}
	}

	/*
	 * This method creates a new thumbnail corresponding to the given template.
	 * Add the mapping (template - thumbnail) to the thumbnails map.
	 * 
	 * @param template, the name of the thumbnail to be created
	 * @return the thumbnail corresponding to template
	 * @precondition template exists in templates map
	 */
	public Image getNewThumbnail(String template) {
		if (templates.containsKey(template)) {
			Image thumbnail = createThumbnail(template);
			if(templateThumbnails.containsKey(template))
				templateThumbnails.replace(template, thumbnail);
			else
				templateThumbnails.put(template, thumbnail);
			return thumbnail;
		}
		return null;
	}

	/*
	 * This method gets a thumbnail corresponding to the given template.
	 * If the thumbnail has not yet been created, create one and add the mapping
	 * (template - thumbnail) to the thumbnails map.
	 * 
	 * @param template, the name of the thumbnail to be created
	 * @return the thumbnail corresponding to template
	 * @precondition template exists in templates map
	 */
	public Image getThumbnail(String template) {
		if (templateThumbnails.containsKey(template))
			return templateThumbnails.get(template);
		if (templates.containsKey(template)) { //Create the thumbnail
			Image thumbnail = createThumbnail(template);
			templateThumbnails.put(template, thumbnail);
			return thumbnail;
		}
		return null;
	}

	/* Private method
	 * Adds the thumbnail to the thumbnail map iff the list of templates
	 * contains the template
	 */
	private void addThumbnail(String template, Image thumbnail) {
		if (!templates.containsKey(template))
			return;

		templateThumbnails.put(template, thumbnail);
	}

	/*
	 * Reads a thumbnail corresponding to the string thumbnail and adds the 
	 * (template - image thumbnail) mapping to the thumbnails map
	 * 
	 * @param template is the name of the template corresponding to the thumbnail 
	 * @param thumbnail is an encoded string, which contains the image
	 * @precondition templates map must contain the mapping template
	 */
	public void addThumbnail(String template, String thumbnail) {
		if (template == null || thumbnail == null) return;
		if (!templates.containsKey(template))
			return;

		byte[] bytes = thumbnail.getBytes();
		byte[] imageBytes = Base64.getDecoder().decode(bytes);
		try {
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
			addThumbnail(template, image);
		} catch(IOException e) {
			return;
		}
	}

	/* Private method
	 * This method creates a thumbnail of the list of annotations corresponding 
	 * to @param template and @return that thumbnail
	 */
	private Image createThumbnail(String template) {
		if (template == null || !templates.containsKey(template))
			return null;

		//Create network and view
		CyNetwork net = networkFactory.createNetwork(SavePolicy.DO_NOT_SAVE);
		CyNetworkView view = networkViewFactory.createNetworkView(net);

		//Add the template to our view
		useTemplate(template, view);
		Rectangle2D.Double unionRectangle = getUnionofAnnotations(view); 
		if(unionRectangle.getWidth() * unionRectangle.getHeight() < 101)
			unionRectangle.setRect(0., 0., 1000., 1000.);
		view.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, 
				unionRectangle.getX() + (unionRectangle.getWidth() / 2));
		view.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, 
				unionRectangle.getY() + (unionRectangle.getHeight() / 2));
		view.setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH, unionRectangle.getWidth());
		view.setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT, unionRectangle.getHeight());

		// Get the image
		Image img = getViewImage(template, view, unionRectangle);
		return img;
	}

	/* Private method 
	 * Given @param networkView, this method finds the union of annotations and @return
	 * this union in the form of a Rectangle2D.Double
	 * */
	private Rectangle2D.Double getUnionofAnnotations(CyNetworkView networkView) { 
		Rectangle2D.Double unionOfAnnotations = new Rectangle2D.Double();
		List<Annotation> annotations = registrar.getService(AnnotationManager.class).getAnnotations(networkView);
		List<ShapeAnnotation> shapeAnnotations = new ArrayList<>();
		if(annotations != null) 
			for(Annotation annotation : annotations) 
				if(annotation instanceof ShapeAnnotation) 
					shapeAnnotations.add((ShapeAnnotation) annotation);

		for(ShapeAnnotation shapeAnnotation : shapeAnnotations) {
			Map<String, String> argMap = shapeAnnotation.getArgMap();
			double xCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.X));
			double yCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.Y));
			double width = Double.parseDouble(argMap.get(ShapeAnnotation.WIDTH)) / shapeAnnotation.getZoom();
			double height = Double.parseDouble(argMap.get(ShapeAnnotation.HEIGHT)) / shapeAnnotation.getZoom();
			if(unionOfAnnotations.isEmpty())
				unionOfAnnotations = new Rectangle2D.Double(xCoordinate, yCoordinate, width, height);
			else
				Rectangle2D.Double.union(new Rectangle2D.Double(xCoordinate, yCoordinate, width, height), 
						unionOfAnnotations, unionOfAnnotations);
		}

		unionOfAnnotations = new Rectangle2D.Double(unionOfAnnotations.getX()-PADDING, unionOfAnnotations.getY()-PADDING, 
				unionOfAnnotations.getWidth()+PADDING, unionOfAnnotations.getHeight()+PADDING);
		return unionOfAnnotations;
	}

	/* Private method
	 * Gets the image, with size @param bounds, of the network view @param view with the name @param template
	 * 
	 * @return the image corresponding to the view
	 */
	private Image getViewImage(final String template, final CyNetworkView view, Rectangle2D.Double bounds) {
		final int width = (int) Math.abs(bounds.getWidth());
		final int height = (int) Math.abs(bounds.getHeight());

		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		if (SwingUtilities.isEventDispatchThread()) {
			renderTemplate(template, view, image, width, height);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					//@Override
					public void run() {
						renderTemplate(template, view, image, width, height);
					}
				});
			} catch (Exception e) {e.printStackTrace();}
		}

		Rectangle2D.Double newBounds = getAdjustedDimensions(bounds);
		// Now scale the image to something more reasonable
		return image.getScaledInstance((int) newBounds.getWidth(), (int) newBounds.getHeight(), Image.SCALE_SMOOTH);
	}

	/* Private method
	 * Given @param bounds, adjust and scale the dimensions of them to fit within a specific containment
	 * */
	private static Rectangle2D.Double getAdjustedDimensions(Rectangle2D.Double bounds) { 
		final int width = (int) Math.abs(bounds.getWidth());
		final int height = (int) Math.abs(bounds.getHeight());
		int min = (width < height ? width : height);
		int max = (width < height ? height : width);
		int newWidth = 0;
		int newHeight = 0;

		if(((double) max) / ((double) min) > 5.) {
			double adjustmentRatio = max / 500.;
			newWidth = (int) (width / adjustmentRatio);
			newHeight = (int) (height / adjustmentRatio);
		} else {
			double adjustmentRatio = min / 100.;
			newWidth = (int) (width / adjustmentRatio);
			newHeight = (int) (height / adjustmentRatio);
		}

		return new Rectangle2D.Double(bounds.getX(), bounds.getY(), newWidth, newHeight);
	}

	/*
	 * Renders the template and its annotations into the given network view and places it in
	 * its own template panel.
	 * 
	 * @param template is the name of the template to be rendered
	 * @param view is the network view containing a visual of what the template looks like
	 * @param img contains an wrapper image of the view
	 * @param width is the width of the image
	 * @param height is the height of the image
	 */
	public void renderTemplate(String template, CyNetworkView view, BufferedImage img, int width, int height) {
		final Graphics2D g = (Graphics2D) img.getGraphics();

		try {
			final Dimension size = new Dimension(width, height);

			JPanel panel = new JPanel();
			panel.setPreferredSize(size);
			panel.setSize(size);
			panel.setMinimumSize(size);
			panel.setMaximumSize(size);
			panel.setBackground(Color.WHITE);

			JWindow window = new JWindow();
			window.getContentPane().add(panel, BorderLayout.CENTER);
			RenderingEngine<CyNetwork> re = renderingEngineFactory.createRenderingEngine(panel, view);

			useTemplate(template, view);
			window.pack();
			window.repaint();

			re.createImage(width-(int)PADDING, height-(int)PADDING);
			re.printCanvas(g);
			g.dispose();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}