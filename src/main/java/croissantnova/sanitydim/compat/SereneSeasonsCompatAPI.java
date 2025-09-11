package croissantnova.sanitydim.compat;

import croissantnova.sanitydim.capability.SanityProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;

import org.jetbrains.annotations.NotNull;
import sereneseasons.core.SereneSeasons;

public class SereneSeasonsCompatAPI {
    public static boolean isModLoaded() {
        return ModList.get().isLoaded(SereneSeasons.MOD_ID);
    }
}
