package io.gomint.command.validator;

import com.google.common.base.Joiner;
import io.gomint.command.CommandSender;
import io.gomint.command.ParamType;
import io.gomint.command.ParamValidator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
public class EnumValidator extends ParamValidator {

    private final List<String> values = new ArrayList<>();

    public EnumValidator( List<String> enumValues ) {
        if ( enumValues != null ) {
            this.values.addAll( enumValues );
        }
    }

    @Override
    public ParamType getType() {
        return ParamType.STRING_ENUM;
    }

    @Override
    public boolean hasValues() {
        return true;
    }

    @Override
    public List<String> values() {
        return this.values;
    }

    @Override
    public Object validate( String input, CommandSender commandSender ) {
        if ( this.values.contains( input ) ) {
            return input;
        }

        return null;
    }

    @Override
    public String consume( Iterator<String> data ) {
        if ( data.hasNext() ) {
            return data.next();
        }

        return null;
    }

    @Override
    public String getHelpText() {
        return Joiner.on( " | " ).join( this.values );
    }

}
