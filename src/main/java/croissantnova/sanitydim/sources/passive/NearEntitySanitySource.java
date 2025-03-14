package croissantnova.sanitydim.sources.passive;

import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.config.ConfigManager;
import croissantnova.sanitydim.config.custom.PassiveSanityEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NearEntitySanitySource implements IPassiveSanitySource {
    private ResourceLocation dim;
    private float sanityChange;

    @Override
    public float get(@NotNull ServerPlayer player, @NotNull ISanity cap, @NotNull ResourceLocation dim) {
        this.dim = dim;
        sanityChange = 0;


        List<PassiveSanityEntity> passiveSanityEntityList = ConfigManager.getConfigValues().passive_sanityEntities.getValue(dim);
        double distance = ConfigManager.getConfigValues().passive_endermanDistance.getValue(dim);

        AABB searchArea = player.getBoundingBox().inflate(distance);
        List<EnderMan> nearbyEndermen = player.level().getEntitiesOfClass(EnderMan.class, searchArea);
        for (EnderMan enderman : nearbyEndermen) {
            // TODO: make endermen change sanity exposure (accounting for angry endermen as well)
        }

        return sanityChange;
    }
}
