package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/RecordItem.class */
public class RecordItem extends Item {
    private static final Map<SoundEvent, RecordItem> BY_NAME = Maps.newHashMap();
    private final int analogOutput;
    private final SoundEvent sound;

    protected RecordItem(int i, SoundEvent soundEvent, Item.Properties properties) {
        super(properties);
        this.analogOutput = i;
        this.sound = soundEvent;
        BY_NAME.put(this.sound, this);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos clickedPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(clickedPos);
        if (!blockState.is(Blocks.JUKEBOX) || ((Boolean) blockState.getValue(JukeboxBlock.HAS_RECORD)).booleanValue()) {
            return InteractionResult.PASS;
        }
        ItemStack itemInHand = useOnContext.getItemInHand();
        if (!level.isClientSide) {
            ((JukeboxBlock) Blocks.JUKEBOX).setRecord(level, clickedPos, blockState, itemInHand);
            level.levelEvent(null, 1010, clickedPos, Item.getId(this));
            itemInHand.shrink(1);
            Player player = useOnContext.getPlayer();
            if (player != null) {
                player.awardStat(Stats.PLAY_RECORD);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public int getAnalogOutput() {
        return this.analogOutput;
    }

    @Override // net.minecraft.world.item.Item
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        list.add(getDisplayName().withStyle(ChatFormatting.GRAY));
    }

    public MutableComponent getDisplayName() {
        return new TranslatableComponent(getDescriptionId() + ".desc");
    }

    @Nullable
    public static RecordItem getBySound(SoundEvent soundEvent) {
        return BY_NAME.get(soundEvent);
    }

    public SoundEvent getSound() {
        return this.sound;
    }
}
