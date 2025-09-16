package net.minecraft.world.entity.schedule;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import java.util.List;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/schedule/Timeline.class */
public class Timeline {
    private final List<Keyframe> keyframes = Lists.newArrayList();
    private int previousIndex;

    public Timeline addKeyframe(int i, float f) {
        this.keyframes.add(new Keyframe(i, f));
        sortAndDeduplicateKeyframes();
        return this;
    }

    private void sortAndDeduplicateKeyframes() {
        Int2ObjectAVLTreeMap int2ObjectAVLTreeMap = new Int2ObjectAVLTreeMap();
        this.keyframes.forEach(keyframe -> {
        });
        this.keyframes.clear();
        this.keyframes.addAll(int2ObjectAVLTreeMap.values());
        this.previousIndex = 0;
    }

    public float getValueAt(int i) {
        if (this.keyframes.size() <= 0) {
            return 0.0f;
        }
        Keyframe keyframe = this.keyframes.get(this.previousIndex);
        Keyframe keyframe2 = this.keyframes.get(this.keyframes.size() - 1);
        boolean z = i < keyframe.getTimeStamp();
        int i2 = z ? 0 : this.previousIndex;
        float value = z ? keyframe2.getValue() : keyframe.getValue();
        for (int i3 = i2; i3 < this.keyframes.size(); i3++) {
            Keyframe keyframe3 = this.keyframes.get(i3);
            if (keyframe3.getTimeStamp() > i) {
                break;
            }
            this.previousIndex = i3;
            value = keyframe3.getValue();
        }
        return value;
    }
}
