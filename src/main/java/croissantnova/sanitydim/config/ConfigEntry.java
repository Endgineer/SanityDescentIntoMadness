package croissantnova.sanitydim.config;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ConfigEntry<T> {
    public static List<ConfigEntry<?>> configEntries = new ArrayList<>();

    public final String proxyKey;
    private final ConfigManager.ProxyValueEntry<?> proxyValueEntry;

    private ConfigEntry(String proxyKey, ConfigManager.ProxyValueEntry<?> proxyValueEntry) {
        this.proxyKey = proxyKey;
        this.proxyValueEntry = proxyValueEntry;
        configEntries.add(this);
    }

    public void putInProxies(@NotNull Map<String, ConfigManager.ProxyValueEntry<?>> proxies) {
        proxies.put(proxyKey, proxyValueEntry);
    }

    public abstract T getConfigValue(ResourceLocation dim);



    private static <T> @NotNull ConfigEntry<Float> createFloat(String proxyKey, Supplier<T> getter, Function<T, T> finalizer) {
        return new ConfigEntry<Float>(proxyKey, new ConfigManager.ProxyValueEntry<>(getter, finalizer)) {
            @Override
            public Float getConfigValue(ResourceLocation dim) {
                return ConfigManager.proxyd2f(this.proxyKey, dim);
            }
        };
    }

    public static final ActiveSanitySource activeSanitySource = new ActiveSanitySource();
    public static final PassiveSanitySource passiveSanitySource = new PassiveSanitySource();

    public static class ActiveSanitySource {
        private static @NotNull ConfigEntry<Float> createFloatActive(String proxyKey, Supplier<Double> getter) {
            return createFloat(proxyKey, getter, ConfigManager::finalizeActive);
        }

        public final ConfigEntry<Float> killedSneakingTerror = createFloatActive(
                "sanity.active.killed_sneaking_terror",
                () -> ConfigManager.getConfigValues().m_sneakingTerrorKillRatio.get()
        );
    }

    public static class PassiveSanitySource {
        private static @NotNull ConfigEntry<Float> createFloatPassive(String proxyKey, Supplier<Double> getter) {
            return createFloat(proxyKey, getter, ConfigManager::finalizePassive);
        }

        public final ConfigEntry<Float> bodyPartSlightlyWounded = createFloatPassive(
                "sanity.passive.body_part_slightly_wounded",
                () -> ConfigManager.getConfigValues().passiveSanitySource.bodyPartSlightlyWounded.get()
        );
        public final ConfigEntry<Float> bodyPartWounded = createFloatPassive(
                "sanity.passive.body_part_wounded",
                () -> ConfigManager.getConfigValues().passiveSanitySource.bodyPartWounded.get()
        );
        public final ConfigEntry<Float> bodyPartHeavilyWounded = createFloatPassive(
                "sanity.passive.body_part_heavily_wounded",
                () -> ConfigManager.getConfigValues().passiveSanitySource.bodyPartHeavilyWounded.get()
        );
        public final ConfigEntry<Float> bodyPartDead = createFloatPassive(
                "sanity.passive.body_part_dead",
                () -> ConfigManager.getConfigValues().passiveSanitySource.bodyPartDead.get()
        );

        public final ConfigEntry<Float> lowThirst = createFloatPassive(
                "sanity.passive.low_thirst",
                () -> ConfigManager.getConfigValues().passiveSanitySource.lowHydration.get()
        );

        public final ConfigEntry<Float> heatStrokeTemperature = createFloatPassive(
                "sanity.passive.heat_stroke_temperature",
                () -> ConfigManager.getConfigValues().passiveSanitySource.heatStrokeTemperature.get()
        );
        public final ConfigEntry<Float> frostbiteTemperature = createFloatPassive(
                "sanity.passive.frostbite_temperature",
                () -> ConfigManager.getConfigValues().passiveSanitySource.frostbiteTemperature.get()
        );
        public final ConfigEntry<Float> hotTemperature = createFloatPassive(
                "sanity.passive.hot_temperature",
                () -> ConfigManager.getConfigValues().passiveSanitySource.hotTemperature.get()
        );
        public final ConfigEntry<Float> coldTemperature = createFloatPassive(
                "sanity.passive.cold_temperature",
                () -> ConfigManager.getConfigValues().passiveSanitySource.coldTemperature.get()
        );
        public final ConfigEntry<Float> normalTemperature = createFloatPassive(
                "sanity.passive.normal_temperature",
                () -> ConfigManager.getConfigValues().passiveSanitySource.normalTemperature.get()
        );
    }
}
