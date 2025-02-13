package de.maxhenkel.wiretap.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;
import de.maxhenkel.wiretap.Wiretap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class HeadUtils {

    public static final String MICROPHONE = "mic-chl0xy0g";
    public static final String SPEAKER = "speaker-chl0xy0g";

    public static ItemStack createMicrophone(UUID id) {
        return createHead("Microphone", id, MICROPHONE, Wiretap.SERVER_CONFIG.microphoneSkinUrl.get());
    }

    public static ItemStack createSpeaker(UUID id) {
        return createHead("Speaker", id, SPEAKER, Wiretap.SERVER_CONFIG.speakerSkinUrl.get());
    }

    @Nullable
    public static UUID getMicrophone(GameProfile profile) {
        if (profile == null) {
            return null;
        }
        if (!profile.getName().equals(MICROPHONE)) {
            return null;
        }
        return profile.getId();
    }

    @Nullable
    public static UUID getSpeaker(GameProfile profile) {
        if (profile == null) {
            return null;
        }
        if (!profile.getName().equals(SPEAKER)) {
            return null;
        }
        return profile.getId();
    }

    public static ItemStack createHead(String itemName, UUID id, String name, String skinUrl) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

        MutableComponent loreComponent = Component.literal("ID: %s".formatted(id.toString())).withStyle(style -> style.withItalic(false)).withStyle(ChatFormatting.GRAY);
        ItemLore lore = new ItemLore(List.of(loreComponent));
        stack.set(DataComponents.LORE, lore);

        MutableComponent nameComponent = Component.literal(itemName).withStyle(style -> style.withItalic(false).withColor(ChatFormatting.WHITE));
        stack.set(DataComponents.CUSTOM_NAME, nameComponent);

        ResolvableProfile profile = new ResolvableProfile(Optional.of(name), Optional.of(id), getTextures(skinUrl));
        stack.set(DataComponents.PROFILE, profile);

        return stack;
    }

    private static final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    private static PropertyMap getTextures(String skinUrl) {
        PropertyMap properties = new PropertyMap();
        List<Property> textures = new ArrayList<>();

        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textureMap = new HashMap<>();
        textureMap.put(MinecraftProfileTexture.Type.SKIN, new MinecraftProfileTexture(skinUrl, null));

        String json = gson.toJson(new MinecraftTexturesPayload(textureMap));

        String base64Payload = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

        textures.add(new Property("textures", base64Payload));

        properties.putAll("textures", textures);
        return properties;
    }

    private record MinecraftTexturesPayload(Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures) {
    }
}
