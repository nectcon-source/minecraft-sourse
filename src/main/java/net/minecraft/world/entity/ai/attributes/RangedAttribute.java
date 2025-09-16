package net.minecraft.world.entity.ai.attributes;

import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/attributes/RangedAttribute.class */
public class RangedAttribute extends Attribute {
    private final double minValue;
    private final double maxValue;

    public RangedAttribute(String str, double d, double d2, double d3) {
        super(str, d);
        this.minValue = d2;
        this.maxValue = d3;
        if (d2 > d3) {
            throw new IllegalArgumentException("Minimum value cannot be bigger than maximum value!");
        }
        if (d < d2) {
            throw new IllegalArgumentException("Default value cannot be lower than minimum value!");
        }
        if (d > d3) {
            throw new IllegalArgumentException("Default value cannot be bigger than maximum value!");
        }
    }

    @Override // net.minecraft.world.entity.p000ai.attributes.Attribute
    public double sanitizeValue(double d) {
        return Mth.clamp(d, this.minValue, this.maxValue);
    }
}
