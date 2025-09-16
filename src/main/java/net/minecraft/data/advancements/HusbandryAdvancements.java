package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.BeeNestDestroyedTrigger;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.BredAnimalsTrigger;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.FilledBucketTrigger;
import net.minecraft.advancements.critereon.FishingRodHookedTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnBlockTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlacedBlockTrigger;
import net.minecraft.advancements.critereon.TameAnimalTrigger;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/data/advancements/HusbandryAdvancements.class */
public class HusbandryAdvancements implements Consumer<Consumer<Advancement>> {
   private static final EntityType<?>[] BREEDABLE_ANIMALS = {EntityType.HORSE, EntityType.DONKEY, EntityType.MULE, EntityType.SHEEP, EntityType.COW, EntityType.MOOSHROOM, EntityType.PIG, EntityType.CHICKEN, EntityType.WOLF, EntityType.OCELOT, EntityType.RABBIT, EntityType.LLAMA, EntityType.CAT, EntityType.PANDA, EntityType.FOX, EntityType.BEE, EntityType.HOGLIN, EntityType.STRIDER};
   private static final Item[] FISH = {Items.COD, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.SALMON};
   private static final Item[] FISH_BUCKETS = {Items.COD_BUCKET, Items.TROPICAL_FISH_BUCKET, Items.PUFFERFISH_BUCKET, Items.SALMON_BUCKET};
   private static final Item[] EDIBLE_ITEMS = {Items.APPLE, Items.MUSHROOM_STEW, Items.BREAD, Items.PORKCHOP, Items.COOKED_PORKCHOP, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.COOKED_COD, Items.COOKED_SALMON, Items.COOKIE, Items.MELON_SLICE, Items.BEEF, Items.COOKED_BEEF, Items.CHICKEN, Items.COOKED_CHICKEN, Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.CARROT, Items.POTATO, Items.BAKED_POTATO, Items.POISONOUS_POTATO, Items.GOLDEN_CARROT, Items.PUMPKIN_PIE, Items.RABBIT, Items.COOKED_RABBIT, Items.RABBIT_STEW, Items.MUTTON, Items.COOKED_MUTTON, Items.CHORUS_FRUIT, Items.BEETROOT, Items.BEETROOT_SOUP, Items.DRIED_KELP, Items.SUSPICIOUS_STEW, Items.SWEET_BERRIES, Items.HONEY_BOTTLE};

