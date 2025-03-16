package croissantnova.sanitydim.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import croissantnova.sanitydim.capability.InnerEntityCapImplProvider;
import croissantnova.sanitydim.capability.SanityProvider;
import croissantnova.sanitydim.config.ConfigProxy;
import croissantnova.sanitydim.entity.NightmareEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

public class InnerEntityRenderer<T extends NightmareEntity & GeoAnimatable> extends GeoEntityRenderer<T>
{
    private final Minecraft minecraftClientInstance = Minecraft.getInstance();
    private final AtomicBoolean shouldRender = new AtomicBoolean(false);
    private final AtomicBoolean isTargetMe = new AtomicBoolean(false);

    public InnerEntityRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model)
    {
        super(renderManager, model);
    }

    public boolean shouldRender(T entity)
    {
        LocalPlayer player = minecraftClientInstance.player;
        if (player == null || entity == null) {
            return false;
        }

        if (ConfigProxy.getSaneSeeInnerEntities(player.level().dimension().location()) || player.isCreative() || player.isSpectator()) {
            return true;
        }

        entity.getCapability(InnerEntityCapImplProvider.CAP).ifPresent(iec ->
                isTargetMe.set(iec.getPlayerTargetUUID() != null && iec.getPlayerTargetUUID().equals(player.getUUID())));
        if (isTargetMe.get()) {
            return true;
        }

        player.getCapability(SanityProvider.CAP).ifPresent(s ->
                shouldRender.set(s.getSanity() >= .6f));

        return shouldRender.get();
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight)
    {
        if (shouldRender(entity)) {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource,
                                    float partialTick)
    {
        return RenderType.entityTranslucent(getTextureLocation(animatable), false);
    }
}
