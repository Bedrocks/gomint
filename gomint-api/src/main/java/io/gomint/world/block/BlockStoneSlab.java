/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.world.block;

import io.gomint.world.block.data.StoneType;

/**
 * @author geNAZt
 * @version 1.0
 */
public interface BlockStoneSlab extends BlockSlab {

    /**
     * Get the type of stone this slab has
     *
     * @return type of stone
     */
    StoneType getStoneType();

    /**
     * Set the type of stone for this block
     *
     * @param stoneType for this block
     */
    void setStoneType( StoneType stoneType );

}
