package am2.client.particles.ribbon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.Vector;

public class AMRibbon extends Particle {

    int NUM_CURVES = 5;    //number of ribbonCurves per ribbon
    int CURVE_RESOLUTION = 25; //lower -> faster
    float RIBBON_WIDTH = 2.25f;
    float NOISE_STEP = 0.005f;
    float MAX_SEPARATION = 2f;

    PerlinNoise noise;

    float ribbonSeparation, noisePosn;
    Vector<Vec3d> pts;
    LinkedList<RibbonCurve> curves;
    float ribbonColor;
    float ribbonWidth;
    RibbonCurve currentCurve; //current RibbonCurve
    int stepId;

    int movement = 2;

    Vec3d ribbonTarget;

    public AMRibbon(World world, float pcolor, float width, double x, double y, double z) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.setPosition(x, y, z);
        motionX = 0;
        motionY = 0;
        motionZ = 0;
        //this.setSize(5, 5);
        noise = new PerlinNoise();
        curves = new LinkedList<>();
        pts = new Vector<>();

        ribbonColor = pcolor;
        ribbonWidth = width;
        stepId = 0;

        ribbonTarget = new Vec3d(random(-movement, movement), random(-movement, movement), random(-movement, movement));
        ribbonSeparation = lerp(-MAX_SEPARATION, MAX_SEPARATION, noise.noise1(noisePosn += NOISE_STEP));

        pts.addElement(getRandPt());
        pts.addElement(getRandPt());
        pts.addElement(getRandPt());

        this.particleAge = 0;
        this.particleMaxAge = 200;

        addRibbonCurve();
    }


    @Override
    public void renderParticle(BufferBuilder par1Tessellator, Entity ent, float partialframe, float cosyaw, float cospitch, float sinyaw, float sinsinpitch, float cossinpitch) {
        // Draw any pending vertices and end the current buffer state from vanilla
        try {
            Tessellator.getInstance().draw();
        } catch (IllegalStateException e) {
            // Buffer wasn't drawing, that's fine
        }

        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        GL11.glPushMatrix();
        GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);

        double d = (this.prevPosX + (this.posX - this.prevPosX) * partialframe) - Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double d1 = (this.prevPosY + (this.posY - this.prevPosY) * partialframe) - Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double d2 = (this.prevPosZ + (this.posZ - this.prevPosZ) * partialframe) - Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        GL11.glTranslatef((float) d, (float) d1, (float) d2);
        GL11.glColor4f(1, 1, 1, 1f);
        //GL11.glScalef(0.1f, 0.1f, 0.1f);

        draw();
        GL11.glPopMatrix();
        GL11.glPopAttrib();

        // Restore vanilla particle rendering state
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/particle/particles.png"));
        par1Tessellator.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
    }

    @Override
    public int getFXLayer() {
        return 2;
    }

    void draw() {
        ribbonTarget = new Vec3d(random(-movement, movement), random(-movement, movement), random(-movement, movement));
        ribbonSeparation = lerp(-MAX_SEPARATION, MAX_SEPARATION, noise.noise1(noisePosn += NOISE_STEP));
        currentCurve.addSegment();
        int size = curves.size();
        if (size > NUM_CURVES - 1) {
            RibbonCurve c = (RibbonCurve) curves.get(0);
            c.removeSegment();
        }
        stepId++;

        if (stepId > CURVE_RESOLUTION) addRibbonCurve();

        //draw curves
        for (int i = 0; i < size; i++) {
            RibbonCurve c = (RibbonCurve) curves.get(i);
            c.draw();
        }
    }

    private float random(float min, float max) {
        return (float) ((Math.random() * max) - min);
    }

    private float lerp(float start, float stop, float amount) {
        return start + ((stop - start) * amount);
    }

    void addRibbonCurve() {
        //add new point
        pts.addElement(getRandPt());

        Vec3d nextPt = (Vec3d) pts.elementAt(pts.size() - 1);
        Vec3d curPt = (Vec3d) pts.elementAt(pts.size() - 2);
        Vec3d lastPt = (Vec3d) pts.elementAt(pts.size() - 3);

        Vec3d lastMidPt = new Vec3d((curPt.x + lastPt.x) / 2, (curPt.y + lastPt.y) / 2, (curPt.z + lastPt.z) / 2);

        Vec3d midPt = new Vec3d((curPt.x + nextPt.x) / 2, (curPt.y + nextPt.y) / 2, (curPt.z + nextPt.z) / 2);

        float width = 0.2f;

        currentCurve = new RibbonCurve(lastMidPt, midPt, curPt, width, CURVE_RESOLUTION, ribbonColor);
        curves.add(currentCurve);

        //remove old curves
        if (curves.size() > NUM_CURVES) {
            curves.removeFirst();
        }

        stepId = 0;

    }

    Vec3d getRandPt() {
        return new Vec3d(
                ribbonTarget.x + random(-ribbonSeparation, ribbonSeparation),
                ribbonTarget.y + random(-ribbonSeparation, ribbonSeparation),
                ribbonTarget.z + random(-ribbonSeparation, ribbonSeparation));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }
}