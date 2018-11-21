/*
 * Copyright (c) 2018 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world.generator.vanilla.chunk;

import com.google.common.util.concurrent.SettableFuture;
import io.gomint.math.BlockPosition;
import io.gomint.server.world.ChunkAdapter;
import io.netty.util.collection.ShortObjectHashMap;
import io.netty.util.collection.ShortObjectMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author geNAZt
 * @version 1.0
 * <p>
 * A chunk square holds 24 * 24 chunks for a specific view distance of 32
 */
@RequiredArgsConstructor
public class ChunkSquare {

    @Getter private final int x;
    @Getter private final int z;
    private final ShortObjectMap<ChunkAdapter> chunks = new ShortObjectHashMap<>();
    @Getter private final SettableFuture<Boolean> future = SettableFuture.create();
    @Getter @Setter private boolean loading;

    public synchronized boolean isComplete() {
        if ( this.chunks.size() == 16 * 16 ) {
            this.future.set( true );
            return true;
        }

        return false;
    }

    public synchronized void storeChunk( ChunkAdapter chunkAdapter ) {
        byte iX = (byte) ( chunkAdapter.getX() % 16 );
        byte iZ = (byte) ( chunkAdapter.getZ() % 16 );
        short index = (short) ( iX << 8 | iZ & 0xff );
        this.chunks.put( index, chunkAdapter );
    }

    public synchronized ChunkAdapter getChunk( int x, int z ) {
        byte iX = (byte) ( x % 16 );
        byte iZ = (byte) ( z % 16 );
        short index = (short) ( iX << 8 | iZ & 0xff );
        return this.chunks.get( index );
    }

    /**
     * Get the center position of this chunk square
     *
     * @return center location of this square
     */
    public BlockPosition getCenterPosition() {
        return new BlockPosition( ( this.x * 16 ) * 16 + ( ( 16 / 2 ) * 16 ), 260, ( this.z * 16 ) * 16 + ( ( 16 / 2 ) * 16 ) );
    }

}
