package majcek210.xplrclose.client;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

// had some inspo to use as a guide(its stil written by me and wasn't copied)
public class EspHandler {
    private static final RenderPipeline PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("xplrclose", "pipeline/esp"))
                    .withDepthStencilState(Optional.empty())
                    .build()
            );

    private static final RenderType ESP_TYPE = RenderType.create("xplrclose:esp", RenderSetup.builder(PIPELINE).createRenderSetup());

    public static final List<Player> targetList = new CopyOnWriteArrayList<>();
    public static void register() {
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            for (Player plr : targetList) {
                if (plr.isRemoved()) {
                    continue;
                }
                renderOnPlayer(context, plr);
            }
        });
    }

    public static void renderOnPlayer(LevelRenderContext cntx, Player player) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        var hitBox = player.getBoundingBox();

        Vec3 cam = cntx.levelState().cameraRenderState.pos;
        PoseStack stack = cntx.poseStack();
        stack.pushPose();
        stack.translate(-cam.x, -cam.x,-cam.z);

        cntx.submitNodeCollector().submitCustomGeometry(stack, ESP_TYPE, (pose, consumer) -> {
            box(consumer, pose,
                    (float) hitBox.minX, (float) hitBox.minY, (float) hitBox.minZ,
                    (float) hitBox.maxX, (float) hitBox.maxY, (float) hitBox.maxZ,
                    1f, 0f, 0f, 1f);

        });

        stack.popPose();

    }

    private static void box(VertexConsumer v, PoseStack.Pose p,
                            float x0, float y0, float z0, float x1, float y1, float z1,
                            float r, float g, float b, float a) {
        line(v, p, x0, y0, z0, x1, y0, z0, r, g, b, a);
        line(v, p, x0, y1, z0, x1, y1, z0, r, g, b, a);
        line(v, p, x0, y0, z1, x1, y0, z1, r, g, b, a);
        line(v, p, x0, y1, z1, x1, y1, z1, r, g, b, a);

        line(v, p, x0, y0, z0, x0, y1, z0, r, g, b, a);
        line(v, p, x1, y0, z0, x1, y1, z0, r, g, b, a);
        line(v, p, x0, y0, z1, x0, y1, z1, r, g, b, a);
        line(v, p, x1, y0, z1, x1, y1, z1, r, g, b, a);

        line(v, p, x0, y0, z0, x0, y0, z1, r, g, b, a);
        line(v, p, x1, y0, z0, x1, y0, z1, r, g, b, a);
        line(v, p, x0, y1, z0, x0, y1, z1, r, g, b, a);
        line(v, p, x1, y1, z0, x1, y1, z1, r, g, b, a);
    }
    private static void line(VertexConsumer v, PoseStack.Pose p,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float r, float g, float b, float a) {
        float nx = Math.signum(x1 - x0), ny = Math.signum(y1-y0), nz = Math.signum(z1-z0);
        v.addVertex(p, x0, y0, z0).setColor(r, g, b, a).setNormal(p, nx, ny, nz).setLineWidth(2f);
        v.addVertex(p, x1, y1, z1).setColor(r, g, b, a).setNormal(p, nx, ny, nz).setLineWidth(2f);
    }
}
