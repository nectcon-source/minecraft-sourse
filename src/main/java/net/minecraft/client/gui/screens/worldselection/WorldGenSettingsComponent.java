package net.minecraft.client.gui.screens.worldselection;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class WorldGenSettingsComponent implements TickableWidget, Widget {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Component CUSTOM_WORLD_DESCRIPTION = new TranslatableComponent("generator.custom");
   private static final Component AMPLIFIED_HELP_TEXT = new TranslatableComponent("generator.amplified.info");
   private static final Component MAP_FEATURES_INFO = new TranslatableComponent("selectWorld.mapFeatures.info");
   private MultiLineLabel amplifiedWorldInfo = MultiLineLabel.EMPTY;
   private Font font;
   private int width;
   private EditBox seedEdit;
   private Button featuresButton;
   public Button bonusItemsButton;
   private Button typeButton;
   private Button customizeTypeButton;
   private Button importSettingsButton;
   private RegistryAccess.RegistryHolder registryHolder;
   private WorldGenSettings settings;
   private Optional<WorldPreset> preset;
   private OptionalLong seed;

   public WorldGenSettingsComponent(RegistryAccess.RegistryHolder var1, WorldGenSettings var2, Optional<WorldPreset> var3, OptionalLong var4) {
      this.registryHolder = var1;
      this.settings = var2;
      this.preset = var3;
      this.seed = var4;
   }

   public void init(final CreateWorldScreen var1, Minecraft var2, Font var3) {
      this.font = var3;
      this.width = var1.width;
      this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterSeed"));
      this.seedEdit.setValue(toString(this.seed));
      this.seedEdit.setResponder(var1x -> this.seed = this.parseSeed());
      var1.addWidget(this.seedEdit);
      int var4 = this.width / 2 - 155;
      int var5 = this.width / 2 + 5;
      this.featuresButton = var1.addButton(new Button(var4, 100, 150, 20, new TranslatableComponent("selectWorld.mapFeatures"), var1x -> {
         this.settings = this.settings.withFeaturesToggled();
         var1x.queueNarration(250);
      }) {
         @Override
         public Component getMessage() {
            return CommonComponents.optionStatus(super.getMessage(), WorldGenSettingsComponent.this.settings.generateFeatures());
         }

         @Override
         protected MutableComponent createNarrationMessage() {
            return super.createNarrationMessage().append(". ").append(new TranslatableComponent("selectWorld.mapFeatures.info"));
         }
      });
      this.featuresButton.visible = false;
      this.typeButton = var1.addButton(
         new Button(var5, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), var2x -> {
            while(this.preset.isPresent()) {
               int var3x = WorldPreset.PRESETS.indexOf(this.preset.get()) + 1;
               if (var3x >= WorldPreset.PRESETS.size()) {
                  var3x = 0;
               }
   
               WorldPreset var4x = WorldPreset.PRESETS.get(var3x);
               this.preset = Optional.of(var4x);
               this.settings = var4x.create(this.registryHolder, this.settings.seed(), this.settings.generateFeatures(), this.settings.generateBonusChest());
               if (!this.settings.isDebug() || Screen.hasShiftDown()) {
                  break;
               }
            }
   
            var1.updateDisplayOptions();
            var2x.queueNarration(250);
         }) {
            @Override
            public Component getMessage() {
               return super.getMessage()
                  .copy()
                  .append(" ")
                  .append(WorldGenSettingsComponent.this.preset.map(WorldPreset::description).orElse(WorldGenSettingsComponent.CUSTOM_WORLD_DESCRIPTION));
            }
   
            @Override
            protected MutableComponent createNarrationMessage() {
               return Objects.equals(WorldGenSettingsComponent.this.preset, Optional.of(WorldPreset.AMPLIFIED))
                  ? super.createNarrationMessage().append(". ").append(WorldGenSettingsComponent.AMPLIFIED_HELP_TEXT)
                  : super.createNarrationMessage();
            }
         }
      );
      this.typeButton.visible = false;
      this.typeButton.active = this.preset.isPresent();
      this.customizeTypeButton = (Button)var1.addButton(new Button(var5, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), (var3x) -> {
         WorldPreset.PresetEditor var4__ = (WorldPreset.PresetEditor)WorldPreset.EDITORS.get(this.preset);
         if (var4__ != null) {
            var2.setScreen(var4__.createEditScreen(var1, this.settings));
         }

      }));
      this.customizeTypeButton.visible = false;
      this.bonusItemsButton = var1.addButton(new Button(var4, 151, 150, 20, new TranslatableComponent("selectWorld.bonusItems"), var1x -> {
         this.settings = this.settings.withBonusChestToggled();
         var1x.queueNarration(250);
      }) {
         @Override
         public Component getMessage() {
            return CommonComponents.optionStatus(super.getMessage(), WorldGenSettingsComponent.this.settings.generateBonusChest() && !var1.hardCore);
         }
      });
      this.bonusItemsButton.visible = false;
      this.importSettingsButton = var1.addButton(
         new Button(var4,185, 150, 20,
            new TranslatableComponent("selectWorld.import_worldgen_settings"),
            var3x -> {
               TranslatableComponent var4x = new TranslatableComponent("selectWorld.import_worldgen_settings.select_file");
               String var5x = TinyFileDialogs.tinyfd_openFileDialog(var4x.getString(), null, null, null, false);
               if (var5x != null) {
                  RegistryAccess.RegistryHolder var6xx = RegistryAccess.builtin();
                  PackRepository var7xxx = new PackRepository(
                     new ServerPacksSource(), new FolderRepositorySource(var1.getTempDataPackDir().toFile(), PackSource.WORLD)
                  );

                  ServerResources var8;
                  try {
                     MinecraftServer.configurePackRepository(var7xxx, var1.dataPacks, false);
                     CompletableFuture<ServerResources> var9xxxx = ServerResources.loadResources(
                             var7xxx.openAllSelected(), Commands.CommandSelection.INTEGRATED, 2, Util.backgroundExecutor(), var2
                     );
                     var2.managedBlock(var9xxxx::isDone);
                     var8 = var9xxxx.get();
                  } catch (ExecutionException | InterruptedException var25) {
                     LOGGER.error("Error loading data packs when importing world settings", var25);
                     Component var10xxxxx = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
                     Component var11xxxxxx = new TextComponent(var25.getMessage());
                     var2.getToasts().addToast(SystemToast.multiline(var2, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, var10xxxxx, var11xxxxxx));
                     var7xxx.close();
                     return;
                  }

                  RegistryReadOps<JsonElement> var28xxxx = RegistryReadOps.create(JsonOps.INSTANCE, var8.getResourceManager(), var6xx);
                  JsonParser var29xxxxx = new JsonParser();

                  DataResult<WorldGenSettings> var30;
                  try (BufferedReader var12xxxxxx = Files.newBufferedReader(Paths.get(var5x))) {
                     JsonElement var14xxxxxxx = var29xxxxx.parse(var12xxxxxx);
                     var30 = WorldGenSettings.CODEC.parse(var28xxxx, var14xxxxxxx);
                  } catch (JsonIOException | JsonSyntaxException | IOException var27) {
                     var30 = DataResult.error("Failed to parse file: " + var27.getMessage());
                  }

                  if (var30.error().isPresent()) {
                     Component var31 = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
                     String var33 = ((DataResult.PartialResult)var30.error().get()).message();
                     LOGGER.error("Error parsing world settings: {}", var33);
                     Component var34 = new TextComponent(var33);
                     var2.getToasts().addToast(SystemToast.multiline(var2, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, var31, var34));
                  }

                  var8.close();
                  Lifecycle var32xxxxxx = var30.lifecycle();
                  var30.resultOrPartial(LOGGER::error)
                     .ifPresent(
                        var5xx -> {
                           BooleanConsumer var6x = var5xxx -> {
                              var2.setScreen(var1);
                              if (var5xxx) {
                                 this.importSettings(var6xx, var5xx);
                              }
                           };
                           if (var32xxxxxx == Lifecycle.stable()) {
                              this.importSettings(var6xx, var5xx);
                           } else if (var32xxxxxx == Lifecycle.experimental()) {
                              var2.setScreen(
                                 new ConfirmScreen(
                                    var6x,
                                    new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.title"),
                                    new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.question")
                                 )
                              );
                           } else {
                              var2.setScreen(
                                 new ConfirmScreen(
                                    var6x,
                                    new TranslatableComponent("selectWorld.import_worldgen_settings.deprecated.title"),
                                    new TranslatableComponent("selectWorld.import_worldgen_settings.deprecated.question")
                                 )
                              );
                           }
                        }
                     );
               }
            }
         )
      );
      this.importSettingsButton.visible = false;
      this.amplifiedWorldInfo = MultiLineLabel.create(var3, AMPLIFIED_HELP_TEXT, this.typeButton.getWidth());
   }

   private void importSettings(RegistryAccess.RegistryHolder var1, WorldGenSettings var2) {
      this.registryHolder = var1;
      this.settings = var2;
      this.preset = WorldPreset.of(var2);
      this.seed = OptionalLong.of(var2.seed());
      this.seedEdit.setValue(toString(this.seed));
      this.typeButton.active = this.preset.isPresent();
   }

   @Override
   public void tick() {
      this.seedEdit.tick();
   }

   @Override
   public void render(PoseStack var1, int var2, int var3, float var4) {
      if (this.featuresButton.visible) {
         this.font.drawShadow(var1, MAP_FEATURES_INFO, (float)(this.width / 2 - 150), 122.0F, -6250336);
      }

      this.seedEdit.render(var1, var2, var3, var4);
      if(this.preset.equals(Optional.of(WorldPreset.AMPLIFIED))) {
         this.amplifiedWorldInfo.renderLeftAligned(var1, this.typeButton.x + 2, this.typeButton.y + 22, 9, 10526880);
      }
   }

   protected void updateSettings(WorldGenSettings var1) {
      this.settings = var1;
   }

   private static String toString(OptionalLong var0) {
      return var0.isPresent() ? Long.toString(var0.getAsLong()) : "";
   }

   private static OptionalLong parseLong(String var0) {
      try {
         return OptionalLong.of(Long.parseLong(var0));
      } catch (NumberFormatException var2) {
         return OptionalLong.empty();
      }
   }

   public WorldGenSettings makeSettings(boolean var1) {
      OptionalLong var2 = this.parseSeed();
      return this.settings.withSeed(var1, var2);
   }

   private OptionalLong parseSeed() {
      String var1 = this.seedEdit.getValue();
      OptionalLong var2;
      if (StringUtils.isEmpty(var1)) {
         var2 = OptionalLong.empty();
      } else {
         OptionalLong var3 = parseLong(var1);
         if (var3.isPresent() && var3.getAsLong() != 0L) {
            var2 = var3;
         } else {
            var2 = OptionalLong.of((long)var1.hashCode());
         }
      }

      return var2;
   }

   public boolean isDebug() {
      return this.settings.isDebug();
   }

   public void setDisplayOptions(boolean var1) {
      this.typeButton.visible = var1;
      if (this.settings.isDebug()) {
         this.featuresButton.visible = false;
         this.bonusItemsButton.visible = false;
         this.customizeTypeButton.visible = false;
         this.importSettingsButton.visible = false;
      } else {
         this.featuresButton.visible = var1;
         this.bonusItemsButton.visible = var1;
         this.customizeTypeButton.visible = var1 && WorldPreset.EDITORS.containsKey(this.preset);
         this.importSettingsButton.visible = var1;
      }

      this.seedEdit.setVisible(var1);
   }

   public RegistryAccess.RegistryHolder registryHolder() {
      return this.registryHolder;
   }

   void updateDataPacks(ServerResources var1) {
      RegistryAccess.RegistryHolder var2 = RegistryAccess.builtin();
      RegistryWriteOps<JsonElement> var3x = RegistryWriteOps.create(JsonOps.INSTANCE, this.registryHolder);
      RegistryReadOps<JsonElement> var4xx = RegistryReadOps.create(JsonOps.INSTANCE, var1.getResourceManager(), var2);
      DataResult<WorldGenSettings> var5xxx = WorldGenSettings.CODEC.encodeStart(var3x, this.settings).flatMap(var1x -> WorldGenSettings.CODEC.parse(var4xx, var1x));
      var5xxx.resultOrPartial(Util.prefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error)).ifPresent(var2x -> {
         this.settings = var2x;
         this.registryHolder = var2;
      });
   }
}
