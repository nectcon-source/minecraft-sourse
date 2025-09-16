package net.minecraft.world.level.border;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/border/BorderChangeListener.class */
public interface BorderChangeListener {
    void onBorderSizeSet(WorldBorder worldBorder, double d);

    void onBorderSizeLerping(WorldBorder worldBorder, double d, double d2, long j);

    void onBorderCenterSet(WorldBorder worldBorder, double d, double d2);

    void onBorderSetWarningTime(WorldBorder worldBorder, int i);

    void onBorderSetWarningBlocks(WorldBorder worldBorder, int i);

    void onBorderSetDamagePerBlock(WorldBorder worldBorder, double d);

    void onBorderSetDamageSafeZOne(WorldBorder worldBorder, double d);

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/border/BorderChangeListener$DelegateBorderChangeListener.class */
    public static class DelegateBorderChangeListener implements BorderChangeListener {
        private final WorldBorder worldBorder;

        public DelegateBorderChangeListener(WorldBorder worldBorder) {
            this.worldBorder = worldBorder;
        }

        @Override // net.minecraft.world.level.border.BorderChangeListener
        public void onBorderSizeSet(WorldBorder worldBorder, double d) {
            this.worldBorder.setSize(d);
        }

        @Override // net.minecraft.world.level.border.BorderChangeListener
        public void onBorderSizeLerping(WorldBorder worldBorder, double d, double d2, long j) {
            this.worldBorder.lerpSizeBetween(d, d2, j);
        }

        @Override // net.minecraft.world.level.border.BorderChangeListener
        public void onBorderCenterSet(WorldBorder worldBorder, double d, double d2) {
            this.worldBorder.setCenter(d, d2);
        }

        @Override // net.minecraft.world.level.border.BorderChangeListener
        public void onBorderSetWarningTime(WorldBorder worldBorder, int i) {
            this.worldBorder.setWarningTime(i);
        }

        @Override // net.minecraft.world.level.border.BorderChangeListener
        public void onBorderSetWarningBlocks(WorldBorder worldBorder, int i) {
            this.worldBorder.setWarningBlocks(i);
        }

        @Override // net.minecraft.world.level.border.BorderChangeListener
        public void onBorderSetDamagePerBlock(WorldBorder worldBorder, double d) {
            this.worldBorder.setDamagePerBlock(d);
        }

        @Override // net.minecraft.world.level.border.BorderChangeListener
        public void onBorderSetDamageSafeZOne(WorldBorder worldBorder, double d) {
            this.worldBorder.setDamageSafeZone(d);
        }
    }
}
