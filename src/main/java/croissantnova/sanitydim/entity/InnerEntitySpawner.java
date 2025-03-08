package croissantnova.sanitydim.entity;

import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.capability.SanityProvider;
import croissantnova.sanitydim.config.ConfigProxy;
import croissantnova.sanitydim.util.PlayerHelper;
import croissantnova.sanitydim.util.TickHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InnerEntitySpawner
{
    private static final RandomSource RANDOM_SOURCE = RandomSource.create();

    public static final int SPAWN_RADIUS = 16;
    public static final int DETECTION_RADIUS = 64;
    public static final int SPAWN_TIMEOUT = TickHelper.fromSeconds(30);

    // Spawning stops at 5 but a rotting stalker can spawn at a score of 4 making it 6 in total
    public static final int ROTTING_STALKER_SPAWN_SCORE = 2;
    public static final int SNEAKING_TERROR_SPAWN_SCORE = 1;
    public static final int MAX_SPAWN_SCORE = 5;

    public static final float SPAWN_THRESHOLD = 0.75f;
    public static final Map<ServerPlayer, Integer> PLAYER_SPAWN_TIMEOUT = new HashMap<>();


    public static float calculateSpawnChance(int seconds) {
        final float threshold = 0.95f; // 95% chance that the entity has spawned
        return 1 - (float) Math.pow(1 - threshold, 1.0 / TickHelper.fromSeconds(seconds));
    }

    public static float getSpawnChance(ResourceLocation dim) {
        return calculateSpawnChance(ConfigProxy.getInnerEntitySpawnChanceSeconds(dim));
    }

    public static @NotNull List<InnerEntity> getInnerEntitiesInRadius(@NotNull Level level, BlockPos blockPos, int radius) {
        return level.getEntitiesOfClass(InnerEntity.class, new AABB(blockPos).inflate(radius));
    }

    // every tick try to spawn an inner entity
    // gives MAX_SPAWN_POS_TRIES to get a valid spawn position for the entity or try all over again next tick
    // SPAWN_CHANCE roughly determines chance to spawn every tick after spawn timeout is over
    public static void trySpawnForPlayer(ServerPlayer player)
    {
        new InnerEntitySpawner(player);
    }





    private final ServerPlayer player;
    private final float spawnChance;

    private InnerEntitySpawner(ServerPlayer player) {
        this.player = player;
        this.spawnChance = getSpawnChance(PlayerHelper.getDim(player));
        trySpawnForPlayer();
    }

    private void trySpawnForPlayer() {
        if (player == null || player.isCreative() || player.isSpectator() || player.level().getDifficulty().equals(Difficulty.PEACEFUL)) {
            return;
        }
        if (!(player.level().isNight() || player.level().isThundering())) {
            return;
        }

        // buffer period between spawns to stop entity spam
        PLAYER_SPAWN_TIMEOUT.putIfAbsent(player, 0);
        int spawnTimeout = PLAYER_SPAWN_TIMEOUT.get(player);
        if (spawnTimeout > 0)
        {
            PLAYER_SPAWN_TIMEOUT.put(player, spawnTimeout - 1);
            return;
        }

        if (RANDOM_SOURCE.nextFloat() > spawnChance) {
            return;
        }

        Optional<ISanity> o = player.getCapability(SanityProvider.CAP).resolve();
        if (o.isEmpty()) {
            return;
        }
        ISanity sanityCapability = o.get();

        if (sanityCapability.getSanity() < SPAWN_THRESHOLD || hasMaxInnerEntities()) {
            return;
        }

        InnerEntity entity = createNewRandomInnerEntity(player);
        if (entity == null) {
            return;
        }

        // adds paranoia since the inner entity may or may not spawn depending on obstructions in environment
        playSpawnAttemptSound();

        BlockPos spawnPos = getRandomSpawnPos();
        if (spawnPos == null) {
            return;
        }

        entity.setPos(new Vec3(spawnPos.getX() + 0.5f, spawnPos.getY() + 0.5f, spawnPos.getZ() + 0.5f));
        if (!entity.checkSpawnObstruction(player.level()) || !player.level().noCollision(entity)) {
            return;
        }

        if (((ServerLevel)player.level()).tryAddFreshEntityWithPassengers(entity)) {
            PLAYER_SPAWN_TIMEOUT.put(player, SPAWN_TIMEOUT);
        }
    }

    private void playSpawnAttemptSound() {
        PlayerHelper.playSound(player, SoundEvents.AMBIENT_CAVE.get(), SoundSource.HOSTILE, 16f, 1f);
    }

    private boolean hasMaxInnerEntities() {
        List<InnerEntity> entities = getInnerEntitiesInRadius(player.level(), player.blockPosition(), DETECTION_RADIUS);
        int score = 0;
        for (InnerEntity entity : entities) {
            if (entity instanceof RottingStalker) {
                score += ROTTING_STALKER_SPAWN_SCORE;
            }
            else if (entity instanceof SneakingTerror) {
                score += SNEAKING_TERROR_SPAWN_SCORE;
            }
        }
        return score >= MAX_SPAWN_SCORE;
    }




    private int getHeightForSpawning(Level level, @NotNull BlockPos blockPos) {
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        for (int i = 0; i < SPAWN_RADIUS; i++) {
            if (!level.getBlockState(mutable).isAir() && level.getBlockState(mutable.move(Direction.UP)).isAir())
            {
                return mutable.getY();
            }
        }
        for (int i = 0; i < SPAWN_RADIUS; i++)
        {
            if (level.getBlockState(mutable).isAir() && !level.getBlockState(mutable.move(Direction.DOWN)).isAir())
            {
                return mutable.getY() - 1;
            }
        }
        return 0;
    }

    private InnerEntity createNewRandomInnerEntity(@NotNull ServerPlayer player) {
        int index = RANDOM_SOURCE.nextInt(EntityRegistry.INNER_ENTITIES.size());
        return EntityRegistry.INNER_ENTITIES.get(index).get().create(player.level());
    }

    private @Nullable BlockPos getRandomSpawnPos() {
        BlockPos playerPos = player.blockPosition();
        ArrayList<BlockPos> possiblePositions = getPossiblePositions(playerPos);

        Collections.shuffle(possiblePositions, new Random());

        for (BlockPos trialPos : possiblePositions) {
            int spawnHeight = getHeightForSpawning(player.level(), trialPos);
            if (spawnHeight != 0) {
                return new BlockPos(trialPos.getX(), spawnHeight, trialPos.getZ());
            }
        }

        return null;
    }

    private static @NotNull ArrayList<BlockPos> getPossiblePositions(@NotNull BlockPos playerPos) {
        int centerX = playerPos.getX();
        int centerY = playerPos.getY();
        int centerZ = playerPos.getZ();

        ArrayList<BlockPos> possiblePositions = new ArrayList<>();

        for (int x = centerX - SPAWN_RADIUS; x <= centerX + SPAWN_RADIUS; x++) {
            for (int z = centerZ - SPAWN_RADIUS; z <= centerZ + SPAWN_RADIUS; z++) {
                possiblePositions.add(new BlockPos(x, centerY, z));
            }
        }
        return possiblePositions;
    }
}