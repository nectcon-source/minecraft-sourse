package net.minecraft.client.gui;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.chat.ChatListener;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.chat.OverlayChatListener;
import net.minecraft.client.gui.chat.StandardChatListener;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.apache.commons.lang3.StringUtils;

public class Gui extends GuiComponent {
   private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
   private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
   private static final ResourceLocation PUMPKIN_BLUR_LOCATION = new ResourceLocation("textures/misc/pumpkinblur.png");
   private static final Component DEMO_EXPIRED_TEXT = new TranslatableComponent("demo.demoExpired");
   private final Random random = new Random();
   private final Minecraft minecraft;
   private final ItemRenderer itemRenderer;
   private final ChatComponent chat;
   private int tickCount;
   @Nullable
   private Component overlayMessageString;
   private int overlayMessageTime;
   private boolean animateOverlayMessageColor;
   public float vignetteBrightness = 1.0F;
   private int toolHighlightTimer;
   private ItemStack lastToolHighlight = ItemStack.EMPTY;
   private final DebugScreenOverlay debugScreen;
   private final SubtitleOverlay subtitleOverlay;
   private final SpectatorGui spectatorGui;
   private final PlayerTabOverlay tabList;
   private final BossHealthOverlay bossOverlay;
   private int titleTime;
   @Nullable
   private Component title;
   @Nullable
   private Component subtitle;
   private int titleFadeInTime;
   private int titleStayTime;
   private int titleFadeOutTime;
   private int lastHealth;
   private int displayHealth;
   private long lastHealthTime;
   private long healthBlinkTime;
   private int screenWidth;
   private int screenHeight;
   private final Map<ChatType, List<ChatListener>> chatListeners = Maps.newHashMap();

   public Gui(Minecraft var1) {
      this.lastToolHighlight = ItemStack.EMPTY;
      this.minecraft = var1;
      this.itemRenderer = var1.getItemRenderer();
      this.debugScreen = new DebugScreenOverlay(var1);
      this.spectatorGui = new SpectatorGui(var1);
      this.chat = new ChatComponent(var1);
      this.tabList = new PlayerTabOverlay(var1, this);
      this.bossOverlay = new BossHealthOverlay(var1);
      this.subtitleOverlay = new SubtitleOverlay(var1);

      for(ChatType var5 : ChatType.values()) {
         this.chatListeners.put(var5, Lists.newArrayList());
      }

      ChatListener var6 = NarratorChatListener.INSTANCE;
      (this.chatListeners.get(ChatType.CHAT)).add(new StandardChatListener(var1));
      (this.chatListeners.get(ChatType.CHAT)).add(var6);
      (this.chatListeners.get(ChatType.SYSTEM)).add(new StandardChatListener(var1));
      (this.chatListeners.get(ChatType.SYSTEM)).add(var6);
      (this.chatListeners.get(ChatType.GAME_INFO)).add(new OverlayChatListener(var1));
      this.resetTitleTimes();
   }

   public void resetTitleTimes() {
      this.titleFadeInTime = 10;
      this.titleStayTime = 70;
      this.titleFadeOutTime = 20;
   }

