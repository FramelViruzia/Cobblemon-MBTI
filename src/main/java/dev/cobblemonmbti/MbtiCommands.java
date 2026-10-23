package dev.cobblemonmbti;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

/** /mbti reload  - re-read the config file.   /mbti dump  - write config/cobblemon_mbti_report.txt */
public final class MbtiCommands {
    private MbtiCommands() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommandManager.literal("mbti")
                .then(ClientCommandManager.literal("reload").executes(ctx -> {
                    MbtiConfig.reload();
                    MbtiBadgeRenderer.resetFailure();
                    ctx.getSource().sendFeedback(Component.literal("[MBTI] config reloaded"));
                    return Command.SINGLE_SUCCESS;
                }))
                .then(ClientCommandManager.literal("dump").executes(ctx -> {
                    ctx.getSource().sendFeedback(Component.literal("[MBTI] " + MbtiDump.writeReport()));
                    return Command.SINGLE_SUCCESS;
                })));
    }
}
