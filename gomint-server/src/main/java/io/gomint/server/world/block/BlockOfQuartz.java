package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemStack;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.block.helper.ToolPresets;
import io.gomint.server.world.block.state.AxisBlockState;
import io.gomint.server.world.block.state.EnumBlockState;
import io.gomint.world.block.BlockBlockOfQuartz;
import io.gomint.world.block.BlockType;
import io.gomint.world.block.data.Axis;
import io.gomint.world.block.data.QuartzVariant;

import java.util.Collections;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:quartz_block", def = true)
@RegisterInfo(sId = "minecraft:quartz_bricks")
public class BlockOfQuartz extends Block implements BlockBlockOfQuartz {

    private enum VariantMagic {
        SMOOTH("minecraft:quartz_block","smooth"),
        LINES("minecraft:quartz_block","lines"),
        DEFAULT("minecraft:quartz_block","default"),
        CHISELED("minecraft:quartz_block","chiseled"),
        BRICKS("minecraft:quartz_bricks", "bricks");

        private final String blockId;
        private final String value;

        VariantMagic(String blockId, String value) {
            this.blockId = blockId;
            this.value = value;
        }
    }

    private static final AxisBlockState AXIS = new AxisBlockState(() -> new String[]{"pillar_axis"});
    private static final EnumBlockState<VariantMagic, String> VARIANT = new EnumBlockState<>(v -> new String[]{"chisel_type"}, VariantMagic.values(), v -> v.value, v -> {
        for (VariantMagic value : VariantMagic.values()) {
            if (value.value.equals(v)) {
                return value;
            }
        }

        return null;
    });

    @Override
    public long getBreakTime() {
        return 1200;
    }

    @Override
    public float getBlastResistance() {
        return 4.0f;
    }

    @Override
    public BlockType getBlockType() {
        return BlockType.BLOCK_OF_QUARTZ;
    }

    @Override
    public QuartzVariant getVariant() {
        if ("minecraft:quartz_bricks".equals(this.getBlockId())) {
            return QuartzVariant.BRICKS;
        }

        return QuartzVariant.valueOf(VARIANT.getState(this).name());
    }

    @Override
    public void setVariant(QuartzVariant variant) {
        if (variant == QuartzVariant.BRICKS) {
            this.setBlockId("minecraft:quartz_bricks");
            return;
        }

        VARIANT.setState(this, VariantMagic.valueOf(variant.name()));
    }

    @Override
    public Class<? extends ItemStack>[] getToolInterfaces() {
        return ToolPresets.PICKAXE;
    }

    @Override
    public List<ItemStack> getDrops(ItemStack itemInHand) {
        if (isCorrectTool(itemInHand)) {
            return super.getDrops(itemInHand);
        }

        return Collections.emptyList();
    }

    @Override
    public void setAxis(Axis axis) {
        AXIS.setState(this, axis);
    }

    @Override
    public Axis getAxis() {
        return AXIS.getState(this);
    }

}
