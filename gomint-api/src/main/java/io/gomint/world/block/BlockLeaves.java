/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.world.block;

import io.gomint.world.block.data.WoodType;

/**
 * @author geNAZt
 * @version 1.0
 */
public interface BlockLeaves extends Block {

    /**
     * Set the type of leave
     *
     * @param type of the leave
     */
    void setWoodType( WoodType type );

    /**
     * Get type of leave
     *
     * @return type of leave
     */
    WoodType getWoodType();

}
