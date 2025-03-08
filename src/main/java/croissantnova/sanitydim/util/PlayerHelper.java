package croissantnova.sanitydim.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public class PlayerHelper {

    public static boolean isHoldingSword(ServerPlayer player) {
        ItemStack mainHandItem = player.getMainHandItem();
        return mainHandItem.getItem() instanceof SwordItem;
    }
}
