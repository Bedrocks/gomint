package io.gomint.server.plugin;

import io.gomint.GoMint;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author geNAZt
 * @version 1.0
 */
public class PluginClassloader extends URLClassLoader {

    private static final Set<PluginClassloader> allLoaders = new CopyOnWriteArraySet<>();
    private static ClassLoader applicationClassloader;

    static {
        ClassLoader.registerAsParallelCapable();

        applicationClassloader = GoMint.class.getClassLoader();
    }

    public PluginClassloader( URL[] urls ) {
        super( urls );
        allLoaders.add( this );
    }

    @Override
    protected Class<?> loadClass( String name, boolean resolve ) throws ClassNotFoundException {
        return loadClass0( name, resolve, true );
    }

    private Class<?> loadClass0( String name, boolean resolve, boolean checkOther ) throws ClassNotFoundException {
        try {
            return super.loadClass( name, resolve );
        } catch ( ClassNotFoundException ex ) {
            // Ignored
        }

        if ( checkOther ) {
            for ( PluginClassloader loader : allLoaders ) {
                if ( loader != this ) {
                    try {
                        return loader.loadClass0( name, resolve, false );
                    } catch ( ClassNotFoundException ex ) {
                        // Ignored
                    }
                }
            }

            try {
                return applicationClassloader.loadClass( name );
            } catch ( ClassNotFoundException ex ) {
                // Ignored
            }
        }

        throw new ClassNotFoundException( name );
    }

    public void remove() {
        allLoaders.remove( this );

        try {
            super.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

}
