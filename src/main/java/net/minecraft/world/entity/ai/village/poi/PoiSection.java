package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/village/poi/PoiSection.class */
public class PoiSection {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Short2ObjectMap<PoiRecord> records = new Short2ObjectOpenHashMap();
    private final Map<PoiType, Set<PoiRecord>> byType = Maps.newHashMap();
    private final Runnable setDirty;
    private boolean isValid;

    public static Codec<PoiSection> codec(Runnable runnable) {
        return RecordCodecBuilder.<PoiSection>create(
                        var1 -> var1.group(
                                        RecordCodecBuilder.point(runnable),
                                        Codec.BOOL.optionalFieldOf("Valid", false).forGetter(var0xx -> var0xx.isValid),
                                        PoiRecord.codec(runnable).listOf().fieldOf("Records").forGetter(var0xx -> ImmutableList.copyOf(var0xx.records.values()))
                                )
                                .apply(var1, PoiSection::new)
                )
                .orElseGet(Util.prefix("Failed to read POI section: ", LOGGER::error), () -> new PoiSection(runnable, false, ImmutableList.of()));
    }

    public PoiSection(Runnable runnable) {
        this(runnable, true, ImmutableList.of());
    }

    private PoiSection(Runnable runnable, boolean z, List<PoiRecord> list) {
        this.setDirty = runnable;
        this.isValid = z;
        list.forEach(this::add);
    }

    public Stream<PoiRecord> getRecords(Predicate<PoiType> predicate, PoiManager.Occupancy occupancy) {
        return this.byType.entrySet().stream().filter(entry -> {
            return predicate.test(entry.getKey());
        }).flatMap(entry2 -> {
            return ((Set) entry2.getValue()).stream();
        }).filter(occupancy.getTest());
    }

    public void add(BlockPos blockPos, PoiType poiType) {
        if (add(new PoiRecord(blockPos, poiType, this.setDirty))) {
            LOGGER.debug("Added POI of type {} @ {}", new Supplier[]{() -> {
                return poiType;
            }, () -> {
                return blockPos;
            }});
            this.setDirty.run();
        }
    }

    private boolean add(PoiRecord poiRecord) {
        BlockPos pos = poiRecord.getPos();
        PoiType poiType = poiRecord.getPoiType();
        short sectionRelativePos = SectionPos.sectionRelativePos(pos);
        PoiRecord poiRecord2 = (PoiRecord) this.records.get(sectionRelativePos);
        if (poiRecord2 != null) {
            if (poiType.equals(poiRecord2.getPoiType())) {
                return false;
            }
            String str = "POI data mismatch: already registered at " + pos;
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                throw ((IllegalStateException) Util.pauseInIde(new IllegalStateException(str)));
            }
            LOGGER.error(str);
        }
        this.records.put(sectionRelativePos, poiRecord);
        this.byType.computeIfAbsent(poiType, poiType2 -> {
            return Sets.newHashSet();
        }).add(poiRecord);
        return true;
    }

    public void remove(BlockPos blockPos) {
        PoiRecord poiRecord = (PoiRecord) this.records.remove(SectionPos.sectionRelativePos(blockPos));
        if (poiRecord == null) {
            LOGGER.error("POI data mismatch: never registered at " + blockPos);
            return;
        }
        this.byType.get(poiRecord.getPoiType()).remove(poiRecord);
        Logger logger = LOGGER;
        poiRecord.getClass();
        poiRecord.getClass();
        logger.debug("Removed POI of type {} @ {}", new Supplier[]{poiRecord::getPoiType, poiRecord::getPos});
        this.setDirty.run();
    }

    public boolean release(BlockPos blockPos) {
        PoiRecord poiRecord = (PoiRecord) this.records.get(SectionPos.sectionRelativePos(blockPos));
        if (poiRecord == null) {
            throw ((IllegalStateException) Util.pauseInIde(new IllegalStateException("POI never registered at " + blockPos)));
        }
        boolean releaseTicket = poiRecord.releaseTicket();
        this.setDirty.run();
        return releaseTicket;
    }

    public boolean exists(BlockPos blockPos, Predicate<PoiType> predicate) {
        PoiRecord poiRecord = (PoiRecord) this.records.get(SectionPos.sectionRelativePos(blockPos));
        return poiRecord != null && predicate.test(poiRecord.getPoiType());
    }

    public Optional<PoiType> getType(BlockPos blockPos) {
        PoiRecord poiRecord = (PoiRecord) this.records.get(SectionPos.sectionRelativePos(blockPos));
        return poiRecord != null ? Optional.of(poiRecord.getPoiType()) : Optional.empty();
    }

    public void refresh(Consumer<BiConsumer<BlockPos, PoiType>> consumer) {
        if (!this.isValid) {
            Short2ObjectOpenHashMap short2ObjectOpenHashMap = new Short2ObjectOpenHashMap(this.records);
            clear();
            consumer.accept((blockPos, poiType) -> {
                add((PoiRecord) short2ObjectOpenHashMap.computeIfAbsent(SectionPos.sectionRelativePos(blockPos), i -> {
                    return new PoiRecord(blockPos, poiType, this.setDirty);
                }));
            });
            this.isValid = true;
            this.setDirty.run();
        }
    }

    private void clear() {
        this.records.clear();
        this.byType.clear();
    }

    boolean isValid() {
        return this.isValid;
    }
}