   @Override // java.util.function.Consumer
   public void accept(Consumer<Advancement> consumer) {
      Advancement save = Advancement.Builder.advancement().display((ItemLike) Blocks.HAY_BLOCK, (Component) new TranslatableComponent("advancements.husbandry.root.title"), (Component) new TranslatableComponent("advancements.husbandry.root.description"), new ResourceLocation("textures/gui/advancements/backgrounds/husbandry.png"), FrameType.TASK, false, false, false).addCriterion("consumed_item", ConsumeItemTrigger.TriggerInstance.usedItem()).save(consumer, "husbandry/root");
      Advancement save2 = Advancement.Builder.advancement().parent(save).display((ItemLike) Items.WHEAT, (Component) new TranslatableComponent("advancements.husbandry.plant_seed.title"), (Component) new TranslatableComponent("advancements.husbandry.plant_seed.description"), (ResourceLocation) null, FrameType.TASK, true, true, false).requirements(RequirementsStrategy.OR).addCriterion("wheat", PlacedBlockTrigger.TriggerInstance.placedBlock(Blocks.WHEAT)).addCriterion("pumpkin_stem", PlacedBlockTrigger.TriggerInstance.placedBlock(Blocks.PUMPKIN_STEM)).addCriterion("melon_stem", PlacedBlockTrigger.TriggerInstance.placedBlock(Blocks.MELON_STEM)).addCriterion("beetroots", PlacedBlockTrigger.TriggerInstance.placedBlock(Blocks.BEETROOTS)).addCriterion("nether_wart", PlacedBlockTrigger.TriggerInstance.placedBlock(Blocks.NETHER_WART)).save(consumer, "husbandry/plant_seed");
      Advancement save3 = Advancement.Builder.advancement().parent(save).display((ItemLike) Items.WHEAT, (Component) new TranslatableComponent("advancements.husbandry.breed_an_animal.title"), (Component) new TranslatableComponent("advancements.husbandry.breed_an_animal.description"), (ResourceLocation) null, FrameType.TASK, true, true, false).requirements(RequirementsStrategy.OR).addCriterion("bred", BredAnimalsTrigger.TriggerInstance.bredAnimals()).save(consumer, "husbandry/breed_an_animal");
      addFood(Advancement.Builder.advancement()).parent(save2).display((ItemLike) Items.APPLE, (Component) new TranslatableComponent("advancements.husbandry.balanced_diet.title"), (Component) new TranslatableComponent("advancements.husbandry.balanced_diet.description"), (ResourceLocation) null, FrameType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).save(consumer, "husbandry/balanced_diet");
      Advancement.Builder.advancement().parent(save2).display((ItemLike) Items.NETHERITE_HOE, (Component) new TranslatableComponent("advancements.husbandry.netherite_hoe.title"), (Component) new TranslatableComponent("advancements.husbandry.netherite_hoe.description"), (ResourceLocation) null, FrameType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).addCriterion("netherite_hoe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HOE)).save(consumer, "husbandry/obtain_netherite_hoe");
      Advancement save4 = Advancement.Builder.advancement().parent(save).display((ItemLike) Items.LEAD, (Component) new TranslatableComponent("advancements.husbandry.tame_an_animal.title"), (Component) new TranslatableComponent("advancements.husbandry.tame_an_animal.description"), (ResourceLocation) null, FrameType.TASK, true, true, false).addCriterion("tamed_animal", TameAnimalTrigger.TriggerInstance.tamedAnimal()).save(consumer, "husbandry/tame_an_animal");
      addBreedable(Advancement.Builder.advancement()).parent(save3).display((ItemLike) Items.GOLDEN_CARROT, (Component) new TranslatableComponent("advancements.husbandry.breed_all_animals.title"), (Component) new TranslatableComponent("advancements.husbandry.breed_all_animals.description"), (ResourceLocation) null, FrameType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).save(consumer, "husbandry/bred_all_animals");
      addFishBuckets(Advancement.Builder.advancement()).parent(addFish(Advancement.Builder.advancement()).parent(save).requirements(RequirementsStrategy.OR).display((ItemLike) Items.FISHING_ROD, (Component) new TranslatableComponent("advancements.husbandry.fishy_business.title"), (Component) new TranslatableComponent("advancements.husbandry.fishy_business.description"), (ResourceLocation) null, FrameType.TASK, true, true, false).save(consumer, "husbandry/fishy_business")).requirements(RequirementsStrategy.OR).display((ItemLike) Items.PUFFERFISH_BUCKET, (Component) new TranslatableComponent("advancements.husbandry.tactical_fishing.title"), (Component) new TranslatableComponent("advancements.husbandry.tactical_fishing.description"), (ResourceLocation) null, FrameType.TASK, true, true, false).save(consumer, "husbandry/tactical_fishing");
      addCatVariants(Advancement.Builder.advancement()).parent(save4).display((ItemLike) Items.COD, (Component) new TranslatableComponent("advancements.husbandry.complete_catalogue.title"), (Component) new TranslatableComponent("advancements.husbandry.complete_catalogue.description"), (ResourceLocation) null, FrameType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).save(consumer, "husbandry/complete_catalogue");
      Advancement.Builder.advancement().parent(save).addCriterion("safely_harvest_honey", ItemUsedOnBlockTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BlockTags.BEEHIVES).build()).setSmokey(true), ItemPredicate.Builder.item().of(Items.GLASS_BOTTLE))).display((ItemLike) Items.HONEY_BOTTLE, (Component) new TranslatableComponent("advancements.husbandry.safely_harvest_honey.title"), (Component) new TranslatableComponent("advancements.husbandry.safely_harvest_honey.description"), (ResourceLocation) null, FrameType.TASK, true, true, false).save(consumer, "husbandry/safely_harvest_honey");
      Advancement.Builder.advancement().parent(save).addCriterion("silk_touch_nest", BeeNestDestroyedTrigger.TriggerInstance.destroyedBeeNest(Blocks.BEE_NEST, ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))), MinMaxBounds.Ints.exactly(3))).display((ItemLike) Blocks.BEE_NEST, (Component) new TranslatableComponent("advancements.husbandry.silk_touch_nest.title"), (Component) new TranslatableComponent("advancements.husbandry.silk_touch_nest.description"), (ResourceLocation) null, FrameType.TASK, true, true, false).save(consumer, "husbandry/silk_touch_nest");
   }

   private Advancement.Builder addFood(Advancement.Builder builder) {
      for (Item item : EDIBLE_ITEMS) {
         builder.addCriterion(Registry.ITEM.getKey(item).getPath(), ConsumeItemTrigger.TriggerInstance.usedItem(item));
      }
      return builder;
   }

   private Advancement.Builder addBreedable(Advancement.Builder builder) {
      for (EntityType<?> entityType : BREEDABLE_ANIMALS) {
         builder.addCriterion(EntityType.getKey(entityType).toString(), BredAnimalsTrigger.TriggerInstance.bredAnimals(EntityPredicate.Builder.entity().of(entityType)));
      }
      builder.addCriterion(EntityType.getKey(EntityType.TURTLE).toString(), BredAnimalsTrigger.TriggerInstance.bredAnimals(EntityPredicate.Builder.entity().of(EntityType.TURTLE).build(), EntityPredicate.Builder.entity().of(EntityType.TURTLE).build(), EntityPredicate.ANY));
      return builder;
   }

   private Advancement.Builder addFishBuckets(Advancement.Builder builder) {
      for (Item item : FISH_BUCKETS) {
         builder.addCriterion(Registry.ITEM.getKey(item).getPath(), FilledBucketTrigger.TriggerInstance.filledBucket(ItemPredicate.Builder.item().of(item).build()));
      }
      return builder;
   }

   private Advancement.Builder addFish(Advancement.Builder builder) {
      for (Item item : FISH) {
         builder.addCriterion(Registry.ITEM.getKey(item).getPath(), FishingRodHookedTrigger.TriggerInstance.fishedItem(ItemPredicate.ANY, EntityPredicate.ANY, ItemPredicate.Builder.item().of(item).build()));
      }
      return builder;
   }

   private Advancement.Builder addCatVariants(Advancement.Builder builder) {
      Cat.TEXTURE_BY_TYPE.forEach((num, resourceLocation) -> {
         builder.addCriterion(resourceLocation.getPath(), TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(resourceLocation).build()));
      });
      return builder;
   }
}
