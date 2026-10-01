package com.valithedev.slabplacer.placement;

import com.valithedev.slabplacer.config.PlacementMode;
import com.valithedev.slabplacer.config.SlabPlacerConfig;
import com.valithedev.slabplacer.placement.PlacementPolicy.SlabPlacement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlacementPolicyTest {
    @ParameterizedTest
    @CsvSource({
            "ONLY_TOP, TOP, true",
            "ONLY_TOP, BOTTOM, false",
            "ONLY_TOP, DOUBLE, false",
            "ONLY_BOTTOM, TOP, false",
            "ONLY_BOTTOM, BOTTOM, true",
            "ONLY_BOTTOM, DOUBLE, false",
            "PREVENT_DOUBLE, TOP, true",
            "PREVENT_DOUBLE, BOTTOM, true",
            "PREVENT_DOUBLE, DOUBLE, false",
            "NO_FUNCTION, TOP, true",
            "NO_FUNCTION, BOTTOM, true",
            "NO_FUNCTION, DOUBLE, true"
    })
    void eachModeAllowsOnlyTheSpecifiedSlabPlacements(PlacementMode mode, SlabPlacement placement,
                                                     boolean allowed) {
        assertEquals(allowed, PlacementPolicy.allows(new SlabPlacerConfig(mode), placement));
    }
}
