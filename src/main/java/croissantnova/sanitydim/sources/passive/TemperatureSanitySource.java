package croissantnova.sanitydim.sources.passive;

import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.config.ConfigEntryOld2;
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
            for (TemperatureEnum temperatureStage : TemperatureEnum.values()) {
                sanityChange = getTemperatureSanity(temperatureStage, getConfigForTemperatureEnum(temperatureStage));
                if (sanityChange != 0) {
                    break;
                }
            }
        });

        return sanityChange;
    }

    private float getTemperatureSanity(@NotNull TemperatureEnum temperatureStage, ConfigEntryOld2<Float> configEntryOld2) {
        if (temperatureStage.matches(temperature)) {
            return configEntryOld2.getConfigValue(dim);
        }
        return 0;
    }

    private ConfigEntryOld2<Float> getConfigForTemperatureEnum(@NotNull TemperatureEnum tempEnum) {
        return switch (tempEnum) {
            case HEAT_STROKE -> ConfigEntryOld2.passiveSanitySource.heatStrokeTemperature;
            case FROSTBITE -> ConfigEntryOld2.passiveSanitySource.frostbiteTemperature;
            case HOT -> ConfigEntryOld2.passiveSanitySource.hotTemperature;
            case COLD -> ConfigEntryOld2.passiveSanitySource.coldTemperature;
            case NORMAL -> ConfigEntryOld2.passiveSanitySource.normalTemperature;
        };
    }
}
