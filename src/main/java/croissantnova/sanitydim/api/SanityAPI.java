package croissantnova.sanitydim.api;

import croissantnova.sanitydim.capability.SanityProvider;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SanityAPI {

    public static final int MAX_SANITY = 100;
    public static final int MIN_SANITY = -100;


    public static void setSanity(@NotNull ServerPlayer player, float sanity) {
        player.getCapability(SanityProvider.CAP).ifPresent(cap -> {
            cap.setSanity(sanity);
        });
    }

    public static float getSanity(@NotNull ServerPlayer player) {
        AtomicReference<Float> sanity = new AtomicReference<>(0f);

        player.getCapability(SanityProvider.CAP).ifPresent(cap -> {
            sanity.set(cap.getSanity());
        });

        return sanity.get();
    }

    public static void addSanity(@NotNull ServerPlayer player, float sanity) {
        setSanity(player, getSanity(player) + sanity);
    }
}
