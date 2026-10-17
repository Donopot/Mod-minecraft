package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import net.mcreator.minepiece.entity.SummonEntity;
import br.com.oneminepiece.opworldlike.gravity.DevilFruitGravityConfig;
import br.com.oneminepiece.opworldlike.love.DevilFruitLoveConfig;
import br.com.oneminepiece.opworldlike.hollow.DevilFruitHollowConfig;
import br.com.oneminepiece.opworldlike.wolf.DevilFruitWolfConfig;
import br.com.oneminepiece.opworldlike.kage.DevilFruitKageConfig;
import net.mcreator.minepiece.entity.SkillProjectileEntity;
import net.mcreator.minepiece.init.MinepieceModItems;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comportements <b>propres à un Fruit</b>, là où le moteur générique ne suffit pas
 * (actions {@code FRUIT-*-TECH-*-DEV} du plan : « ne pas conserver seulement un nom et des dégâts
 * différents »).
 *
 * <p>Le point d'entrée {@link #customCast} est appelé par le lanceur avant la résolution générique.
 * S'il retourne {@code true}, le comportement spécifique a pris en charge la technique et le moteur
 * générique ne s'exécute pas. Chaque entrée est adossée au contrat du Fruit concerné
 * ({@code docs/fruits/&lt;ID&gt;.md}).</p>
 */
public final class DevilFruitBehaviours {

   private static final Logger LOGGER = LoggerFactory.getLogger("MinePieceDevilFruit");

   /** Pas de progression du déplacement (blocs) : assez fin pour ne jamais traverser un mur d'1 bloc. */
   private static final double STEP = 0.25;

   private DevilFruitBehaviours() {
   }

   public static CustomCastResult dispatch(ServerPlayer player, ItemStack fruit, DevilFruitMove move) {
      if (customCast(player, fruit, move)) return CustomCastResult.EXECUTED;
      return CustomTechniqueRoutes.handles(fruit, move) ? CustomCastResult.REFUSED : CustomCastResult.NOT_HANDLED;
   }

