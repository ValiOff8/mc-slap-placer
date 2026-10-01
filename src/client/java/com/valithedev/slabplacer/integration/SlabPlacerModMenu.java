package com.valithedev.slabplacer.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.valithedev.slabplacer.screen.SlabPlacerSettingsScreen;

/** Opens the shared settings screen from Mod Menu and returns to it when closed. */
public final class SlabPlacerModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<SlabPlacerSettingsScreen> getModConfigScreenFactory() {
        return SlabPlacerSettingsScreen::new;
    }
}
