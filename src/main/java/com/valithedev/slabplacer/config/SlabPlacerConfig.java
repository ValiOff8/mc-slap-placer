package com.valithedev.slabplacer.config;

import java.util.Objects;

/** Stores one exclusive placement mode. */
public final class SlabPlacerConfig {
    private PlacementMode mode = PlacementMode.NO_FUNCTION;

    public SlabPlacerConfig() {
    }

    public SlabPlacerConfig(PlacementMode mode) {
        setMode(mode);
    }

    public PlacementMode getMode() {
        return mode;
    }

    public void setMode(PlacementMode mode) {
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    public void cycleMode() {
        mode = mode.next();
    }

    /** Returns an independent draft for editing before saving. */
    public SlabPlacerConfig copy() {
        return new SlabPlacerConfig(mode);
    }
}