   /**
    * Point d'entrée. Retourne vrai si le comportement propre au Fruit a traité la technique.
    *
    * @param player le lanceur
    * @param fruit  le Fruit du Démon équipé
    * @param move   la technique sélectionnée
    */
   public static boolean customCast(ServerPlayer player, ItemStack fruit, DevilFruitMove move) {
      if(MythicalZoanTechniques.handles(fruit))return MythicalZoanTechniques.cast(player,fruit,move);
      if(ZoanTechniqueEpisodes.handles(fruit))return ZoanTechniqueEpisodes.cast(player,fruit,move);
      if(MythicBatchTechniques.handles(fruit))return MythicBatchTechniques.cast(player,fruit,move);
      if(ParameciaBatchTechniques.handles(fruit))return ParameciaBatchTechniques.cast(player,fruit,move);
      if(MaterialBatchTechniques.handles(fruit))return MaterialBatchTechniques.cast(player,fruit,move);
      if(ControlledBatchTechniques.handles(fruit))return ControlledBatchTechniques.cast(player,fruit,move);
      if(ElementalBatchTechniques.handles(fruit))return ElementalBatchTechniques.cast(player,fruit,move);
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_ARO_ARO)) {
         return switch (move.id()) {
            case "move" -> aroMoveDash(player, move);
            default -> false;
         };
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_RIKI_RIKI)) {
         return rikiRiki(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_SORU_SORU)) {
         return soruSoru(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_SUPA_SUPA)) {
         return supaSupa(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_UTA_UTA)) {
         return utaUta(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_NIKYU_NIKYU)) {
         return nikyuNikyu(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_YAMI_YAMI)) {
         return yamiYami(player, fruit, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA)) {
         return pikaPika(player, fruit, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE)) {
         return opeOpe(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_MORI_MORI)) {
         return moriMori(player, fruit, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_MOKU_MOKU)) {
         return mokuMoku(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_MOCHI_MOCHI)) {
         return mochiMochi(player, fruit, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_KIRO_KIRO)) {
         return kiroKiro(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_KIRA_KIRA)) {
         return kiraKira(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_KAGE_KAGE)) {
         return kageKage(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_WOLF)) {
         return inuWolf(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI)) {
         return okuchi(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_IMU)) {
         return imu(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_HORO_HORO)) {
         return horoHoro(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_HITO_HITO_DAIBUTSU)) {
         return hitoHitoDaibutsu(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_MERO_MERO)) {
         return meroMero(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_ZUSHI_ZUSHI)) {
         return zushiZushi(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_SUNA_SUNA)) {
         return sunaSuna(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA)) {
         return meraMera(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_MAGU_MAGU)) {
         return maguMagu(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_HIE_HIE)) {
         return hieHie(player, move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_GURA_GURA)) {
         if (SeismicWave.isWave(move.id())) {
            // FRUIT-GURA_GURA-03 : l'onde se propage et frappe à son arrivée — ver15 appliquait
            // d'un seul coup une portée allant jusqu'à 48 blocs.
            SeismicWave.cast(player, move);
            return true;
         }
         return false;
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO)) {
         return GoroTechniques.cast(player,move);
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU)) {
         if (GomuArmStrike.handles(move.id())) {
            // FRUIT-GOMU_GOMU-03/04 : le bras s'étire jusqu'à son point d'impact et c'est LÀ que les
            // dégâts sont appliqués — un mur arrête le bras, donc arrête le coup.
            GomuArmStrike.cast(player, move);
            return true;
         }
         return false;
      }
      // FRUITS ZOAN (FRUIT-{NEKO_NEKO_*,RYU_RYU_*,TORI_TORI,UO_UO,USHI_USHI}-04/05) : les deux
      // mécaniques communes aux huit — bonds bornés et combos bornés — sont écrites UNE fois
      // (ZoanMovement, ZoanClaw) et ne s'appliquent qu'à la famille Zoan. Tout le reste de leur
      // catalogue passe par le moteur générique.
      if (DevilFruitPowerLogic.family(fruit.getItem()) == DevilFruitPowerLogic.Family.ZOAN) {
         if (ZoanMovement.isMovementMove(move.id())) {
            return ZoanMovement.leap(player, 0.0);
         }
         if (move.type() == MoveType.MELEE && ZoanClaw.isComboMove(move.id())) {
            if (ZoanClaw.isActive(player)) return false;
            ZoanClaw.strikeCombo(player, fruit, move);
            return true; // A missed melee attempt still plays its gesture and pays its cost.
         }
      }
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_BOMU_BOMU)) {
         return switch (move.id()) {
            // FRUIT-BOMU_BOMU-03 : origine de contact, aérienne ou corporelle — le moteur générique
            // (cône de frappe) ne peut pas les distinguer, on passe donc par le moteur d'explosion.
            case "punchbomb", "kickbomb", "bomboverhead", "zenshinkibaku" -> {
               DevilFruitExplosions.detonate(player, move);
               yield true;
            }
            case "nosefancycannon" -> noseFancyCannon(player, fruit, move);
            default -> false;
         };
      }
      return false;
   }

   /**
    * <b>FRUIT-BOMU_BOMU-03 — Nose Fancy Cannon : explosion à l'impact du projectile.</b>
    *
    * <p>Troisième origine demandée par le plan. Le projectile conserve les valeurs de ver15 (2,2
    * dégâts, 80 ticks de vol, recul 0,5) ; ce qui change est qu'il <b>explose</b> là où il touche, et
    * que cette explosion obéit aux mêmes réglages que les deux autres origines.</p>
    */
   private static boolean noseFancyCannon(ServerPlayer player, ItemStack fruit, DevilFruitMove move) {
      String[] vis = DevilFruitRegistry.projectileVisual(move.id());
      if (vis == null) {
         vis = DevilFruitElement.projectileVisualFallback(DevilFruitElement.of(fruit));
      }
      SkillProjectileEntity projectile = CombatSkillLogic.fireProjectile(player, move.damage(),
         move.knockback(), move.effect(), move.effectDuration(), move.effectAmplifier(), move.lifetime(),
         move.speed(), vis[0], vis[1], move.particle());
      if (projectile != null) {
         projectile.explodeOnImpact(DevilFruitExplosions.radius(move.id()), move.particle());
      }
      return true;
   }

   /**
    * <b>FRUIT-MORI_MORI — racines, végétation, drainage (-03/-04/-05).</b>
    *
    * <p>Trois techniques portent les trois mécaniques, chacune là où elle a un sens physique :
    * <b>Root Surroundings</b> (racines autour) <b>enracine</b> les cibles proches ; <b>Giant Root</b>
    * fait <b>pousser la végétation</b> ; <b>Mori Mori Assault</b> (au contact) <b>draine</b>. Les deux
    * autres techniques restent inchangées : elles n'ont pas de mécanique propre à porter, et leur en
    * attribuer une serait un ajout, pas un portage.</p>
    *
    * @return vrai si la technique est prise en charge ici
    */
   /**
    * <b>FRUIT-OPE_OPE-03/04/05 — la ROOM, l'échange et la découpe.</b>
    *
    * <p>Trois techniques, dont <b>deux n'existent pas dans la source</b> : ver15 ne déclare que
    * <i>Stone Ope Ope</i>, une mêlée, et le plan demande explicitement d'ajouter la ROOM, l'échange de
    * positions et une découpe temporaire. Ce n'est donc pas un portage : c'est du contenu
    * <b>demandé</b> par le plan, avec des valeurs <b>choisies</b> et déclarées comme telles.</p>
    *
    * <p><i>Stone Ope Ope</i> n'est volontairement <b>pas</b> interceptée : le contrat TECH-01 demande
    * d'en préserver la portée et le type de dégâts, donc elle garde le comportement de mêlée générique.
    * C'est la ROOM qui gouverne les techniques chirurgicales — pas la mêlée d'origine.</p>
    */
   /**
    * <b>FRUIT-YAMI_YAMI-03/04/05 — l'attraction, l'absorption et le scellement.</b>
    *
    * <p>Ce que le moteur générique ne faisait pas : une <b>attraction progressive</b> à rayon et force
    * bornés qui ne tire <b>jamais à travers un mur</b> (-03), une fenêtre d'<b>absorption bornée par
    * capacité et par énergie</b> (-04), et le <b>scellement temporaire</b> du pouvoir des cibles
    * touchées (-05).</p>
    *
    * <p><b>Les valeurs ver15 ne bougent pas</b> : Kurouzu 6 / 10 / 1, Black Hole 24 / 48 / 2, Dark Ball
    * 40, Dark Matter 200, avec leurs coûts et recharges. Seules les règles que ver15 ne déclarait pas
    * sont écrites ici.</p>
    *
    * <p><b>Où le scellement s'applique, et pourquoi seulement là.</b> Il est posé par les deux
    * techniques de <b>contact</b> (Kurouzu, Black Hole) : c'est le contact des ténèbres qui scelle, et
    * c'est ce que la source décrit. Les deux projectiles gardent exactement leurs dégâts ver15 et
    * n'emportent pas de scellement — les faire sceller aurait demandé de déclarer une durée d'effet
    * dans le registre, c'est-à-dire un <b>second propriétaire</b> de la durée, en plus de la
    * configuration. Un seul propriétaire, donc : le service.</p>
    */
   private static boolean yamiYami(ServerPlayer player, ItemStack fruit, DevilFruitMove move) {
      return switch (move.id()) {
         // Kurouzu : on attire, puis on frappe — et le contact scelle le pouvoir.
         case "kurouzu" -> {
            boolean attire = YamiDarkness.startPull(player, move.range());
            int scellees = YamiSeal.strikeAndSeal(player, fruit, move);
            YamiDarkness.playDarknessSound(player);
            yield attire || scellees > 0;
         }
         // Black Hole : le vortex attire PLUS LOIN (rayon ver15 de 48, borné dans le code) et OUVRE la
         // fenêtre d'absorption — c'est lui qui avale les coups, tant qu'il reste capacité et énergie.
         case "blackhole" -> {
            boolean attire = YamiDarkness.startPull(player, move.range());
            boolean absorbe = YamiDarkness.startAbsorbing(player);
            int scellees = YamiSeal.strikeAndSeal(player, fruit, move);
            YamiDarkness.playDarknessSound(player);
            yield attire || absorbe || scellees > 0;
         }
         // Dark Ball / Dark Matter : projectiles du moteur générique, valeurs ver15 inchangées.
         default -> false;
      };
   }

   /**
    * <b>FRUIT-PIKA_PIKA-03/04/05 — les rayons, le déplacement lumineux, la volée annoncée.</b>
    *
    * <p>Ce que le moteur générique ne pouvait pas prouver, et que ce fruit ajoute : une <b>portée
    * explicite</b> sur les rayons (-03), une <b>destination calculée côté serveur</b> et validée pour le
    * déplacement (-04), et une <b>fenêtre d'avertissement</b> avant l'impact avec une volée bornée
    * (-05).</p>
    *
    * <p>Les <b>valeurs ver15 ne bougent pas</b> : dégâts, recul, énergie, recharge, durée d'effet et
    * particules sont ceux du catalogue. Seules les règles que ver15 ne déclarait pas sont écrites ici.</p>
    */
   private static boolean pikaPika(ServerPlayer player, ItemStack fruit, DevilFruitMove move) {
      return switch (move.id()) {
         // -03 : rayons à portée explicite (vitesse ver15 conservée : CORE-016).
         case "laser1" -> LightBeams.fireRay(player, fruit, move, LightBeams.laserRange()) != null;
         case "magatama" -> {
            int perles = br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig.magatamaPerVolley();
            boolean auMoinsUne = false;
            for (int i = 0; i < perles; i++) {
               auMoinsUne |= LightBeams.fireRay(player, fruit, move, LightBeams.magatamaRange()) != null;
            }
            yield auMoinsUne;
         }
         // -05 : la barre de lasers ANNONCÉE — signal visible maintenant, volée bornée plus tard.
         case "laser2" -> LightCharge.begin(player, fruit, move);
         // -04 : déplacement lumineux, destination calculée côté serveur puis validée.
         case "yatanokagami" -> {
            boolean deplace = LightTravel.travel(player, 0.0);
            if (deplace) {
               // Yata no Kagami reste aussi un élan (les 40 ticks de Vitesse de ver15).
               LightBuffs.apply(player, net.minecraft.world.effect.MobEffects.SPEED,
                  move.effectDuration(), move.effectAmplifier());
            }
            yield deplace;
         }
         // Bonus de lumière : bornés, prolongés plutôt qu'empilés, rendus sans voler les effets d'autrui.
         case "amanomurakumo" -> LightBuffs.apply(player, net.minecraft.world.effect.MobEffects.STRENGTH,
            move.effectDuration(), move.effectAmplifier());
         case "passive" -> LightBuffs.apply(player, net.minecraft.world.effect.MobEffects.SPEED,
            move.effectDuration(), move.effectAmplifier());
         // TECH-03 / TECH-07 : frappe lumineuse — cône et ligne de vue du moteur, UN impact par cible,
         // et pour « Light Speed Kick » un élan borné en plus.
         case "kick" -> {
            LightTravel.dash(player, 0.0);
            LightBeams.strike(player, fruit, move, 0.0);
            yield true; // Movement/attack happened even if the kick misses.
         }
         case "speedispower" -> {
            // « Speed is Power » : la lumière active ajoute un bonus BORNÉ — la vitesse EST la puissance.
            double bonus = LightBeams.lightBonus(player);
            LightBeams.strike(player, fruit, move, bonus);
            yield true;
         }
         default -> false;
      };
   }

   private static boolean opeOpe(ServerPlayer player, DevilFruitMove move) {
      if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) {
         return false;
      }
      return switch (move.id()) {
         case "room" -> OpeRoom.open(player);
         case "shambles" -> OpeRoom.shambles(player);
         case "amputation" -> {
            net.minecraft.world.entity.LivingEntity cible = OpeRoom.targetInRoom(player);
            yield cible != null && Amputation.amputate(level, player, cible);
         }
         default -> false;
      };
   }

   private static boolean moriMori(ServerPlayer player, ItemStack fruit, DevilFruitMove move) {
      if ("rootsurround".equals(move.id())) {
         if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return false;
         }
         int pris = 0;
         for (var cible : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
               player.getBoundingBox().inflate(6.0))) {
            if (Roots.entangle(cible, player)) {
               pris++;
            }
         }
         LOGGER.info("[MinePiece] Root Surroundings : {} cible(s) enracinée(s).", pris);
         return pris > 0;
      }
      if ("rootgiant".equals(move.id())) {
         var centre = player.blockPosition().relative(player.getDirection(), 2);
         int poses = Roots.growVegetation(player, centre);
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§eVégétation : §f" + poses + " bloc(s) §7(définitif)"), true);
         LOGGER.info("[MinePiece] Giant Root : {} bloc(s) de végétation définitive.", poses);
         return poses > 0;
      }
      if ("assault".equals(move.id())) {
         // Drainage au contact : on draine ce que la frappe mêlée a touché, pas une zone entière —
         // sans quoi le pouvoir se transformerait en pompe à distance. La cible est donc celle du CÔNE
         // de mêlée, avec la même constante que le moteur de combat : une seule définition du « touché ».
         if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return false;
         }
         var regard = player.getLookAngle();
         net.minecraft.world.entity.LivingEntity cible = null;
         for (var candidat : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
               player.getBoundingBox().inflate(move.range()))) {
            if (!CombatSkillLogic.isValidTarget(player, candidat) || !player.hasLineOfSight(candidat)) {
               continue;
            }
            var vers = candidat.position().subtract(player.getEyePosition()).normalize();
            if (vers.dot(regard) > CombatSkillLogic.MELEE_CONE_COS) {
               cible = candidat;
               break;
            }
         }
         if (cible == null) {
            return false;
         }
         double gagne = LifeDrain.drain(player, cible);
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§aDrainage : §f+" + Math.round(gagne) + " §7énergie"), true);
         LOGGER.info("[MinePiece] Assaut Mori Mori : drainage de {} énergie.", gagne);
         return true;
      }
      return false;
   }

   /**
    * <b>FRUIT-MOKU_MOKU — le nuage de fumée (-03/-04/-05).</b>
    *
    * <p><b>White Out</b> est la technique du nuage : c'est le grand écran de fumée du canon. Tout le
    * reste (mobilité du porteur dans son propre nuage, capture des cibles non protégées) est une
    * <b>conséquence d'être dans le nuage</b>, et non une technique séparée : le nuage est un LIEU, et le
    * comportement dépend d'y être ou non. Cela évite d'avoir plusieurs vérités sur « qu'est-ce qu'être
    * dans la fumée ».</p>
    *
    * @return vrai si la technique est prise en charge ici
    */
   private static boolean mokuMoku(ServerPlayer player, DevilFruitMove move) {
      if ("whiteout".equals(move.id())) {
         boolean cree = SmokeCloud.create(player);
         if (cree && player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Les cibles déjà présentes quand le nuage apparaît sont capturées tout de suite : sinon
            // l'effet dépendrait de l'ordre des ticks, ce qui rendrait le comportement imprévisible.
            int pris = SmokeCloud.captureInClouds(serverLevel);
            LOGGER.info("[MinePiece] White Out : nuage créé, {} cible(s) capturée(s).", pris);
         }
         return cree;
      }
      return false;
   }

   /**
    * <b>FRUIT-MOCHI_MOCHI — le bras de mochi et les constructions (-03/-04/-05).</b>
    *
    * <p>Les frappes de bras passent par la <b>même machinerie</b> que celles de Gomu : la collision suit
    * donc le membre par construction, sans formule parallèle à maintenir. Ce qui change est l'identité —
    * une <b>texture de mochi</b> au lieu de celle de Luffy (le critère est explicite : le rendu ne doit
    * pas réutiliser <b>involontairement</b> la texture de Luffy) — et une conséquence : ce que le bras
    * touche <b>colle</b>. Le collage est posé au moment exact où l'impact est confirmé, donc « touché » et
    * « collé » ne peuvent pas diverger.</p>
    *
    * @return vrai si la technique est prise en charge ici
    */
   private static boolean mochiMochi(ServerPlayer player, ItemStack fruit, DevilFruitMove move) {
      if (GomuArmStrike.handles(move.id())) {
         GomuArmStrike.cast(player, move);
         return true;
      }
      if ("mochibuild".equals(move.id())) {
         // FRUIT-MOCHI_MOCHI-05 : la construction est posée devant le porteur, à ses pieds. Chaque bloc
         // passe par PowerBlockService, donc il EXPIRE et le terrain d'origine est RESTITUÉ.
         var centre = player.blockPosition().relative(player.getDirection(), 2);
         int poses = MochiBuild.build(player, centre);
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§eConstruction de mochi : §f" + poses + " bloc(s) §7(" + MochiBuild.activeCount(player)
            + "/" + br.com.oneminepiece.opworldlike.mochi.DevilFruitMochiConfig.constructionMaxActive()
            + " constructions)"), true);
         LOGGER.info("[MinePiece] Construction de mochi : {} bloc(s) posé(s).", poses);
         return poses > 0;
      }
      return false;
   }

   /**
    * <b>FRUIT-KIRO_KIRO — le poids réglable (-03/-04).</b>
    *
    * <p><b>Weight Manipulation</b> fait <b>cycler</b> le poids entre des paliers lisibles : c'est
    * l'« interface » de réglage demandée par le plan. Le porteur n'écrit jamais une valeur directement —
    * il demande un palier, et le <b>serveur</b> décide de ce qui est retenu.</p>
    *
    * <p>Et la valeur annoncée au joueur est la valeur <b>CONFIRMÉE</b>, pas celle demandée : c'est tout
    * le sens du critère. Afficher la demande ferait croire à un pouvoir qu'on n'a pas.</p>
    *
    * @return vrai si la technique est prise en charge ici
    */
   private static boolean kiroKiro(ServerPlayer player, DevilFruitMove move) {
      if ("weightmanipulation".equals(move.id())) {
         double actuel = WeightState.current(player);
         double suivant = PALIERS_POIDS[0];
         for (int i = 0; i < PALIERS_POIDS.length; i++) {
            if (Math.abs(PALIERS_POIDS[i] - actuel) < 1.0E-9) {
               suivant = PALIERS_POIDS[(i + 1) % PALIERS_POIDS.length];
               break;
            }
         }
         double confirme = WeightState.request(player, suivant);
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§bPoids : §f" + confirme + "× §7(palier suivant, borné par le serveur)"), true);
         LOGGER.info("[MinePiece] Poids demandé {} → confirmé {}.", suivant, confirme);
         return true;
      }
      return false;
   }

   /** Paliers de poids proposés par Weight Manipulation, du plus léger au plus lourd. */
   private static final double[] PALIERS_POIDS = {0.1, 1.0, 50.0, 100.0};

   /**
    * <b>FRUIT-KIRA_KIRA — le corps de diamant (-03/-04).</b>
    *
    * <p>Le corps de diamant est une capacité <b>activée</b>, et l'activation comme la désactivation sont
    * diffusées au monde : le critère parle des <b>observateurs</b>, pas du porteur. Un pouvoir qui ne se
    * voit que de celui qui le lance ne communique rien — les autres ne peuvent ni le reconnaître, ni
    * l'anticiper, ni comprendre pourquoi leurs coups ne portent pas.</p>
    *
    * <p>Un second appui désactive : c'est le seul moyen pour le porteur de <b>rendre</b> son état
    * visiblement, et c'est aussi ce que demande le critère (« activation <b>et</b> désactivation »).</p>
    *
    * @return vrai si la technique est prise en charge ici
    */
   private static boolean kiraKira(ServerPlayer player, DevilFruitMove move) {
      if ("diamondbody".equals(move.id())) {
         boolean change = DiamondBody.toggle(player);
         LOGGER.info("[MinePiece] Corps de diamant : état changé = {} (actif = {}).",
            change, DiamondBody.isActive(player));
         return change;
      }
      return false;
   }

   /**
    * <b>FRUIT-KAGE_KAGE — acquisition et restitution des ombres (-03/-05).</b>
    *
    * <p><b>Shadow Asgard</b> est l'acquisition : le porteur prend les ombres autour de lui, dans le
    * rayon du contrat, jusqu'au plafond. Chaque ombre prise a un <b>propriétaire légitime</b>, qui n'a
    * aucun moyen de la récupérer lui-même — d'où l'insistance du service sur la restitution.</p>
    *
    * <p>Les cibles <b>alliées</b> sont écartées : les invocations du porteur et les membres de son
    * équipe. Le plan demande que les invocations respectent les <b>alliances</b> ; prendre l'ombre d'un
    * allié serait l'exact contraire, et sans cette règle un porteur au milieu de son groupe se
    * renforcerait en appauvrissant les siens.</p>
    *
    * <p><b>Doppelman</b> reste l'invocation réelle de CORE-021 : propriétaire, IA, durée de vie. Le plan
    * la décrivait comme un « visuel immobile » — c'était l'état de la source ver15, pas celui du port.</p>
    *
    * @return vrai si la technique est prise en charge ici
    */
   private static boolean kageKage(ServerPlayer player, DevilFruitMove move) {
      if ("shadowasgard".equals(move.id())) {
         int pris = 0;
         for (LivingEntity cible : ciblesDompables(player)) {
            if (ShadowLedger.steal(player, cible)) {
               pris++;
            } else if (ShadowLedger.heldCount(player) >= DevilFruitKageConfig.shadowCap()) {
               break; // plafond atteint : on n'insiste pas
            }
         }
         LOGGER.info("[MinePiece] Shadow Asgard : {} ombre(s) prise(s), {} détenue(s).",
            pris, ShadowLedger.heldCount(player));
         return true;
      }
      return false;
   }

   /** Cibles éligibles au vol d'ombre : vivantes, ni soi, ni ses invocations, ni ses alliés. */
   private static java.util.List<LivingEntity> ciblesDompables(ServerPlayer player) {
      java.util.List<LivingEntity> out = new java.util.ArrayList<>();
      double rayon = DevilFruitKageConfig.stealRadius();
      for (LivingEntity cible : player.level().getEntitiesOfClass(LivingEntity.class,
            player.getBoundingBox().inflate(rayon))) {
         if (cible == player || !cible.isAlive()) {
            continue;
         }
         if (cible instanceof SummonEntity summon && DevilFruitSummons.isOwnedBy(summon, player)) {
            continue; // ses propres invocations
         }
         if (player.isAlliedTo(cible)) {
            continue; // alliés : on ne les appauvrit pas
         }
         out.add(cible);
      }
      return out;
   }

   /**
    * <b>FRUIT-INU_INU_WOLF — bonds, morsures et poursuite avec endurance (-04).</b>
    *
    * <p><b>Soru</b> devient le <b>bond</b> du loup : un déplacement échantillonné qui s'arrête au premier
    * obstacle — on saute <i>sur</i> le mur, on ne le traverse pas. <b>Okami Hajiki</b> devient la
    * <b>morsure</b> : frappe courte et recul fort, car mordre sert à écarter. <b>Poursuite</b> est
    * l'ajout demandé par le plan, avec une <b>endurance</b> qui se vide et <b>ne se recharge pas en
    * courant</b> — sans quoi poursuivre serait condamner.</p>
    *
    * <p>Important : ces hooks sont dans la branche du <b>loup</b> uniquement. Soru, Shigan et Rankyaku
    * sont partagés par plusieurs Zoan (Rokushiki) : les autres fruits gardent le comportement commun.</p>
    *
    * @return vrai si la technique est prise en charge ici
    */
   private static boolean inuWolf(ServerPlayer player, DevilFruitMove move) {
      if ("soru".equals(move.id())) {
         double parcouru = WolfStrike.leap(player, DevilFruitWolfConfig.leapDistance());
         LOGGER.info("[MinePiece] Soru (bond du loup) : {} bloc(s) parcouru(s).", parcouru);
         return true;
      }
      if ("okamihajiki".equals(move.id())) {
         int mordus = WolfStrike.bite(player, move.damage(), DevilFruitWolfConfig.biteKnockback());
         LOGGER.info("[MinePiece] Okami Hajiki (morsure) : {} cible(s).", mordus);
         return true;
      }
      if ("poursuite".equals(move.id())) {
         if (WolfPursuit.isPursuing(player)) {
            WolfPursuit.end(player, "relance");
            return true; // relancer arrête : on ne cumule pas deux poursuites
         }
         var cible = cibleDevant(player, DevilFruitWolfConfig.pursuitMaxRange());
         if (cible == null) {
            LOGGER.info("[MinePiece] Poursuite refusée : aucune cible devant.");
            return false; // refus sans coût
         }
         return WolfPursuit.begin(player, cible);
      }
      return false;
   }

   /** Cible vivante la plus proche DEVANT le joueur, dans la portée donnée. */
   private static LivingEntity cibleDevant(ServerPlayer player, double portee) {
      LivingEntity meilleure = null;
      double meilleureDistance = Double.MAX_VALUE;
      for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class,
            player.getBoundingBox().inflate(portee))) {
         if (!CombatSkillLogic.isValidTarget(player, candidate) || !player.hasLineOfSight(candidate)) {
            continue;
         }
         double d = player.distanceTo(candidate);
         if (d > portee || candidate.getEyePosition().subtract(player.getEyePosition()).normalize()
               .dot(player.getLookAngle()) <= CombatSkillLogic.MELEE_CONE_COS) continue;
         if (d < meilleureDistance) {
            meilleureDistance = d;
            meilleure = candidate;
         }
      }
      return meilleure;
   }

   /**
    * <b>FRUIT-INU_INU_OKUCHI_NO_MAKAMI — le souffle glacé et la défense de glace.</b>
    *
    * <p><b>Namuji Hyoga</b> est le souffle : un cône dirigé, arrêté par les murs, qui gèle ce qu'il
    * touche (gel borné par le service partagé, donc libération garantie).</p>
    *
    * <p><b>Hyoga Heki</b> est la défense temporaire du contrat (-05) : elle a un coût et des exceptions,
    * et elle expire <b>sans supprimer d'autres bonus</b>.</p>
    *
    * <p>Les autres techniques (Attack Overhead, Attack Sweep, Raimei Hakke, Narikabura, Himorogiri)
    * restent au moteur commun : leurs paramètres sont ceux de ver15, et les toucher ferait diverger
    * l'audit sans rien apporter au contrat.</p>
    *
    * @return vrai si la technique est prise en charge ici
    */
   private static boolean okuchi(ServerPlayer player, DevilFruitMove move) {
      if ("namujihyoga".equals(move.id())) {
         int pris = IceBreath.strike(player, move.damage(), move.knockback());
         LOGGER.info("[MinePiece] Namuji Hyoga : {} cible(s) prises dans le souffle.", pris);
         return true;
      }
      if ("hyogaheki".equals(move.id())) {
         if (IceDefense.isActive(player)) {
            LOGGER.info("[MinePiece] Hyoga Heki refusé : défense déjà active.");
            return false; // refus sans coût : la règle globale s'applique
         }
         if (!IceDefense.begin(player)) {
            return false; // scellé, mort : refus sans coût
         }
         return true;
      }
      return false;
   }

   /**
    * <b>FRUIT-IMU — la flèche, avec un rendu qui lui appartient (FRUIT-IMU-05).</b>
    *
    * <p><b>Le défaut corrigé</b> : la technique déclarait {@code entity_gomu_arm} comme entité visuelle —
    * un reste de valeur par défaut MCreator. Concrètement, « Imu Arrow » faisait apparaître <b>le bras
    * élastique de Luffy</b>. Le plan l'exige noir sur blanc : « n'utilise plus Luffy par défaut ».</p>
    *
    * <p><b>La correction</b> : on ne touche <b>pas</b> aux paramètres déclarés (dégâts, portée, coût,
    * recharge restent ceux de ver15, donc l'audit reste propre) — on remplace le <b>rendu</b> : une
    * flèche visible (modèle {@code aroaro_arrow}, déjà présent dans les ressources du mod) file dans la
    * direction du regard, tandis que la frappe est résolue par le moteur de mêlée commun.</p>
    *
    * <p>Comme ce comportement prend la technique en charge, le lanceur générique <b>n'exécute plus</b> la
    * branche « bras de Gomu » : la preuve {@code T1} compte les entités de bras avant et après.</p>
    *
    * @return vrai pour Imu Arrow
    */
   private static boolean imu(ServerPlayer player, DevilFruitMove move) {
      if (!"imuarrow".equals(move.id())) {
         return false; // Silent Command / Sovereign Grasp : primitives du moteur commun.
      }
      ServerLevel level = (ServerLevel) player.level();
      // Rendu : une vraie flèche, pas un bras élastique.
      Vec3 look = player.getLookAngle();
      Vec3 eye = player.getEyePosition();
      SkillProjectileEntity fleche = new SkillProjectileEntity(
         net.mcreator.minepiece.init.MinepieceModEntities.SKILL_PROJECTILE,
         eye.x, eye.y, eye.z, look, level);
      fleche.setOwner(player);
      // Purement visuelle : dégâts et recul nuls, la frappe est résolue ci-dessous.
      fleche.damage(0.0).knockback(0.0).element(ElementType.PHYSICAL);
      fleche.model("aroaro_arrow").texture("aroaro_arrow").scale(2.5F);
      fleche.trail(MinepieceModParticles.SLASH).lifetime(30);
      fleche.shoot(look.x, look.y, look.z, 2.5F, 0.0F);
      level.addFreshEntity(fleche);
      // La frappe : cône de mêlée commun, avec les paramètres déclarés de la technique.
      int touches = CombatSkillLogic.meleeStrike(player, move.damage(), move.range(), move.knockback(),
         null, 0, 0);
      LOGGER.info("[MinePiece] Imu Arrow : flèche tirée (modèle aroaro_arrow) — {} cible(s) touchée(s),"
         + " aucun bras de Gomu invoqué.", touches);
      return true;
   }

   /**
    * <b>FRUIT-HORO_HORO — fantômes : le contrôle à distance.</b>
    *
    * <p><b>Toku Horo</b> est la technique par laquelle le porteur <b>sort de son corps</b>. Son corps
    * reste sur place, immobile, et son esprit agit à distance : c'est le contrôle à distance du contrat,
    * borné en durée et en énergie, avec <b>sortie garantie</b> sur échéance, énergie, suppression, mort
    * et <b>déconnexion</b>.</p>
    *
    * <p>Les autres techniques (Negative Hollow, Mini Hollow, Kumac, Rap) restent au moteur commun : le
    * <b>projectile fantôme</b> et l'<b>état négatif</b> sont portés par l'élément {@code GHOST}, donc
    * toutes les techniques du fruit en héritent sans recopie.</p>
    *
    * @return toujours vrai pour Toku Horo
    */
   private static boolean horoHoro(ServerPlayer player, DevilFruitMove move) {
      if (!"tokuhoro".equals(move.id())) {
         return false; // Negative Hollow / Mini Hollow / Kumac / Rap : moteur commun + élément GHOST.
      }
      if (RemoteControl.isControlling(player)) {
         RemoteControl.end(player, "interruption");
         return true;
      }
      // Lecture directe : on ne DÉCLARE pas le type de retour (c'est une classe interne imbriquée,
      // source d'erreur récurrente) — on consomme la valeur.
      double energie = MinepieceModVariables.get(player).PlayerEnergy;
      if (energie < DevilFruitHollowConfig.controlCostPerSecond()) {
         LOGGER.info("[MinePiece] Toku Horo refusé : énergie {}.", energie);
         return false; // refus sans coût : la règle globale s'applique
      }
      return RemoteControl.begin(player);
   }

   /**
    * <b>FRUIT-HITO_HITO_DAIBUTSU — Bouddha géant : l'onde de choc.</b>
    *
    * <p><b>Impact Wave</b> ne frappe pas un cône : elle <b>voyage</b>. Le front décide de ce qu'il
    * atteint, donc l'attaque est esquivable et lisible — c'est le critère du contrat.</p>
    *
    * <p>Les frappes de mêlée du fruit (Attack Overhead, Brilliant Punk, Impact Ball…) restent au moteur
    * commun : elles héritent automatiquement de la <b>portée proportionnelle à l'échelle</b>, donc un
    * géant de 3,5× frappe à 3,5× la portée sans qu'aucune technique n'ait à le savoir.</p>
    *
    * @return toujours vrai pour Impact Wave
    */
   private static boolean hitoHitoDaibutsu(ServerPlayer player, DevilFruitMove move) {
      if (!"impactwave".equals(move.id())) {
         return false; // les autres : moteur commun (portée déjà cohérente avec l'échelle).
      }
      boolean lance = GiantShockWave.cast(player);
      ServerLevel level = (ServerLevel) player.level();
      var source = CombatSkillLogic.elementalDamageSource(level, ElementType.EXPLOSIVE, player, null);
      int touches = 0;
      // Le corps du géant frappe aussi au contact : c'est une onde, pas un projectile.
      for (LivingEntity cible : level.getEntitiesOfClass(LivingEntity.class,
            player.getBoundingBox().inflate(3.0))) {
         if (cible == player || !cible.isAlive() || !CombatSkillLogic.isValidTarget(player, cible)) {
            continue;
         }
         if (cible.hurtServer(level, source, (float) move.damage())) {
            touches++;
         }
      }
      LOGGER.info("[MinePiece] Impact Wave : onde lancée={} · {} cible(s) au contact · échelle du porteur {}",
         lance, touches, round1(player.getScale()));
      return true;
   }

   private static double round1(double v) {
      return Math.round(v * 10.0) / 10.0;
   }

   /**
    * <b>FRUIT-MERO_MERO — charme : l'attaque de ZONE.</b>
    *
    * <p>Le plan exige de « différencier projectile, coup physique et attaque de zone », chacun avec son
    * ciblage, son coût et sa durée propres. Les deux premiers modes passent par le <b>moteur commun</b>
    * (le projectile <i>Pistol Kiss</i> et les kicks en cône) et tirent leur comportement de
    * l'élément {@code CHARM} : ils sont donc traités identiquement partout, sans recopie.</p>
    *
    * <p><b>Perfume Femur Magna</b> est le troisième mode : une <b>onde de charme</b> autour du porteur,
    * qui touche <b>toutes</b> les cibles admissibles du rayon — ciblage franchement différent du cône
    * frontal des kicks.</p>
    *
    * @return toujours vrai : entièrement pris en charge ici
    */
   private static boolean meroMero(ServerPlayer player, DevilFruitMove move) {
      ServerLevel level = (ServerLevel) player.level();
      if (!"perfumefemur".equals(move.id())) {
         return false; // Pistol Kiss (projectile) et les kicks (cône) : moteur commun + élément CHARM.
      }
      double rayon = DevilFruitLoveConfig.areaRadius();
      var source = CombatSkillLogic.elementalDamageSource(level, ElementType.CHARM, player, null);
      int petrifiees = 0;
      int insensibles = 0;
      for (LivingEntity cible : level.getEntitiesOfClass(LivingEntity.class,
            player.getBoundingBox().inflate(rayon))) {
         if (cible == player || !cible.isAlive() || !CombatSkillLogic.isValidTarget(player, cible)) {
            continue;
         }
         if (Petrification.applyFrom(player, cible) > 0) {
            petrifiees++;
         } else {
            insensibles++;
         }
         cible.hurtServer(level, source, (float) move.damage());
      }
      LOGGER.info("[MinePiece] Perfume Femur Magna : rayon {} — {} pétrifiée(s), {} insensible(s) "
         + "(cibles non admissibles laissées LIBRES).", rayon, petrifiees, insensibles);
      // Onde visible : le charme se voit.
      level.sendParticles(MinepieceModParticles.FLOWER_PINK, player.getX(),
         player.getY() + 1.0, player.getZ(), 24, rayon * 0.5, 0.6, rayon * 0.5, 0.02);
      return true;
   }

   /**
    * <b>FRUIT-ZUSHI_ZUSHI — gravité : champ, lévitation, météores.</b>
    *
    * <ul>
    *   <li><b>Gravito</b> ouvre un <b>champ de gravité</b> qui alourdit ce qu'il contient ;</li>
    *   <li><b>Moko</b> <b>lévite</b> les cibles autour du porteur — temporairement, et elles sont
    *       toujours libérées ;</li>
    *   <li><b>Meteor</b> et <b>Meteor (small)</b> font <b>tomber</b> un météore : annonce, puis impact
    *       unique. ver15 en faisait un projectile horizontal, ce qui n'est pas une chute.</li>
    * </ul>
    *
    * @return toujours vrai pour ces techniques : entièrement prises en charge ici
    */
   private static boolean zushiZushi(ServerPlayer player, DevilFruitMove move) {
      ServerLevel level = (ServerLevel) player.level();
      switch (move.id()) {
         case "gravito" -> {
            // Le champ alourdit : intensité négative, donc vers le bas.
            GravityField.create(level, player.position(), player,
               DevilFruitGravityConfig.fieldRadius(), DevilFruitGravityConfig.fieldIntensity(),
               DevilFruitGravityConfig.fieldLifetimeTicks(), false);
            var source = CombatSkillLogic.elementalDamageSource(level, ElementType.PHYSICAL, player, null);
            int touches = 0;
            for (LivingEntity cible : level.getEntitiesOfClass(LivingEntity.class,
                  player.getBoundingBox().inflate(move.range()))) {
               if (cible == player || !cible.isAlive() || !CombatSkillLogic.isValidTarget(player, cible)) {
                  continue;
               }
               if (cible.hurtServer(level, source, (float) move.damage())) {
                  touches++;
                  CombatSkillLogic.applyElementalEffect(cible, ElementType.PHYSICAL);
               }
            }
            LOGGER.info("[MinePiece] Gravito : champ r={} intensité plafonnée à {} — {} cible(s).",
               DevilFruitGravityConfig.fieldRadius(), GravityField.cappedIntensity(DevilFruitGravityConfig.fieldIntensity()), touches);
            return true;
         }
         case "moko" -> {
            var source = CombatSkillLogic.elementalDamageSource(level, ElementType.PHYSICAL, player, null);
            int leves = 0;
            for (LivingEntity cible : level.getEntitiesOfClass(LivingEntity.class,
                  player.getBoundingBox().inflate(move.range()))) {
               if (!Levitation.canLevitate(player, cible)) {
                  continue; // permissions de cible refusées : ni le porteur, ni un allié
               }
               if (Levitation.apply(player, cible) > 0) {
                  leves++;
               }
               cible.hurtServer(level, source, (float) move.damage());
            }
            LOGGER.info("[MinePiece] Moko : {} cible(s) lévitée(s) sur {} blocs.", leves, move.range());
            return true;
         }
         case "meteor", "meteorsmall" -> {
            Vec3 impact = player.position().add(player.getLookAngle().scale(move.range() > 0 ? 6.0 : 6.0));
            boolean petit = move.id().equals("meteorsmall");
            double radius = petit ? DevilFruitGravityConfig.meteorRadius() * 0.6 : DevilFruitGravityConfig.meteorRadius();
            return MeteorFall.launch(player, impact, move.damage(), radius);
         }
         default -> {
            return false; // Attack Speed (buff) / Rubble (projectile) : moteur générique.
         }
      }
   }

   /**
    * <b>FRUIT-SUNA_SUNA — sable : tempête et dessiccation.</b>
    *
    * <p><b>Barjan</b> déchaîne une tempête de sable : un volume visible (rayon × hauteur) dont les
    * cibles subissent la dessiccation et des dégâts — <b>seules</b> celles qui s'y trouvent, et rien
    * ne subsiste après l'expiration.</p>
    *
    * <p><b>Ground Death</b> dessèche tout ce qui l'entoure : c'est la technique d'aspiration du
    * porteur, elle applique la dessiccation à son plein rayon.</p>
    *
    * @return toujours vrai pour ces techniques : entièrement prises en charge ici
    */
   private static boolean sunaSuna(ServerPlayer player, DevilFruitMove move) {
      ServerLevel level = (ServerLevel) player.level();
      switch (move.id()) {
         case "barjan" -> {
            double[] volume = SandStorm.create(player);
            int touches = 0;
            for (LivingEntity cible : level.getEntitiesOfClass(LivingEntity.class,
                  SandStorm.volume(player.position(), volume[0], volume[1]))) {
               touches++;
            }
            LOGGER.info("[MinePiece] Barjan : tempête r={} h={} — {} cible(s) dans le volume.",
               volume[0], volume[1], touches);
            return true;
         }
         case "grounddeath" -> {
            var source = CombatSkillLogic.elementalDamageSource(level, ElementType.PHYSICAL, player, null);
            int dessechees = 0;
            for (LivingEntity cible : level.getEntitiesOfClass(LivingEntity.class,
                  player.getBoundingBox().inflate(move.range()))) {
               if (cible == player || !cible.isAlive() || !CombatSkillLogic.isValidTarget(player, cible)) {
                  continue;
               }
               // La dessiccation d'abord : elle décide si la cible est sensible (une cible humide résiste).
               if (Desiccation.apply(cible) > 0) {
                  dessechees++;
               }
               cible.hurtServer(level, source, (float) move.damage());
            }
            LOGGER.info("[MinePiece] Ground Death : {} cible(s) desséchée(s) sur {} blocs.",
               dessechees, move.range());
            return true;
         }
         default -> {
            return false; // Desert Spada / Sables / Desert La Spada : projectiles (moteur générique).
         }
      }
   }

   /**
    * <b>FRUIT-MERA_MERA — feu : anneau temporaire.</b>
    *
    * <p><b>Fire Pillar</b> dresse un anneau de feu au sol : le porteur pose une couronne enflammée
    * dont les dégâts sont calculés sur le <b>même</b> rayon que le feu visible
    * ({@code FireRing} garde une seule géométrie pour les deux), et dont les blocs passent par le
    * service d’attribution ; aucun bloc FIRE n’est posé par cet anneau visuel.</p>
    *
    * <p><b>Great Flame Commandment</b> en dresse un large : c'est la technique ultime du fruit, sa
    * zone doit être à la mesure de son coût.</p>
    *
    * @return toujours vrai pour ces techniques : entièrement prises en charge ici
    */
   private static boolean meraMera(ServerPlayer player, DevilFruitMove move) {
      return MeraTechniques.cast(player, move);
   }

   /**
    * <b>FRUIT-MAGU_MAGU — magma : brûlure propre et nappes.</b>
    *
    * <p>ver15 traitait ce fruit avec le moteur générique : trois projectiles à particules de flamme et
    * deux frappes, sans qu'aucun magma ne reste jamais au sol, et une « brûlure » qui n'était qu'un
    * incendie deux fois plus long.</p>
    *
    * <ul>
    *   <li><b>Dai Funka / Inugami Guren / Ryusei Kazan</b> restent des projectiles : la brûlure de
    *       magma leur est appliquée au point d'impact par {@code applyElementalEffect} ;</li>
    *   <li><b>Kagero</b> ouvre une nappe au sol devant le porteur ;</li>
    *   <li><b>Volcanic Explosion</b> frappe en zone <b>et</b> ouvre une nappe — c'est l'éruption.</li>
    * </ul>
    *
    * @return toujours vrai pour ces techniques : entièrement prises en charge ici
    */
   private static boolean maguMagu(ServerPlayer player, DevilFruitMove move) {
      return MaguTechniques.cast(player,move);
   }

   /**
    * <b>FRUIT-HIE_HIE — glace et gel progressif.</b>
    *
    * <p>ver15 traitait ce fruit avec le moteur générique : trois projectiles à particules de neige, et
    * deux frappes qui posaient un {@code SLOWNESS} d'amplificateur fixe. Les pics « Ice Spikes »
    * eux-mêmes étaient un projectile : le nom de la technique n'avait aucun rapport avec ce qu'elle
    * faisait.</p>
    *
    * <ul>
    *   <li><b>Ice Ball / Ice Age</b> — frappe de zone + <b>gel progressif</b> ({@link FreezeService}) :
    *       chaque coup fait monter d'un palier l'état de la victime, jusqu'au gel total ;</li>
    *   <li><b>Ice Age</b> gèle en outre la surface <b>autour</b> du porteur, <b>Ice Ball</b> un
    *       <b>chemin</b> droit devant — dans les deux cas jamais dans l'espace du porteur, pour ne pas
    *       contourner la faiblesse aquatique ;</li>
    *   <li><b>Ice Spikes</b> dresse de vrais pics définitifs et fauche ce qu'ils trouvent.</li>
    * </ul>
    *
    * @return toujours vrai pour ces trois techniques : elles sont entièrement prises en charge ici
    */
   private static boolean hieHie(ServerPlayer player, DevilFruitMove move) {
      ServerLevel level = (ServerLevel) player.level();
      switch (move.id()) {
         case "iceball", "iceage" -> {
            if (move.id().equals("iceage")) {
               IceTerrain.freezeField(player, move.range());
            } else {
               IceTerrain.freezePath(player, move);
            }
            int touches = 0;
            AABB zone = player.getBoundingBox().inflate(move.range());
            var source = CombatSkillLogic.elementalDamageSource(level, ElementType.ICE, player, null);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, zone)) {
               if (target == player || !target.isAlive() || !CombatSkillLogic.isValidTarget(player, target)) {
                  continue;
               }
               if (target.distanceTo(player) > move.range()) {
                  continue;
               }
               if (!target.hurtServer(level, source, (float) move.damage())) {
                  continue;
               }
               touches++;
               // Gel PROGRESSIF : le palier monte à chaque coup reçu, il ne se repose pas à l'identique.
               FreezeService.apply(target, FreezeService.STAGE_TICKS);
               CombatSkillLogic.applyElementalEffect(target, ElementType.ICE);
            }
            level.sendParticles(move.particle(), player.getX(), player.getY() + 1.0, player.getZ(),
               40, 1.5, 1.0, 1.5, 0.05);
            return true;
         }
         case "icespikes" -> {
            int pics = IceTerrain.raiseSpikes(player, move);
            int touches = 0;
            AABB zone = player.getBoundingBox().inflate(move.range() + 2.0);
            var source = CombatSkillLogic.elementalDamageSource(level, ElementType.ICE, player, null);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, zone)) {
               if (target == player || !target.isAlive() || !CombatSkillLogic.isValidTarget(player, target)) {
                  continue;
               }
               if (target.distanceTo(player) > move.range() + 2.0) {
                  continue;
               }
               if (target.hurtServer(level, source, (float) move.damage())) {
                  touches++;
                  FreezeService.apply(target, FreezeService.STAGE_TICKS / 2);
                  CombatSkillLogic.applyElementalEffect(target, ElementType.ICE);
               }
            }
            LOGGER.info("[MinePiece] Ice Spikes : {} pic(s) dressé(s), {} cible(s) fauchée(s).", pics, touches);
            return true;
         }
         default -> {
            return false; // Ice Block: Partisan / Pheasant Beak restent des projectiles.
         }
      }
   }

   /**
    * <b>FRUIT-ARO_ARO-04 — Aro Move : déplacement sur flèche, arrêté par l'obstacle.</b>
    *
    * <p>Contrat : « Définir un déplacement sur flèche avec arrêt à obstacle. Le joueur n'est pas
    * déplacé dans un bloc ni dans un chunk non autorisé. » ver15 déclarait cette technique en
    * {@code MELEE} générique (une frappe de 15 dégâts) ; le plan propose explicitement d'en faire un
    * déplacement, c'est ce contrat qui est appliqué ici.</p>
    *
    * <p>L'avance se fait par pas de {@value #STEP} bloc, et chaque pas n'est retenu que si la boîte de
    * collision <b>du joueur</b> y est entièrement libre ({@code noCollision}) et si le chunk est chargé.
    * La destination ne peut donc ni traverser un mur, ni se situer dans un bloc, ni sortir dans un
    * chunk non autorisé. La distance est bornée par la portée déclarée de la technique.</p>
    *
    * @return toujours vrai : la technique est prise en charge, mouvement ou pas
    */
   private static boolean aroMoveDash(ServerPlayer player, DevilFruitMove move) {
      ServerLevel level = (ServerLevel) player.level();
      Vec3 direction = player.getLookAngle();
      Vec3 horizontal = new Vec3(direction.x, 0.0, direction.z);
      if (horizontal.lengthSqr() < 1.0E-6) {
         return true;                                   // regard vertical : rien à parcourir
      }
      horizontal = horizontal.normalize();

      double maxDistance = Math.max(1.0, move.range());
      Vec3 origin = player.position();
      Vec3 cursor = origin;
      double travelled = 0.0;

      while (travelled + STEP <= maxDistance) {
         Vec3 candidate = cursor.add(horizontal.scale(STEP));
         BlockPos feet = BlockPos.containing(candidate.x, candidate.y, candidate.z);
         if (!level.hasChunkAt(feet)) {
            break;                                     // chunk non chargé → jamais de téléportation dedans
         }
         AABB box = player.getBoundingBox().move(candidate.subtract(origin));
         if (!level.noCollision(player, box)) {
            break;                                     // obstacle : on s'arrête juste avant
         }
         cursor = candidate;
         travelled += STEP;
      }

      if (travelled <= 0.0) {
         // Obstacle immédiat : la technique aboutit sans déplacement — aucun coût supplémentaire.
         level.sendParticles(MinepieceModParticles.SHOCK_HIT, origin.x, origin.y + 1.0, origin.z, 8, 0.2, 0.2, 0.2, 0.0);
         return true;
      }

      player.teleportTo(cursor.x, cursor.y, cursor.z);
      player.setDeltaMovement(Vec3.ZERO);

      // Traînée de flèche entre le départ et l'arrivée (rendu de la « flèche » du contrat).
      int points = (int) Math.max(2.0, travelled * 4.0);
      for (int i = 0; i <= points; i++) {
         double t = i / (double) points;
         Vec3 p = origin.add(cursor.subtract(origin).scale(t));
         level.sendParticles(MinepieceModParticles.SHOCK_HIT, p.x, p.y + 1.0, p.z, 2, 0.05, 0.05, 0.05, 0.0);
      }
      level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.7F, 1.4F);
      return true;
   }

   /**
    * <b>NIKYU NIKYU</b> — les trois mécaniques propres du contrat du Fruit.
    *
    * <p>Pad Ho est une <b>répulsion orientée</b> : le moteur générique applique des dégâts dans un cône,
    * ce qui n'est pas la même chose que de repousser ce qui est devant sans toucher à ce qui est derrière,
    * ni d'épargner une cible adossée à un mur. Ursus Shock est une onde <b>chargée</b> dont la zone est
    * <b>annoncée</b> avant la frappe. La déviation, elle, n'est pas déclenchée par une touche : elle vit
    * dans le projectile (voir {@link net.mcreator.minepiece.entity.SkillProjectileEntity}).</p>
    */
   private static boolean nikyuNikyu(ServerPlayer player, DevilFruitMove move) {
      return switch (move.id()) {
         case "padho" -> NikyuRepulsion.padHo(player, move);
         case "ursusshock" -> NikyuRepulsion.ursusShock(player, move);
         default -> false;
      };
   }

   /**
    * <b>RIKI RIKI</b> — la force est un <b>multiplicateur</b> adossé à {@code ATTACK_DAMAGE} (la source
    * lit {@code f_22276_}), plafonné et annulé sous scellement ; la saisie est <b>brève</b> et toujours
    * rendue ; la projection s'arrête au mur et la casse épargne les blocs protégés.
    */
   private static boolean rikiRiki(ServerPlayer player, DevilFruitMove move) {
      return switch (move.id()) {
         case "galleonlariat", "attackoverhead", "hadoelbow" -> {
            // Le multiplicateur du Fruit s'applique ici, et pas dans le moteur générique : c'est la
            // règle du contrat, elle doit rester au même endroit que sa preuve.
            double multiplicateur = RikiForce.strengthMultiplier(player);
            var touches = CombatSkillLogic.strikeTargets(player,
               MinepieceModVariables.get(player).PlayerDevilFruit, move, move.damage() * (multiplicateur - 1));
            if (move.id().equals("hadoelbow")) {
               // La prise n'existe que si quelqu'un est saisi : la saisir sans cible serait un mensonge.
               for (net.minecraft.world.entity.LivingEntity cible : touches) {
                  if (RikiForce.grab(player, cible)) {
                     break;
                  }
               }
            }
            yield true;
         }
         case "stonethrowing", "stonethrowinglarge" -> {
            net.minecraft.world.entity.LivingEntity tenue = RikiForce.held(player);
            if (tenue != null) {
               yield RikiForce.throwTarget(player, tenue);
            }
            yield false;
         }
         default -> false;
      };
   }

   /**
    * <b>SORU SORU</b> — deux invocations distinctes (Zeus la foudre, Prometheus le feu), un registre qui
    * compte par propriétaire et par genre (donc un rappel <b>remplace</b> au lieu d'empiler), et des
    * ordres que seul le propriétaire peut donner.
    */
   private static boolean soruSoru(ServerPlayer player, DevilFruitMove move) {
      return switch (move.id()) {
         case "raitei" -> SoruSummons.summon(player, SoruSummons.Kind.RAITEI) != null;
         case "hebunrifire" -> SoruSummons.summon(player, SoruSummons.Kind.HEBUNRI) != null;
         default -> false;
      };
   }

   /**
    * <b>SUPA SUPA</b> — les lames sortent pendant l'attaque et l'allonge suit, les postures se
    * remplacent sans jamais s'empiler, la découpe épargne les blocs protégés.
    */
   private static boolean supaSupa(ServerPlayer player, DevilFruitMove move) {
      return switch (move.id()) {
         case "atomicspurt", "sparbreak", "projectileslash", "sparklingdaisy" -> {
            SupaBlades.activate(player, SupaBlades.BLADE_TICKS);
            SupaBlades.setStance(player, move.id().equals("projectileslash")
               ? SupaBlades.Stance.DEFENSIVE : SupaBlades.Stance.OFFENSIVE);
            double portee = SupaBlades.reach(player, move.range());
            int touches = CombatSkillLogic.meleeStrike(player, move.damage(), portee, move.knockback(),
               move.effect(), move.effectDuration(), move.effectAmplifier());
            SupaBlades.cut(player, SupaBlades.CUT_RADIUS);
            yield touches >= 0;
         }
         default -> false;
      };
   }

   /**
    * <b>UTA UTA</b> — le domaine musical s'ouvre, applique son effet <b>en mesure</b> et se referme par
    * l'une des quatre sorties ; Tot Musica est une invocation adossée à son maître et à une durée.
    */
   private static boolean utaUta(ServerPlayer player, DevilFruitMove move) {
      return switch (move.id()) {
         case "posing" -> UtaDomain.enter(player, UtaDomain.RADIUS);
         case "summontotmusica" -> UtaDomain.summonTotMusica(player) != null;
         default -> false;
      };
   }
}
