//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import net.minecraft.network.chat.TranslatableComponent;

public class RestoreTask extends LongRunningTask {
   private final Backup backup;
   private final long worldId;
   private final RealmsConfigureWorldScreen lastScreen;

   public RestoreTask(Backup var1, long var2, RealmsConfigureWorldScreen var4) {
      this.backup = var1;
      this.worldId = var2;
      this.lastScreen = var4;
   }

   public void run() {
      this.setTitle(new TranslatableComponent("mco.backup.restoring"));
      RealmsClient var1 = RealmsClient.create();
      int var2 = 0;

      while(var2 < 25) {
         try {
            if (this.aborted()) {
               return;
            }

            var1.restoreWorld(this.worldId, this.backup.backupId);
            pause(1);
            if (this.aborted()) {
               return;
            }

            setScreen(this.lastScreen.getNewScreen());
            return;
         } catch (RetryCallException var3_7) {
            if (this.aborted()) {
               return;
            }

            pause(var3_7.delaySeconds);
            ++var2;
         } catch (RealmsServiceException var3_4) {
            if (this.aborted()) {
               return;
            }

            LOGGER.error("Couldn't restore backup", var3_4);
            setScreen(new RealmsGenericErrorScreen(var3_4, this.lastScreen));
            return;
         } catch (Exception var3_1) {
            if (this.aborted()) {
               return;
            }

            LOGGER.error("Couldn't restore backup", var3_1);
            this.error(var3_1.getLocalizedMessage());
            return;
         }
      }

   }
}
