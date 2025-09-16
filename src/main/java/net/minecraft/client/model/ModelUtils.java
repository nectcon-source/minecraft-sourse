

package net.minecraft.client.model;

public class ModelUtils {
   public static float rotlerpRad(float var0, float var1, float var2) {
      float var3;
      for(var3 = var1 - var0; var3 < -(float)Math.PI; var3 += ((float)Math.PI * 2F)) {
      }

      while(var3 >= (float)Math.PI) {
         var3 -= ((float)Math.PI * 2F);
      }

      return var0 + var2 * var3;
   }
}
