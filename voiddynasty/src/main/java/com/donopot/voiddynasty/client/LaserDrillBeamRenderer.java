package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.LaserDrillItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Full-bright crossed ribbons: continuous beam, zero particle trail after release. */
@Environment(EnvType.CLIENT)
public final class LaserDrillBeamRenderer {
    private record Beam(Vec3 start, Vec3 end) {}
    private static final RenderStateDataKey<List<Beam>> BEAMS = RenderStateDataKey.create(() -> "voiddynasty:drill_beams");
    private LaserDrillBeamRenderer() {}

    public static void init() {
        LevelExtractionEvents.END_EXTRACTION.register(context -> {
            Minecraft mc = Minecraft.getInstance();
            var beams = new ArrayList<Beam>();
            Vec3 camera = context.levelState().cameraRenderState.pos;
            float partial = context.levelState().cameraRenderState.cameraEntityPartialTicks;
            for (Player player : context.level().players()) {
                if (!player.isAlive() || player.isSpectator() || !player.isUsingItem()
                        || !(player.getUseItem().getItem() instanceof LaserDrillItem)
                        || player.getTicksUsingItem() < LaserDrillItem.CHARGE_TICKS
                        || player.position().distanceToSqr(camera) > 32*32) continue;
                Vec3 eye = player.getEyePosition(partial), forward = player.getViewVector(partial);
                Vec3 end = context.level().clip(new ClipContext(eye, eye.add(forward.scale(LaserDrillItem.RANGE)),
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
                boolean firstPerson = player == mc.player && context.levelState().cameraRenderState.isFirstPerson;
                Vec3 start = muzzle(player, eye, forward, firstPerson);
                // Near impacts must never put the visible muzzle beyond the hit surface.
                if (eye.distanceToSqr(end) < eye.distanceToSqr(start)) start = eye;
                beams.add(new Beam(start.subtract(camera), end.subtract(camera)));
            }
            ((FabricRenderState)context.levelState()).setData(BEAMS, List.copyOf(beams));
        });
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            List<Beam> beams = ((FabricRenderState)context.levelState()).getDataOrDefault(BEAMS, List.of());
            for (Beam beam : beams) context.submitNodeCollector().submitCustomGeometry(context.poseStack(),
                    RenderTypes.lightning(), (pose, vertices) -> {
                        ribbons(pose, vertices, beam, 0.035, 0x5829CBDD);
                        ribbons(pose, vertices, beam, 0.009, 0xF0DBFFF9);
                    });
        });
    }

    static Vec3 muzzle(Player player, Vec3 eye, Vec3 forward, boolean firstPerson) {
        Vec3 side = forward.cross(new Vec3(0,1,0));
        if (side.lengthSqr() < 0.001) side = new Vec3(1,0,0);
        boolean rightHand = (player.getMainArm() == HumanoidArm.RIGHT) == (player.getUsedItemHand() == InteractionHand.MAIN_HAND);
        return eye.add(forward.scale(firstPerson ? 0.9 : 0.95))
                .add(side.normalize().scale((rightHand ? 1 : -1) * (firstPerson ? 0.32 : -0.07)))
                .add(0, firstPerson ? -0.16 : -0.13, 0);
    }

    private static void ribbons(PoseStack.Pose pose, VertexConsumer vertices, Beam beam, double width, int color) {
        Vec3 direction = beam.end.subtract(beam.start).normalize();
        Vec3 side = direction.cross(new Vec3(0,1,0));
        if (side.lengthSqr() < 0.001) side = direction.cross(new Vec3(1,0,0));
        side = side.normalize().scale(width);
        quad(pose, vertices, beam.start, beam.end, side, color);
        quad(pose, vertices, beam.start, beam.end, direction.cross(side).normalize().scale(width), color);
    }
    private static void quad(PoseStack.Pose pose, VertexConsumer vertices, Vec3 a, Vec3 b, Vec3 offset, int color) {
        vertex(pose, vertices, a.add(offset), color); vertex(pose, vertices, a.subtract(offset), color);
        vertex(pose, vertices, b.subtract(offset), color); vertex(pose, vertices, b.add(offset), color);
    }
    private static void vertex(PoseStack.Pose pose, VertexConsumer vertices, Vec3 point, int color) {
        vertices.addVertex(pose, (float)point.x, (float)point.y, (float)point.z).setColor(color);
    }
}
