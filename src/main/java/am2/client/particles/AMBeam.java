package am2.client.particles;

import am2.ArsMagica;
import am2.api.particles.IBeamParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class AMBeam extends Particle implements IBeamParticle {

    int type;
    double dX, dY, dZ;

    double updateX, updateY, updateZ;

    private float yaw, pitch;
    private float prevYaw, prevPitch;

    private float rotateSpeed = 5f;
//	private final double offset = 0d;

    private float length;
    private final float endMod = 1.0F;
    private int maxLengthAge = 10;

    private boolean positionChanged = false;

    private boolean fppc = false; //first person player cast

    public AMBeam(World world, double x, double y, double z, double destX, double destY, double destZ) {
        this(world, x, y, z, destX, destY, destZ, 0);
    }

    public AMBeam(World world, double x, double y, double z, double destX, double destY, double destZ, int color) {
        super(world, x, y, z);
        this.type = 0;
        this.dX = destX;
        this.dY = destY;
        this.dZ = destZ;

        this.motionX = 0D;
        this.motionY = 0D;
        this.motionZ = 0D;

        setSize(0.2f, 0.2f);

        calculateLengthAndRotation();
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;

        rotateSpeed = 30;
        maxLengthAge = 10;

        this.particleMaxAge = 10;
    }

    /**
     * Sets the beam to be instantly full size instead of playing the grow
     * animation.
     *
     * @return
     */
    public AMBeam setInstantSpawn() {
        this.maxLengthAge = 1;
        return this;
    }

    public void setMaxAge(int maxAge) {
        this.particleMaxAge = maxAge;
    }

    private void calculateLengthAndRotation() {
        float deltaX = (float) (this.posX - this.dX);
        float deltaY = (float) (this.posY - this.dY);
        float deltaZ = (float) (this.posZ - this.dZ);

        this.length = MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        double hDist = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        this.yaw = ((float) (Math.atan2(deltaX, deltaZ) * 180.0D / 3.141592653589793D));
        this.pitch = ((float) (Math.atan2(deltaY, hDist) * 180.0D / 3.141592653589793D));

    }

    public void setBeamLocationAndTarget(double posX, double posY, double posZ, double targetX, double targetY, double targetZ) {
        this.updateX = posX;
        this.updateY = posY;
        this.updateZ = posZ;
        this.dX = targetX;
        this.dY = targetY;
        this.dZ = targetZ;

        if (this.particleAge > this.particleMaxAge - 5) {
            this.particleMaxAge = this.particleAge + 5;
        }

        positionChanged = true;
    }

    private void storePrevInformation() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.positionChanged) {
            this.posX = this.updateX;
            this.posY = this.updateY;
            this.posZ = this.updateZ;
            if (this.fppc) {
                EntityPlayer player = Minecraft.getMinecraft().player;
                if (player != null) {
                    float yaw = player.rotationYaw;
                    float rotationYaw = (float) (yaw * Math.PI / 180);
                    float offsetX = (float) Math.cos(rotationYaw) * 0.06f;
                    float offsetZ = (float) Math.sin(rotationYaw) * 0.06f;
                    this.posX -= offsetX;
                    this.posZ -= offsetZ;
                    this.posY += 0.06f;
                    this.prevPosX = this.posX;
                    this.prevPosY = this.posY;
                    this.prevPosZ = this.posZ;
                }
            }
            this.positionChanged = false;
        }

        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
    }

    @Override
    public void setFirstPersonPlayerCast() {
        this.fppc = true;
    }

    private void handleAging() {
        this.particleAge++;
        if (this.particleAge >= this.particleMaxAge) {
            this.setExpired();
        }
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void onUpdate() {
        storePrevInformation();
        calculateLengthAndRotation();
        handleAging();
    }

    @Override
    public int getFXLayer() {
        return 2;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

        try {
            GL11.glPushMatrix();
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
            float scaleFactor = 1.0F;
            float rot = this.world.provider.getWorldTime() % (360 / this.rotateSpeed) * this.rotateSpeed + this.rotateSpeed * partialTicks;

            float size = (float) this.particleAge / (float) this.maxLengthAge;
            if (size > 1) size = 1;

            float op = 0.4F;
            float widthMod = 1.0f;

            TextureAtlasSprite beamIcon = null;

            switch (this.type) {
                default:
                    beamIcon = AMParticleIcons.instance.getHiddenIconByName("beam");
                    break;
                case 1:
                    beamIcon = AMParticleIcons.instance.getHiddenIconByName("beam1");
                    break;
                case 2:
                    beamIcon = AMParticleIcons.instance.getHiddenIconByName("beam2");
            }

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

            float xx = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
            float yy = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
            float zz = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);
            GL11.glTranslated(xx, yy, zz);

            if (fppc) {
                widthMod = 0.3f;
            }

            float deltaYaw = Math.abs(this.yaw) - Math.abs(this.prevYaw);

            float ry = this.prevYaw + (deltaYaw) * partialTicks;
            float rp = this.prevPitch + (this.pitch - this.prevPitch) * partialTicks;
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(180.0F + ry, 0.0F, 0.0F, -1.0F);
            GL11.glRotatef(rp, 1.0F, 0.0F, 0.0F);

            double offset1 = (-0.15D * widthMod) * size;
            double offset2 = (0.15D * widthMod) * size;
            double offset3 = (-0.15D * widthMod) * size * this.endMod;
            double offset4 = (0.15D * widthMod) * size * this.endMod;

            GL11.glRotatef(rot, 0.0F, 1.0F, 0.0F);
            int i = 5;
            float inc = 36.0F;
            if (ArsMagica.config.LowGFX()) {
                i = 3;
                inc = 90;
            } else if (ArsMagica.config.NoGFX()) {
                i = 1;
                inc = 180;
            }

            Tessellator tessellator = Tessellator.getInstance();
            for (int t = 0; t < i; t++) {
                double l = this.length * size * scaleFactor;
                double tl = beamIcon.getMinU();
                double br = beamIcon.getMaxU();
                double mU = beamIcon.getMaxV();
                double mV = beamIcon.getMinV();

                GL11.glRotatef(inc, 0.0F, 1.0F, 0.0F);

                int b = this.getBrightnessForRender(rotationXZ);
                int j = b >> 16 & 65535;
                int k = b & 65535;

                buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                buffer.pos(offset3, l, 0.0D).tex(br, mV).color(this.particleRed, this.particleGreen, this.particleBlue, op).lightmap(j, k).endVertex();
                buffer.pos(offset1, 0.0D, 0.0D).tex(br, mU).color(this.particleRed, this.particleGreen, this.particleBlue, op).lightmap(j, k).endVertex();
                buffer.pos(offset2, 0.0D, 0.0D).tex(tl, mU).color(this.particleRed, this.particleGreen, this.particleBlue, op).lightmap(j, k).endVertex();
                buffer.pos(offset4, l, 0.0D).tex(tl, mV).color(this.particleRed, this.particleGreen, this.particleBlue, op).lightmap(j, k).endVertex();
                tessellator.draw();
            }

            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();

        } catch (Exception e) {
            am2.ArsMagica.LOGGER.error("Exception caught: ", e);
            GL11.glPopMatrix(); // Ensure we pop the matrix even on error
        }
    }

    @Override
    public void setRGBColorF(float r, float g, float b) {
        this.particleGreen = g;
        this.particleRed = r;
        this.particleBlue = b;
    }

    @Override
    public void setRGBColor(int color) {
        this.particleRed = ((color >> 16) & 0xFF) / 255.0F;
        this.particleGreen = ((color >> 8) & 0xFF) / 255.0F;
        this.particleBlue = (color & 0xFF) / 255.0F;
    }

    @Override
    public void setRGBColorI(int r, int g, int b) {
        this.particleRed = r / 255.0f;
        this.particleGreen = g / 255.0f;
        this.particleBlue = b / 255.0f;
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
}
