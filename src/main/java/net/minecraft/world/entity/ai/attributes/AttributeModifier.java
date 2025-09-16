package net.minecraft.world.entity.ai.attributes;

import io.netty.util.internal.ThreadLocalRandom;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/attributes/AttributeModifier.class */
public class AttributeModifier {
    private static final Logger LOGGER = LogManager.getLogger();
    private final double amount;
    private final Operation operation;
    private final Supplier<String> nameGetter;

    /* renamed from: id */
    private final UUID f435id;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/attributes/AttributeModifier$Operation.class */
    public enum Operation {
        ADDITION(0),
        MULTIPLY_BASE(1),
        MULTIPLY_TOTAL(2);

        private static final Operation[] OPERATIONS = {ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL};
        private final int value;

        Operation(int i) {
            this.value = i;
        }

        public int toValue() {
            return this.value;
        }

        public static Operation fromValue(int i) {
            if (i < 0 || i >= OPERATIONS.length) {
                throw new IllegalArgumentException("No operation with value " + i);
            }
            return OPERATIONS[i];
        }
    }

    public AttributeModifier(String str, double d, Operation operation) {
        this(Mth.createInsecureUUID(ThreadLocalRandom.current()), (Supplier<String>) () -> {
            return str;
        }, d, operation);
    }

    public AttributeModifier(UUID uuid, String str, double d, Operation operation) {
        this(uuid, (Supplier<String>) () -> {
            return str;
        }, d, operation);
    }

    public AttributeModifier(UUID uuid, Supplier<String> supplier, double d, Operation operation) {
        this.f435id = uuid;
        this.nameGetter = supplier;
        this.amount = d;
        this.operation = operation;
    }

    public UUID getId() {
        return this.f435id;
    }

    public String getName() {
        return this.nameGetter.get();
    }

    public Operation getOperation() {
        return this.operation;
    }

    public double getAmount() {
        return this.amount;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.f435id, ((AttributeModifier) obj).f435id);
    }

    public int hashCode() {
        return this.f435id.hashCode();
    }

    public String toString() {
        return "AttributeModifier{amount=" + this.amount + ", operation=" + this.operation + ", name='" + this.nameGetter.get() + "', id=" + this.f435id + '}';
    }

    public CompoundTag save() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Name", getName());
        compoundTag.putDouble("Amount", this.amount);
        compoundTag.putInt("Operation", this.operation.toValue());
        compoundTag.putUUID("UUID", this.f435id);
        return compoundTag;
    }

    @Nullable
    public static AttributeModifier load(CompoundTag compoundTag) {
        try {
            return new AttributeModifier(compoundTag.getUUID("UUID"), compoundTag.getString("Name"), compoundTag.getDouble("Amount"), Operation.fromValue(compoundTag.getInt("Operation")));
        } catch (Exception e) {
            LOGGER.warn("Unable to create attribute: {}", e.getMessage());
            return null;
        }
    }
}
