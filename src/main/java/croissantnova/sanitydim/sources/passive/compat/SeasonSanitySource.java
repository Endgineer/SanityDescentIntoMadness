package croissantnova.sanitydim.sources.passive.compat;

import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.compat.SereneSeasonsCompatAPI;
import croissantnova.sanitydim.config.ConfigProxy;
import croissantnova.sanitydim.sources.passive.IPassiveSanitySource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import sereneseasons.season.SeasonHandler;
import sereneseasons.season.SeasonSavedData;
import sereneseasons.season.SeasonTime;
import sereneseasons.api.season.Season.SubSeason;

public class SeasonSanitySource implements IPassiveSanitySource {
    @Override
    public float get(@NotNull ServerPlayer player, @NotNull ISanity cap, @NotNull ResourceLocation dim) {
        if (SereneSeasonsCompatAPI.isModLoaded()) {
            SeasonSavedData seasonData = SeasonHandler.getSeasonSavedData(player.level());
            SeasonTime time = new SeasonTime(seasonData.seasonCycleTicks);
            SubSeason subSeason = time.getSubSeason();
            switch (subSeason) {
                case EARLY_SPRING:
                    return ConfigProxy.getEarlySpring(dim);
                case MID_SPRING:
                    return ConfigProxy.getMidSpring(dim);
                case LATE_SPRING:
                    return ConfigProxy.getLateSpring(dim);
                case EARLY_SUMMER:
                    return ConfigProxy.getEarlySummer(dim);
                case MID_SUMMER:
                    return ConfigProxy.getMidSummer(dim);
                case LATE_SUMMER:
                    return ConfigProxy.getLateSummer(dim);
                case EARLY_AUTUMN:
                    return ConfigProxy.getEarlyAutumn(dim);
                case MID_AUTUMN:
                    return ConfigProxy.getMidAutumn(dim);
                case LATE_AUTUMN:
                    return ConfigProxy.getLateAutumn(dim);
                case EARLY_WINTER:
                    return ConfigProxy.getEarlyWinter(dim);
                case MID_WINTER:
                    return ConfigProxy.getMidWinter(dim);
                case LATE_WINTER:
                    return ConfigProxy.getLateWinter(dim);
            }
        }
        
        return 0f;
    }
}
