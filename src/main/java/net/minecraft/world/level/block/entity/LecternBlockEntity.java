package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/LecternBlockEntity.class */
public class LecternBlockEntity extends BlockEntity implements Clearable, MenuProvider {
    private final Container bookAccess;
    private final ContainerData dataAccess;
    private ItemStack book;
    private int page;
    private int pageCount;

    public LecternBlockEntity() {
        super(BlockEntityType.LECTERN);
        this.bookAccess = new Container() { // from class: net.minecraft.world.level.block.entity.LecternBlockEntity.1
            @Override // net.minecraft.world.Container
            public int getContainerSize() {
                return 1;
            }

            @Override // net.minecraft.world.Container
            public boolean isEmpty() {
                return LecternBlockEntity.this.book.isEmpty();
            }

            @Override // net.minecraft.world.Container
            public ItemStack getItem(int i) {
                return i == 0 ? LecternBlockEntity.this.book : ItemStack.EMPTY;
            }

            @Override // net.minecraft.world.Container
            public ItemStack removeItem(int i, int i2) {
                if (i == 0) {
                    ItemStack split = LecternBlockEntity.this.book.split(i2);
                    if (LecternBlockEntity.this.book.isEmpty()) {
                        LecternBlockEntity.this.onBookItemRemove();
                    }
                    return split;
                }
                return ItemStack.EMPTY;
            }

            @Override // net.minecraft.world.Container
            public ItemStack removeItemNoUpdate(int i) {
                if (i == 0) {
                    ItemStack itemStack = LecternBlockEntity.this.book;
                    LecternBlockEntity.this.book = ItemStack.EMPTY;
                    LecternBlockEntity.this.onBookItemRemove();
                    return itemStack;
                }
                return ItemStack.EMPTY;
            }

            @Override // net.minecraft.world.Container
            public void setItem(int i, ItemStack itemStack) {
            }

            @Override // net.minecraft.world.Container
            public int getMaxStackSize() {
                return 1;
            }

            @Override // net.minecraft.world.Container
            public void setChanged() {
                LecternBlockEntity.this.setChanged();
            }

            @Override // net.minecraft.world.Container
            public boolean stillValid(Player player) {
                if (LecternBlockEntity.this.level.getBlockEntity(LecternBlockEntity.this.worldPosition) != LecternBlockEntity.this || player.distanceToSqr(LecternBlockEntity.this.worldPosition.getX() + 0.5d, LecternBlockEntity.this.worldPosition.getY() + 0.5d, LecternBlockEntity.this.worldPosition.getZ() + 0.5d) > 64.0d) {
                    return false;
                }
                return LecternBlockEntity.this.hasBook();
            }

            @Override // net.minecraft.world.Container
            public boolean canPlaceItem(int i, ItemStack itemStack) {
                return false;
            }

            @Override // net.minecraft.world.Clearable
            public void clearContent() {
            }
        };
        this.dataAccess = new ContainerData() { // from class: net.minecraft.world.level.block.entity.LecternBlockEntity.2
            @Override // net.minecraft.world.inventory.ContainerData
            public int get(int i) {
                if (i == 0) {
                    return LecternBlockEntity.this.page;
                }
                return 0;
            }

            @Override // net.minecraft.world.inventory.ContainerData
            public void set(int i, int i2) {
                if (i == 0) {
                    LecternBlockEntity.this.setPage(i2);
                }
            }

            @Override // net.minecraft.world.inventory.ContainerData
            public int getCount() {
                return 1;
            }
        };
        this.book = ItemStack.EMPTY;
    }

    public ItemStack getBook() {
        return this.book;
    }

    public boolean hasBook() {
        Item item = this.book.getItem();
        return item == Items.WRITABLE_BOOK || item == Items.WRITTEN_BOOK;
    }

    public void setBook(ItemStack itemStack) {
        setBook(itemStack, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBookItemRemove() {
        this.page = 0;
        this.pageCount = 0;
        LecternBlock.resetBookState(getLevel(), getBlockPos(), getBlockState(), false);
    }

    public void setBook(ItemStack itemStack, @Nullable Player player) {
        this.book = resolveBook(itemStack, player);
        this.page = 0;
        this.pageCount = WrittenBookItem.getPageCount(this.book);
        setChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setPage(int i) {
        int clamp = Mth.clamp(i, 0, this.pageCount - 1);
        if (clamp != this.page) {
            this.page = clamp;
            setChanged();
            LecternBlock.signalPageChange(getLevel(), getBlockPos(), getBlockState());
        }
    }

    public int getPage() {
        return this.page;
    }

    public int getRedstoneSignal() {
        return Mth.floor((this.pageCount > 1 ? getPage() / (this.pageCount - 1.0f) : 1.0f) * 14.0f) + (hasBook() ? 1 : 0);
    }

    private ItemStack resolveBook(ItemStack itemStack, @Nullable Player player) {
        if ((this.level instanceof ServerLevel) && itemStack.getItem() == Items.WRITTEN_BOOK) {
            WrittenBookItem.resolveBookComponents(itemStack, createCommandSourceStack(player), player);
        }
        return itemStack;
    }

    private CommandSourceStack createCommandSourceStack(@Nullable Player player) {
        String string;
        Component displayName;
        if (player == null) {
            string = "Lectern";
            displayName = new TextComponent("Lectern");
        } else {
            string = player.getName().getString();
            displayName = player.getDisplayName();
        }
        return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(this.worldPosition), Vec2.ZERO, (ServerLevel) this.level, 2, string, displayName, this.level.getServer(), player);
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        if (compoundTag.contains("Book", 10)) {
            this.book = resolveBook(ItemStack.of(compoundTag.getCompound("Book")), null);
        } else {
            this.book = ItemStack.EMPTY;
        }
        this.pageCount = WrittenBookItem.getPageCount(this.book);
        this.page = Mth.clamp(compoundTag.getInt("Page"), 0, this.pageCount - 1);
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (!getBook().isEmpty()) {
            compoundTag.put("Book", getBook().save(new CompoundTag()));
            compoundTag.putInt("Page", this.page);
        }
        return compoundTag;
    }

    @Override // net.minecraft.world.Clearable
    public void clearContent() {
        setBook(ItemStack.EMPTY);
    }

    @Override // net.minecraft.world.inventory.MenuConstructor
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new LecternMenu(i, this.bookAccess, this.dataAccess);
    }

    @Override // net.minecraft.world.MenuProvider
    public Component getDisplayName() {
        return new TranslatableComponent("container.lectern");
    }
}
