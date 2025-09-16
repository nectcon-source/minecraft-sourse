package net.minecraft.world;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/InteractionResult.class */
public enum InteractionResult {
    SUCCESS,
    CONSUME,
    PASS,
    FAIL;

    public boolean consumesAction() {
        return this == SUCCESS || this == CONSUME;
    }

    public boolean shouldSwing() {
        return this == SUCCESS;
    }

    public static InteractionResult sidedSuccess(boolean z) {
        return z ? SUCCESS : CONSUME;
    }
}
