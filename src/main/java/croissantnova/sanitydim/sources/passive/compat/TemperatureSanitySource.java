package croissantnova.sanitydim.sources.passive.compat;

import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.compat.ColdSweatCompatAPI;
import croissantnova.sanitydim.config.ConfigProxy;
import croissantnova.sanitydim.sources.passive.IPassiveSanitySource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;

import java.util.Map;

public class TemperatureSanitySource implements IPassiveSanitySource {
    @Override
    public float get(@NotNull ServerPlayer player, @NotNull ISanity cap, @NotNull ResourceLocation dim) {
        if (ColdSweatCompatAPI.isModLoaded()) {
            return EntityTempManager.getTemperatureCap(player).map(temperatureCapability -> {
                Map<TempEffectType<?>, TempEffect> tempEffects = temperatureCapability.getTempEffects();
                return tempEffects.size() > 0 ? ConfigProxy.getColdSweaty(dim) : 0f;
            }).orElse(0f);
        }
        
        return 0f;
    }
}
