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
import org.lwjgl.opengl.GL11;

public class QuantumLightBeamRenderer {
    private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("chessmod", "textures/entity/beam_texture.png");

    public void renderBeam(BlockPos startPos, BlockPos endPos, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        Vec3 start = new Vec3(startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5);
        Vec3 end = new Vec3(endPos.getX() + 0.5, endPos.getY() + 0.5, endPos.getZ() + 0.5);




        VertexConsumer buffer = pBufferSource.getBuffer(RenderType.entityTranslucent(BEAM_TEXTURE, false));
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Matrix4f matrix = pPoseStack.last().pose();
        Matrix3f normalMatrix = pPoseStack.last().normal();

        float[] color = {1.0f, 1.0f, 1.0f};

        // render the beam as a quad
        renderBeamQuad(buffer, matrix, normalMatrix, start, end, color, pPackedLight, pPackedOverlay);
        RenderSystem.disableBlend();
    }

    private void renderBeamQuad(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, Vec3 start, Vec3 end, float[] color, int light, int overlay) {

        //We should be able to change the beam dimensions here instead of farting around with vertices
        //Texture should be a tileable square, and its width should be texturewidth for this to work
        //note that a "block" in minecraft is 64 wide, so that's what we're workign from for a few things
        final float blocksize = 64f;
        final float beamwidth = 16f;
        final float beamlength = 64f;
        final float texturewidth = 16f;
        final float beamwidthoffset = beamwidth/blocksize/2;

        //We want the alpha to reduce in two phases so we can have the outter part of the the beam (we drop top and bottom half)
        //fade faster than the bottom part. We then draw an extended section of the beam, starting at where the alphe ended with the
        //first section, and then fade both to 0
        final float initialalpha = 1f;
        final float centeralphamidway = 0.5f;
        final float topalphamidway = 0.3f;

        Vec3 direction = end.subtract(start).normalize().scale(beamlength/64);
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize().scale(0.2f); // scale for beam width


        float offset = 1f-(System.currentTimeMillis()%1000)/1000f;

        /*
        imagine that when you're drawing the vector you're looking directly at the rectangle. Draw the bottom left as 1,
        the bottom right as 2, the top right as 3 and the top left as 4, always in that order, and you're looking down the
        normal... think right-hand rule
         */


        //1 top half main part
        buffer.vertex(matrix, (float) (0.5), (float) (0.5), (float) (0.5))
                .color(color[0], color[1], color[2], initialalpha)
                .uv(0.0f, offset)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                .endVertex();
        //2 top half main part
        buffer.vertex(matrix, (float) (0.5 + direction.x), (float) (0.5+direction.y), (float) (0.5 + direction.z))
                .color(color[0], color[1], color[2], centeralphamidway)
                .uv(0.0f, offset+beamlength/texturewidth)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) right.x(), (float) right.y(), (float) right.z())
                .endVertex();
        //3 top half main part
        buffer.vertex(matrix, (float) (0.5 + direction.x), (float) (0.5+beamwidthoffset+direction.y), (float) (0.5 + direction.z))
                .color(color[0], color[1], color[2], topalphamidway)
                .uv(beamwidth/texturewidth, offset+beamlength/texturewidth)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                .endVertex();
        //4 top half main part
        buffer.vertex(matrix, (float) (0.5), (float) (0.5 + beamwidthoffset), (float) (0.5))
                .color(color[0], color[1], color[2], initialalpha)
                .uv(beamwidth/texturewidth, offset)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) right.x(), (float) right.y(), (float) right.z())
                .endVertex();

        //1 bottom half main part
        buffer.vertex(matrix, (float) (0.5), (float) (0.5), (float) (0.5))
                .color(color[0], color[1], color[2], initialalpha)
                .uv(0.0f, offset)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                .endVertex();
        //2 bottom half main part
        buffer.vertex(matrix, (float) (0.5 + direction.x), (float) (0.5+direction.y), (float) (0.5 + direction.z))
                .color(color[0], color[1], color[2], centeralphamidway)
                .uv(0.0f, offset+beamlength/texturewidth)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) right.x(), (float) right.y(), (float) right.z())
                .endVertex();
        //3 bottom half main part
        buffer.vertex(matrix, (float) (0.5 + direction.x), (float) (0.5 - beamwidthoffset+direction.y), (float) (0.5 + direction.z))
                .color(color[0], color[1], color[2], topalphamidway)
                .uv(beamwidth/texturewidth, offset+beamlength/texturewidth)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                .endVertex();
        //4 bottom half main part
        buffer.vertex(matrix, (float) (0.5), (float) (0.5 - beamwidthoffset), (float) (0.5))
                .color(color[0], color[1], color[2], initialalpha)
                .uv(beamwidth/texturewidth, offset)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) right.x(), (float) right.y(), (float) right.z())
                .endVertex();

        //1 top half fade part
        buffer.vertex(matrix, (float) (0.5 + direction.x), (float) (0.5+direction.y), (float) (0.5 + direction.z))
                .color(color[0], color[1], color[2], centeralphamidway)
                .uv(0.0f, offset)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                .endVertex();
        //2 top half fade part
        buffer.vertex(matrix, (float) (0.5 + direction.x*2), (float) (0.5+direction.y*2), (float) (0.5 + direction.z*2))
                .color(color[0], color[1], color[2], 0f)
                .uv(0.0f, offset+beamlength/texturewidth)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) right.x(), (float) right.y(), (float) right.z())
                .endVertex();
        //3 top half fade part
        buffer.vertex(matrix, (float) (0.5 + direction.x*2), (float) (0.5+beamwidthoffset+direction.y*2), (float) (0.5 + direction.z*2))
                .color(color[0], color[1], color[2], 0f)
                .uv(beamwidth/texturewidth, offset+beamlength/texturewidth)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                .endVertex();
        //4 top half fade part (same as 3 main)
        buffer.vertex(matrix, (float) (0.5 + direction.x), (float) (0.5+beamwidthoffset+direction.y), (float) (0.5 + direction.z))
                .color(color[0], color[1], color[2], topalphamidway)
                .uv(beamwidth/texturewidth, offset+beamlength/texturewidth)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                .endVertex();

        //1 bottom half fade part
        buffer.vertex(matrix, (float) (0.5 + direction.x), (float) (0.5+direction.y), (float) (0.5 + direction.z))
                .color(color[0], color[1], color[2], centeralphamidway)
                .uv(0.0f, offset)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                .endVertex();
        //2 bottom half fade part
        buffer.vertex(matrix, (float) (0.5 + direction.x*2), (float) (0.5+direction.y*2), (float) (0.5 + direction.z*2))
                .color(color[0], color[1], color[2], 0f)
                .uv(0.0f, offset+beamlength/texturewidth)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) right.x(), (float) right.y(), (float) right.z())
                .endVertex();
        //3 bottom half fade part
        buffer.vertex(matrix, (float) (0.5 + direction.x*2), (float) (0.5 - beamwidthoffset+direction.y*2), (float) (0.5 + direction.z*2))
                .color(color[0], color[1], color[2], 0)
                .uv(beamwidth/texturewidth, offset+beamlength/texturewidth)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                .endVertex();
        //4 bottom half fade part (same as 3 main)
        buffer.vertex(matrix, (float) (0.5 + direction.x), (float) (0.5 - beamwidthoffset+direction.y), (float) (0.5 + direction.z))
                .color(color[0], color[1], color[2], topalphamidway)
                .uv(beamwidth/texturewidth, offset+beamlength/texturewidth)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, (float) -right.x(), (float) -right.y(), (float) -right.z())
                .endVertex();

    }

}
