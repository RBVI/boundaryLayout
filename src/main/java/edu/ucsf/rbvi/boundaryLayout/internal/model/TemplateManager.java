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

import javax.imageio.ImageIO;

import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
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

	@SuppressWarnings("unchecked")
	public TemplateManager(CyServiceRegistrar registrar) {
		templates = new HashMap<>();
		templateThumbnails = new HashMap<>();
		this.registrar = registrar;
		annotationManager = registrar.getService(AnnotationManager.class);	
		networkFactory = registrar.getService(CyNetworkFactory.class);	
		networkViewFactory = registrar.getService(CyNetworkViewFactory.class);	
		renderingEngineFactory = registrar.getService(RenderingEngineFactory.class);	
	}

	public boolean addTemplate(String templateName, 
			List<Annotation> annotations) {
		List<String> annotationsInfo = 
				getAnnotationInformation(annotations);
		return addTemplateStrings(templateName, annotationsInfo);
	}

	public boolean addTemplateStrings(String templateName, List<String> annotations) {
		if(templates.containsKey(templateName)) 
			return overwriteTemplateStrings(templateName, annotations);
		templates.put(templateName, annotations);
		if(templates.containsKey(templateName)) 
			return true;
		return false;
	}

	public boolean deleteTemplate(String templateName) {
		if(!templates.containsKey(templateName))
			return false;
		templates.remove(templateName);
		if(!templates.containsKey(templateName))
			return true;
		return false;
	}

	public boolean overwriteTemplate(String templateName, 
			List<Annotation> annotations) {
		List<String> annotationsInfo = getAnnotationInformation(annotations);
		return overwriteTemplateStrings(templateName, annotationsInfo);
	}

	public boolean overwriteTemplateStrings(String templateName, 
			List<String> annotations) {
		if(!templates.containsKey(templateName))
			return false;
		templates.replace(templateName, annotations);
		return true;
	}

	public boolean useTemplate(String templateName, CyNetworkView networkView) {
		if(!templates.containsKey(templateName)) {
			// System.out.println("Does not exist!!");
			return false;
		}
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

	public boolean importTemplate(String templateName,
			File templateFile) throws IOException {
		if(!templateFile.exists())
			return false;
		BufferedReader templateReader = new BufferedReader(
				new FileReader(templateFile.getAbsolutePath()));
		List<String> templateInformation = new ArrayList<>();
		String annotationInformation = "";
		while((annotationInformation = templateReader.readLine()) 
				!= null)
			templateInformation.add(annotationInformation);
		if(!templates.containsKey(templateName))
			templates.put(templateName, templateInformation);
		else
			templates.replace(templateName, templateInformation);
		try {
			templateReader.close();
		} catch (IOException e) {
			throw new IOException("Problems writing to stream: " + 
					templateReader.toString() + 
					"[" + e.getMessage()+ "]");
		}
		if(templates.containsKey(templateName))
			return true;
		return false;
	}

	public boolean exportTemplate(String templateName, 
			String absoluteFilePath) throws IOException {
		if(!templates.containsKey(templateName))
			return false;
		File exportedFile = new File(absoluteFilePath);
		if(!exportedFile.exists())
			exportedFile.createNewFile();
		BufferedWriter templateWriter = new 
				BufferedWriter(new FileWriter(exportedFile));
		for(String annotationInformation : templates.get(templateName)) {
			templateWriter.write(annotationInformation);
			templateWriter.newLine();
		}
		try {
			templateWriter.close();
		} catch (IOException e) {
			throw new IOException("Problems writing to stream: " + 
					templateWriter.toString() + 
					"[" + e.getMessage()+ "]");
		}
		return true;
	}

	public CyNetworkView clearCurrentNetworkOfTemplates() {
		CyNetworkView netView = registrar.getService(
				CyApplicationManager.class).getCurrentNetworkView();
		this.clearNetworkofTemplates(netView);
		return netView;
	}

	@SuppressWarnings("unchecked")
	public void clearNetworkofTemplates(CyNetworkView networkView) { 
		CyRow networkRow = getNetworkRow(networkView);
		networkRemoveTemplates(networkView, (List<String>) 
				networkRow.getRaw(NETWORK_TEMPLATES));
	}

	public void networkRemoveTemplates(CyNetworkView networkView, 
			List<String> templateRemoveNames) {
		System.out.println(templateRemoveNames + " are the templates to remove!");
		List<Annotation> annotations = annotationManager.
				getAnnotations(networkView);
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

		//make sure that the annotation is only deleted in the specific network view and 
		//not all views
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

	private static List<String> getAnnotationInformation(
			List<Annotation> annotations) {
		List<String> annotationsInfo = new ArrayList<>();
		if(annotations != null)
			for(Annotation annotation : annotations)
				annotationsInfo.add(annotation.getArgMap().toString());
		return annotationsInfo;
	}

	public List<String> getTemplateNames() {
		return new ArrayList<>(templates.keySet());
	}

	@SuppressWarnings("unchecked")
	private void appendTemplatesActive(CyNetworkView networkView, 
			String templateName) {
		CyRow networkRow = getNetworkRow(networkView);
		List<String> activeTemplates = (List<String>) 
				networkRow.getRaw(NETWORK_TEMPLATES);
		if(activeTemplates == null)
			activeTemplates = new ArrayList<>();
		activeTemplates.add(templateName);
		networkRow.set(NETWORK_TEMPLATES, activeTemplates);		
	}

	@SuppressWarnings("unchecked")
	private void removeTemplatesActive(CyNetworkView networkView, 
			List<String> templateRemoveNames) {
		CyRow networkRow = getNetworkRow(networkView);
		List<String> activeTemplates = (List<String>) 
				networkRow.getRaw(NETWORK_TEMPLATES);
		List<String> templatesToRemove = new ArrayList<>();
		if(templateRemoveNames != null && !templateRemoveNames.isEmpty()) {
			for(String templateRemoveName : templateRemoveNames)
				if(activeTemplates.contains(templateRemoveName))
					templatesToRemove.add(templateRemoveName);
			activeTemplates.removeAll(templatesToRemove);
			networkRow.set(NETWORK_TEMPLATES, activeTemplates);		
		}
	}

	public CyRow getNetworkRow(CyNetworkView networkView) {
		CyTable networkTable = networkView.getModel().getDefaultNetworkTable();
		if(!columnAlreadyExists(networkTable, NETWORK_TEMPLATES))
			networkTable.createListColumn(NETWORK_TEMPLATES, String.class, false);
		List<String> templatesActive = networkTable.getRow(networkView.getSUID()).
				getList(NETWORK_TEMPLATES, String.class);
		if(templatesActive == null)
			templatesActive = new ArrayList<>();
		return networkTable.getRow(networkView.getSUID());
	}

	public static boolean columnAlreadyExists(CyTable networkTable, String columnName) {
		for(CyColumn networkColumn : networkTable.getColumns())
			if(networkColumn.getName().equals(columnName))
				return true;
		return false;
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	public boolean renameTemplate(String oldName, String newName, CyNetworkView networkView) {
		if(!templates.containsKey(oldName))
			return false;
		templates.put(newName, templates.get(oldName));
		templates.remove(oldName);
		if(networkView != null) {
			CyRow networkRow = getNetworkRow(networkView);
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

	public boolean renameTemplate(String oldName, String newName) {
		return renameTemplate(oldName, newName, registrar.getService(
				CyApplicationManager.class).getCurrentNetworkView());
	}

	public Map<String, List<String>> getTemplateMap() {
		return templates;
	}

	public List<String> getTemplate(String template) {
		if (templates.containsKey(template))
			return templates.get(template);
		return null;
	}

	// Thumbnail handling
	public String getEncodedThumbnail(String template) {
		Image th = getThumbnail(template);
		if (th == null) return null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write((BufferedImage) th, "png", baos);
			baos.flush();
			byte[] imageBytes = Base64.getEncoder().encode(baos.toByteArray());
			baos.close();
			return new String(imageBytes);
		} catch (Exception e) {
			return null;
		}
	}

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

	public Image getThumbnail(String template) {
		if (templateThumbnails.containsKey(template))
			return templateThumbnails.get(template);
		if (templates.containsKey(template)) {
			// Create the thumbnail
			Image thumbnail = createThumbnail(template);
			templateThumbnails.put(template, thumbnail);
			return thumbnail;
		}
		return null;
	}
	
	public void addThumbnail(String template, Image thumbnail) {
		if (!templates.containsKey(template))
			return;

		templateThumbnails.put(template, thumbnail);
	}

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

	private Image createThumbnail(String template) {
		if (template == null || !templates.containsKey(template))
			return null;

		// Create a network
		CyNetwork net = networkFactory.createNetwork(SavePolicy.DO_NOT_SAVE);

		// Create a networkView
		CyNetworkView view = networkViewFactory.createNetworkView(net);

		// Add the template to our view
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
			// ImageIO.write((RenderedImage)img, "png", new File("/tmp/image.png"));

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
