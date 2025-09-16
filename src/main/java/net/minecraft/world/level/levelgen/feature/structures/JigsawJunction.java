package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/structures/JigsawJunction.class */
public class JigsawJunction {
    private final int sourceX;
    private final int sourceGroundY;
    private final int sourceZ;
    private final int deltaY;
    private final StructureTemplatePool.Projection destProjection;

    public JigsawJunction(int i, int i2, int i3, int i4, StructureTemplatePool.Projection projection) {
        this.sourceX = i;
        this.sourceGroundY = i2;
        this.sourceZ = i3;
        this.deltaY = i4;
        this.destProjection = projection;
    }

    public int getSourceX() {
        return this.sourceX;
    }

    public int getSourceGroundY() {
        return this.sourceGroundY;
    }

    public int getSourceZ() {
        return this.sourceZ;
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(dynamicOps.createString("source_x"), dynamicOps.createInt(this.sourceX)).put(dynamicOps.createString("source_ground_y"), dynamicOps.createInt(this.sourceGroundY)).put(dynamicOps.createString("source_z"), dynamicOps.createInt(this.sourceZ)).put(dynamicOps.createString("delta_y"), dynamicOps.createInt(this.deltaY)).put(dynamicOps.createString("dest_proj"), dynamicOps.createString(this.destProjection.getName()));
        return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build()));
    }

    public static <T> JigsawJunction deserialize(Dynamic<T> dynamic) {
        return new JigsawJunction(dynamic.get("source_x").asInt(0), dynamic.get("source_ground_y").asInt(0), dynamic.get("source_z").asInt(0), dynamic.get("delta_y").asInt(0), StructureTemplatePool.Projection.byName(dynamic.get("dest_proj").asString("")));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        JigsawJunction jigsawJunction = (JigsawJunction) obj;
        return this.sourceX == jigsawJunction.sourceX && this.sourceZ == jigsawJunction.sourceZ && this.deltaY == jigsawJunction.deltaY && this.destProjection == jigsawJunction.destProjection;
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * ((31 * this.sourceX) + this.sourceGroundY)) + this.sourceZ)) + this.deltaY)) + this.destProjection.hashCode();
    }

    public String toString() {
        return "JigsawJunction{sourceX=" + this.sourceX + ", sourceGroundY=" + this.sourceGroundY + ", sourceZ=" + this.sourceZ + ", deltaY=" + this.deltaY + ", destProjection=" + this.destProjection + '}';
    }
}
