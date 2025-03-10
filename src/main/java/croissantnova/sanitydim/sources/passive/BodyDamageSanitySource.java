package croissantnova.sanitydim.sources.passive;

import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.config.ConfigEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import sfiomn.legendarysurvivaloverhaul.api.bodydamage.BodyDamageUtil;
import sfiomn.legendarysurvivaloverhaul.api.bodydamage.BodyPartEnum;

public class BodyDamageSanitySource implements IPassiveSanitySource {

    @Override
    public float get(@NotNull ServerPlayer player, @NotNull ISanity cap, @NotNull ResourceLocation dim) {
        float sanityAmount = 0;

        for (BodyPartEnum bodyPart : BodyPartEnum.values()) {
            float healthRatio = BodyDamageUtil.getHealthRatio(player, bodyPart);

            sanityAmount += getBodyPartSanityLoss(healthRatio, bodyPart, dim);
        }

        return sanityAmount;
    }

    private float getBodyPartSanityLoss(float healthRatio, BodyPartEnum bodyPart, ResourceLocation dim) {
        if (healthRatio >= 1f) {
            return 0f;
        }
        else if (healthRatio >= 0.66f) {
            return ConfigEntry.passiveSanitySource.bodyPartSlightlyWounded.getConfigValue(dim);
        } else if (healthRatio >= 0.33f) {
            return ConfigEntry.passiveSanitySource.bodyPartWounded.getConfigValue(dim);
        } else if (healthRatio > 0f) {
            return ConfigEntry.passiveSanitySource.bodyPartHeavilyWounded.getConfigValue(dim);
        } else {
            return ConfigEntry.passiveSanitySource.bodyPartDead.getConfigValue(dim);
        }
    }
}
