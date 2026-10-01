package com.valithedev.slabplacer.config;

/** Exactly one placement mode is active at a time. */
public enum PlacementMode {
    ONLY_TOP("Only Top"),
    ONLY_BOTTOM("Only Bottom"),
    PREVENT_DOUBLE("Prevent Double"),
    NO_FUNCTION("No Function");

    private final String displayName;

    PlacementMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PlacementMode next() {
        return switch (this) {
            case ONLY_TOP -> ONLY_BOTTOM;
            case ONLY_BOTTOM -> PREVENT_DOUBLE;
            case PREVENT_DOUBLE -> NO_FUNCTION;
            case NO_FUNCTION -> ONLY_TOP;
        };
    }
}
