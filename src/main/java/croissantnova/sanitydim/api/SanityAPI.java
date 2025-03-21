package croissantnova.sanitydim.api;

import croissantnova.sanitydim.capability.SanityProvider;
import croissantnova.sanitydim.config.ConfigProxy;
import croissantnova.sanitydim.util.PlayerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class SanityAPI {

    public static final int MAX_SANITY = 100;
    public static final int MIN_SANITY = -100;
    public static final int MAX_SANITY_INTERNAL = 1;
    public static final int MIN_SANITY_INTERNAL = 0;


    public static void setSanity(@NotNull Player player, float sanity) {
        player.getCapability(SanityProvider.CAP).ifPresent(cap -> {
            cap.setSanity(sanity);
        });
    }

    public static float getSanity(@NotNull Player player) {
        AtomicReference<Float> sanity = new AtomicReference<>(0f);

        player.getCapability(SanityProvider.CAP).ifPresent(cap -> {
            sanity.set(cap.getSanity());
        });

        return sanity.get();
    }

    public static void addSanity(@NotNull Player player, float sanity) {
        setSanity(player, getSanity(player) + sanity);
    }


    // Booleans

    public static boolean bypassesNightmareInvisibilityToSane(Player player) {
        return ConfigProxy.canSaneSeeNightmares(player.level().dimension().location());
    }

    public static boolean canSeeNightmares(@NotNull Player player) {
        return PlayerHelper.isImmortal(player)
                || bypassesNightmareInvisibilityToSane(player)
                || SanityState.PARANOID.isAtStateOrHigher(player);
    }

    public static boolean canBeTargetedByNightmares(@NotNull Player player) {
        return PlayerHelper.isMortal(player)
                && SanityState.INSANE.isAtStateOrHigher(player);
    }

    public static boolean isNightmareTime(Level level) {
        return level.isNight() || level.isThundering();
    }
}
