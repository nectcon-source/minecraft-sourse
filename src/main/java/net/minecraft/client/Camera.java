package net.minecraft.client;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Camera {
   private boolean initialized;
   private BlockGetter level;
   private Entity entity;
   private Vec3 position = Vec3.ZERO;
   private final BlockPos.MutableBlockPos blockPosition = new BlockPos.MutableBlockPos();
   private final Vector3f forwards = new Vector3f(0.0F, 0.0F, 1.0F);
   private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
   private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);
   private float xRot;
   private float yRot;
   private final Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
   private boolean detached;
   private boolean mirror;
   private float eyeHeight;
   private float eyeHeightOld;

   public void setup(BlockGetter var1, Entity var2, boolean var3, boolean var4, float var5) {
      this.initialized = true;
      this.level = var1;
      this.entity = var2;
      this.detached = var3;
      this.mirror = var4;
      this.setRotation(var2.getViewYRot(var5), var2.getViewXRot(var5));
      this.setPosition(
         Mth.lerp(var5, var2.xo, var2.getX()),
         Mth.lerp(var5, var2.yo, var2.getY()) + (double)Mth.lerp(var5, this.eyeHeightOld, this.eyeHeight),
         Mth.lerp(var5, var2.zo, var2.getZ())
      );
      if (var3) {
         if (var4) {
            this.setRotation(this.yRot + 180.0F, -this.xRot);
         }

         this.move(-this.getMaxZoom(4.0), 0.0, 0.0);
      } else if (var2 instanceof LivingEntity && ((LivingEntity)var2).isSleeping()) {
         Direction var6 = ((LivingEntity)var2).getBedOrientation();
         this.setRotation(var6 != null ? var6.toYRot() - 180.0F : 0.0F, 0.0F);
         this.move(0.0, 0.3, 0.0);
      }
   }

   public void tick() {
      if (this.entity != null) {
         this.eyeHeightOld = this.eyeHeight;
         this.eyeHeight += (this.entity.getEyeHeight() - this.eyeHeight) * 0.5F;
      }
   }

   private double getMaxZoom(double var1) {
      for(int var3 = 0; var3 < 8; ++var3) {
         float var4x = (float)((var3 & 1) * 2 - 1);
         float var5xx = (float)((var3 >> 1 & 1) * 2 - 1);
         float var6xxx = (float)((var3 >> 2 & 1) * 2 - 1);
         var4x *= 0.1F;
         var5xx *= 0.1F;
         var6xxx *= 0.1F;
         Vec3 var7xxxx = this.position.add((double)var4x, (double)var5xx, (double)var6xxx);
         Vec3 var8xxxxx = new Vec3(
            this.position.x - (double)this.forwards.x() * var1 + (double)var4x + (double)var6xxx,
            this.position.y - (double)this.forwards.y() * var1 + (double)var5xx,
            this.position.z - (double)this.forwards.z() * var1 + (double)var6xxx
         );
         HitResult var9xxxxxx = this.level.clip(new ClipContext(var7xxxx, var8xxxxx, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, this.entity));
         if (var9xxxxxx.getType() != HitResult.Type.MISS) {
            double var10xxxxxxx = var9xxxxxx.getLocation().distanceTo(this.position);
            if (var10xxxxxxx < var1) {
               var1 = var10xxxxxxx;
            }
         }
      }

      return var1;
   }

   protected void move(double var1, double var3, double var5) {
      double var7 = (double)this.forwards.x() * var1 + (double)this.up.x() * var3 + (double)this.left.x() * var5;
      double var9x = (double)this.forwards.y() * var1 + (double)this.up.y() * var3 + (double)this.left.y() * var5;
      double var11xx = (double)this.forwards.z() * var1 + (double)this.up.z() * var3 + (double)this.left.z() * var5;
      this.setPosition(new Vec3(this.position.x + var7, this.position.y + var9x, this.position.z + var11xx));
   }

   protected void setRotation(float var1, float var2) {
      this.xRot = var2;
      this.yRot = var1;
      this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
      this.rotation.mul(Vector3f.YP.rotationDegrees(-var1));
      this.rotation.mul(Vector3f.XP.rotationDegrees(var2));
      this.forwards.set(0.0F, 0.0F, 1.0F);
      this.forwards.transform(this.rotation);
      this.up.set(0.0F, 1.0F, 0.0F);
      this.up.transform(this.rotation);
      this.left.set(1.0F, 0.0F, 0.0F);
      this.left.transform(this.rotation);
   }

   protected void setPosition(double var1, double var3, double var5) {
      this.setPosition(new Vec3(var1, var3, var5));
   }

   protected void setPosition(Vec3 var1) {
      this.position = var1;
      this.blockPosition.set(var1.x, var1.y, var1.z);
   }

   public Vec3 getPosition() {
      return this.position;
   }

   public BlockPos getBlockPosition() {
      return this.blockPosition;
   }

   public float getXRot() {
      return this.xRot;
   }

   public float getYRot() {
      return this.yRot;
   }

   public Quaternion rotation() {
      return this.rotation;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public boolean isDetached() {
      return this.detached;
   }

   public FluidState getFluidInCamera() {
      if (!this.initialized) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         FluidState var1 = this.level.getFluidState(this.blockPosition);
         return !var1.isEmpty() && this.position.y >= (double)((float)this.blockPosition.getY() + var1.getHeight(this.level, this.blockPosition))
            ? Fluids.EMPTY.defaultFluidState()
            : var1;
      }
   }

   public final Vector3f getLookVector() {
      return this.forwards;
   }

   public final Vector3f getUpVector() {
      return this.up;
   }

   public void reset() {
      this.level = null;
      this.entity = null;
      this.initialized = false;
   }
}
