package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsBrokenWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsTermsScreen;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class GetServerDetailsTask extends LongRunningTask {
   private final RealmsServer server;
   private final Screen lastScreen;
   private final RealmsMainScreen mainScreen;
   private final ReentrantLock connectLock;

   public GetServerDetailsTask(RealmsMainScreen var1, Screen var2, RealmsServer var3, ReentrantLock var4) {
      this.lastScreen = var2;
      this.mainScreen = var1;
      this.server = var3;
      this.connectLock = var4;
   }

   @Override
   public void run() {
      this.setTitle(new TranslatableComponent("mco.connect.connecting"));
      RealmsClient var1 = RealmsClient.create();
      boolean var2x = false;
      boolean var3xx = false;
      int var4xxx = 5;
      RealmsServerAddress var5xxxx = null;
      boolean var6xxxxx = false;
      boolean var7xxxxxx = false;

      for(int var8xxxxxxx = 0; var8xxxxxxx < 40 && !this.aborted(); ++var8xxxxxxx) {
         try {
            var5xxxx = var1.join(this.server.id);
            var2x = true;
         } catch (RetryCallException var11) {
            var4xxx = var11.delaySeconds;
         } catch (RealmsServiceException var12) {
            if (var12.errorCode == 6002) {
               var6xxxxx = true;
            } else if (var12.errorCode == 6006) {
               var7xxxxxx = true;
            } else {
               var3xx = true;
               this.error(var12.toString());
               LOGGER.error("Couldn't connect to world", var12);
            }
            break;
         } catch (Exception var13) {
            var3xx = true;
            LOGGER.error("Couldn't connect to world", var13);
            this.error(var13.getLocalizedMessage());
            break;
         }

         if (var2x) {
            break;
         }

         this.sleep(var4xxx);
      }

      if (var6xxxxx) {
         setScreen(new RealmsTermsScreen(this.lastScreen, this.mainScreen, this.server));
      } else if (var7xxxxxx) {
         if (this.server.ownerUUID.equals(Minecraft.getInstance().getUser().getUuid())) {
            setScreen(new RealmsBrokenWorldScreen(this.lastScreen, this.mainScreen, this.server.id, this.server.worldType == RealmsServer.WorldType.MINIGAME));
         } else {
            setScreen(
               new RealmsGenericErrorScreen(
                  new TranslatableComponent("mco.brokenworld.nonowner.title"), new TranslatableComponent("mco.brokenworld.nonowner.error"), this.lastScreen
               )
            );
         }
      } else if (!this.aborted() && !var3xx) {
         if (var2x) {
//            RealmsServerAddress var5xxxxxxx = ☃xxxx;
            if (var5xxxx.resourcePackUrl != null && var5xxxx.resourcePackHash != null) {
               Component var9xxxxxxxx = new TranslatableComponent("mco.configure.world.resourcepack.question.line1");
               Component var10xxxxxxxxx = new TranslatableComponent("mco.configure.world.resourcepack.question.line2");
               RealmsServerAddress finalVar5xxxx = var5xxxx;
               setScreen(
                  new RealmsLongConfirmationScreen(
                     var2xx -> {
                        try {
                           if (var2xx) {
                              Function<Throwable, Void> var3x = var1xx -> {
                                 Minecraft.getInstance().getClientPackSource().clearServerPack();
                                 LOGGER.error(var1xx);
                                 setScreen(new RealmsGenericErrorScreen(new TextComponent("Failed to download resource pack!"), this.lastScreen));
                                 return null;
                              };

                              try {
                                 Minecraft.getInstance()
                                    .getClientPackSource()
                                    .downloadAndSelectResourcePack(finalVar5xxxx.resourcePackUrl, finalVar5xxxx.resourcePackHash)
                                    .thenRun(
                                       () -> this.setScreen(
                                             new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, this.server, finalVar5xxxx))
                                          )
                                    )
                                    .exceptionally(var3x);
                              } catch (Exception var8x) {
                                 var3x.apply(var8x);
                              }
                           } else {
                              setScreen(this.lastScreen);
                           }
                        } finally {
                           if (this.connectLock != null && this.connectLock.isHeldByCurrentThread()) {
                              this.connectLock.unlock();
                           }
                        }
                     },
                     RealmsLongConfirmationScreen.Type.Info,
                          var9xxxxxxxx,
                          var10xxxxxxxxx,
                     true
                  )
               );
            } else {
               this.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, this.server, var5xxxx)));
            }
         } else {
            this.error(new TranslatableComponent("mco.errorMessage.connectionFailure"));
         }
      }
   }

   private void sleep(int var1) {
      try {
         Thread.sleep((long)(var1 * 1000));
      } catch (InterruptedException var3) {
         LOGGER.warn(var3.getLocalizedMessage());
      }
   }
}
