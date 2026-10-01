package com.valithedev.slabplacer.placement;

import com.valithedev.slabplacer.config.SlabPlacerConfig;

import java.util.Objects;

/** Decides whether the slab state resulting from vanilla placement is permitted. */
public final class PlacementPolicy {
    private PlacementPolicy() {
    }

    public enum SlabPlacement {
        TOP,
        BOTTOM,
        DOUBLE
    }

    public static boolean allows(SlabPlacerConfig config, SlabPlacement placement) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(placement, "placement");

        return switch (config.getMode()) {
            case ONLY_TOP -> placement == SlabPlacement.TOP;
            case ONLY_BOTTOM -> placement == SlabPlacement.BOTTOM;
            case PREVENT_DOUBLE -> placement != SlabPlacement.DOUBLE;
            case NO_FUNCTION -> true;
        };
    }
}
