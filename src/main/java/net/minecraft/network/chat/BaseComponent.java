package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.locale.Language;
import net.minecraft.util.FormattedCharSequence;

public abstract class BaseComponent implements MutableComponent {
   protected final List<Component> siblings = Lists.newArrayList();
   private FormattedCharSequence visualOrderText = FormattedCharSequence.EMPTY;
   @Nullable
   private Language decomposedWith;
   private Style style = Style.EMPTY;

   @Override
   public MutableComponent append(Component var1) {
      this.siblings.add(var1);
      return this;
   }

   @Override
   public String getContents() {
      return "";
   }

   @Override
   public List<Component> getSiblings() {
      return this.siblings;
   }

   @Override
   public MutableComponent setStyle(Style var1) {
      this.style = var1;
      return this;
   }

   @Override
   public Style getStyle() {
      return this.style;
   }

   public abstract BaseComponent plainCopy();

   @Override
   public final MutableComponent copy() {
      BaseComponent var1 = this.plainCopy();
      var1.siblings.addAll(this.siblings);
      var1.setStyle(this.style);
      return var1;
   }

   @Override
   public FormattedCharSequence getVisualOrderText() {
      Language var1 = Language.getInstance();
      if (this.decomposedWith != var1) {
         this.visualOrderText = var1.getVisualOrder(this);
         this.decomposedWith = var1;
      }

      return this.visualOrderText;
   }

   @Override
   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof BaseComponent)) {
         return false;
      } else {
         BaseComponent var2 = (BaseComponent)var1;
         return this.siblings.equals(var2.siblings) && Objects.equals(this.getStyle(), var2.getStyle());
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.getStyle(), this.siblings);
   }

   @Override
   public String toString() {
      return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
   }
}
