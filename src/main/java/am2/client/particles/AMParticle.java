package am2.client.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

@SideOnly(Side.CLIENT)
public class AMParticle extends Particle {

    private boolean ignoreMaxAge;
    private final List<ParticleController> controllers;
    private final ControllerComparator comparer;

    private float particleRed;
    private float particleGreen;
    private float particleBlue;
    private float particleAlpha;

    private float particleScaleX = 0.2f;
    private float particleScaleY = 0.2f;
    private float particleScaleZ = 0.2f;

    private boolean isRadiant = false;
    private boolean isBreak = false;
    private boolean isAffectedByGravity = false;
    private boolean ignoreNoControllers = false;
    private boolean doVelocityUpdates = true;
    private boolean ignoreLight = false;

    // Optional window into the sprite (fractions 0-1), so a tiny particle shows a crisp patch of a block texture
    private float subU = 0;
    private float subV = 0;
    private float subSize = 1;

    // Tumbling 3D cube mode (rotation in radians, spin in radians per tick)
    private boolean cube = false;
    private float rotX, rotY, rotZ, prevRotX, prevRotY, prevRotZ;
    private float spinX, spinY, spinZ;

    public static String[] particleTypes;

    /** Render this particle as a small tumbling cube instead of a camera-facing quad. */
    public AMParticle setTumblingCube(float maxSpinRadians) {
        this.cube = true;
        this.rotX = this.prevRotX = rand.nextFloat() * 6.2832f;
        this.rotY = this.prevRotY = rand.nextFloat() * 6.2832f;
        this.rotZ = this.prevRotZ = rand.nextFloat() * 6.2832f;
        this.spinX = (rand.nextFloat() * 2 - 1) * maxSpinRadians;
        this.spinY = (rand.nextFloat() * 2 - 1) * maxSpinRadians;
        this.spinZ = (rand.nextFloat() * 2 - 1) * maxSpinRadians;
        return this;
    }

    /** Show only a {@code size} x {@code size} patch of the sprite, starting at ({@code u}, {@code v}); all in 0-1 sprite fractions. */
    public AMParticle setTextureSubRegion(float u, float v, float size) {
        this.subU = u;
        this.subV = v;
        this.subSize = size;
        return this;
    }

    public void setParticleAge(int age) {
        this.particleAge = age;
    }

    public AMParticle(World par1World, double par2, double par4, double par6) {
        super(par1World, 0, 0, 0, 0, 0, 0);
        motionX = 0;
        motionY = 0;
        motionZ = 0;
        this.setPosition(par2, par4, par6);
        par2 += (rand.nextFloat() - rand.nextFloat()) * 0.05F;
        par4 += (rand.nextFloat() - rand.nextFloat()) * 0.05F;
        par6 += (rand.nextFloat() - rand.nextFloat()) * 0.05F;
        particleRed = particleGreen = particleBlue = particleAlpha = 1.0F;
        this.ignoreMaxAge = false;
        particleMaxAge = 20 + rand.nextInt(20);
        controllers = new ArrayList<ParticleController>();
        comparer = new ControllerComparator();

        this.particleGravity = 1;

        this.setRandomScale(0.1f, 0.3f);
    }

    public AMParticle setAffectedByGravity() {
        this.isAffectedByGravity = true;
        return this;
    }

    public AMParticle setDontRequireControllers() {
        this.ignoreNoControllers = true;
        return this;
    }

    public void setNoVelocityUpdates() {
        this.doVelocityUpdates = false;
    }

    public AMParticle setIgnoreLight(boolean ignoreLight) {
        this.ignoreLight = ignoreLight;
        return this;
    }

    public boolean isRadiant() {
        return isRadiant;
    }

    public boolean isBlockTexture() {
        return isBreak;
    }

    public void addRandomOffset(double maxX, double maxY, double maxZ) {
        double newX = this.posX + (rand.nextDouble() * maxX) - (maxX / 2);
        double newY = this.posY + (rand.nextDouble() * maxY) - (maxY / 2);
        double newZ = this.posZ + (rand.nextDouble() * maxZ) - (maxZ / 2);

        this.setPosition(newX, newY, newZ);
    }

    public float getParticleScaleX() {
        return this.particleScaleX;
    }

    public float getParticleScaleY() {
        return this.particleScaleY;
    }

    public float getParticleScaleZ() {
        return this.particleScaleZ;
    }

    public AMParticle setRandomScale(float min, float max) {
        this.setParticleScale((rand.nextFloat() * (max - min)) + min);
        return this;
    }

