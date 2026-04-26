package am2.common.spell.shape;

import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.extensions.EntityExtension;
import am2.common.items.ItemSpellBase;
import am2.common.registry.AMItems;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.UUID;

public class Toggle extends SpellShape {



    @Override
    public float manaCostMultiplier() {
        return 0.7f;
    }

    @Override
    public boolean isTerminusShape() {
        return false;
    }

    @Override
    public boolean isPrincipumShape() {
        return true;
    }

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        UUID current = spell.getUUID();
        EntityExtension ext = (EntityExtension) EntityExtension.For(caster);
        ArrayList<SpellData> rs = ext.runningStacks;
        int foundID = -1;
        for (int i = 0; i < rs.size(); i++) {
            SpellData data = rs.get(i);
            if (data != null && data.getUUID() == current) {
                foundID = i;
                break;
            }
        }
        if (foundID != -1) {
            ext.runningStacks.remove(foundID);
            if (caster instanceof EntityPlayer) {
                InventoryPlayer inv = ((EntityPlayer) caster).inventory;
                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    ItemStack is = inv.getStackInSlot(i);
                    //FIXME
                    if (!is.isEmpty() && is.getItem() instanceof ItemSpellBase && is.getTagCompound() != null && is.getTagCompound().getString("ToggleShapeID").equals(current.toString())) {
                        is.getTagCompound().setBoolean("HasEffect", false);
                    }
                }
            }
        } else {
            ext.runningStacks.add(spell.copy());
            if (caster instanceof EntityPlayer) {
                InventoryPlayer inv = ((EntityPlayer) caster).inventory;
                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    ItemStack is = inv.getStackInSlot(i);
                    //FIXME
                    if (!is.isEmpty() && is.getItem() instanceof ItemSpellBase && is.getTagCompound() != null && is.getTagCompound().getString("ToggleShapeID").equals(current.toString())) {
                        is.getTagCompound().setBoolean("HasEffect", true);
                    }
                }
            }
        }
        return SpellCastResult.SUCCESS;
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(Blocks.LEVER),
                new ItemStack(AMItems.purified_vinteum_dust),
                new ItemStack(AMItems.greater_focus)
        };
    }

    @Override
    public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {
        tag.setString("ToggleShapeID", UUID.randomUUID().toString());
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }
}
