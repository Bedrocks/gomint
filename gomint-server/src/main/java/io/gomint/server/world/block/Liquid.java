package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemAir;
import io.gomint.inventory.item.ItemStack;
import io.gomint.math.BlockPosition;
import io.gomint.math.Location;
import io.gomint.math.MathUtils;
import io.gomint.math.Vector;
import io.gomint.server.entity.Entity;
import io.gomint.server.entity.EntityLiving;
import io.gomint.server.world.UpdateReason;
import io.gomint.server.world.block.state.DirectValueBlockState;
import io.gomint.world.Sound;
import io.gomint.world.block.BlockLiquid;
import io.gomint.world.block.data.Direction;
import io.gomint.world.block.data.Facing;

import java.util.HashMap;
import java.util.Map;

/**
 * @author geNAZt
 * @version 1.0
 */
public abstract class Liquid extends Block implements BlockLiquid {

    private static final String[] LIQUID_DEPTH_KEY = new String[]{"liquid_depth"};
    private static final Direction[] DIRECTIONS_TO_CHECK = Direction.values();

    private enum FlowState {
        CAN_FLOW_DOWN,
        CAN_FLOW,
        BLOCKED
    }

    private static final DirectValueBlockState<Integer> LIQUID_DEPTH = new DirectValueBlockState<>(() -> LIQUID_DEPTH_KEY, 0);

    // Temporary storage for update
    private byte adjacentSources;
    private Map<BlockPosition, FlowState> flowCostVisited;

    @Override
    public float getFillHeight() {
        int data = LIQUID_DEPTH.getState(this);
        if (data >= 8) {
            data = 8;
        }

        if (data == 0) {
            return 1f;
        }

        return (data / 8f);
    }

    @Override
    public void setFillHeight(float height) {
        if (height < 0f || height > 1f) {
            return;
        }

        LIQUID_DEPTH.setState(this, MathUtils.fastRound(8f * height));
    }

    private short getEffectiveFlowDecay(Block block) {
        // Block changed type?
        if (block.getBlockType() != this.getBlockType()) {
            return -1;
        }

        // Get block data and cap by 8
        int data = LIQUID_DEPTH.getState(this);
        return (short) (data >= 8 ? 0 : data);
    }

