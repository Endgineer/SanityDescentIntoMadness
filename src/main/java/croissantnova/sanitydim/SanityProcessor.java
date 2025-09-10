package croissantnova.sanitydim;

import croissantnova.sanitydim.capability.*;
import croissantnova.sanitydim.config.*;
import croissantnova.sanitydim.entity.NightmareEntitySpawner;
import croissantnova.sanitydim.entity.NightmareEntity;
import croissantnova.sanitydim.item.ItemRegistry;
import croissantnova.sanitydim.net.NightmareEntityCapImplPacket;
import croissantnova.sanitydim.net.PacketHandler;
import croissantnova.sanitydim.net.SanityPacket;
import croissantnova.sanitydim.sources.passive.*;
import croissantnova.sanitydim.sources.passive.lso.BrokenLimbsSanitySource;
import croissantnova.sanitydim.sources.passive.lso.TemperatureSanitySource;
import croissantnova.sanitydim.sources.passive.lso.ThirstSanitySource;
import croissantnova.sanitydim.util.DeathScoreHelper;
import croissantnova.sanitydim.util.MathHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public final class SanityProcessor
{
    private static int garlandTimer = 0;

    public static final int MAX_GARLAND_TIMER = 60;
    public static final float SANITY_TARGET_THRESHOLD = .87f;
    public static final List<IPassiveSanitySource> PASSIVE_SANITY_SOURCES = new ArrayList<>(Arrays.asList(
            new PassiveGainSanitySource(),
            new InRain(),
            new HungerSanitySource(),
            new EnderManAnger(),
            new Pet(),
            new Monster(),
            new DepthSanitySource(),
            new DarknessSanitySource(),
            new Lightness(),
            new PassiveBlocks(),
            new PlayerCompany(),
            new Jukebox(),
            new BlockStuck(),
            new MovingOnCarpetSanitySource(),
            new BrokenLimbsSanitySource(),
            new TemperatureSanitySource(),
            new ThirstSanitySource(),
            new NearEntitySanitySource(),
            new SunlightSanitySource(),
            new WornArmorSanitySource(),
            new StatusEffectSanitySource()
    ));

    private SanityProcessor() {}

    private static float calculatePassiveSanity(@NotNull ServerPlayer player, ISanity sanity)
    {
        ResourceLocation dim = player.level().dimension().location();
        float passive = 0;

        for (IPassiveSanitySource pss : PASSIVE_SANITY_SOURCES)
        {
            float val = pss.get(player, sanity, dim);
            val *= getSanityMultiplier(player, val);
            passive += val;
        }

        garlandTimer--;
        ItemStack headItem = player.getItemBySlot(EquipmentSlot.HEAD);
        if (headItem.is(ItemRegistry.GARLAND.get())) {
            // TODO: un-hardcode
            passive -= .00005f * ConfigProxy.getPosMul(dim);
            if (garlandTimer <= 0) {
                headItem.hurtAndBreak(player.isInWaterOrRain() ? 2 : 1, player, p -> {});
            }
        }
        if (garlandTimer <= 0) {
            garlandTimer = MAX_GARLAND_TIMER;
        }

        return passive;
    }

    private static void shareSanity(ServerPlayer player, @NotNull Sanity cap)
    {
        if (cap.getDirty())
        {
            SanityPacket packet = new SanityPacket(cap);
            PacketHandler.CHANNEL_INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
			cap.setDirty(false);
        }
    }

    private static void handlePlayerAte(ServerPlayer player, ItemStack itemStack)
    {
        handleActiveSourceForPlayer(
                player,
                ActiveSanitySources.EATING,
                ConfigProxy::getEatingCooldown,
                dim -> itemStack.getFoodProperties(player).getNutrition() * ConfigProxy.getEating(dim));
    }

    public static float getGarlandMultiplier(ServerPlayer player)
    {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ItemRegistry.GARLAND.get()) ? .92f : 1.0f;
    }

    public static float getSanityMultiplier(ServerPlayer player, float value)
    {
        ResourceLocation dim = player.level().dimension().location();
        return value >= 0 ? ConfigProxy.getNegMul(dim) * getGarlandMultiplier(player) : ConfigProxy.getPosMul(dim);
    }

    public static void addSanity(@NotNull ISanity sanityCapability, float value, @NotNull ServerPlayer player) {
        if (value == 0.0f) {
            return;
        }

        sanityCapability.setSanity(sanityCapability.getSanity() + value * getSanityMultiplier(player, value));
    }

    public static void tickPlayer(final @NotNull ServerPlayer player)
    {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        player.getCapability(SanityProvider.CAP).ifPresent(s ->
        {
            ResourceLocation dim = player.level().dimension().location();

            float passive = calculatePassiveSanity(player, s);
            float snapshot = s.getSanity();
            // passive pre-multiplied so no need for SanityProcessor#addSanity
            s.setSanity(s.getSanity() + passive);
            if (s instanceof IPassiveSanity ps)
            {
                ps.setPassiveIncrease(snapshot != s.getSanity() ? passive : 0);
            }
            if (s instanceof IPersistentSanity ps)
            {
                tickDeathScore(player);
                int[] cds = ps.getActiveSourcesCooldowns();
                for (int i = 0; i < cds.length; ++i)
                    cds[i] = Mth.clamp(cds[i] - 1, 0, Integer.MAX_VALUE);

                Map<Integer, Integer> itemCds = ps.getItemCooldowns();
                for (Iterator<Map.Entry<Integer, Integer>> it = itemCds.entrySet().iterator(); it.hasNext();)
                {
                    Map.Entry<Integer, Integer> entry = it.next();
                    itemCds.put(entry.getKey(), itemCds.get(entry.getKey()) - 1);
                    if (itemCds.get(entry.getKey()) <= 0)
                        it.remove();
                }

                Map<Integer, Integer> brokenBlocksCds = ps.getBrokenBlocksCooldowns();
                for (Iterator<Map.Entry<Integer, Integer>> it = brokenBlocksCds.entrySet().iterator(); it.hasNext();)
                {
                    Map.Entry<Integer, Integer> entry = it.next();
                    brokenBlocksCds.put(entry.getKey(), brokenBlocksCds.get(entry.getKey()) - 1);
                    if (brokenBlocksCds.get(entry.getKey()) <= 0)
                        it.remove();
                }
            }
            if (s instanceof Sanity) {
                shareSanity(player, (Sanity)s);
            }
        });
        NightmareEntitySpawner.trySpawn(player);
    }

    private static void tickDeathScore(ServerPlayer player) {
        DeathScoreHelper dsh = new DeathScoreHelper(player);
        if (!dsh.isDeathSanityEnabled() || !dsh.isDeathStackingEnabled()) {
            return;
        }

        boolean playerLostADeath = dsh.decrementDeathScore();

        if (playerLostADeath) {
            player.displayClientMessage(dsh.getDecrementText(), true);
        }
    }

    public static void tickLevel(final ServerLevel level)
    {
        for (Entity ent : level.getEntities().getAll())
        {
            if (ent instanceof NightmareEntity ie)
            {
                ie.getCapability(NightmareEntityCapImplProvider.CAP).ifPresent(iec ->
                {
                    if (iec instanceof NightmareEntityCapImpl ieci)
                    {
                        if (ieci.hasTarget() && ie.getTarget() == null || !ieci.hasTarget() && ie.getTarget() != null)
                        {
                            ieci.setHasTarget(ie.getTarget() != null);
                            ieci.setPlayerTargetUUID(ie.getTarget() instanceof ServerPlayer sp ? sp.getUUID() : null);
                        }

                        if (ieci.getDirty())
                        {
                            NightmareEntityCapImplPacket packet = new NightmareEntityCapImplPacket(ieci);
                            packet.m_id = ent.getId();
                            PacketHandler.CHANNEL_INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> ent), packet);
                        }
                    }
                });
            }
        }
    }

    public static List<Player> getInsanePlayersInArea(final Level levelIn, BlockPos center, int blockRadius)
    {
        if (levelIn == null || center == null)
            return null;
        List<Player> list = new ArrayList<Player>();
        for (Player player : levelIn.getEntitiesOfClass(
                Player.class,
                new AABB(center.offset(blockRadius, blockRadius, blockRadius), center.offset(-blockRadius, -blockRadius, -blockRadius))))
        {
            player.getCapability(SanityProvider.CAP).ifPresent(s ->
            {
                if (s.getSanity() >= SANITY_TARGET_THRESHOLD)
                    list.add(player);
            });
        }
        return list;
    }

    public static Player getMostInsanePlayer(final Level levelIn)
    {
        return getMostInsanePlayer(levelIn, SANITY_TARGET_THRESHOLD);
    }

    public static Player getMostInsanePlayer(final Level levelIn, float sanityThreshold)
    {
        if (levelIn == null)
            return null;

        Player toReturn = null;
        float maxSanity = Float.MIN_VALUE;
        for (Player player : levelIn.players())
        {
            if (player.isCreative() || player.isSpectator())
                continue;
            ISanity s = player.getCapability(SanityProvider.CAP).orElse(null);
            if (s == null)
                continue;
            float sanity = s.getSanity();
            if (sanity >= sanityThreshold && sanity > maxSanity)
            {
                maxSanity = s.getSanity();
                toReturn = player;
            }
        }
        return toReturn;
    }

    public static void handleActiveSourceForPlayer(
            ServerPlayer player,
            int id,
            Function<ResourceLocation, Integer> cdSupplier,
            Function<ResourceLocation, Float> sanitySupplier)
    {
        if (player == null || player.isCreative() || player.isSpectator())
            return;

        player.getCapability(SanityProvider.CAP).ifPresent(s ->
        {
            ResourceLocation dimLoc = player.level().dimension().location();
            int cd = cdSupplier.apply(dimLoc);

            if (s instanceof IPersistentSanity ps && cd > 0.0f)
            {
                int timePassed = cd - ps.getActiveSourcesCooldowns()[id];
                addSanity(s, sanitySupplier.apply(dimLoc) * MathHelper.clampNorm((float) timePassed / cd), player);
//                s.setSanity(s.getSanity() + sanitySupplier.apply(dimLoc) * MathHelper.clampNorm((float) timePassed / cd));
                ps.getActiveSourcesCooldowns()[id] = cd;
            }
            else
                addSanity(s, sanitySupplier.apply(dimLoc), player);
//                s.setSanity(s.getSanity() + sanitySupplier.apply(dimLoc));
        });
    }

    public static void handlePlayerHurt(ServerPlayer player, float amount)
    {
        if (player == null || player.isCreative() || player.isSpectator() || amount <= 0)
            return;

        player.getCapability(SanityProvider.CAP).ifPresent(s ->
        {
            ResourceLocation dimLoc = player.level().dimension().location();
            addSanity(s, amount * ConfigProxy.getHurtRatio(dimLoc), player);
//            s.setSanity(s.getSanity() + amount * ConfigProxy.getHurtRatio(player.level.dimension().location()));
        });
    }

    public static void handlePlayerPetDeath(ServerPlayer player, TamableAnimal pet)
    {
        if (player == null || player.isCreative() || player.isSpectator() || pet.isOwnedBy(player))
            return;

        player.getCapability(SanityProvider.CAP).ifPresent(s ->
        {
            ResourceLocation dimLoc = player.level().dimension().location();
            addSanity(s, ConfigProxy.getPetDeath(player.level().dimension().location()), player);
//            s.setSanity(s.getSanity() + ConfigProxy.getPetDeath(player.level.dimension().location()));
        });
    }

    public static void handlePlayerEnderManAngered(ServerPlayer player)
    {
        if (player == null || player.isCreative() || player.isSpectator())
            return;

        player.getCapability(SanityProvider.CAP).ifPresent(s ->
        {
            if (s instanceof IPersistentSanity ps && ps.getEnderManAngerTimer() <= 0)
            {
                ps.setEnderManAngerTimer(100);
            }
        });
    }

    public static void handlePlayerGotAdvancement(ServerPlayer player, Advancement adv)
    {
        if (player == null || player.isCreative() || player.isSpectator())
            return;

        if (adv.getDisplay() == null || !adv.getDisplay().shouldAnnounceChat())
            return;

        player.getCapability(SanityProvider.CAP).ifPresent(s ->
        {
            ResourceLocation dimLoc = player.level().dimension().location();
            addSanity(s, ConfigProxy.getAdvancement(dimLoc), player);
        });
    }

    public static void handlePlayerBredAnimals(ServerPlayer player)
    {
        if (player == null || player.isCreative() || player.isSpectator())
            return;

        handleActiveSourceForPlayer(player, ActiveSanitySources.BREEDING_ANIMALS, ConfigProxy::getAnimalBreedingCooldown, ConfigProxy::getAnimalBreeding);
    }

    public static void handlePlayerTradedWithVillager(ServerPlayer player)
    {
        if (player == null || player.isCreative() || player.isSpectator())
            return;

        handleActiveSourceForPlayer(player, ActiveSanitySources.VILLAGER_TRADE, ConfigProxy::getVillagerTradeCooldown, ConfigProxy::getVillagerTrade);
    }

    public static void handlePlayerUsedShears(ServerPlayer player)
    {
        if (player == null || player.isCreative() || player.isSpectator())
            return;

        handleActiveSourceForPlayer(player, ActiveSanitySources.SHEARING, ConfigProxy::getShearingCooldown, ConfigProxy::getShearing);
    }

    public static void handlePlayerSpawnedChicken(ServerPlayer player)
    {
        if (player == null || player.isCreative() || player.isSpectator())
            return;

        handleActiveSourceForPlayer(player, ActiveSanitySources.SPAWNING_BABY_CHICKEN, ConfigProxy::getBabyChickenSpawningCooldown, ConfigProxy::getBabyChickenSpawning);
    }

    public static void handlePlayerUsedItem(ServerPlayer player, ItemStack itemStack)
    {
        if (player == null || player.isCreative() || player.isSpectator())
            return;

        player.getCapability(SanityProvider.CAP).ifPresent(s ->
        {
            if (s instanceof IPersistentSanity ps)
            {
                ResourceLocation dim = player.level().dimension().location();

                for (ConfigItem citem : ConfigProxy.getItems(dim))
                {
                    if (!itemStack.is(ForgeRegistries.ITEMS.getValue(citem.m_name)))
                        continue;

                    if (!ConfigProxy.getIdToItemCat(dim).containsKey(citem.m_cat))
                    {
                        SanityMod.LOGGER.warn("player " + player.getDisplayName().getString() + " used " + citem.m_name + " from category " + citem.m_cat + ", but no such category is present");
                        return;
                    }

                    ConfigItemCategory cat = ConfigProxy.getIdToItemCat(dim).get(citem.m_cat);
                    if (cat.m_cd <= 0)
                    {
                        addSanity(s, citem.m_sanity, player);
                        return;
                    }

                    Map<Integer, Integer> itemCds = ps.getItemCooldowns();

                    if (!itemCds.containsKey(citem.m_cat) || itemCds.get(citem.m_cat) <= 0)
                    {
                        addSanity(s, citem.m_sanity, player);
                    }
                    else
                    {
                        int timePassed = cat.m_cd - itemCds.get(citem.m_cat);
                        addSanity(s, citem.m_sanity * MathHelper.clampNorm((float)timePassed / cat.m_cd), player);
                    }

                    itemCds.put(citem.m_cat, cat.m_cd);

                    return;
                }
            }

            if (itemStack.isEdible())
                handlePlayerAte(player, itemStack);
        });
    }

    public static void handlePlayerFishedItem(ServerPlayer player)
    {
        if (player == null || player.isCreative() || player.isSpectator())
            return;

        handleActiveSourceForPlayer(
                player,
                ActiveSanitySources.FISHING,
                ConfigProxy::getFishingCooldown,
                ConfigProxy::getFishing
        );
    }

    public static void handlePlayerMinedBlock(ServerPlayer player, BlockPos blockPos, BlockState blockState, Block block, boolean correctTool)
    {
        if (player == null)
            return;

        ServerLevel level = player.serverLevel();
        LevelChunk levelChunk = level.getChunkAt(blockPos);

        if (player.isCreative() || player.isSpectator())
        {
            levelChunk.getCapability(SanityLevelChunkProvider.CAP).ifPresent(sl ->
            {
                sl.getArtificiallyPlacedBlocks().remove(blockPos);
                levelChunk.setUnsaved(true);
            });
            return;
        }

        player.getCapability(SanityProvider.CAP).ifPresent(s ->
        {
            if (s instanceof IPersistentSanity ps)
            {
                ResourceLocation dim = level.dimension().location();

                for (ConfigBrokenBlock cbBlock : ConfigProxy.getBrokenBlocks(dim))
                {
                    if (!(cbBlock.m_isTag && blockState.getTags().anyMatch(tag -> tag.location().equals(cbBlock.m_name)) ||
                            block.equals(ForgeRegistries.BLOCKS.getValue(cbBlock.m_name))))
                    {
                        continue;
                    }

                    if (cbBlock.m_toolRequired && !correctTool)
                        return;

                    if (!ConfigProxy.getIdToBrokenBlockCat(dim).containsKey(cbBlock.m_cat))
                    {
                        SanityMod.LOGGER.warn("player " + player.getDisplayName().getString() + " mined " + cbBlock.m_name + " from category " + cbBlock.m_cat + ", but no such category is present");
                        return;
                    }

                    // skip if artificially placed and block needs to be naturally generated
                    if (cbBlock.m_naturallyGend)
                    {
                        AtomicBoolean flag = new AtomicBoolean(false);
                        levelChunk.getCapability(SanityLevelChunkProvider.CAP).ifPresent(sl ->
                        {
                            if (sl.getArtificiallyPlacedBlocks().remove(blockPos))
                            {
                                flag.set(true);
                                levelChunk.setUnsaved(true);
                            }
                        });
                        if (flag.get())
                            return;
                    }

                    ConfigBrokenBlockCategory cat = ConfigProxy.getIdToBrokenBlockCat(dim).get(cbBlock.m_cat);
                    if (cat.m_cd <= 0)
                    {
                        addSanity(s, cbBlock.m_sanity, player);
                        return;
                    }

                    Map<Integer, Integer> brokenBlockCds = ps.getBrokenBlocksCooldowns();

                    if (!brokenBlockCds.containsKey(cbBlock.m_cat) || brokenBlockCds.get(cbBlock.m_cat) <= 0)
                    {
                        addSanity(s, cbBlock.m_sanity, player);
                    }
                    else
                    {
                        int timePassed = cat.m_cd - brokenBlockCds.get(cbBlock.m_cat);
                        addSanity(s, cbBlock.m_sanity * MathHelper.clampNorm((float)timePassed / cat.m_cd), player);
                    }

                    brokenBlockCds.put(cbBlock.m_cat, cat.m_cd);

                    return;
                }
            }
        });
    }

    public static void handlePlayerTrampledFarmland(ServerPlayer player)
    {
        if (player == null || player.isCreative() || player.isSpectator())
            return;

        player.getCapability(SanityProvider.CAP).ifPresent(s ->
        {
            addSanity(s, ConfigProxy.getFarmlandTrample(player.level().dimension().location()), player);
        });
    }

    public static void handlePlayerPottedFlower(ServerPlayer player)
    {
        if (player == null || player.isCreative() || player.isSpectator())
            return;

        player.getCapability(SanityProvider.CAP).ifPresent(s ->
        {
            handleActiveSourceForPlayer(
                    player,
                    ActiveSanitySources.POTTING_FLOWER,
                    ConfigProxy::getPottingFlowerCooldown,
                    ConfigProxy::getPottingFlower
            );
        });
    }

    public static void handlePlayerChangedDimensions(ServerPlayer player)
    {
        if (player == null || player.isCreative() || player.isSpectator())
            return;

        player.getCapability(SanityProvider.CAP).ifPresent(s ->
        {
            addSanity(s, ConfigProxy.getChangedDimension(player.level().dimension().location()), player);
        });
    }

    public static void handlePlayerStruckByLightning(ServerPlayer player)
    {
        if (player == null || player.isCreative() || player.isSpectator())
            return;

        player.getCapability(SanityProvider.CAP).ifPresent(s ->
        {
            addSanity(s, ConfigProxy.getStruckByLightning(player.level().dimension().location()), player);
        });
    }
}