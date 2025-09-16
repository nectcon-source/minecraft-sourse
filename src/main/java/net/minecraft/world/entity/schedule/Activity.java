package net.minecraft.world.entity.schedule;

import net.minecraft.core.Registry;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/schedule/Activity.class */
public class Activity {
    public static final Activity CORE = register("core");
    public static final Activity IDLE = register("idle");
    public static final Activity WORK = register("work");
    public static final Activity PLAY = register("play");
    public static final Activity REST = register("rest");
    public static final Activity MEET = register("meet");
    public static final Activity PANIC = register("panic");
    public static final Activity RAID = register("raid");
    public static final Activity PRE_RAID = register("pre_raid");
    public static final Activity HIDE = register("hide");
    public static final Activity FIGHT = register("fight");
    public static final Activity CELEBRATE = register("celebrate");
    public static final Activity ADMIRE_ITEM = register("admire_item");
    public static final Activity AVOID = register("avoid");
    public static final Activity RIDE = register("ride");
    private final String name;
    private final int hashCode;

    private Activity(String str) {
        this.name = str;
        this.hashCode = str.hashCode();
    }

    public String getName() {
        return this.name;
    }

    private static Activity register(String str) {
        return (Activity) Registry.register(Registry.ACTIVITY, str, new Activity(str));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return this.name.equals(((Activity) obj).name);
    }

    public int hashCode() {
        return this.hashCode;
    }

    public String toString() {
        return getName();
    }
}
