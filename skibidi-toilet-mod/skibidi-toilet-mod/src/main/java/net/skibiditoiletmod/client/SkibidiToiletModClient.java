package net.skibiditoiletmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.skibiditoiletmod.client.render.ModModelLayers;
import net.skibiditoiletmod.client.render.SkibidiToiletEntityModel;
import net.skibiditoiletmod.client.render.SkibidiToiletEntityRenderer;
import net.skibiditoiletmod.entity.ModEntities;

public class SkibidiToiletModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(
                ModModelLayers.SKIBIDI_TOILET,
                SkibidiToiletEntityModel::getTexturedModelData
        );

        EntityRendererRegistry.register(ModEntities.SKIBIDI_TOILET, SkibidiToiletEntityRenderer::new);
    }
}
