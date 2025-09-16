

package net.minecraft.advancements;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancementList {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map<ResourceLocation, Advancement> advancements = Maps.newHashMap();
   private final Set<Advancement> roots = Sets.newLinkedHashSet();
   private final Set<Advancement> tasks = Sets.newLinkedHashSet();
   private Listener listener;

   public AdvancementList() {
   }

   private void remove(Advancement var1) {
      for(Advancement var3 : var1.getChildren()) {
         this.remove(var3);
      }

      LOGGER.info("Forgot about advancement {}", var1.getId());
      this.advancements.remove(var1.getId());
      if (var1.getParent() == null) {
         this.roots.remove(var1);
         if (this.listener != null) {
            this.listener.onRemoveAdvancementRoot(var1);
         }
      } else {
         this.tasks.remove(var1);
         if (this.listener != null) {
            this.listener.onRemoveAdvancementTask(var1);
         }
      }

   }

   public void remove(Set<ResourceLocation> var1) {
      for(ResourceLocation var3 : var1) {
         Advancement var4 = (Advancement)this.advancements.get(var3);
         if (var4 == null) {
            LOGGER.warn("Told to remove advancement {} but I don't know what that is", var3);
         } else {
            this.remove(var4);
         }
      }

   }

   public void add(Map<ResourceLocation, Advancement.Builder> var1) {
      Function<ResourceLocation, Advancement> var2 = Functions.forMap(this.advancements, null);

      while(!var1.isEmpty()) {
         boolean var3 = false;
         Iterator<Map.Entry<ResourceLocation, Advancement.Builder>> var4 = var1.entrySet().iterator();

         while(var4.hasNext()) {
            Map.Entry<ResourceLocation, Advancement.Builder> var5 = (Map.Entry)var4.next();
            ResourceLocation var6 = (ResourceLocation)var5.getKey();
            Advancement.Builder var7 = (Advancement.Builder)var5.getValue();
            if (var7.canBuild(var2)) {
               Advancement var8 = var7.build(var6);
               this.advancements.put(var6, var8);
               var3 = true;
               var4.remove();
               if (var8.getParent() == null) {
                  this.roots.add(var8);
                  if (this.listener != null) {
                     this.listener.onAddAdvancementRoot(var8);
                  }
               } else {
                  this.tasks.add(var8);
                  if (this.listener != null) {
                     this.listener.onAddAdvancementTask(var8);
                  }
               }
            }
         }

         if (!var3) {
            for(Map.Entry<ResourceLocation, Advancement.Builder> var10 : var1.entrySet()) {
               LOGGER.error("Couldn't load advancement {}: {}", var10.getKey(), var10.getValue());
            }
            break;
         }
      }

      LOGGER.info("Loaded {} advancements", this.advancements.size());
   }

   public void clear() {
      this.advancements.clear();
      this.roots.clear();
      this.tasks.clear();
      if (this.listener != null) {
         this.listener.onAdvancementsCleared();
      }

   }

   public Iterable<Advancement> getRoots() {
      return this.roots;
   }

   public Collection<Advancement> getAllAdvancements() {
      return this.advancements.values();
   }

   @Nullable
   public Advancement get(ResourceLocation var1) {
      return (Advancement)this.advancements.get(var1);
   }

   public void setListener(@Nullable Listener var1) {
      this.listener = var1;
      if (var1 != null) {
         for(Advancement var3 : this.roots) {
            var1.onAddAdvancementRoot(var3);
         }

         for(Advancement var5 : this.tasks) {
            var1.onAddAdvancementTask(var5);
         }
      }

   }

   public interface Listener {
      void onAddAdvancementRoot(Advancement var1);

      void onRemoveAdvancementRoot(Advancement var1);

      void onAddAdvancementTask(Advancement var1);

      void onRemoveAdvancementTask(Advancement var1);

      void onAdvancementsCleared();
   }
}
