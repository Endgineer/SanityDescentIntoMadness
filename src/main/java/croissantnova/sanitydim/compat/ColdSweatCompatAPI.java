package croissantnova.sanitydim.compat;

import croissantnova.sanitydim.capability.SanityProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.momosoftworks.coldsweat.ColdSweat;

public class ColdSweatCompatAPI {
    public static boolean isModLoaded() {
        return ModList.get().isLoaded(ColdSweat.MOD_ID);
    }
}
