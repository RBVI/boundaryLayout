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
	@ContainsTunables
	public EdgeWeighter edgeWeighter = new EdgeWeighter();

	@Tunable(description="Category to group nodes by")
	public ListSingleSelection<String> categories = null; 
	
	@Tunable(description="Number of Iterations:",
			tooltip="The number of iterations to run the algorithm. The higher the "+
			"number, the better the accuracy yet longer run-time (1000 is recommended).")
	public int numIterations = 1000;

	@Tunable(description="Default Spring Coefficient",
			tooltip="The smaller this number is, the more the network "+
			"topology affects the layout.")
	public double defaultSpringCoefficient = 1e-4;

	@Tunable(description="Default Spring Length")
	public double defaultSpringLength = 140.0;

	@Tunable(description="Node mass",
			tooltip="The higher the node mass, the less nodes move around the network")
	public double defaultNodeMass = 3.0;

	@Tunable(description="Force deterministic layouts (slower):")
	public boolean isDeterministic;

	@Tunable(description="Don't partition graph before layout:", groups="Standard Settings")
	public boolean singlePartition;

	@Tunable(description="Avoid overlapping nodes (y/n)", 
			tooltip="Apply a force to minimize node overlap")
	public boolean avoidOverlap = false;

	@Tunable(description="Force to apply to avoid node overlap")
	public float overlapForce = 1000000000.0f;

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
		CyTable nodeTable = network.getDefaultNodeTable();
		Collection<CyColumn> columnsCollection = nodeTable.getColumns();
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
		categories = new ListSingleSelection<String>(columnNames);
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent ev) {
		setColumnTunables(ev.getNetwork());
	}
}