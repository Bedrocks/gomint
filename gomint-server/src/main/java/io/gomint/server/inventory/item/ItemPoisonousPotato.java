package io.gomint.server.inventory.item;
import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:poisonous_potato", id = 394 )
public class ItemPoisonousPotato extends ItemFood implements io.gomint.inventory.item.ItemPoisonousPotato {



    @Override
    public float getSaturation() {
        return 0.3f;
    }

    @Override
    public float getHunger() {
        return 2;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.POISONOUS_POTATO;
    }

}