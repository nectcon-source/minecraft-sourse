package net.minecraft.data.models.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/* loaded from: client_deobf_norm.jar:net/minecraft/data/models/model/TextureMapping.class */
public class TextureMapping {
   private final Map<TextureSlot, ResourceLocation> slots = Maps.newHashMap();
   private final Set<TextureSlot> forcedSlots = Sets.newHashSet();

   public TextureMapping put(TextureSlot textureSlot, ResourceLocation resourceLocation) {
      this.slots.put(textureSlot, resourceLocation);
      return this;
   }

   public Stream<TextureSlot> getForced() {
      return this.forcedSlots.stream();
   }

//   public TextureMapping copyForced(TextureSlot textureSlot, TextureSlot textureSlot2) {
//      this.slots.put(textureSlot2, this.slots.get(textureSlot));
//      this.forcedSlots.add(textureSlot2);
//      return this;
//   }
public TextureMapping copyForced(TextureSlot from, TextureSlot to) {
    if (from == null || to == null) {
        throw new IllegalArgumentException("TextureSlot arguments cannot be null");
    }

    ResourceLocation resourceLocation = this.slots.get(from);
    if (resourceLocation == null) {
        throw new IllegalStateException("Source slot " + from + " is not mapped and cannot be copied");
    }

    this.slots.put(to, resourceLocation);
    this.forcedSlots.add(to);
    return this;
}


    //   public ResourceLocation get(TextureSlot textureSlot) {
//      TextureSlot textureSlot2 = textureSlot;
//      while (true) {
//         TextureSlot textureSlot3 = textureSlot2;
//         if (textureSlot3 != null) {
//            ResourceLocation resourceLocation = this.slots.get(textureSlot3);
//            if (resourceLocation != null) {
//               return resourceLocation;
//            }
//            textureSlot2 = textureSlot3.getParent();
//         } else {
//            throw new IllegalStateException("Can't find texture for slot " + textureSlot);
//         }
//      }
//   }
public ResourceLocation get(TextureSlot textureSlot) {
    // Проверяем сразу на null
    if (textureSlot == null) {
        throw new IllegalArgumentException("TextureSlot cannot be null");
    }

    // Чтобы избежать бесконечного цикла, запоминаем посещённые слоты
    Set<TextureSlot> visited = Sets.newHashSet();

    TextureSlot current = textureSlot;
    while (current != null) {
        if (!visited.add(current)) {
            // Если мы сюда попали → цикл
            throw new IllegalStateException("Detected cycle in TextureSlot parent chain: " + current);
        }

        ResourceLocation resourceLocation = this.slots.get(current);
        if (resourceLocation != null) {
            return resourceLocation;
        }

        current = current.getParent();
    }

    throw new IllegalStateException("Can't find texture for slot " + textureSlot);
}


    public TextureMapping copyAndUpdate(TextureSlot textureSlot, ResourceLocation resourceLocation) {
      TextureMapping textureMapping = new TextureMapping();
      textureMapping.slots.putAll(this.slots);
      textureMapping.forcedSlots.addAll(this.forcedSlots);
      textureMapping.put(textureSlot, resourceLocation);
      return textureMapping;
   }

   public static TextureMapping cube(Block block) {
      return cube(getBlockTexture(block));
   }

   public static TextureMapping defaultTexture(Block block) {
      return defaultTexture(getBlockTexture(block));
   }

   public static TextureMapping defaultTexture(ResourceLocation resourceLocation) {
      return new TextureMapping().put(TextureSlot.TEXTURE, resourceLocation);
   }

   public static TextureMapping cube(ResourceLocation resourceLocation) {
      return new TextureMapping().put(TextureSlot.ALL, resourceLocation);
   }

   public static TextureMapping cross(Block block) {
      return singleSlot(TextureSlot.CROSS, getBlockTexture(block));
   }

   public static TextureMapping cross(ResourceLocation resourceLocation) {
      return singleSlot(TextureSlot.CROSS, resourceLocation);
   }

   public static TextureMapping plant(Block block) {
      return singleSlot(TextureSlot.PLANT, getBlockTexture(block));
   }

