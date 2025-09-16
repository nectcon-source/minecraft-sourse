package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class Explosion {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private final boolean fire;
    private final BlockInteraction blockInteraction;
    private final Random random;
    private final Level level;
    private final double x;
    private final double y;
    private final double z;
    @Nullable
    private final Entity source;
    private final float radius;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final List<BlockPos> toBlow;
    private final Map<Player, Vec3> hitPlayers;

    public Explosion(Level var1, @Nullable Entity var2, double var3, double var5, double var7, float var9, List<BlockPos> var10) {
        this(var1, var2, var3, var5, var7, var9, false, Explosion.BlockInteraction.DESTROY, var10);
    }

    public Explosion(Level var1, @Nullable Entity var2, double var3, double var5, double var7, float var9, boolean var10, BlockInteraction var11, List<BlockPos> var12) {
        this(var1, var2, var3, var5, var7, var9, var10, var11);
        this.toBlow.addAll(var12);
    }

    public Explosion(Level var1, @Nullable Entity var2, double var3, double var5, double var7, float var9, boolean var10, BlockInteraction var11) {
        this(var1, var2, null, null, var3, var5, var7, var9, var10, var11);
    }

    public Explosion(Level var1, @Nullable Entity var2, @Nullable DamageSource var3, @Nullable ExplosionDamageCalculator var4, double var5, double var7, double var9, float var11, boolean var12, BlockInteraction var13) {
        this.random = new Random();
        this.toBlow = Lists.newArrayList();
        this.hitPlayers = Maps.newHashMap();
        this.level = var1;
        this.source = var2;
        this.radius = var11;
        this.x = var5;
        this.y = var7;
        this.z = var9;
        this.fire = var12;
        this.blockInteraction = var13;
        this.damageSource = var3 == null ? DamageSource.explosion(this) : var3;
        this.damageCalculator = var4 == null ? this.makeDamageCalculator(var2) : var4;
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity var1) {
        return (var1 == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(var1));
    }

    public static float getSeenPercent(Vec3 var0, Entity var1) {
        AABB var2 = var1.getBoundingBox();
        double var3 = (double)1.0F / ((var2.maxX - var2.minX) * (double)2.0F + (double)1.0F);
        double var5 = (double)1.0F / ((var2.maxY - var2.minY) * (double)2.0F + (double)1.0F);
        double var7 = (double)1.0F / ((var2.maxZ - var2.minZ) * (double)2.0F + (double)1.0F);
        double var9 = ((double)1.0F - Math.floor((double)1.0F / var3) * var3) / (double)2.0F;
        double var11 = ((double)1.0F - Math.floor((double)1.0F / var7) * var7) / (double)2.0F;
        if (!(var3 < (double)0.0F) && !(var5 < (double)0.0F) && !(var7 < (double)0.0F)) {
            int var13 = 0;
            int var14 = 0;

            for(float var15 = 0.0F; var15 <= 1.0F; var15 = (float)((double)var15 + var3)) {
                for(float var16 = 0.0F; var16 <= 1.0F; var16 = (float)((double)var16 + var5)) {
                    for(float var17 = 0.0F; var17 <= 1.0F; var17 = (float)((double)var17 + var7)) {
                        double var18 = Mth.lerp(var15, var2.minX, var2.maxX);
                        double var20 = Mth.lerp(var16, var2.minY, var2.maxY);
                        double var22 = Mth.lerp(var17, var2.minZ, var2.maxZ);
                        Vec3 var24 = new Vec3(var18 + var9, var20, var22 + var11);
                        if (var1.level.clip(new ClipContext(var24, var0, Block.COLLIDER, Fluid.NONE, var1)).getType() == Type.MISS) {
                            ++var13;
                        }

                        ++var14;
                    }
                }
            }

            return (float)var13 / (float)var14;
        } else {
            return 0.0F;
        }
    }

    public void explode() {
        Set<BlockPos> var1 = Sets.newHashSet();
        int var2 = 16;

        for(int var3 = 0; var3 < 16; ++var3) {
            for(int var4 = 0; var4 < 16; ++var4) {
                for(int var5 = 0; var5 < 16; ++var5) {
                    if (var3 == 0 || var3 == 15 || var4 == 0 || var4 == 15 || var5 == 0 || var5 == 15) {
                        double var6 = (double)((float)var3 / 15.0F * 2.0F - 1.0F);
                        double var8 = (double)((float)var4 / 15.0F * 2.0F - 1.0F);
                        double var10 = (double)((float)var5 / 15.0F * 2.0F - 1.0F);
                        double var12 = Math.sqrt(var6 * var6 + var8 * var8 + var10 * var10);
                        var6 /= var12;
                        var8 /= var12;
                        var10 /= var12;
                        float var14 = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                        double var15 = this.x;
                        double var17 = this.y;
                        double var19 = this.z;

                        for(float var21 = 0.3F; var14 > 0.0F; var14 -= 0.22500001F) {
                            BlockPos var22 = new BlockPos(var15, var17, var19);
                            BlockState var23 = this.level.getBlockState(var22);
                            FluidState var24 = this.level.getFluidState(var22);
                            Optional<Float> var25 = this.damageCalculator.getBlockExplosionResistance(this, this.level, var22, var23, var24);
                            if (var25.isPresent()) {
                                var14 -= ((Float)var25.get() + 0.3F) * 0.3F;
                            }

                            if (var14 > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, var22, var23, var14)) {
                                var1.add(var22);
                            }

                            var15 += var6 * (double)0.3F;
                            var17 += var8 * (double)0.3F;
                            var19 += var10 * (double)0.3F;
                        }
                    }
                }
            }
        }

        this.toBlow.addAll(var1);
        float var31 = this.radius * 2.0F;
        int var32 = Mth.floor(this.x - (double)var31 - (double)1.0F);
        int var33 = Mth.floor(this.x + (double)var31 + (double)1.0F);
        int var35 = Mth.floor(this.y - (double)var31 - (double)1.0F);
        int var7 = Mth.floor(this.y + (double)var31 + (double)1.0F);
        int var37 = Mth.floor(this.z - (double)var31 - (double)1.0F);
        int var9 = Mth.floor(this.z + (double)var31 + (double)1.0F);
        List<Entity> var39 = this.level.getEntities(this.source, new AABB((double)var32, (double)var35, (double)var37, (double)var33, (double)var7, (double)var9));
        Vec3 var11 = new Vec3(this.x, this.y, this.z);

        for(int var40 = 0; var40 < var39.size(); ++var40) {
            Entity var13 = (Entity)var39.get(var40);
            if (!var13.ignoreExplosion()) {
                double var41 = (double)(Mth.sqrt(var13.distanceToSqr(var11)) / var31);
                if (var41 <= (double)1.0F) {
                    double var16 = var13.getX() - this.x;
                    double var18 = (var13 instanceof PrimedTnt ? var13.getY() : var13.getEyeY()) - this.y;
                    double var20 = var13.getZ() - this.z;
                    double var45 = (double)Mth.sqrt(var16 * var16 + var18 * var18 + var20 * var20);
                    if (var45 != (double)0.0F) {
                        var16 /= var45;
                        var18 /= var45;
                        var20 /= var45;
                        double var46 = (double)getSeenPercent(var11, var13);
                        double var26 = ((double)1.0F - var41) * var46;
                        var13.hurt(this.getDamageSource(), (float)((int)((var26 * var26 + var26) / (double)2.0F * (double)7.0F * (double)var31 + (double)1.0F)));
                        double var28 = var26;
                        if (var13 instanceof LivingEntity) {
                            var28 = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity)var13, var26);
                        }

                        var13.setDeltaMovement(var13.getDeltaMovement().add(var16 * var28, var18 * var28, var20 * var28));
                        if (var13 instanceof Player) {
                            Player var30 = (Player)var13;
                            if (!var30.isSpectator() && (!var30.isCreative() || !var30.abilities.flying)) {
                                this.hitPlayers.put(var30, new Vec3(var16 * var26, var18 * var26, var20 * var26));
                            }
                        }
                    }
                }
            }
        }

    }

    public void finalizeExplosion(boolean var1) {
        if (this.level.isClientSide) {
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        boolean var2 = this.blockInteraction != Explosion.BlockInteraction.NONE;
        if (var1) {
            if (!(this.radius < 2.0F) && var2) {
                this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, (double)1.0F, (double)0.0F, (double)0.0F);
            } else {
                this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, (double)1.0F, (double)0.0F, (double)0.0F);
            }
        }

        if (var2) {
            ObjectArrayList<Pair<ItemStack, BlockPos>> var3 = new ObjectArrayList();
            Collections.shuffle(this.toBlow, this.level.random);

            for(BlockPos var5 : this.toBlow) {
                BlockState var6 = this.level.getBlockState(var5);
                net.minecraft.world.level.block.Block var7 = var6.getBlock();
                if (!var6.isAir()) {
                    BlockPos var8 = var5.immutable();
                    this.level.getProfiler().push("explosion_blocks");
                    if (var7.dropFromExplosion(this) && this.level instanceof ServerLevel) {
                        BlockEntity var9 = var7.isEntityBlock() ? this.level.getBlockEntity(var5) : null;
                        LootContext.Builder var10 = (new LootContext.Builder((ServerLevel)this.level)).withRandom(this.level.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(var5)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, var9).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                        if (this.blockInteraction == Explosion.BlockInteraction.DESTROY) {
                            var10.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
                        }

                        var6.getDrops(var10).forEach((var2x) -> addBlockDrops(var3, var2x, var8));
                    }

                    this.level.setBlock(var5, Blocks.AIR.defaultBlockState(), 3);
                    var7.wasExploded(this.level, var5, this);
                    this.level.getProfiler().pop();
                }
            }

            ObjectListIterator var12 = var3.iterator();

            while(var12.hasNext()) {
                Pair<ItemStack, BlockPos> var14 = (Pair)var12.next();
                net.minecraft.world.level.block.Block.popResource(this.level, (BlockPos)var14.getSecond(), (ItemStack)var14.getFirst());
            }
        }

        if (this.fire) {
            for(BlockPos var13 : this.toBlow) {
                if (this.random.nextInt(3) == 0 && this.level.getBlockState(var13).isAir() && this.level.getBlockState(var13.below()).isSolidRender(this.level, var13.below())) {
                    this.level.setBlockAndUpdate(var13, BaseFireBlock.getState(this.level, var13));
                }
            }
        }

    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> var0, ItemStack var1, BlockPos var2) {
        int var3 = var0.size();

        for(int var4 = 0; var4 < var3; ++var4) {
            Pair<ItemStack, BlockPos> var5 = var0.get(var4);
            ItemStack var6 = var5.getFirst();
            if (ItemEntity.areMergable(var6, var1)) {
                ItemStack var7 = ItemEntity.merge(var6, var1, 16);
                var0.set(var4, Pair.of(var7, var5.getSecond()));
                if (var1.isEmpty()) {
                    return;
                }
            }
        }

        var0.add(Pair.of(var1, var2));
    }

    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    public Map<Player, Vec3> getHitPlayers() {
        return this.hitPlayers;
    }

    @Nullable
    public LivingEntity getSourceMob() {
        if (this.source == null) {
            return null;
        } else if (this.source instanceof PrimedTnt) {
            return ((PrimedTnt)this.source).getOwner();
        } else if (this.source instanceof LivingEntity) {
            return (LivingEntity)this.source;
        } else {
            if (this.source instanceof Projectile) {
                Entity var1 = ((Projectile)this.source).getOwner();
                if (var1 instanceof LivingEntity) {
                    return (LivingEntity)var1;
                }
            }

            return null;
        }
    }

    public void clearToBlow() {
        this.toBlow.clear();
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }

    public static enum BlockInteraction {
        NONE,
        BREAK,
        DESTROY;

        private BlockInteraction() {
        }
    }
}
