package net.minecraft.world.entity.boss.enderdragon.phases;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhase.class */
public class EnderDragonPhase<T extends DragonPhaseInstance> {
    private static EnderDragonPhase<?>[] phases = new EnderDragonPhase[0];
    public static final EnderDragonPhase<DragonHoldingPatternPhase> HOLDING_PATTERN = create(DragonHoldingPatternPhase.class, "HoldingPattern");
    public static final EnderDragonPhase<DragonStrafePlayerPhase> STRAFE_PLAYER = create(DragonStrafePlayerPhase.class, "StrafePlayer");
    public static final EnderDragonPhase<DragonLandingApproachPhase> LANDING_APPROACH = create(DragonLandingApproachPhase.class, "LandingApproach");
    public static final EnderDragonPhase<DragonLandingPhase> LANDING = create(DragonLandingPhase.class, "Landing");
    public static final EnderDragonPhase<DragonTakeoffPhase> TAKEOFF = create(DragonTakeoffPhase.class, "Takeoff");
    public static final EnderDragonPhase<DragonSittingFlamingPhase> SITTING_FLAMING = create(DragonSittingFlamingPhase.class, "SittingFlaming");
    public static final EnderDragonPhase<DragonSittingScanningPhase> SITTING_SCANNING = create(DragonSittingScanningPhase.class, "SittingScanning");
    public static final EnderDragonPhase<DragonSittingAttackingPhase> SITTING_ATTACKING = create(DragonSittingAttackingPhase.class, "SittingAttacking");
    public static final EnderDragonPhase<DragonChargePlayerPhase> CHARGING_PLAYER = create(DragonChargePlayerPhase.class, "ChargingPlayer");
    public static final EnderDragonPhase<DragonDeathPhase> DYING = create(DragonDeathPhase.class, "Dying");
    public static final EnderDragonPhase<DragonHoverPhase> HOVERING = create(DragonHoverPhase.class, "Hover");
    private final Class<? extends DragonPhaseInstance> instanceClass;

    /* renamed from: id */
    private final int f448id;
    private final String name;

    private EnderDragonPhase(int i, Class<? extends DragonPhaseInstance> cls, String str) {
        this.f448id = i;
        this.instanceClass = cls;
        this.name = str;
    }

    public DragonPhaseInstance createInstance(EnderDragon enderDragon) {
        try {
            return getConstructor().newInstance(enderDragon);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    protected Constructor<? extends DragonPhaseInstance> getConstructor() throws NoSuchMethodException {
        return this.instanceClass.getConstructor(EnderDragon.class);
    }

    public int getId() {
        return this.f448id;
    }

    public String toString() {
        return this.name + " (#" + this.f448id + ")";
    }

    public static EnderDragonPhase<?> getById(int i) {
        if (i < 0 || i >= phases.length) {
            return HOLDING_PATTERN;
        }
        return phases[i];
    }

    public static int getCount() {
        return phases.length;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static <T extends DragonPhaseInstance> EnderDragonPhase<T> create(Class<T> cls, String str) {
        EnderDragonPhase<T> enderDragonPhase = new EnderDragonPhase<>(phases.length, cls, str);
        phases = (EnderDragonPhase[]) Arrays.copyOf(phases, phases.length + 1);
        phases[enderDragonPhase.getId()] = enderDragonPhase;
        return enderDragonPhase;
    }
}
