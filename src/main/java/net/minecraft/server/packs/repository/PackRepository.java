package net.minecraft.server.packs.repository;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.server.packs.PackResources;

public class PackRepository implements AutoCloseable {
   private final Set<RepositorySource> sources;
   private Map<String, Pack> available = ImmutableMap.of();
   private List<Pack> selected = ImmutableList.of();
   private final Pack.PackConstructor constructor;

   public PackRepository(Pack.PackConstructor var1, RepositorySource... var2) {
      this.constructor = var1;
      this.sources = ImmutableSet.copyOf(var2);
   }

   public PackRepository(RepositorySource... var1) {
      this(Pack::new, var1);
   }

   public void reload() {
      List<String> var1 = this.selected.stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
      this.close();
      this.available = this.discoverAvailable();
      this.selected = this.rebuildSelected(var1);
   }

   private Map<String, Pack> discoverAvailable() {
      Map<String, Pack> var1 = Maps.newTreeMap();

      for(RepositorySource var3 : this.sources) {
//         var3.loadPacks(var1x -> var1x.getId(), this.constructor);
          var3.loadPacks(pack -> var1.put(pack.getId(), pack), this.constructor);
      }

      return ImmutableMap.copyOf(var1);
   }

   public void setSelected(Collection<String> var1) {
      this.selected = this.rebuildSelected(var1);
   }

   private List<Pack> rebuildSelected(Collection<String> var1) {
      List<Pack> var2 = this.getAvailablePacks(var1).collect(Collectors.toList());

      for(Pack var4 : this.available.values()) {
         if (var4.isRequired() && !var2.contains(var4)) {
            var4.getDefaultPosition().insert(var2, var4, Functions.identity(), false);
         }
      }

      return ImmutableList.copyOf(var2);
   }

   private Stream<Pack> getAvailablePacks(Collection<String> var1) {
      return var1.stream().map(this.available::get).filter(Objects::nonNull);
   }

   public Collection<String> getAvailableIds() {
      return this.available.keySet();
   }

   public Collection<Pack> getAvailablePacks() {
      return this.available.values();
   }

   public Collection<String> getSelectedIds() {
      return this.selected.stream().map(Pack::getId).collect(ImmutableSet.toImmutableSet());
   }

   public Collection<Pack> getSelectedPacks() {
      return this.selected;
   }

   @Nullable
   public Pack getPack(String var1) {
      return this.available.get(var1);
   }

   @Override
   public void close() {
      this.available.values().forEach(Pack::close);
   }

   public boolean isAvailable(String var1) {
      return this.available.containsKey(var1);
   }

   public List<PackResources> openAllSelected() {
      return this.selected.stream().map(Pack::open).collect(ImmutableList.toImmutableList());
   }
}
