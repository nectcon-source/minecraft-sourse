package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

public class FloatTag extends NumericTag {
   public static final FloatTag ZERO = new FloatTag(0.0F);
   public static final TagType<FloatTag> TYPE = new TagType<FloatTag>() {
      public FloatTag load(DataInput var1, int var2, NbtAccounter var3) throws IOException {
         var3.accountBits(96L);
         return FloatTag.valueOf(var1.readFloat());
      }

      @Override
      public String getName() {
         return "FLOAT";
      }

      @Override
      public String getPrettyName() {
         return "TAG_Float";
      }

      @Override
      public boolean isValue() {
         return true;
      }
   };
   private final float data;

   private FloatTag(float var1) {
      this.data = var1;
   }

   public static FloatTag valueOf(float var0) {
      return var0 == 0.0F ? ZERO : new FloatTag(var0);
   }

   @Override
   public void write(DataOutput var1) throws IOException {
      var1.writeFloat(this.data);
   }

   @Override
   public byte getId() {
      return 5;
   }

   @Override
   public TagType<FloatTag> getType() {
      return TYPE;
   }

   @Override
   public String toString() {
      return this.data + "f";
   }

   public FloatTag copy() {
      return this;
   }

   @Override
   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else {
         return var1 instanceof FloatTag && this.data == ((FloatTag)var1).data;
      }
   }

   @Override
   public int hashCode() {
      return Float.floatToIntBits(this.data);
   }

   @Override
   public Component getPrettyDisplay(String var1, int var2) {
      Component var3 = new TextComponent("f").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      return new TextComponent(String.valueOf(this.data)).append(var3).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
   }

   @Override
   public long getAsLong() {
      return (long)this.data;
   }

   @Override
   public int getAsInt() {
      return Mth.floor(this.data);
   }

   @Override
   public short getAsShort() {
      return (short)(Mth.floor(this.data) & 65535);
   }

   @Override
   public byte getAsByte() {
      return (byte)(Mth.floor(this.data) & 0xFF);
   }

   @Override
   public double getAsDouble() {
      return (double)this.data;
   }

   @Override
   public float getAsFloat() {
      return this.data;
   }

   @Override
   public Number getAsNumber() {
      return this.data;
   }
}
