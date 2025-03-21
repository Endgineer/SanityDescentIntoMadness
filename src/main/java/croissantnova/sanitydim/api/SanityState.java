package croissantnova.sanitydim.api;

import croissantnova.sanitydim.util.MathHelper;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public enum SanityState {
    SANE(0f),
    PARANOID(0.4f),
    INSANE(0.7f);

    private final float min;

    SanityState(float min) {
        this.min = min;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return this.ordinal() < values().length - 1
                ? values()[this.ordinal() + 1].min
                : Float.MAX_VALUE;
    }

    /**
     * Checks whether the player's current sanity level falls within the range defined by this {@code SanityState}.
     *
     * @param player The player whose sanity level is being checked.
     * @return {@code true} if the player's sanity level is between the minimum and maximum thresholds
     *         of this {@code SanityState}, inclusive of the lower bound and exclusive of the upper bound;
     *         {@code false} otherwise.
     */
    public boolean isAtState(@NotNull Player player) {
        float sanity = SanityAPI.getSanity(player);
        float max = getMax();
        return MathHelper.isWithin(sanity, min, max);
    }

    /**
     * Checks if the player meets the minimum sanity threshold for this {@code SanityState}.
     *
     * @param player The player whose sanity level is being checked.
     * @return {@code true} if the player's sanity level is greater than or equal to the minimum threshold for this {@code SanityState}, otherwise {@code false}.
     */
    public boolean isAtStateOrHigher(@NotNull Player player) {
        return SanityAPI.getSanity(player) >= min;
    }

    /**
     * Checks if the player's current sanity level is below the minimum threshold of this {@code SanityState}.
     *
     * @param player The player whose sanity level is being checked.
     * @return {@code true} if the player's sanity level is below the minimum threshold of this {@code SanityState}, otherwise {@code false}.
     */
    public boolean isBelowState(@NotNull Player player) {
        return SanityAPI.getSanity(player) < min;
    }

    /**
     * Checks if the player's current sanity level falls within the specified {@link SanityState}.
     *
     * @param player The player whose sanity level is being checked.
     * @param state  The sanity state range to check against.
     * @return {@code true} if the player's sanity level falls within the specified sanity state range, otherwise {@code false}.
     */
    public static boolean is(@NotNull Player player, @NotNull SanityState state) {
        return state.isAtState(player);
    }
}
