package com.donopot.voiddynastybridge.refit;
import com.donopot.voiddynasty.weapon.LaserBeamEntity;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.*;

/** Untextured, fullbright beam geometry. No game textures or extracted assets are copied. */
public final class FixedLaserRenderer extends EntityRenderer<LaserBeamEntity,FixedLaserRenderer.State> {
    public static final class State extends EntityRenderState { Vec3 start=Vec3.ZERO,end=Vec3.ZERO;int color; }
    public FixedLaserRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(LaserBeamEntity entity,State state,float partial){
        super.extractRenderState(entity,state,partial);var beam=(BeamData)entity;
        state.start=beam.vd$start().subtract(entity.position());state.end=beam.vd$end().subtract(entity.position());state.color=beam.vd$color();
    }
    @Override protected AABB getBoundingBoxForCulling(LaserBeamEntity entity,float partial){var beam=(BeamData)entity;return new AABB(beam.vd$start(),beam.vd$end()).inflate(.2);}
    @Override public void submit(State state,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera){
        var start=state.start;var end=state.end;var ray=end.subtract(start);double length=ray.length();if(length<.001||length>513)return;
        var direction=ray.scale(1/length);var a=start.add(direction.scale(Math.min(.25,length*.15)));
        var axis=Math.abs(direction.y)>.95?new Vec3(1,0,0):new Vec3(0,1,0);
        var u=direction.cross(axis).normalize();var v=direction.cross(u).normalize();int color=state.color;
        collector.submitCustomGeometry(poses,RenderTypes.lightning(),(pose,vertices)->{
            tube(vertices,pose,a,end,u.scale(.055),v.scale(.055),(color&0xFFFFFF)|0x88000000);
            tube(vertices,pose,a,end,u.scale(.018),v.scale(.018),0xFFFFF1DC);
        });
    }
    private static void tube(VertexConsumer out,PoseStack.Pose pose,Vec3 a,Vec3 b,Vec3 u,Vec3 v,int color){
        Vec3[] p={a.add(u).add(v),a.subtract(u).add(v),a.subtract(u).subtract(v),a.add(u).subtract(v),b.add(u).add(v),b.subtract(u).add(v),b.subtract(u).subtract(v),b.add(u).subtract(v)};
        for(int i=0;i<4;i++){int j=(i+1)%4;for(int k:new int[]{i,j,j+4,i+4}){var q=p[k];out.addVertex(pose,(float)q.x,(float)q.y,(float)q.z).setColor(color);}}
    }
}
