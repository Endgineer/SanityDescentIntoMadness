package croissantnova.sanitydim.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public class PlayerHelper {
    private static final RandomSource rng = RandomSource.create();

    public static boolean isHoldingSword(ServerPlayer player) {
        ItemStack mainHandItem = player.getMainHandItem();
        return mainHandItem.getItem() instanceof SwordItem;
    }

    public static ResourceLocation getDim(ServerPlayer player) {
        return player.level().dimension().location();
    }

    @SuppressWarnings("deprecation")
    public static float getSolarLightLevel(ServerPlayer player) {
        return player.getLightLevelDependentMagicValue();
    }

    /**
     * Uses similar code to {@code net.minecraft.world.entity.Mob#isSunBurnTick()}
     */
    public static boolean isPlayerInSunlight(ServerPlayer player) {
        if (player.level().isDay() && !player.level().isClientSide) {
            float solarLightLevel = PlayerHelper.getSolarLightLevel(player);
            BlockPos blockpos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
            
            return solarLightLevel > 0.5F && player.level().canSeeSky(blockpos);
        }
        return false;
    }

    // Side Effects

    public static void playSound(ServerPlayer player, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        Holder<SoundEvent> soundHolder = Holder.direct(SoundEvent.createVariableRangeEvent(soundEvent.getLocation()));
        player.connection.send(new ClientboundSoundPacket(
                soundHolder,
                soundSource,
                player.getX(),
                player.getY(),
                player.getZ(),
                volume,
                pitch,
                rng.nextLong()
        ));
    }
}
