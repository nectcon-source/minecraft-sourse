

package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class PopupScreen extends Screen {
   private final FormattedText message;
   private final ImmutableList<ButtonOption> buttonOptions;
   private MultiLineLabel messageLines;
   private int contentTop;
   private int buttonWidth;

   protected PopupScreen(Component var1, List<FormattedText> var2, ImmutableList<ButtonOption> var3) {
      super(var1);
      this.messageLines = MultiLineLabel.EMPTY;
      this.message = FormattedText.composite(var2);
      this.buttonOptions = var3;
   }

   public String getNarrationMessage() {
      return super.getNarrationMessage() + ". " + this.message.getString();
   }

   public void init(Minecraft var1, int var2, int var3) {
      super.init(var1, var2, var3);

      ButtonOption var5;
      for(UnmodifiableIterator var4 = this.buttonOptions.iterator(); var4.hasNext(); this.buttonWidth = Math.max(this.buttonWidth, 20 + this.font.width(var5.message) + 20)) {
         var5 = (ButtonOption)var4.next();
      }

      int var11 = 5 + this.buttonWidth + 5;
      int var12 = var11 * this.buttonOptions.size();
      this.messageLines = MultiLineLabel.create(this.font, this.message, var12);
      int var6 = this.messageLines.getLineCount() * 9;
      this.contentTop = (int)((double)var3 / (double)2.0F - (double)var6 / (double)2.0F);
      int var7 = this.contentTop + var6 + 9 * 2;
      int var8 = (int)((double)var2 / (double)2.0F - (double)var12 / (double)2.0F);

      for(UnmodifiableIterator var9 = this.buttonOptions.iterator(); var9.hasNext(); var8 += var11) {
         ButtonOption var10 = (ButtonOption)var9.next();
         this.addButton(new Button(var8, var7, this.buttonWidth, 20, var10.message, var10.onPress));
      }

   }

   public void render(PoseStack var1, int var2, int var3, float var4) {
      this.renderDirtBackground(0);
      drawCenteredString(var1, this.font, this.title, this.width / 2, this.contentTop - 9 * 2, -1);
      this.messageLines.renderCentered(var1, this.width / 2, this.contentTop);
      super.render(var1, var2, var3, var4);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public static final class ButtonOption {
      private final Component message;
      private final Button.OnPress onPress;

      public ButtonOption(Component var1, Button.OnPress var2) {
         this.message = var1;
         this.onPress = var2;
      }
   }
}
