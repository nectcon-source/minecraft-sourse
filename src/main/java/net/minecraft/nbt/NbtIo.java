//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.nbt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class NbtIo {
   public static CompoundTag readCompressed(File var0) throws IOException {
      CompoundTag var3;
      try (InputStream var1 = new FileInputStream(var0)) {
         var3 = readCompressed(var1);
      }

      return var3;
   }

   public static CompoundTag readCompressed(InputStream var0) throws IOException {
      CompoundTag var3;
      try (DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(var0)))) {
         var3 = read(var1, NbtAccounter.UNLIMITED);
      }

      return var3;
   }

   public static void writeCompressed(CompoundTag var0, File var1) throws IOException {
      try (OutputStream var2 = new FileOutputStream(var1)) {
         writeCompressed(var0, var2);
      }

   }

   public static void writeCompressed(CompoundTag var0, OutputStream var1) throws IOException {
      try (DataOutputStream var2 = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(var1)))) {
         write(var0, (DataOutput)var2);
      }

   }

   public static void write(CompoundTag var0, File var1) throws IOException {
      try (FileOutputStream var2 = new FileOutputStream(var1)) {
         DataOutputStream var4 = new DataOutputStream(var2);
         Throwable var5 = null;

         try {
            write(var0, (DataOutput)var4);
         } catch (Throwable var28) {
            var5 = var28;
            throw var28;
         } finally {
            if (var4 != null) {
               if (var5 != null) {
                  try {
                     var4.close();
                  } catch (Throwable var27) {
                     var5.addSuppressed(var27);
                  }
               } else {
                  var4.close();
               }
            }

         }
      }

   }

   @Nullable
   public static CompoundTag read(File var0) throws IOException {
      if (!var0.exists()) {
         return null;
      } else {
         Object var5;
         try (FileInputStream var1 = new FileInputStream(var0)) {
            DataInputStream var3 = new DataInputStream(var1);
            Throwable var4 = null;

            try {
               var5 = read(var3, NbtAccounter.UNLIMITED);
            } catch (Throwable var28) {
               var5 = var28;
               var4 = var28;
               throw var28;
            } finally {
               if (var3 != null) {
                  if (var4 != null) {
                     try {
                        var3.close();
                     } catch (Throwable var27) {
                        var4.addSuppressed(var27);
                     }
                  } else {
                     var3.close();
                  }
               }

            }
         }

         return (CompoundTag)var5;
      }
   }

   public static CompoundTag read(DataInput var0) throws IOException {
      return read(var0, NbtAccounter.UNLIMITED);
   }

   public static CompoundTag read(DataInput var0, NbtAccounter var1) throws IOException {
      Tag var2 = readUnnamedTag(var0, 0, var1);
      if (var2 instanceof CompoundTag) {
         return (CompoundTag)var2;
      } else {
         throw new IOException("Root tag must be a named compound tag");
      }
   }

   public static void write(CompoundTag var0, DataOutput var1) throws IOException {
      writeUnnamedTag(var0, var1);
   }

   private static void writeUnnamedTag(Tag var0, DataOutput var1) throws IOException {
      var1.writeByte(var0.getId());
      if (var0.getId() != 0) {
         var1.writeUTF("");
         var0.write(var1);
      }
   }

   private static Tag readUnnamedTag(DataInput var0, int var1, NbtAccounter var2) throws IOException {
      byte var3 = var0.readByte();
      if (var3 == 0) {
         return EndTag.INSTANCE;
      } else {
         var0.readUTF();

         try {
            return TagTypes.getType(var3).load(var0, var1, var2);
         } catch (IOException var4_1) {
            CrashReport var5 = CrashReport.forThrowable(var4_1, "Loading NBT data");
            CrashReportCategory var6 = var5.addCategory("NBT Tag");
            var6.setDetail("Tag type", var3);
            throw new ReportedException(var5);
         }
      }
   }
}
