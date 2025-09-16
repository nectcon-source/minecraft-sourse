package net.minecraft.world.entity.ai.attributes;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/attributes/Attribute.class */
public class Attribute {
    private final double defaultValue;
    private boolean syncable;
    private final String descriptionId;

    protected Attribute(String str, double d) {
        this.defaultValue = d;
        this.descriptionId = str;
    }

    public double getDefaultValue() {
        return this.defaultValue;
    }

    public boolean isClientSyncable() {
        return this.syncable;
    }

    public Attribute setSyncable(boolean z) {
        this.syncable = z;
        return this;
    }

    public double sanitizeValue(double d) {
        return d;
    }

    public String getDescriptionId() {
        return this.descriptionId;
    }
}