   public void render(PoseStack var1, float var2) {
      this.screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
      this.screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
      Font var3 = this.getFont();
      RenderSystem.enableBlend();
      if (Minecraft.useFancyGraphics()) {
         this.renderVignette(this.minecraft.getCameraEntity());
      } else {
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
      }

      ItemStack var4 = this.minecraft.player.inventory.getArmor(3);
      if (this.minecraft.options.getCameraType().isFirstPerson() && var4.getItem() == Blocks.CARVED_PUMPKIN.asItem()) {
         this.renderPumpkin();
      }

      float var5 = Mth.lerp(var2, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
      if (var5 > 0.0F && !this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
         this.renderPortalOverlay(var5);
      }

      if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
         this.spectatorGui.renderHotbar(var1, var2);
      } else if (!this.minecraft.options.hideGui) {
         this.renderHotbar(var2, var1);
      }

      if (!this.minecraft.options.hideGui) {
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
         RenderSystem.enableBlend();
         RenderSystem.enableAlphaTest();
         this.renderCrosshair(var1);
         RenderSystem.defaultBlendFunc();
         this.minecraft.getProfiler().push("bossHealth");
         this.bossOverlay.render(var1);
         this.minecraft.getProfiler().pop();
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
         if (this.minecraft.gameMode.canHurtPlayer()) {
            this.renderPlayerHealth(var1);
         }

         this.renderVehicleHealth(var1);
         RenderSystem.disableBlend();
         int var6 = this.screenWidth / 2 - 91;
         if (this.minecraft.player.isRidingJumpable()) {
            this.renderJumpMeter(var1, var6);
         } else if (this.minecraft.gameMode.hasExperience()) {
            this.renderExperienceBar(var1, var6);
         }

         if (this.minecraft.options.heldItemTooltips && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            this.renderSelectedItemName(var1);
         } else if (this.minecraft.player.isSpectator()) {
            this.spectatorGui.renderTooltip(var1);
         }
      }

      if (this.minecraft.player.getSleepTimer() > 0) {
         this.minecraft.getProfiler().push("sleep");
         RenderSystem.disableDepthTest();
         RenderSystem.disableAlphaTest();
         float var11 = (float) this.minecraft.player.getSleepTimer();
         float var7 = var11 / 100.0F;
         if (var7 > 1.0F) {
            var7 = 1.0F - (var11 - 100.0F) / 10.0F;
         }

         int var8 = (int) (220.0F * var7) << 24 | 1052704;
         fill(var1, 0, 0, this.screenWidth, this.screenHeight, var8);
         RenderSystem.enableAlphaTest();
         RenderSystem.enableDepthTest();
         this.minecraft.getProfiler().pop();
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      }

      if (this.minecraft.isDemo()) {
         this.renderDemoOverlay(var1);
      }

      this.renderEffects(var1);
      if (this.minecraft.options.renderDebug) {
         this.debugScreen.render(var1);
      }

      if (!this.minecraft.options.hideGui) {
         if (this.overlayMessageString != null && this.overlayMessageTime > 0) {
            this.minecraft.getProfiler().push("overlayMessage");
            float var12 = (float) this.overlayMessageTime - var2;
            int var15 = (int) (var12 * 255.0F / 20.0F);
            if (var15 > 255) {
               var15 = 255;
            }

            if (var15 > 8) {
               RenderSystem.pushMatrix();
               RenderSystem.translatef((float) (this.screenWidth / 2), (float) (this.screenHeight - 68), 0.0F);
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               int var19 = 16777215;
               if (this.animateOverlayMessageColor) {
                  var19 = Mth.hsvToRgb(var12 / 50.0F, 0.7F, 0.6F) & 16777215;
               }

               int var9 = var15 << 24 & -16777216;
               int var10 = var3.width(this.overlayMessageString);
               this.drawBackdrop(var1, var3, -4, var10, 16777215 | var9);
               var3.draw(var1, this.overlayMessageString, (float) (-var10 / 2), -4.0F, var19 | var9);
               RenderSystem.disableBlend();
               RenderSystem.popMatrix();
            }

            this.minecraft.getProfiler().pop();
         }

         if (this.title != null && this.titleTime > 0) {
            this.minecraft.getProfiler().push("titleAndSubtitle");
            float var13 = (float) this.titleTime - var2;
            int var16 = 255;
            if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
               float var20 = (float) (this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - var13;
               var16 = (int) (var20 * 255.0F / (float) this.titleFadeInTime);
            }

            if (this.titleTime <= this.titleFadeOutTime) {
               var16 = (int) (var13 * 255.0F / (float) this.titleFadeOutTime);
            }

            var16 = Mth.clamp(var16, 0, 255);
            if (var16 > 8) {
               RenderSystem.pushMatrix();
               RenderSystem.translatef((float) (this.screenWidth / 2), (float) (this.screenHeight / 2), 0.0F);
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.pushMatrix();
               RenderSystem.scalef(4.0F, 4.0F, 4.0F);
               int var21 = var16 << 24 & -16777216;
               int var23 = var3.width(this.title);
               this.drawBackdrop(var1, var3, -10, var23, 16777215 | var21);
               var3.drawShadow(var1, this.title, (float) (-var23 / 2), -10.0F, 16777215 | var21);
               RenderSystem.popMatrix();
               if (this.subtitle != null) {
                  RenderSystem.pushMatrix();
                  RenderSystem.scalef(2.0F, 2.0F, 2.0F);
                  int var27 = var3.width(this.subtitle);
                  this.drawBackdrop(var1, var3, 5, var27, 16777215 | var21);
                  var3.drawShadow(var1, this.subtitle, (float) (-var27 / 2), 5.0F, 16777215 | var21);
                  RenderSystem.popMatrix();
               }

               RenderSystem.disableBlend();
               RenderSystem.popMatrix();
            }

            this.minecraft.getProfiler().pop();
         }

         this.subtitleOverlay.render(var1);
         Scoreboard var14 = this.minecraft.level.getScoreboard();
         Objective var18 = null;
         PlayerTeam var22 = var14.getPlayersTeam(this.minecraft.player.getScoreboardName());
         if (var22 != null) {
            int var24 = var22.getColor().getId();
            if (var24 >= 0) {
               var18 = var14.getDisplayObjective(3 + var24);
            }
         }

         Objective var25 = var18 != null ? var18 : var14.getDisplayObjective(1);
         if (var25 != null) {
            this.displayScoreboardSidebar(var1, var25);
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableAlphaTest();
         RenderSystem.pushMatrix();
         RenderSystem.translatef(0.0F, (float) (this.screenHeight - 48), 0.0F);
         this.minecraft.getProfiler().push("chat");
         this.chat.render(var1, this.tickCount);
         this.minecraft.getProfiler().pop();
         RenderSystem.popMatrix();
         var25 = var14.getDisplayObjective(0);
         if (!this.minecraft.options.keyPlayerList.isDown() || this.minecraft.isLocalServer() && this.minecraft.player.connection.getOnlinePlayers().size() <= 1 && var25 == null) {
            this.tabList.setVisible(false);
         } else {
            this.tabList.setVisible(true);
            this.tabList.render(var1, this.screenWidth, var14, var25);
         }
      }

      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableAlphaTest();
   }


   private void drawBackdrop(PoseStack var1, Font var2, int var3, int var4, int var5) {
      int var6 = this.minecraft.options.getBackgroundColor(0.0F);
      if (var6 != 0) {
         int var7x = -var4 / 2;
         fill(var1, var7x - 2, var3 - 2, var7x + var4 + 2, var3 + 9 + 2, FastColor.ARGB32.multiply(var6, var5));
      }
   }

   private void renderCrosshair(PoseStack var1) {
      Options var2 = this.minecraft.options;
      if (var2.getCameraType().isFirstPerson()) {
         if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
            if (var2.renderDebug && !var2.hideGui && !this.minecraft.player.isReducedDebugInfo() && !var2.reducedDebugInfo) {
               RenderSystem.pushMatrix();
               RenderSystem.translatef((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), (float)this.getBlitOffset());
               Camera var9 = this.minecraft.gameRenderer.getMainCamera();
               RenderSystem.rotatef(var9.getXRot(), -1.0F, 0.0F, 0.0F);
               RenderSystem.rotatef(var9.getYRot(), 0.0F, 1.0F, 0.0F);
               RenderSystem.scalef(-1.0F, -1.0F, -1.0F);
               RenderSystem.renderCrosshair(10);
               RenderSystem.popMatrix();
            } else {
               RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
               int var3 = 15;
               this.blit(var1, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
               if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                  float var4 = this.minecraft.player.getAttackStrengthScale(0.0F);
                  boolean var5 = false;
                  if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && var4 >= 1.0F) {
                     var5 = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
                     var5 &= this.minecraft.crosshairPickEntity.isAlive();
                  }

                  int var6 = this.screenHeight / 2 - 7 + 16;
                  int var7 = this.screenWidth / 2 - 8;
                  if (var5) {
                     this.blit(var1, var7, var6, 68, 94, 16, 16);
                  } else if (var4 < 1.0F) {
                     int var8 = (int)(var4 * 17.0F);
                     this.blit(var1, var7, var6, 36, 94, 16, 4);
                     this.blit(var1, var7, var6, 52, 94, var8, 4);
                  }
               }
            }

         }
      }
   }

   private boolean canRenderCrosshairForSpectator(HitResult var1) {
      if (var1 == null) {
         return false;
      } else if (var1.getType() == HitResult.Type.ENTITY) {
         return ((EntityHitResult)var1).getEntity() instanceof MenuProvider;
      } else if (var1.getType() == HitResult.Type.BLOCK) {
         BlockPos var2 = ((BlockHitResult)var1).getBlockPos();
         Level var3 = this.minecraft.level;
         return var3.getBlockState(var2).getMenuProvider(var3, var2) != null;
      } else {
         return false;
      }
   }

   protected void renderEffects(PoseStack var1) {
      Collection<MobEffectInstance> var2 = this.minecraft.player.getActiveEffects();
      if (!var2.isEmpty()) {
         RenderSystem.enableBlend();
         int var3 = 0;
         int var4 = 0;
         MobEffectTextureManager var5 = this.minecraft.getMobEffectTextures();
         List<Runnable> var6 = Lists.newArrayListWithExpectedSize(var2.size());
         this.minecraft.getTextureManager().bind(AbstractContainerScreen.INVENTORY_LOCATION);

         for(MobEffectInstance var8 : Ordering.natural().reverse().sortedCopy(var2)) {
            MobEffect var9 = var8.getEffect();
            if (var8.showIcon()) {
               int var14_1 = this.screenWidth;
               int var15_1 = 1;
               if (this.minecraft.isDemo()) {
                  var15_1 += 15;
               }

               if (var9.isBeneficial()) {
                  ++var3;
                  var14_1 -= 25 * var3;
               } else {
                  ++var4;
                  var14_1 -= 25 * var4;
                  var15_1 += 26;
               }

               RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               float var16_1;
               if (var8.isAmbient()) {
                   var16_1 = 1.0F;
                   this.blit(var1, var14_1, var15_1, 165, 166, 24, 24);
               } else {
                  this.blit(var1, var14_1, var15_1, 141, 166, 24, 24);
                  if (var8.getDuration() <= 200) {
                     int var13 = 10 - var8.getDuration() / 20;
                     var16_1 = Mth.clamp((float)var8.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + Mth.cos((float)var8.getDuration() * (float)Math.PI / 5.0F) * Mth.clamp((float)var13 / 10.0F * 0.25F, 0.0F, 0.25F);
                  } else {
                      var16_1 = 1.0F;
                  }
               }

               TextureAtlasSprite var18 = var5.get(var9);
               int finalVar14_ = var14_1;
               int finalVar15_ = var15_1;
               var6.add((Runnable)() -> {
                  this.minecraft.getTextureManager().bind(var18.atlas().location());
                  RenderSystem.color4f(1.0F, 1.0F, 1.0F, var16_1);
                  blit(var1, finalVar14_ + 3, finalVar15_ + 3, this.getBlitOffset(), 18, 18, var18);
               });
            }
         }

         var6.forEach(Runnable::run);
      }
   }

   protected void renderHotbar(float var1, PoseStack var2) {
      Player var3 = this.getCameraPlayer();
      if (var3 != null) {
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.minecraft.getTextureManager().bind(WIDGETS_LOCATION);
         ItemStack var4 = var3.getOffhandItem();
         HumanoidArm var5 = var3.getMainArm().getOpposite();
         int var6 = this.screenWidth / 2;
         int var7 = this.getBlitOffset();
         int var8 = 182;
         int var9 = 91;
         this.setBlitOffset(-90);
         this.blit(var2, var6 - 91, this.screenHeight - 22, 0, 0, 182, 22);
         this.blit(var2, var6 - 91 - 1 + var3.inventory.selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
         if (!var4.isEmpty()) {
            if (var5 == HumanoidArm.LEFT) {
               this.blit(var2, var6 - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
            } else {
               this.blit(var2, var6 + 91, this.screenHeight - 23, 53, 22, 29, 24);
            }
         }

         this.setBlitOffset(var7);
         RenderSystem.enableRescaleNormal();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();

         for(int var10 = 0; var10 < 9; ++var10) {
            int var11 = var6 - 90 + var10 * 20 + 2;
            int var12 = this.screenHeight - 16 - 3;
            this.renderSlot(var11, var12, var1, var3, (ItemStack)var3.inventory.items.get(var10));
         }

         if (!var4.isEmpty()) {
            int var14 = this.screenHeight - 16 - 3;
            if (var5 == HumanoidArm.LEFT) {
               this.renderSlot(var6 - 91 - 26, var14, var1, var3, var4);
            } else {
               this.renderSlot(var6 + 91 + 10, var14, var1, var3, var4);
            }
         }

         if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.HOTBAR) {
            float var15 = this.minecraft.player.getAttackStrengthScale(0.0F);
            if (var15 < 1.0F) {
               int var16 = this.screenHeight - 20;
               int var17 = var6 + 91 + 6;
               if (var5 == HumanoidArm.RIGHT) {
                  var17 = var6 - 91 - 22;
               }

               this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
               int var13 = (int)(var15 * 19.0F);
               RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               this.blit(var2, var17, var16, 0, 94, 18, 18);
               this.blit(var2, var17, var16 + 18 - var13, 18, 112 - var13, 18, var13);
            }
         }

         RenderSystem.disableRescaleNormal();
         RenderSystem.disableBlend();
      }
   }

   public void renderJumpMeter(PoseStack var1, int var2) {
      this.minecraft.getProfiler().push("jumpBar");
      this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
      float var3 = this.minecraft.player.getJumpRidingScale();
      int var4 = 182;
      int var5 = (int)(var3 * 183.0F);
      int var6 = this.screenHeight - 32 + 3;
      this.blit(var1, var2, var6, 0, 84, 182, 5);
      if (var5 > 0) {
         this.blit(var1, var2, var6, 0, 89, var5, 5);
      }

      this.minecraft.getProfiler().pop();
   }

   public void renderExperienceBar(PoseStack var1, int var2) {
      this.minecraft.getProfiler().push("expBar");
      this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
      int var3 = this.minecraft.player.getXpNeededForNextLevel();
      if (var3 > 0) {
         int var4 = 182;
         int var5 = (int)(this.minecraft.player.experienceProgress * 183.0F);
         int var6 = this.screenHeight - 32 + 3;
         this.blit(var1, var2, var6, 0, 64, 182, 5);
         if (var5 > 0) {
            this.blit(var1, var2, var6, 0, 69, var5, 5);
         }
      }

      this.minecraft.getProfiler().pop();
      if (this.minecraft.player.experienceLevel > 0) {
         this.minecraft.getProfiler().push("expLevel");
         String var7 = "" + this.minecraft.player.experienceLevel;
         int var8 = (this.screenWidth - this.getFont().width(var7)) / 2;
         int var9 = this.screenHeight - 31 - 4;
         this.getFont().draw(var1, var7, (float)(var8 + 1), (float)var9, 0);
         this.getFont().draw(var1, var7, (float)(var8 - 1), (float)var9, 0);
         this.getFont().draw(var1, var7, (float)var8, (float)(var9 + 1), 0);
         this.getFont().draw(var1, var7, (float)var8, (float)(var9 - 1), 0);
         this.getFont().draw(var1, var7, (float)var8, (float)var9, 8453920);
         this.minecraft.getProfiler().pop();
      }
   }

   public void renderSelectedItemName(PoseStack var1) {
      this.minecraft.getProfiler().push("selectedItemName");
      if (this.toolHighlightTimer > 0 && !this.lastToolHighlight.isEmpty()) {
         MutableComponent var2 = (new TextComponent("")).append(this.lastToolHighlight.getHoverName()).withStyle(this.lastToolHighlight.getRarity().color);
         if (this.lastToolHighlight.hasCustomHoverName()) {
            var2.withStyle(ChatFormatting.ITALIC);
         }

         int var3 = this.getFont().width(var2);
         int var4 = (this.screenWidth - var3) / 2;
         int var5 = this.screenHeight - 59;
         if (!this.minecraft.gameMode.canHurtPlayer()) {
            var5 += 14;
         }

         int var6 = (int)((float)this.toolHighlightTimer * 256.0F / 10.0F);
         if (var6 > 255) {
            var6 = 255;
         }

         if (var6 > 0) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            this.getFont().getClass();
            fill(var1, var4 - 2, var5 - 2, var4 + var3 + 2, var5 + 9 + 2, this.minecraft.options.getBackgroundColor(0));
            this.getFont().drawShadow(var1, var2, (float)var4, (float)var5, 16777215 + (var6 << 24));
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
         }
      }

      this.minecraft.getProfiler().pop();
   }

   public void renderDemoOverlay(PoseStack var1) {
      this.minecraft.getProfiler().push("demo");
      Component var2;
      if (this.minecraft.level.getGameTime() >= 120500L) {
         var2 = DEMO_EXPIRED_TEXT;
      } else {
         var2 = new TranslatableComponent("demo.remainingTime", new Object[]{StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime()))});
      }

      int var3 = this.getFont().width(var2);
      this.getFont().drawShadow(var1, var2, (float)(this.screenWidth - var3 - 10), 5.0F, 16777215);
      this.minecraft.getProfiler().pop();
   }

   private void displayScoreboardSidebar(PoseStack var1, Objective var2) {
      Scoreboard var3 = var2.getScoreboard();
      Collection<Score> var4 = var3.getPlayerScores(var2);
      List<Score> var5 = var4.stream().filter((var0) -> var0.getOwner() != null && !var0.getOwner().startsWith("#")).collect(Collectors.toList());
      if (var5.size() > 15) {
         var4 = Lists.newArrayList(Iterables.skip(var5, var4.size() - 15));
      } else {
         var4 = var5;
      }

      List<Pair<Score, Component>> var6 = Lists.newArrayListWithCapacity(var4.size());
      Component var7 = var2.getDisplayName();
      int var8 = this.getFont().width(var7);
      int var9 = var8;
      int var10 = this.getFont().width(": ");

      for(Score var12 : var4) {
         PlayerTeam var13 = var3.getPlayersTeam(var12.getOwner());
         Component var23_1 = PlayerTeam.formatNameForTeam(var13, new TextComponent(var12.getOwner()));
         var6.add(Pair.of(var12, var23_1));
         var9 = Math.max(var9, this.getFont().width(var23_1) + var10 + this.getFont().width(Integer.toString(var12.getScore())));
      }


      int var27 = var4.size() * 9;
      int var28 = this.screenHeight / 2 + var27 / 3;
      int var29 = 3;
      int var23_1 = this.screenWidth - var9 - 3;
      int var15 = 0;
      int var16 = this.minecraft.options.getBackgroundColor(0.3F);
      int var17 = this.minecraft.options.getBackgroundColor(0.4F);

      for(Pair<Score, Component> var19 : var6) {
         ++var15;
         Score var20 = var19.getFirst();
         Component var21 = var19.getSecond();
         String var22 = ChatFormatting.RED + "" + var20.getScore();
         this.getFont().getClass();
         int var24 = var28 - var15 * 9;
         int var25 = this.screenWidth - 3 + 2;
         this.getFont().getClass();
         fill(var1, var23_1 - 2, var24, var25, var24 + 9, var16);
         this.getFont().draw(var1, var21, (float)var23_1, (float)var24, -1);
         this.getFont().draw(var1, var22, (float)(var25 - this.getFont().width(var22)), (float)var24, -1);
         if (var15 == var4.size()) {
            this.getFont().getClass();
            fill(var1, var23_1 - 2, var24 - 9 - 1, var25, var24 - 1, var17);
            fill(var1, var23_1 - 2, var24 - 1, var25, var24, var16);
            this.getFont().draw(var1, var7, (float)(var23_1 + var9 / 2 - var8 / 2), (float)(var24 - 9), -1);
         }
      }
   }

   private Player getCameraPlayer() {
      return !(this.minecraft.getCameraEntity() instanceof Player) ? null : (Player)this.minecraft.getCameraEntity();
   }

   private LivingEntity getPlayerVehicleWithHealth() {
      Player var1 = this.getCameraPlayer();
      if (var1 != null) {
         Entity var2 = var1.getVehicle();
         if (var2 == null) {
            return null;
         }

         if (var2 instanceof LivingEntity) {
            return (LivingEntity)var2;
         }
      }

      return null;
   }

   private int getVehicleMaxHearts(LivingEntity var1) {
      if (var1 != null && var1.showVehicleHealth()) {
         float var2 = var1.getMaxHealth();
         int var3 = (int)(var2 + 0.5F) / 2;
         if (var3 > 30) {
            var3 = 30;
         }

         return var3;
      } else {
         return 0;
      }
   }

   private int getVisibleVehicleHeartRows(int var1) {
      return (int)Math.ceil((double)var1 / 10.0);
   }

   private void renderPlayerHealth(PoseStack var1) {
      Player var2 = this.getCameraPlayer();
      if (var2 != null) {
         int var3 = Mth.ceil(var2.getHealth());
         boolean var4 = this.healthBlinkTime > (long)this.tickCount && (this.healthBlinkTime - (long)this.tickCount) / 3L % 2L == 1L;
         long var5 = Util.getMillis();
         if (var3 < this.lastHealth && var2.invulnerableTime > 0) {
            this.lastHealthTime = var5;
            this.healthBlinkTime = (long)(this.tickCount + 20);
         } else if (var3 > this.lastHealth && var2.invulnerableTime > 0) {
            this.lastHealthTime = var5;
            this.healthBlinkTime = (long)(this.tickCount + 10);
         }

         if (var5 - this.lastHealthTime > 1000L) {
            this.lastHealth = var3;
            this.displayHealth = var3;
            this.lastHealthTime = var5;
         }

         this.lastHealth = var3;
         int var7 = this.displayHealth;
         this.random.setSeed((long)(this.tickCount * 312871));
         FoodData var8 = var2.getFoodData();
         int var9 = var8.getFoodLevel();
         int var10 = this.screenWidth / 2 - 91;
         int var11 = this.screenWidth / 2 + 91;
         int var12 = this.screenHeight - 39;
         float var13 = (float)var2.getAttributeValue(Attributes.MAX_HEALTH);
         int var14 = Mth.ceil(var2.getAbsorptionAmount());
         int var15 = Mth.ceil((var13 + (float)var14) / 2.0F / 10.0F);
         int var16 = Math.max(10 - (var15 - 2), 3);
         int var17 = var12 - (var15 - 1) * var16 - 10;
         int var18 = var12 - 10;
         int var19 = var14;
         int var20 = var2.getArmorValue();
         int var21 = -1;
         if (var2.hasEffect(MobEffects.REGENERATION)) {
            var21 = this.tickCount % Mth.ceil(var13 + 5.0F);
         }

         this.minecraft.getProfiler().push("armor");

         for(int var22 = 0; var22 < 10; ++var22) {
            if (var20 > 0) {
               int var23 = var10 + var22 * 8;
               if (var22 * 2 + 1 < var20) {
                  this.blit(var1, var23, var17, 34, 9, 9, 9);
               }

               if (var22 * 2 + 1 == var20) {
                  this.blit(var1, var23, var17, 25, 9, 9, 9);
               }

               if (var22 * 2 + 1 > var20) {
                  this.blit(var1, var23, var17, 16, 9, 9, 9);
               }
            }
         }

         this.minecraft.getProfiler().popPush("health");

         for(int var31 = Mth.ceil((var13 + (float)var14) / 2.0F) - 1; var31 >= 0; --var31) {
            int var33 = 16;
            if (var2.hasEffect(MobEffects.POISON)) {
               var33 += 36;
            } else if (var2.hasEffect(MobEffects.WITHER)) {
               var33 += 72;
            }

            int var24 = 0;
            if (var4) {
               var24 = 1;
            }

            int var25 = Mth.ceil((float)(var31 + 1) / 10.0F) - 1;
            int var26 = var10 + var31 % 10 * 8;
            int var27 = var12 - var25 * var16;
            if (var3 <= 4) {
               var27 += this.random.nextInt(2);
            }

            if (var19 <= 0 && var31 == var21) {
               var27 -= 2;
            }

            int var28 = 0;
            if (var2.level.getLevelData().isHardcore()) {
               var28 = 5;
            }

            this.blit(var1, var26, var27, 16 + var24 * 9, 9 * var28, 9, 9);
            if (var4) {
               if (var31 * 2 + 1 < var7) {
                  this.blit(var1, var26, var27, var33 + 54, 9 * var28, 9, 9);
               }

               if (var31 * 2 + 1 == var7) {
                  this.blit(var1, var26, var27, var33 + 63, 9 * var28, 9, 9);
               }
            }

            if (var19 > 0) {
               if (var19 == var14 && var14 % 2 == 1) {
                  this.blit(var1, var26, var27, var33 + 153, 9 * var28, 9, 9);
                  --var19;
               } else {
                  this.blit(var1, var26, var27, var33 + 144, 9 * var28, 9, 9);
                  var19 -= 2;
               }
            } else {
               if (var31 * 2 + 1 < var3) {
                  this.blit(var1, var26, var27, var33 + 36, 9 * var28, 9, 9);
               }

               if (var31 * 2 + 1 == var3) {
                  this.blit(var1, var26, var27, var33 + 45, 9 * var28, 9, 9);
               }
            }
         }

         LivingEntity var32 = this.getPlayerVehicleWithHealth();
         int var34 = this.getVehicleMaxHearts(var32);
         if (var34 == 0) {
            this.minecraft.getProfiler().popPush("food");

            for(int var35 = 0; var35 < 10; ++var35) {
               int var37 = var12;
               int var39 = 16;
               int var41 = 0;
               if (var2.hasEffect(MobEffects.HUNGER)) {
                  var39 += 36;
                  var41 = 13;
               }

               if (var2.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (var9 * 3 + 1) == 0) {
                  var37 = var12 + (this.random.nextInt(3) - 1);
               }

               int var43 = var11 - var35 * 8 - 9;
               this.blit(var1, var43, var37, 16 + var41 * 9, 27, 9, 9);
               if (var35 * 2 + 1 < var9) {
                  this.blit(var1, var43, var37, var39 + 36, 27, 9, 9);
               }

               if (var35 * 2 + 1 == var9) {
                  this.blit(var1, var43, var37, var39 + 45, 27, 9, 9);
               }
            }

            var18 -= 10;
         }

         this.minecraft.getProfiler().popPush("air");
         int var36 = var2.getMaxAirSupply();
         int var38 = Math.min(var2.getAirSupply(), var36);
         if (var2.isEyeInFluid(FluidTags.WATER) || var38 < var36) {
            int var40 = this.getVisibleVehicleHeartRows(var34) - 1;
            var18 -= var40 * 10;
            int var42 = Mth.ceil((double)(var38 - 2) * (double)10.0F / (double)var36);
            int var44 = Mth.ceil((double)var38 * (double)10.0F / (double)var36) - var42;

            for(int var29 = 0; var29 < var42 + var44; ++var29) {
               if (var29 < var42) {
                  this.blit(var1, var11 - var29 * 8 - 9, var18, 16, 18, 9, 9);
               } else {
                  this.blit(var1, var11 - var29 * 8 - 9, var18, 25, 18, 9, 9);
               }
            }
         }

         this.minecraft.getProfiler().pop();
      }
   }

   private void renderVehicleHealth(PoseStack var1) {
      LivingEntity var2 = this.getPlayerVehicleWithHealth();
      if (var2 != null) {
         int var3 = this.getVehicleMaxHearts(var2);
         if (var3 != 0) {
            int var4 = (int)Math.ceil((double)var2.getHealth());
            this.minecraft.getProfiler().popPush("mountHealth");
            int var5 = this.screenHeight - 39;
            int var6 = this.screenWidth / 2 + 91;
            int var7 = var5;
            int var8 = 0;

            for(boolean var9 = false; var3 > 0; var8 += 20) {
               int var10 = Math.min(var3, 10);
               var3 -= var10;

               for(int var11 = 0; var11 < var10; ++var11) {
                  int var12 = 52;
                  int var13 = 0;
                  int var14 = var6 - var11 * 8 - 9;
                  this.blit(var1, var14, var7, 52 + var13 * 9, 9, 9, 9);
                  if (var11 * 2 + 1 + var8 < var4) {
                     this.blit(var1, var14, var7, 88, 9, 9, 9);
                  }

                  if (var11 * 2 + 1 + var8 == var4) {
                     this.blit(var1, var14, var7, 97, 9, 9, 9);
                  }
               }

               var7 -= 10;
            }

         }
      }
   }

   private void renderPumpkin() {
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.defaultBlendFunc();
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableAlphaTest();
      this.minecraft.getTextureManager().bind(PUMPKIN_BLUR_LOCATION);
      Tesselator var1 = Tesselator.getInstance();
      BufferBuilder var2 = var1.getBuilder();
      var2.begin(7, DefaultVertexFormat.POSITION_TEX);
      var2.vertex((double)0.0F, (double)this.screenHeight, (double)-90.0F).uv(0.0F, 1.0F).endVertex();
      var2.vertex((double)this.screenWidth, (double)this.screenHeight, (double)-90.0F).uv(1.0F, 1.0F).endVertex();
      var2.vertex((double)this.screenWidth, (double)0.0F, (double)-90.0F).uv(1.0F, 0.0F).endVertex();
      var2.vertex((double)0.0F, (double)0.0F, (double)-90.0F).uv(0.0F, 0.0F).endVertex();
      var1.end();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.enableAlphaTest();
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void updateVignetteBrightness(Entity var1) {
      if (var1 != null) {
         float var2 = Mth.clamp(1.0F - var1.getBrightness(), 0.0F, 1.0F);
         this.vignetteBrightness = (float)((double)this.vignetteBrightness + (double)(var2 - this.vignetteBrightness) * 0.01);
      }
   }

   private void renderVignette(Entity var1) {
      WorldBorder var2 = this.minecraft.level.getWorldBorder();
      float var3 = (float)var2.getDistanceToBorder(var1);
      double var4 = Math.min(var2.getLerpSpeed() * (double)var2.getWarningTime() * (double)1000.0F, Math.abs(var2.getLerpTarget() - var2.getSize()));
      double var6 = Math.max((double)var2.getWarningBlocks(), var4);
      if ((double)var3 < var6) {
         var3 = 1.0F - (float)((double)var3 / var6);
      } else {
         var3 = 0.0F;
      }

      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      if (var3 > 0.0F) {
         RenderSystem.color4f(0.0F, var3, var3, 1.0F);
      } else {
         RenderSystem.color4f(this.vignetteBrightness, this.vignetteBrightness, this.vignetteBrightness, 1.0F);
      }

      this.minecraft.getTextureManager().bind(VIGNETTE_LOCATION);
      Tesselator var8 = Tesselator.getInstance();
      BufferBuilder var9 = var8.getBuilder();
      var9.begin(7, DefaultVertexFormat.POSITION_TEX);
      var9.vertex(0.0F, this.screenHeight, -90.0F).uv(0.0F, 1.0F).endVertex();
      var9.vertex(this.screenWidth, this.screenHeight, -90.0F).uv(1.0F, 1.0F).endVertex();
      var9.vertex(this.screenWidth, 0.0F, -90.0F).uv(1.0F, 0.0F).endVertex();
      var9.vertex(0.0F, 0.0F, -90.0F).uv(0.0F, 0.0F).endVertex();
      var8.end();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
   }

   private void renderPortalOverlay(float var1) {
      if (var1 < 1.0F) {
         var1 *= var1;
         var1 *= var1;
         var1 = var1 * 0.8F + 0.2F;
      }

      RenderSystem.disableAlphaTest();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.defaultBlendFunc();
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, var1);
      this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
      TextureAtlasSprite var2 = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
      float var3 = var2.getU0();
      float var4 = var2.getV0();
      float var5 = var2.getU1();
      float var6 = var2.getV1();
      Tesselator var7 = Tesselator.getInstance();
      BufferBuilder var8 = var7.getBuilder();
      var8.begin(7, DefaultVertexFormat.POSITION_TEX);
      var8.vertex(0.0F, this.screenHeight, -90.0F).uv(var3, var6).endVertex();
      var8.vertex(this.screenWidth, this.screenHeight, -90.0F).uv(var5, var6).endVertex();
      var8.vertex(this.screenWidth, 0.0F, -90.0F).uv(var5, var4).endVertex();
      var8.vertex(0.0F, 0.0F, -90.0F).uv(var3, var4).endVertex();
      var7.end();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.enableAlphaTest();
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderSlot(int var1, int var2, float var3, Player var4, ItemStack var5) {
      if (!var5.isEmpty()) {
         float var6 = (float)var5.getPopTime() - var3;
         if (var6 > 0.0F) {
            RenderSystem.pushMatrix();
            float var7 = 1.0F + var6 / 5.0F;
            RenderSystem.translatef((float)(var1 + 8), (float)(var2 + 12), 0.0F);
            RenderSystem.scalef(1.0F / var7, (var7 + 1.0F) / 2.0F, 1.0F);
            RenderSystem.translatef((float)(-(var1 + 8)), (float)(-(var2 + 12)), 0.0F);
         }

         this.itemRenderer.renderAndDecorateItem(var4, var5, var1, var2);
         if (var6 > 0.0F) {
            RenderSystem.popMatrix();
         }

         this.itemRenderer.renderGuiItemDecorations(this.minecraft.font, var5, var1, var2);
      }
   }

   public void tick() {
      if (this.overlayMessageTime > 0) {
         --this.overlayMessageTime;
      }

      if (this.titleTime > 0) {
         --this.titleTime;
         if (this.titleTime <= 0) {
            this.title = null;
            this.subtitle = null;
         }
      }

      ++this.tickCount;
      Entity var1 = this.minecraft.getCameraEntity();
      if (var1 != null) {
         this.updateVignetteBrightness(var1);
      }

      if (this.minecraft.player != null) {
         ItemStack var2 = this.minecraft.player.inventory.getSelected();
         if (var2.isEmpty()) {
            this.toolHighlightTimer = 0;
         } else if (!this.lastToolHighlight.isEmpty() && var2.getItem() == this.lastToolHighlight.getItem() && var2.getHoverName().equals(this.lastToolHighlight.getHoverName())) {
            if (this.toolHighlightTimer > 0) {
               --this.toolHighlightTimer;
            }
         } else {
            this.toolHighlightTimer = 40;
         }

         this.lastToolHighlight = var2;
      }
   }

   public void setNowPlaying(Component var1) {
      this.setOverlayMessage(new TranslatableComponent("record.nowPlaying", var1), true);
   }

   public void setOverlayMessage(Component var1, boolean var2) {
      this.overlayMessageString = var1;
      this.overlayMessageTime = 60;
      this.animateOverlayMessageColor = var2;
   }

   public void setTitles(@Nullable Component var1, @Nullable Component var2, int var3, int var4, int var5) {
      if (var1 == null && var2 == null && var3 < 0 && var4 < 0 && var5 < 0) {
         this.title = null;
         this.subtitle = null;
         this.titleTime = 0;
      } else if (var1 != null) {
         this.title = var1;
         this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
      } else if (var2 != null) {
         this.subtitle = var2;
      } else {
         if (var3 >= 0) {
            this.titleFadeInTime = var3;
         }

         if (var4 >= 0) {
            this.titleStayTime = var4;
         }

         if (var5 >= 0) {
            this.titleFadeOutTime = var5;
         }

         if (this.titleTime > 0) {
            this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
         }
      }
   }

   public UUID guessChatUUID(Component var1) {
      String var2 = StringDecomposer.getPlainText(var1);
      String var3 = StringUtils.substringBetween(var2, "<", ">");
      return var3 == null ? Util.NIL_UUID : this.minecraft.getPlayerSocialManager().getDiscoveredUUID(var3);
   }

   public void handleChat(ChatType var1, Component var2, UUID var3) {
      if (!this.minecraft.isBlocked(var3)) {
         if (!this.minecraft.options.hideMatchedNames || !this.minecraft.isBlocked(this.guessChatUUID(var2))) {
            for(ChatListener var5 : this.chatListeners.get(var1)) {
               var5.handle(var1, var2, var3);
            }

         }
      }
   }

   public ChatComponent getChat() {
      return this.chat;
   }

   public int getGuiTicks() {
      return this.tickCount;
   }

   public Font getFont() {
      return this.minecraft.font;
   }

   public SpectatorGui getSpectatorGui() {
      return this.spectatorGui;
   }

   public PlayerTabOverlay getTabList() {
      return this.tabList;
   }

   public void onDisconnected() {
      this.tabList.reset();
      this.bossOverlay.reset();
      this.minecraft.getToasts().clear();
   }

   public BossHealthOverlay getBossOverlay() {
      return this.bossOverlay;
   }

   public void clearCache() {
      this.debugScreen.clearChunkCache();
   }
}
