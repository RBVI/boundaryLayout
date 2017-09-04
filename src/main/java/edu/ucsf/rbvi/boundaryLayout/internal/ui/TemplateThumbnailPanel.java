package edu.ucsf.rbvi.boundaryLayout.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class TemplateThumbnailPanel extends JPanel implements CytoPanelComponent {
	private static final long serialVersionUID = 1L;
	private CyNetworkView networkView;
	private CyServiceRegistrar registrar;
	private static final int graphPicSize = 180;
	private static final int defaultRowHeight = graphPicSize + 8;
	private VisualStyle templateStyle;
	private ImageIcon templateIcon;
	
	public TemplateThumbnailPanel() {
		this(null, null);
	}

	public TemplateThumbnailPanel(CyServiceRegistrar registrar, 
			CyNetworkView networkView) {
		super();
		this.registrar = registrar;
		this.networkView = networkView;
		this.setLayout(new BorderLayout());
		final Image templateImage = createTemplateImage(graphPicSize, graphPicSize);
		if(templateImage != null) 
			templateIcon = new TemplateImageIcon(templateImage);
		else
			templateIcon = new TemplateImageIcon();
		JLabel label = new JLabel("", templateIcon, JLabel.CENTER);
		this.add(label, BorderLayout.CENTER);
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getTitle() {
		return "Templates";
	}

	public CyNetworkView getNetworkView() {
		return networkView;
	}

	public CyNetwork getNetwork() {
		return networkView.getModel();
	}

	@SuppressWarnings("rawtypes")
	public Image createTemplateImage(int width, int height) {
		final VisualStyle visualStyle = getTemplateStyle();
		final CyNetworkView templateView = createNetworkView(networkView.getModel(), 
				visualStyle);
		
		templateView.setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH, new Double(width));
		templateView.setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT, new Double(height));
		
		AnnotationManager annotationManager = registrar.
				getService(AnnotationManager.class);
		List<Annotation> annotations = annotationManager.
				getAnnotations(networkView);

		if(annotations != null)
			for(Annotation annotation : annotations) {
				Annotation addedAnnotation = TemplateManager.getCreatedAnnotation(registrar,
						templateView, annotation.getArgMap());
				addedAnnotation.setName(annotation.getArgMap().get("name"));
				annotationManager.addAnnotation(addedAnnotation);
				addedAnnotation.update();
			}
		templateView.updateView();

		final Image image = new BufferedImage(width, height, BufferedImage.);
		
		return image;
	}
	
	public VisualStyle getTemplateStyle() {
		VisualStyleFactory visualStyleFactory = registrar.getService(VisualStyleFactory.class);
		if (templateStyle == null)
			templateStyle = visualStyleFactory.createVisualStyle("Template");

		return templateStyle;
	}

	public CyNetworkView createNetworkView(final CyNetwork network, VisualStyle visualStyle) {
		CyNetworkViewFactory networkViewFactory = registrar.getService(CyNetworkViewFactory.class);
		VisualMappingManager visualMappingManager = registrar.getService(VisualMappingManager.class);
		final CyNetworkView view = networkViewFactory.createNetworkView(network);
		if (visualStyle == null) 
			visualStyle = visualMappingManager.getDefaultVisualStyle();
		visualMappingManager.setVisualStyle(visualStyle, view);
		visualStyle.apply(view);
		view.updateView();

		return view;
	}
	
	private class TemplateImageIcon extends ImageIcon {
		static final long serialVersionUID = 1L;

		public TemplateImageIcon() {
			super();
		}

		public TemplateImageIcon(Image image) {
			super(image);
		}
	}
}