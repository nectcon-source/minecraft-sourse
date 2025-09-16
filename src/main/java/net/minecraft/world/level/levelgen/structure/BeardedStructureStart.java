package net.minecraft.world.level.levelgen.structure;

import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/BeardedStructureStart.class */
public abstract class BeardedStructureStart<C extends FeatureConfiguration> extends StructureStart<C> {
    public BeardedStructureStart(StructureFeature<C> structureFeature, int i, int i2, BoundingBox boundingBox, int i3, long j) {
        super(structureFeature, i, i2, boundingBox, i3, j);
    }

    @Override // net.minecraft.world.level.levelgen.structure.StructureStart
    protected void calculateBoundingBox() {
        super.calculateBoundingBox();
        this.boundingBox.x0 -= 12;
        this.boundingBox.y0 -= 12;
        this.boundingBox.z0 -= 12;
        this.boundingBox.x1 += 12;
        this.boundingBox.y1 += 12;
        this.boundingBox.z1 += 12;
    }
}