    public void setParticleScale(float scale) {
        this.particleScaleX = scale;
        this.particleScaleY = scale;
        this.particleScaleZ = scale;
    }

    public void setParticleScale(float scaleX, float scaleY, float scaleZ) {
        this.particleScaleX = scaleX;
        this.particleScaleY = scaleY;
        this.particleScaleZ = scaleZ;
    }

    public int GetParticleAge() {
        return this.particleAge;
    }

    public int GetParticleMaxAge() {
        return this.particleMaxAge;
    }

    public void setMaxAge(int age) {
        this.particleMaxAge = age;
    }

    public void setIgnoreMaxAge(boolean ignore) {
        this.ignoreMaxAge = ignore;
        this.particleAge = 0;
    }

    public void setRGBColorF(float r, float g, float b) {
        this.particleRed = r;
        this.particleGreen = g;
        this.particleBlue = b;
    }

    public void setRGBColorI(int color) {
        this.particleRed = ((color >> 16) & 0xFF) / 255.0f;
        this.particleGreen = ((color >> 8) & 0xFF) / 255.0f;
        this.particleBlue = (color & 0xFF) / 255.0f;
    }

    public void SetParticleAlpha(float alpha) {
        this.particleAlpha = alpha;
    }

    public float GetParticleRed() {
        return this.particleRed;
    }

    public float GetParticleGreen() {
        return this.particleGreen;
    }

    public float GetParticleBlue() {
        return this.particleBlue;
    }

    public float GetParticleAlpha() {
        return this.particleAlpha;
    }

    public void AddParticleController(ParticleController controller) {
        controllers.add(controller);
        Collections.sort(controllers, comparer);
    }

    public void RemoveParticleController(ParticleController controller) {
        this.controllers.remove(controller);
    }

    public void ClearParticleControllers() {
        this.controllers.clear();
    }

    @Override
    public int getBrightnessForRender(float par1) {
        return this.ignoreLight ? 15728880 : super.getBrightnessForRender(par1);
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (cube) {
            prevRotX = rotX;
            prevRotY = rotY;
            prevRotZ = rotZ;
            rotX += spinX;
            rotY += spinY;
            rotZ += spinZ;
        }

        if (isAffectedByGravity)
            this.motionY -= 0.04D * this.particleGravity;
        if (doVelocityUpdates)
            this.move(this.motionX, this.motionY, this.motionZ);

        List<ParticleController> remove = new ArrayList<ParticleController>();

        for (ParticleController pmc : controllers) {
            if (pmc.getFinished()) {
                remove.add(pmc);
                continue;
            }
            pmc.onUpdate(this.world);
            if (pmc.getExclusive()) {
                break;
            }
        }

        for (ParticleController pmc : remove) {
            controllers.remove(pmc);
        }

        if ((particleAge++ > particleMaxAge && !this.ignoreMaxAge) || (!ignoreNoControllers && controllers.isEmpty())) {
            this.setExpired();
        }
    }

    public class ControllerComparator implements Comparator<ParticleController> {
        @Override
        public int compare(ParticleController o1, ParticleController o2) {
            return (o1.getPriority() > o2.getPriority() ? 1 : (o1 == o2 ? 0 : -1));
        }
    }

    @Override
    public int getFXLayer() {
        return 2;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float cosyaw, float cospitch, float sinyaw, float sinsinpitch, float cossinpitch) {
        if (!this.world.isRemote) {
            return;
        }
        float f11 = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
        float f12 = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
        float f13 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);

