package croissantnova.sanitydim.sources.passive;

import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.config.ConfigEntryOld2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import sfiomn.legendarysurvivaloverhaul.common.capabilities.thirst.ThirstProvider;

public class ThirstSanitySource implements IPassiveSanitySource {
    private float sanityChange;

    @Override
    public float get(@NotNull ServerPlayer player, @NotNull ISanity cap, @NotNull ResourceLocation dim) {
        sanityChange = 0;

        player.getCapability(ThirstProvider.THIRST_CAPABILITY).ifPresent(thirstCapability -> {
            int hydrationLevel = thirstCapability.getHydrationLevel();
            if (hydrationLevel <= 6) {
                float value = ConfigEntryOld2.passiveSanitySource.lowThirst.getConfigValue(dim);
                // 100% at 6 hydration, 200% at 0 hydration
                float interpolation = 1f + (6 - hydrationLevel) / 6f;
                sanityChange = value * interpolation;
            }
        });

        return sanityChange;
    }
}
