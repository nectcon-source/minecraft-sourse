package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.apache.commons.lang3.tuple.Pair;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BannerPattern.class */
public enum BannerPattern {
    BASE("base", "b", false),
    SQUARE_BOTTOM_LEFT("square_bottom_left", "bl"),
    SQUARE_BOTTOM_RIGHT("square_bottom_right", "br"),
    SQUARE_TOP_LEFT("square_top_left", "tl"),
    SQUARE_TOP_RIGHT("square_top_right", "tr"),
    STRIPE_BOTTOM("stripe_bottom", "bs"),
    STRIPE_TOP("stripe_top", "ts"),
    STRIPE_LEFT("stripe_left", "ls"),
    STRIPE_RIGHT("stripe_right", "rs"),
    STRIPE_CENTER("stripe_center", "cs"),
    STRIPE_MIDDLE("stripe_middle", "ms"),
    STRIPE_DOWNRIGHT("stripe_downright", "drs"),
    STRIPE_DOWNLEFT("stripe_downleft", "dls"),
    STRIPE_SMALL("small_stripes", "ss"),
    CROSS("cross", "cr"),
    STRAIGHT_CROSS("straight_cross", "sc"),
    TRIANGLE_BOTTOM("triangle_bottom", "bt"),
    TRIANGLE_TOP("triangle_top", "tt"),
    TRIANGLES_BOTTOM("triangles_bottom", "bts"),
    TRIANGLES_TOP("triangles_top", "tts"),
    DIAGONAL_LEFT("diagonal_left", "ld"),
    DIAGONAL_RIGHT("diagonal_up_right", "rd"),
    DIAGONAL_LEFT_MIRROR("diagonal_up_left", "lud"),
    DIAGONAL_RIGHT_MIRROR("diagonal_right", "rud"),
    CIRCLE_MIDDLE("circle", "mc"),
    RHOMBUS_MIDDLE("rhombus", "mr"),
    HALF_VERTICAL("half_vertical", "vh"),
    HALF_HORIZONTAL("half_horizontal", "hh"),
    HALF_VERTICAL_MIRROR("half_vertical_right", "vhr"),
    HALF_HORIZONTAL_MIRROR("half_horizontal_bottom", "hhb"),
    BORDER("border", "bo"),
    CURLY_BORDER("curly_border", "cbo"),
    GRADIENT("gradient", "gra"),
    GRADIENT_UP("gradient_up", "gru"),
    BRICKS("bricks", "bri"),
    GLOBE("globe", "glb", true),
    CREEPER("creeper", "cre", true),
    SKULL("skull", "sku", true),
    FLOWER("flower", "flo", true),
    MOJANG("mojang", "moj", true),
    PIGLIN("piglin", "pig", true);

    private static final BannerPattern[] VALUES = values();
    public static final int COUNT = VALUES.length;
    public static final int PATTERN_ITEM_COUNT = (int) Arrays.stream(VALUES).filter(bannerPattern -> {
        return bannerPattern.hasPatternItem;
    }).count();
    public static final int AVAILABLE_PATTERNS = (COUNT - PATTERN_ITEM_COUNT) - 1;
    private final boolean hasPatternItem;
    private final String filename;
    private final String hashname;

    BannerPattern(String str, String str2) {
        this(str, str2, false);
    }

    BannerPattern(String str, String str2, boolean z) {
        this.filename = str;
        this.hashname = str2;
        this.hasPatternItem = z;
    }

    public ResourceLocation location(boolean z) {
        return new ResourceLocation("entity/" + (z ? "banner" : "shield") + "/" + getFilename());
    }

    public String getFilename() {
        return this.filename;
    }

    public String getHashname() {
        return this.hashname;
    }

    @Nullable
    public static BannerPattern byHash(String str) {
        for (BannerPattern bannerPattern : values()) {
            if (bannerPattern.hashname.equals(str)) {
                return bannerPattern;
            }
        }
        return null;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BannerPattern$Builder.class */
    public static class Builder {
        private final List<Pair<BannerPattern, DyeColor>> patterns = Lists.newArrayList();

        public Builder addPattern(BannerPattern bannerPattern, DyeColor dyeColor) {
            this.patterns.add(Pair.of(bannerPattern, dyeColor));
            return this;
        }

        public ListTag toListTag() {
            ListTag listTag = new ListTag();
            for (Pair<BannerPattern, DyeColor> pair : this.patterns) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putString("Pattern", ((BannerPattern) pair.getLeft()).hashname);
                compoundTag.putInt("Color", ((DyeColor) pair.getRight()).getId());
                listTag.add(compoundTag);
            }
            return listTag;
        }
    }
}
