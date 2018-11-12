/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.entity.tileentity;

import io.gomint.entity.Entity;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.inventory.ChestInventory;
import io.gomint.server.inventory.InventoryHolder;
import io.gomint.server.inventory.item.ItemAir;
import io.gomint.server.inventory.item.ItemStack;
import io.gomint.server.world.block.Block;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.block.BlockFace;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
public class ChestTileEntity extends ContainerTileEntity implements InventoryHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger( ChestTileEntity.class );
    private ChestInventory inventory;

    public ChestTileEntity( Block block ) {
        super( block );
        this.inventory = new ChestInventory( this );
    }

    @Override
    public void fromCompound( NBTTagCompound compound ) {
        super.fromCompound( compound );

        // Read in items
        List<Object> itemList = compound.getList( "Items", false );
        if ( itemList == null ) return;

        for ( Object item : itemList ) {
            NBTTagCompound itemCompound = (NBTTagCompound) item;

            io.gomint.server.inventory.item.ItemStack itemStack = getItemStack( itemCompound );
            if ( itemStack instanceof ItemAir ) {
                continue;
            }

            byte slot = itemCompound.getByte( "Slot", (byte) 127 );
            if ( slot == 127 ) {
                LOGGER.warn( "Found item without slot information: {} @ {} setting it to the next free slot", itemStack.getMaterial(), this.block.getLocation() );
                this.inventory.addItem( itemStack );
            } else {
                this.inventory.setItem( slot, itemStack );
            }
        }
    }

    @Override
    public void update( long currentMillis ) {

    }

    @Override
    public void interact( Entity entity, BlockFace face, Vector facePos, io.gomint.inventory.item.ItemStack item ) {
        // Open the chest inventory for the entity
        if ( entity instanceof EntityPlayer ) {
            ( (EntityPlayer) entity ).openInventory( this.inventory );
        }
    }

    @Override
    public void toCompound( NBTTagCompound compound, SerializationReason reason ) {
        super.toCompound( compound, reason );
        compound.addValue( "id", "Chest" );

        if ( reason == SerializationReason.PERSIST ) {
            List<NBTTagCompound> nbtTagCompounds = new ArrayList<>();
            for ( int i = 0; i < this.inventory.size(); i++ ) {
                ItemStack itemStack = (ItemStack) this.inventory.getItem( i );
                if ( itemStack != null ) {
                    NBTTagCompound nbtTagCompound = new NBTTagCompound( "" );
                    nbtTagCompound.addValue( "Slot", (byte) i );
                    putItemStack( itemStack, nbtTagCompound );
                    nbtTagCompounds.add( nbtTagCompound );
                }
            }

            compound.addValue( "Items", nbtTagCompounds );
        }
    }

    /**
     * Get this chests inventory
     *
     * @return inventory of this tile
     */
    public ChestInventory getInventory() {
        return this.inventory;
    }

}
