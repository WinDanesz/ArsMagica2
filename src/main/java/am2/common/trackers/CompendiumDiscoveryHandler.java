package am2.common.trackers;

import am2.ArsMagica;
import am2.common.entity.EntityFlyingBook;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.AMSounds;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

/**
 * Handles the Arcane Compendium discovery mechanic:
 * <p>
 * 1. First-time: detects player near Liquid Etherium, triggers flying book animation.
 * 2. Repeat: detects Books dropped into Liquid Etherium and transforms them.
 */
@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public class CompendiumDiscoveryHandler {

	private static final int PROXIMITY_CHECK_INTERVAL = 40; // ticks between proximity scans
	private static final int MESSAGE_COOLDOWN = 400; // 20 seconds between "no book" messages
	private static final double DISCOVERY_RANGE = 10.0;
	private static final int POOL_TRANSFORM_TICKS = 40; // 2 seconds for dropped book transform

	/** Per-player discovery state (server-side only) */
	private static final Map<UUID, DiscoveryState> playerStates = new HashMap<>();

	/** Tracks EntityItem book-in-etherium timers for repeat acquisition (entityId -> ticks) */
	private static final Map<Integer, Integer> bookTransformTimers = new HashMap<>();

	private static final String[] NO_BOOK_MESSAGES = {
			"arsmagica2.compendium.discovery.no_book_1",
			"arsmagica2.compendium.discovery.no_book_2",
			"arsmagica2.compendium.discovery.no_book_3"
	};

	// ---- Public API ----

	/** Called by EntityFlyingBook when a sequence finishes (success or drop). */
	public static void onSequenceComplete(UUID playerUUID) {
		DiscoveryState state = playerStates.get(playerUUID);
		if (state != null) {
			state.sequenceActive = false;
		}
	}

	// ---- Event Handlers ----

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (event.player.world.isRemote) return;

		EntityPlayer player = event.player;
		EntityExtension ext = EntityExtension.For(player);
		if (ext == null) return;

		// Skip if already discovered
		if (ext.hasDiscoveredCompendium()) return;

		DiscoveryState state = playerStates.computeIfAbsent(player.getUniqueID(), k -> new DiscoveryState());

		long currentTick = player.world.getTotalWorldTime();

		// Don't re-trigger while a sequence is active (auto-reset after 700 ticks / 35 seconds as safety)
		if (state.sequenceActive) {
			if (currentTick - state.sequenceStartTick > 700) {
				state.sequenceActive = false;
			} else {
				return;
			}
		}

		// Throttle proximity checks
		if (currentTick - state.lastCheckTick < PROXIMITY_CHECK_INTERVAL) return;
		state.lastCheckTick = currentTick;

		// Scan for nearby Liquid Etherium pool
		double[] poolCenter = findPoolCenter(player.world, player.getPosition(), (int) DISCOVERY_RANGE);
		if (poolCenter == null) {
			// Player left the area — reset the message cooldown so they get it again on re-approach
			state.nearPool = false;
			return;
		}

		// If the player just entered the pool area, reset message cooldown
		if (!state.nearPool) {
			state.nearPool = true;
			state.lastMessageTick = 0;
		}

		// Check if player has a book
		int bookSlot = selectBookSlot(player);

		if (bookSlot >= 0) {
			// Don't start a new sequence if there's already a flying book or dropped book/compendium nearby
			if (hasBookEntityNearby(player.world, player, DISCOVERY_RANGE)) return;

			// Player has a book — start the flying book sequence
			// Send hum message
			player.sendMessage(new TextComponentTranslation("arsmagica2.compendium.discovery.hum"));

			// Consume one book from inventory
			ItemStack bookStack = player.inventory.getStackInSlot(bookSlot);
			bookStack.shrink(1);
			if (bookStack.isEmpty()) {
				player.inventory.setInventorySlotContents(bookSlot, ItemStack.EMPTY);
			}

			// Spawn the flying book entity at the player's position
			double startX = player.posX;
			double startY = player.posY + player.getEyeHeight();
			double startZ = player.posZ;

			// poolCenter = {centerX, surfaceY, centerZ, poolRadius}
			EntityFlyingBook flyingBook = new EntityFlyingBook(player.world,
					startX, startY, startZ,
					poolCenter[0], poolCenter[1], poolCenter[2], (float) poolCenter[3],
					player.getUniqueID());
			player.world.spawnEntity(flyingBook);

			state.sequenceActive = true;
			state.sequenceStartTick = currentTick;
		} else {
			// No book — show ambient message with cooldown
			if (currentTick - state.lastMessageTick < MESSAGE_COOLDOWN) return;
			state.lastMessageTick = currentTick;

			String msgKey = NO_BOOK_MESSAGES[player.world.rand.nextInt(NO_BOOK_MESSAGES.length)];
			player.sendMessage(new TextComponentTranslation(msgKey));
		}
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (event.world.isRemote) return;

		// Only check every 20 ticks
		if (event.world.getTotalWorldTime() % 20 != 0) return;

		World world = event.world;

		// Find all EntityItem entities that are Books
		List<EntityItem> items = world.getEntities(EntityItem.class, e ->
				e != null && !e.isDead && e.getItem().getItem() == Items.BOOK);

		// Clean up timers for dead entities
		bookTransformTimers.entrySet().removeIf(entry -> world.getEntityByID(entry.getKey()) == null);

		for (EntityItem item : items) {
			// Check if the item is mostly still
			if (Math.abs(item.motionX) > 0.05 || Math.abs(item.motionY) > 0.05 || Math.abs(item.motionZ) > 0.05) {
				bookTransformTimers.remove(item.getEntityId());
				continue;
			}

			// Check if the item is inside a liquid_essence block
			BlockPos itemPos = item.getPosition();
			boolean inEtherium = world.getBlockState(itemPos).getBlock() == AMBlocks.liquid_essence.getBlock()
					|| world.getBlockState(itemPos.down()).getBlock() == AMBlocks.liquid_essence.getBlock();

			if (!inEtherium) {
				bookTransformTimers.remove(item.getEntityId());
				continue;
			}

			int timer = bookTransformTimers.getOrDefault(item.getEntityId(), 0);
			timer += 20; // We check every 20 ticks
			bookTransformTimers.put(item.getEntityId(), timer);

			if (timer >= POOL_TRANSFORM_TICKS) {
				// Transform the book
				transformBookInPool(world, item);
				bookTransformTimers.remove(item.getEntityId());
			}
		}
	}

	// ---- Internal Methods ----

	/**
	 * Checks if there is already a flying book entity or a dropped Book/Compendium item entity near the player.
	 */
	private static boolean hasBookEntityNearby(World world, EntityPlayer player, double range) {
		AxisAlignedBB area = new AxisAlignedBB(
				player.posX - range, player.posY - range, player.posZ - range,
				player.posX + range, player.posY + range, player.posZ + range);

		// Check for existing flying book entities
		if (!world.getEntitiesWithinAABB(EntityFlyingBook.class, area).isEmpty()) {
			return true;
		}

		// Check for dropped Arcane Compendium item entities (output of a completed sequence)
		List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, area);
		for (EntityItem item : items) {
			if (item.isDead) continue;
			if (item.getItem().getItem() == AMItems.arcane_compendium) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Scans for the nearest Liquid Etherium pool within the given radius.
	 * Returns a double[4] = {centerX, surfaceY, centerZ, poolRadius} or null if no pool found.
	 * The center is the average of all connected etherium block positions at the same Y level
	 * as the nearest etherium block found.
	 */
	private static double[] findPoolCenter(World world, BlockPos center, int radius) {
		// Step 1: find nearest etherium block
		BlockPos nearest = null;
		double nearestDistSq = Double.MAX_VALUE;
		int radiusSq = radius * radius;

		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				for (int y = -radius; y <= radius; y++) {
					if (x * x + y * y + z * z > radiusSq) continue;

					BlockPos pos = center.add(x, y, z);
					if (world.getBlockState(pos).getBlock() == AMBlocks.liquid_essence.getBlock()) {
						double distSq = center.distanceSq(pos);
						if (distSq < nearestDistSq) {
							nearestDistSq = distSq;
							nearest = pos;
						}
					}
				}
			}
		}
		if (nearest == null) return null;

		// Step 2: flood-fill all connected etherium blocks at the surface Y level (and one above/below)
		int surfaceY = nearest.getY();
		List<BlockPos> poolBlocks = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		Queue<BlockPos> queue = new LinkedList<>();
		queue.add(nearest);
		visited.add(nearest);

		while (!queue.isEmpty()) {
			BlockPos current = queue.poll();
			if (world.getBlockState(current).getBlock() == AMBlocks.liquid_essence.getBlock()) {
				poolBlocks.add(current);
				// Check horizontal neighbors and one Y up/down
				for (int dx = -1; dx <= 1; dx++) {
					for (int dz = -1; dz <= 1; dz++) {
						for (int dy = -1; dy <= 1; dy++) {
							BlockPos neighbor = current.add(dx, dy, dz);
							if (!visited.contains(neighbor) && Math.abs(neighbor.getY() - surfaceY) <= 1) {
								visited.add(neighbor);
								queue.add(neighbor);
							}
						}
					}
				}
			}
		}

		// Step 3: compute center of pool
		double sumX = 0, sumZ = 0;
		int topY = surfaceY;
		for (BlockPos bp : poolBlocks) {
			sumX += bp.getX() + 0.5;
			sumZ += bp.getZ() + 0.5;
			if (bp.getY() > topY) topY = bp.getY();
		}
		double cx = sumX / poolBlocks.size();
		double cz = sumZ / poolBlocks.size();

		// Pool radius: max distance from center to any pool block edge
		double maxDist = 0;
		for (BlockPos bp : poolBlocks) {
			double d = Math.sqrt(Math.pow(bp.getX() + 0.5 - cx, 2) + Math.pow(bp.getZ() + 0.5 - cz, 2));
			if (d > maxDist) maxDist = d;
		}
		// Add 0.5 for the block edge
		double poolRadius = maxDist + 0.5;

		return new double[]{cx, topY + 1.0, cz, poolRadius};
	}

	/**
	 * Selects a Book from the player's inventory with priority:
	 * 1. Current held slot if it's a Book
	 * 2. Any hotbar slot (0-8)
	 * 3. Any main inventory slot (9-35)
	 *
	 * @return slot index, or -1 if no book found
	 */
	private static int selectBookSlot(EntityPlayer player) {
		// Priority 1: Currently held item
		int heldSlot = player.inventory.currentItem;
		ItemStack held = player.inventory.getStackInSlot(heldSlot);
		if (!held.isEmpty() && held.getItem() == Items.BOOK) {
			return heldSlot;
		}

		// Priority 2: Any hotbar slot
		for (int i = 0; i < 9; i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (!stack.isEmpty() && stack.getItem() == Items.BOOK) {
				return i;
			}
		}

		// Priority 3: Main inventory
		for (int i = 9; i < 36; i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (!stack.isEmpty() && stack.getItem() == Items.BOOK) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Transforms a Book EntityItem in a Liquid Etherium pool into an Arcane Compendium.
	 */
	private static void transformBookInPool(World world, EntityItem bookItem) {
		ItemStack bookStack = bookItem.getItem();
		int count = bookStack.getCount();

		// Replace one book with a compendium
		if (count > 1) {
			bookStack.shrink(1);
			bookItem.setItem(bookStack);
		} else {
			bookItem.setDead();
		}

		// Spawn the Arcane Compendium
		EntityItem compendiumItem = new EntityItem(world,
				bookItem.posX, bookItem.posY + 0.5, bookItem.posZ,
				new ItemStack(AMItems.arcane_compendium));
		compendiumItem.motionX = 0;
		compendiumItem.motionY = 0.1;
		compendiumItem.motionZ = 0;
		compendiumItem.setPickupDelay(10);
		world.spawnEntity(compendiumItem);

		// Play short effect
		world.playSound(null, bookItem.posX, bookItem.posY, bookItem.posZ,
				AMSounds.CAST_ARCANE, SoundCategory.AMBIENT, 1.0f, 1.2f);
	}

	// ---- Cleanup for server shutdown / dimension unload ----

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		playerStates.remove(event.player.getUniqueID());
	}

	public static void clearStates() {
		playerStates.clear();
		bookTransformTimers.clear();
	}

	// ---- Per-Player State ----

	private static class DiscoveryState {
		long lastCheckTick;
		long lastMessageTick;
		boolean sequenceActive;
		long sequenceStartTick;
		boolean nearPool;
	}
}
