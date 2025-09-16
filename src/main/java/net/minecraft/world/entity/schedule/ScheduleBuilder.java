package net.minecraft.world.entity.schedule;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/schedule/ScheduleBuilder.class */
public class ScheduleBuilder {
    private final Schedule schedule;
    private final List<ActivityTransition> transitions = Lists.newArrayList();

    public ScheduleBuilder(Schedule schedule) {
        this.schedule = schedule;
    }

    public ScheduleBuilder changeActivityAt(int i, Activity activity) {
        this.transitions.add(new ActivityTransition(i, activity));
        return this;
    }

//    public Schedule build() {
//        Set set =  this.transitions.stream().map((v0) -> {
//            return v0.getActivity();
//        }).collect(Collectors.toSet());
//
//        set.forEach(this.schedule::ensureTimelineExistsFor);
//        this.transitions.forEach(activityTransition -> {
//            Activity activity = activityTransition.getActivity();
//            this.schedule.getAllTimelinesExceptFor(activity).forEach(timeline -> {
//                timeline.addKeyframe(activityTransition.getTime(), 0.0f);
//            });
//            this.schedule.getTimelineFor(activity).addKeyframe(activityTransition.getTime(), 1.0f);
//        });
//        return this.schedule;
//    }
public Schedule build() {
    Set<Activity> activities = this.transitions.stream()
            .map(ActivityTransition::getActivity)
            .collect(Collectors.toSet());

    activities.forEach(activity -> this.schedule.ensureTimelineExistsFor(activity));

    this.transitions.forEach(activityTransition -> {
        Activity activity = activityTransition.getActivity();
        this.schedule.getAllTimelinesExceptFor(activity).forEach(timeline -> {
            timeline.addKeyframe(activityTransition.getTime(), 0.0f);
        });
        this.schedule.getTimelineFor(activity).addKeyframe(activityTransition.getTime(), 1.0f);
    });
    return this.schedule;
}

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/schedule/ScheduleBuilder$ActivityTransition.class */
    static class ActivityTransition {
        private final int time;
        private final Activity activity;

        public ActivityTransition(int i, Activity activity) {
            this.time = i;
            this.activity = activity;
        }

        public int getTime() {
            return this.time;
        }

        public Activity getActivity() {
            return this.activity;
        }
    }
}
