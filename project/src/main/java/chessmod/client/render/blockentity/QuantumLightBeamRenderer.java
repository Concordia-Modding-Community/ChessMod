package chessmod.client.render.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class QuantumLightBeamRenderer {
    private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("chessmod", "textures/entity/beam_texture.png");

    public void renderBeam(BlockPos startPos, BlockPos endPos, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        // Convert block positions to Vector3f
        Vector3f start = new Vector3f((float) startPos.getX() + 0.5f, (float) startPos.getY() + 0.5f, (float) startPos.getZ() + 0.5f);
        Vector3f end = new Vector3f((float) endPos.getX() + 0.5f, (float) endPos.getY() + 0.5f, (float) endPos.getZ() + 0.5f);

        // Bind the texture
        RenderSystem.setShaderTexture(0, BEAM_TEXTURE);

        // Set up the render type
        VertexConsumer buffer = pBufferSource.getBuffer(RenderType.entityCutout(BEAM_TEXTURE));

        // Set up the matrix for rendering
        Matrix4f matrix = pPoseStack.last().pose();
        Matrix3f normalMatrix = pPoseStack.last().normal();

        // Calculate direction vector from start to end
        Vector3f direction = new Vector3f(end).sub(start).normalize();

        // Define the beam width
        float beamWidth = 0.1f;

        // Calculate a perpendicular vector to the direction
        Vector3f perpendicular = new Vector3f(direction).cross(new Vector3f(0, 1, 0)).normalize().mul(beamWidth);

        // Calculate the normal vector for the beam (cross product of direction and perpendicular)
        Vector3f normal = new Vector3f(direction).cross(perpendicular).normalize();

        // Draw the beam with correct normals
        buffer.vertex(matrix, start.x() + perpendicular.x(), start.y() + perpendicular.y(), start.z() + perpendicular.z())
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(0.0f, 0.0f)
                .overlayCoords(pPackedOverlay)
                .uv2(pPackedLight)
                .normal(normalMatrix, normal.x(), normal.y(), normal.z())
                .endVertex();

        buffer.vertex(matrix, end.x() + perpendicular.x(), end.y() + perpendicular.y(), end.z() + perpendicular.z())
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(1.0f, 0.0f)
                .overlayCoords(pPackedOverlay)
                .uv2(pPackedLight)
                .normal(normalMatrix, normal.x(), normal.y(), normal.z())
                .endVertex();
    }
}
