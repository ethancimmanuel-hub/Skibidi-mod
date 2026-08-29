package net.skibiditoiletmod.client.render;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.skibiditoiletmod.entity.SkibidiToiletEntity;

/**
 * Simple block-built model:
 *  - "bowl": the toilet base, stays put.
 *  - "neck": pivots at the top of the bowl; its pivotY is animated to fake
 *            the head extending upward/forward during an attack lunge.
 *  - "head": child of neck; spins rapidly during the flush death sequence.
 *
 * ModelPart.render() renders a part's own cuboids AND all of its children,
 * so rendering just the root part draws the whole toilet.
 */
public class SkibidiToiletEntityModel extends EntityModel<SkibidiToiletEntity> {

    public static final Identifier TEXTURE =
            new Identifier("skibiditoiletmod", "textures/entity/skibidi_toilet.png");

    private final ModelPart root;
    private final ModelPart bowl;
    private final ModelPart neck;
    private final ModelPart head;

    public SkibidiToiletEntityModel(ModelPart root) {
        this.root = root;
        this.bowl = root.getChild("bowl");
        this.neck = this.bowl.getChild("neck");
        this.head = this.neck.getChild("head");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData rootPart = modelData.getRoot();

        ModelPartData bowl = rootPart.addChild("bowl",
                ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F),
                ModelTransform.pivot(0.0F, 18.0F, 0.0F));

        ModelPartData neck = bowl.addChild("neck",
                ModelPartBuilder.create().uv(0, 14).cuboid(-1.5F, -4.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                ModelTransform.pivot(0.0F, -6.0F, 0.0F));

        neck.addChild("head",
                ModelPartBuilder.create().uv(0, 22).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                ModelTransform.pivot(0.0F, -4.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(SkibidiToiletEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        float extension = entity.getHeadExtension(1.0F); // 0 = retracted, 1 = fully extended
        boolean flushing = entity.isFlushing();

        if (flushing) {
            // Head sinks down into the bowl and spins wildly while draining.
            this.neck.pivotY = -6.0F + (1.0F - extension) * 4.0F;
            this.head.pivotY = -4.0F - (1.0F - extension) * 3.0F;
            this.head.yaw = (float) Math.toRadians(entity.getSpinAngle(1.0F));
            this.head.pitch = MathHelper.RADIANS_PER_DEGREE * 25.0F * extension;
        } else {
            // Attack lunge: neck stretches and head reaches toward the target.
            this.neck.pivotY = -6.0F;
            this.head.pivotY = -4.0F - extension * 6.0F;
            this.head.yaw = headYaw * MathHelper.RADIANS_PER_DEGREE * 0.3F;
            this.head.pitch = headPitch * MathHelper.RADIANS_PER_DEGREE * 0.3F;
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        this.root.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }
}
