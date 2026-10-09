package am2.common.lore;

import am2.ArsMagica;
import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.common.CommonProxy;
import am2.common.config.AMConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Headless harness for the Arcane Compendium unlock logic. No Minecraft runtime is started:
 * the config is loaded from a throwaway file and the proxy is replaced with a recorder.
 */
public class ArcaneCompendiumHarnessTest {

    static class RecordingProxy extends CommonProxy {
        final List<String> toasts = new ArrayList<>();

        @Override
        public void showCompendiumToast(String entryId) {
            toasts.add(entryId);
        }
    }

    private RecordingProxy proxy;

    static {
        net.minecraft.init.Bootstrap.register();
    }

    private void setStaged(boolean staged) throws Exception {
        // Forge's Configuration needs a running Loader, so skip the constructor entirely
        java.lang.reflect.Field u = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        u.setAccessible(true);
        ArsMagica.config = (AMConfig) ((sun.misc.Unsafe) u.get(null)).allocateInstance(AMConfig.class);
        java.lang.reflect.Field f = AMConfig.class.getDeclaredField("stagedCompendium");
        f.setAccessible(true);
        f.setBoolean(ArsMagica.config, staged);
    }

    @BeforeEach
    void setUp() throws Exception {
        proxy = new RecordingProxy();
        ArsMagica.proxy = proxy;
        setStaged(true);
    }

    @Test
    void unlockMarksEntryAndToastsOnce() {
        ArcaneCompendium c = new ArcaneCompendium();
        assertFalse(c.isUnlocked("guide.foo"));
        c.unlockEntry("guide.foo");
        c.unlockEntry("guide.foo");
        assertTrue(c.isUnlocked("guide.foo"));
        assertEquals(1, proxy.toasts.size());
    }

    @Test
    void unstagedConfigShowsEverything() throws Exception {
        setStaged(false);
        ArcaneCompendium c = new ArcaneCompendium();
        assertTrue(c.isUnlocked("anything.at_all"));
    }

    /** Entries arriving in a later sync toast; the full book sent on join does not. */
    @Test
    void syncPacketUnlockShowsToastButJoinSyncDoesNot() {
        ArsMagica.proxy = new CommonProxy(); // server side: toast is a no-op
        ArcaneCompendium server = new ArcaneCompendium();
        server.unlockEntry("guide.old");
        byte[] joinPacket = server.generateUpdatePacket();
        server.unlockEntry("guide.new");
        byte[] laterPacket = server.generateUpdatePacket();

        ArsMagica.proxy = proxy;
        ArcaneCompendium client = new ArcaneCompendium();
        client.handleUpdatePacket(joinPacket);
        assertEquals(0, proxy.toasts.size(), "join sync must not toast the whole book");
        client.handleUpdatePacket(laterPacket);
        assertTrue(client.isUnlocked("guide.new"));
        assertEquals(java.util.Collections.singletonList("guide.new"), proxy.toasts);
    }

    /** The server list is authoritative: a sync replaces the client list. */
    @Test
    void serverSyncIsAuthoritative() {
        ArcaneCompendium server = new ArcaneCompendium();
        server.unlockEntry("guide.a");
        server.unlockEntry("guide.b");
        ArcaneCompendium client = new ArcaneCompendium();
        client.handleUpdatePacket(server.generateUpdatePacket());
        assertTrue(client.isUnlocked("guide.a"));
        assertTrue(client.isUnlocked("guide.b"));
    }

    /** Unlocking one entry must not unlock a different entry that merely shares its last id segment. */
    @Test
    void sameSimpleNameInOtherCategoryStaysLocked() {
        ArcaneCompendium c = new ArcaneCompendium();
        c.unlockEntry("component.mana_drain");
        assertFalse(c.isUnlocked("block.mana_drain"));
    }

    @Test
    void nbtRoundTripKeepsUnlocks() {
        CompendiumEntry e = new CompendiumEntry(null, "roundtrip").setCategory(CompendiumCategory.GUIDE);
        CompendiumCategory.GUIDE.addEntry(e);
        ArcaneCompendium a = new ArcaneCompendium();
        a.unlockEntry(e.getID());
        ArcaneCompendium b = new ArcaneCompendium();
        b.deserializeNBT(a.serializeNBT());
        assertTrue(b.isUnlocked(e.getID()));
        assertFalse(new ArcaneCompendium().isUnlocked(e.getID()));
    }
}
