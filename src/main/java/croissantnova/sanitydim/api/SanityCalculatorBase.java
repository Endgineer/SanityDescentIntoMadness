package croissantnova.sanitydim.api;

import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.config.ConfigManager;
import croissantnova.sanitydim.config.ConfigRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public abstract class SanityCalculatorBase {
    protected final ServerPlayer player;
    protected final ISanity sanityCap;
    protected final ResourceLocation dim;
    protected final ConfigRegistry configValues = ConfigManager.getConfigValues();

    protected SanityCalculatorBase(ServerPlayer player, ISanity sanityCap, ResourceLocation dim) {
        this.player = player;
        this.sanityCap = sanityCap;
        this.dim = dim;
    }

    protected abstract float calculate();
}
