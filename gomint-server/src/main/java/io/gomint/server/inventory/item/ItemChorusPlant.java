package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:chorus_plant", id = 240)
public class ItemChorusPlant extends ItemStack implements io.gomint.inventory.item.ItemChorusPlant {

    @Override
    public ItemType getItemType() {
        return ItemType.CHORUS_PLANT;
    }

}
