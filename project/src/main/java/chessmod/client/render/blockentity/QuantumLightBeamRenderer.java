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
        Vec3 start = new Vec3(startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5).normalize();
        Vec3 end = new Vec3(endPos.getX() + 0.5, endPos.getY() + 0.5, endPos.getZ() + 0.5).normalize().scale(2);

        Vec3 direction = end.subtract(start).normalize();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize().scale(0.2f); // scale for beam width

        VertexConsumer buffer = pBufferSource.getBuffer(RenderType.beaconBeam(BEAM_TEXTURE, false));
        Matrix4f matrix = pPoseStack.last().pose();
        Matrix3f normalMatrix = pPoseStack.last().normal();

        float[] color = {1.0f, 1.0f, 1.0f};
        float alpha = 0.5f;

        System.out.println("Start: " + start);
        System.out.println("End: " + end);
        System.out.println("Direction: " + direction);
        System.out.println("Right: " + right);

        // render the beam as a quad
        renderBeamQuad(buffer, matrix, normalMatrix, start, end, right, color, alpha, pPackedLight, pPackedOverlay);
    }

    private void renderBeamQuad(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, Vec3 start, Vec3 end, Vec3 right, float[] color, float alpha, int light, int overlay) {
        buffer.vertex(matrix, (float) (start.x - right.x), (float) (start.y - right.y), (float) (start.z - right.z))
                .color(color[0], color[1], color[2], alpha)
                .uv(0.0f, 1.0f)
                .overlayCoords(overlay)
                .uv2(light)
                //.normal(normalMatrix, 0, 1, 0)  // testing
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                .endVertex();
        buffer.vertex(matrix, (float) (start.x + right.x), (float) (start.y + right.y), (float) (start.z + right.z))
                .color(color[0], color[1], color[2], alpha)
                .uv(0.0f, 0.0f)
                .overlayCoords(overlay)
                .uv2(light)
                //.normal(normalMatrix, 1, 1, 0)
                .normal(normalMatrix, (float) right.x(), (float) right.y(), (float) right.z())
                .endVertex();

        buffer.vertex(matrix, (float) (end.x - right.x), (float) (end.y - right.y), (float) (end.z - right.z))
                .color(color[0], color[1], color[2], alpha)
                .uv(1.0f, 1.0f)
                .overlayCoords(overlay)
                .uv2(light)
                //.normal(normalMatrix, (float) right.x(), (float) right.y(), (float) right.z())
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                //.normal(normalMatrix, 1, 0, 1)
                .endVertex();
        buffer.vertex(matrix, (float) (end.x + right.x), (float) (end.y + right.y), (float) (end.z + right.z))
                .color(color[0], color[1], color[2], alpha)
                .uv(1.0f, 0.0f)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) right.x(), (float) right.y(), (float) right.z())
                //.normal(normalMatrix, 0, 1, 0)
                .endVertex();
    }
}
