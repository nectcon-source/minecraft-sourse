//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RetryCallException;
import net.minecraft.network.chat.TranslatableComponent;

public class SwitchSlotTask extends LongRunningTask {
   private final long worldId;
   private final int slot;
   private final Runnable callback;

   public SwitchSlotTask(long var1, int var3, Runnable var4) {
      this.worldId = var1;
      this.slot = var3;
      this.callback = var4;
   }

   public void run() {
      RealmsClient var1 = RealmsClient.create();
      this.setTitle(new TranslatableComponent("mco.minigame.world.slot.screen.title"));

      for(int var2 = 0; var2 < 25; ++var2) {
         try {
            if (this.aborted()) {
               return;
            }

            if (var1.switchSlot(this.worldId, this.slot)) {
               this.callback.run();
               break;
            }
         } catch (RetryCallException var3_3) {
            if (this.aborted()) {
               return;
            }

            pause(var3_3.delaySeconds);
         } catch (Exception var3_1) {
            if (this.aborted()) {
               return;
            }

            LOGGER.error("Couldn't switch world!");
            this.error(var3_1.toString());
         }
      }

   }
}
