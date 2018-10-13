package prefuse.util.force;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import edu.ucsf.rbvi.boundaryLayout.internal.layouts.BoundaryAnnotation;

public class PolygonalWallForce extends BoundaryWallForce {
	private BoundaryAnnotation boundary;

	public PolygonalWallForce(BoundaryAnnotation boundary, Point2D center, Point2D dimensions, 
			float gravConst, boolean variableWall, double scaleFactor) {
		super(center, dimensions, gravConst, variableWall, scaleFactor);
		this.boundary = boundary;
	}
	
	@Override
	public void getForce(ForceItem item) {
		if(!isActive(item.category))
			return;
		float[] itemDim = item.dimensions;
		float[] itemLoc = item.location;
		float dx = (float) center.getX() - itemLoc[0];
		float dy = (float) center.getY() - itemLoc[1];
		float dz = (float) Math.sqrt(dx * dx + dy * dy);
		
		double nodeHyp = (double) itemDim[2];
		Point2D itemPoint = new Point2D.Double((double) itemLoc[0], (double) itemLoc[1]);
		Line2D forceLine = boundary.getClosestLine(itemPoint);
		float dist = (float) (forceLine.ptLineDist(itemPoint) - nodeHyp);
		float v = params[OUT_GRAVITATIONAL_CONST] * item.mass / (dist * dist);
		float vx = Math.abs(v * dx / dz);
		float vy = Math.abs(v * dy / dz);
		vx = (vx > ABS_MAX_FORCE ? ABS_MAX_FORCE : vx);
		vy = (vy > ABS_MAX_FORCE ? ABS_MAX_FORCE : vy);
		
		int dir = (activeOn.get(0).equals(item.category) ? 1 : -1);
		
		item.force[0] += vx * dir * (dx > 0 ? 1 : -1);
		item.force[1] += vy * dir * (dy > 0 ? 1 : -1);
	}
}
