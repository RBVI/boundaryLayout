package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.io.IOException;

import org.cytoscape.view.layout.EdgeWeighter;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class ForceDirectedLayoutContext implements TunableValidator {
	
	@ContainsTunables
  public EdgeWeighter edgeWeighter = new EdgeWeighter();

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

}
