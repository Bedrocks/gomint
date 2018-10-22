/*
 * Copyright (c) 2018, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.util;

import io.gomint.world.block.BlockFace;
import io.gomint.world.block.data.Facing;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author geNAZt
 * @version 1.0
 */
@RequiredArgsConstructor
@Getter
public enum Bearing {

    SOUTH( 0 ),
    WEST( 1 ),
    NORTH( 2 ),
    EAST( 3 );

    private final int direction;

    public Facing toFacing() {
        switch ( this ) {
            case SOUTH:
                return Facing.SOUTH;
            case NORTH:
                return Facing.NORTH;
            case EAST:
                return Facing.EAST;
            case WEST:
                return Facing.WEST;
        }

        return Facing.EAST;
    }

    public BlockFace toBlockFace() {
        switch ( this ) {
            case SOUTH:
                return BlockFace.SOUTH;
            case NORTH:
                return BlockFace.NORTH;
            case EAST:
                return BlockFace.EAST;
            case WEST:
                return BlockFace.WEST;
        }

        return BlockFace.EAST;
    }

    public Bearing opposite() {
        switch ( this ) {
            case SOUTH:
                return NORTH;
            case NORTH:
                return SOUTH;
            case EAST:
                return WEST;
            case WEST:
                return EAST;
        }

        return EAST;
    }

    /**
     * Get the correct bearing from the given angle
     *
     * @param angle which should be converted
     * @return bearing value
     */
    public static Bearing fromAngle( float angle ) {
        // Normalize angle
        angle %= 360;
        if ( angle < 0 ) {
            angle += 360.0;
        }

        // Check for south, west, north, east
        if ( ( 0 <= angle && angle < 45 ) || ( 315 <= angle && angle < 360 ) ) {
            return SOUTH;
        }

        if ( 45 <= angle && angle < 135 ) {
            return WEST;
        }

        if ( 135 <= angle && angle < 225 ) {
            return NORTH;
        }

        return EAST;
    }

}
