

package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatComponent extends GuiComponent {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Minecraft minecraft;
   private final List<String> recentChat = Lists.newArrayList();
   private final List<GuiMessage<Component>> allMessages = Lists.newArrayList();
   private final List<GuiMessage<FormattedCharSequence>> trimmedMessages = Lists.newArrayList();
   private final Deque<Component> chatQueue = Queues.newArrayDeque();
   private int chatScrollbarPos;
   private boolean newMessageSinceScroll;
   private long lastMessage = 0L;

   public ChatComponent(Minecraft var1) {
      this.minecraft = var1;
   }

   public void render(PoseStack var1, int var2) {
      if (!this.isChatHidden()) {
         this.processPendingMessages();
         int var3 = this.getLinesPerPage();
         int var4 = this.trimmedMessages.size();
         if (var4 > 0) {
            boolean var5 = false;
            if (this.isChatFocused()) {
               var5 = true;
            }

            double var6 = this.getScale();
            int var8 = Mth.ceil((double)this.getWidth() / var6);
            RenderSystem.pushMatrix();
            RenderSystem.translatef(2.0F, 8.0F, 0.0F);
            RenderSystem.scaled(var6, var6, (double)1.0F);
            double var9 = this.minecraft.options.chatOpacity * (double)0.9F + (double)0.1F;
            double var11 = this.minecraft.options.textBackgroundOpacity;
            double var13 = (double)9.0F * (this.minecraft.options.chatLineSpacing + (double)1.0F);
            double var15 = (double)-8.0F * (this.minecraft.options.chatLineSpacing + (double)1.0F) + (double)4.0F * this.minecraft.options.chatLineSpacing;
            int var17 = 0;

            for(int var18 = 0; var18 + this.chatScrollbarPos < this.trimmedMessages.size() && var18 < var3; ++var18) {
               GuiMessage<FormattedCharSequence> var19 = (GuiMessage)this.trimmedMessages.get(var18 + this.chatScrollbarPos);
               if (var19 != null) {
                  int var20 = var2 - var19.getAddedTime();
                  if (var20 < 200 || var5) {
                     double var21 = var5 ? (double)1.0F : getTimeFactor(var20);
                     int var23 = (int)((double)255.0F * var21 * var9);
                     int var24 = (int)((double)255.0F * var21 * var11);
                     ++var17;
                     if (var23 > 3) {
                        int var25 = 0;
                        double var26 = (double)(-var18) * var13;
                        var1.pushPose();
                        var1.translate((double)0.0F, (double)0.0F, (double)50.0F);
                        fill(var1, -2, (int)(var26 - var13), 0 + var8 + 4, (int)var26, var24 << 24);
                        RenderSystem.enableBlend();
                        var1.translate((double)0.0F, (double)0.0F, (double)50.0F);
                        this.minecraft.font.drawShadow(var1, (FormattedCharSequence)var19.getMessage(), 0.0F, (float)((int)(var26 + var15)), 16777215 + (var23 << 24));
                        RenderSystem.disableAlphaTest();
                        RenderSystem.disableBlend();
                        var1.popPose();
                     }
                  }
               }
            }

            if (!this.chatQueue.isEmpty()) {
               int var28 = (int)((double)128.0F * var9);
               int var30 = (int)((double)255.0F * var11);
               var1.pushPose();
               var1.translate((double)0.0F, (double)0.0F, (double)50.0F);
               fill(var1, -2, 0, var8 + 4, 9, var30 << 24);
               RenderSystem.enableBlend();
               var1.translate((double)0.0F, (double)0.0F, (double)50.0F);
               this.minecraft.font.drawShadow(var1, new TranslatableComponent("chat.queue", new Object[]{this.chatQueue.size()}), 0.0F, 1.0F, 16777215 + (var28 << 24));
               var1.popPose();
               RenderSystem.disableAlphaTest();
               RenderSystem.disableBlend();
            }

            if (var5) {
               this.minecraft.font.getClass();
               int var29 = 9;
               RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
               int var31 = var4 * var29 + var4;
               int var32 = var17 * var29 + var17;
               int var33 = this.chatScrollbarPos * var32 / var4;
               int var22 = var32 * var32 / var31;
               if (var31 != var32) {
                  int var34 = var33 > 0 ? 170 : 96;
                  int var35 = this.newMessageSinceScroll ? 13382451 : 3355562;
                  fill(var1, 0, -var33, 2, -var33 - var22, var35 + (var34 << 24));
                  fill(var1, 2, -var33, 1, -var33 - var22, 13421772 + (var34 << 24));
               }
            }

            RenderSystem.popMatrix();
         }
      }
   }

   private boolean isChatHidden() {
      return this.minecraft.options.chatVisibility == ChatVisiblity.HIDDEN;
   }

   private static double getTimeFactor(int var0) {
      double var1 = (double)var0 / (double)200.0F;
      var1 = (double)1.0F - var1;
      var1 *= (double)10.0F;
      var1 = Mth.clamp(var1, (double)0.0F, (double)1.0F);
      var1 *= var1;
      return var1;
   }

   public void clearMessages(boolean var1) {
      this.chatQueue.clear();
      this.trimmedMessages.clear();
      this.allMessages.clear();
      if (var1) {
         this.recentChat.clear();
      }

   }

   public void addMessage(Component var1) {
      this.addMessage(var1, 0);
   }

   private void addMessage(Component var1, int var2) {
      this.addMessage(var1, var2, this.minecraft.gui.getGuiTicks(), false);
      LOGGER.info("[CHAT] {}", var1.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
   }

   private void addMessage(Component var1, int var2, int var3, boolean var4) {
      if (var2 != 0) {
         this.removeById(var2);
      }

      int var5 = Mth.floor((double)this.getWidth() / this.getScale());
      List<FormattedCharSequence> var6 = ComponentRenderUtils.wrapComponents(var1, var5, this.minecraft.font);
      boolean var7 = this.isChatFocused();

      for(FormattedCharSequence var9 : var6) {
         if (var7 && this.chatScrollbarPos > 0) {
            this.newMessageSinceScroll = true;
            this.scrollChat((double)1.0F);
         }

         this.trimmedMessages.add(0, new GuiMessage(var3, var9, var2));
      }

      while(this.trimmedMessages.size() > 100) {
         this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
      }

      if (!var4) {
         this.allMessages.add(0, new GuiMessage(var3, var1, var2));

         while(this.allMessages.size() > 100) {
            this.allMessages.remove(this.allMessages.size() - 1);
         }
      }

   }

   public void rescaleChat() {
      this.trimmedMessages.clear();
      this.resetChatScroll();

      for(int var1 = this.allMessages.size() - 1; var1 >= 0; --var1) {
         GuiMessage<Component> var2 = (GuiMessage)this.allMessages.get(var1);
         this.addMessage((Component)var2.getMessage(), var2.getId(), var2.getAddedTime(), true);
      }

   }

   public List<String> getRecentChat() {
      return this.recentChat;
   }

   public void addRecentChat(String var1) {
      if (this.recentChat.isEmpty() || !((String)this.recentChat.get(this.recentChat.size() - 1)).equals(var1)) {
         this.recentChat.add(var1);
      }

   }

   public void resetChatScroll() {
      this.chatScrollbarPos = 0;
      this.newMessageSinceScroll = false;
   }

   public void scrollChat(double var1) {
      this.chatScrollbarPos = (int)((double)this.chatScrollbarPos + var1);
      int var3 = this.trimmedMessages.size();
      if (this.chatScrollbarPos > var3 - this.getLinesPerPage()) {
         this.chatScrollbarPos = var3 - this.getLinesPerPage();
      }

      if (this.chatScrollbarPos <= 0) {
         this.chatScrollbarPos = 0;
         this.newMessageSinceScroll = false;
      }

   }

   public boolean handleChatQueueClicked(double var1, double var3) {
      if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden() && !this.chatQueue.isEmpty()) {
         double var5 = var1 - (double)2.0F;
         double var7 = (double)this.minecraft.getWindow().getGuiScaledHeight() - var3 - (double)40.0F;
         if (var5 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && var7 < (double)0.0F && var7 > (double)Mth.floor((double)-9.0F * this.getScale())) {
            this.addMessage((Component)this.chatQueue.remove());
            this.lastMessage = System.currentTimeMillis();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Nullable
   public Style getClickedComponentStyleAt(double var1, double var3) {
      if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
         double var5 = var1 - (double)2.0F;
         double var7 = (double)this.minecraft.getWindow().getGuiScaledHeight() - var3 - (double)40.0F;
         var5 = (double)Mth.floor(var5 / this.getScale());
         var7 = (double)Mth.floor(var7 / (this.getScale() * (this.minecraft.options.chatLineSpacing + (double)1.0F)));
         if (!(var5 < (double)0.0F) && !(var7 < (double)0.0F)) {
            int var9 = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
            if (var5 <= (double)Mth.floor((double)this.getWidth() / this.getScale())) {
               this.minecraft.font.getClass();
               if (var7 < (double)(9 * var9 + var9)) {
                  this.minecraft.font.getClass();
                  int var10 = (int)(var7 / (double)9.0F + (double)this.chatScrollbarPos);
                  if (var10 >= 0 && var10 < this.trimmedMessages.size()) {
                     GuiMessage<FormattedCharSequence> var11 = (GuiMessage)this.trimmedMessages.get(var10);
                     return this.minecraft.font.getSplitter().componentStyleAtWidth((FormattedCharSequence)var11.getMessage(), (int)var5);
                  }
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private boolean isChatFocused() {
      return this.minecraft.screen instanceof ChatScreen;
   }

   private void removeById(int var1) {
      this.trimmedMessages.removeIf((var1x) -> var1x.getId() == var1);
      this.allMessages.removeIf((var1x) -> var1x.getId() == var1);
   }

   public int getWidth() {
      return getWidth(this.minecraft.options.chatWidth);
   }

   public int getHeight() {
      return getHeight((this.isChatFocused() ? this.minecraft.options.chatHeightFocused : this.minecraft.options.chatHeightUnfocused) / (this.minecraft.options.chatLineSpacing + (double)1.0F));
   }

   public double getScale() {
      return this.minecraft.options.chatScale;
   }

   public static int getWidth(double var0) {
      int var2 = 320;
      int var3 = 40;
      return Mth.floor(var0 * (double)280.0F + (double)40.0F);
   }

   public static int getHeight(double var0) {
      int var2 = 180;
      int var3 = 20;
      return Mth.floor(var0 * (double)160.0F + (double)20.0F);
   }

   public int getLinesPerPage() {
      return this.getHeight() / 9;
   }

   private long getChatRateMillis() {
      return (long)(this.minecraft.options.chatDelay * (double)1000.0F);
   }

   private void processPendingMessages() {
      if (!this.chatQueue.isEmpty()) {
         long var1 = System.currentTimeMillis();
         if (var1 - this.lastMessage >= this.getChatRateMillis()) {
            this.addMessage((Component)this.chatQueue.remove());
            this.lastMessage = var1;
         }

      }
   }

   public void enqueueMessage(Component var1) {
      if (this.minecraft.options.chatDelay <= (double)0.0F) {
         this.addMessage(var1);
      } else {
         long var2 = System.currentTimeMillis();
         if (var2 - this.lastMessage >= this.getChatRateMillis()) {
            this.addMessage(var1);
            this.lastMessage = var2;
         } else {
            this.chatQueue.add(var1);
         }
      }

   }
}
