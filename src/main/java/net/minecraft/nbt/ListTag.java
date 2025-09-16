package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class ListTag extends CollectionTag<Tag> {
   public static final TagType<ListTag> TYPE = new TagType<ListTag>() {
      public ListTag load(DataInput var1, int var2, NbtAccounter var3) throws IOException {
         var3.accountBits(296L);
         if (var2 > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
         } else {
            byte var4 = var1.readByte();
            int var5 = var1.readInt();
            if (var4 == 0 && var5 > 0) {
               throw new RuntimeException("Missing type on ListTag");
            } else {
               var3.accountBits(32L * (long)var5);
               TagType<?> var6 = TagTypes.getType(var4);
               List<Tag> var7 = Lists.newArrayListWithCapacity(var5);

               for(int var8 = 0; var8 < var5; ++var8) {
                  var7.add(var6.load(var1, var2 + 1, var3));
               }

               return new ListTag(var7, var4);
            }
         }
      }

      @Override
      public String getName() {
         return "LIST";
      }

      @Override
      public String getPrettyName() {
         return "TAG_List";
      }
   };
   private static final ByteSet INLINE_ELEMENT_TYPES = new ByteOpenHashSet(Arrays.asList((byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6));
   private final List<Tag> list;
   private byte type;

   private ListTag(List<Tag> var1, byte var2) {
      this.list = var1;
      this.type = var2;
   }

   public ListTag() {
      this(Lists.newArrayList(), (byte)0);
   }

   @Override
   public void write(DataOutput var1) throws IOException {
      if (this.list.isEmpty()) {
         this.type = 0;
      } else {
         this.type = this.list.get(0).getId();
      }

      var1.writeByte(this.type);
      var1.writeInt(this.list.size());

      for(Tag var2 : this.list) {
         var2.write(var1);
      }
   }

   @Override
   public byte getId() {
      return 9;
   }

   @Override
   public TagType<ListTag> getType() {
      return TYPE;
   }

   @Override
   public String toString() {
      StringBuilder var1 = new StringBuilder("[");

      for(int var2 = 0; var2 < this.list.size(); ++var2) {
         if (var2 != 0) {
            var1.append(',');
         }

         var1.append(this.list.get(var2));
      }

      return var1.append(']').toString();
   }

   private void updateTypeAfterRemove() {
      if (this.list.isEmpty()) {
         this.type = 0;
      }
   }

   @Override
   public Tag remove(int var1) {
      Tag var2 = (Tag)this.list.remove(var1);
      this.updateTypeAfterRemove();
      return var2;
   }


   @Override
   public boolean isEmpty() {
      return this.list.isEmpty();
   }

   public CompoundTag getCompound(int var1) {
      if (var1 >= 0 && var1 < this.list.size()) {
         Tag var2 = (Tag)this.list.get(var1);
         if (var2.getId() == 10) {
            return (CompoundTag)var2;
         }
      }

      return new CompoundTag();
   }

   public ListTag getList(int var1) {
      if (var1 >= 0 && var1 < this.list.size()) {
         Tag var2 = (Tag)this.list.get(var1);
         if (var2.getId() == 9) {
            return (ListTag)var2;
         }
      }

      return new ListTag();
   }

   public short getShort(int var1) {
      if (var1 >= 0 && var1 < this.list.size()) {
         Tag var2 = (Tag)this.list.get(var1);
         if (var2.getId() == 2) {
            return ((ShortTag)var2).getAsShort();
         }
      }

      return 0;
   }

   public int getInt(int var1) {
      if (var1 >= 0 && var1 < this.list.size()) {
         Tag var2 = (Tag)this.list.get(var1);
         if (var2.getId() == 3) {
            return ((IntTag)var2).getAsInt();
         }
      }

      return 0;
   }

   public int[] getIntArray(int var1) {
      if (var1 >= 0 && var1 < this.list.size()) {
         Tag var2 = (Tag)this.list.get(var1);
         if (var2.getId() == 11) {
            return ((IntArrayTag)var2).getAsIntArray();
         }
      }

      return new int[0];
   }

   public double getDouble(int var1) {
      if (var1 >= 0 && var1 < this.list.size()) {
         Tag var2 = (Tag)this.list.get(var1);
         if (var2.getId() == 6) {
            return ((DoubleTag)var2).getAsDouble();
         }
      }

      return (double)0.0F;
   }

   public float getFloat(int var1) {
      if (var1 >= 0 && var1 < this.list.size()) {
         Tag var2 = (Tag)this.list.get(var1);
         if (var2.getId() == 5) {
            return ((FloatTag)var2).getAsFloat();
         }
      }

      return 0.0F;
   }

   public String getString(int var1) {
      if (var1 >= 0 && var1 < this.list.size()) {
         Tag var2 = (Tag)this.list.get(var1);
         return var2.getId() == 8 ? var2.getAsString() : var2.toString();
      } else {
         return "";
      }
   }

   @Override
   public int size() {
      return this.list.size();
   }

   public Tag get(int var1) {
      return this.list.get(var1);
   }

   @Override
   public Tag set(int var1, Tag var2) {
      Tag var3 = this.get(var1);
      if (!this.setTag(var1, var2)) {
         throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", var2.getId(), this.type));
      } else {
         return var3;
      }
   }

   @Override
   public void add(int var1, Tag var2) {
      if (!this.addTag(var1, var2)) {
         throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", var2.getId(), this.type));
      }
   }

   @Override
   public boolean setTag(int var1, Tag var2) {
      if (this.updateType(var2)) {
         this.list.set(var1, var2);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean addTag(int var1, Tag var2) {
      if (this.updateType(var2)) {
         this.list.add(var1, var2);
         return true;
      } else {
         return false;
      }
   }

   private boolean updateType(Tag var1) {
      if (var1.getId() == 0) {
         return false;
      } else if (this.type == 0) {
         this.type = var1.getId();
         return true;
      } else {
         return this.type == var1.getId();
      }
   }

   public ListTag copy() {
      Iterable<Tag> var1 = (Iterable<Tag>)(TagTypes.getType(this.type).isValue() ? this.list : Iterables.transform(this.list, Tag::copy));
      List<Tag> var2 = Lists.newArrayList(var1);
      return new ListTag(var2, this.type);
   }

   @Override
   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else {
         return var1 instanceof ListTag && Objects.equals(this.list, ((ListTag)var1).list);
      }
   }

   @Override
   public int hashCode() {
      return this.list.hashCode();
   }

   @Override
   public Component getPrettyDisplay(String var1, int var2) {
      if (this.isEmpty()) {
         return new TextComponent("[]");
      } else if (INLINE_ELEMENT_TYPES.contains(this.type) && this.size() <= 8) {
         String var7 = ", ";
         MutableComponent var8 = new TextComponent("[");

         for(int var9 = 0; var9 < this.list.size(); ++var9) {
            if (var9 != 0) {
               var8.append(", ");
            }

            var8.append(((Tag)this.list.get(var9)).getPrettyDisplay());
         }

         var8.append("]");
         return var8;
      } else {
         MutableComponent var3 = new TextComponent("[");
         if (!var1.isEmpty()) {
            var3.append("\n");
         }

         String var4 = String.valueOf(',');

         for(int var5 = 0; var5 < this.list.size(); ++var5) {
            MutableComponent var6 = new TextComponent(Strings.repeat(var1, var2 + 1));
            var6.append(((Tag)this.list.get(var5)).getPrettyDisplay(var1, var2 + 1));
            if (var5 != this.list.size() - 1) {
               var6.append(var4).append(var1.isEmpty() ? " " : "\n");
            }

            var3.append(var6);
         }

         if (!var1.isEmpty()) {
            var3.append("\n").append(Strings.repeat(var1, var2));
         }

         var3.append("]");
         return var3;
      }
   }

   @Override
   public byte getElementType() {
      return this.type;
   }

   @Override
   public void clear() {
      this.list.clear();
      this.type = 0;
   }
}
