package net.minecraft.data.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PotatoBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.IntLimiter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.RandomIntGenerator;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

/* loaded from: client_deobf_norm.jar:net/minecraft/data/loot/BlockLoot.class */
public class BlockLoot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
    private static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));
    private static final LootItemCondition.Builder HAS_NO_SILK_TOUCH = HAS_SILK_TOUCH.invert();
    private static final LootItemCondition.Builder HAS_SHEARS = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
    private static final LootItemCondition.Builder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
    private static final LootItemCondition.Builder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();
    private static final Set<Item> EXPLOSION_RESISTANT =  Stream.of( new Block[]{Blocks.DRAGON_EGG, Blocks.BEACON, Blocks.CONDUIT, Blocks.SKELETON_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.PLAYER_HEAD, Blocks.ZOMBIE_HEAD, Blocks.CREEPER_HEAD, Blocks.DRAGON_HEAD, Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX}).map((v0) -> {
        return v0.asItem();
    }).collect(ImmutableSet.toImmutableSet());
    private static final float[] NORMAL_LEAVES_SAPLING_CHANCES = {0.05f, 0.0625f, 0.083333336f, 0.1f};
    private static final float[] JUNGLE_LEAVES_SAPLING_CHANGES = {0.025f, 0.027777778f, 0.03125f, 0.041666668f, 0.1f};
    private final Map<ResourceLocation, LootTable.Builder> map = Maps.newHashMap();

    private static <T> T applyExplosionDecay(ItemLike itemLike, FunctionUserBuilder<T> functionUserBuilder) {
        if (!EXPLOSION_RESISTANT.contains(itemLike.asItem())) {
            return functionUserBuilder.apply(ApplyExplosionDecay.explosionDecay());
        }
        return functionUserBuilder.unwrap();
    }

    private static <T> T applyExplosionCondition(ItemLike itemLike, ConditionUserBuilder<T> conditionUserBuilder) {
        if (!EXPLOSION_RESISTANT.contains(itemLike.asItem())) {
            return conditionUserBuilder.when(ExplosionCondition.survivesExplosion());
        }
        return conditionUserBuilder.unwrap();
    }

    private static LootTable.Builder createSingleItemTable(ItemLike itemLike) {
        return LootTable.lootTable().withPool((LootPool.Builder) applyExplosionCondition(itemLike, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(itemLike))));
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static LootTable.Builder createSelfDropDispatchTable(Block block, LootItemCondition.Builder builder, LootPoolEntryContainer.Builder<?> builder2) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(((LootPoolSingletonContainer.Builder) LootItem.lootTableItem(block).when(builder)).otherwise(builder2)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LootTable.Builder createSilkTouchDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
        return createSelfDropDispatchTable(block, HAS_SILK_TOUCH, builder);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LootTable.Builder createShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
        return createSelfDropDispatchTable(block, HAS_SHEARS, builder);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LootTable.Builder createSilkTouchOrShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
        return createSelfDropDispatchTable(block, HAS_SHEARS_OR_SILK_TOUCH, builder);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike itemLike) {
        return createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder) applyExplosionCondition(block, LootItem.lootTableItem(itemLike)));
    }

    /* JADX WARN: Type inference failed for: r3v2, types: [net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer$Builder, net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder] */
    private static LootTable.Builder createSingleItemTable(ItemLike itemLike, RandomIntGenerator randomIntGenerator) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder) applyExplosionDecay(itemLike, LootItem.lootTableItem(itemLike).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(randomIntGenerator)))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r2v2, types: [net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer$Builder, net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder] */
    public static LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike itemLike, RandomIntGenerator randomIntGenerator) {
        return createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder) applyExplosionDecay(block, LootItem.lootTableItem(itemLike).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(randomIntGenerator))));
    }

    private static LootTable.Builder createSilkTouchOnlyTable(ItemLike itemLike) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(HAS_SILK_TOUCH).setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(itemLike)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LootTable.Builder createPotFlowerItemTable(ItemLike itemLike) {
        return LootTable.lootTable().withPool((LootPool.Builder) applyExplosionCondition(Blocks.FLOWER_POT, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(Blocks.FLOWER_POT)))).withPool((LootPool.Builder) applyExplosionCondition(itemLike, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(itemLike))));
    }

    /* JADX WARN: Type inference failed for: r3v2, types: [net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer$Builder, net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder] */
    /* JADX WARN: Type inference failed for: r4v3, types: [net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction$Builder, net.minecraft.world.level.storage.loot.functions.LootItemFunction$Builder] */
    private static LootTable.Builder createSlabItemTable(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder) applyExplosionDecay(block, LootItem.lootTableItem(block).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(2)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE)))))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Incorrect types in method signature: <T::Ljava/lang/Comparable<TT;>;:Lnet/minecraft/util/StringRepresentable;>(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/block/state/properties/Property<TT;>;TT;)Lnet/minecraft/world/level/storage/loot/LootTable$Builder; */