   public static TextureMapping plant(ResourceLocation resourceLocation) {
      return singleSlot(TextureSlot.PLANT, resourceLocation);
   }

   public static TextureMapping rail(Block block) {
      return singleSlot(TextureSlot.RAIL, getBlockTexture(block));
   }

   public static TextureMapping rail(ResourceLocation resourceLocation) {
      return singleSlot(TextureSlot.RAIL, resourceLocation);
   }

   public static TextureMapping wool(Block block) {
      return singleSlot(TextureSlot.WOOL, getBlockTexture(block));
   }

   public static TextureMapping stem(Block block) {
      return singleSlot(TextureSlot.STEM, getBlockTexture(block));
   }

   public static TextureMapping attachedStem(Block block, Block block2) {
      return new TextureMapping().put(TextureSlot.STEM, getBlockTexture(block)).put(TextureSlot.UPPER_STEM, getBlockTexture(block2));
   }

   public static TextureMapping pattern(Block block) {
      return singleSlot(TextureSlot.PATTERN, getBlockTexture(block));
   }

   public static TextureMapping fan(Block block) {
      return singleSlot(TextureSlot.FAN, getBlockTexture(block));
   }

   public static TextureMapping crop(ResourceLocation resourceLocation) {
      return singleSlot(TextureSlot.CROP, resourceLocation);
   }

   public static TextureMapping pane(Block block, Block block2) {
      return new TextureMapping().put(TextureSlot.PANE, getBlockTexture(block)).put(TextureSlot.EDGE, getBlockTexture(block2, "_top"));
   }

   public static TextureMapping singleSlot(TextureSlot textureSlot, ResourceLocation resourceLocation) {
      return new TextureMapping().put(textureSlot, resourceLocation);
   }

