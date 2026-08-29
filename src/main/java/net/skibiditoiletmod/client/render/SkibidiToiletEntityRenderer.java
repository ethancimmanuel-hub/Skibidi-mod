package net.skibiditoiletmod.client.render;

import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.skibiditoiletmod.entity.SkibidiToiletEntity;

public class SkibidiToiletEntityRenderer extends MobEntityRenderer<SkibidiToiletEntity, SkibidiToiletEntityModel> {

    public SkibidiToiletEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new SkibidiToiletEntityModel(context.getPart(ModModelLayers.SKIBIDI_TOILET)), 0.6F);
    }

    @Override
    public Identifier getTexture(SkibidiToiletEntity entity) {
        return SkibidiToiletEntityModel.TEXTURE;
    }
}
