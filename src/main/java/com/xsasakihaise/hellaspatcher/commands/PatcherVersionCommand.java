// java
package com.xsasakihaise.hellaspatcher.commands;

import com.xsasakihaise.hellaspatcher.HellasPatcherInfoConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class PatcherVersionCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher, HellasPatcherInfoConfig infoConfig) {
        dispatcher.register(
                Commands.literal("hellas")
                        .then(Commands.literal("patcher")
                                .then(Commands.literal("version")
                                        .executes(ctx -> {
                                            if (!infoConfig.isValid()) {
                                                ctx.getSource().sendSuccess(new StringTextComponent("Fehler: HellasPatcher-Info nicht geladen (fehlende oder ungültige JSON)."), false);
                                                return 0;
                                            }
                                            ctx.getSource().sendSuccess(new StringTextComponent("-----------------------------------"), false);
                                            ctx.getSource().sendSuccess(new StringTextComponent("Version: " + infoConfig.getVersion()), false);
                                            ctx.getSource().sendSuccess(new StringTextComponent("-----------------------------------"), false);
                                            return 1;
                                        })
                                )
                        )
        );
    }
}