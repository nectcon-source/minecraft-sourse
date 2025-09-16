package net.minecraft.world.entity.schedule;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/schedule/Keyframe.class */
public class Keyframe {
    private final int timeStamp;
    private final float value;

    public Keyframe(int i, float f) {
        this.timeStamp = i;
        this.value = f;
    }

    public int getTimeStamp() {
        return this.timeStamp;
    }

    public float getValue() {
        return this.value;
    }
}
