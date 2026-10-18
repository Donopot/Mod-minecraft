package net.mcreator.minepiece.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.mcreator.minepiece.client.model.ModelhitoHitoDaibutsuHumanbeast;
import net.mcreator.minepiece.client.model.ModelhitoHitoDaibutsuHumanbeast2;
import net.mcreator.minepiece.client.model.ModelinuInuOkuchinomakamiHumanbeast;
import net.mcreator.minepiece.client.model.ModelinuInuWolfHumanbeast;
import net.mcreator.minepiece.client.model.ModelnekonekoLeopardHumanbeast;
import net.mcreator.minepiece.client.model.ModelnekonekoLeopardHumanbeast2;
import net.mcreator.minepiece.client.model.ModelnekonekoSabertigerHumanbeast;
import net.mcreator.minepiece.client.model.Modeloverlay;
import net.mcreator.minepiece.client.model.ModelryuRyuBrachiosaurusHumanbeast;
import net.mcreator.minepiece.client.model.ModelryuRyuBrachiosaurusHumanbeast2;
import net.mcreator.minepiece.client.model.ModelryuRyuPachycephalosaurusHumanbeast;
import net.mcreator.minepiece.client.model.ModelryuRyuPachycephalosaurusHumanbeast2;
import net.mcreator.minepiece.client.model.ModelryuRyuPteranodonHumanbeast;
import net.mcreator.minepiece.client.model.ModeltoritoriPhoenixHumanbeast;
import net.mcreator.minepiece.client.model.ModeltoritoriPhoenixHumanbeastFly;
import net.mcreator.minepiece.client.model.ModeluouoSeiryuHumanBeast;
import net.mcreator.minepiece.client.model.ModelushiUshiGiraffeHumanbeast;
import net.mcreator.minepiece.client.model.ModelushiUshiGiraffeHumanbeast2;
import net.mcreator.minepiece.client.model.ModelwingsBatBat;
import net.mcreator.minepiece.client.model.ModelmoriMoriFlower;
import net.mcreator.minepiece.client.model.ModelsupasupaForm2;
import net.mcreator.minepiece.client.model.ModelsupasupaForm22;
import net.mcreator.minepiece.client.model.Modelpaws;
import net.mcreator.minepiece.client.model.ModelpawsSlim;
import net.mcreator.minepiece.client.model.ModelhornsYamato;
import net.mcreator.minepiece.client.model.ModelhornsKaido;
import net.mcreator.minepiece.client.model.ModelenelDrum;
import net.mcreator.minepiece.client.model.ModelfeatherRobe;
import net.mcreator.minepiece.client.model.ModelinuInuWolfHumanbeastSlim;
import net.mcreator.minepiece.client.model.ModeloverlaySlim;
import net.minecraft.world.entity.player.PlayerModelType;
import net.mcreator.minepiece.client.model.ModelwingsBatBatFly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * Couche de rendu de la transformation Zoan â€” port de Kleiders Custom Renderer.
 *
 * <p>Quand le joueur local est transformÃ© (forme 1) et porte un fruit Zoan, cette couche superpose le
 * rendu Â« full beast Â» du fruit : le modÃ¨le humanbeast (texture du fruit), puis le corps Â« humanbeast2 Â»
 * (texture = peau du joueur) et/ou l'overlay (texture {@code transform_*}), les ailes ou la forme vol.
 * L'Ã©tat (the avatar render state) est synchronisÃ© du serveur.</p>
 *
 * <p>CORE-019 : rendu empilÃ© fidÃ¨le Ã  ver15 (KleidersRenderEventProcedure) â€” chaque forme hybride empile
 * jusqu'Ã  3 modÃ¨les ; les modÃ¨les Â« 2 Â» utilisent la peau du joueur ({@code state.skin.body().texturePath()}).</p>
 */
