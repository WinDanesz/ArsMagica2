package am2.common.spell.shape;

import am2.api.affinity.Affinity;
import am2.api.extensions.IEntityExtension;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMItems;
import am2.common.registry.AMSounds;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.EnumSet;
import java.util.UUID;

public class Glyph extends SpellShape {

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        IEntityExtension ext = EntityExtension.For(caster);

        if (caster.isSneaking()) {
            // PLACE: store glyph position and the remaining spell chain (the payload).
            // Mana is already consumed by SpellCaster.cast() before this is called.
            if (!world.isRemote) {
                RayTraceResult mop = spell.raytrace(caster, world, 8.0f, true, false);
                if (mop == null) {
                    return SpellCastResult.EFFECT_FAILED;
                }
                if (mop.typeOfHit == RayTraceResult.Type.ENTITY && mop.entityHit instanceof EntityLivingBase) {
                    // Attach glyph to a creature
                    EntityLivingBase entityTarget = (EntityLivingBase) mop.entityHit;
                    ext.setGlyph(entityTarget.posX, entityTarget.posY, entityTarget.posZ);
                    ext.setGlyphEntity(entityTarget.getUniqueID());
                    ext.setGlyphSpell(spell.hasMoreStages() ? spell.writeToNBT(new NBTTagCompound()) : null);
                    if (caster instanceof EntityPlayer) {
                        ((EntityPlayer) caster).sendStatusMessage(
                            new TextComponentTranslation("am2.glyph.stored.entity", entityTarget.getDisplayName().getFormattedText()), true);
                    }
                } else if (mop.typeOfHit == RayTraceResult.Type.BLOCK) {
                    Vec3d hitVec = mop.hitVec;
                    ext.setGlyph(hitVec.x, hitVec.y + 0.5, hitVec.z);
                    ext.setGlyphEntity(null);
                    // Store the follow-up stages as payload; null payload is valid (detonation will fail gracefully)
                    ext.setGlyphSpell(spell.hasMoreStages() ? spell.writeToNBT(new NBTTagCompound()) : null);
                    if (caster instanceof EntityPlayer) {
                        ((EntityPlayer) caster).sendStatusMessage(new TextComponentTranslation("am2.glyph.stored"), true);
                    }
                } else {
                    return SpellCastResult.EFFECT_FAILED;
                }
            }
            return SpellCastResult.SUCCESS;
        } else {
            // DETONATE: always free (mana not deducted regardless of which spell triggered this)
            if (!world.isRemote) {
                if (!ext.hasGlyph()) {
                    if (caster instanceof EntityPlayer) {
                        ((EntityPlayer) caster).sendStatusMessage(new TextComponentTranslation("am2.glyph.none"), true);
                    }
                    return SpellCastResult.EFFECT_FAILED;
                }
                double gx = ext.getGlyphX();
                double gy = ext.getGlyphY();
                double gz = ext.getGlyphZ();
                UUID entityId = ext.getGlyphEntityId();
                NBTTagCompound storedSpellTag = ext.getGlyphSpell();
                ext.clearGlyph();
                if (storedSpellTag == null) {
                    return SpellCastResult.EFFECT_FAILED;
                }
                SpellData storedSpell = SpellData.readFromNBT(storedSpellTag);
                // If glyph was placed on a creature, resolve its current position and use it as target
                if (entityId != null && world instanceof WorldServer) {
                    Entity attachedEntity = ((WorldServer) world).getEntityFromUuid(entityId);
                    if (attachedEntity instanceof EntityLivingBase) {
                        EntityLivingBase entityTarget = (EntityLivingBase) attachedEntity;
                        gx = entityTarget.posX;
                        gy = entityTarget.posY;
                        gz = entityTarget.posZ;
                        SpellCastResult detonResult = storedSpell.execute(world, caster, entityTarget, gx, gy, gz, EnumFacing.UP);
                        return detonResult == SpellCastResult.SUCCESS ? SpellCastResult.FREE_CAST : detonResult;
                    }
                    // Entity not found (dead/unloaded) — fall through to stored position
                }
                SpellCastResult detonResult = storedSpell.execute(world, caster, target, gx, gy, gz, EnumFacing.UP);
                return detonResult == SpellCastResult.SUCCESS ? SpellCastResult.FREE_CAST : detonResult;
            }
            return SpellCastResult.FREE_CAST;
        }
    }



    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.WHITE.getDyeDamage()),
                new ItemStack(AMItems.rune, 1, EnumDyeColor.BLACK.getDyeDamage()),
                new ItemStack(AMItems.chimerite),
                Items.FEATHER,
                Items.ENDER_EYE
        };
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

    @Override
    public float manaCostMultiplier() {
        return 1.25f;
    }

    @Override
    public boolean isTerminusShape() {
        // Allow bare [Glyph] spells (detonator with no follow-up stages)
        return true;
    }

    @Override
    public boolean isPrincipumShape() {
        return true;
    }

    @Override
    public SoundEvent getSoundForAffinity(Affinity affinity, SpellData stack, World world) {
        return AMSounds.RUNE_CAST;
    }
}
