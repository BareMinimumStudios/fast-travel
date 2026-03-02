package net.fasttravel.init;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fasttravel.FastTravelMain;
import net.fasttravel.block.TeleporterBlock;
import net.fasttravel.block.entity.TeleporterEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BlockInit {

    public static final Block TELEPORTER = register("teleporter", new TeleporterBlock(AbstractBlock.Settings.copy(Blocks.STONE)));

    public static BlockEntityType<TeleporterEntity> TELEPORTER_ENTITY;

    private static Block register(String id, Block block) {
        return register(FastTravelMain.identifierOf(id), block);
    }

    private static Block register(Identifier id, Block block) {
        Item item = Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        ItemGroupEvents.modifyEntriesEvent(ItemInit.FASTTRAVEL_ITEM_GROUP).register(entries -> entries.add(item));

        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void init() {
        TELEPORTER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, FastTravelMain.identifierOf("teleporter_entity"),
                FabricBlockEntityTypeBuilder.create(TeleporterEntity::new, TELEPORTER).build(null));
    }

}
