package am2.common.defs;

import am2.api.extensions.ISpellCaster;
import am2.common.extensions.AffinityData;
import am2.common.items.ItemSpellBook;
import am2.common.registry.AMItems;
import am2.common.spell.SpellCaster;
import am2.network.AMNetworkHandler;
import am2.network.packets.PacketAbilityToggle;
import am2.network.packets.PacketCharmCast;
import am2.network.packets.PacketSpellShapeGroupChange;
import am2.network.packets.PacketSpellbookSlotChange;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class Keybindings {
    public static final KeyBinding ICE_BRIDGE = new KeyBinding("key.am2.bridge", Keyboard.KEY_I, "keybindings.am2");
    public static final KeyBinding ENDER_TP = new KeyBinding("key.am2.teleport", Keyboard.KEY_N, "keybindings.am2");
    public static final KeyBinding SHAPE_GROUP = new KeyBinding("key.am2.shape_groups", Keyboard.KEY_C, "keybindings.am2");
    //public static final KeyBinding AURA_CUSTOMIZATION = new KeyBinding("key.am2.aura_customization", Keyboard.KEY_B, "keybindings.am2");
    public static final KeyBinding NIGHT_VISION = new KeyBinding("key.am2.dark_vision", Keyboard.KEY_L, "keybindings.am2");
    public static final KeyBinding WALL_CLIMB = new KeyBinding("key.am2.wall_climb", Keyboard.KEY_K, "keybindings.am2");
    public static final KeyBinding SPELL_BOOK_PREV = new KeyBinding("key.am2.spellbookprev", Keyboard.KEY_Z, "keybindings.am2");
    public static final KeyBinding SPELL_BOOK_NEXT = new KeyBinding("key.am2.spellbooknext", Keyboard.KEY_X, "keybindings.am2");
    public static final KeyBinding CHARM_CAST = new KeyBinding("key.am2.charmcast", Keyboard.KEY_V, "keybindings.am2");

    /** True while the charm cast key is held — used for channeled spell support. */
    private boolean charmCasting = false;

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        EntityPlayer clientPlayer = FMLClientHandler.instance().getClient().player;

        if (NIGHT_VISION.isPressed())
            AMNetworkHandler.getNetwork().sendToServer(new PacketAbilityToggle(AffinityData.NIGHT_VISION));
        else if (WALL_CLIMB.isPressed())
            AMNetworkHandler.getNetwork().sendToServer(new PacketAbilityToggle(AffinityData.WALL_CLIMB));
        else if (ICE_BRIDGE.isPressed())
            AMNetworkHandler.getNetwork().sendToServer(new PacketAbilityToggle(AffinityData.ICE_BRIDGE_STATE));
        else if (SHAPE_GROUP.isPressed()) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            ItemStack curItem = player.inventory.getStackInSlot(player.inventory.currentItem);
            // If held item isn't a spell or spell book, check baubles charm slot
            if (curItem.isEmpty() || (curItem.getItem() != AMItems.spell && !(curItem.getItem() instanceof ItemSpellBook))) {
                curItem = ItemSpellBook.findSpellBook(player);
                if (curItem.isEmpty()) return;
            }
            int shapeGroup;
            if (curItem.getItem() == AMItems.spell && curItem.hasCapability(SpellCaster.INSTANCE, null)) {
                ISpellCaster caster = curItem.getCapability(SpellCaster.INSTANCE, null);
                shapeGroup = nextNonEmptyShapeGroup(caster);
                // Update client-side capability immediately so rendering and casting
                // are correct before the server confirms via slot sync.
                caster.setCurentShapeGroup(shapeGroup);
            } else {
                ItemStack spellStack = ((ItemSpellBook) curItem.getItem()).getActiveItemStack(curItem);
                if (spellStack.isEmpty() || !spellStack.hasCapability(SpellCaster.INSTANCE, null)) {
                    return;
                }
                ISpellCaster caster = spellStack.getCapability(SpellCaster.INSTANCE, null);
                shapeGroup = nextNonEmptyShapeGroup(caster);
                ((ItemSpellBook) curItem.getItem()).replaceActiveItemStack(curItem, spellStack);
            }

            AMNetworkHandler.getNetwork().sendToServer(new PacketSpellShapeGroupChange(shapeGroup, clientPlayer.getEntityId()));

        } else if (SPELL_BOOK_NEXT.isPressed()) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            ItemStack curItem = ItemSpellBook.findSpellBook(player);
            if (!curItem.isEmpty()) {
                int slot = ItemSpellBook.isSpellBookInBaubles(player) ? -1 : player.inventory.currentItem;
                AMNetworkHandler.getNetwork().sendToServer(new PacketSpellbookSlotChange(ItemSpellBook.ID_NEXT_SPELL, slot));
            }
        } else if (SPELL_BOOK_PREV.isPressed()) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            ItemStack curItem = ItemSpellBook.findSpellBook(player);
            if (!curItem.isEmpty()) {
                int slot = ItemSpellBook.isSpellBookInBaubles(player) ? -1 : player.inventory.currentItem;
                AMNetworkHandler.getNetwork().sendToServer(new PacketSpellbookSlotChange(ItemSpellBook.ID_PREV_SPELL, slot));
            }
        }
