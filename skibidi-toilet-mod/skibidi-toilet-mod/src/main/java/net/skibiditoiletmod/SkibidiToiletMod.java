package net.skibiditoiletmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.skibiditoiletmod.entity.ModEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkibidiToiletMod implements ModInitializer {

    public static final String MOD_ID = "skibiditoiletmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final SpawnEggItem SKIBIDI_TOILET_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "skibidi_toilet_spawn_egg"),
            new SpawnEggItem(ModEntities.SKIBIDI_TOILET, 0x3b3b3b, 0x8fd6ff, new net.minecraft.item.Item.Settings())
    );

    @Override
    public void onInitialize() {
        LOGGER.info("[SkibidiToiletMod] Initializing - flush responsibly.");

        ModEntities.register();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries ->
                entries.add(SKIBIDI_TOILET_SPAWN_EGG));
    }
}