//    public static LootTable.Builder createSinglePropConditionTable(Block block, Property property, Comparable comparable) {
//        return LootTable.lootTable().withPool((LootPool.Builder) applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(block).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, comparable))))));
//    }
    public static LootTable.Builder createSinglePropConditionTable(Block block, Property<?> property, Comparable<?> comparable) {
        StatePropertiesPredicate.Builder predicateBuilder = StatePropertiesPredicate.Builder.properties();

        // Специальная обработка для разных типов свойств
        if (property instanceof BooleanProperty) {
            predicateBuilder.hasProperty((BooleanProperty)property, (Boolean)comparable);
        } else if (property instanceof IntegerProperty) {
            predicateBuilder.hasProperty((IntegerProperty)property, (Integer)comparable);
        } else if (property instanceof EnumProperty) {
            predicateBuilder.hasProperty((EnumProperty<?>)property, comparable.toString());
        } else {
            // Для других свойств используем строковое представление
            predicateBuilder.hasProperty(property, comparable.toString());
        }

        return LootTable.lootTable()
                .withPool(applyExplosionCondition(block,
                        LootPool.lootPool()
                                .setRolls(ConstantIntValue.exactly(1))
                                .add(LootItem.lootTableItem(block)
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                .setProperties(predicateBuilder)
                                        ))));
    }
    private static LootTable.Builder createNameableBlockEntityTable(Block block) {
        return LootTable.lootTable().withPool((LootPool.Builder) applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(block).apply((LootItemFunction.Builder) CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)))));
    }

    /* JADX WARN: Type inference failed for: r3v4, types: [net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer$Builder] */
    private static LootTable.Builder createShulkerBoxDrop(Block block) {
        return LootTable.lootTable().withPool((LootPool.Builder) applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(block).apply((LootItemFunction.Builder) CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)).apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Lock", "BlockEntityTag.Lock").copy("LootTable", "BlockEntityTag.LootTable").copy("LootTableSeed", "BlockEntityTag.LootTableSeed")).apply((LootItemFunction.Builder) SetContainerContents.setContents().withEntry(DynamicLoot.dynamicEntry(ShulkerBoxBlock.CONTENTS))))));
    }

    /* JADX WARN: Type inference failed for: r3v4, types: [net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer$Builder] */
    private static LootTable.Builder createBannerDrop(Block block) {
        return LootTable.lootTable().withPool((LootPool.Builder) applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(block).apply((LootItemFunction.Builder) CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)).apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Patterns", "BlockEntityTag.Patterns")))));
    }

    /* JADX WARN: Type inference failed for: r2v5, types: [net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer$Builder] */
    private static LootTable.Builder createBeeNestDrop(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(HAS_SILK_TOUCH).setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(block).apply((LootItemFunction.Builder) CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees")).apply(CopyBlockState.copyState(block).copy(BeehiveBlock.HONEY_LEVEL))));
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static LootTable.Builder createBeeHiveDrop(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(((LootPoolSingletonContainer.Builder) LootItem.lootTableItem(block).when(HAS_SILK_TOUCH)).apply((LootItemFunction.Builder) CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees")).apply((LootItemFunction.Builder) CopyBlockState.copyState(block).copy(BeehiveBlock.HONEY_LEVEL)).otherwise(LootItem.lootTableItem(block))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r2v2, types: [net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer$Builder, net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder] */
    public static LootTable.Builder createOreDrop(Block block, Item item) {
        return createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder) applyExplosionDecay(block, LootItem.lootTableItem(item).apply((LootItemFunction.Builder) ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r2v2, types: [net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer$Builder] */
    public static LootTable.Builder createMushroomBlockDrop(Block block, ItemLike itemLike) {
        return createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder) applyExplosionDecay(block, LootItem.lootTableItem(itemLike).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(RandomValueBounds.between(-6.0f, 2.0f))).apply(LimitCount.limitCount(IntLimiter.lowerBound(0)))));
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static LootTable.Builder createGrassDrops(Block block) {
        return createShearsDispatchTable(block, (LootPoolEntryContainer.Builder) applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder) LootItem.lootTableItem(Items.WHEAT_SEEDS).when(LootItemRandomChanceCondition.randomChance(0.125f))).apply((LootItemFunction.Builder) ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 2))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r3v4, types: [net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer$Builder] */
    /* JADX WARN: Type inference failed for: r4v3, types: [net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction$Builder, net.minecraft.world.level.storage.loot.functions.LootItemFunction$Builder] */
    /* JADX WARN: Type inference failed for: r4v7, types: [net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction$Builder, net.minecraft.world.level.storage.loot.functions.LootItemFunction$Builder] */
    public static LootTable.Builder createStemDrops(Block block, Item item) {
        return LootTable.lootTable().withPool((LootPool.Builder) applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(item).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.06666667f)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 0)))).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.13333334f)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 1)))).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.2f)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 2)))).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.26666668f)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 3)))).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.33333334f)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 4)))).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.4f)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 5)))).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.46666667f)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 6)))).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336f)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 7)))))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LootTable.Builder createAttachedStemDrops(Block block, Item item) {
        return LootTable.lootTable().withPool((LootPool.Builder) applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(item).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336f))))));
    }

    private static LootTable.Builder createShearsOnlyDrop(ItemLike itemLike) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).when(HAS_SHEARS).add(LootItem.lootTableItem(itemLike)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r1v3, types: [net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer$Builder] */
    /* JADX WARN: Type inference failed for: r3v3, types: [net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer$Builder, net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder] */
    public static LootTable.Builder createLeavesDrops(Block block, Block block2, float... fArr) {
        return createSilkTouchOrShearsDispatchTable(block, ((LootPoolSingletonContainer.Builder) applyExplosionCondition(block, LootItem.lootTableItem(block2))).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, fArr))).withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).when(HAS_NO_SHEARS_OR_SILK_TOUCH).add(((LootPoolSingletonContainer.Builder) applyExplosionDecay(block, LootItem.lootTableItem(Items.STICK).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(RandomValueBounds.between(1.0f, 2.0f))))).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.02f, 0.022222223f, 0.025f, 0.033333335f, 0.1f))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LootTable.Builder createOakLeavesDrops(Block block, Block block2, float... fArr) {
        return createLeavesDrops(block, block2, fArr).withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).when(HAS_NO_SHEARS_OR_SILK_TOUCH).add(((LootPoolSingletonContainer.Builder) applyExplosionCondition(block, LootItem.lootTableItem(Items.APPLE))).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.005f, 0.0055555557f, 0.00625f, 0.008333334f, 0.025f))));
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static LootTable.Builder createCropDrops(Block block, Item item, Item item2, LootItemCondition.Builder builder) {
        return (LootTable.Builder) applyExplosionDecay(block, LootTable.lootTable().withPool(LootPool.lootPool().add(((LootPoolSingletonContainer.Builder) LootItem.lootTableItem(item).when(builder)).otherwise(LootItem.lootTableItem(item2)))).withPool(LootPool.lootPool().when(builder).add(LootItem.lootTableItem(item2).apply((LootItemFunction.Builder) ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286f, 3)))));
    }

    private static LootTable.Builder createDoublePlantShearsDrop(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(HAS_SHEARS).add(LootItem.lootTableItem(block).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(2)))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v2, types: [net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer$Builder] */
    public static LootTable.Builder createDoublePlantWithSeedDrops(Block block, Block block2) {
        LootPoolEntryContainer.Builder<?> otherwise = ((LootPoolSingletonContainer.Builder) LootItem.lootTableItem(block2).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(2))).when(HAS_SHEARS)).otherwise(((LootPoolSingletonContainer.Builder) applyExplosionCondition(block, LootItem.lootTableItem(Items.WHEAT_SEEDS))).when(LootItemRandomChanceCondition.randomChance(0.125f)));
        return LootTable.lootTable().withPool(LootPool.lootPool().add(otherwise).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))).when(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER).build()).build()), new BlockPos(0, 1, 0)))).withPool(LootPool.lootPool().add(otherwise).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER))).when(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER).build()).build()), new BlockPos(0, -1, 0))));
    }

    public static LootTable.Builder noDrop() {
        return LootTable.lootTable();
    }

    @Override // java.util.function.Consumer
    public void accept(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
        dropSelf(Blocks.GRANITE);
        dropSelf(Blocks.POLISHED_GRANITE);
        dropSelf(Blocks.DIORITE);
        dropSelf(Blocks.POLISHED_DIORITE);
        dropSelf(Blocks.ANDESITE);
        dropSelf(Blocks.POLISHED_ANDESITE);
        dropSelf(Blocks.DIRT);
        dropSelf(Blocks.COARSE_DIRT);
        dropSelf(Blocks.COBBLESTONE);
        dropSelf(Blocks.OAK_PLANKS);
        dropSelf(Blocks.SPRUCE_PLANKS);
        dropSelf(Blocks.BIRCH_PLANKS);
        dropSelf(Blocks.JUNGLE_PLANKS);
        dropSelf(Blocks.ACACIA_PLANKS);
        dropSelf(Blocks.DARK_OAK_PLANKS);
        dropSelf(Blocks.OAK_SAPLING);
        dropSelf(Blocks.SPRUCE_SAPLING);
        dropSelf(Blocks.BIRCH_SAPLING);
        dropSelf(Blocks.JUNGLE_SAPLING);
        dropSelf(Blocks.ACACIA_SAPLING);
        dropSelf(Blocks.DARK_OAK_SAPLING);
        dropSelf(Blocks.SAND);
        dropSelf(Blocks.RED_SAND);
        dropSelf(Blocks.GOLD_ORE);
        dropSelf(Blocks.IRON_ORE);
        dropSelf(Blocks.OAK_LOG);
        dropSelf(Blocks.SPRUCE_LOG);
        dropSelf(Blocks.BIRCH_LOG);
        dropSelf(Blocks.JUNGLE_LOG);
        dropSelf(Blocks.ACACIA_LOG);
        dropSelf(Blocks.DARK_OAK_LOG);
        dropSelf(Blocks.STRIPPED_SPRUCE_LOG);
        dropSelf(Blocks.STRIPPED_BIRCH_LOG);
        dropSelf(Blocks.STRIPPED_JUNGLE_LOG);
        dropSelf(Blocks.STRIPPED_ACACIA_LOG);
        dropSelf(Blocks.STRIPPED_DARK_OAK_LOG);
        dropSelf(Blocks.STRIPPED_OAK_LOG);
        dropSelf(Blocks.STRIPPED_WARPED_STEM);
        dropSelf(Blocks.STRIPPED_CRIMSON_STEM);
        dropSelf(Blocks.OAK_WOOD);
        dropSelf(Blocks.SPRUCE_WOOD);
        dropSelf(Blocks.BIRCH_WOOD);
        dropSelf(Blocks.JUNGLE_WOOD);
        dropSelf(Blocks.ACACIA_WOOD);
        dropSelf(Blocks.DARK_OAK_WOOD);
        dropSelf(Blocks.STRIPPED_OAK_WOOD);
        dropSelf(Blocks.STRIPPED_SPRUCE_WOOD);
        dropSelf(Blocks.STRIPPED_BIRCH_WOOD);
        dropSelf(Blocks.STRIPPED_JUNGLE_WOOD);
        dropSelf(Blocks.STRIPPED_ACACIA_WOOD);
        dropSelf(Blocks.STRIPPED_DARK_OAK_WOOD);
        dropSelf(Blocks.STRIPPED_CRIMSON_HYPHAE);
        dropSelf(Blocks.STRIPPED_WARPED_HYPHAE);
        dropSelf(Blocks.SPONGE);
        dropSelf(Blocks.WET_SPONGE);
        dropSelf(Blocks.LAPIS_BLOCK);
        dropSelf(Blocks.SANDSTONE);
        dropSelf(Blocks.CHISELED_SANDSTONE);
        dropSelf(Blocks.CUT_SANDSTONE);
        dropSelf(Blocks.NOTE_BLOCK);
        dropSelf(Blocks.POWERED_RAIL);
        dropSelf(Blocks.DETECTOR_RAIL);
        dropSelf(Blocks.STICKY_PISTON);
        dropSelf(Blocks.PISTON);
        dropSelf(Blocks.WHITE_WOOL);
        dropSelf(Blocks.ORANGE_WOOL);
        dropSelf(Blocks.MAGENTA_WOOL);
        dropSelf(Blocks.LIGHT_BLUE_WOOL);
        dropSelf(Blocks.YELLOW_WOOL);
        dropSelf(Blocks.LIME_WOOL);
        dropSelf(Blocks.PINK_WOOL);
        dropSelf(Blocks.GRAY_WOOL);
        dropSelf(Blocks.LIGHT_GRAY_WOOL);
        dropSelf(Blocks.CYAN_WOOL);
        dropSelf(Blocks.PURPLE_WOOL);
        dropSelf(Blocks.BLUE_WOOL);
        dropSelf(Blocks.BROWN_WOOL);
        dropSelf(Blocks.GREEN_WOOL);
        dropSelf(Blocks.RED_WOOL);
        dropSelf(Blocks.BLACK_WOOL);
        dropSelf(Blocks.DANDELION);
        dropSelf(Blocks.POPPY);
        dropSelf(Blocks.BLUE_ORCHID);
        dropSelf(Blocks.ALLIUM);
        dropSelf(Blocks.AZURE_BLUET);
        dropSelf(Blocks.RED_TULIP);
        dropSelf(Blocks.ORANGE_TULIP);
        dropSelf(Blocks.WHITE_TULIP);
        dropSelf(Blocks.PINK_TULIP);
        dropSelf(Blocks.OXEYE_DAISY);
        dropSelf(Blocks.CORNFLOWER);
        dropSelf(Blocks.WITHER_ROSE);
        dropSelf(Blocks.LILY_OF_THE_VALLEY);
        dropSelf(Blocks.BROWN_MUSHROOM);
        dropSelf(Blocks.RED_MUSHROOM);
        dropSelf(Blocks.GOLD_BLOCK);
        dropSelf(Blocks.IRON_BLOCK);
        dropSelf(Blocks.BRICKS);
        dropSelf(Blocks.MOSSY_COBBLESTONE);
        dropSelf(Blocks.OBSIDIAN);
        dropSelf(Blocks.CRYING_OBSIDIAN);
        dropSelf(Blocks.TORCH);
        dropSelf(Blocks.OAK_STAIRS);
        dropSelf(Blocks.REDSTONE_WIRE);
        dropSelf(Blocks.DIAMOND_BLOCK);
        dropSelf(Blocks.CRAFTING_TABLE);
        dropSelf(Blocks.OAK_SIGN);
        dropSelf(Blocks.SPRUCE_SIGN);
        dropSelf(Blocks.BIRCH_SIGN);
        dropSelf(Blocks.ACACIA_SIGN);
        dropSelf(Blocks.JUNGLE_SIGN);
        dropSelf(Blocks.DARK_OAK_SIGN);
        dropSelf(Blocks.LADDER);
        dropSelf(Blocks.RAIL);
        dropSelf(Blocks.COBBLESTONE_STAIRS);
        dropSelf(Blocks.LEVER);
        dropSelf(Blocks.STONE_PRESSURE_PLATE);
        dropSelf(Blocks.OAK_PRESSURE_PLATE);
        dropSelf(Blocks.SPRUCE_PRESSURE_PLATE);
        dropSelf(Blocks.BIRCH_PRESSURE_PLATE);
        dropSelf(Blocks.JUNGLE_PRESSURE_PLATE);
        dropSelf(Blocks.ACACIA_PRESSURE_PLATE);
        dropSelf(Blocks.DARK_OAK_PRESSURE_PLATE);
        dropSelf(Blocks.REDSTONE_TORCH);
        dropSelf(Blocks.STONE_BUTTON);
        dropSelf(Blocks.CACTUS);
        dropSelf(Blocks.SUGAR_CANE);
        dropSelf(Blocks.JUKEBOX);
        dropSelf(Blocks.OAK_FENCE);
        dropSelf(Blocks.PUMPKIN);
        dropSelf(Blocks.NETHERRACK);
        dropSelf(Blocks.SOUL_SAND);
        dropSelf(Blocks.SOUL_SOIL);
        dropSelf(Blocks.BASALT);
        dropSelf(Blocks.POLISHED_BASALT);
        dropSelf(Blocks.SOUL_TORCH);
        dropSelf(Blocks.CARVED_PUMPKIN);
        dropSelf(Blocks.JACK_O_LANTERN);
        dropSelf(Blocks.REPEATER);
        dropSelf(Blocks.OAK_TRAPDOOR);
        dropSelf(Blocks.SPRUCE_TRAPDOOR);
        dropSelf(Blocks.BIRCH_TRAPDOOR);
        dropSelf(Blocks.JUNGLE_TRAPDOOR);
        dropSelf(Blocks.ACACIA_TRAPDOOR);
        dropSelf(Blocks.DARK_OAK_TRAPDOOR);
        dropSelf(Blocks.STONE_BRICKS);
        dropSelf(Blocks.MOSSY_STONE_BRICKS);
        dropSelf(Blocks.CRACKED_STONE_BRICKS);
        dropSelf(Blocks.CHISELED_STONE_BRICKS);
        dropSelf(Blocks.IRON_BARS);
        dropSelf(Blocks.OAK_FENCE_GATE);
        dropSelf(Blocks.BRICK_STAIRS);
        dropSelf(Blocks.STONE_BRICK_STAIRS);
        dropSelf(Blocks.LILY_PAD);
        dropSelf(Blocks.NETHER_BRICKS);
        dropSelf(Blocks.NETHER_BRICK_FENCE);
        dropSelf(Blocks.NETHER_BRICK_STAIRS);
        dropSelf(Blocks.CAULDRON);
        dropSelf(Blocks.END_STONE);
        dropSelf(Blocks.REDSTONE_LAMP);
        dropSelf(Blocks.SANDSTONE_STAIRS);
        dropSelf(Blocks.TRIPWIRE_HOOK);
        dropSelf(Blocks.EMERALD_BLOCK);
        dropSelf(Blocks.SPRUCE_STAIRS);
        dropSelf(Blocks.BIRCH_STAIRS);
        dropSelf(Blocks.JUNGLE_STAIRS);
        dropSelf(Blocks.COBBLESTONE_WALL);
        dropSelf(Blocks.MOSSY_COBBLESTONE_WALL);
        dropSelf(Blocks.FLOWER_POT);
        dropSelf(Blocks.OAK_BUTTON);
        dropSelf(Blocks.SPRUCE_BUTTON);
        dropSelf(Blocks.BIRCH_BUTTON);
        dropSelf(Blocks.JUNGLE_BUTTON);
        dropSelf(Blocks.ACACIA_BUTTON);
        dropSelf(Blocks.DARK_OAK_BUTTON);
        dropSelf(Blocks.SKELETON_SKULL);
        dropSelf(Blocks.WITHER_SKELETON_SKULL);
        dropSelf(Blocks.ZOMBIE_HEAD);
        dropSelf(Blocks.CREEPER_HEAD);
        dropSelf(Blocks.DRAGON_HEAD);
        dropSelf(Blocks.ANVIL);
        dropSelf(Blocks.CHIPPED_ANVIL);
        dropSelf(Blocks.DAMAGED_ANVIL);
        dropSelf(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
        dropSelf(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
        dropSelf(Blocks.COMPARATOR);
        dropSelf(Blocks.DAYLIGHT_DETECTOR);
        dropSelf(Blocks.REDSTONE_BLOCK);
        dropSelf(Blocks.QUARTZ_BLOCK);
        dropSelf(Blocks.CHISELED_QUARTZ_BLOCK);
        dropSelf(Blocks.QUARTZ_PILLAR);
        dropSelf(Blocks.QUARTZ_STAIRS);
        dropSelf(Blocks.ACTIVATOR_RAIL);
        dropSelf(Blocks.WHITE_TERRACOTTA);
        dropSelf(Blocks.ORANGE_TERRACOTTA);
        dropSelf(Blocks.MAGENTA_TERRACOTTA);
        dropSelf(Blocks.LIGHT_BLUE_TERRACOTTA);
        dropSelf(Blocks.YELLOW_TERRACOTTA);
        dropSelf(Blocks.LIME_TERRACOTTA);
        dropSelf(Blocks.PINK_TERRACOTTA);
        dropSelf(Blocks.GRAY_TERRACOTTA);
        dropSelf(Blocks.LIGHT_GRAY_TERRACOTTA);
        dropSelf(Blocks.CYAN_TERRACOTTA);
        dropSelf(Blocks.PURPLE_TERRACOTTA);
        dropSelf(Blocks.BLUE_TERRACOTTA);
        dropSelf(Blocks.BROWN_TERRACOTTA);
        dropSelf(Blocks.GREEN_TERRACOTTA);
        dropSelf(Blocks.RED_TERRACOTTA);
        dropSelf(Blocks.BLACK_TERRACOTTA);
        dropSelf(Blocks.ACACIA_STAIRS);
        dropSelf(Blocks.DARK_OAK_STAIRS);
        dropSelf(Blocks.SLIME_BLOCK);
        dropSelf(Blocks.IRON_TRAPDOOR);
        dropSelf(Blocks.PRISMARINE);
        dropSelf(Blocks.PRISMARINE_BRICKS);
        dropSelf(Blocks.DARK_PRISMARINE);
        dropSelf(Blocks.PRISMARINE_STAIRS);
        dropSelf(Blocks.PRISMARINE_BRICK_STAIRS);
        dropSelf(Blocks.DARK_PRISMARINE_STAIRS);
        dropSelf(Blocks.HAY_BLOCK);
        dropSelf(Blocks.WHITE_CARPET);
        dropSelf(Blocks.ORANGE_CARPET);
        dropSelf(Blocks.MAGENTA_CARPET);
        dropSelf(Blocks.LIGHT_BLUE_CARPET);
        dropSelf(Blocks.YELLOW_CARPET);
        dropSelf(Blocks.LIME_CARPET);
        dropSelf(Blocks.PINK_CARPET);
        dropSelf(Blocks.GRAY_CARPET);
        dropSelf(Blocks.LIGHT_GRAY_CARPET);
        dropSelf(Blocks.CYAN_CARPET);
        dropSelf(Blocks.PURPLE_CARPET);
        dropSelf(Blocks.BLUE_CARPET);
        dropSelf(Blocks.BROWN_CARPET);
        dropSelf(Blocks.GREEN_CARPET);
        dropSelf(Blocks.RED_CARPET);
        dropSelf(Blocks.BLACK_CARPET);
        dropSelf(Blocks.TERRACOTTA);
        dropSelf(Blocks.COAL_BLOCK);
        dropSelf(Blocks.RED_SANDSTONE);
        dropSelf(Blocks.CHISELED_RED_SANDSTONE);
        dropSelf(Blocks.CUT_RED_SANDSTONE);
        dropSelf(Blocks.RED_SANDSTONE_STAIRS);
        dropSelf(Blocks.SMOOTH_STONE);
        dropSelf(Blocks.SMOOTH_SANDSTONE);
        dropSelf(Blocks.SMOOTH_QUARTZ);
        dropSelf(Blocks.SMOOTH_RED_SANDSTONE);
        dropSelf(Blocks.SPRUCE_FENCE_GATE);
        dropSelf(Blocks.BIRCH_FENCE_GATE);
        dropSelf(Blocks.JUNGLE_FENCE_GATE);
        dropSelf(Blocks.ACACIA_FENCE_GATE);
        dropSelf(Blocks.DARK_OAK_FENCE_GATE);
        dropSelf(Blocks.SPRUCE_FENCE);
        dropSelf(Blocks.BIRCH_FENCE);
        dropSelf(Blocks.JUNGLE_FENCE);
        dropSelf(Blocks.ACACIA_FENCE);
        dropSelf(Blocks.DARK_OAK_FENCE);
        dropSelf(Blocks.END_ROD);
        dropSelf(Blocks.PURPUR_BLOCK);
        dropSelf(Blocks.PURPUR_PILLAR);
        dropSelf(Blocks.PURPUR_STAIRS);
        dropSelf(Blocks.END_STONE_BRICKS);
        dropSelf(Blocks.MAGMA_BLOCK);
        dropSelf(Blocks.NETHER_WART_BLOCK);
        dropSelf(Blocks.RED_NETHER_BRICKS);
        dropSelf(Blocks.BONE_BLOCK);
        dropSelf(Blocks.OBSERVER);
        dropSelf(Blocks.TARGET);
        dropSelf(Blocks.WHITE_GLAZED_TERRACOTTA);
        dropSelf(Blocks.ORANGE_GLAZED_TERRACOTTA);
        dropSelf(Blocks.MAGENTA_GLAZED_TERRACOTTA);
        dropSelf(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
        dropSelf(Blocks.YELLOW_GLAZED_TERRACOTTA);
        dropSelf(Blocks.LIME_GLAZED_TERRACOTTA);
        dropSelf(Blocks.PINK_GLAZED_TERRACOTTA);
        dropSelf(Blocks.GRAY_GLAZED_TERRACOTTA);
        dropSelf(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA);
        dropSelf(Blocks.CYAN_GLAZED_TERRACOTTA);
        dropSelf(Blocks.PURPLE_GLAZED_TERRACOTTA);
        dropSelf(Blocks.BLUE_GLAZED_TERRACOTTA);
        dropSelf(Blocks.BROWN_GLAZED_TERRACOTTA);
        dropSelf(Blocks.GREEN_GLAZED_TERRACOTTA);
        dropSelf(Blocks.RED_GLAZED_TERRACOTTA);
        dropSelf(Blocks.BLACK_GLAZED_TERRACOTTA);
        dropSelf(Blocks.WHITE_CONCRETE);
        dropSelf(Blocks.ORANGE_CONCRETE);
        dropSelf(Blocks.MAGENTA_CONCRETE);
        dropSelf(Blocks.LIGHT_BLUE_CONCRETE);
        dropSelf(Blocks.YELLOW_CONCRETE);
        dropSelf(Blocks.LIME_CONCRETE);
        dropSelf(Blocks.PINK_CONCRETE);
        dropSelf(Blocks.GRAY_CONCRETE);
        dropSelf(Blocks.LIGHT_GRAY_CONCRETE);
        dropSelf(Blocks.CYAN_CONCRETE);
        dropSelf(Blocks.PURPLE_CONCRETE);
        dropSelf(Blocks.BLUE_CONCRETE);
        dropSelf(Blocks.BROWN_CONCRETE);
        dropSelf(Blocks.GREEN_CONCRETE);
        dropSelf(Blocks.RED_CONCRETE);
        dropSelf(Blocks.BLACK_CONCRETE);
        dropSelf(Blocks.WHITE_CONCRETE_POWDER);
        dropSelf(Blocks.ORANGE_CONCRETE_POWDER);
        dropSelf(Blocks.MAGENTA_CONCRETE_POWDER);
        dropSelf(Blocks.LIGHT_BLUE_CONCRETE_POWDER);
        dropSelf(Blocks.YELLOW_CONCRETE_POWDER);
        dropSelf(Blocks.LIME_CONCRETE_POWDER);
        dropSelf(Blocks.PINK_CONCRETE_POWDER);
        dropSelf(Blocks.GRAY_CONCRETE_POWDER);
        dropSelf(Blocks.LIGHT_GRAY_CONCRETE_POWDER);
        dropSelf(Blocks.CYAN_CONCRETE_POWDER);
        dropSelf(Blocks.PURPLE_CONCRETE_POWDER);
        dropSelf(Blocks.BLUE_CONCRETE_POWDER);
        dropSelf(Blocks.BROWN_CONCRETE_POWDER);
        dropSelf(Blocks.GREEN_CONCRETE_POWDER);
        dropSelf(Blocks.RED_CONCRETE_POWDER);
        dropSelf(Blocks.BLACK_CONCRETE_POWDER);
        dropSelf(Blocks.KELP);
        dropSelf(Blocks.DRIED_KELP_BLOCK);
        dropSelf(Blocks.DEAD_TUBE_CORAL_BLOCK);
        dropSelf(Blocks.DEAD_BRAIN_CORAL_BLOCK);
        dropSelf(Blocks.DEAD_BUBBLE_CORAL_BLOCK);
        dropSelf(Blocks.DEAD_FIRE_CORAL_BLOCK);
        dropSelf(Blocks.DEAD_HORN_CORAL_BLOCK);
        dropSelf(Blocks.CONDUIT);
        dropSelf(Blocks.DRAGON_EGG);
        dropSelf(Blocks.BAMBOO);
        dropSelf(Blocks.POLISHED_GRANITE_STAIRS);
        dropSelf(Blocks.SMOOTH_RED_SANDSTONE_STAIRS);
        dropSelf(Blocks.MOSSY_STONE_BRICK_STAIRS);
        dropSelf(Blocks.POLISHED_DIORITE_STAIRS);
        dropSelf(Blocks.MOSSY_COBBLESTONE_STAIRS);
        dropSelf(Blocks.END_STONE_BRICK_STAIRS);
        dropSelf(Blocks.STONE_STAIRS);
        dropSelf(Blocks.SMOOTH_SANDSTONE_STAIRS);
        dropSelf(Blocks.SMOOTH_QUARTZ_STAIRS);
        dropSelf(Blocks.GRANITE_STAIRS);
        dropSelf(Blocks.ANDESITE_STAIRS);
        dropSelf(Blocks.RED_NETHER_BRICK_STAIRS);
        dropSelf(Blocks.POLISHED_ANDESITE_STAIRS);
        dropSelf(Blocks.DIORITE_STAIRS);
        dropSelf(Blocks.BRICK_WALL);
        dropSelf(Blocks.PRISMARINE_WALL);
        dropSelf(Blocks.RED_SANDSTONE_WALL);
        dropSelf(Blocks.MOSSY_STONE_BRICK_WALL);
        dropSelf(Blocks.GRANITE_WALL);
        dropSelf(Blocks.STONE_BRICK_WALL);
        dropSelf(Blocks.NETHER_BRICK_WALL);
        dropSelf(Blocks.ANDESITE_WALL);
        dropSelf(Blocks.RED_NETHER_BRICK_WALL);
        dropSelf(Blocks.SANDSTONE_WALL);
        dropSelf(Blocks.END_STONE_BRICK_WALL);
        dropSelf(Blocks.DIORITE_WALL);
        dropSelf(Blocks.LOOM);
        dropSelf(Blocks.SCAFFOLDING);
        dropSelf(Blocks.HONEY_BLOCK);
        dropSelf(Blocks.HONEYCOMB_BLOCK);
        dropSelf(Blocks.RESPAWN_ANCHOR);
        dropSelf(Blocks.LODESTONE);
        dropSelf(Blocks.WARPED_STEM);
        dropSelf(Blocks.WARPED_HYPHAE);
        dropSelf(Blocks.WARPED_FUNGUS);
        dropSelf(Blocks.WARPED_WART_BLOCK);
        dropSelf(Blocks.CRIMSON_STEM);
        dropSelf(Blocks.CRIMSON_HYPHAE);
        dropSelf(Blocks.CRIMSON_FUNGUS);
        dropSelf(Blocks.SHROOMLIGHT);
        dropSelf(Blocks.CRIMSON_PLANKS);
        dropSelf(Blocks.WARPED_PLANKS);
        dropSelf(Blocks.WARPED_PRESSURE_PLATE);
        dropSelf(Blocks.WARPED_FENCE);
        dropSelf(Blocks.WARPED_TRAPDOOR);
        dropSelf(Blocks.WARPED_FENCE_GATE);
        dropSelf(Blocks.WARPED_STAIRS);
        dropSelf(Blocks.WARPED_BUTTON);
        dropSelf(Blocks.WARPED_SIGN);
        dropSelf(Blocks.CRIMSON_PRESSURE_PLATE);
        dropSelf(Blocks.CRIMSON_FENCE);
        dropSelf(Blocks.CRIMSON_TRAPDOOR);
        dropSelf(Blocks.CRIMSON_FENCE_GATE);
        dropSelf(Blocks.CRIMSON_STAIRS);
        dropSelf(Blocks.CRIMSON_BUTTON);
        dropSelf(Blocks.CRIMSON_SIGN);
        dropSelf(Blocks.NETHERITE_BLOCK);
        dropSelf(Blocks.ANCIENT_DEBRIS);
        dropSelf(Blocks.BLACKSTONE);
        dropSelf(Blocks.POLISHED_BLACKSTONE_BRICKS);
        dropSelf(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);
        dropSelf(Blocks.BLACKSTONE_STAIRS);
        dropSelf(Blocks.BLACKSTONE_WALL);
        dropSelf(Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
        dropSelf(Blocks.CHISELED_POLISHED_BLACKSTONE);
        dropSelf(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        dropSelf(Blocks.POLISHED_BLACKSTONE);
        dropSelf(Blocks.POLISHED_BLACKSTONE_STAIRS);
        dropSelf(Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        dropSelf(Blocks.POLISHED_BLACKSTONE_BUTTON);
        dropSelf(Blocks.POLISHED_BLACKSTONE_WALL);
        dropSelf(Blocks.CHISELED_NETHER_BRICKS);
        dropSelf(Blocks.CRACKED_NETHER_BRICKS);
        dropSelf(Blocks.QUARTZ_BRICKS);
        dropSelf(Blocks.CHAIN);
        dropSelf(Blocks.WARPED_ROOTS);
        dropSelf(Blocks.CRIMSON_ROOTS);
        dropOther(Blocks.FARMLAND, Blocks.DIRT);
        dropOther(Blocks.TRIPWIRE, Items.STRING);
        dropOther(Blocks.GRASS_PATH, Blocks.DIRT);
        dropOther(Blocks.KELP_PLANT, Blocks.KELP);
        dropOther(Blocks.BAMBOO_SAPLING, Blocks.BAMBOO);
        add(Blocks.STONE, block -> {
            return createSingleItemTableWithSilkTouch(block, Blocks.COBBLESTONE);
        });
        add(Blocks.GRASS_BLOCK, block2 -> {
            return createSingleItemTableWithSilkTouch(block2, Blocks.DIRT);
        });
        add(Blocks.PODZOL, block3 -> {
            return createSingleItemTableWithSilkTouch(block3, Blocks.DIRT);
        });
        add(Blocks.MYCELIUM, block4 -> {
            return createSingleItemTableWithSilkTouch(block4, Blocks.DIRT);
        });
        add(Blocks.TUBE_CORAL_BLOCK, block5 -> {
            return createSingleItemTableWithSilkTouch(block5, Blocks.DEAD_TUBE_CORAL_BLOCK);
        });
        add(Blocks.BRAIN_CORAL_BLOCK, block6 -> {
            return createSingleItemTableWithSilkTouch(block6, Blocks.DEAD_BRAIN_CORAL_BLOCK);
        });
        add(Blocks.BUBBLE_CORAL_BLOCK, block7 -> {
            return createSingleItemTableWithSilkTouch(block7, Blocks.DEAD_BUBBLE_CORAL_BLOCK);
        });
        add(Blocks.FIRE_CORAL_BLOCK, block8 -> {
            return createSingleItemTableWithSilkTouch(block8, Blocks.DEAD_FIRE_CORAL_BLOCK);
        });
        add(Blocks.HORN_CORAL_BLOCK, block9 -> {
            return createSingleItemTableWithSilkTouch(block9, Blocks.DEAD_HORN_CORAL_BLOCK);
        });
        add(Blocks.CRIMSON_NYLIUM, block10 -> {
            return createSingleItemTableWithSilkTouch(block10, Blocks.NETHERRACK);
        });
        add(Blocks.WARPED_NYLIUM, block11 -> {
            return createSingleItemTableWithSilkTouch(block11, Blocks.NETHERRACK);
        });
        add(Blocks.BOOKSHELF, block12 -> {
            return createSingleItemTableWithSilkTouch(block12, Items.BOOK, ConstantIntValue.exactly(3));
        });
        add(Blocks.CLAY, block13 -> {
            return createSingleItemTableWithSilkTouch(block13, Items.CLAY_BALL, ConstantIntValue.exactly(4));
        });
        add(Blocks.ENDER_CHEST, block14 -> {
            return createSingleItemTableWithSilkTouch(block14, Blocks.OBSIDIAN, ConstantIntValue.exactly(8));
        });
        add(Blocks.SNOW_BLOCK, block15 -> {
            return createSingleItemTableWithSilkTouch(block15, Items.SNOWBALL, ConstantIntValue.exactly(4));
        });
        add(Blocks.CHORUS_PLANT, createSingleItemTable(Items.CHORUS_FRUIT, RandomValueBounds.between(0.0f, 1.0f)));
        dropPottedContents(Blocks.POTTED_OAK_SAPLING);
        dropPottedContents(Blocks.POTTED_SPRUCE_SAPLING);
        dropPottedContents(Blocks.POTTED_BIRCH_SAPLING);
        dropPottedContents(Blocks.POTTED_JUNGLE_SAPLING);
        dropPottedContents(Blocks.POTTED_ACACIA_SAPLING);
        dropPottedContents(Blocks.POTTED_DARK_OAK_SAPLING);
        dropPottedContents(Blocks.POTTED_FERN);
        dropPottedContents(Blocks.POTTED_DANDELION);
        dropPottedContents(Blocks.POTTED_POPPY);
        dropPottedContents(Blocks.POTTED_BLUE_ORCHID);
        dropPottedContents(Blocks.POTTED_ALLIUM);
        dropPottedContents(Blocks.POTTED_AZURE_BLUET);
        dropPottedContents(Blocks.POTTED_RED_TULIP);
        dropPottedContents(Blocks.POTTED_ORANGE_TULIP);
        dropPottedContents(Blocks.POTTED_WHITE_TULIP);
        dropPottedContents(Blocks.POTTED_PINK_TULIP);
        dropPottedContents(Blocks.POTTED_OXEYE_DAISY);
        dropPottedContents(Blocks.POTTED_CORNFLOWER);
        dropPottedContents(Blocks.POTTED_LILY_OF_THE_VALLEY);
        dropPottedContents(Blocks.POTTED_WITHER_ROSE);
        dropPottedContents(Blocks.POTTED_RED_MUSHROOM);
        dropPottedContents(Blocks.POTTED_BROWN_MUSHROOM);
        dropPottedContents(Blocks.POTTED_DEAD_BUSH);
        dropPottedContents(Blocks.POTTED_CACTUS);
        dropPottedContents(Blocks.POTTED_BAMBOO);
        dropPottedContents(Blocks.POTTED_CRIMSON_FUNGUS);
        dropPottedContents(Blocks.POTTED_WARPED_FUNGUS);
        dropPottedContents(Blocks.POTTED_CRIMSON_ROOTS);
        dropPottedContents(Blocks.POTTED_WARPED_ROOTS);
        add(Blocks.ACACIA_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.BIRCH_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.BRICK_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.COBBLESTONE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.DARK_OAK_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.DARK_PRISMARINE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.JUNGLE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.NETHER_BRICK_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.OAK_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.PETRIFIED_OAK_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.PRISMARINE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.PRISMARINE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.PURPUR_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.QUARTZ_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.RED_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.CUT_RED_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.CUT_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.SPRUCE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.STONE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.STONE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.SMOOTH_STONE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.POLISHED_GRANITE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.SMOOTH_RED_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.MOSSY_STONE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.POLISHED_DIORITE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.MOSSY_COBBLESTONE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.END_STONE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.SMOOTH_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.SMOOTH_QUARTZ_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.GRANITE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.ANDESITE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.RED_NETHER_BRICK_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.POLISHED_ANDESITE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.DIORITE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.CRIMSON_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.WARPED_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.BLACKSTONE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.POLISHED_BLACKSTONE_SLAB, BlockLoot::createSlabItemTable);
        add(Blocks.ACACIA_DOOR, BlockLoot::createDoorTable);
        add(Blocks.BIRCH_DOOR, BlockLoot::createDoorTable);
        add(Blocks.DARK_OAK_DOOR, BlockLoot::createDoorTable);
        add(Blocks.IRON_DOOR, BlockLoot::createDoorTable);
        add(Blocks.JUNGLE_DOOR, BlockLoot::createDoorTable);
        add(Blocks.OAK_DOOR, BlockLoot::createDoorTable);
        add(Blocks.SPRUCE_DOOR, BlockLoot::createDoorTable);
        add(Blocks.WARPED_DOOR, BlockLoot::createDoorTable);
        add(Blocks.CRIMSON_DOOR, BlockLoot::createDoorTable);
        add(Blocks.BLACK_BED, block16 -> {
            return createSinglePropConditionTable(block16, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.BLUE_BED, block17 -> {
            return createSinglePropConditionTable(block17, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.BROWN_BED, block18 -> {
            return createSinglePropConditionTable(block18, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.CYAN_BED, block19 -> {
            return createSinglePropConditionTable(block19, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.GRAY_BED, block20 -> {
            return createSinglePropConditionTable(block20, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.GREEN_BED, block21 -> {
            return createSinglePropConditionTable(block21, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.LIGHT_BLUE_BED, block22 -> {
            return createSinglePropConditionTable(block22, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.LIGHT_GRAY_BED, block23 -> {
            return createSinglePropConditionTable(block23, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.LIME_BED, block24 -> {
            return createSinglePropConditionTable(block24, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.MAGENTA_BED, block25 -> {
            return createSinglePropConditionTable(block25, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.PURPLE_BED, block26 -> {
            return createSinglePropConditionTable(block26, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.ORANGE_BED, block27 -> {
            return createSinglePropConditionTable(block27, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.PINK_BED, block28 -> {
            return createSinglePropConditionTable(block28, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.RED_BED, block29 -> {
            return createSinglePropConditionTable(block29, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.WHITE_BED, block30 -> {
            return createSinglePropConditionTable(block30, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.YELLOW_BED, block31 -> {
            return createSinglePropConditionTable(block31, BedBlock.PART, BedPart.HEAD);
        });
        add(Blocks.LILAC, block32 -> {
            return createSinglePropConditionTable(block32, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
        });
        add(Blocks.SUNFLOWER, block33 -> {
            return createSinglePropConditionTable(block33, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
        });
        add(Blocks.PEONY, block34 -> {
            return createSinglePropConditionTable(block34, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
        });
        add(Blocks.ROSE_BUSH, block35 -> {
            return createSinglePropConditionTable(block35, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
        });
        add(Blocks.TNT, LootTable.lootTable().withPool((LootPool.Builder) applyExplosionCondition(Blocks.TNT, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(Blocks.TNT).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.TNT).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty((Property<Boolean>) TntBlock.UNSTABLE, false)))))));
        add(Blocks.COCOA, block36 -> {
            return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder) applyExplosionDecay(block36, LootItem.lootTableItem(Items.COCOA_BEANS).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(3)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block36).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CocoaBlock.AGE, 2)))))));
        });
        add(Blocks.SEA_PICKLE, block37 -> {
            return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder) applyExplosionDecay(Blocks.SEA_PICKLE, LootItem.lootTableItem(block37).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(2)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block37).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SeaPickleBlock.PICKLES, 2)))).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(3)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block37).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SeaPickleBlock.PICKLES, 3)))).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(4)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block37).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SeaPickleBlock.PICKLES, 4)))))));
        });
        add(Blocks.COMPOSTER, block38 -> {
            return LootTable.lootTable().withPool(LootPool.lootPool().add((LootPoolEntryContainer.Builder) applyExplosionDecay(block38, LootItem.lootTableItem(Items.COMPOSTER)))).withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.BONE_MEAL)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block38).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ComposterBlock.LEVEL, 8))));
        });
        add(Blocks.BEACON, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.BREWING_STAND, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.CHEST, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.DISPENSER, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.DROPPER, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.ENCHANTING_TABLE, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.FURNACE, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.HOPPER, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.TRAPPED_CHEST, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.SMOKER, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.BLAST_FURNACE, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.BARREL, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.CARTOGRAPHY_TABLE, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.FLETCHING_TABLE, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.GRINDSTONE, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.LECTERN, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.SMITHING_TABLE, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.STONECUTTER, BlockLoot::createNameableBlockEntityTable);
        add(Blocks.BELL, (v0) -> {
            return createSingleItemTable(v0);
        });
        add(Blocks.LANTERN, (v0) -> {
            return createSingleItemTable(v0);
        });
        add(Blocks.SOUL_LANTERN, (v0) -> {
            return createSingleItemTable(v0);
        });
        add(Blocks.SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.BLACK_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.BLUE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.BROWN_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.CYAN_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.GRAY_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.GREEN_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.LIGHT_BLUE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.LIGHT_GRAY_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.LIME_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.MAGENTA_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.ORANGE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.PINK_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.PURPLE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.RED_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.WHITE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.YELLOW_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        add(Blocks.BLACK_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.BLUE_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.BROWN_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.CYAN_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.GRAY_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.GREEN_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.LIGHT_BLUE_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.LIGHT_GRAY_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.LIME_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.MAGENTA_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.ORANGE_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.PINK_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.PURPLE_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.RED_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.WHITE_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.YELLOW_BANNER, BlockLoot::createBannerDrop);
        add(Blocks.PLAYER_HEAD, block39 -> {
            return LootTable.lootTable().withPool((LootPool.Builder) applyExplosionCondition(block39, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(block39).apply((LootItemFunction.Builder) CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("SkullOwner", "SkullOwner")))));
        });
        add(Blocks.BEE_NEST, BlockLoot::createBeeNestDrop);
        add(Blocks.BEEHIVE, BlockLoot::createBeeHiveDrop);
        add(Blocks.BIRCH_LEAVES, block40 -> {
            return createLeavesDrops(block40, Blocks.BIRCH_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES);
        });
        add(Blocks.ACACIA_LEAVES, block41 -> {
            return createLeavesDrops(block41, Blocks.ACACIA_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES);
        });
        add(Blocks.JUNGLE_LEAVES, block42 -> {
            return createLeavesDrops(block42, Blocks.JUNGLE_SAPLING, JUNGLE_LEAVES_SAPLING_CHANGES);
        });
        add(Blocks.SPRUCE_LEAVES, block43 -> {
            return createLeavesDrops(block43, Blocks.SPRUCE_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES);
        });
        add(Blocks.OAK_LEAVES, block44 -> {
            return createOakLeavesDrops(block44, Blocks.OAK_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES);
        });
        add(Blocks.DARK_OAK_LEAVES, block45 -> {
            return createOakLeavesDrops(block45, Blocks.DARK_OAK_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES);
        });
        add(Blocks.BEETROOTS, createCropDrops(Blocks.BEETROOTS, Items.BEETROOT, Items.BEETROOT_SEEDS, LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.BEETROOTS).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BeetrootBlock.AGE, 3))));
        add(Blocks.WHEAT, createCropDrops(Blocks.WHEAT, Items.WHEAT, Items.WHEAT_SEEDS, LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.WHEAT).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 7))));
        add(Blocks.CARROTS, (LootTable.Builder) applyExplosionDecay(Blocks.CARROTS, LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.CARROT))).withPool(LootPool.lootPool().when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.CARROTS).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CarrotBlock.AGE, 7))).add(LootItem.lootTableItem(Items.CARROT).apply((LootItemFunction.Builder) ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286f, 3))))));
        LootItemCondition.Builder properties = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.POTATOES).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PotatoBlock.AGE, 7));
        add(Blocks.POTATOES, (LootTable.Builder) applyExplosionDecay(Blocks.POTATOES, LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.POTATO))).withPool(LootPool.lootPool().when(properties).add(LootItem.lootTableItem(Items.POTATO).apply((LootItemFunction.Builder) ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286f, 3)))).withPool(LootPool.lootPool().when(properties).add(LootItem.lootTableItem(Items.POISONOUS_POTATO).when(LootItemRandomChanceCondition.randomChance(0.02f))))));
        add(Blocks.SWEET_BERRY_BUSH, block46 -> {
            return (LootTable.Builder) applyExplosionDecay(block46, LootTable.lootTable().withPool(LootPool.lootPool().when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SWEET_BERRY_BUSH).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SweetBerryBushBlock.AGE, 3))).add(LootItem.lootTableItem(Items.SWEET_BERRIES)).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 3.0f))).apply((LootItemFunction.Builder) ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))).withPool(LootPool.lootPool().when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SWEET_BERRY_BUSH).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SweetBerryBushBlock.AGE, 2))).add(LootItem.lootTableItem(Items.SWEET_BERRIES)).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(RandomValueBounds.between(1.0f, 2.0f))).apply((LootItemFunction.Builder) ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))));
        });
        add(Blocks.BROWN_MUSHROOM_BLOCK, block47 -> {
            return createMushroomBlockDrop(block47, Blocks.BROWN_MUSHROOM);
        });
        add(Blocks.RED_MUSHROOM_BLOCK, block48 -> {
            return createMushroomBlockDrop(block48, Blocks.RED_MUSHROOM);
        });
        add(Blocks.COAL_ORE, block49 -> {
            return createOreDrop(block49, Items.COAL);
        });
        add(Blocks.EMERALD_ORE, block50 -> {
            return createOreDrop(block50, Items.EMERALD);
        });
        add(Blocks.NETHER_QUARTZ_ORE, block51 -> {
            return createOreDrop(block51, Items.QUARTZ);
        });
        add(Blocks.DIAMOND_ORE, block52 -> {
            return createOreDrop(block52, Items.DIAMOND);
        });
        add(Blocks.NETHER_GOLD_ORE, block53 -> {
            return createSilkTouchDispatchTable(block53, (LootPoolEntryContainer.Builder) applyExplosionDecay(block53, LootItem.lootTableItem(Items.GOLD_NUGGET).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 6.0f))).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
        });
        add(Blocks.LAPIS_ORE, block54 -> {
            return createSilkTouchDispatchTable(block54, (LootPoolEntryContainer.Builder) applyExplosionDecay(block54, LootItem.lootTableItem(Items.LAPIS_LAZULI).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(RandomValueBounds.between(4.0f, 9.0f))).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
        });
        add(Blocks.COBWEB, block55 -> {
            return createSilkTouchOrShearsDispatchTable(block55, (LootPoolEntryContainer.Builder) applyExplosionCondition(block55, LootItem.lootTableItem(Items.STRING)));
        });
        add(Blocks.DEAD_BUSH, block56 -> {
            return createShearsDispatchTable(block56, (LootPoolEntryContainer.Builder) applyExplosionDecay(block56, LootItem.lootTableItem(Items.STICK).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(RandomValueBounds.between(0.0f, 2.0f)))));
        });
        add(Blocks.NETHER_SPROUTS, (v0) -> {
            return createShearsOnlyDrop(v0);
        });
        add(Blocks.SEAGRASS, (v0) -> {
            return createShearsOnlyDrop(v0);
        });
        add(Blocks.VINE, (v0) -> {
            return createShearsOnlyDrop(v0);
        });
        add(Blocks.TALL_SEAGRASS, createDoublePlantShearsDrop(Blocks.SEAGRASS));
        add(Blocks.LARGE_FERN, block57 -> {
            return createDoublePlantWithSeedDrops(block57, Blocks.FERN);
        });
        add(Blocks.TALL_GRASS, block58 -> {
            return createDoublePlantWithSeedDrops(block58, Blocks.GRASS);
        });
        add(Blocks.MELON_STEM, block59 -> {
            return createStemDrops(block59, Items.MELON_SEEDS);
        });
        add(Blocks.ATTACHED_MELON_STEM, block60 -> {
            return createAttachedStemDrops(block60, Items.MELON_SEEDS);
        });
        add(Blocks.PUMPKIN_STEM, block61 -> {
            return createStemDrops(block61, Items.PUMPKIN_SEEDS);
        });
        add(Blocks.ATTACHED_PUMPKIN_STEM, block62 -> {
            return createAttachedStemDrops(block62, Items.PUMPKIN_SEEDS);
        });
        add(Blocks.CHORUS_FLOWER, block63 -> {
            return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(((LootPoolSingletonContainer.Builder) applyExplosionCondition(block63, LootItem.lootTableItem(block63))).when(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS))));
        });
        add(Blocks.FERN, BlockLoot::createGrassDrops);
        add(Blocks.GRASS, BlockLoot::createGrassDrops);
        add(Blocks.GLOWSTONE, block64 -> {
            return createSilkTouchDispatchTable(block64, (LootPoolEntryContainer.Builder) applyExplosionDecay(block64, LootItem.lootTableItem(Items.GLOWSTONE_DUST).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 4.0f))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)).apply((LootItemFunction.Builder) LimitCount.limitCount(IntLimiter.clamp(1, 4)))));
        });
        add(Blocks.MELON, block65 -> {
            return createSilkTouchDispatchTable(block65, (LootPoolEntryContainer.Builder) applyExplosionDecay(block65, LootItem.lootTableItem(Items.MELON_SLICE).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(RandomValueBounds.between(3.0f, 7.0f))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)).apply((LootItemFunction.Builder) LimitCount.limitCount(IntLimiter.upperBound(9)))));
        });
        add(Blocks.REDSTONE_ORE, block66 -> {
            return createSilkTouchDispatchTable(block66, (LootPoolEntryContainer.Builder) applyExplosionDecay(block66, LootItem.lootTableItem(Items.REDSTONE).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(RandomValueBounds.between(4.0f, 5.0f))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))));
        });
        add(Blocks.SEA_LANTERN, block67 -> {
            return createSilkTouchDispatchTable(block67, (LootPoolEntryContainer.Builder) applyExplosionDecay(block67, LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 3.0f))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)).apply((LootItemFunction.Builder) LimitCount.limitCount(IntLimiter.clamp(1, 5)))));
        });
        add(Blocks.NETHER_WART, block68 -> {
            return LootTable.lootTable().withPool((LootPool.Builder) applyExplosionDecay(block68, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(Items.NETHER_WART).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 4.0f)).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block68).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(NetherWartBlock.AGE, 3)))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block68).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(NetherWartBlock.AGE, 3)))))));
        });
        add(Blocks.SNOW, block69 -> {
            return LootTable.lootTable().withPool(LootPool.lootPool().when(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS)).add(AlternativesEntry.alternatives(AlternativesEntry.alternatives(LootItem.lootTableItem(Items.SNOWBALL).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 1))), ((LootPoolSingletonContainer.Builder) LootItem.lootTableItem(Items.SNOWBALL).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 2)))).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(2))), ((LootPoolSingletonContainer.Builder) LootItem.lootTableItem(Items.SNOWBALL).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 3)))).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(3))), ((LootPoolSingletonContainer.Builder) LootItem.lootTableItem(Items.SNOWBALL).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 4)))).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(4))), ((LootPoolSingletonContainer.Builder) LootItem.lootTableItem(Items.SNOWBALL).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 5)))).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(5))), ((LootPoolSingletonContainer.Builder) LootItem.lootTableItem(Items.SNOWBALL).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 6)))).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(6))), ((LootPoolSingletonContainer.Builder) LootItem.lootTableItem(Items.SNOWBALL).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 7)))).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(7))), LootItem.lootTableItem(Items.SNOWBALL).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(8)))).when(HAS_NO_SILK_TOUCH), AlternativesEntry.alternatives(LootItem.lootTableItem(Blocks.SNOW).when((LootItemCondition.Builder) LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 1))), LootItem.lootTableItem(Blocks.SNOW).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(2))).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 2))), LootItem.lootTableItem(Blocks.SNOW).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(3))).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 3))), LootItem.lootTableItem(Blocks.SNOW).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(4))).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 4))), LootItem.lootTableItem(Blocks.SNOW).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(5))).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 5))), LootItem.lootTableItem(Blocks.SNOW).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(6))).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 6))), LootItem.lootTableItem(Blocks.SNOW).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(7))).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block69).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 7))), LootItem.lootTableItem(Blocks.SNOW_BLOCK)))));
        });
        add(Blocks.GRAVEL, block70 -> {
            return createSilkTouchDispatchTable(block70, (LootPoolEntryContainer.Builder) applyExplosionCondition(block70, ((LootPoolSingletonContainer.Builder) LootItem.lootTableItem(Items.FLINT).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.1f, 0.14285715f, 0.25f, 1.0f))).otherwise(LootItem.lootTableItem(block70))));
        });
        add(Blocks.CAMPFIRE, block71 -> {
            return createSilkTouchDispatchTable(block71, (LootPoolEntryContainer.Builder) applyExplosionCondition(block71, LootItem.lootTableItem(Items.CHARCOAL).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(2)))));
        });
        add(Blocks.GILDED_BLACKSTONE, block72 -> {
            return createSilkTouchDispatchTable(block72, (LootPoolEntryContainer.Builder) applyExplosionCondition(block72, ((LootPoolSingletonContainer.Builder) LootItem.lootTableItem(Items.GOLD_NUGGET).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 5.0f))).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.1f, 0.14285715f, 0.25f, 1.0f))).otherwise(LootItem.lootTableItem(block72))));
        });
        add(Blocks.SOUL_CAMPFIRE, block73 -> {
            return createSilkTouchDispatchTable(block73, (LootPoolEntryContainer.Builder) applyExplosionCondition(block73, LootItem.lootTableItem(Items.SOUL_SOIL).apply((LootItemFunction.Builder) SetItemCountFunction.setCount(ConstantIntValue.exactly(1)))));
        });
        dropWhenSilkTouch(Blocks.GLASS);
        dropWhenSilkTouch(Blocks.WHITE_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.ORANGE_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.MAGENTA_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.LIGHT_BLUE_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.YELLOW_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.LIME_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.PINK_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.GRAY_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.LIGHT_GRAY_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.CYAN_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.PURPLE_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.BLUE_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.BROWN_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.GREEN_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.RED_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.BLACK_STAINED_GLASS);
        dropWhenSilkTouch(Blocks.GLASS_PANE);
        dropWhenSilkTouch(Blocks.WHITE_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.ORANGE_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.MAGENTA_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.YELLOW_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.LIME_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.PINK_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.GRAY_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.CYAN_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.PURPLE_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.BLUE_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.BROWN_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.GREEN_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.RED_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.BLACK_STAINED_GLASS_PANE);
        dropWhenSilkTouch(Blocks.ICE);
        dropWhenSilkTouch(Blocks.PACKED_ICE);
        dropWhenSilkTouch(Blocks.BLUE_ICE);
        dropWhenSilkTouch(Blocks.TURTLE_EGG);
        dropWhenSilkTouch(Blocks.MUSHROOM_STEM);
        dropWhenSilkTouch(Blocks.DEAD_TUBE_CORAL);
        dropWhenSilkTouch(Blocks.DEAD_BRAIN_CORAL);
        dropWhenSilkTouch(Blocks.DEAD_BUBBLE_CORAL);
        dropWhenSilkTouch(Blocks.DEAD_FIRE_CORAL);
        dropWhenSilkTouch(Blocks.DEAD_HORN_CORAL);
        dropWhenSilkTouch(Blocks.TUBE_CORAL);
        dropWhenSilkTouch(Blocks.BRAIN_CORAL);
        dropWhenSilkTouch(Blocks.BUBBLE_CORAL);
        dropWhenSilkTouch(Blocks.FIRE_CORAL);
        dropWhenSilkTouch(Blocks.HORN_CORAL);
        dropWhenSilkTouch(Blocks.DEAD_TUBE_CORAL_FAN);
        dropWhenSilkTouch(Blocks.DEAD_BRAIN_CORAL_FAN);
        dropWhenSilkTouch(Blocks.DEAD_BUBBLE_CORAL_FAN);
        dropWhenSilkTouch(Blocks.DEAD_FIRE_CORAL_FAN);
        dropWhenSilkTouch(Blocks.DEAD_HORN_CORAL_FAN);
        dropWhenSilkTouch(Blocks.TUBE_CORAL_FAN);
        dropWhenSilkTouch(Blocks.BRAIN_CORAL_FAN);
        dropWhenSilkTouch(Blocks.BUBBLE_CORAL_FAN);
        dropWhenSilkTouch(Blocks.FIRE_CORAL_FAN);
        dropWhenSilkTouch(Blocks.HORN_CORAL_FAN);
        otherWhenSilkTouch(Blocks.INFESTED_STONE, Blocks.STONE);
        otherWhenSilkTouch(Blocks.INFESTED_COBBLESTONE, Blocks.COBBLESTONE);
        otherWhenSilkTouch(Blocks.INFESTED_STONE_BRICKS, Blocks.STONE_BRICKS);
        otherWhenSilkTouch(Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS);
        otherWhenSilkTouch(Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS);
        otherWhenSilkTouch(Blocks.INFESTED_CHISELED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS);
        addNetherVinesDropTable(Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT);
        addNetherVinesDropTable(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT);
        add(Blocks.CAKE, noDrop());
        add(Blocks.FROSTED_ICE, noDrop());
        add(Blocks.SPAWNER, noDrop());
        add(Blocks.FIRE, noDrop());
        add(Blocks.SOUL_FIRE, noDrop());
        add(Blocks.NETHER_PORTAL, noDrop());
        Set<ResourceLocation> newHashSet = Sets.newHashSet();
        Iterator<Block> it = Registry.BLOCK.iterator();
        while (it.hasNext()) {
            Block next = it.next();
            ResourceLocation lootTable = next.getLootTable();
            if (lootTable != BuiltInLootTables.EMPTY && newHashSet.add(lootTable)) {
                LootTable.Builder remove = this.map.remove(lootTable);
                if (remove == null) {
                    throw new IllegalStateException(String.format("Missing loottable '%s' for '%s'", lootTable, Registry.BLOCK.getKey(next)));
                }
                biConsumer.accept(lootTable, remove);
            }
        }
        if (!this.map.isEmpty()) {
            throw new IllegalStateException("Created block loot tables for non-blocks: " + this.map.keySet());
        }
    }

    /* JADX WARN: Type inference failed for: r1v2, types: [net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer$Builder] */
    private void addNetherVinesDropTable(Block block, Block block2) {
        LootTable.Builder createSilkTouchOrShearsDispatchTable = createSilkTouchOrShearsDispatchTable(block, LootItem.lootTableItem(block).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.33f, 0.55f, 0.77f, 1.0f)));
        add(block, createSilkTouchOrShearsDispatchTable);
        add(block2, createSilkTouchOrShearsDispatchTable);
    }

    public static LootTable.Builder createDoorTable(Block block) {
        return createSinglePropConditionTable(block, DoorBlock.HALF, DoubleBlockHalf.LOWER);
    }

    public void dropPottedContents(Block block) {
        add(block, block2 -> {
            return createPotFlowerItemTable(((FlowerPotBlock) block2).getContent());
        });
    }

    public void otherWhenSilkTouch(Block block, Block block2) {
        add(block, createSilkTouchOnlyTable(block2));
    }

    public void dropOther(Block block, ItemLike itemLike) {
        add(block, createSingleItemTable(itemLike));
    }

    public void dropWhenSilkTouch(Block block) {
        otherWhenSilkTouch(block, block);
    }

    public void dropSelf(Block block) {
        dropOther(block, block);
    }

    private void add(Block block, Function<Block, LootTable.Builder> function) {
        add(block, function.apply(block));
    }

    private void add(Block block, LootTable.Builder builder) {
        this.map.put(block.getLootTable(), builder);
    }
}
