package chessmod.client.render.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class QuantumLightBeamRenderer {
    private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("chessmod", "textures/entity/beam_texture.png");

    public void renderBeam(BlockPos startPos, BlockPos endPos, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        Vec3 start = new Vec3(startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5);
        Vec3 end = new Vec3(endPos.getX() + 0.5, endPos.getY() + 0.5, endPos.getZ() + 0.5);

        Vec3 direction = end.subtract(start).normalize();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize().scale(0.1f);

        VertexConsumer buffer = pBufferSource.getBuffer(RenderType.beaconBeam(BEAM_TEXTURE, false));
        Matrix4f matrix = pPoseStack.last().pose();
        Matrix3f normalMatrix = pPoseStack.last().normal();

        float[] color = {1.0f, 1.0f, 1.0f}; //  color for the beam
        float alpha = 0.5f; // Beam transparency

        // render  beam as a quad strip with normals
        buffer.vertex(matrix, (float) (start.x + right.x), (float) (start.y + right.y), (float) (start.z + right.z))
                .color(color[0], color[1], color[2], alpha)
                .uv(0.0f, 0.0f)
                .overlayCoords(pPackedOverlay)
                .uv2(pPackedLight)
                .normal(normalMatrix, (float) right.x(), (float) right.y(), (float) right.z())
                .endVertex();
        buffer.vertex(matrix, (float) (end.x + right.x), (float) (end.y + right.y), (float) (end.z + right.z))
                .color(color[0], color[1], color[2], alpha)
                .uv(1.0f, 0.0f)
                .overlayCoords(pPackedOverlay)
                .uv2(pPackedLight)
                .normal(normalMatrix, (float) right.x(), (float) right.y(), (float) right.z())
                .endVertex();
        buffer.vertex(matrix, (float) (start.x - right.x), (float) (start.y - right.y), (float) (start.z - right.z))
                .color(color[0], color[1], color[2], alpha)
                .uv(0.0f, 1.0f)
                .overlayCoords(pPackedOverlay)
                .uv2(pPackedLight)
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                .endVertex();
        buffer.vertex(matrix, (float) (end.x - right.x), (float) (end.y - right.y), (float) (end.z - right.z))
                .color(color[0], color[1], color[2], alpha)
                .uv(1.0f, 1.0f)
                .overlayCoords(pPackedOverlay)
                .uv2(pPackedLight)
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                .endVertex();
    }
}