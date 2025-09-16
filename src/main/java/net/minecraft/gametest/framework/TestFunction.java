package net.minecraft.gametest.framework;

import java.util.function.Consumer;
import net.minecraft.world.level.block.Rotation;

public class TestFunction {
   private final String batchName = null;
   private final String testName = null;
   private final String structureName = null;
   private final boolean required = false;
   private final Consumer<GameTestHelper> function = null;
   private final int maxTicks = 0;
   private final long setupTicks = 0;
   private final Rotation rotation = null;

   public void run(GameTestHelper var1) {
      this.function.accept(var1);
   }

   public String getTestName() {
      return this.testName;
   }

   public String getStructureName() {
      return this.structureName;
   }

   @Override
   public String toString() {
      return this.testName;
   }

   public int getMaxTicks() {
      return this.maxTicks;
   }

   public boolean isRequired() {
      return this.required;
   }

   public String getBatchName() {
      return this.batchName;
   }

   public long getSetupTicks() {
      return this.setupTicks;
   }

   public Rotation getRotation() {
      return this.rotation;
   }
}
