package am2.client.gui;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;


public class AMGuiIcons {


    public static TextureAtlasSprite padlock;
    public static TextureAtlasSprite gatewayPortal;
    public static TextureAtlasSprite evilBook;
    public static TextureAtlasSprite selectedRunes;

    public static TextureAtlasSprite warning;
    public static TextureAtlasSprite checkmark;
    public static TextureAtlasSprite newEntry;

    public static TextureAtlasSprite frame;

    public static AMGuiIcons instance = new AMGuiIcons();


    private AMGuiIcons() {

    }

    public void init(TextureMap textureMap) {

        padlock = textureMap.registerSprite(new ResourceLocation("arsmagica2:gui_icons/padlock"));
        warning = textureMap.registerSprite(new ResourceLocation("arsmagica2:gui_icons/update_available"));
        checkmark = textureMap.registerSprite(new ResourceLocation("arsmagica2:gui_icons/up_to_date"));

        newEntry = textureMap.registerSprite(new ResourceLocation("arsmagica2:gui_icons/new"));

        evilBook = textureMap.registerSprite(new ResourceLocation("arsmagica2:items/evil_book"));

        gatewayPortal = textureMap.registerSprite(new ResourceLocation("arsmagica2:gui_icons/gateway"));

        selectedRunes = textureMap.registerSprite(new ResourceLocation("arsmagica2:gui_icons/rune_selected_aura"));

        frame = textureMap.registerSprite(new ResourceLocation("arsmagica2:gui_icons/spell_frame"));
    }
}
