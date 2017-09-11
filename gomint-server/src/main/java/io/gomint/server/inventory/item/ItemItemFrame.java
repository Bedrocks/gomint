package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 199 )
 public class ItemItemFrame extends ItemStack implements io.gomint.inventory.item.ItemItemFrame {

    // CHECKSTYLE:OFF
    public ItemItemFrame( short data, int amount ) {
        super( 199, data, amount );
    }

    public ItemItemFrame( short data, int amount, NBTTagCompound nbt ) {
        super( 199, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}