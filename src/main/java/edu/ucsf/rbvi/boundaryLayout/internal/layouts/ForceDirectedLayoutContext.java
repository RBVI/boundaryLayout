package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.EdgeWeighter;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListSingleSelection;

public class ForceDirectedLayoutContext implements TunableValidator, SetCurrentNetworkListener {	
	
	@Tunable(description="Number of Iterations:", gravity=4.0, groups={"Layout Parameters"})
	public int numIterations = 250;

	@Tunable(description="Default Spring Coefficient",
			tooltip="The smaller this number is, the more the network "+
			"topology affects the layout.", gravity=5.0, groups={"Layout Parameters"})
	public double defaultSpringCoefficient = 1e-4;

	@Tunable(description="Default Spring Length", groups={"Layout Parameters"}, gravity=6.0)
	public double defaultSpringLength = 140.0;

	@Tunable(description="Node mass", gravity=7.0, groups={"Layout Parameters"},
			tooltip="The higher the node mass, the less nodes move around the network")
	public double defaultNodeMass = 3.0;

	@Tunable(description="Avoid overlapping nodes (y/n)",
	         groups={"Layout Parameters"},gravity=12.0,	
			tooltip="Apply a force to minimize node overlap")
	public boolean avoidOverlap = true;

	@Tunable(description="Force to apply to avoid node overlap",
	         groups={"Layout Parameters"},gravity=10.0)
	public float overlapForce = 100f;
		
	@Tunable(description="speed limit", gravity=9.0, groups={"Layout Parameters"})
	public float speedLimit = 0.1f;
	
	@Tunable(description="Strength of boundaries", gravity=13.0, groups = {"Boundary Parameters"})
	public float wallGravitationalConstant = 20.0f;
	
	@Tunable(description="Variable wall forces", gravity = 14.0, groups = {"Boundary Parameters"})
	public boolean variableWallForce = true;
	
	@Tunable(description="Edge weight column", groups={"Edge Weight Settings"}, gravity=15.0)
	public ListSingleSelection<String> edgeWeight = null; 

	@ContainsTunables
	public EdgeWeighter edgeWeighter = new EdgeWeighter();

	public ForceDirectedLayoutContext(CyServiceRegistrar registrar) {
		super();
		registrar.registerService(this, SetCurrentNetworkListener.class, new Properties());
		CyNetwork network = registrar.getService(CyApplicationManager.class).getCurrentNetwork();
		if (network != null)
			setColumnTunables(network);
	}
	
	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		try {
			if (!isPositive(numIterations))
				errMsg.append("Number of iterations must be > 0; current value = "+numIterations);
			if (!isPositive(defaultSpringCoefficient))
				errMsg.append("Default spring coefficient must be > 0; current value = "+defaultSpringCoefficient);
			if (!isPositive(defaultSpringLength))
				errMsg.append("Default spring length must be > 0; current value = "+defaultSpringLength);
			if (!isPositive(defaultNodeMass))
				errMsg.append("Default node mass must be > 0; current value = "+defaultNodeMass);
		} catch (IOException e) {}
		return isPositive(numIterations) && isPositive(defaultSpringCoefficient)
				&& isPositive(defaultSpringLength) && isPositive(defaultNodeMass)
				? ValidationState.OK : ValidationState.INVALID;
	}

	private static boolean isPositive(final int n) {
		return n > 0;
	}

	private static boolean isPositive(final double n) {
		return n > 0.0;
	}

	protected void setColumnTunables(CyNetwork network) {
		CyTable edgeTable = network.getDefaultEdgeTable();
		Collection<CyColumn> columnsCollection = edgeTable.getColumns();
		List<String> columnNames = new ArrayList<String>();
		for(CyColumn column : columnsCollection)
		{
			String name = column.getName();
			if (name.equals(CyNetwork.SUID) ||
					name.equals(CyNetwork.NAME) ||
					name.equals(CyRootNetwork.SHARED_NAME) || 
					name.equals(CyNetwork.SELECTED))
				continue;
			columnNames.add(name);
		}
		
		// Now sort the list
		Collections.sort(columnNames);
		columnNames.add(0, "--None--");
		edgeWeight = new ListSingleSelection<String>(columnNames);
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent ev) {
		if (ev.getNetwork() != null)
			setColumnTunables(ev.getNetwork());
	}
}
