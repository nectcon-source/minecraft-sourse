package net.minecraft.world.level.block.entity;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/CampfireBlockEntity.class */
public class CampfireBlockEntity extends BlockEntity implements Clearable, TickableBlockEntity {
    private final NonNullList<ItemStack> items;
    private final int[] cookingProgress;
    private final int[] cookingTime;

    public CampfireBlockEntity() {
        super(BlockEntityType.CAMPFIRE);
        this.items = NonNullList.withSize(4, ItemStack.EMPTY);
        this.cookingProgress = new int[4];
        this.cookingTime = new int[4];
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        boolean booleanValue = ((Boolean) getBlockState().getValue(CampfireBlock.LIT)).booleanValue();
        if (this.level.isClientSide) {
            if (booleanValue) {
                makeParticles();
            }
        } else {
            if (booleanValue) {
                cook();
                return;
            }
            for (int i = 0; i < this.items.size(); i++) {
                if (this.cookingProgress[i] > 0) {
                    this.cookingProgress[i] = Mth.clamp(this.cookingProgress[i] - 2, 0, this.cookingTime[i]);
                }
            }
        }
    }

    private void cook() {
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack itemStack = this.items.get(i);
            if (!itemStack.isEmpty()) {
                int[] iArr = this.cookingProgress;
                int i2 = i;
                iArr[i2] = iArr[i2] + 1;
                if (this.cookingProgress[i] >= this.cookingTime[i]) {
                    SimpleContainer simpleContainer = new SimpleContainer(itemStack);
                    ItemStack itemStack2 = (ItemStack) this.level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, simpleContainer, this.level).map(campfireCookingRecipe -> {
                        return campfireCookingRecipe.assemble(simpleContainer);
                    }).orElse(itemStack);
                    BlockPos blockPos = getBlockPos();
                    Containers.dropItemStack(this.level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack2);
                    this.items.set(i, ItemStack.EMPTY);
                    markUpdated();
                }
            }
        }
    }

    private void makeParticles() {
        Level level = getLevel();
        if (level == null) {
            return;
        }
        BlockPos blockPos = getBlockPos();
        Random random = level.random;
        if (random.nextFloat() < 0.11f) {
            for (int i = 0; i < random.nextInt(2) + 2; i++) {
                CampfireBlock.makeParticles(level, blockPos, ((Boolean) getBlockState().getValue(CampfireBlock.SIGNAL_FIRE)).booleanValue(), false);
            }
        }
        int i2 = ((Direction) getBlockState().getValue(CampfireBlock.FACING)).get2DDataValue();
        for (int i3 = 0; i3 < this.items.size(); i3++) {
            if (!this.items.get(i3).isEmpty() && random.nextFloat() < 0.2f) {
                Direction from2DDataValue = Direction.from2DDataValue(Math.floorMod(i3 + i2, 4));
                double x = ((blockPos.getX() + 0.5d) - (from2DDataValue.getStepX() * 0.3125f)) + (from2DDataValue.getClockWise().getStepX() * 0.3125f);
                double y = blockPos.getY() + 0.5d;
                double z = ((blockPos.getZ() + 0.5d) - (from2DDataValue.getStepZ() * 0.3125f)) + (from2DDataValue.getClockWise().getStepZ() * 0.3125f);
                for (int i4 = 0; i4 < 4; i4++) {
                    level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0d, 5.0E-4d, 0.0d);
                }
            }
        }
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.items.clear();
        ContainerHelper.loadAllItems(compoundTag, this.items);
        if (compoundTag.contains("CookingTimes", 11)) {
            int[] intArray = compoundTag.getIntArray("CookingTimes");
            System.arraycopy(intArray, 0, this.cookingProgress, 0, Math.min(this.cookingTime.length, intArray.length));
        }
        if (compoundTag.contains("CookingTotalTimes", 11)) {
            int[] intArray2 = compoundTag.getIntArray("CookingTotalTimes");
            System.arraycopy(intArray2, 0, this.cookingTime, 0, Math.min(this.cookingTime.length, intArray2.length));
        }
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        saveMetadataAndItems(compoundTag);
        compoundTag.putIntArray("CookingTimes", this.cookingProgress);
        compoundTag.putIntArray("CookingTotalTimes", this.cookingTime);
        return compoundTag;
    }

    private CompoundTag saveMetadataAndItems(CompoundTag compoundTag) {
        super.save(compoundTag);
        ContainerHelper.saveAllItems(compoundTag, this.items, true);
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 13, getUpdateTag());
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag getUpdateTag() {
        return saveMetadataAndItems(new CompoundTag());
    }

    public Optional<CampfireCookingRecipe> getCookableRecipe(ItemStack itemStack) {
        return this.items.stream().noneMatch((v0) -> {
            return v0.isEmpty();
        }) ? Optional.empty() : this.level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SimpleContainer(itemStack), this.level);
    }

    public boolean placeFood(ItemStack itemStack, int i) {
        for (int i2 = 0; i2 < this.items.size(); i2++) {
            if (this.items.get(i2).isEmpty()) {
                this.cookingTime[i2] = i;
                this.cookingProgress[i2] = 0;
                this.items.set(i2, itemStack.split(1));
                markUpdated();
                return true;
            }
        }
        return false;
    }

    private void markUpdated() {
        setChanged();
        getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override // net.minecraft.world.Clearable
    public void clearContent() {
        this.items.clear();
    }

    public void dowse() {
        if (this.level != null) {
            if (!this.level.isClientSide) {
                Containers.dropContents(this.level, getBlockPos(), getItems());
            }
            markUpdated();
        }
    }
}
