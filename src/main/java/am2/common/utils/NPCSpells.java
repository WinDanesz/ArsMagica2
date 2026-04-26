package am2.common.utils;

import am2.api.ArsMagicaAPI;
import am2.api.extensions.ISpellCaster;
import am2.api.spell.SpellPart;
import am2.common.registry.AMItems;
import am2.common.spell.SpellCaster;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.List;

/**
 * NPC Spells - provides pre-built spell ItemStacks for NPC AI.
 * Uses lazy initialization to ensure capabilities are registered before creating spells.
 */
public class NPCSpells {
    private static volatile NPCSpells INSTANCE;

    /**
     * For backwards compatibility with existing code that uses NPCSpells.instance
     *
     * @deprecated Use {@link #getInstance()} instead for lazy initialization
     */
    @Deprecated
    public static NPCSpells instance;

    /**
     * Get the NPCSpells instance, creating it if necessary.
     * This uses lazy initialization so it can be called at any time after
     * capability registration (which happens in preInit).
     */
    public static NPCSpells getInstance() {
        if (INSTANCE == null) {
            synchronized (NPCSpells.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NPCSpells();
                    instance = INSTANCE; // For backwards compatibility
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Initialize NPCSpells. Can be called explicitly but getInstance() will also initialize.
     *
     * @deprecated Use {@link #getInstance()} instead - initialization is automatic
     */
    @Deprecated
    public static void init() {
        getInstance();
    }

    public final ItemStack lightMage_DiminishedAttack;
    public final ItemStack lightMage_NormalAttack;
    public final ItemStack lightMage_AugmentedAttack;

    public final ItemStack darkMage_DiminishedAttack;
    public final ItemStack darkMage_NormalAttack;
    public final ItemStack darkMage_AugmentedAttack;

    public final ItemStack enderGuardian_enderWave;
    public final ItemStack enderGuardian_enderBolt;
    public final ItemStack enderGuardian_enderTorrent;
    public final ItemStack enderGuardian_otherworldlyRoar;

    public final ItemStack dispel;
    public final ItemStack blink;
    public final ItemStack arcaneBolt;
    public final ItemStack meltArmor;
    public final ItemStack waterBolt;
    public final ItemStack fireBolt;
    public final ItemStack healSelf;
    public final ItemStack nauseate;
    public final ItemStack lightningRune;
    public final ItemStack scrambleSynapses;
    public final ItemStack manaLink;
    public final ItemStack lightningBolt;
    public final ItemStack lightningElemental_attack;

    private NPCSpells() {
        lightMage_DiminishedAttack = createSpell(Lists.newArrayList(Projectile(), PhysicalDamage()));
        lightMage_NormalAttack = createSpell(Lists.newArrayList(Projectile(), FrostDamage(), Slow()));
        lightMage_AugmentedAttack = createSpell(Lists.newArrayList(Projectile(), MagicDamage(), Blind(), Damage()));

        darkMage_DiminishedAttack = createSpell(Lists.newArrayList(Projectile(), MagicDamage()));
        darkMage_NormalAttack = createSpell(Lists.newArrayList(Projectile(), FireDamage(), Ignition()));
        darkMage_AugmentedAttack = createSpell(Lists.newArrayList(Projectile(), LightningDamage(), Knockback(), Damage()));

        enderGuardian_enderWave = createSpell(Lists.newArrayList(Wave(), Radius(), Radius(), MagicDamage(), Knockback()));
        enderGuardian_enderBolt = createSpell(Lists.newArrayList(Projectile(), MagicDamage(), RandomTeleport(), Damage()));
        enderGuardian_otherworldlyRoar = createSpell(Lists.newArrayList(AoE(), Blind(), Silence(), Knockback(), Radius(), Radius(), Radius(), Radius(), Radius()));
        enderGuardian_enderTorrent = createSpell(Lists.newArrayList(Projectile(), Silence(), Knockback(), Speed(), AoE(), ManaDrain(), LifeDrain()));

        dispel = createSpell(Lists.newArrayList(Self(), Dispel()));
        blink = createSpell(Lists.newArrayList(Self(), Blink()));
        arcaneBolt = createSpell(Lists.newArrayList(Projectile(), MagicDamage()));
        meltArmor = createSpell(Lists.newArrayList(Projectile(), MeltArmor()));
        waterBolt = createSpell(Lists.newArrayList(Projectile(), WateryGrave(), Drown()));
        fireBolt = createSpell(Lists.newArrayList(Projectile(), FireDamage(), Ignition()));
        healSelf = createSpell(Lists.newArrayList(Self(), Heal()));
        nauseate = createSpell(Lists.newArrayList(Projectile(), Nauseate(), ScrambleSynapses()));
        lightningRune = createSpell(Lists.newArrayList(Projectile(), Rune(), AoE(), LightningDamage(), Damage()));
        scrambleSynapses = createSpell(Lists.newArrayList(Projectile(), LightningDamage(), AoE(), ScrambleSynapses(), Radius(), Radius(), Radius(), Radius(), Radius()));
        manaLink = createSpell(Lists.newArrayList(ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "touch")), ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "mana_link"))));
        lightningBolt = createSpell(Lists.newArrayList(Projectile(), LightningDamage(), Damage()));
        lightningElemental_attack = createSpell(Lists.newArrayList(Projectile(), LightningDamage()));
    }

