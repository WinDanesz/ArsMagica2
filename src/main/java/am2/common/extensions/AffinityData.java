package am2.common.extensions;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.extensions.IAffinityData;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import am2.common.registry.Affinities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class AffinityData implements IAffinityData, ICapabilityProvider, ICapabilitySerializable<NBTBase> {
    public static final ResourceLocation ID = new ResourceLocation("arsmagica2:AffinityData");
    public static final String NIGHT_VISION = "night_vision";
    public static final String ICE_BRIDGE_STATE = "ice_bridge";
    public static final float MAX_DEPTH = 100F;
    private static final float ADJACENT_FACTOR = 0.25f;
    private static final float MINOR_OPPOSING_FACTOR = 0.5f;
    private static final float MAJOR_OPPOSING_FACTOR = 0.75f;

    private static final int SYNC_DEPTHS = 0x1;
    private static final int SYNC_ABILITY_BOOLEANS = 0x2;
    private static final int SYNC_ABILITY_FLOATS = 0x4;
    private static final int SYNC_COOLDOWNS = 0x8;
    private static final int SYNC_DIMINISHING_RETURNS = 0x10;

    private ConcurrentHashMap<Affinity, Double> depths;
    private HashMap<String, Boolean> abilityBooleans;
    private HashMap<String, Float> abilityFloats;
    private HashMap<String, Integer> cooldowns;
    private float diminishingReturns = 1.0F;
    public float accumulatedLifeRegen = 0.0f;
    public float accumulatedHungerRegen = 0.0f;

    private int syncCode = 0;

    @CapabilityInject(value = IAffinityData.class)
    public static Capability<IAffinityData> INSTANCE = null;

    public AffinityData() {
        this.depths = new ConcurrentHashMap<>();
        this.abilityBooleans = new HashMap<>();
        this.abilityFloats = new HashMap<>();
        this.cooldowns = new HashMap<>();
    }

    public static AffinityData For(EntityLivingBase living) {
        return (AffinityData) living.getCapability(INSTANCE, null);
    }

    @Override
    public double getAffinityDepth(Affinity aff) {
        Double depth = this.depths.get(aff);
        if (depth == null)
            depth = 0D;
        return depth / MAX_DEPTH;
    }

    @Override
    public void setAffinityDepth(Affinity name, double value) {
        value = MathHelper.clamp(value, 0, MAX_DEPTH);
        if (value != this.getAffinityDepth(name)) {
            this.syncCode |= SYNC_DEPTHS;
            this.depths.put(name, value);
        }
    }

    @Override
    public Map<Affinity, Double> getAffinities() {
        return this.depths;
    }

    @Override
    public void init(EntityPlayer entity) {
        HashMap<Affinity, Double> map = new HashMap<>();
        for (Affinity aff : ArsMagicaAPI.getAffinityRegistry().getValues())
            map.put(aff, 0D);
    }

    @Override
    public boolean getAbilityBoolean(String name) {
        Boolean bool = this.getAbilityBooleanMap().get(name);
        return bool != null && bool;
    }

    @Override
    public void addAbilityBoolean(String name, boolean bool) {
        if (this.getAbilityBoolean(name) != bool) {
            this.abilityBooleans.put(name, bool);
            this.syncCode |= SYNC_ABILITY_BOOLEANS;
        }
    }

    @Override
    public float getAbilityFloat(String name) {
        Float f = this.getAbilityFloatMap().get(name);
        return f == null ? 0f : f;
    }

    @Override
    public void addAbilityFloat(String name, float f) {
        if (this.getAbilityFloat(name) != f) {
            this.abilityFloats.put(name, f);
            this.syncCode |= SYNC_ABILITY_FLOATS;
        }
    }

    @Override
    public Map<String, Boolean> getAbilityBooleanMap() {
        return this.abilityBooleans;
    }

    @Override
    public Map<String, Float> getAbilityFloatMap() {
        return this.abilityFloats;
    }

    @Override
    public void addCooldown(String name, int cooldown) {
        if (this.getCooldown(name) != cooldown) {
            this.cooldowns.put(name, cooldown);
            this.syncCode |= SYNC_COOLDOWNS;
        }
    }

    @Override
    public int getCooldown(String name) {
        return this.cooldowns.getOrDefault(name, 0);
    }

    @Override
    public Map<String, Integer> getCooldowns() {
        return this.cooldowns;
    }

    @Override
    public float getDiminishingReturnsFactor() {
        return this.diminishingReturns;
    }

    @Override
    public void tickDiminishingReturns() {
        if (this.getDiminishingReturnsFactor() < 1.3f) {
            this.diminishingReturns += 0.005f;
            this.syncCode |= SYNC_DIMINISHING_RETURNS;
        }
    }

    @Override
    public void addDiminishingReturns(boolean isChanneled) {
        this.diminishingReturns -= isChanneled ? 0.1f : 0.3f;
        this.syncCode |= SYNC_DIMINISHING_RETURNS;
        if (this.diminishingReturns < 0)
            this.diminishingReturns = 0F;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == INSTANCE)
            return (T) this;
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return new IAffinityData.Storage().writeNBT(INSTANCE, this, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        new IAffinityData.Storage().readNBT(INSTANCE, this, null, nbt);
    }

    @Override
    public Affinity[] getHighestAffinities() {
        double max1 = 0;
        double max2 = 0;
        Affinity maxAff1 = Affinities.none;
        Affinity maxAff2 = Affinities.none;
        // Iterate depths directly – ConcurrentHashMap guarantees no CME even if
        // another thread modifies the map concurrently (e.g. incrementAffinity
        // called server-side while the GUI renders client-side).
        for (Entry<Affinity, Double> entry : this.depths.entrySet()) {
            if (entry.getValue() > max1) {
                max2 = max1;
                maxAff2 = maxAff1;
                max1 = entry.getValue();
                maxAff1 = entry.getKey();
            } else if (entry.getValue() > max2) {
                max2 = entry.getValue();
                maxAff2 = entry.getKey();
            }
        }
        return new Affinity[]{maxAff1, maxAff2};
    }

    @Override
    public byte[] generateUpdatePacket() {
        AMDataWriter writer = new AMDataWriter();
        writer.add(this.syncCode);
        if ((this.syncCode & SYNC_DEPTHS) == SYNC_DEPTHS) {
            writer.add(this.depths.size());
            for (Entry<Affinity, Double> entry : this.depths.entrySet()) {
                writer.add(entry.getKey().getRegistryName().toString());
                writer.add(entry.getValue());
            }
        }
        if ((this.syncCode & SYNC_ABILITY_BOOLEANS) == SYNC_ABILITY_BOOLEANS) {
            writer.add(this.abilityBooleans.size());
            for (Entry<String, Boolean> entry : this.abilityBooleans.entrySet()) {
                writer.add(entry.getKey());
                writer.add(entry.getValue());
            }
        }
        if ((this.syncCode & SYNC_ABILITY_FLOATS) == SYNC_ABILITY_FLOATS) {
            writer.add(this.abilityFloats.size());
            for (Entry<String, Float> entry : this.abilityFloats.entrySet()) {
                writer.add(entry.getKey());
                writer.add(entry.getValue());
            }
        }
        if ((this.syncCode & SYNC_COOLDOWNS) == SYNC_COOLDOWNS) {
            writer.add(this.cooldowns.size());
            for (Entry<String, Integer> entry : this.cooldowns.entrySet()) {
                writer.add(entry.getKey());
                writer.add(entry.getValue());
            }
        }
        if ((this.syncCode & SYNC_DIMINISHING_RETURNS) == SYNC_DIMINISHING_RETURNS)
            writer.add(this.diminishingReturns);
        this.syncCode = 0;
        return writer.generate();
    }

    @Override
    public void handleUpdatePacket(byte[] bytes) {
        AMDataReader reader = new AMDataReader(bytes, false);
        int syncCode = reader.getInt();
        if ((syncCode & SYNC_DEPTHS) == SYNC_DEPTHS) {
            this.depths.clear();
            int size = reader.getInt();
            for (int i = 0; i < size; i++) {
                Affinity key = ArsMagicaAPI.getAffinityRegistry().getValue(new ResourceLocation(reader.getString()));
                double value = reader.getDouble();
                if (key != null)
                    this.depths.put(key, value);
            }
        }
        if ((syncCode & SYNC_ABILITY_BOOLEANS) == SYNC_ABILITY_BOOLEANS) {
            this.abilityBooleans.clear();
            int size = reader.getInt();
            for (int i = 0; i < size; i++) {
                String key = reader.getString();
                boolean value = reader.getBoolean();
                if (key != null)
                    this.abilityBooleans.put(key, value);
            }
        }
        if ((syncCode & SYNC_ABILITY_FLOATS) == SYNC_ABILITY_FLOATS) {
            this.abilityFloats.clear();
            int size = reader.getInt();
            for (int i = 0; i < size; i++) {
                String key = reader.getString();
                float value = reader.getFloat();
                if (key != null)
                    this.abilityFloats.put(key, value);
            }
        }
        if ((syncCode & SYNC_COOLDOWNS) == SYNC_COOLDOWNS) {
            this.cooldowns.clear();
            int size = reader.getInt();
            for (int i = 0; i < size; i++) {
                String key = reader.getString();
                int value = reader.getInt();
                if (key != null)
                    this.cooldowns.put(key, value);
            }
        }
        if ((syncCode & SYNC_DIMINISHING_RETURNS) == SYNC_DIMINISHING_RETURNS)
            this.diminishingReturns = reader.getFloat();
    }

    @Override
    public void incrementAffinity(Affinity affinity, float amt) {
        if (affinity == Affinities.none || this.isLocked()) return;

        float adjacentDecrement = amt * ADJACENT_FACTOR;
        float minorOppositeDecrement = amt * MINOR_OPPOSING_FACTOR;
        float majorOppositeDecrement = amt * MAJOR_OPPOSING_FACTOR;

        this.addToAffinity(affinity, amt);

        if (this.getAffinityDepth(affinity) * MAX_DEPTH == MAX_DEPTH) {
            this.setLocked(true);
        }

        for (Affinity adjacent : affinity.getAdjacentAffinities()) {
            this.subtractFromAffinity(adjacent, adjacentDecrement);
        }

        for (Affinity minorOpposite : affinity.getMinorOpposingAffinities()) {
            this.subtractFromAffinity(minorOpposite, minorOppositeDecrement);
        }

        for (Affinity majorOpposite : affinity.getMajorOpposingAffinities()) {
            this.subtractFromAffinity(majorOpposite, majorOppositeDecrement);
        }

        Affinity directOpposite = affinity.getOpposingAffinity();
        if (directOpposite != null) {
            this.subtractFromAffinity(directOpposite, amt);
        }
    }

    private void addToAffinity(Affinity affinity, float amt) {
        if (affinity == Affinities.none) return;
        double existingAmt = this.getAffinityDepth(affinity) * MAX_DEPTH;
        existingAmt += amt;
        if (existingAmt > MAX_DEPTH) existingAmt = MAX_DEPTH;
        else if (existingAmt < 0) existingAmt = 0;
        this.setAffinityDepth(affinity, existingAmt);
    }

    private void subtractFromAffinity(Affinity affinity, float amt) {
        if (affinity == Affinities.none) return;
        double existingAmt = this.getAffinityDepth(affinity) * MAX_DEPTH;
        existingAmt -= amt;
        if (existingAmt > MAX_DEPTH) existingAmt = MAX_DEPTH;
        else if (existingAmt < 0) existingAmt = 0;
        this.setAffinityDepth(affinity, existingAmt);
    }

    @Override
    public void setLocked(boolean b) {
        this.addAbilityBoolean("affinity_data_locked", b);
    }

    @Override
    public boolean isLocked() {
        return this.getAbilityBoolean("affinity_data_locked");
    }

    @Override
    public boolean shouldUpdate() {
        return this.syncCode != 0;
    }

    @Override
    public void forceUpdate() {
        this.syncCode = 0xFFFFFFFF;
    }
}
