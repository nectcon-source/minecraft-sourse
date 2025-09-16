package net.minecraft.client.gui.screens.controls;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.lang3.ArrayUtils;

public class ControlList extends ContainerObjectSelectionList<ControlList.Entry> {
   private final ControlsScreen controlsScreen;
   private int maxNameWidth;

   public ControlList(ControlsScreen var1, Minecraft var2) {
      super(var2, var1.width + 45, var1.height, 43, var1.height - 32, 20);
      this.controlsScreen = var1;
      KeyMapping[] var3 = (KeyMapping[])ArrayUtils.clone(var2.options.keyMappings);
      Arrays.sort(var3);
      String var4 = null;

      for(KeyMapping var8 : var3) {
         String var9 = var8.getCategory();
         if (!var9.equals(var4)) {
            var4 = var9;
            this.addEntry(new CategoryEntry(new TranslatableComponent(var9)));
         }

         Component var10 = new TranslatableComponent(var8.getName());
         int var11 = var2.font.width(var10);
         if (var11 > this.maxNameWidth) {
            this.maxNameWidth = var11;
         }

         this.addEntry(new KeyEntry(var8, var10));
      }
   }

   @Override
   protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 15;
   }

   @Override
   public int getRowWidth() {
      return super.getRowWidth() + 32;
   }

   public class CategoryEntry extends ControlList.Entry {
      private final Component name;
      private final int width;

      public CategoryEntry(Component var2) {
         this.name = var2;
         this.width = ControlList.this.minecraft.font.width(this.name);
      }

      @Override
      public void render(PoseStack var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, float var10) {
         ControlList.this.minecraft
            .font
            .draw(var1, this.name, (float)(ControlList.this.minecraft.screen.width / 2 - this.width / 2), (float)(var3 + var6 - 9 - 1), 16777215);
      }

      @Override
      public boolean changeFocus(boolean var1) {
         return false;
      }

      @Override
      public List<? extends GuiEventListener> children() {
         return Collections.emptyList();
      }
   }

   public abstract static class Entry extends ContainerObjectSelectionList.Entry<ControlList.Entry> {
   }

   public class KeyEntry extends ControlList.Entry {
      private final KeyMapping key;
      private final Component name;
      private final Button changeButton;
      private final Button resetButton;

      private KeyEntry(final KeyMapping var2, final Component var3) {
         this.key = var2;
         this.name = var3;
         this.changeButton = new Button(0, 0, 75, 20, var3, (var2x) -> ControlList.this.controlsScreen.selectedKey = var2) {
            protected MutableComponent createNarrationMessage() {
               return var2.isUnbound() ? new TranslatableComponent("narrator.controls.unbound", new Object[]{var3}) : new TranslatableComponent("narrator.controls.bound", new Object[]{var3, super.createNarrationMessage()});
            }
         };
         this.resetButton = new Button(0, 0, 50, 20, new TranslatableComponent("controls.reset"), (var2x) -> {
            ControlList.this.minecraft.options.setKey(var2, var2.getDefaultKey());
            KeyMapping.resetMapping();
         }) {
            protected MutableComponent createNarrationMessage() {
               return new TranslatableComponent("narrator.controls.reset", new Object[]{var3});
            }
         };
      }

      @Override
      public void render(PoseStack var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, float var10) {
         boolean var11 = ControlList.this.controlsScreen.selectedKey == this.key;
         ControlList.this.minecraft.font.draw(var1, this.name, (float)(var4 + 90 - ControlList.this.maxNameWidth), (float)(var3 + var6 / 2 - 9 / 2), 16777215);
         this.resetButton.x = var4 + 190;
         this.resetButton.y = var3;
         this.resetButton.active = !this.key.isDefault();
         this.resetButton.render(var1, var7, var8, var10);
         this.changeButton.x = var4 + 105;
         this.changeButton.y = var3;
         this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
         boolean var12 = false;
         if (!this.key.isUnbound()) {
            for(KeyMapping var16 : ControlList.this.minecraft.options.keyMappings) {
               if (var16 != this.key && this.key.same(var16)) {
                  var12 = true;
                  break;
               }
            }

         }

         if (var11) {
         this.changeButton.setMessage((new TextComponent("> ")).append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.YELLOW)).append(" <").withStyle(ChatFormatting.YELLOW));
      } else if (var12) {
         this.changeButton.setMessage(this.changeButton.getMessage().copy().withStyle(ChatFormatting.RED));
      }

            this.changeButton.render(var1, var7, var8, var10);
      }

      @Override
      public List<? extends GuiEventListener> children() {
         return ImmutableList.of(this.changeButton, this.resetButton);
      }

      @Override
      public boolean mouseClicked(double var1, double var3, int var5) {
         if (this.changeButton.mouseClicked(var1, var3, var5)) {
            return true;
         } else {
            return this.resetButton.mouseClicked(var1, var3, var5);
         }
      }

      @Override
      public boolean mouseReleased(double var1, double var3, int var5) {
         return this.changeButton.mouseReleased(var1, var3, var5) || this.resetButton.mouseReleased(var1, var3, var5);
      }
   }
}
