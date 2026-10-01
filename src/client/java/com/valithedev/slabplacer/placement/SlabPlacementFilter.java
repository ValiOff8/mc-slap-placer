package com.valithedev.slabplacer.placement;

import com.valithedev.slabplacer.SlabPlacerClient;
import com.valithedev.slabplacer.config.PlacementMode;
import com.valithedev.slabplacer.config.SlabPlacerConfig;
import com.valithedev.slabplacer.placement.PlacementPolicy.SlabPlacement;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Predicts the slab state using the same placement context as vanilla. */
public final class SlabPlacementFilter {
    private SlabPlacementFilter() {
    }

    public static boolean shouldBlock(ItemStack stack, UseOnContext useContext) {
        SlabPlacerConfig config = SlabPlacerClient.getConfig();
        if (config.getMode() == PlacementMode.NO_FUNCTION) {
            return false;
        }
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof SlabBlock slabBlock)) {
            return false;
        }

        BlockPlaceContext context = new BlockPlaceContext(useContext);
        if (!context.canPlace()) {
            return false;
        }

        context = blockItem.updatePlacementContext(context);
        if (context == null) {
            return false;
        }

        BlockState state = slabBlock.getStateForPlacement(context);
        if (state == null || !state.hasProperty(SlabBlock.TYPE)) {
            return false;
        }

        // Item components can override the slab type after normal placement.
        state = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).apply(state);
        SlabPlacement placement = switch (state.getValue(SlabBlock.TYPE)) {
            case TOP -> SlabPlacement.TOP;
            case BOTTOM -> SlabPlacement.BOTTOM;
            case DOUBLE -> SlabPlacement.DOUBLE;
        };
        return !PlacementPolicy.allows(config, placement);
    }
}
