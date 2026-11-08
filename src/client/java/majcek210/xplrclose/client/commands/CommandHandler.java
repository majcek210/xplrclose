package majcek210.xplrclose.client.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import majcek210.xplrclose.client.config.XplrcloseConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

public class CommandHandler {

    private CommandHandler() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, ctx) ->
                dispatcher.register(literal("xplrclose")
                        .then(literal("ignored").executes(c -> {
                            var list = XplrcloseConfig.getIgnoredPlayers();
                            if (list.isEmpty()) {
                                c.getSource().sendFeedback(Component.literal("Nobody is currently ignored!"));
                            } else {
                                c.getSource().sendFeedback(Component.literal(
                                        "Ignored (" + list.size() + "): " + String.join(", ", list.values())
                                ));
                            }
                            return 1;
                        }))
                        .then(literal("unignore")
                            .then(argument("name", StringArgumentType.word())
                                .suggests((c, b) -> {
                                    XplrcloseConfig.getIgnoredPlayers().values().forEach(b::suggest);
                                    return b.buildFuture();
                                })
                                    .executes(c -> {
                                        String name = StringArgumentType.getString(c, "name");
                                        String removed = XplrcloseConfig.uningnorePlayerByName(name);

                                        if (removed == null) {
                                            c.getSource().sendError(Component.literal(name + " is currently not ignored!"));
                                            return 0;
                                        }

                                        c.getSource().sendFeedback(Component.literal("No longer ignoring " + removed));
                                        return 1;
                                    }))
        )));
    }
}
