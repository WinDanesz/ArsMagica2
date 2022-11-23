package am2.client.particles.ribbon;

import java.util.LinkedList;

import am2.client.particles.AMParticleIcons;
import net.minecraft.util.math.Vec3d;

public class RibbonCurve{
	float ribbonWidth;
	float resolution;
	Vec3d startPt, endPt, controlPt;
	int stepId;
	float ribbonColor = 0;
	LinkedList<Quad3D> quads;

	RibbonCurve(Vec3d pStartPt, Vec3d pEndPt, Vec3d pControlPt, float pwidth, float presolution, float pcolor){
		startPt = pStartPt;
		endPt = pEndPt;
		controlPt = pControlPt;
		resolution = presolution;
		ribbonWidth = pwidth;
		stepId = 0;
		ribbonColor = pcolor;
		quads = new LinkedList<>();
	}

	void draw(){
		int size = quads.size();
		for (int i = 0; i < size; i++){
			Quad3D q = quads.get(i);
			q.draw();
		}
	}

	void removeSegment(){
		if (quads.size() > 1) quads.removeFirst();
	}

	void addSegment(){
		float t = stepId / resolution;
		Vec3d p0 = getOffsetPoint(t, 0);
		Vec3d p3 = getOffsetPoint(t, ribbonWidth);

		stepId++;
		if (stepId > resolution) return;

		t = stepId / resolution;
		Vec3d p1 = getOffsetPoint(t, 0);
		Vec3d p2 = getOffsetPoint(t, ribbonWidth);

		Quad3D q = new Quad3D(p0, p1, p2, p3, AMParticleIcons.instance.getIconByName("symbols"));
		quads.add(q);
	}

	/**
	 * Given a bezier curve defined by 3 points, an offset distance (k) and a time (t), returns an Vec3d
	 */

	Vec3d getOffsetPoint(float t, float k){
		Vec3d p0 = startPt;
		Vec3d p1 = controlPt;
		Vec3d p2 = endPt;

		//-- x(t), y(t)
		double xt = (1 - t) * (1 - t) * p0.x + 2 * t * (1 - t) * p1.x + t * t * p2.x;
		double yt = (1 - t) * (1 - t) * p0.y + 2 * t * (1 - t) * p1.y + t * t * p2.y;
		double zt = (1 - t) * (1 - t) * p0.z + 2 * t * (1 - t) * p1.z + t * t * p2.z;

		//-- x'(t), y'(t)
		double xd = t * (p0.x - 2 * p1.x + p2.x) - p0.x + p1.x;
		double yd = t * (p0.y - 2 * p1.y + p2.y) - p0.y + p1.y;
		double zd = t * (p0.z - 2 * p1.z + p2.z) - p0.z + p1.z;
		double dd = (float)Math.pow(xd * xd + yd * yd + zd * zd, 1 / 3);

		return new Vec3d(xt + (k * yd) / dd, yt - (k * xd) / dd, zt - (k * xd) / dd);

	}
}
