package net.minecraft.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;

public class CrudeIncrementalIntIdentityHashBiMap<K> implements IdMap<K> {
   private static final Object EMPTY_SLOT = null;
   private K[] keys;
   private int[] values;
   private K[] byId;
   private int nextId;
   private int size;

   public CrudeIncrementalIntIdentityHashBiMap(int var1) {
      var1 = (int)((float)var1 / 0.8F);
      this.keys = (K[])(new Object[var1]);
      this.values = new int[var1];
      this.byId = (K[])(new Object[var1]);
   }

   @Override
   public int getId(@Nullable K var1) {
      return this.getValue(this.indexOf(var1, this.hash(var1)));
   }

   @Nullable
   @Override
   public K byId(int var1) {
      return var1 >= 0 && var1 < this.byId.length ? this.byId[var1] : null;
   }

   private int getValue(int var1) {
      return var1 == -1 ? -1 : this.values[var1];
   }

   public int add(K var1) {
      int var2 = this.nextId();
      this.addMapping(var1, var2);
      return var2;
   }

   private int nextId() {
      while(this.nextId < this.byId.length && this.byId[this.nextId] != null) {
         ++this.nextId;
      }

      return this.nextId;
   }

   private void grow(int var1) {
      K[] var2 = this.keys;
      int[] var3 = this.values;
      this.keys = (K[])(new Object[var1]);
      this.values = new int[var1];
      this.byId = (K[])(new Object[var1]);
      this.nextId = 0;
      this.size = 0;

      for(int var4 = 0; var4 < var2.length; ++var4) {
         if (var2[var4] != null) {
            this.addMapping(var2[var4], var3[var4]);
         }
      }
   }

   public void addMapping(K var1, int var2) {
      int var3 = Math.max(var2, this.size + 1);
      if ((float)var3 >= (float)this.keys.length * 0.8F) {
         int var4;
         for(var4 = this.keys.length << 1; var4 < var2; var4 <<= 1) {
         }

         this.grow(var4);
      }

      int var5 = this.findEmpty(this.hash(var1));
      this.keys[var5] = var1;
      this.values[var5] = var2;
      this.byId[var2] = var1;
      ++this.size;
      if (var2 == this.nextId) {
         ++this.nextId;
      }
   }

   private int hash(@Nullable K var1) {
      return (Mth.murmurHash3Mixer(System.identityHashCode(var1)) & 2147483647) % this.keys.length;
   }

   private int indexOf(@Nullable K var1, int var2) {
      for(int var3 = var2; var3 < this.keys.length; ++var3) {
         if (this.keys[var3] == var1) {
            return var3;
         }

         if (this.keys[var3] == EMPTY_SLOT) {
            return -1;
         }
      }

      for(int var4 = 0; var4 < var2; ++var4) {
         if (this.keys[var4] == var1) {
            return var4;
         }

         if (this.keys[var4] == EMPTY_SLOT) {
            return -1;
         }
      }

      return -1;
   }

   private int findEmpty(int var1) {
      for(int var2 = var1; var2 < this.keys.length; ++var2) {
         if (this.keys[var2] == EMPTY_SLOT) {
            return var2;
         }
      }

      for(int var3 = 0; var3 < var1; ++var3) {
         if (this.keys[var3] == EMPTY_SLOT) {
            return var3;
         }
      }

      throw new RuntimeException("Overflowed :(");
   }

   @Override
   public Iterator<K> iterator() {
      return Iterators.filter(Iterators.forArray(this.byId), Predicates.notNull());
   }

   public void clear() {
      Arrays.fill(this.keys, null);
      Arrays.fill(this.byId, null);
      this.nextId = 0;
      this.size = 0;
   }

   public int size() {
      return this.size;
   }
}
