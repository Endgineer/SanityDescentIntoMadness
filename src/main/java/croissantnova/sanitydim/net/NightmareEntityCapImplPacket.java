package croissantnova.sanitydim.net;

import croissantnova.sanitydim.capability.NightmareEntityCapImpl;
import croissantnova.sanitydim.capability.NightmareEntityCapImplProvider;
import croissantnova.sanitydim.entity.NightmareEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NightmareEntityCapImplPacket
{
    public int m_id;
    public NightmareEntityCapImpl m_cap;
    public FriendlyByteBuf m_buf;

    public NightmareEntityCapImplPacket()
    {
    }

    public NightmareEntityCapImplPacket(NightmareEntityCapImpl cap)
    {
        this.m_cap = cap;
    }

    public static void encode(NightmareEntityCapImplPacket packet, FriendlyByteBuf buf)
    {
        buf.writeInt(packet.m_id);
        packet.m_cap.serialize(buf);
    }

    public static NightmareEntityCapImplPacket decode(FriendlyByteBuf buf)
    {
        int id = buf.readInt();
        NightmareEntityCapImplPacket packet = new NightmareEntityCapImplPacket();
        packet.m_id = id;
        packet.m_buf = buf;
        return packet;
    }

    public static void handle(NightmareEntityCapImplPacket packet, Supplier<NetworkEvent.Context> ctx)
    {
        if (packet.m_buf == null)
            return;

        ctx.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
            {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null)
                    return;

                Entity ent = Minecraft.getInstance().player.level().getEntity(packet.m_id);

                if (!(ent instanceof NightmareEntity))
                    return;

                ent.getCapability(NightmareEntityCapImplProvider.CAP).ifPresent(iec ->
                {
                    if (iec instanceof NightmareEntityCapImpl ieci)
                        ieci.deserialize(packet.m_buf);
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }
}