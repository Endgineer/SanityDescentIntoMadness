package croissantnova.sanitydim.sources.passive.compat;

import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.compat.ColdSweatCompatAPI;
import croissantnova.sanitydim.config.ConfigProxy;
import croissantnova.sanitydim.sources.passive.IPassiveSanitySource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.common.entity.data.Preference;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TemperatureSanitySource implements IPassiveSanitySource {
    @Override
    public float get(@NotNull ServerPlayer player, @NotNull ISanity cap, @NotNull ResourceLocation dim) {
        if (ColdSweatCompatAPI.isModLoaded() && EntityTempManager.isTemperatureEnabled(player)) {
            Temperature.Units preferredUnits = CSMath.getIfNotNull(player, p -> Preference.getOrDefault(p, Preference.UNITS, Temperature.Units.F), Temperature.Units.F);
            double temp = CSMath.truncate(Temperature.convertIfNeeded(Temperature.get((LivingEntity) player, Temperature.Trait.BODY), Temperature.Trait.BODY, preferredUnits), 2);
            return Math.abs(temp) >= 50 ? ConfigProxy.getColdSweaty(dim) : 0f;
        }
        
        return 0f;
    }
}
