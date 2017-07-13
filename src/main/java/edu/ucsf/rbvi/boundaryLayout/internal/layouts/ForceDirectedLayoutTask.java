package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import  java.awt.geom.Point2D;
import  java.awt.geom.Rectangle2D;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.AbstractPartitionLayoutTask;
import org.cytoscape.view.layout.LayoutEdge;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPartition;
import org.cytoscape.view.layout.LayoutPoint;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

import prefuse.util.force.CircularWallForce;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceItem;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.RectangularWallForce;
import prefuse.util.force.SpringForce;
import prefuse.util.force.WallForce;

// TODO: add circle and wall forces

public class ForceDirectedLayoutTask extends AbstractPartitionLayoutTask {

	private ForceDirectedLayout.Integrators integrator;
	private Map<LayoutNode,ForceItem> forceItems;
	private ForceDirectedLayoutContext context;
	private CyServiceRegistrar registrar;
	private String chosenCategory;
	final CyNetworkView netView;

	public ForceDirectedLayoutTask( final String displayName,
			final CyNetworkView netView,
			final Set<View<CyNode>> nodesToLayOut,
			final ForceDirectedLayoutContext context,
			final ForceDirectedLayout.Integrators integrator,
			final String attrName, final CyServiceRegistrar registrar, 
			final UndoSupport undo) {
		super(displayName, context.singlePartition, netView, nodesToLayOut, attrName, undo);

		context.setColumnTunables(netView.getModel());

		this.netView = netView;
		this.context = context;
		this.integrator = integrator;
		this.registrar = registrar;
		this.chosenCategory = attrName;

		edgeWeighter = context.edgeWeighter;
		edgeWeighter.setWeightAttribute(layoutAttribute);

		forceItems = new HashMap<LayoutNode, ForceItem>();
	}


