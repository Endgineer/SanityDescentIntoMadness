package croissantnova.sanitydim.sources.passive;

import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.config.ConfigManager;
import croissantnova.sanitydim.util.PlayerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class SunlightSanitySource implements IPassiveSanitySource {

    @Override
    public float get(@NotNull ServerPlayer player, @NotNull ISanity cap, @NotNull ResourceLocation dim) {
        return PlayerHelper.isPlayerInSunlight(player) ?
                ConfigManager.getConfigValues().passive_sunlight.get(dim).floatValue() :
                0f;
    }
}
