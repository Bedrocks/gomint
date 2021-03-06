package io.gomint.entity.monster;

import io.gomint.GoMint;
import io.gomint.entity.EntityLiving;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public interface EntityCod extends EntityLiving {

    /**
     * Create a new cod with no config
     *
     * @return empty, fresh cod
     */
    static EntityCod create() {
        return GoMint.instance().createEntity( EntityCod.class );
    }

}
