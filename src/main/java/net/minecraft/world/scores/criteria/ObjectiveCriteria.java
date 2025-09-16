package net.minecraft.world.scores.criteria;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/scores/criteria/ObjectiveCriteria.class */
public class ObjectiveCriteria {
    public static final Map<String, ObjectiveCriteria> CRITERIA_BY_NAME = Maps.newHashMap();
    public static final ObjectiveCriteria DUMMY = new ObjectiveCriteria("dummy");
    public static final ObjectiveCriteria TRIGGER = new ObjectiveCriteria("trigger");
    public static final ObjectiveCriteria DEATH_COUNT = new ObjectiveCriteria("deathCount");
    public static final ObjectiveCriteria KILL_COUNT_PLAYERS = new ObjectiveCriteria("playerKillCount");
    public static final ObjectiveCriteria KILL_COUNT_ALL = new ObjectiveCriteria("totalKillCount");
    public static final ObjectiveCriteria HEALTH = new ObjectiveCriteria("health", true, RenderType.HEARTS);
    public static final ObjectiveCriteria FOOD = new ObjectiveCriteria("food", true, RenderType.INTEGER);
    public static final ObjectiveCriteria AIR = new ObjectiveCriteria("air", true, RenderType.INTEGER);
    public static final ObjectiveCriteria ARMOR = new ObjectiveCriteria("armor", true, RenderType.INTEGER);
    public static final ObjectiveCriteria EXPERIENCE = new ObjectiveCriteria("xp", true, RenderType.INTEGER);
    public static final ObjectiveCriteria LEVEL = new ObjectiveCriteria("level", true, RenderType.INTEGER);
    public static final ObjectiveCriteria[] TEAM_KILL = {new ObjectiveCriteria("teamkill." + ChatFormatting.BLACK.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.DARK_BLUE.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.DARK_GREEN.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.DARK_AQUA.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.DARK_RED.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.DARK_PURPLE.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.GOLD.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.GRAY.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.DARK_GRAY.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.BLUE.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.GREEN.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.AQUA.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.RED.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.LIGHT_PURPLE.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.YELLOW.getName()), new ObjectiveCriteria("teamkill." + ChatFormatting.WHITE.getName())};
    public static final ObjectiveCriteria[] KILLED_BY_TEAM = {new ObjectiveCriteria("killedByTeam." + ChatFormatting.BLACK.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.DARK_BLUE.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.DARK_GREEN.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.DARK_AQUA.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.DARK_RED.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.DARK_PURPLE.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.GOLD.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.GRAY.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.DARK_GRAY.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.BLUE.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.GREEN.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.AQUA.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.RED.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.LIGHT_PURPLE.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.YELLOW.getName()), new ObjectiveCriteria("killedByTeam." + ChatFormatting.WHITE.getName())};
    private final String name;
    private final boolean readOnly;
    private final RenderType renderType;

    public ObjectiveCriteria(String str) {
        this(str, false, RenderType.INTEGER);
    }

    protected ObjectiveCriteria(String str, boolean z, RenderType renderType) {
        this.name = str;
        this.readOnly = z;
        this.renderType = renderType;
        CRITERIA_BY_NAME.put(str, this);
    }

    public static Optional<ObjectiveCriteria> byName(String str) {
        if (CRITERIA_BY_NAME.containsKey(str)) {
            return Optional.of(CRITERIA_BY_NAME.get(str));
        }
        int indexOf = str.indexOf(58);
        if (indexOf < 0) {
            return Optional.empty();
        }
        return Registry.STAT_TYPE.getOptional(ResourceLocation.of(str.substring(0, indexOf), '.')).flatMap(statType -> {
            return getStat(statType, ResourceLocation.of(str.substring(indexOf + 1), '.'));
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <T> Optional<ObjectiveCriteria> getStat(StatType<T> statType, ResourceLocation resourceLocation) {
        Optional<T> optional = statType.getRegistry().getOptional(resourceLocation);
        statType.getClass();
        return optional.map(statType::get);
    }

    public String getName() {
        return this.name;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public RenderType getDefaultRenderType() {
        return this.renderType;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/scores/criteria/ObjectiveCriteria$RenderType.class */
    public enum RenderType {
        INTEGER("integer"),
        HEARTS("hearts");


        /* renamed from: id */
        private final String id;
        private static final Map<String, RenderType> BY_ID;

        static {
            ImmutableMap.Builder<String, RenderType> builder = ImmutableMap.builder();
            for (RenderType renderType : values()) {
                builder.put(renderType.id, renderType);
            }
            BY_ID = builder.build();
        }

        RenderType(String str) {
            this.id = str;
        }

        public String getId() {
            return this.id;
        }

        public static RenderType byId(String str) {
            return BY_ID.getOrDefault(str, INTEGER);
        }
    }
}
