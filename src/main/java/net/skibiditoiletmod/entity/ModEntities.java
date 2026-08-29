package net.skibiditoiletmod.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registers the Skibidi Toilet entity type and its attributes.
 */
public final class ModEntities {

    public static final EntityType<SkibidiToiletEntity> SKIBIDI_TOILET = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("skibiditoiletmod", "skibidi_toilet"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, SkibidiToiletEntity::new)
                    .dimensions(EntityDimensions.fixed(0.8f, 1.2f))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(3)
                    .build()
    );

    private ModEntities() {
    }

    /** Called once from the main mod initializer. */
    public static void register() {
        FabricDefaultAttributeRegistry.register(SKIBIDI_TOILET, SkibidiToiletEntity.createAttributes());
    }
}