    public final ItemStack createSpell(List<SpellPart> parts) {
        ItemStack is = new ItemStack(AMItems.spell);
        // Initialize tag compound - required for spell to not be "Malformed"
        if (!is.hasTagCompound()) {
            is.setTagCompound(new NBTTagCompound());
        }

        // Filter out null parts and log warnings
        List<SpellPart> validParts = Lists.newArrayList();
        for (SpellPart part : parts) {
            if (part != null) {
                validParts.add(part);
            }
        }

        ISpellCaster caster = SpellCaster.of(is);
        if (caster != null) {
            List<List<SpellPart>> stages = SpellUtils.transformParts(validParts);
            caster.setSpellCommon(stages);
        }
        return is;
    }

    private SpellPart AoE() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "aoe"));
    }

    private SpellPart FrostDamage() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "frost_damage"));
    }

    private SpellPart MagicDamage() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "magic_damage"));
    }

    private SpellPart Radius() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "radius"));
    }

    private SpellPart PhysicalDamage() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "physical_damage"));
    }

    private SpellPart Projectile() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "projectile"));
    }

    private SpellPart ScrambleSynapses() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "scramble_synapses"));
    }

    private SpellPart Damage() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "damage"));
    }

    private SpellPart LightningDamage() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "lightning_damage"));
    }

    private SpellPart Slow() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "slow"));
    }

    private SpellPart Blind() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "blind"));
    }

    private SpellPart FireDamage() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "fire_damage"));
    }

    private SpellPart Ignition() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "ignition"));
    }

    private SpellPart Knockback() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "knockback"));
    }

    private SpellPart Wave() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "wave"));
    }

    private SpellPart RandomTeleport() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "random_teleport"));
    }

    private SpellPart Silence() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "silence"));
    }

    private SpellPart Speed() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "speed"));
    }

    private SpellPart ManaDrain() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "mana_drain"));
    }

    private SpellPart LifeDrain() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "life_drain"));
    }

    private SpellPart Self() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "self"));
    }

    private SpellPart Dispel() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "dispel"));
    }

    private SpellPart Blink() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "blink"));
    }

    private SpellPart MeltArmor() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "melt_armor"));
    }

    private SpellPart WateryGrave() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "watery_grave"));
    }

    private SpellPart Drown() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "drown"));
    }

    private SpellPart Heal() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "heal"));
    }

    private SpellPart Nauseate() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "nauseate"));
    }

    private SpellPart Rune() {
        return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "rune"));
    }
}
