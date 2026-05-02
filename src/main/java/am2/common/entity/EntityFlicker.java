package am2.common.entity;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.event.FlickerAffinityEvent;
import am2.api.math.AMVector3;
import am2.client.particles.AMParticle;
import am2.client.particles.AMParticleDefs;
import am2.client.particles.ParticleFadeOut;
import am2.client.particles.ParticleMoveOnHeading;
import am2.common.armor.ArmorHelper;
import am2.common.items.ItemFlickerJar;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.registry.ImbuementRegistry;
import am2.common.utils.InventoryUtilities;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Collection;


public class EntityFlicker extends EntityAmbientCreature {

    private static final DataParameter<Integer> WATCHER_FLICKERTYPE = EntityDataManager.createKey(EntityFlicker.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> WATCHER_AMBIENTFLICK = EntityDataManager.createKey(EntityFlicker.class, DataSerializers.BOOLEAN);

    private static final int DIRECTION_CHANGE_TIME = 200;

    private AMVector3 targetPosition = null;
    private AMVector3 normalizedMovementVector = AMVector3.zero();

    private int flickCount = 0;

    public EntityFlicker(World par1World) {
        super(par1World);
        this.setSize(0.5f, 0.5f);
        int affinity_length = GetAffinities().toArray().length;
        setFlickerType(getRNG().nextInt(affinity_length));
    }

    public static Collection<Affinity> GetAffinities() {
        return ArsMagicaAPI.getAffinityRegistry().getValuesCollection();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(WATCHER_FLICKERTYPE, 0);
        this.dataManager.register(WATCHER_AMBIENTFLICK, false);
    }

    @Override
    public void setDead() {
        ArsMagica.proxy.decrementFlickerCount();
        super.setDead();
    }

    public void setFlickerType(int affinity) {
        this.dataManager.set(WATCHER_FLICKERTYPE, affinity);
    }

    public Affinity getFlickerAffinity() {
        return ArsMagicaAPI.getAffinityRegistry().getValuesCollection().toArray(new Affinity[0])[dataManager.get(WATCHER_FLICKERTYPE)];
    }

    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
        if (this.world.isRemote)
            return false;
        flick();
        return !par1DamageSource.isUnblockable();
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    public void fall(float par1, float par2) {
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean canTriggerWalking() {
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
        ArsMagica.proxy.incrementFlickerCount();
        super.readFromNBT(par1nbtTagCompound);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;

        long time = world.getWorldTime() % 24000;
        if (!world.isRemote && time >= 18500 && time <= 18600) {
            //this.dataManager.set(WATCHER_AMBIENTFLICK, (byte)1);
            this.setDead();
            return;
        }

        boolean playerClose = false;
        AMVector3 me = new AMVector3(this);
        if (!world.isRemote) {
            for (Object o : world.playerEntities) {
                if (me.distanceSqTo(new AMVector3((EntityPlayer) o)) < 25) {

                    ItemStack chestArmor = ((EntityPlayer) o).getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                    if (chestArmor.isEmpty() || !ArmorHelper.isInfusionPreset(chestArmor, ImbuementRegistry.FLICKER_LURE))
                        playerClose = true;
                    break;
                }
            }
        }

        if (this.ticksExisted > 100 && playerClose && !this.dataManager.get(WATCHER_AMBIENTFLICK)) {
            if (this.getActivePotionEffects().isEmpty() || (this.getActivePotionEffects().size() == 1 && world.rand.nextDouble() < 0.1f))
                this.dataManager.set(WATCHER_AMBIENTFLICK, true);
        } else if (this.dataManager.get(WATCHER_AMBIENTFLICK)) {
            flickCount++;
            if (world.isRemote && flickCount > 7)
                flick(); //client flick
            else if (!world.isRemote && flickCount > 10)
                flick(); //server flick
        }

        if (world.isRemote) {
            //for (int i = 0; i < + 1; ++i){
            if (getRNG().nextInt(10) < ArsMagica.config.getGFXLevel()) {
                AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, AMParticleDefs.getParticleForAffinity(getFlickerAffinity()), posX, posY, posZ);
                if (effect != null) {
                    effect.addRandomOffset(this.width, this.height, this.width);
                    effect.setDontRequireControllers();
                    effect.setMaxAge(10);
                    if (getFlickerAffinity() == Affinities.earth)
                        effect.setParticleScale(0.01f + rand.nextFloat() * 0.05f);
                    else
                        effect.setParticleScale(0.05f + rand.nextFloat() * 0.05f);
                }
            }
        }
    }


    @Override
    protected void updateAITasks() {
        super.updateAITasks();

        AMVector3 me = new AMVector3(this);

        boolean needsNewPath = targetPosition == null || this.ticksExisted % DIRECTION_CHANGE_TIME == 0;
        if (needsNewPath && world.rand.nextDouble() < 0.1f)
            pickNewTargetPosition();

        if (targetPosition == null) //this represents the pause state in between picking new waypoints
            return;

        if (me.distanceSqTo(targetPosition) > 400f) {
            targetPosition = null;
            return;
        }

        if (me.distanceSqTo(targetPosition) < 1f) {
            targetPosition = null;
            return;
        }

        this.rotationYaw = AMVector3.anglePreNorm(me, targetPosition);

        normalizedMovementVector = me.copy().sub(targetPosition).normalize();

        if (normalizedMovementVector.y > 0)
            rotatePitchTowards(-70 * normalizedMovementVector.y, 30);
        else
            rotatePitchTowards(0, 30);

        float speed = 0.2f;
        this.addVelocity(-normalizedMovementVector.x * speed, -normalizedMovementVector.y * speed, -normalizedMovementVector.z * speed);
    }

    public AMVector3 getNormalizedMovement() {
        return this.normalizedMovementVector;
    }

    private void rotatePitchTowards(float p, float step) {
        if (this.rotationPitch != p) {
            if (step > 0 && this.rotationPitch + step > p) {
                step = p - this.rotationPitch;
            } else if (step < 0 && this.rotationPitch + step < p) {
                step = p - this.rotationPitch;
            }
            this.rotationPitch += step;
        }
    }

    private void flick() {
        if (this.world.isRemote) {
            for (int i = 0; i < 10 * ArsMagica.config.getGFXLevel(); ++i) {
                AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "radiant", posX, posY, posZ);
                if (particle != null) {
                    particle.AddParticleController(
                            new ParticleMoveOnHeading(
                                    particle,
                                    world.rand.nextDouble() * 360,
                                    world.rand.nextDouble() * 360,
                                    world.rand.nextDouble() * 0.3f + 0.01f,
                                    1,
                                    false));
                    particle.setRGBColorI(getFlickerAffinity().getColor());
                    particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed((float) (world.rand.nextDouble() * 0.1 + 0.05)).setKillParticleOnFinish(true));
                    particle.setIgnoreMaxAge(true);
                    particle.setParticleScale(0.1f);
                }
            }
        } else {
            this.setDead();
        }
    }

    private void pickNewTargetPosition() {
        int groundLevel = 0;
        Affinity aff = this.getFlickerAffinity();
        for (int x = -5; x <= 5; x++) {
            for (int y = 0; y <= 5; y++) {
                for (int z = 0; z <= 5; z++) {
                    if (world.getBlockState(getPosition().add(x, y, z)).getBlock() == AMBlocks.flicker_lure) {
                        BlockPos pos = getPosition().add(x, y, z);
                        groundLevel = getTopBlockNearMe();
                        targetPosition = new AMVector3(pos.getX() + world.rand.nextInt(5) - 2, groundLevel + world.rand.nextInt(3), pos.getY() + world.rand.nextInt(5) - 2);
                        return;
                    }
                }
            }
        }
        if (aff == Affinities.water) {
            for (int i = 0; i < 5; ++i) {
                targetPosition = new AMVector3(this.posX - 5 + world.rand.nextInt(10), this.posY - 5 + world.rand.nextInt(10), this.posZ - 5 + world.rand.nextInt(10));
                Block block = world.getBlockState(targetPosition.toBlockPos()).getBlock();
                if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
                    break;
                }
            }
        } else if (aff == Affinities.air) {
            groundLevel = getTopBlockNearMe();
            targetPosition = new AMVector3(this.posX - 5 + world.rand.nextInt(10), groundLevel + 10 + world.rand.nextInt(15), this.posZ - 5 + world.rand.nextInt(10));
        } else if (aff == Affinities.earth) {
            groundLevel = getTopBlockNearMe();
            targetPosition = new AMVector3(this.posX - 5 + world.rand.nextInt(10), groundLevel + world.rand.nextInt(1) + 1, this.posZ - 5 + world.rand.nextInt(10));
        } else {
            groundLevel = getTopBlockNearMe();
            targetPosition = new AMVector3(this.posX - 5 + world.rand.nextInt(10), groundLevel + 3 + world.rand.nextInt(5), this.posZ - 5 + world.rand.nextInt(10));
        }
    }

    private int getTopBlockNearMe() {

        BlockPos checkPos = this.getPosition();

        while (checkPos.getY() > 0 && world.isAirBlock(checkPos))
            checkPos = checkPos.down();
        while (checkPos.getY() < world.getActualHeight() && !world.isAirBlock(checkPos))
            checkPos = checkPos.up();

        return checkPos.getY();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound par1nbtTagCompound) {
        super.writeEntityToNBT(par1nbtTagCompound);
        par1nbtTagCompound.setInteger("flickerType", dataManager.get(WATCHER_FLICKERTYPE));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
        super.readEntityFromNBT(par1nbtTagCompound);
        dataManager.set(WATCHER_FLICKERTYPE, par1nbtTagCompound.getInteger("flickerType"));
        ArsMagica.proxy.incrementFlickerCount();
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (player.getHeldItem(hand).getItem() == AMItems.flicker_jar && !this.isDead) {
            if (player.getHeldItem(hand).getItemDamage() == 0) {
                if (!world.isRemote) {
                    setDead();
                    InventoryUtilities.decrementStackQuantity(player.inventory, player.inventory.currentItem, 1);
                    ItemStack newStack = new ItemStack(AMItems.flicker_jar);
                    ((ItemFlickerJar) AMItems.flicker_jar).setFlickerJarTypeFromFlicker(newStack, this);
                    if (!InventoryUtilities.mergeIntoInventory(player.inventory, newStack)) {
                        player.dropItem(newStack, false);
                    }
                }
                return EnumActionResult.SUCCESS;
            } else {
                flick();
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    public boolean getCanSpawnHere() {
        if (ArsMagica.proxy.getTotalFlickerCount() > ArsMagica.config.GetFlickerMaxPerPlayer() * world.playerEntities.size() || world.rand.nextDouble() > ArsMagica.config.GetFlickerSpawnChance() / 100.0) {
            return false;
        }
        //get the biome we're trying to spawn in
        Biome biome = this.getEntityWorld().getBiome(this.getPosition());
        if (biome != null) {
            //get the tags on this biome
            Type[] biomeTags = BiomeDictionary.getTypes(biome).toArray(new Type[0]);
            //pick a random tag to focus on
            Type tagType = biomeTags[world.rand.nextInt(biomeTags.length)];
            //create a list of valid types based on that tag
            ArrayList<Integer> validAffinities = new ArrayList<>();
            //populate the list
            //DO NOT USE THIS LIST FOR AIR/EARTH/LIFE - they are handled by special cases.
            switch (tagType.getName()) {
                case "DEAD":
                case "END":
                    validAffinities.add(Affinities.ender.getID());
                    break;
                case "PLAINS":
                case "FOREST":
                case "CONIFEROUS":
                case "JUNGLE":
                    validAffinities.add(Affinities.nature.getID());
                    break;
                case "COLD":
                case "SNOWY":
                    validAffinities.add(Affinities.ice.getID());
                    break;
                case "MAGICAL":
                    validAffinities.add(Affinities.arcane.getID());
                    break;
                case "DRY":
                case "HOT":
                case "SAVANNA":
                case "NETHER":
                    validAffinities.add(Affinities.fire.getID());
                    break;
                case "OCEAN":
                    validAffinities.add(Affinities.life.getID());
                case "SWAMP":
                case "WATER":
                case "RIVER":
                case "WET":
                case "BEACH":
                    validAffinities.add(Affinities.water.getID());
                    break;
                case "DENSE":
                case "LUSH":
                case "MESA":
                case "SANDY":
                case "SPARSE":
                case "SPOOKY":
                case "WASTELAND":
                case "HILLS":
                case "MOUNTAIN":
                case "MUSHROOM":
                default:
                    break;
            }

            //special conditions for air/earth flickers based on y coordinate
            if (posY < 55) {
                validAffinities.add(Affinities.earth.getID());
            }

            if (world.canBlockSeeSky(getPosition()))
                validAffinities.add(Affinities.air.getID());

            if (world.isRaining() && world.rand.nextBoolean()) {
                validAffinities.clear();
                validAffinities.add(Affinities.lightning.getID());
            }

            if (validAffinities.size() <= 0)
                return false;

            if (world.provider.getDimension() == 1) {
                validAffinities.clear();
                validAffinities.add(Affinities.ender.getID());
            }

            //life flickers always have a chance to spawn?
            MinecraftForge.EVENT_BUS.post(new FlickerAffinityEvent(validAffinities, this, biome));
            if (world.provider.getDimension() != -1 && world.rand.nextDouble() < 0.1f) {
                this.setFlickerType(Affinities.life.getID());
            } else {
                this.setFlickerType(validAffinities.get(world.rand.nextInt(validAffinities.size())));
            }

            if (this.world.checkNoEntityCollision(this.getEntityBoundingBox()) && this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this)) {
                ArsMagica.proxy.incrementFlickerCount();
                return true;
            }
        }
        return false;
    }
}
