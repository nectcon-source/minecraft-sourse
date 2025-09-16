package net.minecraft.world.item;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/TooltipFlag.class */
public interface TooltipFlag {
    boolean isAdvanced();

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/TooltipFlag$Default.class */
    public enum Default implements TooltipFlag {
        NORMAL(false),
        ADVANCED(true);

        private final boolean advanced;

        Default(boolean z) {
            this.advanced = z;
        }

        @Override // net.minecraft.world.item.TooltipFlag
        public boolean isAdvanced() {
            return this.advanced;
        }
    }
}
