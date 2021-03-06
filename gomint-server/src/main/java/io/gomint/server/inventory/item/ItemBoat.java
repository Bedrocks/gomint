package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:boat", id = 333 )
public class ItemBoat extends ItemStack implements io.gomint.inventory.item.ItemBoat {

    @Override
    public ItemType getItemType() {
        return ItemType.BOAT;
    }

    @Override
    public long getBurnTime() {
        return 60000;
    }

}