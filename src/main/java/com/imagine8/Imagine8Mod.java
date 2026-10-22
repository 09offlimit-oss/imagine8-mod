package com.imagine8;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Consumer;

public class Imagine8Mod implements ModInitializer {
    public static final String MOD_ID = "imagine8";

    // ---- Tweak these ----
    /** Chance per player, every NIGHT_CHECK_INTERVAL ticks at night, that Imagine8 shows up. */
    public static final double NIGHT_SPAWN_CHANCE = 0.04;
    public static final int NIGHT_CHECK_INTERVAL = 200; // 10 seconds
    /** Despawns after chasing for this long (600 ticks = 30 seconds). */
    public static final int MAX_CHASE_TICKS = 600;

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static SoundEvent sound(String name) {
        ResourceLocation id = id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static final SoundEvent CHASE_LOOP = sound("chase_loop");
    public static final SoundEvent JUMPSCARE = sound("jumpscare");

    public static final EntityType<Imagine8Entity> IMAGINE8 = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, id("imagine8"),
            EntityType.Builder.of(Imagine8Entity::new, MobCategory.MONSTER)
                    .sized(0.8f, 2.4f)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build("imagine8"));

    public static final Item SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM, id("imagine8_spawn_egg"),
            new SpawnEggItem(IMAGINE8, 0x000000, 0xFFFFFF, new Item.Properties()));
    public static final Item CURSED_EIGHT_BALL = Registry.register(BuiltInRegistries.ITEM, id("cursed_eight_ball"),
            new CursedEightBallItem(new Item.Properties().stacksTo(16)));

    /** Set by the client initializer so the common entity class never touches client-only classes. */
    public static Consumer<Imagine8Entity> CLIENT_LOOP_STARTER = e -> {};

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(IMAGINE8, Imagine8Entity.createAttributes());
        PayloadTypeRegistry.playS2C().register(JumpscarePayload.TYPE, JumpscarePayload.CODEC);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(e -> e.accept(SPAWN_EGG));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(e -> e.accept(CURSED_EIGHT_BALL));

        ServerTickEvents.END_WORLD_TICK.register(Imagine8Mod::nightSpawner);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommand(dispatcher));
    }

    private static void nightSpawner(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD) return;
        if (level.getGameTime() % NIGHT_CHECK_INTERVAL != 0) return;
        if (level.isDay()) return;
        if (level.getDifficulty() == Difficulty.PEACEFUL) return;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative()) continue;
            if (level.random.nextDouble() > NIGHT_SPAWN_CHANCE) continue;
            boolean alreadyNearby = !level.getEntitiesOfClass(Imagine8Entity.class, player.getBoundingBox().inflate(96)).isEmpty();
            if (alreadyNearby) continue;
            spawnImagine8(level, player, 24, 34, false);
        }
    }

    /** Spawns Imagine8 somewhere around the player and points it at them. */
    public static boolean spawnImagine8(ServerLevel level, ServerPlayer player, double minDist, double maxDist, boolean summoned) {
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double dist = minDist + level.random.nextDouble() * (maxDist - minDist);
            int x = Mth.floor(player.getX() + Math.cos(angle) * dist);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * dist);
            if (!level.hasChunk(x >> 4, z >> 4)) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (Math.abs(y - player.getY()) > 14) continue;
            if (!level.getFluidState(new BlockPos(x, y - 1, z)).isEmpty()) continue;

            Imagine8Entity entity = IMAGINE8.create(level);
            if (entity == null) return false;
            entity.moveTo(x + 0.5, y, z + 0.5, level.random.nextFloat() * 360f, 0f);
            if (!level.noCollision(entity)) continue;
            entity.setSummoned(summoned);
            entity.setTarget(player);
            level.addFreshEntity(entity);
            return true;
        }
        return false;
    }

    private static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("imagine8")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> summon(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> summon(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
    }

    private static int summon(CommandSourceStack source, ServerPlayer target) {
        if (spawnImagine8(target.serverLevel(), target, 12, 18, true)) {
            source.sendSuccess(() -> Component.literal("Imagine8 has awoken..."), true);
            return 1;
        }
        source.sendFailure(Component.literal("Imagine8 couldn't find room to appear. Try again."));
        return 0;
    }
}
