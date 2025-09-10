package croissantnova.sanitydim.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class EntityHelper {
    public static List<? extends Entity> getEntities(Level level, AABB searchArea, EntityType<?> entityType) {
        return level.getEntities(
                (Entity) null, // necessary cast in order to avoid ambiguity between Entity and EntityTypeTest
                searchArea,
                entity -> entity.getType() == entityType
        );
    }
}
