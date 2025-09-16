package net.minecraft.world.entity.ai.goal;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/GolemRandomStrollInVillageGoal.class */
public class GolemRandomStrollInVillageGoal extends RandomStrollGoal {
    public GolemRandomStrollInVillageGoal(PathfinderMob pathfinderMob, double d) {
        super(pathfinderMob, d, 240, false);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.RandomStrollGoal
    @Nullable
    protected Vec3 getPosition() {
        Vec3 positionTowardsPoi;
        float nextFloat = this.mob.level.random.nextFloat();
        if (this.mob.level.random.nextFloat() < 0.3f) {
            return getPositionTowardsAnywhere();
        }
        if (nextFloat < 0.7f) {
            positionTowardsPoi = getPositionTowardsVillagerWhoWantsGolem();
            if (positionTowardsPoi == null) {
                positionTowardsPoi = getPositionTowardsPoi();
            }
        } else {
            positionTowardsPoi = getPositionTowardsPoi();
            if (positionTowardsPoi == null) {
                positionTowardsPoi = getPositionTowardsVillagerWhoWantsGolem();
            }
        }
        return positionTowardsPoi == null ? getPositionTowardsAnywhere() : positionTowardsPoi;
    }

    @Nullable
    private Vec3 getPositionTowardsAnywhere() {
        return RandomPos.getLandPos(this.mob, 10, 7);
    }

    @Nullable
    private Vec3 getPositionTowardsVillagerWhoWantsGolem() {
        List<Villager> entities = ((ServerLevel) this.mob.level).getEntities(EntityType.VILLAGER, this.mob.getBoundingBox().inflate(32.0d), this::doesVillagerWantGolem);
        if (entities.isEmpty()) {
            return null;
        }
        return RandomPos.getLandPosTowards(this.mob, 10, 7, entities.get(this.mob.level.random.nextInt(entities.size())).position());
    }

    @Nullable
    private Vec3 getPositionTowardsPoi() {
        BlockPos randomPoiWithinSection;
        SectionPos randomVillageSection = getRandomVillageSection();
        if (randomVillageSection == null || (randomPoiWithinSection = getRandomPoiWithinSection(randomVillageSection)) == null) {
            return null;
        }
        return RandomPos.getLandPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf(randomPoiWithinSection));
    }

    @Nullable
    private SectionPos getRandomVillageSection() {
        ServerLevel serverLevel = (ServerLevel) this.mob.level;
        List<SectionPos> list =  SectionPos.cube(SectionPos.of(this.mob), 2).filter(sectionPos -> {
            return serverLevel.sectionsToVillage(sectionPos) == 0;
        }).collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        }
        return list.get(serverLevel.random.nextInt(list.size()));
    }

    @Nullable
    private BlockPos getRandomPoiWithinSection(SectionPos sectionPos) {
        ServerLevel serverLevel = (ServerLevel) this.mob.level;
        List<BlockPos> list = (List) serverLevel.getPoiManager().getInRange(poiType -> {
            return true;
        }, sectionPos.center(), 8, PoiManager.Occupancy.IS_OCCUPIED).map((v0) -> {
            return v0.getPos();
        }).collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        }
        return list.get(serverLevel.random.nextInt(list.size()));
    }

    private boolean doesVillagerWantGolem(Villager villager) {
        return villager.wantsToSpawnGolem(this.mob.level.getGameTime());
    }
}
