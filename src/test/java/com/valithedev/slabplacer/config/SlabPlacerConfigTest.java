package com.valithedev.slabplacer.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SlabPlacerConfigTest {
    @Test
    void defaultsToNormalMinecraftPlacement() {
        assertEquals(PlacementMode.NO_FUNCTION, new SlabPlacerConfig().getMode());
    }

    @Test
    void cyclesThroughAllFourModesAndWrapsBackToTheFirst() {
        SlabPlacerConfig config = new SlabPlacerConfig();
        PlacementMode[] expected = {
                PlacementMode.ONLY_TOP, PlacementMode.ONLY_BOTTOM,
                PlacementMode.PREVENT_DOUBLE, PlacementMode.NO_FUNCTION,
                PlacementMode.ONLY_TOP
        };
        for (PlacementMode mode : expected) {
            config.cycleMode();
            assertEquals(mode, config.getMode());
        }
    }

    @Test
    void switchingModesReplacesThePreviousChoice() {
        SlabPlacerConfig config = new SlabPlacerConfig(PlacementMode.ONLY_TOP);
        config.setMode(PlacementMode.PREVENT_DOUBLE);
        assertEquals(PlacementMode.PREVENT_DOUBLE, config.getMode());
        config.setMode(PlacementMode.NO_FUNCTION);
        config.cycleMode();
        assertEquals(PlacementMode.ONLY_TOP, config.getMode());
    }

    @Test
    void editingADraftDoesNotChangeTheActiveMode() {
        SlabPlacerConfig config = new SlabPlacerConfig(PlacementMode.ONLY_BOTTOM);
        SlabPlacerConfig draft = config.copy();
        assertEquals(PlacementMode.ONLY_BOTTOM, draft.getMode());
        draft.cycleMode();
        assertEquals(PlacementMode.PREVENT_DOUBLE, draft.getMode());
        assertEquals(PlacementMode.ONLY_BOTTOM, config.getMode());
    }

    @Test
    void nullModeIsRejectedWithoutChangingTheCurrentChoice() {
        SlabPlacerConfig config = new SlabPlacerConfig(PlacementMode.ONLY_TOP);
        assertThrows(NullPointerException.class, () -> config.setMode(null));
        assertEquals(PlacementMode.ONLY_TOP, config.getMode());
    }
}
