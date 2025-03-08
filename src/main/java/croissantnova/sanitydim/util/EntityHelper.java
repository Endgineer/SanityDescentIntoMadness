package croissantnova.sanitydim.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityHelper {

    private static final TagKey<EntityType<?>> INNER_ENTITY_DESPAWNABLE_TAG = TagKey.create(
            ForgeRegistries.ENTITY_TYPES.getRegistryKey(),
            new ResourceLocation("sanitydim", "inner_entity_despawnable")
    );

    public static boolean despawnsNearInnerEntities(Entity entity) {
        return entity.getType().is(INNER_ENTITY_DESPAWNABLE_TAG);
    }
}
