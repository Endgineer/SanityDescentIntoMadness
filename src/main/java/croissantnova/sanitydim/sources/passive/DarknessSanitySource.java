package croissantnova.sanitydim.sources.passive;

import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.config.ConfigProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class DarknessSanitySource implements IPassiveSanitySource
{
    @Override
    public float get(@Nonnull ServerPlayer player, @Nonnull ISanity cap, @Nonnull ResourceLocation dim)
    {
        Vec3 eyePos = player.getEyePosition();
        BlockPos blockPos = BlockPos.containing(eyePos.x, eyePos.y, eyePos.z);

        if (player.level().getMaxLocalRawBrightness(blockPos) <= ConfigProxy.getDarknessThreshold(dim)) {
            return ConfigProxy.getDarkness(dim);
        }

        return 0;
    }
}