    @Override
    public Vector getFlowVector() {
        // Create a new vector and get the capped flow decay of this block
        Vector vector = Vector.ZERO;
        short decay = this.getEffectiveFlowDecay(this);

        // Check all 4 sides if we can flow into that
        for (Direction face : Direction.values()) {
            Block other = this.getSide(face);
            short blockDecay = this.getEffectiveFlowDecay(other);

            // Do we have decay?
            if (blockDecay < 0) {
                // We don't, check if we can flow replacing the block
                if (!other.canBeFlowedInto()) {
                    continue;
                }

                // Check if the block under this is a water block so we can connect those two
                blockDecay = this.getEffectiveFlowDecay(this.world.getBlockAt(other.getPosition().add(0, -1, 0)));
                if (blockDecay >= 0) {
                    byte realDecay = (byte) (blockDecay - (decay - 8));
                    vector = vector.add((other.getPosition().getX() - this.getPosition().getX()) * (float) realDecay,
                        (other.getPosition().getY() - this.getPosition().getY()) * (float) realDecay,
                        (other.getPosition().getZ() - this.getPosition().getZ()) * (float) realDecay);
                }
            } else {
                // Check if we need to update the other blocks decay
                byte realDecay = (byte) (blockDecay - decay);
                vector = vector.add((other.getPosition().getX() - this.getPosition().getX()) * (float) realDecay,
                    (other.getPosition().getY() - this.getPosition().getY()) * (float) realDecay,
                    (other.getPosition().getZ() - this.getPosition().getZ()) * (float) realDecay);
            }
        }

        if (LIQUID_DEPTH.getState(this) >= 8) {
            BlockPosition pos = this.getPosition();
            if (!this.canFlowInto(this.world.getBlockAt(pos = pos.add(0, 0, -1))) ||
                !this.canFlowInto(this.world.getBlockAt(pos = pos.add(0, 0, 1))) ||
                !this.canFlowInto(this.world.getBlockAt(pos = pos.add(-1, 0, 0))) ||
                !this.canFlowInto(this.world.getBlockAt(pos = pos.add(1, 0, 0))) ||
                !this.canFlowInto(this.world.getBlockAt(pos = pos.add(0, 1, -1))) ||
                !this.canFlowInto(this.world.getBlockAt(pos = pos.add(0, 1, 1))) ||
                !this.canFlowInto(this.world.getBlockAt(pos = pos.add(-1, 1, 0))) ||
                !this.canFlowInto(this.world.getBlockAt(pos.add(1, 1, 0)))) {
                vector = vector.normalize().add(0, -6, 0);
            }
        }

        return vector.normalize();
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    @Override
    public void stepOn(Entity entity) {
        // Reset fall distance
        entity.resetFallDistance();
    }

    @Override
    public boolean canBeReplaced(ItemStack item) {
        return true;
    }

    public abstract int getTickDiff();

    public abstract boolean isFlowing();

    @Override
    public long update(UpdateReason updateReason, long currentTimeMS, float dT) {
        if (!isFlowing()) {
            return -1;
        }

        if (updateReason == UpdateReason.BLOCK_ADDED ||
            updateReason == UpdateReason.NEIGHBOUR_UPDATE ||
            updateReason == UpdateReason.RANDOM) {
            if (isUpdateScheduled()) {
                return -1;
            }

            return currentTimeMS + getTickDiff(); // Water updates every 5 client ticks
        }

        int decay = this.getEffectiveFlowDecay(this);

        // Check for own decay updates
        this.checkOwnDecay(decay);

        // Check for spread
        this.checkSpread(decay);

        return currentTimeMS + getTickDiff(); // Water updates every 5 client ticks
    }

    private void checkSpread(int decay) {
        if (decay >= 0 && this.location.getY() > 0) {
            Block bottomBlock = this.getSide(Facing.DOWN);
            this.flowIntoBlock(bottomBlock, decay | 0x08);
            if (decay == 0 || !bottomBlock.canBeFlowedInto()) {
                int adjacentDecay;
                if (decay >= 8) {
                    adjacentDecay = 1;
                } else {
                    adjacentDecay = decay + this.getFlowDecayPerBlock();
                }

                if (adjacentDecay < 8) {
                    boolean[] flags = this.getOptimalFlowDirections();
                    if (flags[0]) {
                        this.flowIntoBlock(this.getSide(Facing.WEST), adjacentDecay);
                    }

                    if (flags[1]) {
                        this.flowIntoBlock(this.getSide(Facing.EAST), adjacentDecay);
                    }

                    if (flags[2]) {
                        this.flowIntoBlock(this.getSide(Facing.NORTH), adjacentDecay);
                    }

                    if (flags[3]) {
                        this.flowIntoBlock(this.getSide(Facing.SOUTH), adjacentDecay);
                    }
                }
            }

            this.checkForHarden();
        }
    }

    /**
     * Method to check for block hardening when two liquids collide
     */
    protected void checkForHarden() {
    }

    private void checkOwnDecay(int decay) {
        // Check for decay updates of own block
        if (decay > 0) {
            short smallestFlowDecay = -100;
            this.adjacentSources = 0;

            // Check for neighbour decay
            smallestFlowDecay = this.getSmallestFlowDecay(this.getSide(Facing.NORTH), smallestFlowDecay);
            smallestFlowDecay = this.getSmallestFlowDecay(this.getSide(Facing.SOUTH), smallestFlowDecay);
            smallestFlowDecay = this.getSmallestFlowDecay(this.getSide(Facing.WEST), smallestFlowDecay);
            smallestFlowDecay = this.getSmallestFlowDecay(this.getSide(Facing.EAST), smallestFlowDecay);

            int newDecay = smallestFlowDecay + this.getFlowDecayPerBlock();
            if (newDecay >= 8 || smallestFlowDecay < 0) { // There is no neighbour?
                // Always stop flowing => decay to air
                newDecay = -1;
            }

            // Check if there is a block on top (flowing from top downwards)
            int topFlowDecay = this.getFlowDecay(this.getSide(Facing.UP));
            if (topFlowDecay >= 0) {
                newDecay = topFlowDecay | 0x08;
            }

            // Did we hit a bottom block and are surrounded by other source blocks? -> convert to source block
            if (this.adjacentSources >= 2 && this instanceof FlowingWater) {
                Block bottomBlock = this.getSide(Facing.DOWN);
                if (bottomBlock.isSolid() || (bottomBlock instanceof FlowingWater && ((FlowingWater) bottomBlock).getFillHeight() == 1f)) {
                    newDecay = 0;
                }
            }

            // Do we need to update water decay value?
            if (newDecay != decay) {
                decay = newDecay;

                // Did we fully decay?
                boolean decayed = decay < 0;
                if (decayed) {
                    this.setBlockType(Air.class);
                } else {
                    LIQUID_DEPTH.setState(this, decay);
                }
            }
        }
    }

    private short calculateFlowCost(BlockPosition pos, short accumulatedCost, short maxCost, Direction origin, Direction current) {
        this.ensureFlowCostVisited();

        short cost = 1000;
        for (Direction facing : DIRECTIONS_TO_CHECK) {
            if (facing == current || facing == origin) {
                continue;
            }

            Block other = this.world.getBlockAt(pos);
            Block sideBock = other.getSide(facing);
            BlockPosition checkingPos = sideBock.getPosition();

            if (!this.flowCostVisited.containsKey(checkingPos)) {
                if (!this.canFlowInto(sideBock)) {
                    this.flowCostVisited.put(checkingPos, FlowState.BLOCKED);
                } else if (((Block) this.world.getBlockAt(checkingPos.add(BlockPosition.DOWN))).canBeFlowedInto()) {
                    this.flowCostVisited.put(checkingPos, FlowState.CAN_FLOW_DOWN);
                } else {
                    this.flowCostVisited.put(checkingPos, FlowState.CAN_FLOW);
                }
            }

            FlowState status = this.flowCostVisited.get(checkingPos);
            if (status == FlowState.BLOCKED) {
                continue;
            } else if (status == FlowState.CAN_FLOW_DOWN) {
                return accumulatedCost;
            }

            if (accumulatedCost >= maxCost) {
                continue;
            }

            short realCost = this.calculateFlowCost(checkingPos, (short) (accumulatedCost + 1), maxCost, origin, facing);
            if (realCost < cost) {
                cost = realCost;
            }
        }

        return cost;
    }

    private boolean[] getOptimalFlowDirections() {
        this.ensureFlowCostVisited();

        short[] flowCost = new short[]{
            1000,
            1000,
            1000,
            1000
        };

        short maxCost = (short) (4 / this.getFlowDecayPerBlock());
        int j = 0;
        for (Direction face : DIRECTIONS_TO_CHECK) {
            Block other = this.getSide(face);
            if (!this.canFlowInto(other)) {
                this.flowCostVisited.put(other.getPosition(), FlowState.BLOCKED);
            } else if (((Block) this.world.getBlockAt(this.getPosition().add(BlockPosition.DOWN))).canBeFlowedInto()) {
                this.flowCostVisited.put(other.getPosition(), FlowState.CAN_FLOW_DOWN);
                flowCost[j] = maxCost = 0;
            } else if (maxCost > 0) {
                BlockPosition pos = other.getPosition();
                this.flowCostVisited.put(pos, FlowState.CAN_FLOW);
                flowCost[j] = this.calculateFlowCost(pos, (short) 1, maxCost, face, face);
                maxCost = (short) Math.min(maxCost, flowCost[j]);
            }

            j++;
        }

        this.flowCostVisited.clear();
        double minCost = Double.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            double d = flowCost[i];
            if (d < minCost) {
                minCost = d;
            }
        }

        boolean[] isOptimalFlowDirection = new boolean[4];
        for (int i = 0; i < 4; ++i) {
            isOptimalFlowDirection[i] = (flowCost[i] == minCost);
        }

        return isOptimalFlowDirection;
    }

