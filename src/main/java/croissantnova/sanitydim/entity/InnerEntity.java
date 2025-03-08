package croissantnova.sanitydim.entity;

import croissantnova.sanitydim.capability.SanityProvider;
import croissantnova.sanitydim.config.ConfigEntry;
import croissantnova.sanitydim.config.ConfigProxy;
import croissantnova.sanitydim.sound.SoundRegistry;
import croissantnova.sanitydim.util.EntityHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class InnerEntity extends Monster
{
    private final AtomicBoolean m_skipAttackInteraction = new AtomicBoolean(false);

    protected InnerEntity(EntityType<? extends Monster> entityType, Level level)
    {
        super(entityType, level);
    }

    @Override
    public boolean skipAttackInteraction(@NotNull Entity entity)
    {
        if (entity instanceof Player player && !ConfigProxy.getSaneSeeInnerEntities(player.level().dimension().location()) &&
                !(player.isCreative() || player.isSpectator()) && getTarget() != player)
        {
            player.getCapability(SanityProvider.CAP).ifPresent(s ->
            {
                m_skipAttackInteraction.set(s.getSanity() < .6f);
            });

            return m_skipAttackInteraction.get();
        }

        return super.skipAttackInteraction(entity);
    }

    @Override
    public boolean shouldDropExperience()
    {
        return false;
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@Nonnull DamageSource damageSource)
    {
        return SoundRegistry.INNER_ENTITY_HURT.get();
    }

    @Override
    protected @NotNull SoundEvent getDeathSound()
    {
        return SoundRegistry.INNER_ENTITY_HURT.get();
    }

    @Override
    public void tick() {
        super.tick();

        int despawnDistance = ConfigProxy.getInnerEntityDespawnMobsDistance(this.level().dimension().location());
        if (despawnDistance <= 0) {
            return;
        }

        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(despawnDistance));
        for (Entity entity : entities) {
            if (EntityHelper.despawnsNearInnerEntities(entity)) {
                entity.remove(RemovalReason.DISCARDED);
            }
        }
    }
}