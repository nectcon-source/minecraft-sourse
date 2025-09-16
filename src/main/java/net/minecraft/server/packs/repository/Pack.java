package net.minecraft.server.packs.repository;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Pack implements AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final PackMetadataSection BROKEN_ASSETS_FALLBACK = new PackMetadataSection(
      new TranslatableComponent("resourcePack.broken_assets").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC}),
      SharedConstants.getCurrentVersion().getPackVersion()
   );
   private final String id;
   private final Supplier<PackResources> supplier;
   private final Component title;
   private final Component description;
   private final PackCompatibility compatibility;
   private final Pack.Position defaultPosition;
   private final boolean required;
   private final boolean fixedPosition;
   private final PackSource packSource;

   @Nullable
   public static Pack create(String var0, boolean var1, Supplier<PackResources> var2, Pack.PackConstructor var3, Pack.Position var4, PackSource var5) {
      try (PackResources var6 = var2.get()) {
         PackMetadataSection var8 = var6.getMetadataSection(PackMetadataSection.SERIALIZER);
         if (var1 && var8 == null) {
            LOGGER.error("Broken/missing pack.mcmeta detected, fudging it into existance. Please check that your launcher has downloaded all assets for the game correctly!");
            var8 = BROKEN_ASSETS_FALLBACK;
         }

         if (var8 != null) {
            Pack var9 = var3.create(var0, var1, var2, var6, var8, var4, var5);
            return var9;
         }

         LOGGER.warn("Couldn't find pack meta for pack {}", var0);
      } catch (IOException var6_1) {
         LOGGER.warn("Couldn't get pack info for: {}", var6_1.toString());
      }

      return null;
   }

   public Pack(
      String var1,
      boolean var2,
      Supplier<PackResources> var3,
      Component var4,
      Component var5,
      PackCompatibility var6,
      Pack.Position var7,
      boolean var8,
      PackSource var9
   ) {
      this.id = var1;
      this.supplier = var3;
      this.title = var4;
      this.description = var5;
      this.compatibility = var6;
      this.required = var2;
      this.defaultPosition = var7;
      this.fixedPosition = var8;
      this.packSource = var9;
   }

   public Pack(String var1, boolean var2, Supplier<PackResources> var3, PackResources var4, PackMetadataSection var5, Position var6, PackSource var7) {
      this(var1, var2, var3, new TextComponent(var4.getName()), var5.getDescription(), PackCompatibility.forFormat(var5.getPackFormat()), var6, false, var7);
   }

   public Component getTitle() {
      return this.title;
   }

   public Component getDescription() {
      return this.description;
   }

   public Component getChatLink(boolean var1) {
      return ComponentUtils.wrapInSquareBrackets(this.packSource.decorate(new TextComponent(this.id)))
         .withStyle(
            var2 -> var2.withColor(var1 ? ChatFormatting.GREEN : ChatFormatting.RED)
                  .withInsertion(StringArgumentType.escapeIfRequired(this.id))
                  .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("").append(this.title).append("\n").append(this.description)))
         );
   }

   public PackCompatibility getCompatibility() {
      return this.compatibility;
   }

   public PackResources open() {
      return this.supplier.get();
   }

   public String getId() {
      return this.id;
   }

   public boolean isRequired() {
      return this.required;
   }

   public boolean isFixedPosition() {
      return this.fixedPosition;
   }

   public Pack.Position getDefaultPosition() {
      return this.defaultPosition;
   }

   public PackSource getPackSource() {
      return this.packSource;
   }

   @Override
   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof Pack)) {
         return false;
      } else {
         Pack var2 = (Pack)var1;
         return this.id.equals(var2.id);
      }
   }

   @Override
   public int hashCode() {
      return this.id.hashCode();
   }

   @Override
   public void close() {
   }

   @FunctionalInterface
   public interface PackConstructor {
      @Nullable
      Pack create(String var1, boolean var2, Supplier<PackResources> var3, PackResources var4, PackMetadataSection var5, Pack.Position var6, PackSource var7);
   }

   public static enum Position {
      TOP,
      BOTTOM;

      public <T> int insert(List<T> var1, T var2, Function<T, Pack> var3, boolean var4) {
         Position var5 = var4 ? this.opposite() : this;
         if (var5 == BOTTOM) {
            int var8;
            for(var8 = 0; var8 < var1.size(); ++var8) {
               Pack var9 = var3.apply(var1.get(var8));
               if (!var9.isFixedPosition() || var9.getDefaultPosition() != this) {
                  break;
               }
            }

            var1.add(var8, var2);
            return var8;
         } else {
            int var6;
            for(var6 = var1.size() - 1; var6 >= 0; --var6) {
               Pack var7 = var3.apply(var1.get(var6));
               if (!var7.isFixedPosition() || var7.getDefaultPosition() != this) {
                  break;
               }
            }

            var1.add(var6 + 1, var2);
            return var6 + 1;
         }
      }

      public Pack.Position opposite() {
         return this == TOP ? BOTTOM : TOP;
      }
   }
}
