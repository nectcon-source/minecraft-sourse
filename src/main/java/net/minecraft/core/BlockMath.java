package net.minecraft.core;

import com.google.common.collect.Maps;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import java.util.EnumMap;
import java.util.function.Supplier;
import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockMath {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final EnumMap<Direction, Transformation> vanillaUvTransformLocalToGlobal = Util.make(Maps.newEnumMap(Direction.class), var0 -> {
      var0.put(Direction.SOUTH, Transformation.identity());
      var0.put(Direction.EAST, new Transformation(null, new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), 90.0F, true), null, null));
      var0.put(Direction.WEST, new Transformation(null, new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), -90.0F, true), null, null));
      var0.put(Direction.NORTH, new Transformation(null, new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), 180.0F, true), null, null));
      var0.put(Direction.UP, new Transformation(null, new Quaternion(new Vector3f(1.0F, 0.0F, 0.0F), -90.0F, true), null, null));
      var0.put(Direction.DOWN, new Transformation(null, new Quaternion(new Vector3f(1.0F, 0.0F, 0.0F), 90.0F, true), null, null));
   });
   public static final EnumMap<Direction, Transformation> vanillaUvTransformGlobalToLocal = Util.make(Maps.newEnumMap(Direction.class), var0 -> {
      for(Direction var4 : Direction.values()) {
         var0.put(var4, (vanillaUvTransformLocalToGlobal.get(var4)).inverse());
      }
   });

   public static Transformation blockCenterToCorner(Transformation var0) {
      Matrix4f var1 = Matrix4f.createTranslateMatrix(0.5F, 0.5F, 0.5F);
      var1.multiply(var0.getMatrix());
      var1.multiply(Matrix4f.createTranslateMatrix(-0.5F, -0.5F, -0.5F));
      return new Transformation(var1);
   }

   public static Transformation getUVLockTransform(Transformation var0, Direction var1, Supplier<String> var2) {
      Direction var3 = Direction.rotate(var0.getMatrix(), var1);
      Transformation var4 = var0.inverse();
      if (var4 == null) {
         LOGGER.warn(var2.get());
         return new Transformation(null, null, new Vector3f(0.0F, 0.0F, 0.0F), null);
      } else {
         Transformation var5 = (vanillaUvTransformGlobalToLocal.get(var1)).compose(var4).compose(vanillaUvTransformLocalToGlobal.get(var3));
         return blockCenterToCorner(var5);
      }
   }
}
