package com.valithedev.slabplacer.screen;

import com.valithedev.slabplacer.SlabPlacerClient;
import com.valithedev.slabplacer.config.PlacementMode;
import com.valithedev.slabplacer.config.SlabPlacerConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** English settings for the local player's slab placement mode. */
public final class SlabPlacerSettingsScreen extends Screen {
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int MUTED_COLOR = 0xFFBBBBBB;

    private final Screen parent;
    private final SlabPlacerConfig draft;
    private Button modeButton;
    private int contentTop;
    private boolean saveFailed;

    public SlabPlacerSettingsScreen(Screen parent) {
        super(Component.literal("Slab Placer Settings"));
        this.parent = parent;
        this.draft = SlabPlacerClient.getConfig().copy();
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(310, width - 32);
        int left = (width - contentWidth) / 2;
        contentTop = Math.max(8, (height - 180) / 2);

        modeButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            draft.cycleMode();
            refreshModeLabel();
        }).bounds(left, contentTop + 44, contentWidth, 20).build());

        int footerWidth = (contentWidth - 8) / 2;
        addRenderableWidget(Button.builder(Component.literal("Save"), button -> save())
                .bounds(left, contentTop + 160, footerWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
                .bounds(left + contentWidth - footerWidth, contentTop + 160, footerWidth, 20)
                .build());
        refreshModeLabel();
    }

    private void refreshModeLabel() {
        modeButton.setMessage(Component.literal("Mode: " + draft.getMode().getDisplayName()));
        modeButton.setTooltip(Tooltip.create(Component.literal(
                "Click to switch to " + draft.getMode().next().getDisplayName() + ".")));
    }

    private void save() {
        if (SlabPlacerClient.saveConfig(draft)) {
            onClose();
        } else {
            saveFailed = true;
        }
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, contentTop, TEXT_COLOR);
        graphics.centeredText(font, Component.literal("Select one placement mode."),
                width / 2, contentTop + 20, MUTED_COLOR);

        String description = switch (draft.getMode()) {
            case ONLY_TOP -> "Allow upper-half slabs only.";
            case ONLY_BOTTOM -> "Allow lower-half slabs only.";
            case PREVENT_DOUBLE -> "Allow both single-slab halves.";
            case NO_FUNCTION -> "Use normal Minecraft placement.";
        };
        String detail = switch (draft.getMode()) {
            case ONLY_TOP -> "Bottom and double slabs are blocked.";
            case ONLY_BOTTOM -> "Top and double slabs are blocked.";
            case PREVENT_DOUBLE -> "Combining slabs into a full block is blocked.";
            case NO_FUNCTION -> "All slab placement restrictions are disabled.";
        };
        graphics.centeredText(font, Component.literal(description), width / 2,
                contentTop + 82, draft.getMode() == PlacementMode.NO_FUNCTION ? MUTED_COLOR : 0xFF88DD88);
        graphics.centeredText(font, Component.literal(detail), width / 2,
                contentTop + 96, MUTED_COLOR);
        graphics.centeredText(font, Component.literal("Click the mode button to cycle."),
                width / 2, contentTop + 124, MUTED_COLOR);

        if (saveFailed) {
            graphics.centeredText(font, Component.literal("Could not save. Please try again."),
                    width / 2, contentTop + 144, 0xFFFF8888);
        }
    }
}
