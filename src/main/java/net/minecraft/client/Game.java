package net.minecraft.client;

import com.mojang.bridge.Bridge;
import com.mojang.bridge.game.GameSession;
import com.mojang.bridge.game.GameVersion;
import com.mojang.bridge.game.Language;
import com.mojang.bridge.game.PerformanceMetrics;
import com.mojang.bridge.game.RunningGame;
import com.mojang.bridge.launcher.Launcher;
import com.mojang.bridge.launcher.SessionEventListener;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.FrameTimer;

public class Game implements RunningGame {
   private final Minecraft minecraft;
   @Nullable
   private final Launcher launcher;
   private SessionEventListener listener = SessionEventListener.NONE;

   public Game(Minecraft var1) {
      this.minecraft = var1;
      this.launcher = Bridge.getLauncher();
      if (this.launcher != null) {
         this.launcher.registerGame(this);
      }
   }

   public GameVersion getVersion() {
      return SharedConstants.getCurrentVersion();
   }

   public Language getSelectedLanguage() {
      return this.minecraft.getLanguageManager().getSelected();
   }

   @Nullable
   public GameSession getCurrentSession() {
      ClientLevel var0 = this.minecraft.level;
      return var0 == null ? null : new Session(var0, this.minecraft.player, this.minecraft.player.connection);
   }

   public PerformanceMetrics getPerformanceMetrics() {
      FrameTimer var1 = this.minecraft.getFrameTimer();
      long var2x = 2147483647L;
      long var4xx = -2147483648L;
      long var6xxx = 0L;

      for(long var11 : var1.getLog()) {
         var2x = Math.min(var2x, var11);
         var4xx = Math.max(var4xx, var11);
         var6xxx += var11;
      }

      return new Game.Metrics((int)var2x, (int)var4xx, (int)(var6xxx / (long)var1.getLog().length), var1.getLog().length);
   }

   public void setSessionEventListener(SessionEventListener var1) {
      this.listener = var1;
   }

   public void onStartGameSession() {
      this.listener.onStartGameSession(this.getCurrentSession());
   }

   public void onLeaveGameSession() {
      this.listener.onLeaveGameSession(this.getCurrentSession());
   }

   static class Metrics implements PerformanceMetrics {
      private final int min;
      private final int max;
      private final int average;
      private final int samples;

      public Metrics(int var1, int var2, int var3, int var4) {
         this.min = var1;
         this.max = var2;
         this.average = var3;
         this.samples = var4;
      }

      public int getMinTime() {
         return this.min;
      }

      public int getMaxTime() {
         return this.max;
      }

      public int getAverageTime() {
         return this.average;
      }

      public int getSampleCount() {
         return this.samples;
      }
   }
}
