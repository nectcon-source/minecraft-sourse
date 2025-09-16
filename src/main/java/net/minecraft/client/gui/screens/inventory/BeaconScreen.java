

package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public class BeaconScreen extends AbstractContainerScreen<BeaconMenu> {
   private static final ResourceLocation BEACON_LOCATION = new ResourceLocation("textures/gui/container/beacon.png");
   private static final Component PRIMARY_EFFECT_LABEL = new TranslatableComponent("block.minecraft.beacon.primary");
   private static final Component SECONDARY_EFFECT_LABEL = new TranslatableComponent("block.minecraft.beacon.secondary");
   private BeaconConfirmButton confirmButton;
   private boolean initPowerButtons;
   private MobEffect primary;
   private MobEffect secondary;

   public BeaconScreen(final BeaconMenu var1, Inventory var2, Component var3) {
      super(var1, var2, var3);
      this.imageWidth = 230;
      this.imageHeight = 219;
      var1.addSlotListener(new ContainerListener() {
         public void refreshContainer(AbstractContainerMenu var1x, NonNullList<ItemStack> var2) {
         }

         public void slotChanged(AbstractContainerMenu var1x, int var2, ItemStack var3) {
         }

         public void setContainerData(AbstractContainerMenu var1x, int var2, int var3) {
            BeaconScreen.this.primary = var1.getPrimaryEffect();
            BeaconScreen.this.secondary = var1.getSecondaryEffect();
            BeaconScreen.this.initPowerButtons = true;
         }
      });
   }

   protected void init() {
      super.init();
      this.confirmButton = (BeaconConfirmButton)this.addButton(new BeaconConfirmButton(this.leftPos + 164, this.topPos + 107));
      this.addButton(new BeaconCancelButton(this.leftPos + 190, this.topPos + 107));
      this.initPowerButtons = true;
      this.confirmButton.active = false;
   }

   public void tick() {
      super.tick();
      int var1 = ((BeaconMenu)this.menu).getLevels();
      if (this.initPowerButtons && var1 >= 0) {
         this.initPowerButtons = false;

         for(int var2 = 0; var2 <= 2; ++var2) {
            int var3 = BeaconBlockEntity.BEACON_EFFECTS[var2].length;
            int var4 = var3 * 22 + (var3 - 1) * 2;

            for(int var5 = 0; var5 < var3; ++var5) {
               MobEffect var6 = BeaconBlockEntity.BEACON_EFFECTS[var2][var5];
               BeaconPowerButton var7 = new BeaconPowerButton(this.leftPos + 76 + var5 * 24 - var4 / 2, this.topPos + 22 + var2 * 25, var6, true);
               this.addButton(var7);
               if (var2 >= var1) {
                  var7.active = false;
               } else if (var6 == this.primary) {
                  var7.setSelected(true);
               }
            }
         }

         int var8 = 3;
         int var9 = BeaconBlockEntity.BEACON_EFFECTS[3].length + 1;
         int var10 = var9 * 22 + (var9 - 1) * 2;

         for(int var11 = 0; var11 < var9 - 1; ++var11) {
            MobEffect var13 = BeaconBlockEntity.BEACON_EFFECTS[3][var11];
            BeaconPowerButton var14 = new BeaconPowerButton(this.leftPos + 167 + var11 * 24 - var10 / 2, this.topPos + 47, var13, false);
            this.addButton(var14);
            if (3 >= var1) {
               var14.active = false;
            } else if (var13 == this.secondary) {
               var14.setSelected(true);
            }
         }

         if (this.primary != null) {
            BeaconPowerButton var12 = new BeaconPowerButton(this.leftPos + 167 + (var9 - 1) * 24 - var10 / 2, this.topPos + 47, this.primary, false);
            this.addButton(var12);
            if (3 >= var1) {
               var12.active = false;
            } else if (this.primary == this.secondary) {
               var12.setSelected(true);
            }
         }
      }

      this.confirmButton.active = ((BeaconMenu)this.menu).hasPayment() && this.primary != null;
   }

   protected void renderLabels(PoseStack var1, int var2, int var3) {
      drawCenteredString(var1, this.font, PRIMARY_EFFECT_LABEL, 62, 10, 14737632);
      drawCenteredString(var1, this.font, SECONDARY_EFFECT_LABEL, 169, 10, 14737632);

      for(AbstractWidget var5 : this.buttons) {
         if (var5.isHovered()) {
            var5.renderToolTip(var1, var2 - this.leftPos, var3 - this.topPos);
            break;
         }
      }

   }

   protected void renderBg(PoseStack var1, float var2, int var3, int var4) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bind(BEACON_LOCATION);
      int var5 = (this.width - this.imageWidth) / 2;
      int var6 = (this.height - this.imageHeight) / 2;
      this.blit(var1, var5, var6, 0, 0, this.imageWidth, this.imageHeight);
      this.itemRenderer.blitOffset = 100.0F;
      this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.NETHERITE_INGOT), var5 + 20, var6 + 109);
      this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.EMERALD), var5 + 41, var6 + 109);
      this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.DIAMOND), var5 + 41 + 22, var6 + 109);
      this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.GOLD_INGOT), var5 + 42 + 44, var6 + 109);
      this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.IRON_INGOT), var5 + 42 + 66, var6 + 109);
      this.itemRenderer.blitOffset = 0.0F;
   }

   public void render(PoseStack var1, int var2, int var3, float var4) {
      this.renderBackground(var1);
      super.render(var1, var2, var3, var4);
      this.renderTooltip(var1, var2, var3);
   }

   abstract static class BeaconScreenButton extends AbstractButton {
      private boolean selected;

      protected BeaconScreenButton(int var1, int var2) {
         super(var1, var2, 22, 22, TextComponent.EMPTY);
      }

      public void renderButton(PoseStack var1, int var2, int var3, float var4) {
         Minecraft.getInstance().getTextureManager().bind(BeaconScreen.BEACON_LOCATION);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         int var5 = 219;
         int var6 = 0;
         if (!this.active) {
            var6 += this.width * 2;
         } else if (this.selected) {
            var6 += this.width * 1;
         } else if (this.isHovered()) {
            var6 += this.width * 3;
         }

         this.blit(var1, this.x, this.y, var6, 219, this.width, this.height);
         this.renderIcon(var1);
      }

      protected abstract void renderIcon(PoseStack var1);

      public boolean isSelected() {
         return this.selected;
      }

      public void setSelected(boolean var1) {
         this.selected = var1;
      }
   }

   class BeaconPowerButton extends BeaconScreenButton {
      private final MobEffect effect;
      private final TextureAtlasSprite sprite;
      private final boolean isPrimary;
      private final Component tooltip;

      public BeaconPowerButton(int var2, int var3, MobEffect var4, boolean var5) {
         super(var2, var3);
         this.effect = var4;
         this.sprite = Minecraft.getInstance().getMobEffectTextures().get(var4);
         this.isPrimary = var5;
         this.tooltip = this.createTooltip(var4, var5);
      }

      private Component createTooltip(MobEffect var1, boolean var2) {
         MutableComponent var3 = new TranslatableComponent(var1.getDescriptionId());
         if (!var2 && var1 != MobEffects.REGENERATION) {
            var3.append(" II");
         }

         return var3;
      }

      public void onPress() {
         if (!this.isSelected()) {
            if (this.isPrimary) {
               BeaconScreen.this.primary = this.effect;
            } else {
               BeaconScreen.this.secondary = this.effect;
            }

            BeaconScreen.this.buttons.clear();
            BeaconScreen.this.children.clear();
            BeaconScreen.this.init();
            BeaconScreen.this.tick();
         }
      }

      public void renderToolTip(PoseStack var1, int var2, int var3) {
         BeaconScreen.this.renderTooltip(var1, this.tooltip, var2, var3);
      }

      protected void renderIcon(PoseStack var1) {
         Minecraft.getInstance().getTextureManager().bind(this.sprite.atlas().location());
         blit(var1, this.x + 2, this.y + 2, this.getBlitOffset(), 18, 18, this.sprite);
      }
   }

   abstract static class BeaconSpriteScreenButton extends BeaconScreenButton {
      private final int iconX;
      private final int iconY;

      protected BeaconSpriteScreenButton(int var1, int var2, int var3, int var4) {
         super(var1, var2);
         this.iconX = var3;
         this.iconY = var4;
      }

      protected void renderIcon(PoseStack var1) {
         this.blit(var1, this.x + 2, this.y + 2, this.iconX, this.iconY, 18, 18);
      }
   }

   class BeaconConfirmButton extends BeaconSpriteScreenButton {
      public BeaconConfirmButton(int var2, int var3) {
         super(var2, var3, 90, 220);
      }

      public void onPress() {
         BeaconScreen.this.minecraft.getConnection().send(new ServerboundSetBeaconPacket(MobEffect.getId(BeaconScreen.this.primary), MobEffect.getId(BeaconScreen.this.secondary)));
         BeaconScreen.this.minecraft.player.connection.send(new ServerboundContainerClosePacket(BeaconScreen.this.minecraft.player.containerMenu.containerId));
         BeaconScreen.this.minecraft.setScreen((Screen)null);
      }

      public void renderToolTip(PoseStack var1, int var2, int var3) {
         BeaconScreen.this.renderTooltip(var1, CommonComponents.GUI_DONE, var2, var3);
      }
   }

   class BeaconCancelButton extends BeaconSpriteScreenButton {
      public BeaconCancelButton(int var2, int var3) {
         super(var2, var3, 112, 220);
      }

      public void onPress() {
         BeaconScreen.this.minecraft.player.connection.send(new ServerboundContainerClosePacket(BeaconScreen.this.minecraft.player.containerMenu.containerId));
         BeaconScreen.this.minecraft.setScreen((Screen)null);
      }

      public void renderToolTip(PoseStack var1, int var2, int var3) {
         BeaconScreen.this.renderTooltip(var1, CommonComponents.GUI_CANCEL, var2, var3);
      }
   }
}
