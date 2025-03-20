package croissantnova.sanitydim.config.value;

import net.minecraftforge.common.ForgeConfigSpec;

public interface IConfigValue {

    default void commentate(ForgeConfigSpec.Builder builder, String... comments) {
        for (String comment : comments) {
            builder.comment(comment);
        }
    }
}
