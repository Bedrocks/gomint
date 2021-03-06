package io.gomint.entity.passive;

import io.gomint.GoMint;
import io.gomint.entity.EntityLiving;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public interface EntityOcelot extends EntityLiving {

    /**
     * Create a new entity ocelot with no config
     *
     * @return empty, fresh ocelot
     */
    static EntityOcelot create() {
        return GoMint.instance().createEntity( EntityOcelot.class );
    }

}
