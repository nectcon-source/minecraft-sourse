package net.minecraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;

public abstract class Option {
   public static final ProgressOption BIOME_BLEND_RADIUS = new ProgressOption(
      "options.biomeBlendRadius", 0.0, 7.0, 1.0F, var0 -> (double)var0.biomeBlendRadius, (var0, var1) -> {
         var0.biomeBlendRadius = Mth.clamp((int)var1.doubleValue(), 0, 7);
         Minecraft.getInstance().levelRenderer.allChanged();
      }, (var0, var1) -> {
      double var2 = var1.get(var0);
      int var4 = (int)var2 * 2 + 1;
      return var1.genericValueLabel(new TranslatableComponent("options.biomeBlendRadius." + var4));
   });
   public static final ProgressOption CHAT_HEIGHT_FOCUSED = new ProgressOption(
      "options.chat.height.focused", 0.0, 1.0, 0.0F, var0 -> var0.chatHeightFocused, (var0, var1) -> {
         var0.chatHeightFocused = var1;
         Minecraft.getInstance().gui.getChat().rescaleChat();
      }, (var0, var1) -> {
         double var2 = var1.toPct(var1.get(var0));
         return var1.pixelValueLabel(ChatComponent.getHeight(var2));
      }
   );
   public static final ProgressOption CHAT_HEIGHT_UNFOCUSED = new ProgressOption(
      "options.chat.height.unfocused", 0.0, 1.0, 0.0F, var0 -> var0.chatHeightUnfocused, (var0, var1) -> {
         var0.chatHeightUnfocused = var1;
         Minecraft.getInstance().gui.getChat().rescaleChat();
      }, (var0, var1) -> {
         double var2 = var1.toPct(var1.get(var0));
         return var1.pixelValueLabel(ChatComponent.getHeight(var2));
      }
   );
   public static final ProgressOption CHAT_OPACITY = new ProgressOption("options.chat.opacity", 0.0, 1.0, 0.0F, var0 -> var0.chatOpacity, (var0, var1) -> {
      var0.chatOpacity = var1;
      Minecraft.getInstance().gui.getChat().rescaleChat();
   }, (var0, var1) -> {
      double var2 = var1.toPct(var1.get(var0));
      return var1.percentValueLabel(var2 * 0.9 + 0.1);
   });
   public static final ProgressOption CHAT_SCALE = new ProgressOption("options.chat.scale", 0.0, 1.0, 0.0F, var0 -> var0.chatScale, (var0, var1) -> {
      var0.chatScale = var1;
      Minecraft.getInstance().gui.getChat().rescaleChat();
   }, (var0, var1) -> {
      double var2 = var1.toPct(var1.get(var0));
      return (Component)(var2 == 0.0 ? CommonComponents.optionStatus(var1.getCaption(), false) : var1.percentValueLabel(var2));
   });
   public static final ProgressOption CHAT_WIDTH = new ProgressOption("options.chat.width", 0.0, 1.0, 0.0F, var0 -> var0.chatWidth, (var0, var1) -> {
      var0.chatWidth = var1;
      Minecraft.getInstance().gui.getChat().rescaleChat();
   }, (var0, var1) -> {
      double var2 = var1.toPct(var1.get(var0));
      return var1.pixelValueLabel(ChatComponent.getWidth(var2));
   });
   public static final ProgressOption CHAT_LINE_SPACING = new ProgressOption(
      "options.chat.line_spacing",
      0.0,
      1.0,
      0.0F,
      var0 -> var0.chatLineSpacing,
      (var0, var1) -> var0.chatLineSpacing = var1,
      (var0, var1) -> var1.percentValueLabel(var1.toPct(var1.get(var0)))
   );
   public static final ProgressOption CHAT_DELAY = new ProgressOption(
      "options.chat.delay_instant", 0.0, 6.0, 0.1F, var0 -> var0.chatDelay, (var0, var1) -> var0.chatDelay = var1, (var0, var1) -> {
         double var2 = var1.get(var0);
         return var2 <= 0.0 ? new TranslatableComponent("options.chat.delay_none") : new TranslatableComponent("options.chat.delay", String.format("%.1f", var2));
      }
   );
   public static final ProgressOption FOV = new ProgressOption(
      "options.fov", 30.0, 110.0, 1.0F, var0 -> var0.fov, (var0, var1) -> var0.fov = var1, (var0, var1) -> {
         double var2 = var1.get(var0);
         if (var2 == 70.0) {
            return var1.genericValueLabel(new TranslatableComponent("options.fov.min"));
         } else {
            return var2 == var1.getMaxValue() ? var1.genericValueLabel(new TranslatableComponent("options.fov.max")) : var1.genericValueLabel((int)var2);
         }
      }
   );
   private static final Component ACCESSIBILITY_TOOLTIP_FOV_EFFECT = new TranslatableComponent("options.fovEffectScale.tooltip");
   public static final ProgressOption FOV_EFFECTS_SCALE = new ProgressOption(
      "options.fovEffectScale",
      0.0,
      1.0,
      0.0F,
      var0 -> Math.pow((double)var0.fovEffectScale, 2.0),
      (var0, var1) -> var0.fovEffectScale = Mth.sqrt(var1),
      (var0, var1) -> {
         var1.setTooltip(Minecraft.getInstance().font.split(ACCESSIBILITY_TOOLTIP_FOV_EFFECT, 200));
         double var2 = var1.toPct(var1.get(var0));
         return var2 == 0.0 ? var1.genericValueLabel(new TranslatableComponent("options.fovEffectScale.off")) : var1.percentValueLabel(var2);
      }
   );
   private static final Component ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT = new TranslatableComponent("options.screenEffectScale.tooltip");
   public static final ProgressOption SCREEN_EFFECTS_SCALE = new ProgressOption(
      "options.screenEffectScale",
      0.0,
      1.0,
      0.0F,
      var0 -> (double)var0.screenEffectScale,
      (var0, var1) -> var0.screenEffectScale = var1.floatValue(),
      (var0, var1) -> {
         var1.setTooltip(Minecraft.getInstance().font.split(ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT, 200));
         double var2 = var1.toPct(var1.get(var0));
         return var2 == 0.0 ? var1.genericValueLabel(new TranslatableComponent("options.screenEffectScale.off")) : var1.percentValueLabel(var2);
      }
   );
   public static final ProgressOption FRAMERATE_LIMIT = new ProgressOption(
      "options.framerateLimit",
      10.0,
      260.0,
      10.0F,
      var0 -> (double)var0.framerateLimit,
      (var0, var1) -> {
         var0.framerateLimit = (int)var1.doubleValue();
         Minecraft.getInstance().getWindow().setFramerateLimit(var0.framerateLimit);
      },
      (var0, var1) -> {
         double var2 = var1.get(var0);
         return var2 == var1.getMaxValue()
            ? var1.genericValueLabel(new TranslatableComponent("options.framerateLimit.max"))
            : var1.genericValueLabel(new TranslatableComponent("options.framerate", (int)var2));
      }
   );
   public static final ProgressOption GAMMA = new ProgressOption(
      "options.gamma", 0.0, 1.0, 0.0F, var0 -> var0.gamma, (var0, var1) -> var0.gamma = var1, (var0, var1) -> {
         double var2 = var1.toPct(var1.get(var0));
         if (var2 == 0.0) {
            return var1.genericValueLabel(new TranslatableComponent("options.gamma.min"));
         } else {
            return var2 == 1.0 ? var1.genericValueLabel(new TranslatableComponent("options.gamma.max")) : var1.percentAddValueLabel((int)(var2 * 100.0));
         }
      }
   );
   public static final ProgressOption MIPMAP_LEVELS = new ProgressOption(
      "options.mipmapLevels", 0.0, 4.0, 1.0F, var0 -> (double)var0.mipmapLevels, (var0, var1) -> var0.mipmapLevels = (int)var1.doubleValue(), (var0, var1) -> {
         double var2 = var1.get(var0);
         return (Component)(var2 == 0.0 ? CommonComponents.optionStatus(var1.getCaption(), false) : var1.genericValueLabel((int)var2));
      }
   );
   public static final ProgressOption MOUSE_WHEEL_SENSITIVITY = new LogaritmicProgressOption(
      "options.mouseWheelSensitivity",
      0.01,
      10.0,
      0.01F,
      var0 -> var0.mouseWheelSensitivity,
      (var0, var1) -> var0.mouseWheelSensitivity = var1,
      (var0, var1) -> {
         double var2 = var1.toPct(var1.get(var0));
         return var1.genericValueLabel(new TextComponent(String.format("%.2f", var1.toValue(var2))));
      }
   );
   public static final BooleanOption RAW_MOUSE_INPUT = new BooleanOption("options.rawMouseInput", var0 -> var0.rawMouseInput, (var0, var1) -> {
      var0.rawMouseInput = var1;
      Window var2 = Minecraft.getInstance().getWindow();
      if (var2 != null) {
         var2.updateRawMouseInput(var1);
      }
   });
   public static final ProgressOption RENDER_DISTANCE = new ProgressOption(
      "options.renderDistance", 2.0, 16.0, 1.0F, var0 -> (double)var0.renderDistance, (var0, var1) -> {
         var0.renderDistance = (int)var1.doubleValue();
         Minecraft.getInstance().levelRenderer.needsUpdate();
      }, (var0, var1) -> {
         double var2 = var1.get(var0);
         return var1.genericValueLabel(new TranslatableComponent("options.chunks", (int)var2));
      }
   );
   public static final ProgressOption ENTITY_DISTANCE_SCALING = new ProgressOption(
      "options.entityDistanceScaling",
      0.5,
      5.0,
      0.25F,
      var0 -> (double)var0.entityDistanceScaling,
      (var0, var1) -> var0.entityDistanceScaling = (float)var1.doubleValue(),
      (var0, var1) -> {
         double var2 = var1.get(var0);
         return var1.percentValueLabel(var2);
      }
   );
   public static final ProgressOption SENSITIVITY = new ProgressOption(
      "options.sensitivity", 0.0, 1.0, 0.0F, var0 -> var0.sensitivity, (var0, var1) -> var0.sensitivity = var1, (var0, var1) -> {
         double var2 = var1.toPct(var1.get(var0));
         if (var2 == 0.0) {
            return var1.genericValueLabel(new TranslatableComponent("options.sensitivity.min"));
         } else {
            return var2 == 1.0 ? var1.genericValueLabel(new TranslatableComponent("options.sensitivity.max")) : var1.percentValueLabel(2.0 * var2);
         }
      }
   );
   public static final ProgressOption TEXT_BACKGROUND_OPACITY = new ProgressOption(
      "options.accessibility.text_background_opacity", 0.0, 1.0, 0.0F, var0 -> var0.textBackgroundOpacity, (var0, var1) -> {
         var0.textBackgroundOpacity = var1;
         Minecraft.getInstance().gui.getChat().rescaleChat();
      }, (var0, var1) -> var1.percentValueLabel(var1.toPct(var1.get(var0)))
   );
   public static final CycleOption AMBIENT_OCCLUSION = new CycleOption("options.ao", (var0, var1) -> {
      var0.ambientOcclusion = AmbientOcclusionStatus.byId(var0.ambientOcclusion.getId() + var1);
      Minecraft.getInstance().levelRenderer.allChanged();
   }, (var0, var1) -> var1.genericValueLabel(new TranslatableComponent(var0.ambientOcclusion.getKey())));
   public static final CycleOption ATTACK_INDICATOR = new CycleOption(
      "options.attackIndicator",
      (var0, var1) -> var0.attackIndicator = AttackIndicatorStatus.byId(var0.attackIndicator.getId() + var1),
      (var0, var1) -> var1.genericValueLabel(new TranslatableComponent(var0.attackIndicator.getKey()))
   );
   public static final CycleOption CHAT_VISIBILITY = new CycleOption(
      "options.chat.visibility",
      (var0, var1) -> var0.chatVisibility = ChatVisiblity.byId((var0.chatVisibility.getId() + var1) % 3),
      (var0, var1) -> var1.genericValueLabel(new TranslatableComponent(var0.chatVisibility.getKey()))
   );
   private static final Component GRAPHICS_TOOLTIP_FAST = new TranslatableComponent("options.graphics.fast.tooltip");
   private static final Component GRAPHICS_TOOLTIP_FABULOUS = new TranslatableComponent(
      "options.graphics.fabulous.tooltip", new TranslatableComponent("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC)
   );
   private static final Component GRAPHICS_TOOLTIP_FANCY = new TranslatableComponent("options.graphics.fancy.tooltip");
   public static final CycleOption GRAPHICS = new CycleOption("options.graphics", (var0, var1) -> {
      Minecraft var2 = Minecraft.getInstance();
      GpuWarnlistManager var3x = var2.getGpuWarnlistManager();
      if (var0.graphicsMode == GraphicsStatus.FANCY && var3x.willShowWarning()) {
         var3x.showWarning();
      } else {
         var0.graphicsMode = var0.graphicsMode.cycleNext();
         if (var0.graphicsMode == GraphicsStatus.FABULOUS && (!GlStateManager.supportsFramebufferBlit() || var3x.isSkippingFabulous())) {
            var0.graphicsMode = GraphicsStatus.FAST;
         }

         var2.levelRenderer.allChanged();
      }
   }, (var0, var1) -> {
      switch(var0.graphicsMode) {
         case FAST:
            var1.setTooltip(Minecraft.getInstance().font.split(GRAPHICS_TOOLTIP_FAST, 200));
            break;
         case FANCY:
            var1.setTooltip(Minecraft.getInstance().font.split(GRAPHICS_TOOLTIP_FANCY, 200));
            break;
         case FABULOUS:
            var1.setTooltip(Minecraft.getInstance().font.split(GRAPHICS_TOOLTIP_FABULOUS, 200));
      }

      MutableComponent var2 = new TranslatableComponent(var0.graphicsMode.getKey());
      return var0.graphicsMode == GraphicsStatus.FABULOUS ? var1.genericValueLabel(var2.withStyle(ChatFormatting.ITALIC)) : var1.genericValueLabel(var2);
   });
   public static final CycleOption GUI_SCALE = new CycleOption(
      "options.guiScale",
      (var0, var1) -> var0.guiScale = Integer.remainderUnsigned(
            var0.guiScale + var1, Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode()) + 1
         ),
      (var0, var1) -> var0.guiScale == 0 ? var1.genericValueLabel(new TranslatableComponent("options.guiScale.auto")) : var1.genericValueLabel(var0.guiScale)
   );
   public static final CycleOption MAIN_HAND = new CycleOption(
      "options.mainHand", (var0, var1) -> var0.mainHand = var0.mainHand.getOpposite(), (var0, var1) -> var1.genericValueLabel(var0.mainHand.getName())
   );
   public static final CycleOption NARRATOR = new CycleOption(
      "options.narrator",
      (var0, var1) -> {
         if (NarratorChatListener.INSTANCE.isActive()) {
            var0.narratorStatus = NarratorStatus.byId(var0.narratorStatus.getId() + var1);
         } else {
            var0.narratorStatus = NarratorStatus.OFF;
         }
   
         NarratorChatListener.INSTANCE.updateNarratorStatus(var0.narratorStatus);
      },
      (var0, var1) -> NarratorChatListener.INSTANCE.isActive()
            ? var1.genericValueLabel(var0.narratorStatus.getName())
            : var1.genericValueLabel(new TranslatableComponent("options.narrator.notavailable"))
   );
   public static final CycleOption PARTICLES = new CycleOption(
      "options.particles",
      (var0, var1) -> var0.particles = ParticleStatus.byId(var0.particles.getId() + var1),
      (var0, var1) -> var1.genericValueLabel(new TranslatableComponent(var0.particles.getKey()))
   );
   public static final CycleOption RENDER_CLOUDS = new CycleOption("options.renderClouds", (var0, var1) -> {
      var0.renderClouds = CloudStatus.byId(var0.renderClouds.getId() + var1);
      if (Minecraft.useShaderTransparency()) {
         RenderTarget var2 = Minecraft.getInstance().levelRenderer.getCloudsTarget();
         if (var2 != null) {
            var2.clear(Minecraft.ON_OSX);
         }
      }
   }, (var0, var1) -> var1.genericValueLabel(new TranslatableComponent(var0.renderClouds.getKey())));
   public static final CycleOption TEXT_BACKGROUND = new CycleOption(
      "options.accessibility.text_background",
      (var0, var1) -> var0.backgroundForChatOnly = !var0.backgroundForChatOnly,
      (var0, var1) -> var1.genericValueLabel(
            new TranslatableComponent(
               var0.backgroundForChatOnly ? "options.accessibility.text_background.chat" : "options.accessibility.text_background.everywhere"
            )
         )
   );
   private static final Component CHAT_TOOLTIP_HIDE_MATCHED_NAMES = new TranslatableComponent("options.hideMatchedNames.tooltip");
   public static final BooleanOption AUTO_JUMP = new BooleanOption("options.autoJump", var0 -> var0.autoJump, (var0, var1) -> var0.autoJump = var1);
   public static final BooleanOption AUTO_SUGGESTIONS = new BooleanOption(
      "options.autoSuggestCommands", var0 -> var0.autoSuggestions, (var0, var1) -> var0.autoSuggestions = var1
   );
   public static final BooleanOption HIDE_MATCHED_NAMES = new BooleanOption(
      "options.hideMatchedNames", CHAT_TOOLTIP_HIDE_MATCHED_NAMES, var0 -> var0.hideMatchedNames, (var0, var1) -> var0.hideMatchedNames = var1
   );
   public static final BooleanOption CHAT_COLOR = new BooleanOption("options.chat.color", var0 -> var0.chatColors, (var0, var1) -> var0.chatColors = var1);
   public static final BooleanOption CHAT_LINKS = new BooleanOption("options.chat.links", var0 -> var0.chatLinks, (var0, var1) -> var0.chatLinks = var1);
   public static final BooleanOption CHAT_LINKS_PROMPT = new BooleanOption(
      "options.chat.links.prompt", var0 -> var0.chatLinksPrompt, (var0, var1) -> var0.chatLinksPrompt = var1
   );
   public static final BooleanOption DISCRETE_MOUSE_SCROLL = new BooleanOption(
      "options.discrete_mouse_scroll", var0 -> var0.discreteMouseScroll, (var0, var1) -> var0.discreteMouseScroll = var1
   );
   public static final BooleanOption ENABLE_VSYNC = new BooleanOption("options.vsync", var0 -> var0.enableVsync, (var0, var1) -> {
      var0.enableVsync = var1;
      if (Minecraft.getInstance().getWindow() != null) {
         Minecraft.getInstance().getWindow().updateVsync(var0.enableVsync);
      }
   });
   public static final BooleanOption ENTITY_SHADOWS = new BooleanOption(
      "options.entityShadows", var0 -> var0.entityShadows, (var0, var1) -> var0.entityShadows = var1
   );
   public static final BooleanOption FORCE_UNICODE_FONT = new BooleanOption("options.forceUnicodeFont", var0 -> var0.forceUnicodeFont, (var0, var1) -> {
      var0.forceUnicodeFont = var1;
      Minecraft var2 = Minecraft.getInstance();
      if (var2.getWindow() != null) {
         var2.selectMainFont(var1);
      }
   });
   public static final BooleanOption INVERT_MOUSE = new BooleanOption(
      "options.invertMouse", var0 -> var0.invertYMouse, (var0, var1) -> var0.invertYMouse = var1
   );
   public static final BooleanOption REALMS_NOTIFICATIONS = new BooleanOption(
      "options.realmsNotifications", var0 -> var0.realmsNotifications, (var0, var1) -> var0.realmsNotifications = var1
   );
   public static final BooleanOption REDUCED_DEBUG_INFO = new BooleanOption(
      "options.reducedDebugInfo", var0 -> var0.reducedDebugInfo, (var0, var1) -> var0.reducedDebugInfo = var1
   );
   public static final BooleanOption SHOW_SUBTITLES = new BooleanOption(
      "options.showSubtitles", var0 -> var0.showSubtitles, (var0, var1) -> var0.showSubtitles = var1
   );
   public static final BooleanOption SNOOPER_ENABLED = new BooleanOption("options.snooper", var0 -> {
      if (var0.snooperEnabled) {
      }

      return false;
   }, (var0, var1) -> var0.snooperEnabled = var1);
   public static final CycleOption TOGGLE_CROUCH = new CycleOption(
      "key.sneak",
      (var0, var1) -> var0.toggleCrouch = !var0.toggleCrouch,
      (var0, var1) -> var1.genericValueLabel(new TranslatableComponent(var0.toggleCrouch ? "options.key.toggle" : "options.key.hold"))
   );
   public static final CycleOption TOGGLE_SPRINT = new CycleOption(
      "key.sprint",
      (var0, var1) -> var0.toggleSprint = !var0.toggleSprint,
      (var0, var1) -> var1.genericValueLabel(new TranslatableComponent(var0.toggleSprint ? "options.key.toggle" : "options.key.hold"))
   );
   public static final BooleanOption TOUCHSCREEN = new BooleanOption("options.touchscreen", var0 -> var0.touchscreen, (var0, var1) -> var0.touchscreen = var1);
   public static final BooleanOption USE_FULLSCREEN = new BooleanOption("options.fullscreen", var0 -> var0.fullscreen, (var0, var1) -> {
      var0.fullscreen = var1;
      Minecraft var2 = Minecraft.getInstance();
      if (var2.getWindow() != null && var2.getWindow().isFullscreen() != var0.fullscreen) {
         var2.getWindow().toggleFullScreen();
         var0.fullscreen = var2.getWindow().isFullscreen();
      }
   });
   public static final BooleanOption VIEW_BOBBING = new BooleanOption("options.viewBobbing", var0 -> var0.bobView, (var0, var1) -> var0.bobView = var1);
   private final Component caption;
   private Optional<List<FormattedCharSequence>> toolTip = Optional.empty();

   public Option(String var1) {
      this.caption = new TranslatableComponent(var1);
   }

   public abstract AbstractWidget createButton(Options var1, int var2, int var3, int var4);

   protected Component getCaption() {
      return this.caption;
   }

   public void setTooltip(List<FormattedCharSequence> var1) {
      this.toolTip = Optional.of(var1);
   }

   public Optional<List<FormattedCharSequence>> getTooltip() {
      return this.toolTip;
   }

   protected Component pixelValueLabel(int var1) {
      return new TranslatableComponent("options.pixel_value", this.getCaption(), var1);
   }

   protected Component percentValueLabel(double var1) {
      return new TranslatableComponent("options.percent_value", this.getCaption(), (int)(var1 * 100.0));
   }

   protected Component percentAddValueLabel(int var1) {
      return new TranslatableComponent("options.percent_add_value", this.getCaption(), var1);
   }

   protected Component genericValueLabel(Component var1) {
      return new TranslatableComponent("options.generic_value", this.getCaption(), var1);
   }

   protected Component genericValueLabel(int var1) {
      return this.genericValueLabel(new TextComponent(Integer.toString(var1)));
   }
}
