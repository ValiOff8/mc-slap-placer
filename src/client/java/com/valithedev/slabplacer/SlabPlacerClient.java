package com.valithedev.slabplacer;

import com.mojang.blaze3d.platform.InputConstants;
import com.valithedev.slabplacer.config.ConfigStore;
import com.valithedev.slabplacer.config.SlabPlacerConfig;
import com.valithedev.slabplacer.screen.SlabPlacerSettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class SlabPlacerClient implements ClientModInitializer {
    private static final ConfigStore CONFIG_STORE = new ConfigStore(
            FabricLoader.getInstance().getConfigDir().resolve("slabplacer.json"));
    private static SlabPlacerConfig config = new SlabPlacerConfig();

    @Override
    public void onInitializeClient() {
        config = CONFIG_STORE.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("slabplacer", "controls"));
        KeyMapping settingsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.slabplacer.open_settings", InputConstants.Type.KEYBOARD,
                InputConstants.KEY_K, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (settingsKey.consumeClick()) {
                if (client.gui.screen() == null && client.player != null) {
                    client.gui.setScreen(new SlabPlacerSettingsScreen(null));
                }
            }
        });
    }

    public static SlabPlacerConfig getConfig() {
        return config;
    }

    public static boolean saveConfig(SlabPlacerConfig draft) {
        SlabPlacerConfig saved = draft.copy();
        if (!CONFIG_STORE.save(saved)) {
            return false;
        }
        config = saved;
        return true;
    }
}