        if (this.isRadiant) {
            renderRadiant(Tessellator.getInstance(), partialTicks);
        } else {
            if (this.particleTexture == null) { //|| this.renderManager.renderEngine == null){
                return;
            }
            //			tessellator.setBrightness(0x0F0000F0);
            //			tessellator.setColorRGBA_F(this.GetParticleRed(), this.GetParticleGreen(), this.GetParticleBlue(), this.GetParticleAlpha());

            float scaleFactorX = this.getParticleScaleX();
            float scaleFactorY = this.getParticleScaleY();
            float scaleFactorZ = this.getParticleScaleZ();

            float spriteMinU = this.particleTexture.getMinU();
            float spriteMinV = this.particleTexture.getMinV();
            float spanU = this.particleTexture.getMaxU() - spriteMinU;
            float spanV = this.particleTexture.getMaxV() - spriteMinV;
            float min_u = spriteMinU + spanU * subU;
            float min_v = spriteMinV + spanV * subV;
            float max_u = min_u + spanU * subSize;
            float max_v = min_v + spanV * subSize;

            int brightness = this.getBrightnessForRender(partialTicks);
            int j = brightness >> 16 & 65535;
            int k = brightness & 65535;

            if (cube) {
                renderCube(buffer, partialTicks, f11, f12, f13, scaleFactorX, min_u, min_v, max_u, max_v, j, k);
                return;
            }

            buffer.pos(f11 - cosyaw * scaleFactorX - sinsinpitch * scaleFactorX, f12 - cospitch * scaleFactorY, f13 - sinyaw * scaleFactorZ - cossinpitch * scaleFactorZ).tex(max_u, max_v).color(this.GetParticleRed(), this.GetParticleGreen(), this.GetParticleBlue(), this.GetParticleAlpha()).lightmap(j, k).endVertex();
            buffer.pos(f11 - cosyaw * scaleFactorX + sinsinpitch * scaleFactorX, f12 + cospitch * scaleFactorY, f13 - sinyaw * scaleFactorZ + cossinpitch * scaleFactorZ).tex(max_u, min_v).color(this.GetParticleRed(), this.GetParticleGreen(), this.GetParticleBlue(), this.GetParticleAlpha()).lightmap(j, k).endVertex();
            buffer.pos(f11 + cosyaw * scaleFactorX + sinsinpitch * scaleFactorX, f12 + cospitch * scaleFactorY, f13 + sinyaw * scaleFactorZ + cossinpitch * scaleFactorZ).tex(min_u, min_v).color(this.GetParticleRed(), this.GetParticleGreen(), this.GetParticleBlue(), this.GetParticleAlpha()).lightmap(j, k).endVertex();
            buffer.pos(f11 + cosyaw * scaleFactorX - sinsinpitch * scaleFactorX, f12 - cospitch * scaleFactorY, f13 + sinyaw * scaleFactorZ - cossinpitch * scaleFactorZ).tex(min_u, max_v).color(this.GetParticleRed(), this.GetParticleGreen(), this.GetParticleBlue(), this.GetParticleAlpha()).lightmap(j, k).endVertex();
        }
    }

    /**
     * Draws the particle as a cube with edge length {@code 2 * half}, centered on (cx, cy, cz) relative to the camera.
     * Only faces turned towards the camera are emitted, so translucent textures don't show the cube's insides.
     */
    private void renderCube(BufferBuilder buffer, float partialTicks, float cx, float cy, float cz, float half,
                            float minU, float minV, float maxU, float maxV, int lightU, int lightV) {
        float ax = prevRotX + (rotX - prevRotX) * partialTicks;
        float ay = prevRotY + (rotY - prevRotY) * partialTicks;
        float az = prevRotZ + (rotZ - prevRotZ) * partialTicks;
        float sinX = (float) Math.sin(ax), cosX = (float) Math.cos(ax);
        float sinY = (float) Math.sin(ay), cosY = (float) Math.cos(ay);
        float sinZ = (float) Math.sin(az), cosZ = (float) Math.cos(az);

        float[] n = new float[3];
        float[] u = new float[3];
        float[] v = new float[3];
        float[] corner = new float[3];
        float[] us = {0, 1, 1, 0};
        float[] vs = {1, 1, 0, 0};
        float[] signU = {-1, 1, 1, -1};
        float[] signV = {-1, -1, 1, 1};

        for (int axis = 0; axis < 3; axis++) {
            for (int sign = -1; sign <= 1; sign += 2) {
                // Face normal, plus tangent axes ordered so u x v = normal (corners come out counter-clockwise from outside)
                java.util.Arrays.fill(n, 0);
                java.util.Arrays.fill(u, 0);
                java.util.Arrays.fill(v, 0);
                n[axis] = sign;
                u[(axis + (sign > 0 ? 1 : 2)) % 3] = 1;
                v[(axis + (sign > 0 ? 2 : 1)) % 3] = 1;

                rotateCubeVector(n, sinX, cosX, sinY, cosY, sinZ, cosZ);
                rotateCubeVector(u, sinX, cosX, sinY, cosY, sinZ, cosZ);
                rotateCubeVector(v, sinX, cosX, sinY, cosY, sinZ, cosZ);

                // Skip faces pointing away from the camera: (camera - faceCenter) . normal <= 0
                float toCamera = -(cx + n[0] * half) * n[0] - (cy + n[1] * half) * n[1] - (cz + n[2] * half) * n[2];
                if (toCamera <= 0) continue;

                // Block-style shading: lighter on top, darker underneath
                float shade = 0.8f + 0.2f * n[1];
                float r = this.GetParticleRed() * shade;
                float g = this.GetParticleGreen() * shade;
                float b = this.GetParticleBlue() * shade;

                for (int c = 0; c < 4; c++) {
                    for (int i = 0; i < 3; i++) {
                        corner[i] = (n[i] + u[i] * signU[c] + v[i] * signV[c]) * half;
                    }
                    buffer.pos(cx + corner[0], cy + corner[1], cz + corner[2])
                            .tex(minU + (maxU - minU) * us[c], minV + (maxV - minV) * vs[c])
                            .color(r, g, b, this.GetParticleAlpha())
                            .lightmap(lightU, lightV).endVertex();
                }
            }
        }
    }

    private static void rotateCubeVector(float[] p, float sinX, float cosX, float sinY, float cosY, float sinZ, float cosZ) {
        float x = p[0], y = p[1], z = p[2];
        float t = y * cosX - z * sinX;
        z = y * sinX + z * cosX;
        y = t;
        t = x * cosY + z * sinY;
        z = -x * sinY + z * cosY;
        x = t;
        t = x * cosZ - y * sinZ;
        y = x * sinZ + y * cosZ;
        x = t;
        p[0] = x;
        p[1] = y;
        p[2] = z;
    }

    private void renderRadiant(Tessellator tessellator, float partialFrame) {
        RenderHelper.disableStandardItemLighting();
        float var4 = (this.GetParticleAge() + partialFrame) / this.GetParticleMaxAge();
        float var5 = 0.0F;

        if (var4 > 0.8F) {
            var5 = (var4 - 0.8F) / 0.2F;
        }

        Random var6 = new Random(432L);
        float f11 = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialFrame - Minecraft.getMinecraft().getRenderManager().viewerPosX);
        float f12 = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialFrame - Minecraft.getMinecraft().getRenderManager().viewerPosY);
        float f13 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialFrame - Minecraft.getMinecraft().getRenderManager().viewerPosZ);

        GlStateManager.pushMatrix();
        GlStateManager.translate(f11, f12, f13);
        GlStateManager.scale(getParticleScaleX(), getParticleScaleY(), getParticleScaleZ());

        for (int var7 = 0; var7 < 50.0F; ++var7) {
            GlStateManager.rotate(var6.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(var6.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(var6.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(var6.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(var6.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(var6.nextFloat() * 360.0F + var4 * 90.0F, 0.0F, 0.0F, 1.0F);
            tessellator.getBuffer().begin(6, DefaultVertexFormats.POSITION_COLOR);
//			int i = 0xF00F0;
//	        int j = i >> 16 & 65535;
//	        int k = i & 65535;
            float var8 = var6.nextFloat() * 2.0F + 2.0F + var5 * 0.5F;
            float var9 = var6.nextFloat() * 2.0F + 1.0F + var5 * 2.0F;
            tessellator.getBuffer().pos(0.0D, 0.0D, 0.0D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).endVertex();
            tessellator.getBuffer().pos(-0.866D * var9, var8, -0.5F * var9).color(this.particleRed, this.particleGreen, this.particleBlue, 0).endVertex();
            tessellator.getBuffer().pos(0.866D * var9, var8, -0.5F * var9).color(this.particleRed, this.particleGreen, this.particleBlue, 0).endVertex();
            tessellator.getBuffer().pos(0.0D, var8, 1.0F * var9).color(this.particleRed, this.particleGreen, this.particleBlue, 0).endVertex();
            tessellator.getBuffer().pos(-0.866D * var9, var8, -0.5F * var9).color(this.particleRed, this.particleGreen, this.particleBlue, 0).endVertex();
            tessellator.draw();
        }

        GlStateManager.popMatrix();
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public double getPrevPosX() {
        return prevPosX;
    }

    public double getPrevPosY() {
        return prevPosY;
    }

    public double getPrevPosZ() {
        return prevPosZ;
    }

    public void setPrevPos(double prevPosX, double prevPosY, double prevPosZ) {
        this.prevPosX = prevPosX;
        this.prevPosY = prevPosY;
        this.prevPosZ = prevPosZ;
    }

    public void pushPos() {
        setPrevPos(posX, posY, posZ);
    }

    public World getWorld() {
        return world;
    }

    public void SetParticleTextureByName(String name) {
        if (name.equalsIgnoreCase("radiant")) {
            this.isRadiant = true;
        }
        this.particleTexture = AMParticleIcons.instance.getIconByName(name);
    }

    public void setPosition(Entity entity) {
        setPosition(entity.posX, entity.posY, entity.posZ);
    }

    public boolean isCollided() {
        return canCollide;
    }

    public void addVelocity(double d, double e, double f) {
        this.motionX = d;
        this.motionY = e;
        this.motionZ = f;
    }

}
