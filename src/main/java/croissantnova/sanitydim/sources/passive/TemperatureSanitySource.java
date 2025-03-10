package croissantnova.sanitydim.sources.passive;

import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.config.ConfigEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import sfiomn.legendarysurvivaloverhaul.api.temperature.TemperatureEnum;
import sfiomn.legendarysurvivaloverhaul.common.capabilities.temperature.TemperatureProvider;

public class TemperatureSanitySource implements IPassiveSanitySource {
    private ResourceLocation dim;
    private float sanityChange;
    private float temperature;

    @Override
    public float get(@NotNull ServerPlayer player, @NotNull ISanity cap, @NotNull ResourceLocation dim) {
        this.dim = dim;
        sanityChange = 0;

        player.getCapability(TemperatureProvider.TEMPERATURE_CAPABILITY).ifPresent(temperatureCapability -> {
            temperature = temperatureCapability.getTemperatureLevel();
            for (TemperatureEnum tempEnum : TemperatureEnum.values()) {
                sanityChange = getTemperatureSanity(tempEnum, getConfigForTemperatureEnum(tempEnum));
                if (sanityChange != 0) {
                    break;
                }
            }
        });

        return sanityChange;
    }

    private float getTemperatureSanity(TemperatureEnum temperatureEnum, ConfigEntry<Float> configEntry) {
        if (temperatureEnum.matches(temperature)) {
            return configEntry.getConfigValue(dim);
        }
        return 0;
    }

    private ConfigEntry<Float> getConfigForTemperatureEnum(TemperatureEnum tempEnum) {
        return switch (tempEnum) {
            case HEAT_STROKE -> ConfigEntry.passiveSanitySource.heatStrokeTemperature;
            case FROSTBITE -> ConfigEntry.passiveSanitySource.frostbiteTemperature;
            case HOT -> ConfigEntry.passiveSanitySource.hotTemperature;
            case COLD -> ConfigEntry.passiveSanitySource.coldTemperature;
            case NORMAL -> ConfigEntry.passiveSanitySource.normalTemperature;
        };
    }
}
