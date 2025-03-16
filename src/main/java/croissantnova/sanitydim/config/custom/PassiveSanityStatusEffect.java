package croissantnova.sanitydim.config.custom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public record PassiveSanityStatusEffect(ResourceLocation id, float sanity, int amplifier) {

    public EntityType<?> getEntityType() {
        return ForgeRegistries.ENTITY_TYPES.getValue(id);
    }
}
