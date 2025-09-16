//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;

public interface TagType<T extends Tag> {
   T load(DataInput var1, int var2, NbtAccounter var3) throws IOException;

   default boolean isValue() {
      return false;
   }

   String getName();

   String getPrettyName();

   static TagType<EndTag> createInvalid(final int var0) {
      return new TagType<EndTag>() {
         public EndTag load(DataInput var1, int var2, NbtAccounter var3) throws IOException {
            throw new IllegalArgumentException("Invalid tag id: " + var0);
         }

         public String getName() {
            return "INVALID[" + var0 + "]";
         }

         public String getPrettyName() {
            return "UNKNOWN_" + var0;
         }
      };
   }
}
