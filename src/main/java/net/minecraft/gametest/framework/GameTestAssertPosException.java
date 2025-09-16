package net.minecraft.gametest.framework;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public class GameTestAssertPosException extends GameTestAssertException {
   private final BlockPos absolutePos = null;
   private final BlockPos relativePos = null;
   private final long tick = 0L;

   public GameTestAssertPosException(String var1) {
      super(var1);
   }

   @Override
   public String getMessage() {
      String var1 = ""
         + this.absolutePos.getX()
         + ","
         + this.absolutePos.getY()
         + ","
         + this.absolutePos.getZ()
         + " (relative: "
         + this.relativePos.getX()
         + ","
         + this.relativePos.getY()
         + ","
         + this.relativePos.getZ()
         + ")";
      return super.getMessage() + " at " + var1 + " (t=" + this.tick + ")";
   }

   @Nullable
   public String getMessageToShowAtBlock() {
      return super.getMessage() + " here";
   }

   @Nullable
   public BlockPos getAbsolutePos() {
      return this.absolutePos;
   }
}