public class PlayerTransformLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

   /** ModÃ¨le parent (PlayerModel) courant â€” alimentÃ© avant le rendu, lu par le setupAnim des modÃ¨les. */


   /** Ã‰tat de transformation synchronisÃ© du serveur. */



   /** Couche empilÃ©e : modÃ¨le + texture (null = peau du joueur). */
   private static final class StackedLayer {
      final EntityModel<EntityRenderState> model;
      final Identifier texture;
      StackedLayer(EntityModel<EntityRenderState> model, Identifier texture) {
         this.model = model;
         this.texture = texture;
      }
   }

   private final net.mcreator.minepiece.client.model.GomuGearModel gomu,haki,cloud;
   private final net.mcreator.minepiece.client.model.GearCloudCollar cloudCollar=new net.mcreator.minepiece.client.model.GearCloudCollar();
   private final net.mcreator.minepiece.client.model.AmaruModel amaru;
   private final Map<String,net.mcreator.minepiece.client.model.SourceRigModel> priorityRigs=new HashMap<>();
   private final ModelenelDrum amaruDrums;
   private static final Identifier AMARU_BODY=tex("amaru_body");
   private static final Identifier AMARU_ENERGY=tex("amaru_energy");

   private final Map<String, List<StackedLayer>> stacks = new HashMap<>();
   private final Map<String,net.mcreator.minepiece.client.model.AncientBeastModel> ancientBeasts=new HashMap<>();
   private final net.mcreator.minepiece.client.model.AncientBeastModel dome;
   private final net.mcreator.minepiece.client.model.KaidoDragonModel kaidoDragon;
   private final Map<String,net.mcreator.minepiece.client.model.MythicalBeastModel> mythicalBeasts=new HashMap<>();
   private final net.mcreator.minepiece.client.model.MythicalBeastModel makamiMane,makamiFace,dragonTrim,phoenixFlames,phoenixWings;
   private final Map<String,net.mcreator.minepiece.client.model.LandBeastModel> beasts=new HashMap<>();
   private final net.mcreator.minepiece.client.model.LandBeastModel wolfFace,saberFangs,saberMarks;

   /** Variantes Â« slim Â» : un mÃªme fruit peut avoir un modÃ¨le diffÃ©rent selon la peau du joueur. */
   private final Map<String, List<StackedLayer>> slimStacks = new HashMap<>();

   public PlayerTransformLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
      super(renderer);
      EntityModelSet es = Minecraft.getInstance().getEntityModels();
      for(String rig:new String[]{"amaru","torifullform","buddha","wolfbeast","leopardbeastrework"})
         priorityRigs.put(rig,new net.mcreator.minepiece.client.model.SourceRigModel(rig));
      kaidoDragon=new net.mcreator.minepiece.client.model.KaidoDragonModel();
      phoenixWings=new net.mcreator.minepiece.client.model.MythicalBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.PHOENIX_WINGS),8);
      makamiFace=new net.mcreator.minepiece.client.model.MythicalBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.MAKAMI_FACE),6);
      ancientBeasts.put("ryu_ryu_brachiosaurus",new net.mcreator.minepiece.client.model.AncientBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.AncientBeastModel.BRACHIO),0));
      ancientBeasts.put("ryu_ryu_pachycephalosaurus",new net.mcreator.minepiece.client.model.AncientBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.AncientBeastModel.PACHY),1));
      dome=new net.mcreator.minepiece.client.model.AncientBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.AncientBeastModel.DOME),2);
      mythicalBeasts.put("inu_inu_okuchi_no_makami",new net.mcreator.minepiece.client.model.MythicalBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.MAKAMI),0));
      mythicalBeasts.put("hito_hito_daibutsu",new net.mcreator.minepiece.client.model.MythicalBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.DAIBUTSU),1));
      mythicalBeasts.put("tori_tori_phoenix",new net.mcreator.minepiece.client.model.MythicalBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.PHOENIX),2));
      mythicalBeasts.put("uo_uo_seiryu",new net.mcreator.minepiece.client.model.MythicalBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.DRAGON),4));
      mythicalBeasts.put("ryu_ryu_pteranodon",new net.mcreator.minepiece.client.model.MythicalBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.PTERO),3));
      makamiMane=new net.mcreator.minepiece.client.model.MythicalBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.MAKAMI_MANE),6);
      dragonTrim=new net.mcreator.minepiece.client.model.MythicalBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.DRAGON_TRIM),7);
      phoenixFlames=new net.mcreator.minepiece.client.model.MythicalBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.PHOENIX),5);
      beasts.put("inu_inu_wolf",new net.mcreator.minepiece.client.model.LandBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.LandBeastModel.WOLF),0));
      beasts.put("neko_neko_leopard",new net.mcreator.minepiece.client.model.LandBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.LandBeastModel.LEOPARD),1));
      beasts.put("neko_neko_sabertiger",new net.mcreator.minepiece.client.model.LandBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.LandBeastModel.LEOPARD),2));
      beasts.put("ushi_ushi_giraffe",new net.mcreator.minepiece.client.model.LandBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.LandBeastModel.GIRAFFE),3));
      beasts.put("batto_batto",new net.mcreator.minepiece.client.model.LandBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.LandBeastModel.BAT),4));
      wolfFace=new net.mcreator.minepiece.client.model.LandBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.LandBeastModel.WOLF_FACE),0);
      saberFangs=new net.mcreator.minepiece.client.model.LandBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.LandBeastModel.SABER_FANGS),2);
      saberMarks=new net.mcreator.minepiece.client.model.LandBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.LandBeastModel.SABER_MARKS),2);

      gomu=new net.mcreator.minepiece.client.model.GomuGearModel(es.bakeLayer(net.mcreator.minepiece.client.model.GomuGearModel.BODY),0);
      haki=new net.mcreator.minepiece.client.model.GomuGearModel(es.bakeLayer(net.mcreator.minepiece.client.model.GomuGearModel.BODY),1);
      cloud=new net.mcreator.minepiece.client.model.GomuGearModel(es.bakeLayer(net.mcreator.minepiece.client.model.GomuGearModel.CLOUD),2);
      amaru=new net.mcreator.minepiece.client.model.AmaruModel(es.bakeLayer(net.mcreator.minepiece.client.model.AmaruModel.LAYER_LOCATION));
      amaruDrums=new ModelenelDrum(es.bakeLayer(ModelenelDrum.LAYER_LOCATION));
      amaruDrums.drum.z+=6; // Keep the four Enel drums behind the broadened electric torso.

      // --- Loup (Inu Inu no Mi, modÃ¨le Wolf) : humanbeast + overlay ---
      stack("minepiece:item_devil_fruit_inu_inu_wolf",
         new ModelinuInuWolfHumanbeast(es.bakeLayer(ModelinuInuWolfHumanbeast.LAYER_LOCATION)), tex("inu_inu_wolf_humanbeast"),
         new Modeloverlay(es.bakeLayer(Modeloverlay.LAYER_LOCATION)), tex("transform_inu_inu_wolf_humanbeast"));

      // --- LÃ©opard (Neko Neko, modÃ¨le Leopard) : humanbeast + corps beast (peau) ---
      stack("minepiece:item_devil_fruit_neko_neko_leopard",
         new ModelnekonekoLeopardHumanbeast(es.bakeLayer(ModelnekonekoLeopardHumanbeast.LAYER_LOCATION)), tex("nekoneko_leopard_humanbeast"),
         new ModelnekonekoLeopardHumanbeast2(es.bakeLayer(ModelnekonekoLeopardHumanbeast2.LAYER_LOCATION)), null);

      // --- Tigre Ã  dents de sabre : humanbeast + corps beast (rÃ©utilise le lÃ©opard) ---
      stack("minepiece:item_devil_fruit_neko_neko_sabertiger",
         new ModelnekonekoSabertigerHumanbeast(es.bakeLayer(ModelnekonekoSabertigerHumanbeast.LAYER_LOCATION)), tex("nekoneko_sabertiger_humanbeast"),
         new ModelnekonekoLeopardHumanbeast2(es.bakeLayer(ModelnekonekoLeopardHumanbeast2.LAYER_LOCATION)), null);

      // --- Girafe (Ushi Ushi) : humanbeast + corps beast ---
      stack("minepiece:item_devil_fruit_ushi_ushi_giraffe",
         new ModelushiUshiGiraffeHumanbeast(es.bakeLayer(ModelushiUshiGiraffeHumanbeast.LAYER_LOCATION)), tex("ushi_ushi_giraffe_humanbeast"),
         new ModelushiUshiGiraffeHumanbeast2(es.bakeLayer(ModelushiUshiGiraffeHumanbeast2.LAYER_LOCATION)), null);

      // --- PhÃ©nix (Tori Tori) : humanbeast + forme vol ---
      stack("minepiece:item_devil_fruit_tori_tori_phoenix",
         new ModeltoritoriPhoenixHumanbeast(es.bakeLayer(ModeltoritoriPhoenixHumanbeast.LAYER_LOCATION)), tex("toritori_phoenix_humanbeast"),
         new ModeltoritoriPhoenixHumanbeastFly(es.bakeLayer(ModeltoritoriPhoenixHumanbeastFly.LAYER_LOCATION)), tex("toritori_phoenix_humanbeast"),
         new ModelfeatherRobe(es.bakeLayer(ModelfeatherRobe.LAYER_LOCATION)), tex("feather_robe_white"));

      // --- Daibutsu (Hito Hito) : humanbeast + corps beast (peau) ---
      stack("minepiece:item_devil_fruit_hito_hito_daibutsu",
         new ModelhitoHitoDaibutsuHumanbeast(es.bakeLayer(ModelhitoHitoDaibutsuHumanbeast.LAYER_LOCATION)), tex("transform_hito_hito_daibutsu_humanbeast"),
         new ModelhitoHitoDaibutsuHumanbeast2(es.bakeLayer(ModelhitoHitoDaibutsuHumanbeast2.LAYER_LOCATION)), null);

      // --- Seiryu (Uo Uo, dragon de Kaidou) : human_beast + overlay (peau de dragon) ---
      stack("minepiece:item_devil_fruit_uo_uo_seiryu",
         new ModeluouoSeiryuHumanBeast(es.bakeLayer(ModeluouoSeiryuHumanBeast.LAYER_LOCATION)), tex("uouo_dragon_human_beast"),
         new Modeloverlay(es.bakeLayer(Modeloverlay.LAYER_LOCATION)), tex("uouo_dragon_human_beast_skin"));

      // --- Brachiosaure (Ryu Ryu) : humanbeast + corps beast ---
      stack("minepiece:item_devil_fruit_ryu_ryu_brachiosaurus",
         new ModelryuRyuBrachiosaurusHumanbeast(es.bakeLayer(ModelryuRyuBrachiosaurusHumanbeast.LAYER_LOCATION)), tex("ryu_ryu_brachiosaurus_humanbeast"),
         new ModelryuRyuBrachiosaurusHumanbeast2(es.bakeLayer(ModelryuRyuBrachiosaurusHumanbeast2.LAYER_LOCATION)), null);

      // --- PachycÃ©phalosaure (Ryu Ryu) : humanbeast + corps beast + overlay ---
      stack("minepiece:item_devil_fruit_ryu_ryu_pachycephalosaurus",
         new ModelryuRyuPachycephalosaurusHumanbeast(es.bakeLayer(ModelryuRyuPachycephalosaurusHumanbeast.LAYER_LOCATION)), tex("ryu_ryu_pachycephalosaurus_humanbeast"),
         new ModelryuRyuPachycephalosaurusHumanbeast2(es.bakeLayer(ModelryuRyuPachycephalosaurusHumanbeast2.LAYER_LOCATION)), null,
         new Modeloverlay(es.bakeLayer(Modeloverlay.LAYER_LOCATION)), tex("transform_ryu_ryu_pachycephalosaurus_humanbeast"));

      // --- PtÃ©ranodon (Ryu Ryu) : humanbeast ---
      stack("minepiece:item_devil_fruit_ryu_ryu_pteranodon",
         new ModelryuRyuPteranodonHumanbeast(es.bakeLayer(ModelryuRyuPteranodonHumanbeast.LAYER_LOCATION)), tex("ryu_ryu_pteranodon_humanbeast"));

      // --- Okuchi no Makami (Inu Inu, Yamato) : humanbeast ---
      stack("minepiece:item_devil_fruit_inu_inu_okuchi_no_makami",
         new ModelinuInuOkuchinomakamiHumanbeast(es.bakeLayer(ModelinuInuOkuchinomakamiHumanbeast.LAYER_LOCATION)), tex("inu_inu_okuchinomakami_humanbeast"),
         new ModelfeatherRobe(es.bakeLayer(ModelfeatherRobe.LAYER_LOCATION)),tex("feather_robe_lightblue"));


      // =====================================================================================
      // ModÃ¨les extraits du jar source ver15 (KleidersRenderEventProcedure) â€” les 5 visuels de
      // transformation qui manquaient : Supa Supa, Nikyu Nikyu, Mori Mori, Goro Goro, Kira Kira.
      // =====================================================================================

      // --- Supa Supa (Mr. 1) : deux couches (lames + corps) ---
      stack("minepiece:item_devil_fruit_supa_supa",
         new ModelsupasupaForm2(es.bakeLayer(ModelsupasupaForm2.LAYER_LOCATION)), tex("supasupa_form2"),
         new ModelsupasupaForm22(es.bakeLayer(ModelsupasupaForm22.LAYER_LOCATION)), tex("supasupa_form2"));

      // --- Nikyu Nikyu (Kuma) : pattes (le joueur Â« slim Â» a son propre modÃ¨le) ---
      stack("minepiece:item_devil_fruit_nikyu_nikyu",
         new Modelpaws(es.bakeLayer(Modelpaws.LAYER_LOCATION)), tex("paws"));
      stackSlim("minepiece:item_devil_fruit_nikyu_nikyu",
         new ModelpawsSlim(es.bakeLayer(ModelpawsSlim.LAYER_LOCATION)), tex("paws"));

      // --- Mori Mori (Aramaki) : fleur ---
      stack("minepiece:item_devil_fruit_mori_mori",
         new ModelmoriMoriFlower(es.bakeLayer(ModelmoriMoriFlower.LAYER_LOCATION)), tex("mori_mori_flower"));

      // --- Goro accessory only: no Yamato horns or unsupported Amaru transformation. ---
      stack("minepiece:item_devil_fruit_goro_goro",
         new ModelenelDrum(es.bakeLayer(ModelenelDrum.LAYER_LOCATION)), tex("enel_drum"));

      // --- Kira Kira (Kizaru) : pas de modÃ¨le propre en ver15, la peau overlay porte le rendu ---
      stack("minepiece:item_devil_fruit_kira_kira",
         new Modeloverlay(es.bakeLayer(Modeloverlay.LAYER_LOCATION)), tex("kirakira_form2"));

      // --- Inu Inu Wolf : variante Â« slim Â» dÃ©diÃ©e (le source a bien deux modÃ¨les) ---
      stackSlim("minepiece:item_devil_fruit_inu_inu_wolf",
         new ModelinuInuWolfHumanbeastSlim(es.bakeLayer(ModelinuInuWolfHumanbeastSlim.LAYER_LOCATION)), tex("inu_inu_wolf_humanbeast"),
         new ModeloverlaySlim(es.bakeLayer(ModeloverlaySlim.LAYER_LOCATION)), tex("transform_inu_inu_wolf_humanbeast"));

      // --- PhÃ©nix : la robe de plumes fait partie de la pile en ver15 ---
      // (ajoutÃ©e Ã  la pile existante du PhÃ©nix : la source superpose toujours Modelfeather_robe)

      // --- Chauve-souris (Batto Batto) : ailes repliÃ©es + ailes vol ---
      stack("minepiece:item_devil_fruit_batto_batto",
         new ModelwingsBatBat(es.bakeLayer(ModelwingsBatBat.LAYER_LOCATION)), tex("wing_bat_bat"),
         new ModelwingsBatBatFly(es.bakeLayer(ModelwingsBatBatFly.LAYER_LOCATION)), tex("wing_bat_bat"));
   }

   /** Only complete hybrid stacks replace the vanilla body; horns, paws, wings and overlays add to it. */
   public static boolean replacesBody(String fruit) {
      if(fruit.equals(net.mcreator.minepiece.procedures.GomuGears.FRUIT))return true;
      return switch (fruit) {
         case "minepiece:item_devil_fruit_goro_goro", "minepiece:item_devil_fruit_inu_inu_wolf", "minepiece:item_devil_fruit_neko_neko_leopard",
              "minepiece:item_devil_fruit_neko_neko_sabertiger", "minepiece:item_devil_fruit_ushi_ushi_giraffe",
              "minepiece:item_devil_fruit_hito_hito_daibutsu", "minepiece:item_devil_fruit_uo_uo_seiryu",
              "minepiece:item_devil_fruit_ryu_ryu_brachiosaurus", "minepiece:item_devil_fruit_ryu_ryu_pachycephalosaurus",
              "minepiece:item_devil_fruit_inu_inu_okuchi_no_makami"
            -> true;
         default -> false;
      };
   }

   private static Identifier tex(String name) {
      return Identifier.fromNamespaceAndPath("minepiece", "textures/entities/" + name + ".png");
   }

   /**
    * Variante des joueurs au modÃ¨le <b>fin</b> (Â« slim Â» / Alex).
    *
    * <p>La source ver15 possÃ¨de deux modÃ¨les pour certains visuels selon le modÃ¨le de peau du joueur
    * ({@code getModelName().equals("slim")}), et n'en utilise qu'un : superposer le modÃ¨le Â« large Â» Ã 
    * une peau fine dÃ©forme le rendu. On garde donc les deux et on choisit Ã  l'affichage.</p>
    */
   private void stackSlim(String fruitId, Object... layers) {
      List<StackedLayer> list = new ArrayList<>();
      for (int i = 0; i < layers.length; i += 2) {
         EntityModel<EntityRenderState> model = (EntityModel<EntityRenderState>) layers[i];
         Identifier texture = (Identifier) layers[i + 1];
         list.add(new StackedLayer(model, texture));
      }
      slimStacks.put(fruitId, list);
   }

   private void stack(String fruitId, Object... layers) {
      List<StackedLayer> list = new ArrayList<>();
      for (int i = 0; i < layers.length; i += 2) {
         EntityModel<EntityRenderState> model = (EntityModel<EntityRenderState>) layers[i];
         Identifier texture = (Identifier) layers[i + 1];
         list.add(new StackedLayer(model, texture));
      }
      stacks.put(fruitId, list);
   }

   private void stoneAware(EntityModel<EntityRenderState> model,Identifier texture,PoseStack stack,SubmitNodeCollector collector,int light,AvatarRenderState state,int color,int order){
      if(((net.mcreator.minepiece.client.ParameciaRenderData)state).minepiece$stone()){texture=tex("tex_stone");color=0xFFBFC2C8;}
      renderColoredCutoutModel(model,texture,stack,collector,light,state,color,order);
   }

   @Override
   public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
      net.mcreator.minepiece.client.TransformRenderData data = (net.mcreator.minepiece.client.TransformRenderData) state;
      double playerForm = data.minepiece$form();
      String fruitId = data.minepiece$fruit();
      int material=data.minepiece$material();
      if(material!=0){fruitId=(material&1)!=0?"minepiece:item_devil_fruit_kira_kira":"minepiece:item_devil_fruit_supa_supa";playerForm=1;}
      if (playerForm <= 0.0 || (state.isInvisible && state.isInvisibleToPlayer && !state.appearsGlowing())) {
         return;
      }
      if(fruitId.equals(net.mcreator.minepiece.procedures.GomuGears.FRUIT)){
         data.minepiece$setPoses(net.mcreator.minepiece.client.TransformPoses.capture(getParentModel()));
         if(state.skin!=null)stoneAware(gomu,state.skin.body().texturePath(),poseStack,collector,lightCoords,state,playerForm==2?0xFFFF9D9D:-1,0);
         if(playerForm==4)stoneAware(haki,tex("tex_white2"),poseStack,collector,lightCoords,state,0xFF292128,1);
         if(playerForm==5){stoneAware(cloud,tex("tex_white3"),poseStack,collector,lightCoords,state,-1,1);stoneAware(cloudCollar,Identifier.fromNamespaceAndPath("minepiece","upgrade24/cloudring2.png"),poseStack,collector,lightCoords,state,-1,1);}
         net.mcreator.minepiece.client.Batch05VisualLab.rendered("gear-"+(int)playerForm);return;
      }
      String beastKey=fruitId.replace("minepiece:item_devil_fruit_","");
      String priority=beastKey.equals("goro_goro")?"amaru":playerForm==2?switch(beastKey){
         case "tori_tori_phoenix"->"torifullform";case "hito_hito_daibutsu"->"buddha";
         case "inu_inu_wolf"->"wolfbeast";case "neko_neko_leopard"->"leopardbeastrework";default->"";
      }:"";
      if(!priority.isEmpty()){
         data.minepiece$setPoses(net.mcreator.minepiece.client.TransformPoses.capture(getParentModel()));
         float scale=switch(priority){case "amaru"->.285F;case "buddha"->.797F;case "torifullform"->.55F;case "leopardbeastrework"->.794F;default->1.01F;};
         poseStack.pushPose();poseStack.translate(0,1.5,0);poseStack.scale(scale,scale,scale);poseStack.translate(0,-1.5,0);
         var model=priorityRigs.get(priority);var texture=Identifier.fromNamespaceAndPath("minepiece","upgrade24/"+priority+".png");
         stoneAware(model,texture,poseStack,collector,priority.equals("amaru")?15728880:lightCoords,state,-1,0);
         if(!((net.mcreator.minepiece.client.ParameciaRenderData)state).minepiece$stone() && (priority.equals("amaru")||priority.equals("leopardbeastrework"))){
            var glow=Identifier.fromNamespaceAndPath("minepiece","upgrade24/"+priority+"_glow.png");
            collector.order(1).submitModel(model,state,poseStack,net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucentEmissive(glow),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,state.isInvisible?0x25FFFFFF:-1,null,state.outlineColor);
         }
         poseStack.popPose();
         if(playerForm==2){net.mcreator.minepiece.client.Batch06VisualLab.rendered("beast-"+fruitId);net.mcreator.minepiece.client.Batch07VisualLab.rendered("beast-"+fruitId);}
         return;
      }
            if(playerForm==2 && ancientBeasts.containsKey(beastKey)){
         data.minepiece$setPoses(net.mcreator.minepiece.client.TransformPoses.capture(getParentModel()));
         stoneAware(ancientBeasts.get(beastKey),beastKey.equals("ryu_ryu_brachiosaurus")?tex("b08_brachiosaurus_guard"):tex("tex_white3"),poseStack,collector,lightCoords,state,beastKey.equals("ryu_ryu_brachiosaurus")?-1:0xFF6EABC9,0);
         if(beastKey.equals("ryu_ryu_pachycephalosaurus"))stoneAware(dome,tex("tex_white3"),poseStack,collector,lightCoords,state,0xFFF0D8E4,1);
         net.mcreator.minepiece.client.Batch08VisualLab.rendered("beast-"+fruitId);return;
      }
      if(playerForm==2 && mythicalBeasts.containsKey(beastKey)){
         net.mcreator.minepiece.client.Batch07VisualLab.rendered("beast-"+fruitId);
         data.minepiece$setPoses(net.mcreator.minepiece.client.TransformPoses.capture(getParentModel()));
         Identifier texture=switch(beastKey){case "hito_hito_daibutsu"->tex("b07_daibutsu");case "tori_tori_phoenix"->tex("b07_phoenix_fly");case "ryu_ryu_pteranodon"->tex("b07_pteranodon_fly");default->tex("tex_white3");};
         int color=-1;
         if(beastKey.equals("uo_uo_seiryu")){poseStack.pushPose();poseStack.translate(0,1.5,0);poseStack.scale(.35F,.35F,.35F);poseStack.translate(0,-1.5,0);stoneAware(kaidoDragon,tex("kaidou_beast"),poseStack,collector,lightCoords,state,-1,0);poseStack.popPose();return;}
         stoneAware(mythicalBeasts.get(beastKey),texture,poseStack,collector,lightCoords,state,color,0);
         if(beastKey.equals("inu_inu_okuchi_no_makami"))stoneAware(makamiFace,tex("tex_white3"),poseStack,collector,lightCoords,state,0xFF342F3D,2);
         if(beastKey.equals("inu_inu_okuchi_no_makami"))stoneAware(makamiMane,tex("tex_white3"),poseStack,collector,15728880,state,0xFF8AE6F7,1);

         if(beastKey.equals("tori_tori_phoenix"))stoneAware(phoenixWings,tex("tex_white3"),poseStack,collector,15728880,state,0xFF40E2FF,2);
         if(beastKey.equals("tori_tori_phoenix"))stoneAware(phoenixFlames,tex("b07_phoenix_flame_"+((int)(state.ageInTicks/3)%8)),poseStack,collector,15728880,state,-1,1);
         return;
      }
      if(playerForm==2 && beasts.containsKey(beastKey)){
         data.minepiece$setPoses(net.mcreator.minepiece.client.TransformPoses.capture(getParentModel()));
         Identifier texture=beastKey.equals("ushi_ushi_giraffe")?tex("b06_giraffe_walk"):beastKey.equals("neko_neko_leopard")?tex("b06_leopard_walk"):tex("tex_white3");
         int color=beastKey.equals("inu_inu_wolf")?0xFFC3C5CA:beastKey.equals("batto_batto")?0xFF483448:beastKey.equals("neko_neko_sabertiger")?0xFFEBA348:-1;
         stoneAware(beasts.get(beastKey),texture,poseStack,collector,lightCoords,state,color,0);
         net.mcreator.minepiece.client.Batch06VisualLab.rendered("beast-"+fruitId);
         if(beastKey.equals("inu_inu_wolf"))stoneAware(wolfFace,tex("tex_white2"),poseStack,collector,lightCoords,state,0xFF28232A,1);
         if(beastKey.equals("neko_neko_sabertiger"))stoneAware(saberFangs,tex("tex_white3"),poseStack,collector,lightCoords,state,-1,1);
         if(beastKey.equals("neko_neko_sabertiger"))stoneAware(saberMarks,tex("tex_white3"),poseStack,collector,lightCoords,state,0xFF242029,2);
         return;
      }
      if (fruitId.equals(net.mcreator.minepiece.procedures.GoroAmaru.FRUIT)) {
         data.minepiece$setPoses(net.mcreator.minepiece.client.TransformPoses.capture(getParentModel()));
         if(((net.mcreator.minepiece.client.ParameciaRenderData)state).minepiece$stone()){stoneAware(amaru,tex("tex_stone"),poseStack,collector,lightCoords,state,-1,0);stoneAware(amaruDrums,tex("tex_stone"),poseStack,collector,lightCoords,state,-1,1);return;}
         int alpha=state.isInvisible?0x25000000:0xFF000000;
         collector.order(0).submitModel(amaru,state,poseStack,net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucentEmissive(AMARU_BODY),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,alpha|0xB3EBFF,null,state.outlineColor);
         collector.order(1).submitModel(amaru,state,poseStack,net.minecraft.client.renderer.rendertype.RenderTypes.energySwirl(AMARU_ENERGY,state.ageInTicks*.009F,state.ageInTicks*.006F),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,(state.isInvisible?0x0F66CCFF:0x5066CCFF),null,state.outlineColor);
         if(!state.isInvisible)stoneAware(amaruDrums,tex("enel_drum"),poseStack,collector,15728880,state,0xFF85E9FF,2);
         return;
      }
      List<StackedLayer> layers = null;
      if (state.skin != null && state.skin.model() == PlayerModelType.SLIM) {
         layers = slimStacks.get(fruitId);
      }
      if (layers == null) {
         layers = stacks.get(fruitId);
      }
      if (layers == null) {
         return;
      }
      if(material!=0)net.mcreator.minepiece.client.Batch03VisualLab.rendered((material&1)!=0?"b03_diamond_coat":"b03_blades_coat");
      Identifier skin = state.skin != null ? state.skin.body().texturePath() : null;
      int order = 0;
      net.mcreator.minepiece.client.Batch06VisualLab.rendered("hybrid-"+fruitId);net.mcreator.minepiece.client.Batch07VisualLab.rendered("hybrid-"+fruitId);net.mcreator.minepiece.client.Batch08VisualLab.rendered("hybrid-"+fruitId);
      for (StackedLayer layer : layers) {
         if(fruitId.equals("minepiece:item_devil_fruit_batto_batto") && ((layer.model instanceof ModelwingsBatBatFly)!=data.minepiece$airborne()))continue;
         if(fruitId.equals("minepiece:item_devil_fruit_tori_tori_phoenix") && ((layer.model instanceof ModeltoritoriPhoenixHumanbeastFly && !data.minepiece$airborne())||(layer.model instanceof ModeltoritoriPhoenixHumanbeast && data.minepiece$airborne())))continue;
         Identifier texture = layer.texture != null ? layer.texture : skin;
         if (texture == null) {
            continue;
         }
         data.minepiece$setPoses(net.mcreator.minepiece.client.TransformPoses.capture(getParentModel()));
         stoneAware(layer.model, texture, poseStack, collector, lightCoords, state, -1, order);
         order++;
      }
   }
}
