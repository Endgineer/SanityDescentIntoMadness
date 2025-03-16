package croissantnova.sanitydim.util;

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
