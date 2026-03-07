package net.fasttravel.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DivinityShrapnelItem extends Item {

    public DivinityShrapnelItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.fasttravel.divinity_shrapnel.tooltip.1"));
        tooltip.add(Text.translatable("item.fasttravel.divinity_shrapnel.tooltip.2"));
        tooltip.add(Text.translatable("item.fasttravel.divinity_shrapnel.tooltip.3"));
        tooltip.add(Text.translatable("item.fasttravel.divinity_shrapnel.tooltip.4"));

        super.appendTooltip(stack, world, tooltip, context);
    }
}
