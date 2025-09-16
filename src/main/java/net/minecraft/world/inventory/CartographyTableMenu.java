package net.minecraft.world.inventory;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class CartographyTableMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private long lastSoundTime;
    public final Container container = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            CartographyTableMenu.this.slotsChanged(this);
            super.setChanged();
        }
    };
    private final ResultContainer resultContainer = new ResultContainer() {
        @Override
        public void setChanged() {
            CartographyTableMenu.this.slotsChanged(this);
            super.setChanged();
        }
    };

    public CartographyTableMenu(int var1, Inventory var2) {
        this(var1, var2, ContainerLevelAccess.NULL);
    }

    public CartographyTableMenu(int var1, Inventory var2, final ContainerLevelAccess var3) {
        super(MenuType.CARTOGRAPHY_TABLE, var1);
        this.access = var3;
        this.addSlot(new Slot(this.container, 0, 15, 15) {
            @Override
            public boolean mayPlace(ItemStack var1) {
                return var1.getItem() == Items.FILLED_MAP;
            }
        });
        this.addSlot(new Slot(this.container, 1, 15, 52) {
            @Override
            public boolean mayPlace(ItemStack var1) {
                Item var2 = var1.getItem();
                return var2 == Items.PAPER || var2 == Items.MAP || var2 == Items.GLASS_PANE;
            }
        });
        this.addSlot(new Slot(this.resultContainer, 2, 145, 39) {
            @Override
            public boolean mayPlace(ItemStack var1) {
                return false;
            }

            @Override
            public ItemStack onTake(Player var1, ItemStack var2) {
                CartographyTableMenu.this.slots.get(0).remove(1);
                CartographyTableMenu.this.slots.get(1).remove(1);
            var2.getItem().onCraftedBy(var2, var1.level, var1);
                var3.execute((var1x, var2x) -> {
                    long var3 = var1x.getGameTime();
                    if (CartographyTableMenu.this.lastSoundTime != var3) {
                        var1x.playSound(null, var2x, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        CartographyTableMenu.this.lastSoundTime = var3;
                    }
                });
                return super.onTake(var1, var2);
            }
        });

        for(int var4 = 0; var4 < 3; ++var4) {
            for(int var5x = 0; var5x < 9; ++var5x) {
                this.addSlot(new Slot(var2, var5x + var4 * 9 + 9, 8 + var5x * 18, 84 + var4 * 18));
            }
        }

        for(int var6 = 0; var6 < 9; ++var6) {
            this.addSlot(new Slot(var2, var6, 8 + var6 * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player var1) {
        return stillValid(this.access, var1, Blocks.CARTOGRAPHY_TABLE);
    }

    @Override
    public void slotsChanged(Container var1) {
        ItemStack var2 = this.container.getItem(0);
        ItemStack var3 = this.container.getItem(1);
        ItemStack var4 = this.resultContainer.getItem(2);
        if (var4.isEmpty() || !var2.isEmpty() && !var3.isEmpty()) {
            if (!var2.isEmpty() && !var3.isEmpty()) {
                this.setupResultSlot(var2, var3, var4);
            }
        } else {
            this.resultContainer.removeItemNoUpdate(2);
        }
    }

    private void setupResultSlot(ItemStack var1, ItemStack var2, ItemStack var3) {
        this.access.execute((var4, var5) -> {
            Item var6 = var2.getItem();
            MapItemSavedData var7x = MapItem.getSavedData(var1, var4);
            if (var7x != null) {
                ItemStack var8xx;
                if (var6 == Items.PAPER && !var7x.locked && var7x.scale < 4) {
                    var8xx = var1.copy();
                    var8xx.setCount(1);
                    var8xx.getOrCreateTag().putInt("map_scale_direction", 1);
                    this.broadcastChanges();
                } else if (var6 == Items.GLASS_PANE && !var7x.locked) {
                    var8xx = var1.copy();
                    var8xx.setCount(1);
                    var8xx.getOrCreateTag().putBoolean("map_to_lock", true);
                    this.broadcastChanges();
                } else {
                    if (var6 != Items.MAP) {
                        this.resultContainer.removeItemNoUpdate(2);
                        this.broadcastChanges();
                        return;
                    }

                    var8xx = var1.copy();
                    var8xx.setCount(2);
                    this.broadcastChanges();
                }

                if (!ItemStack.matches(var8xx, var3)) {
                    this.resultContainer.setItem(2, var8xx);
                    this.broadcastChanges();
                }
            }
        });
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack var1, Slot var2) {
        return var2.container != this.resultContainer && super.canTakeItemForPickAll(var1, var2);
    }

    @Override
    public ItemStack quickMoveStack(Player var1, int var2) {
        ItemStack var3 = ItemStack.EMPTY;
        Slot var4x = this.slots.get(var2);
        if (var4x != null && var4x.hasItem()) {
            ItemStack var5xx = var4x.getItem();
            Item var7xxx = var5xx.getItem();
            var3 = var5xx.copy();
            if (var2 == 2) {
                var7xxx.onCraftedBy(var5xx, var1.level, var1);
                if (!this.moveItemStackTo(var5xx, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                var4x.onQuickCraft(var5xx, var3);
            } else if (var2 != 1 && var2 != 0) {
                if (var7xxx == Items.FILLED_MAP) {
                    if (!this.moveItemStackTo(var5xx, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (var7xxx != Items.PAPER && var7xxx != Items.MAP && var7xxx != Items.GLASS_PANE) {
                    if (var2 >= 3 && var2 < 30) {
                        if (!this.moveItemStackTo(var5xx, 30, 39, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (var2 >= 30 && var2 < 39 && !this.moveItemStackTo(var5xx, 3, 30, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(var5xx, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(var5xx, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (var5xx.isEmpty()) {
                var4x.set(ItemStack.EMPTY);
            }

            var4x.setChanged();
            if (var5xx.getCount() == var3.getCount()) {
                return ItemStack.EMPTY;
            }

            var4x.onTake(var1, var5xx);
            this.broadcastChanges();
        }

        return var3;
    }

    @Override
    public void removed(Player var1) {
        super.removed(var1);
        this.resultContainer.removeItemNoUpdate(2);
        this.access.execute((var2, var3) -> this.clearContainer(var1, var1.level, this.container));
    }
}
