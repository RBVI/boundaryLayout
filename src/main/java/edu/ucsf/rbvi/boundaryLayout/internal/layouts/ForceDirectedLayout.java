package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

import prefuse.util.force.EulerIntegrator;
import prefuse.util.force.Integrator;
import prefuse.util.force.RungeKuttaIntegrator;

/**
 * Initializes the force-directed capabilities of boundary layout
 */
public class ForceDirectedLayout extends AbstractLayoutAlgorithm {

	private static final String ALGORITHM_ID = "boundary-layout";
	static final String ALGORITHM_DISPLAY_NAME = "Boundary Layout";
	final CyServiceRegistrar registrar;

	private Integrators integrator = Integrators.RUNGEKUTTA;

	public enum Integrators {
		RUNGEKUTTA("Runge-Kutta"), EULER("Euler");

		private String name;

		private Integrators(String str) {
			name = str;
		}

		@Override
		public String toString() {
			return name;
		}

		public Integrator getNewIntegrator() {
			if (this == EULER)
				return new EulerIntegrator();
			else
				return new RungeKuttaIntegrator();
		}
	}

	/**
	 * Constructs a ForceDirectedLayout given the registrar
	 * @param registrar is the registrar of this session
	 * @param undo allows the user to undo this layout
	 */
	public ForceDirectedLayout(final CyServiceRegistrar registrar, UndoSupport undo) {
		super(ALGORITHM_ID, ALGORITHM_DISPLAY_NAME, undo);
		this.registrar = registrar;
	}

	/**
	 * Creates an iterator, which holds the newly created task
	 * @return the task iterator, which holds a task corresponding to the boundaryLayout algorithm
	 */
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, 
			Set<View<CyNode>> nodesToLayOut, String attrName) {
		ForceDirectedLayoutContext settings = (ForceDirectedLayoutContext) context;
		ForceDirectedLayoutTask newTask = new ForceDirectedLayoutTask(toString(), networkView, nodesToLayOut,
				settings, attrName, integrator, registrar, undoSupport);
		return new TaskIterator(newTask);
	}

	/**
	 * Creates the context of the layout seen by the user when running
	 * @return the context of this boundary layout 
	 */
	@Override
	public ForceDirectedLayoutContext createLayoutContext() {
		ForceDirectedLayoutContext context = new ForceDirectedLayoutContext(registrar);
		return context;
	}

	/**
	 * @return a set of the supported node attribute types
	 */
	@Override
	public Set<Class<?>> getSupportedNodeAttributeTypes() {
		final Set<Class<?>> ret = new HashSet<Class<?>>();

		ret.add(Integer.class);
		ret.add(Boolean.class);
		ret.add(String.class);
		ret.add(Long.class);

		return ret;
	}

	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}
}
