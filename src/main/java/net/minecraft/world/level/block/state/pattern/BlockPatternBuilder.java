package net.minecraft.world.level.block.state.pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/pattern/BlockPatternBuilder.class */
public class BlockPatternBuilder {
    private static final Joiner COMMA_JOINED = Joiner.on(",");
    private final List<String[]> pattern = Lists.newArrayList();
    private final Map<Character, Predicate<BlockInWorld>> lookup = Maps.newHashMap();
    private int height;
    private int width;

    private BlockPatternBuilder() {
        this.lookup.put(' ', Predicates.alwaysTrue());
    }

    public BlockPatternBuilder aisle(String... strArr) {
        if (ArrayUtils.isEmpty(strArr) || StringUtils.isEmpty(strArr[0])) {
            throw new IllegalArgumentException("Empty pattern for aisle");
        }
        if (this.pattern.isEmpty()) {
            this.height = strArr.length;
            this.width = strArr[0].length();
        }
        if (strArr.length != this.height) {
            throw new IllegalArgumentException("Expected aisle with height of " + this.height + ", but was given one with a height of " + strArr.length + ")");
        }
        for (String str : strArr) {
            if (str.length() != this.width) {
                throw new IllegalArgumentException("Not all rows in the given aisle are the correct width (expected " + this.width + ", found one with " + str.length() + ")");
            }
            for (char c : str.toCharArray()) {
                if (!this.lookup.containsKey(Character.valueOf(c))) {
                    this.lookup.put(Character.valueOf(c), null);
                }
            }
        }
        this.pattern.add(strArr);
        return this;
    }

    public static BlockPatternBuilder start() {
        return new BlockPatternBuilder();
    }

    public BlockPatternBuilder where(char c, Predicate<BlockInWorld> predicate) {
        this.lookup.put(Character.valueOf(c), predicate);
        return this;
    }

    public BlockPattern build() {
        return new BlockPattern(createPattern());
    }

    private Predicate<BlockInWorld>[][][] createPattern() {
        ensureAllCharactersMatched();
        Predicate<BlockInWorld>[][][] predicateArr = (Predicate[][][]) Array.newInstance((Class<?>) Predicate.class, this.pattern.size(), this.height, this.width);
        for (int i = 0; i < this.pattern.size(); i++) {
            for (int i2 = 0; i2 < this.height; i2++) {
                for (int i3 = 0; i3 < this.width; i3++) {
                    predicateArr[i][i2][i3] = this.lookup.get(Character.valueOf(this.pattern.get(i)[i2].charAt(i3)));
                }
            }
        }
        return predicateArr;
    }

    private void ensureAllCharactersMatched() {
        List<Character> newArrayList = Lists.newArrayList();
        for (Map.Entry<Character, Predicate<BlockInWorld>> entry : this.lookup.entrySet()) {
            if (entry.getValue() == null) {
                newArrayList.add(entry.getKey());
            }
        }
        if (!newArrayList.isEmpty()) {
            throw new IllegalStateException("Predicates for character(s) " + COMMA_JOINED.join(newArrayList) + " are missing");
        }
    }
}
