package net.minecraft.server;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerAdvancements {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = new GsonBuilder()
      .registerTypeAdapter(AdvancementProgress.class, new AdvancementProgress.Serializer())
      .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
      .setPrettyPrinting()
      .create();
   private static final TypeToken<Map<ResourceLocation, AdvancementProgress>> TYPE_TOKEN = new TypeToken<Map<ResourceLocation, AdvancementProgress>>() {
   };
   private final DataFixer dataFixer;
   private final PlayerList playerList;
   private final File file;
   private final Map<Advancement, AdvancementProgress> advancements = Maps.newLinkedHashMap();
   private final Set<Advancement> visible = Sets.newLinkedHashSet();
   private final Set<Advancement> visibilityChanged = Sets.newLinkedHashSet();
   private final Set<Advancement> progressChanged = Sets.newLinkedHashSet();
   private ServerPlayer player;
   @Nullable
   private Advancement lastSelectedTab;
   private boolean isFirstPacket = true;

   public PlayerAdvancements(DataFixer var1, PlayerList var2, ServerAdvancementManager var3, File var4, ServerPlayer var5) {
      this.dataFixer = var1;
      this.playerList = var2;
      this.file = var4;
      this.player = var5;
      this.load(var3);
   }

   public void setPlayer(ServerPlayer var1) {
      this.player = var1;
   }

   public void stopListening() {
      for(CriterionTrigger<?> var1 : CriteriaTriggers.all()) {
         var1.removePlayerListeners(this);
      }
   }

   public void reload(ServerAdvancementManager var1) {
      this.stopListening();
      this.advancements.clear();
      this.visible.clear();
      this.visibilityChanged.clear();
      this.progressChanged.clear();
      this.isFirstPacket = true;
      this.lastSelectedTab = null;
      this.load(var1);
   }

   private void registerListeners(ServerAdvancementManager var1) {
      for(Advancement var3 : var1.getAllAdvancements()) {
         this.registerListeners(var3);
      }
   }

   private void ensureAllVisible() {
      List<Advancement> var1 = Lists.newArrayList();

      for(Map.Entry<Advancement, AdvancementProgress> var3 : this.advancements.entrySet()) {
         if (((AdvancementProgress)var3.getValue()).isDone()) {
            var1.add(var3.getKey());
            this.progressChanged.add(var3.getKey());
         }
      }

      for(Advancement var5 : var1) {
         this.ensureVisibility(var5);
      }
   }

   private void checkForAutomaticTriggers(ServerAdvancementManager var1) {
      for(Advancement var3 : var1.getAllAdvancements()) {
         if (var3.getCriteria().isEmpty()) {
            this.award(var3, "");
            var3.getRewards().grant(this.player);
         }
      }
   }

   private void load(ServerAdvancementManager var1) {
      if (this.file.isFile()) {
         try (JsonReader var2 = new JsonReader(new StringReader(Files.toString(this.file, StandardCharsets.UTF_8)))) {
            var2.setLenient(false);
            Dynamic<JsonElement> var4 = new Dynamic(JsonOps.INSTANCE, Streams.parse(var2));
            if (!var4.get("DataVersion").asNumber().result().isPresent()) {
               var4 = var4.set("DataVersion", var4.createInt(1343));
            }

            var4 = this.dataFixer.update(DataFixTypes.ADVANCEMENTS.getType(), var4, var4.get("DataVersion").asInt(0), SharedConstants.getCurrentVersion().getWorldVersion());
            var4 = var4.remove("DataVersion");
            Map<ResourceLocation, AdvancementProgress> var5 = GSON.getAdapter(TYPE_TOKEN).fromJsonTree((JsonElement)var4.getValue());
            if (var5 == null) {
               throw new JsonParseException("Found null for advancements");
            }

            Stream<Map.Entry<ResourceLocation, AdvancementProgress>> var6 = var5.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue));

            for(Entry<ResourceLocation, AdvancementProgress> var8 : var6.collect(Collectors.toList())) {
               Advancement var9 = var1.getAdvancement(var8.getKey());
               if (var9 == null) {
                  LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", var8.getKey(), this.file);
               } else {
                  this.startProgress(var9, var8.getValue());
               }
            }
         } catch (JsonParseException var2_3) {
            LOGGER.error("Couldn't parse player advancements in {}", this.file, var2_3);
         } catch (IOException var2_1) {
            LOGGER.error("Couldn't access player advancements in {}", this.file, var2_1);
         }
      }

      this.checkForAutomaticTriggers(var1);
      this.ensureAllVisible();
      this.registerListeners(var1);
   }

   public void save() {
      Map<ResourceLocation, AdvancementProgress> var1 = Maps.newHashMap();

      for(Map.Entry<Advancement, AdvancementProgress> var3 : this.advancements.entrySet()) {
         AdvancementProgress var4 = (AdvancementProgress)var3.getValue();
         if (var4.hasProgress()) {
            var1.put(((Advancement)var3.getKey()).getId(), var4);
         }
      }

      if (this.file.getParentFile() != null) {
         this.file.getParentFile().mkdirs();
      }

      JsonElement var36 = GSON.toJsonTree(var1);
      var36.getAsJsonObject().addProperty("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());

      try (OutputStream var37 = new FileOutputStream(this.file)) {
         Writer var5 = new OutputStreamWriter(var37, Charsets.UTF_8.newEncoder());
         Throwable var6 = null;

         try {
            GSON.toJson(var36, var5);
         } catch (Throwable var31) {
            var6 = var31;
            throw var31;
         } finally {
            if (var5 != null) {
               if (var6 != null) {
                  try {
                     var5.close();
                  } catch (Throwable var30) {
                     var6.addSuppressed(var30);
                  }
               } else {
                  var5.close();
               }
            }

         }
      } catch (IOException var3_4) {
         LOGGER.error("Couldn't save player advancements to {}", this.file, var3_4);
      }
   }

   public boolean award(Advancement var1, String var2) {
      boolean var3 = false;
      AdvancementProgress var4 = this.getOrStartProgress(var1);
      boolean var5 = var4.isDone();
      if (var4.grantProgress(var2)) {
         this.unregisterListeners(var1);
         this.progressChanged.add(var1);
         var3 = true;
         if (!var5 && var4.isDone()) {
            var1.getRewards().grant(this.player);
            if (var1.getDisplay() != null && var1.getDisplay().shouldAnnounceChat() && this.player.level.getGameRules().getBoolean(GameRules.RULE_ANNOUNCE_ADVANCEMENTS)) {
               this.playerList.broadcastMessage(new TranslatableComponent("chat.type.advancement." + var1.getDisplay().getFrame().getName(), new Object[]{this.player.getDisplayName(), var1.getChatComponent()}), ChatType.SYSTEM, Util.NIL_UUID);
            }
         }
      }

      if (var4.isDone()) {
         this.ensureVisibility(var1);
      }

      return var3;
   }

   public boolean revoke(Advancement var1, String var2) {
      boolean var3 = false;
      AdvancementProgress var4 = this.getOrStartProgress(var1);
      if (var4.revokeProgress(var2)) {
         this.registerListeners(var1);
         this.progressChanged.add(var1);
         var3 = true;
      }

      if (!var4.hasProgress()) {
         this.ensureVisibility(var1);
      }

      return var3;
   }

   private void registerListeners(Advancement var1) {
      AdvancementProgress var2 = this.getOrStartProgress(var1);
      if (!var2.isDone()) {
         for(Map.Entry<String, Criterion> var4 : var1.getCriteria().entrySet()) {
            CriterionProgress var5 = var2.getCriterion(var4.getKey());
            if (var5 != null && !var5.isDone()) {
               CriterionTriggerInstance var6 = (var4.getValue()).getTrigger();
               if (var6 != null) {
                  CriterionTrigger<CriterionTriggerInstance> var7 = CriteriaTriggers.getCriterion(var6.getCriterion());
                  if (var7 != null) {
                     var7.addPlayerListener(this, new CriterionTrigger.Listener(var6, var1, (String)var4.getKey()));
                  }
               }
            }
         }
      }
   }

   private void unregisterListeners(Advancement var1) {
      AdvancementProgress var2 = this.getOrStartProgress(var1);

      for(Map.Entry<String, Criterion> var4 : var1.getCriteria().entrySet()) {
         CriterionProgress var5 = var2.getCriterion(var4.getKey());
         if (var5 != null && (var5.isDone() || var2.isDone())) {
            CriterionTriggerInstance var6 = (var4.getValue()).getTrigger();
            if (var6 != null) {
               CriterionTrigger<CriterionTriggerInstance> var7 = CriteriaTriggers.getCriterion(var6.getCriterion());
               if (var7 != null) {
                  var7.removePlayerListener(this, new CriterionTrigger.Listener(var6, var1, var4.getKey()));
               }
            }
         }
      }
   }

   public void flushDirty(ServerPlayer var1) {
      if (this.isFirstPacket || !this.visibilityChanged.isEmpty() || !this.progressChanged.isEmpty()) {
         Map<ResourceLocation, AdvancementProgress> var2 = Maps.newHashMap();
         Set<Advancement> var3 = Sets.newLinkedHashSet();
         Set<ResourceLocation> var4 = Sets.newLinkedHashSet();

         for(Advancement var6 : this.progressChanged) {
            if (this.visible.contains(var6)) {
               var2.put(var6.getId(), this.advancements.get(var6));
            }
         }

         for(Advancement var8 : this.visibilityChanged) {
            if (this.visible.contains(var8)) {
               var3.add(var8);
            } else {
               var4.add(var8.getId());
            }
         }

         if (this.isFirstPacket || !var2.isEmpty() || !var3.isEmpty() || !var4.isEmpty()) {
            var1.connection.send(new ClientboundUpdateAdvancementsPacket(this.isFirstPacket, var3, var4, var2));
            this.visibilityChanged.clear();
            this.progressChanged.clear();
         }
      }

      this.isFirstPacket = false;
   }

   public void setSelectedTab(@Nullable Advancement var1) {
      Advancement var2 = this.lastSelectedTab;
      if (var1 != null && var1.getParent() == null && var1.getDisplay() != null) {
         this.lastSelectedTab = var1;
      } else {
         this.lastSelectedTab = null;
      }

      if (var2 != this.lastSelectedTab) {
         this.player.connection.send(new ClientboundSelectAdvancementsTabPacket(this.lastSelectedTab == null ? null : this.lastSelectedTab.getId()));
      }
   }

   public AdvancementProgress getOrStartProgress(Advancement var1) {
      AdvancementProgress var2 = this.advancements.get(var1);
      if (var2 == null) {
         var2 = new AdvancementProgress();
         this.startProgress(var1, var2);
      }

      return var2;
   }

   private void startProgress(Advancement var1, AdvancementProgress var2) {
      var2.update(var1.getCriteria(), var1.getRequirements());
      this.advancements.put(var1, var2);
   }

   private void ensureVisibility(Advancement var1) {
      boolean var2 = this.shouldBeVisible(var1);
      boolean var3 = this.visible.contains(var1);
      if (var2 && !var3) {
         this.visible.add(var1);
         this.visibilityChanged.add(var1);
         if (this.advancements.containsKey(var1)) {
            this.progressChanged.add(var1);
         }
      } else if (!var2 && var3) {
         this.visible.remove(var1);
         this.visibilityChanged.add(var1);
      }

      if (var2 != var3 && var1.getParent() != null) {
         this.ensureVisibility(var1.getParent());
      }

      for(Advancement var5 : var1.getChildren()) {
         this.ensureVisibility(var5);
      }
   }

   private boolean shouldBeVisible(Advancement var1) {
      for(int var2 = 0; var1 != null && var2 <= 2; ++var2) {
         if (var2 == 0 && this.hasCompletedChildrenOrSelf(var1)) {
            return true;
         }

         if (var1.getDisplay() == null) {
            return false;
         }

         AdvancementProgress var3 = this.getOrStartProgress(var1);
         if (var3.isDone()) {
            return true;
         }

         if (var1.getDisplay().isHidden()) {
            return false;
         }

         var1 = var1.getParent();
      }

      return false;
   }

   private boolean hasCompletedChildrenOrSelf(Advancement var1) {
      AdvancementProgress var2 = this.getOrStartProgress(var1);
      if (var2.isDone()) {
         return true;
      } else {
         for(Advancement var4 : var1.getChildren()) {
            if (this.hasCompletedChildrenOrSelf(var4)) {
               return true;
            }
         }

         return false;
      }
   }
}
