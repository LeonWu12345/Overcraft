package com.jam8ee.overcraft.core.gameplay.hero;

import com.jam8ee.overcraft.OvercraftMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = OvercraftMod.MODID)
public final class HeroCommands {
    private HeroCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("hero")
                        // 0 = 所有人可用；如果你想只给 OP：.requires(src -> src.hasPermission(2))
                        .requires(src -> src.hasPermission(0))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(HeroId.values()).map(HeroId::id),
                                        builder
                                ))
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    ServerPlayer player = source.getPlayerOrException();

                                    String raw = StringArgumentType.getString(ctx, "id");
                                    HeroId hero = HeroId.fromString(raw);

                                    HeroLoadoutApplier.switchHero(player, hero);

                                    source.sendSuccess(
                                            () -> Component.literal("Switched hero to: " + hero.id()),
                                            true
                                    );
                                    return 1;
                                }))
        );
    }
}
