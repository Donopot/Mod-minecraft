package net.mcreator.minepiece.client.model;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
/** Reserved mesh coordinates only, converted to current native builders. No Forge runtime. */
public final class SourcePriorityMeshes {
public static LayerDefinition mochifist(){
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-16.4167f, -36.0f, -12.0f, 30.0f, 46.0f, 30.0f, new CubeDeformation(0.0f)).texOffs(72, 76).addBox(-14.4167f, 10.0f, -10.0f, 26.0f, 55.0f, 26.0f, new CubeDeformation(0.0f)).texOffs(0, 76).addBox(-10.4167f, 65.0f, -6.0f, 18.0f, 96.0f, 18.0f, new CubeDeformation(0.0f)).texOffs(120, 0).addBox(-16.4167f, -53.0f, -4.0f, 30.0f, 17.0f, 22.0f, new CubeDeformation(0.0f)).texOffs(120, 39).addBox(1.5833f, -53.0f, -12.0f, 12.0f, 17.0f, 8.0f, new CubeDeformation(0.0f)).texOffs(0, 0).addBox(-5.4167f, -53.0f, -12.0f, 7.0f, 9.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-1.5833f, (float)77.0f, (float)-1.0f, (float)0.0f, (float)1.5708f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)256, (int)256);
 }
public static LayerDefinition yakimochi(){
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -8.0f, -2.0f, 4.0f, 5.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(10, 9).addBox(-2.0f, -10.0f, -2.0f, 1.0f, 1.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(0, 0).addBox(-2.0f, -9.0f, -2.0f, 1.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(0, 9).addBox(-1.0f, -10.0f, -2.0f, 3.0f, 2.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)30.0f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)16, (int)16);
 }
public static LayerDefinition paw(){
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 4.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(0, 12).addBox(3.0f, -1.0f, -1.0f, 2.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(8, 8).addBox(2.0f, -1.0f, 2.0f, 2.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(0, 8).addBox(2.0f, -1.0f, -4.0f, 2.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)24.0f, (float)0.0f, (float)1.5708f, (float)0.0f, (float)-1.5708f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)16, (int)16);
 }
}
