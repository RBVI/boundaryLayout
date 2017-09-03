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

public class TemplateThumbnailPanel extends JPanel implements CytoPanelComponent {
	private static final long serialVersionUID = 1L;
	private CyNetworkView networkView;
	private CyServiceRegistrar registrar;
	private static final int graphPicSize = 80;
	private static final int defaultRowHeight = graphPicSize + 8;
	private VisualStyle templateStyle;
	
	public TemplateThumbnailPanel() {
		this(null, null);
	}

	public TemplateThumbnailPanel(CyServiceRegistrar registrar, 
			CyNetworkView networkView) {
		super();
		this.registrar = registrar;
		this.networkView = networkView;
		final Image templateImage = createTemplateImage(graphPicSize, graphPicSize);
		TemplateImageIcon templateIcon;
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
		return CytoPanelName.WEST;
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
		RenderingEngineFactory renderingEngineFactory = registrar.
				getService(RenderingEngineFactory.class);

		if(annotations != null)
			for(Annotation annotation : annotations) {
				
			}

		final Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = (Graphics2D) image.getGraphics();
		
		SwingUtilities.invokeLater(new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					final Dimension size = new Dimension(width, height);

					JPanel panel = new JPanel();
					panel.setPreferredSize(size);
					panel.setSize(size);
					panel.setMinimumSize(size);
					panel.setMaximumSize(size);
					panel.setBackground((Color) visualStyle.getDefaultValue(
							BasicVisualLexicon.NETWORK_BACKGROUND_PAINT));

					JWindow window = new JWindow();
					window.getContentPane().add(panel, BorderLayout.CENTER);

					RenderingEngine<CyNetwork> renderingEngine = renderingEngineFactory.
							createRenderingEngine(panel, templateView);

					visualStyle.apply(templateView);
					templateView.fitContent();
					templateView.updateView();
					window.pack();
					window.repaint();

					renderingEngine.createImage(width, height);
					renderingEngine.printCanvas(graphics);
					graphics.dispose();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		});

		return image;
	}
	
	public VisualStyle getTemplateStyle() {
		VisualStyleFactory visualStyleFactory = registrar.getService(VisualStyleFactory.class);
		if (templateStyle == null) {
			templateStyle = visualStyleFactory.createVisualStyle("Template");

			templateStyle.setDefaultValue(BasicVisualLexicon.NODE_SIZE, 40.0);
			templateStyle.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, 40.0);
			templateStyle.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, 40.0);
			templateStyle.setDefaultValue(BasicVisualLexicon.NODE_PAINT, Color.RED);
			templateStyle.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.RED);
			templateStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 0.0);

			templateStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 5.0);
			templateStyle.setDefaultValue(BasicVisualLexicon.EDGE_PAINT, Color.BLUE);
			templateStyle.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, Color.BLUE);
			templateStyle.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.BLUE);
			templateStyle.setDefaultValue(BasicVisualLexicon.EDGE_SELECTED_PAINT, Color.BLUE);
			templateStyle.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT, Color.BLUE);
		}

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