    private void ensureFlowCostVisited() {
        if (this.flowCostVisited == null) {
            this.flowCostVisited = new HashMap<>();
        }
    }

    /**
     * Let the block be replaced by the new liquid
     *
     * @param block        which should be replaced with a liquid
     * @param newFlowDecay decay value of the liquid
     */
    protected void flowIntoBlock(Block block, int newFlowDecay) {
        if (this.canFlowInto(block) && !(block instanceof Liquid)) {
            if (!block.getBlockId().equals("minecraft:air")) {
                this.world.breakBlock(block.getPosition(), block.getDrops(ItemAir.create(0)), false);
            }

            Liquid liquid = block.setBlockType(this.getClass());
            LIQUID_DEPTH.setState(liquid, newFlowDecay);
        }
    }

    private short getSmallestFlowDecay(Block block, short decay) {
        short blockDecay = this.getFlowDecay(block);

        if (blockDecay < 0) {
            return decay;
        } else if (blockDecay == 0) {
            ++this.adjacentSources;
        } else if (blockDecay >= 8) {
            blockDecay = 0;
        }

        return (decay >= 0 && blockDecay >= decay) ? decay : blockDecay;
    }

    protected byte getFlowDecayPerBlock() {
        return 1; // 1 for water, 2 for lava
    }

    private short getFlowDecay(Block block) {
        if (block.getBlockType() != this.getBlockType()) {
            return -1;
        }

        return LIQUID_DEPTH.getState(block).shortValue();
    }

    @Override
    public boolean canBeFlowedInto() {
        return true;
    }

    private boolean canFlowInto(Block block) {
        return block.canBeFlowedInto() && !(block instanceof Liquid && ((Liquid) block).getFillHeight() == 1f);
    }

    @Override
    public Vector addVelocity(Entity entity, Vector pushedByBlocks) {
        return pushedByBlocks.add(this.getFlowVector());
    }

    /**
     * Called when two liquids collide
     *
     * @param colliding with which we collide
     * @param result    of the collision
     */
    protected void liquidCollide(Block colliding, Class<? extends Block> result) {
        this.setBlockType(result);
        this.world.playSound(this.location.add(0.5f, 0.5f, 0.5f), Sound.FIZZ, (byte) 0);
    }

    @Override
    public boolean beforePlacement(EntityLiving entity, ItemStack item, Facing face, Location location) {
        LIQUID_DEPTH.detectFromPlacement(this, entity, item, face);
        return super.beforePlacement(entity, item, face, location);
    }

}
