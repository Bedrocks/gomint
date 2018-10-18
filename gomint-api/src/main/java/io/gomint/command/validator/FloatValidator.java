package io.gomint.command.validator;

import io.gomint.command.CommandSender;
import io.gomint.command.ParamType;
import io.gomint.command.ParamValidator;

import java.util.Iterator;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
public class FloatValidator extends ParamValidator {

    @Override
    public ParamType getType() {
        return ParamType.FLOAT;
    }

    @Override
    public boolean hasValues() {
        return false;
    }

    @Override
    public List<String> values() {
        return null;
    }

    @Override
    public Object validate( String input, CommandSender commandSender ) {
        try {
            return Float.parseFloat( input );
        } catch ( NumberFormatException e ) {
            return null;
        }
    }

    @Override
    public String getHelpText() {
        return "float";
    }

    @Override
    public String consume( Iterator<String> data ) {
        if ( data.hasNext() ) {
            return data.next();
        }

        return null;
    }

}
