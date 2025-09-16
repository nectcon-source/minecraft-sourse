package net.minecraft.world.entity.schedule;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/schedule/Schedule.class */
public class Schedule {
    public static final Schedule EMPTY = register("empty").changeActivityAt(0, Activity.IDLE).build();
    public static final Schedule SIMPLE = register("simple").changeActivityAt(5000, Activity.WORK).changeActivityAt(11000, Activity.REST).build();
    public static final Schedule VILLAGER_BABY = register("villager_baby").changeActivityAt(10, Activity.IDLE).changeActivityAt(3000, Activity.PLAY).changeActivityAt(6000, Activity.IDLE).changeActivityAt(10000, Activity.PLAY).changeActivityAt(12000, Activity.REST).build();
    public static final Schedule VILLAGER_DEFAULT = register("villager_default").changeActivityAt(10, Activity.IDLE).changeActivityAt(2000, Activity.WORK).changeActivityAt(9000, Activity.MEET).changeActivityAt(11000, Activity.IDLE).changeActivityAt(12000, Activity.REST).build();
    private final Map<Activity, Timeline> timelines = Maps.newHashMap();

    protected static ScheduleBuilder register(String str) {
        return new ScheduleBuilder((Schedule) Registry.register(Registry.SCHEDULE, str, new Schedule()));
    }

    protected void ensureTimelineExistsFor(Activity activity) {
        if (!this.timelines.containsKey(activity)) {
            this.timelines.put(activity, new Timeline());
        }
    }

    protected Timeline getTimelineFor(Activity activity) {
        return this.timelines.get(activity);
    }

    protected List<Timeline> getAllTimelinesExceptFor(Activity activity) {
        return  this.timelines.entrySet().stream().filter(entry -> {
            return entry.getKey() != activity;
        }).map((v0) -> {
            return v0.getValue();
        }).collect(Collectors.toList());
    }

    public Activity getActivityAt(int i) {
        return  this.timelines.entrySet().stream().max(Comparator.comparingDouble(entry -> {
            return ( entry.getValue()).getValueAt(i);
        })).map((v0) -> {
            return v0.getKey();
        }).orElse(Activity.IDLE);
    }
}
