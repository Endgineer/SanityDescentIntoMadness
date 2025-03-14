package croissantnova.sanitydim.config.custom;

import croissantnova.sanitydim.SanityMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PassiveSanityEntityProcessor {

    public static List<PassiveSanityEntity> process(List<? extends String> raw) {
        return raw
                .stream()
                .map(PassiveSanityEntityProcessor::process)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static PassiveSanityEntity process(String entry) {
        String[] parameters = splitBySemicolon(entry, 3);
        if (parameters.length != 3) {
            SanityMod.LOGGER.error("config format error in {} -> the number of parameters is not 3", entry);
            return null;
        }

        Float radius = convertToFloat(entry, parameters[0]);
        Float sanity = convertToFloat(entry, parameters[2]);
        if (radius == null || sanity == null) {
            return null;
        }

        PassiveSanityEntity entity = new PassiveSanityEntity();
        entity.id = new ResourceLocation(parameters[0]);
        entity.radius = radius;
        entity.sanity = sanity;
        return entity;
    }

    private static String[] splitBySemicolon(String entry, int amount) {
        return entry.trim().split("\\s*;\\s*", amount);
    }

    private static Float convertToFloat(String entry, String parameter) {
        try {
            return Float.parseFloat(parameter);
        }
        catch (NumberFormatException e) {
            SanityMod.LOGGER.error("config format error in {} -> can't convert {} to float", entry, parameter);
            return null;
        }
    }

    // TODO: check if entities defined in List<PassiveSanityEntity> exist after registries are finished loading
    public static boolean isEntityRegistered(ResourceLocation id) {
        return ForgeRegistries.ENTITY_TYPES.containsKey(id);
    }
}