   public static TextureMapping column(Block block) {
      return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.END, getBlockTexture(block, "_top"));
   }

   public static TextureMapping cubeTop(Block block) {
      return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.TOP, getBlockTexture(block, "_top"));
   }

   public static TextureMapping logColumn(Block block) {
      return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(block)).put(TextureSlot.END, getBlockTexture(block, "_top"));
   }

   public static TextureMapping column(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
      return new TextureMapping().put(TextureSlot.SIDE, resourceLocation).put(TextureSlot.END, resourceLocation2);
   }

   public static TextureMapping cubeBottomTop(Block block) {
      return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.TOP, getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
   }

   public static TextureMapping cubeBottomTopWithWall(Block block) {
      ResourceLocation blockTexture = getBlockTexture(block);
      return new TextureMapping().put(TextureSlot.WALL, blockTexture).put(TextureSlot.SIDE, blockTexture).put(TextureSlot.TOP, getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
   }

   public static TextureMapping columnWithWall(Block block) {
      ResourceLocation blockTexture = getBlockTexture(block);
      return new TextureMapping().put(TextureSlot.WALL, blockTexture).put(TextureSlot.SIDE, blockTexture).put(TextureSlot.END, getBlockTexture(block, "_top"));
   }

   public static TextureMapping door(Block block) {
      return new TextureMapping().put(TextureSlot.TOP, getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
   }

   public static TextureMapping particle(Block block) {
      return new TextureMapping().put(TextureSlot.PARTICLE, getBlockTexture(block));
   }

   public static TextureMapping particle(ResourceLocation resourceLocation) {
      return new TextureMapping().put(TextureSlot.PARTICLE, resourceLocation);
   }

   public static TextureMapping fire0(Block block) {
      return new TextureMapping().put(TextureSlot.FIRE, getBlockTexture(block, "_0"));
   }

   public static TextureMapping fire1(Block block) {
      return new TextureMapping().put(TextureSlot.FIRE, getBlockTexture(block, "_1"));
   }

   public static TextureMapping lantern(Block block) {
      return new TextureMapping().put(TextureSlot.LANTERN, getBlockTexture(block));
   }

   public static TextureMapping torch(Block block) {
      return new TextureMapping().put(TextureSlot.TORCH, getBlockTexture(block));
   }

   public static TextureMapping torch(ResourceLocation resourceLocation) {
      return new TextureMapping().put(TextureSlot.TORCH, resourceLocation);
   }

   public static TextureMapping particleFromItem(Item item) {
      return new TextureMapping().put(TextureSlot.PARTICLE, getItemTexture(item));
   }

   public static TextureMapping commandBlock(Block block) {
      return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.FRONT, getBlockTexture(block, "_front")).put(TextureSlot.BACK, getBlockTexture(block, "_back"));
   }

   public static TextureMapping orientableCube(Block block) {
      return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.FRONT, getBlockTexture(block, "_front")).put(TextureSlot.TOP, getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
   }

   public static TextureMapping orientableCubeOnlyTop(Block block) {
      return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.FRONT, getBlockTexture(block, "_front")).put(TextureSlot.TOP, getBlockTexture(block, "_top"));
   }

   public static TextureMapping orientableCubeSameEnds(Block block) {
      return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.FRONT, getBlockTexture(block, "_front")).put(TextureSlot.END, getBlockTexture(block, "_end"));
   }

   public static TextureMapping top(Block block) {
      return new TextureMapping().put(TextureSlot.TOP, getBlockTexture(block, "_top"));
   }

   public static TextureMapping craftingTable(Block block, Block block2) {
      return new TextureMapping().put(TextureSlot.PARTICLE, getBlockTexture(block, "_front")).put(TextureSlot.DOWN, getBlockTexture(block2)).put(TextureSlot.UP, getBlockTexture(block, "_top")).put(TextureSlot.NORTH, getBlockTexture(block, "_front")).put(TextureSlot.EAST, getBlockTexture(block, "_side")).put(TextureSlot.SOUTH, getBlockTexture(block, "_side")).put(TextureSlot.WEST, getBlockTexture(block, "_front"));
   }

   public static TextureMapping fletchingTable(Block block, Block block2) {
      return new TextureMapping().put(TextureSlot.PARTICLE, getBlockTexture(block, "_front")).put(TextureSlot.DOWN, getBlockTexture(block2)).put(TextureSlot.UP, getBlockTexture(block, "_top")).put(TextureSlot.NORTH, getBlockTexture(block, "_front")).put(TextureSlot.SOUTH, getBlockTexture(block, "_front")).put(TextureSlot.EAST, getBlockTexture(block, "_side")).put(TextureSlot.WEST, getBlockTexture(block, "_side"));
   }

   public static TextureMapping campfire(Block block) {
      return new TextureMapping().put(TextureSlot.LIT_LOG, getBlockTexture(block, "_log_lit")).put(TextureSlot.FIRE, getBlockTexture(block, "_fire"));
   }

   public static TextureMapping layer0(Item item) {
      return new TextureMapping().put(TextureSlot.LAYER0, getItemTexture(item));
   }

   public static TextureMapping layer0(Block block) {
      return new TextureMapping().put(TextureSlot.LAYER0, getBlockTexture(block));
   }

   public static TextureMapping layer0(ResourceLocation resourceLocation) {
      return new TextureMapping().put(TextureSlot.LAYER0, resourceLocation);
   }

   public static ResourceLocation getBlockTexture(Block block) {
      ResourceLocation key = Registry.BLOCK.getKey(block);
      return new ResourceLocation(key.getNamespace(), "block/" + key.getPath());
   }

   public static ResourceLocation getBlockTexture(Block block, String str) {
      ResourceLocation key = Registry.BLOCK.getKey(block);
      return new ResourceLocation(key.getNamespace(), "block/" + key.getPath() + str);
   }

   public static ResourceLocation getItemTexture(Item item) {
      ResourceLocation key = Registry.ITEM.getKey(item);
      return new ResourceLocation(key.getNamespace(), "item/" + key.getPath());
   }

   public static ResourceLocation getItemTexture(Item item, String str) {
      ResourceLocation key = Registry.ITEM.getKey(item);
      return new ResourceLocation(key.getNamespace(), "item/" + key.getPath() + str);
   }
}
