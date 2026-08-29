package net.skibiditoiletmod.client.render;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public final class ModModelLayers {
    public static final EntityModelLayer SKIBIDI_TOILET =
            new EntityModelLayer(new Identifier("skibiditoiletmod", "skibidi_toilet"), "main");

    private ModModelLayers() {
    }
}
