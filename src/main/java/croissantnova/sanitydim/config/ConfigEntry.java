package croissantnova.sanitydim.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public enum ConfigEntry {
    INNER_ENTITY_DESPAWN_MOBS_DISTANCE(
            "sanity.entity.despawn_nearby_mobs_distance",
            new ConfigManager.ProxyValueEntry<>(() -> ConfigManager.getDefault().m_innerEntitiesDespawnNearbyMobsDistance.get(), ConfigManager::noFinalize)
    ),
    ROTTING_STALKER_KILL_RATIO(
            "sanity.active.rotting_stalker_kill_ratio",
            new ConfigManager.ProxyValueEntry<>(() -> ConfigManager.getDefault().m_rottingStalkerKillRatio.get(), ConfigManager::finalizeActive)
    ),
    SNEAKING_TERROR_KILL_RATIO(
            "sanity.active.sneaking_terror_kill_ratio",
            new ConfigManager.ProxyValueEntry<>(() -> ConfigManager.getDefault().m_sneakingTerrorKillRatio.get(), ConfigManager::finalizeActive)
    ),
    ;

    private final String proxyKey;
    private final ConfigManager.ProxyValueEntry<?> proxyValueEntry;

    ConfigEntry(String proxyKey, ConfigManager.ProxyValueEntry<?> proxyValueEntry) {
        this.proxyKey = proxyKey;
        this.proxyValueEntry = proxyValueEntry;
    }

    public String getProxyKey() {
        return proxyKey;
    }


    public void putInProxies(@NotNull Map<String, ConfigManager.ProxyValueEntry<?>> proxies) {
        proxies.put(proxyKey, proxyValueEntry);
    }

    public void configSpecBuilder(ForgeConfigSpec.Builder builder) {

    }
}
