package croissantnova.sanitydim.sources.passive.compat;

import croissantnova.sanitydim.sources.SanityCalculatorBase;
import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.compat.ThirstWasTakenCompatAPI;
import croissantnova.sanitydim.config.ConfigManager;
import croissantnova.sanitydim.config.ConfigProxy;
import croissantnova.sanitydim.config.registry.ConfigRegistry;
import croissantnova.sanitydim.sources.passive.IPassiveSanitySource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;

public class ThirstSanitySource implements IPassiveSanitySource {
    @Override
    public float get(@NotNull ServerPlayer player, @NotNull ISanity sanityCap, @NotNull ResourceLocation dim) {
        if (ThirstWasTakenCompatAPI.isModLoaded()) {
            return player.getCapability(ModCapabilities.PLAYER_THIRST).map(thirstCapability -> {
                ConfigRegistry config = ConfigManager.getConfigValues();
                int thirstLevel = thirstCapability.getThirst();
                
                if (thirstLevel <= ConfigProxy.getThirstThreshold(dim)) {
                    return ConfigProxy.getThirsty(dim);
                }
                
                if (thirstLevel >= config.passiveConfig.wellHydratedThreshold.get(dim)) {
                    return config.passiveConfig.wellHydrated.get(dim).floatValue();
                }
                
                return 0f;
            }).orElse(0f);
        }
        
        return 0f;
    }
}