	public void layoutPartition(LayoutPartition part) {
		LayoutPoint initialLocation = null;
		// System.out.println("layoutPartion: "+part.getEdgeList().size()+" edges");
		// Calculate our edge weights
		part.calculateEdgeWeights();
		// System.out.println("layoutPartion: "+part.getEdgeList().size()+" edges after calculateEdgeWeights");

		//m_fsim.setIntegrator(integrator.getNewIntegrator());
		//m_fsim.clear();

		ForceSimulator m_fsim = new ForceSimulator();
		for(ShapeAnnotation shapeAnnotation : getShapeAnnotations())
			addAnnotationForce(m_fsim, shapeAnnotation);
		m_fsim.addForce(new NBodyForce(context.avoidOverlap, context.overlapForce));
		m_fsim.addForce(new SpringForce());
		m_fsim.addForce(new DragForce());
		m_fsim.addForce(new RectangularWallForce(-500, new Point2D.Double(0., 0.), 800, 800));

		forceItems.clear();

		List<LayoutNode> nodeList = part.getNodeList();
		List<LayoutEdge> edgeList = part.getEdgeList();


		if(context.isDeterministic){
			Collections.sort(nodeList);
			Collections.sort(edgeList);
		}
		// initialize nodes
		for (LayoutNode ln: nodeList) {
			ForceItem fitem = forceItems.get(ln); 
			if ( fitem == null ) {
				fitem = new ForceItem();
				forceItems.put(ln, fitem);
			}

			View<CyNode> nodeView = netView.getNodeView(ln.getNode());
			fitem.mass = getMassValue(ln);
			fitem.location[0] = 0f; 
			fitem.location[1] = 0f; 

			double width = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH) / 2;
			double height = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT) / 2;
			fitem.dimensions[0] = (float) width;
			fitem.dimensions[1] = (float) height;
			m_fsim.addItem(fitem);
		}

		// initialize edges
		for (LayoutEdge e: edgeList) {
			LayoutNode n1 = e.getSource();
			ForceItem f1 = forceItems.get(n1); 
			LayoutNode n2 = e.getTarget();
			ForceItem f2 = forceItems.get(n2); 
			if ( f1 == null || f2 == null )
				continue;
			m_fsim.addSpring(f1, f2, getSpringCoefficient(e), getSpringLength(e)); 
		}

		// setTaskStatus(5); // This is a rough approximation, but probably good enough
		if (taskMonitor != null) {
			taskMonitor.setStatusMessage("Initializing partition "+part.getPartitionNumber());
		}

		// Figure out our starting point
		initialLocation = part.getAverageLocation();

		// perform layout
		long timestep = 1000L;
		for ( int i = 0; i < context.numIterations && !cancelled; i++ ) {
			timestep *= (1.0 - i/(double)context.numIterations);
			long step = timestep+50;
			m_fsim.runSimulator(step);
			setTaskStatus((int)(((double)i/(double)context.numIterations)*90.+5));
		}
		// update positions
		part.resetNodes(); // reset the nodes so we get the new average location
		for (LayoutNode ln: part.getNodeList()) {
			if (!ln.isLocked()) {
				ForceItem fitem = forceItems.get(ln); 
				System.out.println(fitem.location[0] + "," + fitem.location[1]);
				ln.setX(fitem.location[0]);
				ln.setY(fitem.location[1]);
				part.moveNodeToLocation(ln);
			}
		}
	}
	/**
	 * Get the mass value associated with the given node. Subclasses should
	 * override this method to perform custom mass assignment.
	 * @param n the node for which to compute the mass value
	 * @return the mass value for the node. By default, all items are given
	 * a mass value of 1.0.
	 */
	protected float getMassValue(LayoutNode n) {
		return (float)context.defaultNodeMass;
	}

	/**
	 * Get the spring length for the given edge. Subclasses should
	 * override this method to perform custom spring length assignment.
	 * @param e the edge for which to compute the spring length
	 * @return the spring length for the edge. A return value of
	 * -1 means to ignore this method and use the global default.
	 */
	protected float getSpringLength(LayoutEdge e) {
		double weight = e.getWeight();
		if (weight == 0.0)
			return (float)(context.defaultSpringLength);

		return (float)(context.defaultSpringLength/weight);
	}

	/**
	 * Get the spring coefficient for the given edge, which controls the
	 * tension or strength of the spring. Subclasses should
	 * override this method to perform custom spring tension assignment.
	 * @param e the edge for which to compute the spring coefficient.
	 * @return the spring coefficient for the edge. A return value of
	 * -1 means to ignore this method and use the global default.
	 */
	protected float getSpringCoefficient(LayoutEdge e) {
		return (float)context.defaultSpringCoefficient;
	}

	//gets all the shape annotations in the network view
	protected List<ShapeAnnotation> getShapeAnnotations() {
		List<Annotation> annotations = 
				registrar.getService(AnnotationManager.class).getAnnotations(netView);
		List<ShapeAnnotation> shapeAnnotations = new ArrayList<ShapeAnnotation>();
		for(Annotation annotation : annotations)
			if(annotation instanceof ShapeAnnotation)
				shapeAnnotations.add((ShapeAnnotation)annotation);
		return shapeAnnotations;
	}

	//add a force annotation for each of the shape annotations depending on type
	//of annotation
	protected void addAnnotationForce(ForceSimulator m_fsim, ShapeAnnotation shapeAnnotation) {
		float[] annotationCenter = getAnnotationCenter(shapeAnnotation);
		float[] annotationDimensions = getAnnotationDimensions(shapeAnnotation);
		switch(shapeAnnotation.getShapeType()) {
			case "RECTANGLE":
				m_fsim.addForce(new RectangularWallForce(new Point2D.Double(annotationCenter[0], 
					annotationCenter[1]), annotationDimensions[0], annotationDimensions[1]));
				break;
			case "ELLIPSE":
				if(annotationDimensions[0] == annotationDimensions[1])
					m_fsim.addForce(new CircularWallForce(new Point2D.Double(annotationCenter[0], 
					annotationCenter[1]), annotationDimensions[0]));
				//add else for ellipse
				break;
		}
	}
	
	//gets dimensions for the shape annotation passed
	private static float[] getAnnotationDimensions(ShapeAnnotation shapeAnnotation) {
		float[] annotationDimensions = {Float.parseFloat(shapeAnnotation.WIDTH), 
			Float.parseFloat(shapeAnnotation.HEIGHT)};
		return annotationDimensions;
	}
	
	//gets centerpoint for the shape annotation passed
	private static float[] getAnnotationCenter(ShapeAnnotation shapeAnnotation) { 
		float[] annotationCenter = {Float.parseFloat(shapeAnnotation.X), 
				Float.parseFloat(shapeAnnotation.Y)};
		return annotationCenter;
	}
}