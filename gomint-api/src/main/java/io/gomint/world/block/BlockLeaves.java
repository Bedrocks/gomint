/*
 * Copyright (c) 2018 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.world.block;

import io.gomint.world.block.data.LogType;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public interface BlockLeaves extends Block {

    /**
     * Set the type of leave
     *
     * @param type of the leave
     */
    void setLeaveType( LogType type );

    /**
     * Get type of leave
     *
     * @return type of leave
     */
    LogType getLeaveType();

}
