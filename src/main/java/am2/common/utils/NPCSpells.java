package am2.common.utils;

import java.util.List;

import com.google.common.collect.Lists;

import am2.api.ArsMagicaAPI;
import am2.api.extensions.ISpellCaster;
import am2.api.spell.AbstractSpellPart;
import am2.common.defs.ItemDefs;
import am2.common.spell.SpellCaster;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class NPCSpells{
	public static final NPCSpells instance = new NPCSpells();

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

	private NPCSpells(){
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
	}
	
	public final ItemStack createSpell(List<AbstractSpellPart> parts) {
		ItemStack is = new ItemStack(ItemDefs.spell);
		ISpellCaster caster = is.getCapability(SpellCaster.INSTANCE, null);
		if (caster != null) {
			caster.setSpellCommon(SpellUtils.transformParts(parts));
		}
		return is;
	}
	
	private AbstractSpellPart AoE() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "aoe"));}
	private AbstractSpellPart FrostDamage() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "frost_damage"));}
	private AbstractSpellPart MagicDamage() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "magic_damage"));}
	private AbstractSpellPart Radius() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "radius"));}
	private AbstractSpellPart PhysicalDamage() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "physical_damage"));}
	private AbstractSpellPart Projectile() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "projectile"));}
	private AbstractSpellPart ScrambleSynapses() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "scramble_synapses"));}
	private AbstractSpellPart Damage() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "damage"));}
	private AbstractSpellPart LightningDamage() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "lightning_damage"));}
	private AbstractSpellPart Slow() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "slow"));}
	private AbstractSpellPart Blind() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "blind"));}
	private AbstractSpellPart FireDamage() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "fire_damage"));}
	private AbstractSpellPart Ignition() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "ignition"));}
	private AbstractSpellPart Knockback() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "knockback"));}
	private AbstractSpellPart Wave() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "wave"));}
	private AbstractSpellPart RandomTeleport() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "random_teleport"));}
	private AbstractSpellPart Silence() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "silence"));}
	private AbstractSpellPart Speed() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "speed"));}
	private AbstractSpellPart ManaDrain() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "mana_drain"));}
	private AbstractSpellPart LifeDrain() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "life_drain"));}
	private AbstractSpellPart Self() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "self"));}
	private AbstractSpellPart Dispel() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "dispel"));}
	private AbstractSpellPart Blink() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "blink"));}
	private AbstractSpellPart MeltArmor() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "melt_armor"));}
	private AbstractSpellPart WateryGrave() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "watery_grave"));}
	private AbstractSpellPart Drown() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "drown"));}
	private AbstractSpellPart Heal() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "heal"));}
	private AbstractSpellPart Nauseate() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "nauseate"));}
	private AbstractSpellPart Rune() {return ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "rune"));}
}