//		else if (this.SpellBookNextSpellKey.isPressed()){
//			EntityPlayer player = Minecraft.getMinecraft().player;
//			ItemStack curItem = player.inventory.getStackInSlot(player.inventory.currentItem);
//			if (curItem == null){
//				return;
//			}
//			if (curItem.getItem() == ItemDefs.spellBook || curItem.getItem() == ItemDefs.arcaneSpellbook){
//				//send packet to server
//				AMNetHandler.INSTANCE.sendSpellbookSlotChange(player, player.inventory.currentItem, ItemSpellBook.ID_NEXT_SPELL);
//			}
//		}else if (this.SpellBookPrevSpellKey.isPressed()){
//			EntityPlayer player = Minecraft.getMinecraft().player;
//			ItemStack curItem = player.inventory.getStackInSlot(player.inventory.currentItem);
//			if (curItem == null){
//				return;
//			}
//			if (curItem.getItem() == ItemDefs.spellBook || curItem.getItem() == ItemDefs.arcaneSpellbook){
//				//send packet to server
//				AMNetHandler.INSTANCE.sendSpellbookSlotChange(player, player.inventory.currentItem, ItemSpellBook.ID_PREV_SPELL);
//			}
//		}
//        else if (AURA_CUSTOMIZATION.isPressed()) {
//            if (ArsMagica.proxy.playerTracker.hasAA(clientPlayer)) {
//                Minecraft.getMinecraft().displayGuiScreen(new AuraCustomizationMenu());
//            }
//        }
    }

    /** Cycles forward from the current shape group, skipping any that are empty. */
    private static int nextNonEmptyShapeGroup(ISpellCaster caster) {
        int count = caster.getShapeGroupCount();
        if (count <= 1) return 0;
        int start = caster.getCurrentShapeGroup();
        for (int i = 1; i <= count; i++) {
            int candidate = (start + i) % count;
            if (caster.isShapeGroupNonEmpty(candidate)) return candidate;
        }
        // All groups empty – stay where we are.
        return start;
    }

    /**
     * Tick handler for charm-slot casting.  Fires BEGIN on first press,
     * TICK every subsequent tick while held, and STOP on release —
     * supporting both instant and channeled spells.
     * <p>
     * Also executes the spell client-side so shapes like Beam and Cone
     * can spawn their particles (they only render when {@code world.isRemote}).
     * This mirrors vanilla's {@code onUsingTick} which runs on both sides.
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getMinecraft().player == null) return;
        // Don't process keybinds when a GUI is open
        if (Minecraft.getMinecraft().currentScreen != null) {
            if (charmCasting) {
                AMNetworkHandler.getNetwork().sendToServer(new PacketCharmCast(PacketCharmCast.STOP));
                charmCasting = false;
            }
            return;
        }

        boolean keyDown = CHARM_CAST.isKeyDown();

        if (keyDown && !charmCasting) {
            // Key just pressed — send BEGIN and cast client-side for visuals
            if (ItemSpellBook.isSpellBookInBaubles(Minecraft.getMinecraft().player)) {
                AMNetworkHandler.getNetwork().sendToServer(new PacketCharmCast(PacketCharmCast.BEGIN));
                castCharmSpellClientSide();
                charmCasting = true;
            }
        } else if (keyDown && charmCasting) {
            // Key held — send TICK and cast client-side for channeled visuals
            AMNetworkHandler.getNetwork().sendToServer(new PacketCharmCast(PacketCharmCast.TICK));
            castCharmChanneledClientSide();
        } else if (!keyDown && charmCasting) {
            // Key released — send STOP
            AMNetworkHandler.getNetwork().sendToServer(new PacketCharmCast(PacketCharmCast.STOP));
            charmCasting = false;
        }
    }

    /**
     * Execute the active charm spell on the client side so that shapes
     * with client-only rendering (Beam, Cone) produce their visuals.
     */
    private static void castCharmSpellClientSide() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        ItemStack bookStack = ItemSpellBook.findSpellBook(player);
        if (bookStack.isEmpty() || !(bookStack.getItem() instanceof ItemSpellBook)) return;
        ItemStack spellStack = ((ItemSpellBook) bookStack.getItem()).getActiveItemStack(bookStack);
        if (spellStack.isEmpty() || !spellStack.hasCapability(SpellCaster.INSTANCE, null)) return;
        ISpellCaster caster = spellStack.getCapability(SpellCaster.INSTANCE, null);
        caster.cast(spellStack, player.world, player);
    }

    /**
     * Client-side tick cast for channeled spells only.
     */
    private static void castCharmChanneledClientSide() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        ItemStack bookStack = ItemSpellBook.findSpellBook(player);
        if (bookStack.isEmpty() || !(bookStack.getItem() instanceof ItemSpellBook)) return;
        ItemStack spellStack = ((ItemSpellBook) bookStack.getItem()).getActiveItemStack(bookStack);
        if (spellStack.isEmpty() || !spellStack.hasCapability(SpellCaster.INSTANCE, null)) return;
        ISpellCaster caster = spellStack.getCapability(SpellCaster.INSTANCE, null);
        if (caster.createSpellData(spellStack).isChanneled()) {
            caster.cast(spellStack, player.world, player);
        }
    }
}
