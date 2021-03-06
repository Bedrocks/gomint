package io.gomint.server.command.gomint;

import io.gomint.GoMint;
import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.annotation.Alias;
import io.gomint.command.annotation.Description;
import io.gomint.command.annotation.Name;
import io.gomint.command.annotation.Permission;
import io.gomint.plugin.Plugin;

import java.util.Map;

/**
 * @author rjworks
 * @author Kaooot
 * @version 1.0
 */
@Name("plugins")
@Description("View plugins enabled on this server.")
@Permission("gomint.command.plugins")
@Alias("pl")
public class PluginsCommand extends Command {

    @Override
    public CommandOutput execute(CommandSender commandSender, String alias, Map<String, Object> arguments) {
        Map<String, Plugin> plugins = GoMint.instance().getPluginManager().getPlugins();
        return CommandOutput.successful(String.join(", ", plugins.keySet()));
    }
}
