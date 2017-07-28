package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

/*
 * #%L
 * Cytoscape Prefuse Layout Impl (layout-prefuse-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


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

public class ForceDirectedLayout extends AbstractLayoutAlgorithm {

	private static final String ALGORITHM_ID = "boundary-constraint-layout";
	static final String ALGORITHM_DISPLAY_NAME = "Boundary Constraint Layout";
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

	public ForceDirectedLayout(final CyServiceRegistrar registrar, UndoSupport undo) {
		super(ALGORITHM_ID, ALGORITHM_DISPLAY_NAME, undo);
		this.registrar = registrar;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, 
			Set<View<CyNode>> nodesToLayOut, String attrName) {
		ForceDirectedLayoutContext settings = null;
		if (context != null && (context instanceof ForceDirectedLayoutContext)) {
			settings = (ForceDirectedLayoutContext) context;
		}
		if (settings == null) {
			settings = createLayoutContext();
		}

		ForceDirectedLayoutTask newTask = 
			new ForceDirectedLayoutTask(toString(), networkView, nodesToLayOut,
			                            settings, attrName, integrator, 
			                            registrar, undoSupport);
		return new TaskIterator(newTask);
	}

	@Override
	public ForceDirectedLayoutContext createLayoutContext() {
		ForceDirectedLayoutContext context = new ForceDirectedLayoutContext(registrar);
		return context;
	}

	@Override
	public Set<Class<?>> getSupportedNodeAttributeTypes() {
		final Set<Class<?>> ret = new HashSet<Class<?>>();

		ret.add(Integer.class);
		// ret.add(Double.class);
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
