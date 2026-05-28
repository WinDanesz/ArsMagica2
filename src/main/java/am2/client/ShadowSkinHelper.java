package am2.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class ShadowSkinHelper {
    public static final ResourceLocation locationStevePng = DefaultPlayerSkin.getDefaultSkinLegacy();
    private ResourceLocation locationSkin = locationStevePng;
    private String skinType = "default";

    public ResourceLocation getLocationSkin() {
        return this.locationSkin;
    }

    public String getSkinType() {
        return this.skinType;
    }

    public ThreadDownloadImageData getTextureSkin() {
        ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(this.locationSkin);
        return texture instanceof ThreadDownloadImageData ? (ThreadDownloadImageData) texture : null;
    }

    public void setupCustomSkin(String mimicUser) {
        String sanitizedUser = StringUtils.stripControlCodes(mimicUser);

        if (sanitizedUser.isEmpty()) {
            this.locationSkin = locationStevePng;
            this.skinType = "default";
            return;
        }

        GameProfile profile = TileEntitySkull.updateGameProfile(new GameProfile((UUID) null, sanitizedUser));
        UUID playerUuid = EntityPlayer.getUUID(profile);
        ResourceLocation resolvedSkin = DefaultPlayerSkin.getDefaultSkin(playerUuid);
        String resolvedSkinType = DefaultPlayerSkin.getSkinType(playerUuid);

        if (profile.isComplete()) {
            SkinManager skinManager = Minecraft.getMinecraft().getSkinManager();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> skins = skinManager.loadSkinFromCache(profile); 

            if (skins.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                MinecraftProfileTexture skinTexture = skins.get(MinecraftProfileTexture.Type.SKIN);
                resolvedSkin = skinManager.loadSkin(skinTexture, MinecraftProfileTexture.Type.SKIN);
                String textureSkinType = skinTexture.getMetadata("model");
                if (textureSkinType != null) {
                    resolvedSkinType = textureSkinType;
                }
            } else {
                skinManager.loadProfileTextures(profile, new SkinManager.SkinAvailableCallback() {
                    @Override
                    public void skinAvailable(MinecraftProfileTexture.Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
                        if (typeIn == MinecraftProfileTexture.Type.SKIN) {
                            ShadowSkinHelper.this.locationSkin = location;
                            String textureSkinType = profileTexture.getMetadata("model");
                            ShadowSkinHelper.this.skinType = textureSkinType != null ? textureSkinType : DefaultPlayerSkin.getSkinType(playerUuid);
                        }
                    }
                }, true);
            }
        }

        this.locationSkin = resolvedSkin;
        this.skinType = resolvedSkinType;
    }
}
