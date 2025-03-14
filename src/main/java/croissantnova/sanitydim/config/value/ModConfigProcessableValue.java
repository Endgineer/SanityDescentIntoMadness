package croissantnova.sanitydim.config.value;

import croissantnova.sanitydim.config.ConfigManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @param <T> The type of configuration value; e.g. {@code List<? extends String>}
 * @param <P> The processed type resulting from processing the configuration value; e.g. {@code List<PassiveSanityEntity>}
 */
public abstract class ModConfigProcessableValue<T, P> {
    public static final List<ModConfigProcessableValue<?, ?>> CONFIG_VALUES = new ArrayList<>();

    private final String proxyKey;
    private final Function<P, P> finalizer;
    protected ForgeConfigSpec.ConfigValue<T> configValue;

    public ModConfigProcessableValue(String proxyKey, Function<P, P> finalizer) {
        CONFIG_VALUES.add(this);
        this.proxyKey = proxyKey;
        this.finalizer = finalizer;
    }

    /**
     * Needs to initialize {@link ModConfigProcessableValue#configValue} through a definition call
     */
    public abstract void build(ForgeConfigSpec.Builder builder);

    public abstract P process();

    public void loadProxy(@NotNull Map<String, ConfigManager.ProxyValueEntry<?>> proxies) {
        proxies.put(proxyKey, new ConfigManager.ProxyValueEntry<>(
                this::process,
                finalizer
        ));
    }

    public P getValue(ResourceLocation dim) {
        return ConfigManager.proxy(proxyKey, dim);
    }
}
