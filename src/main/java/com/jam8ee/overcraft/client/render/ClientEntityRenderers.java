package com.jam8ee.overcraft.client.render;

import com.jam8ee.overcraft.OvercraftMod;
import com.jam8ee.overcraft.entity.effect.BioticFieldEntity;
import com.jam8ee.overcraft.entity.projectile.HelixRocketEntity;
import com.jam8ee.overcraft.core.registry.ModEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = OvercraftMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientEntityRenderers {

    private ClientEntityRenderers() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BIOTIC_FIELD.get(), BioticFieldRenderer::new);
        event.registerEntityRenderer(ModEntities.HELIX_ROCKET.get(), HelixRocketRenderer::new);
    }

    // ===================== Biotic Field Renderer (yellow ring) =====================
    private static final class BioticFieldRenderer extends EntityRenderer<BioticFieldEntity> {

        @SuppressWarnings("removal")
        private static final ResourceLocation DUMMY =
                new ResourceLocation(OvercraftMod.MODID, "textures/misc/empty.png");

        private static final float R = 1.0f;
        private static final float G = 0.82f;
        private static final float B = 0.0f;

        protected BioticFieldRenderer(EntityRendererProvider.Context ctx) {
            super(ctx);
            this.shadowRadius = 0.0f;
        }

        @Override
        public ResourceLocation getTextureLocation(BioticFieldEntity entity) {
            return DUMMY;
        }

        @Override
        public void render(BioticFieldEntity entity,
                           float entityYaw,
                           float partialTicks,
                           PoseStack poseStack,
                           MultiBufferSource buffer,
                           int packedLight) {

            poseStack.pushPose();
            poseStack.translate(0.0, 0.02, 0.0);

            VertexConsumer vc = buffer.getBuffer(RenderType.lines());
            Matrix4f mat = poseStack.last().pose();

            float outer = BioticFieldEntity.RADIUS;
            float inner = Math.max(0.0f, outer - 0.25f);

            drawCircle(vc, mat, outer, 72, R, G, B, 0.95f);
            drawCircle(vc, mat, inner, 72, R, G, B, 0.70f);

            poseStack.popPose();
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }

        private static void drawCircle(VertexConsumer vc,
                                       Matrix4f mat,
                                       float radius,
                                       int segments,
                                       float red, float green, float blue, float alpha) {
            for (int i = 0; i < segments; i++) {
                double a0 = (Math.PI * 2.0) * (i / (double) segments);
                double a1 = (Math.PI * 2.0) * ((i + 1) / (double) segments);

                float x0 = (float) (Math.cos(a0) * radius);
                float z0 = (float) (Math.sin(a0) * radius);
                float x1 = (float) (Math.cos(a1) * radius);
                float z1 = (float) (Math.sin(a1) * radius);

                vc.vertex(mat, x0, 0.0f, z0).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
                vc.vertex(mat, x1, 0.0f, z1).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
            }
        }

        @Override
        public boolean shouldRender(BioticFieldEntity entity, Frustum frustum, double x, double y, double z) {
            return true;
        }
    }

    // ===================== Helix Rocket Renderer (simple line) =====================
    private static final class HelixRocketRenderer extends EntityRenderer<HelixRocketEntity> {

        @SuppressWarnings("removal")
        private static final ResourceLocation DUMMY =
                new ResourceLocation(OvercraftMod.MODID, "textures/misc/empty.png");

        protected HelixRocketRenderer(EntityRendererProvider.Context ctx) {
            super(ctx);
            this.shadowRadius = 0.0f;
        }

        @Override
        public ResourceLocation getTextureLocation(HelixRocketEntity entity) {
            return DUMMY;
        }

        @Override
        public void render(HelixRocketEntity entity,
                           float entityYaw,
                           float partialTicks,
                           PoseStack poseStack,
                           MultiBufferSource buffer,
                           int packedLight) {

            // 用一条短线表现火箭（够用；以后你想换模型再做）
            poseStack.pushPose();

            VertexConsumer vc = buffer.getBuffer(RenderType.lines());
            Matrix4f mat = poseStack.last().pose();

            float r = 1.0f, g = 0.6f, b = 0.1f, a = 1.0f;

            // 从原点画到前方一点点
            vc.vertex(mat, 0.0f, 0.0f, 0.0f).color(r, g, b, a).normal(0, 1, 0).endVertex();
            vc.vertex(mat, 0.0f, 0.0f, 0.35f).color(r, g, b, a).normal(0, 1, 0).endVertex();

            poseStack.popPose();
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }

        @Override
        public boolean shouldRender(HelixRocketEntity entity, Frustum frustum, double x, double y, double z) {
            return true;
        }
    }
}
