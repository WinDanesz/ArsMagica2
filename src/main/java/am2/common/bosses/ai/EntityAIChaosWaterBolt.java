package am2.common.bosses.ai;

import am2.api.ArsMagicaAPI;
import am2.api.extensions.ISpellCaster;
import am2.api.spell.SpellPart;
import am2.common.bosses.BossActions;
import am2.common.bosses.EntityWaterGuardian;
import am2.common.spell.SpellCaster;
import am2.common.utils.NPCSpells;
import com.google.common.collect.Lists;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class EntityAIChaosWaterBolt extends EntityAIBase {
    private final EntityWaterGuardian host;
    private static ItemStack castStack;

    private static SpellPart WateryGrave() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "watery_grave"));
    }

    private static SpellPart Projectile() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "projectile"));
    }

    private static SpellPart MagicDamage() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "magic_damage"));
    }

    private static SpellPart Knockback() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "knockback"));
    }

    private static ItemStack getCastStack() {
        if (castStack.isEmpty()) {
            castStack = NPCSpells.getInstance().createSpell(Lists.newArrayList(Projectile(), WateryGrave(), MagicDamage(), Knockback()));
        }
        return castStack;
    }

    public EntityAIChaosWaterBolt(EntityWaterGuardian host) {
        this.host = host;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        return host.getCurrentAction() == BossActions.IDLE && host.isActionValid(BossActions.CASTING);
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (host.getCurrentAction() == BossActions.CASTING && host.getTicksInCurrentAction() > 100) {
            host.setCurrentAction(BossActions.IDLE);
            return false;
        }
        return true;
    }

    @Override
    public void updateTask() {
        if (host.getCurrentAction() != BossActions.CASTING)
            host.setCurrentAction(BossActions.CASTING);

        if (!host.world.isRemote && host.getCurrentAction() == BossActions.CASTING) {
            float yaw = host.world.rand.nextFloat() * 360;
            host.rotationYaw = yaw;
            host.prevRotationYaw = yaw;
            ItemStack stack = getCastStack();
            ISpellCaster spell = SpellCaster.of(stack);
            if (spell != null) {
                spell.cast(stack, host.world, host);
            }
        }
    }
}
