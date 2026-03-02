package net.fasttravel.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fasttravel.FastTravelMain;
import net.fasttravel.item.DivinityShrapnelItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ItemInit {

    // Item Group
    public static final RegistryKey<ItemGroup> FASTTRAVEL_ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, FastTravelMain.identifierOf("item_group"));

    public static final Item DIVINITY_SHRAPNEL = register("divinity_shrapnel", new DivinityShrapnelItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE).fireproof()));

    private static Item register(String id, Item item) {
        return register(FastTravelMain.identifierOf(id), item);
    }

    private static Item register(Identifier id, Item item) {
        ItemGroupEvents.modifyEntriesEvent(FASTTRAVEL_ITEM_GROUP).register(entries -> entries.add(item));
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void init() {
        Registry.register(Registries.ITEM_GROUP, FASTTRAVEL_ITEM_GROUP,
                FabricItemGroup.builder().icon(() -> new ItemStack(ItemInit.DIVINITY_SHRAPNEL)).displayName(Text.translatable("item.fasttravel.item_group")).build());
    }
}
