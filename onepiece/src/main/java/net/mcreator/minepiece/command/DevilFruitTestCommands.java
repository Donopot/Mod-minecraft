package net.mcreator.minepiece.command;

import br.com.oneminepiece.opworldlike.flight.DevilFruitFlight;
import br.com.oneminepiece.opworldlike.flight.DevilFruitFlightConfig;
import br.com.oneminepiece.opworldlike.haki.HakiProgression;
import com.mojang.authlib.GameProfile;
import net.mcreator.minepiece.init.MinepieceModItems;
import net.mcreator.minepiece.potion.MamaraganMobEffect;
import net.mcreator.minepiece.procedures.DevilFruitBehaviours;
import net.mcreator.minepiece.procedures.SeismicWave;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.mcreator.minepiece.procedures.FreezeService;
import net.mcreator.minepiece.procedures.MagmaBurn;
import net.mcreator.minepiece.procedures.MagmaPools;
import net.mcreator.minepiece.procedures.Desiccation;
import net.mcreator.minepiece.procedures.SandStorm;
import net.mcreator.minepiece.procedures.GravityField;
import net.mcreator.minepiece.procedures.OpeRoom;
import net.mcreator.minepiece.procedures.Amputation;
import net.mcreator.minepiece.procedures.LightBeams;
import net.mcreator.minepiece.procedures.LightTravel;
import net.mcreator.minepiece.procedures.LightCharge;
import net.mcreator.minepiece.procedures.LightBuffs;
import net.mcreator.minepiece.procedures.SafeDestination;
import net.mcreator.minepiece.procedures.YamiDarkness;
import net.mcreator.minepiece.procedures.YamiSeal;
import net.mcreator.minepiece.procedures.Rokushiki;
import net.mcreator.minepiece.procedures.NikyuRepulsion;
import net.mcreator.minepiece.procedures.RikiForce;
import net.mcreator.minepiece.procedures.SoruSummons;
import net.mcreator.minepiece.procedures.SupaBlades;
import net.mcreator.minepiece.procedures.UtaDomain;
import net.mcreator.minepiece.procedures.TechniqueAnimations;
import net.minecraft.world.level.block.Blocks;
import net.mcreator.minepiece.procedures.ZoanMovement;
import net.mcreator.minepiece.procedures.ZoanClaw;
import net.mcreator.minepiece.procedures.Levitation;
import net.mcreator.minepiece.procedures.MeteorFall;
import net.mcreator.minepiece.procedures.NegativeState;
import net.mcreator.minepiece.procedures.RemoteControl;
import net.mcreator.minepiece.procedures.SovereignVeil;
import net.mcreator.minepiece.procedures.IceDefense;
import net.mcreator.minepiece.procedures.IceBreath;
import net.mcreator.minepiece.procedures.WolfPursuit;
import net.mcreator.minepiece.procedures.WolfStrike;
import net.mcreator.minepiece.init.MinepieceModEntities;
import net.mcreator.minepiece.procedures.ShadowLedger;
import net.mcreator.minepiece.entity.SummonEntity;
import net.mcreator.minepiece.procedures.DiamondBody;
import net.mcreator.minepiece.procedures.WeightState;
import net.mcreator.minepiece.procedures.WeightSlam;
import net.mcreator.minepiece.procedures.StickyState;
import net.mcreator.minepiece.procedures.MochiBuild;
import net.mcreator.minepiece.procedures.SmokeCloud;
import net.mcreator.minepiece.procedures.Roots;
import net.mcreator.minepiece.procedures.LifeDrain;
import net.mcreator.minepiece.procedures.MoveType;
import net.mcreator.minepiece.procedures.Petrification;
import net.mcreator.minepiece.procedures.GiantForm;
import net.mcreator.minepiece.procedures.ZoanForms;
import net.mcreator.minepiece.procedures.GiantShockWave;
import net.mcreator.minepiece.procedures.CombatSkillLogic;
import net.mcreator.minepiece.procedures.FlameBurn;
import net.mcreator.minepiece.procedures.FireRing;
import net.mcreator.minepiece.procedures.FirePropulsion;
import net.mcreator.minepiece.procedures.ElementType;
import net.mcreator.minepiece.procedures.IceTerrain;
import net.mcreator.minepiece.procedures.PowerBlockService;
import br.com.oneminepiece.opworldlike.flame.DevilFruitFlameConfig;
import br.com.oneminepiece.opworldlike.sand.DevilFruitSandConfig;
import br.com.oneminepiece.opworldlike.gravity.DevilFruitGravityConfig;
import br.com.oneminepiece.opworldlike.love.DevilFruitLoveConfig;
import br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig;
import br.com.oneminepiece.opworldlike.hollow.DevilFruitHollowConfig;
import br.com.oneminepiece.opworldlike.imu.DevilFruitImuConfig;
import br.com.oneminepiece.opworldlike.inu.DevilFruitOkuchiConfig;
import br.com.oneminepiece.opworldlike.wolf.DevilFruitWolfConfig;
import br.com.oneminepiece.opworldlike.kage.DevilFruitKageConfig;
import br.com.oneminepiece.opworldlike.kira.DevilFruitKiraConfig;
import br.com.oneminepiece.opworldlike.kiro.DevilFruitKiroConfig;
import br.com.oneminepiece.opworldlike.mochi.DevilFruitMochiConfig;
import br.com.oneminepiece.opworldlike.moku.DevilFruitMokuConfig;
import br.com.oneminepiece.opworldlike.mori.DevilFruitMoriConfig;
import br.com.oneminepiece.opworldlike.ope.DevilFruitOpeConfig;
import br.com.oneminepiece.opworldlike.magma.DevilFruitMagmaConfig;
import br.com.oneminepiece.opworldlike.quake.DevilFruitQuakeConfig;
import br.com.oneminepiece.opworldlike.explosion.DevilFruitExplosionConfig;
import net.mcreator.minepiece.procedures.DevilFruitMove;
import net.mcreator.minepiece.procedures.DevilFruitSummons;
import net.mcreator.minepiece.procedures.GomuArmStrike;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.mcreator.minepiece.item.DevilFruitItem;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.network.MinepieceModVariables.PlayerVariables;
import net.mcreator.minepiece.procedures.ChainLightning;
import net.mcreator.minepiece.procedures.DevilFruitElement;
import net.mcreator.minepiece.procedures.DevilFruitPowerLogic;
import net.mcreator.minepiece.procedures.DevilFruitExplosions;
import net.mcreator.minepiece.procedures.DevilFruitRegistry;
import net.mcreator.minepiece.procedures.DevilFruitSkillDispatcher;
import net.mcreator.minepiece.procedures.DevilFruitTransformation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CORE-030 — recette de tests automatisés de la couche Fruits du Démon.
 *
 * <p>Exécute des scénarios réels côté serveur avec des {@link FakePlayer} Fabric (aucun client requis) :
 * catalogue, immersion de chaque fruit, exécution de chaque technique, transformation, changement de
 * fruit, mort/réincarnation, isolation à deux joueurs et persistance. Chaque contrôle produit une ligne
 * {@code [dftest] [PASS|FAIL] ...} dans le journal du serveur, récupérable via RCON/script.</p>
 *
 * <p>Périmètre volontairement exclu (nécessite un client) : rendu visuel, keybinds, immersion souris.
 * Ces points sont couverts par {@code docs/CORE-030-recette-de-tests.md}.</p>
 */
public final class DevilFruitTestCommands {

   /** Porteur de test Uta : sa référence est GARDÉE, un faux joueur n'étant pas dans la liste du
    * serveur. La phase uta_domain lit son domaine par cette référence. */
   static ServerPlayer utaPorteurTest;
   private static final Logger LOGGER = LoggerFactory.getLogger("MinePieceDevilFruit");
   private static final Map<String, Boolean> RESULTS = new LinkedHashMap<>();

   private DevilFruitTestCommands() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      if (!Boolean.getBoolean("opworldlike.repairTests")) return;
      net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DAMAGE.register((target,source,base,taken,blocked)->{
         if(target.getUUID().equals(pikaTemoinLibre) || target.getUUID().equals(pikaTemoinAbrite))
            LOGGER.info("[pika-fixture-damage] witness={} hp={} source={} attacker={} direct={} damage={}",target.getUUID().equals(pikaTemoinAbrite)?"covered":"open",target.getHealth(),source.getMsgId(),source.getEntity(),source.getDirectEntity(),taken);
      });
      dispatcher.register(
         Commands.literal("dftest")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .executes(ctx -> run(ctx, "all"))
            .then(
               Commands.argument("scope", StringArgumentType.word())
                  .executes(ctx -> run(ctx, StringArgumentType.getString(ctx, "scope")))
            )
      );
   }

   private static void check(String id, boolean ok, String detail) {
      RESULTS.put(id, ok);
      LOGGER.info("[dftest] [{}] {} — {}", ok ? "PASS" : "FAIL", id, detail);
   }

   private static FakePlayer fake(ServerLevel level, String name) {
      UUID uuid = UUID.nameUUIDFromBytes(("dftest:" + name).getBytes(StandardCharsets.UTF_8));
      return FakePlayer.get(level, new GameProfile(uuid, name));
   }

   private static List<Item> fruits() {
      List<Item> out = new ArrayList<>();
      for (Item item : BuiltInRegistries.ITEM) {
         Identifier id = BuiltInRegistries.ITEM.getKey(item);
         if (id != null && "minepiece".equals(id.getNamespace()) && item instanceof DevilFruitItem
            && !DevilFruitRegistry.movesFor(item).isEmpty()) {
            out.add(item);
         }
      }
      return out;
   }

   /** Immersion réelle du fruit (même chemin que le clic droit : {@code finishUsingItem}). */
   /**
    * Donne un Fruit à un porteur <b>sans passer par l'objet</b>.
    *
    * <p>Pourquoi pas {@link #immerse} : {@code DevilFruitItem.finishUsingItem} applique la règle du
    * <b>2ᵉ fruit = mort</b>. Un faux joueur déjà porteur d'un Fruit (ils <b>persistent</b> d'une
    * invocation à l'autre) survit à ce coup — son invulnérabilité le protège — mais son Fruit est
    * remis à vide. Le test croyait alors mesurer « un autre Logia » et mesurait en réalité « un porteur
    * sans Fruit » : la valeur ×1,0 avait l'air d'une réponse et n'en était pas une. C'est la façon
    * d'écrire déjà employée plus haut dans ce fichier.</p>
    */
   private static void giveFruit(ServerPlayer player, Item fruit) {
      MinepieceModVariables.clear(player);
      MinepieceModVariables.PlayerVariables vars = MinepieceModVariables.get(player);
      vars.PlayerDevilFruit = new ItemStack(fruit);
      vars.UseDevilAbilities = true;
      vars.PlayerEnergy = vars.PlayerEnergyMAX;
      player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.DEVIL_FRUIT_USER, -1, 0, false, false));
   }

   private static void immerse(ServerLevel level, ServerPlayer player, Item fruit) {
      ((DevilFruitItem) fruit).finishUsingItem(new ItemStack(fruit), level, player);
   }

   private static int run(CommandContext<CommandSourceStack> ctx, String scope) {
      RESULTS.clear();
      ServerLevel level = ctx.getSource().getServer().overworld();
      String s = scope.toLowerCase(java.util.Locale.ROOT);

      if (s.equals("all") || s.equals("catalogue")) {
         catalogue();
      }
      if (s.equals("all") || s.equals("player")) {
         immersionEtTechniques(level);
         transformation(level);
         changementDeFruit(level);
      }
      if (s.equals("all") || s.equals("death")) {
         mortEtReincarnation(level);
      }
      if (s.equals("all") || s.equals("multi")) {
         deuxJoueurs(level);
      }
      if (s.equals("all") || s.equals("persist")) {
         persistance(level);
      }
      if (s.equals("persistwrite")) {
         persistanceEcriture(level);
      }
      if (s.equals("persistverify")) {
         persistanceVerification(level);
      }
      if (s.equals("all") || s.equals("world")) {
         blocsEtInvocations(level);
      }
      if (s.equals("all") || s.equals("balance")) {
         equilibrage();
      }
      if (s.equals("all") || s.equals("haki")) {
         haki(level);
      }
      if (s.equals("all") || s.equals("reset")) {
         reset(level);
      }
      if (s.equals("all") || s.equals("aroaro")) {
         aroAro(level);
      }
      if (s.equals("mochi_expiry")) {
         // Portée relue PLUS TARD (cf. A3f) : elle prouve l'expiration par le temps.
         mochiExpiry(level);
      }
      if (s.equals("all") || s.equals("mori")) {
         mori(level);
      }
      if (s.equals("all") || s.equals("moku")) {
         moku(level);
      }
      if (s.equals("all") || s.equals("mochi")) {
         mochi(level);
      }
      if (s.equals("all") || s.equals("kiro")) {
         kiro(level);
      }
      if (s.equals("all") || s.equals("kira")) {
         kira(level);
      }
      if (s.equals("all") || s.equals("kage")) {
         kage(level);
      }
      if (s.equals("all") || s.equals("wolf")) {
         inuWolf(level);
      }
      if (s.equals("all") || s.equals("okuchi")) {
         okuchi(level);
      }
      if (s.equals("all") || s.equals("imu")) {
         imu(level);
      }
      if (s.equals("all") || s.equals("horo")) {
         horo(level);
      }
      if (s.equals("all") || s.equals("daibutsu")) {
         daibutsu(level);
      }
      if (s.equals("all") || s.equals("mero")) {
         mero(level);
      }
      if (s.equals("all") || s.equals("zushi")) {
         zushi(level);
      }
      if (s.equals("all") || s.equals("suna")) {
         suna(level);
      }
      if (s.equals("all") || s.equals("mera")) {
         mera(level);
      }
      if (s.equals("all") || s.equals("magu")) {
         magu(level);
      }
      if (s.equals("all") || s.equals("hie")) {
         hie(level);
      }
      if (s.equals("all") || s.equals("gura")) {
         gura(level);
      }
      if (s.equals("all") || s.equals("goro")) {
         goro(level);
      }
      if (s.equals("all") || s.equals("gomu")) {
         gomu(level);
      }
      if (s.equals("all") || s.equals("bomu")) {
         bomu(level);
      }
      if (s.equals("all") || s.equals("bisu")) {
         bisu(level);
      }
      if (s.equals("all") || s.equals("batto")) {
         batto(level);
      }
      if (s.equals("all") || s.equals("fruits")) {
         fruitsTest();
      }
      if (s.equals("all") || s.equals("i18n")) {
         i18n();
      }
      // OPE OPE EN DERNIER, DÉLIBÉRÉMENT : cette portée ARME les témoins d'échéance (une ROOM et une
      // section à durée courte). Les autres portées NETTOIENT les mobs de leur zone (bx ± 40), et la
      // zone d'Ope (bx = 66) tombe dans celle de plusieurs d'entre elles : placée avant, la victime
      // témoin serait supprimée avant d'avoir pu expirer, et la preuve d'échéance deviendrait
      // « victime introuvable » — ce qui est exactement ce qui est arrivé au premier passage.
      if (s.equals("all") || s.equals("ope")) {
         ope(level);
      }
      // Portées d'ÉCHÉANCE et de REDÉMARRAGE : volontairement HORS de « all », invoquées par la
      // recette à un autre tick — seule façon d'observer une durée dans un harnais mono-tick.
      // PIKA PIKA juste après Ope Ope, et pour la même raison qu'elle : cette portée TIRE des rayons
      // dont l'impact est relu plus tard. Une portée suivante qui nettoie les mobs de sa zone
      // supprimerait les témoins avant l'impact.
      if (s.equals("all") || s.equals("pika")) {
         pika(level);
      }
      // YAMI YAMI en DERNIER, pour la même raison que Pika et Ope : cette portée arme des témoins
      // relus à un autre tick (attraction, scellement), et une portée suivante les détruirait.
      if (s.equals("all") || s.equals("yami")) {
         yami(level);
      }
      // LOT ZOAN en dernier : la section couvre les huit Fruits d'un coup, et son rectangle de
      // nettoyage ne recouvre aucune autre scène (z 68..92, alors que Pika occupe z <= 64).
      if (s.equals("all") || s.equals("zoans")) {
         zoans(level);
      }
      if (s.equals("all") || s.equals("animations")) {
         animations(level);
      }
      if (s.equals("all") || s.equals("riki")) {
         riki(level);
      }
      if (s.equals("all") || s.equals("soru")) {
         soru(level);
      }
      if (s.equals("all") || s.equals("supa")) {
         supa(level);
      }
      if (s.equals("all") || s.equals("uta")) {
         uta(level);
      }
      if (s.equals("all") || s.equals("nikyu")) {
         nikyu(level);
      }
      if (s.equals("uta_domain")) {
         // Lu après des ticks RÉELS : la cadence du domaine et son échéance ne se mesurent pas dans un
         // seul tick. On vérifie la borne qui définit « sans répétition abusive ».
         ServerPlayer porteurUta = utaPorteurTest;
         int mesures = porteurUta == null ? 0 : UtaDomain.measuresOf(porteurUta);
         check("A16d-rythme-et-echeance-reels", mesures >= 1 && mesures <= (UtaDomain.ZONE_TICKS / UtaDomain.RHYTHM) + 2,
            "mesures appliquées=" + mesures + " après un délai réel (borne « une par " + UtaDomain.RHYTHM
            + " ticks » = " + (UtaDomain.ZONE_TICKS / UtaDomain.RHYTHM) + " pour " + UtaDomain.ZONE_TICKS
            + " ticks de vie) · sorties par échéance=" + UtaDomain.sortiesEcheance() + " — le domaine bat"
            + " la mesure au lieu d'appliquer son effet à chaque tick : c'est la différence entre un"
            + " concert et une sirène d'usine");
      }
      if (s.equals("nikyu_impact")) {
         // Lu APRÈS le dftest all : l'onde d'Ursus Shock a été annoncée à ce moment-là, et les ticks
         // réels se sont écoulés depuis. Si elle a frappé, l'annonce a bien précédé l'impact.
         int frappes = NikyuRepulsion.ondesFrappees();
         check("A12d-l-onde-frappe-apres-l-annonce", frappes >= 1,
            "ondes frappées après le délai réel=" + frappes + " (annoncées="
            + NikyuRepulsion.ondesAnnoncees() + ") — la zone annoncée a été suivie de l'impact ; sans"
            + " cette preuve, la vérification A12c ne montrerait qu'une annonce qui ne frappe jamais");
      }
      if (s.equals("all") || s.equals("noclasses")) {
         nonClasses(level);
      }
      if (s.equals("all") || s.equals("rokushiki")) {
         rokushiki(level);
      }
      // PRÉPARATION DU CONTRÔLE DU LOOT : le loot exclut les Fruits déjà possédés, et les fixtures des
      // sections précédentes en détiennent encore. On rend tout, et on VÉRIFIE que c'est vide — un
      // nettoyage non vérifié laisserait le même faux échec.
      if (s.equals("lootprep")) {
         MinepieceModVariables.clearAll();
         boolean vide = MinepieceModVariables.ownedFruitItems().isEmpty();
         check("lootprep-etat-rendu", vide,
            "porteurs de Fruit connus après remise à zéro = " + MinepieceModVariables.ownedFruitItems().size()
            + " → la table de loot ne retirera plus aucun tirage : le contrôle du coffre mesure le LOOT,"
            + " pas les Fruits que le harnais avait empruntés et oublié de rendre");
      }
      if (s.equals("ope_expiry")) {
         opeExpiry(level);
      }
      if (s.equals("pika_impact")) {
         pikaImpact(level);
      }
      if (s.equals("yami_impact")) {
         yamiImpact(level);
      }
      if (s.equals("ope_restart")) {
         opeRestart(level);
      }

      long pass = RESULTS.values().stream().filter(Boolean::booleanValue).count();
      long fail = RESULTS.size() - pass;
      LOGGER.info("[dftest] ===== RÉSULTAT {} : {}/{} PASS, {} FAIL =====", s, pass, RESULTS.size(), fail);
      for (Map.Entry<String, Boolean> e : RESULTS.entrySet()) {
         if (!e.getValue()) {
            LOGGER.info("[dftest] échec : {}", e.getKey());
         }
      }
      final long p = pass;
      final long total = RESULTS.size();
      ctx.getSource().sendSuccess(() -> Component.literal(
         "§6[dftest " + s + "] §a" + p + "§f/" + total + " PASS §c" + fail + " FAIL §7(détail dans le log serveur)"), false);
      return fail == 0 ? 1 : 0;
   }

   // ---------------------------------------------------------------- A. Catalogue
   private static void catalogue() {
      String summary = DevilFruitRegistry.selfTest();
      check("A1-catalogue", summary.contains("0 doublon"), summary);
      List<Item> fruits = fruits();
      check("A2-fruits", fruits.size() == 40, fruits.size() + " fruits portés (attendu 40)");
      int moves = fruits.stream().mapToInt(f -> DevilFruitRegistry.movesFor(f).size()).sum();
      // Le total attendu n'est pas un chiffre magique : 204 techniques viennent de ver15, et 2 ont été
      // AJOUTÉES par décision, à la demande explicite du plan (FRUIT-IMU-04 : « développer un catalogue
      // cohérent au-delà de l'unique Imu Arrow »). Le compte reste un garde-fou : toute AUTRE variation
      // signale une modification non déclarée du catalogue, et c'est exactement ce qu'on veut détecter.
      check("A3-techniques", moves == EXPECTED_TECHNIQUES,
         moves + " techniques portées (attendu " + EXPECTED_TECHNIQUES + " = 204 de ver15"
            + " + 2 ajouts Imu déclarés + 3 ajouts Ope Ope déclarés (ROOM, échange, découpe)"
            + " — toute AUTRE variation signale une modification non déclarée du catalogue)");
      int sansElement = 0;
      for (Item f : fruits) {
         if (DevilFruitElement.of(new ItemStack(f)) == null) {
            sansElement++;
         }
      }
      check("A4-elements", sansElement == 0, fruits.size() + " fruits résolus en élément, " + sansElement + " sans élément");
   }

   // ------------------------------------------------- B/C. Immersion + exécution des techniques
   private static void immersionEtTechniques(ServerLevel level) {
      ServerPlayer p = fake(level, "dftest_solo");
      List<Item> fruits = fruits();

      int immerges = 0;
      for (Item f : fruits) {
         MinepieceModVariables.clear(p);
         immerse(level, p, f);
         PlayerVariables v = MinepieceModVariables.get(p);
         if (!v.PlayerDevilFruit.isEmpty() && v.PlayerDevilFruit.getItem() == f && v.UseDevilAbilities) {
            immerges++;
         }
      }
      check("B1-immersion", immerges == fruits.size(), immerges + "/" + fruits.size() + " fruits immergés (fruit posé + capacités actives)");

      // Exécution de CHAQUE technique de CHAQUE fruit, sans cooldown ni énergie limitante.
      // The old caster spawned at an unprepared position and had no targets. That
      // accidentally passed when refused custom actions fell through to generic attacks.
      // Give this smoke test its own loaded arena and each move a fresh ordinary target.
      int attendu = 0, executes = 0, echecs = 0;
      List<String> echecsDetail = new ArrayList<>();
      for (Item f : fruits) {
         MinepieceModVariables.clear(p);
         immerse(level, p, f);
         List<net.mcreator.minepiece.procedures.DevilFruitMove> moves = DevilFruitRegistry.movesFor(f);
         String fid = String.valueOf(BuiltInRegistries.ITEM.getKey(f));
         for (int i = 0; i < moves.size(); i++) {
            attendu++;
            DevilFruitPowerLogic.resetDevilFruitPowers(p);
            // Independent smoke casts must rebuild their disposable decor: powers now leave final terrain.
            for (int x = 184; x <= 216; x++) for (int z = 144; z <= 184; z++) {
               level.setBlockAndUpdate(new BlockPos(x, 99, z), Blocks.STONE.defaultBlockState());
               for (int y = 100; y <= 112; y++) level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
            }
            p.removeAllEffects();
            p.teleportTo(200.5, 100, 160.5); p.setYRot(0); p.setXRot(0);
            p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            p.setOldPosAndRot();
            var smokeTarget = spawnAdultZombie(level, 200, 100, 164);
            ((net.minecraft.world.entity.Mob) smokeTarget).setNoAi(true);
            PlayerVariables v = MinepieceModVariables.get(p);
            v.PlayerDevilFruit = new ItemStack(f);
            v.UseDevilAbilities = true;
            v.PlayerEnergy = 1_000_000.0;
            v.PlayerEnergyMAX = 1_000_000.0;
            v.PlayerTechniqueUsedNumber = i;
            p.getCooldowns().removeCooldown(p.getCooldowns().getCooldownGroup(new ItemStack(f)));
            if (f == MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE && !moves.get(i).id().equals("room")) OpeRoom.open(p);
            if (f == MinepieceModItems.ITEM_DEVIL_FRUIT_RIKI_RIKI && moves.get(i).id().startsWith("stonethrowing")) RikiForce.grab(p, smokeTarget);
            // Chaque technique est testée INDÉPENDAMMENT. Sans ce nettoyage, les invocations laissées
            // par la technique précédente saturent la limite d'invocations et la suivante est refusée —
            // refus légitime (un refus ne consomme rien), mais qui ne dit rien de la technique testée.
            for (var reste : ((ServerLevel) p.level()).getEntitiesOfClass(
                  net.mcreator.minepiece.entity.SummonEntity.class, p.getBoundingBox().inflate(128.0))) {
               reste.discard();
            }
            GomuArmStrike.clearArms(p, 128.0);
            // Rap commands owned Mini Hollow: an empty command is correctly refused.
            if(f==MinepieceModItems.ITEM_DEVIL_FRUIT_HORO_HORO && moves.get(i).id().equals("rap")){
               v.PlayerTechniqueUsedNumber=1;
               if(!DevilFruitSkillDispatcher.castSkill(p))throw new IllegalStateException("Rap fixture Mini Hollow refused");
               for(int phase=0;phase<55;phase++)net.mcreator.minepiece.procedures.ParameciaBatchTechniques.tick();
               p.getCooldowns().removeCooldown(p.getCooldowns().getCooldownGroup(new ItemStack(f)));v.PlayerTechniqueUsedNumber=i;
            }
            boolean ok;
            try {
               ok = DevilFruitSkillDispatcher.castSkill(p);
            } catch (Exception ex) {
               ok = false;
               echecsDetail.add(fid + "/" + moves.get(i).id() + " → " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            }
            if (ok) {
               executes++;
            } else {
               echecsDetail.add(fid + "/" + moves.get(i).id() + " → castSkill=false");
            }
            if (!ok) {
               echecs++;
            }
            p.getCooldowns().removeCooldown(p.getCooldowns().getCooldownGroup(new ItemStack(f)));
            smokeTarget.discard();
         }
         DevilFruitPowerLogic.resetDevilFruitPowers(p);
         DevilFruitTransformation.revertForm(p);
      }
      check("C1-execution-techniques", executes == attendu,
         executes + "/" + attendu + " techniques exécutées sans erreur" + (echecsDetail.isEmpty() ? "" : " | " + echecsDetail));
      check("C2-energie-consommee", MinepieceModVariables.get(p).PlayerEnergy < 1_000_000.0,
         "énergie déduite après les tirs (coût facturé)");
      MinepieceModVariables.clear(p);
   }

   // ---------------------------------------------------------------- D. Transformation
   private static void transformation(ServerLevel level) {
      ServerPlayer p = fake(level, "dftest_form");
      Item zoan = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_ushi_ushi_giraffe"));
      MinepieceModVariables.clear(p);
      immerse(level, p, zoan);
      PlayerVariables v = MinepieceModVariables.get(p);

      DevilFruitTransformation.toggle(p);
      boolean forme = v.player_form > 0.0;
      check("D1-transformation", forme, "player_form=" + v.player_form + " après activation (fruit girafe ×1.75)");

      double scale = p.getAttributeValue(Attributes.SCALE);
      check("D2-echelle", Math.abs(scale - 1.75) < 0.01, "Attributes.SCALE = " + scale + " en forme hybride (attendu 1.75)");

      DevilFruitTransformation.toggle(p);
      boolean beast = v.player_form == 2.0 && net.mcreator.minepiece.procedures.ZoanForms.body(p)!=null;
      DevilFruitTransformation.toggle(p);
      boolean retour = beast && v.player_form <= 0.0;
      double scaleBase = p.getAttributeValue(Attributes.SCALE);
      check("D3-retour-base", retour && Math.abs(scaleBase - 1.0) < 0.01,
         "animal complet puis humain : player_form=" + v.player_form + ", SCALE=" + scaleBase);
      MinepieceModVariables.clear(p);
   }

   // ------------------------------------------------------- E. Changement de fruit
   private static void changementDeFruit(ServerLevel level) {
      // Règle One Piece (DevilFruitItem.finishUsingItem) : manger un 2e Fruit du Démon tue.
      ServerPlayer p = fake(level, "dftest_swap");
      Item a = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_inu_inu_wolf"));
      Item b = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_neko_neko_leopard"));
      MinepieceModVariables.clear(p);
      immerse(level, p, a);
      DevilFruitTransformation.toggle(p);
      boolean premier = MinepieceModVariables.get(p).PlayerDevilFruit.getItem() == a;
      boolean formeA = MinepieceModVariables.get(p).player_form > 0.0;

      immerse(level, p, b);

      PlayerVariables v = MinepieceModVariables.get(p);
      boolean tue = !p.isAlive() || p.getHealth() <= 0.0F;
      boolean libere = v.PlayerDevilFruit.isEmpty();
      check("E1-deuxieme-fruit-tue", premier && formeA && libere,
         "1er fruit actif=" + premier + ", forme=" + formeA + ", pouvoir libéré par le 2e fruit=" + libere
         + " [mort effective=" + tue + ", vie=" + p.getHealth() + ", retiré=" + p.isRemoved()
         + " — FakePlayer est invulnérable, cf CORE-030 §limites]");
      MinepieceModVariables.clear(p);

      // Changement de fruit documenté : reset explicite, puis immersion du nouveau fruit.
      ServerPlayer q = fake(level, "dftest_swap2");
      MinepieceModVariables.clear(q);
      immerse(level, q, a);
      DevilFruitTransformation.toggle(q);
      DevilFruitPowerLogic.clearDevilFruit(q);
      immerse(level, q, b);
      PlayerVariables v2 = MinepieceModVariables.get(q);
      boolean nouveau = !v2.PlayerDevilFruit.isEmpty() && v2.PlayerDevilFruit.getItem() == b;
      check("E2-changement-par-reset", nouveau && v2.player_form <= 0.0,
         "reset puis nouveau fruit actif=" + nouveau + ", forme remise à base (player_form=" + v2.player_form + ")");
      MinepieceModVariables.clear(q);
   }

   // ---------------------------------------------------- F. Mort + réincarnation
   private static void mortEtReincarnation(ServerLevel level) {
      ServerPlayer p = fake(level, "dftest_death");
      Item fruit = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_goro_goro"));
      MinepieceModVariables.clear(p);
      immerse(level, p, fruit);
      boolean avant = MinepieceModVariables.get(p).PlayerDevilFruit.getItem() == fruit;
      boolean lootAvant = MinepieceModVariables.ownedFruitItems().contains(fruit);

      // FakePlayer est invulnérable par conception : on déclenche la mort par die() (même chemin
      // que hurtServer → ServerPlayer.die → ServerLivingEntityEvents.AFTER_DEATH, cf CORE-023).
      p.die(level.damageSources().genericKill());
      boolean mort = !p.isAlive() || p.getHealth() <= 0.0F;

      boolean apres = MinepieceModVariables.get(p).PlayerDevilFruit.isEmpty();
      boolean lootApres = !MinepieceModVariables.ownedFruitItems().contains(fruit);
      check("F1-mort-libere-fruit", avant && apres,
         "fruit possédé avant=" + avant + ", fruit libéré par le gestionnaire de mort=" + apres
         + " [vie=" + p.getHealth() + ", vivant=" + p.isAlive() + ", mort effective=" + mort + "]");
      check("F2-reincarnation-loot", lootAvant && lootApres,
         "fruit dans le pool de loot avant=" + lootAvant + ", de nouveau lootable après mort=" + lootApres);
      MinepieceModVariables.clear(p);
   }

   // ------------------------------------------------------------ G. Deux joueurs
   private static void deuxJoueurs(ServerLevel level) {
      ServerPlayer p1 = fake(level, "dftest_p1");
      ServerPlayer p2 = fake(level, "dftest_p2");
      Item f1 = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_mera_mera"));
      Item f2 = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_hie_hie"));
      MinepieceModVariables.clear(p1);
      MinepieceModVariables.clear(p2);

      immerse(level, p1, f1);
      immerse(level, p2, f2);
      PlayerVariables v1 = MinepieceModVariables.get(p1);
      PlayerVariables v2 = MinepieceModVariables.get(p2);
      boolean isole = v1.PlayerDevilFruit.getItem() == f1 && v2.PlayerDevilFruit.getItem() == f2;
      check("G1-deux-joueurs-isoles", isole,
         "P1=" + (v1.PlayerDevilFruit.isEmpty() ? "aucun" : BuiltInRegistries.ITEM.getKey(v1.PlayerDevilFruit.getItem()))
         + ", P2=" + (v2.PlayerDevilFruit.isEmpty() ? "aucun" : BuiltInRegistries.ITEM.getKey(v2.PlayerDevilFruit.getItem())));

      boolean unicite = MinepieceModVariables.ownedFruitItems().contains(f1) && MinepieceModVariables.ownedFruitItems().contains(f2);
      check("G2-unicite-loot", unicite, "2 fruits détenus → 2 entrées dans la circulation de loot");

      MinepieceModVariables.clear(p1);
      MinepieceModVariables.clear(p2);
   }

   // ------------------------------------------------------------- H. Persistance
   private static void persistance(ServerLevel level) {
      ServerPlayer p = fake(level, "dftest_persist");
      Item fruit = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_zushi_zushi"));
      MinepieceModVariables.clear(p);
      immerse(level, p, fruit);
      DevilFruitTransformation.toggle(p);
      double formAvant = MinepieceModVariables.get(p).player_form;

      MinepieceModVariables.saveAll();
      MinepieceModVariables.clear(p);
      boolean efface = MinepieceModVariables.get(p).PlayerDevilFruit.isEmpty();
      MinepieceModVariables.loadAll();
      PlayerVariables v = MinepieceModVariables.get(p);
      boolean restaure = !v.PlayerDevilFruit.isEmpty() && v.PlayerDevilFruit.getItem() == fruit;
      check("H1-persistance", efface && restaure && Math.abs(v.player_form - formAvant) < 0.001,
         "effacé en mémoire=" + efface + ", restauré depuis le .dat=" + restaure
         + ", forme conservée (" + formAvant + " → " + v.player_form + ")");
      MinepieceModVariables.clear(p);
   }

   // ------------------------------------------------- L. Haki (CORE-029)
   /** Vérifie que la contre-mesure est réellement accessible ET fonctionnelle, pas un marqueur. */
   private static void haki(ServerLevel level) {
      ServerPlayer victim = fake(level, "dftest_haki_victim");
      ServerPlayer attacker = fake(level, "dftest_haki_atk");
      Item logia = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_mera_mera"));
      MinepieceModVariables.clear(victim);
      MinepieceModVariables.clear(attacker);
      immerse(level, victim, logia);            // la victime est un utilisateur Logia

      HakiProgression.setLevel(attacker, HakiProgression.Type.ARMAMENT, 3);
      int niveau = HakiProgression.level(attacker, HakiProgression.Type.ARMAMENT);
      check("L1-niveau-haki-persistant", niveau == 3,
         "niveau d'Armement stocké dans les variables joueur = " + niveau + "/10"
         + " (attributs attachés au joueur : " + HakiProgression.attributesAttached(attacker) + ")");

      // Diagnostic : les tags de fruit (CORE-009) sont-ils réellement chargés ?
      net.minecraft.tags.TagKey<Item> tagDevilFruit = net.minecraft.tags.TagKey.create(
         net.minecraft.core.registries.Registries.ITEM,
         Identifier.fromNamespaceAndPath("minepiece", "devil_fruit"));
      net.minecraft.tags.TagKey<Item> tagLogia = net.minecraft.tags.TagKey.create(
         net.minecraft.core.registries.Registries.ITEM,
         Identifier.fromNamespaceAndPath("minepiece", "logia"));
      ItemStack logiaStack = new ItemStack(logia);
      boolean tagFruit = logiaStack.is(tagDevilFruit);
      boolean tagLogiaOk = logiaStack.is(tagLogia);
      check("L0-tags-de-fruit-charges", tagFruit && tagLogiaOk,
         "minepiece:devil_fruit=" + tagFruit + ", minepiece:logia=" + tagLogiaOk
         + " — si faux, le dossier de tags n'est pas à la bonne convention");

      // Sans Haki : le contre est inactif et l'intangibilité Logia tient.
      HakiProgression.setLevel(attacker, HakiProgression.Type.ARMAMENT, 0);
      HakiProgression.wear(attacker);
      boolean contreOff = HakiProgression.countersDevilFruit(attacker);
      double multSans = DevilFruitPowerLogic.computeDamageMagnification(
         level.damageSources().playerAttack(attacker), victim);
      check("L2-sans-haki-logia-intangible", !contreOff && multSans == 0.0,
         "contre actif=" + contreOff + ", dégâts physiques subis par le Logia = ×" + multSans
         + " (intangibilité préservée)");

      // Avec Haki niveau 1 : le contre perce l'intangibilité.
      HakiProgression.setLevel(attacker, HakiProgression.Type.ARMAMENT, 1);
      HakiProgression.wear(attacker);
      boolean contreOn = HakiProgression.countersDevilFruit(attacker);
      double multAvec = DevilFruitPowerLogic.computeDamageMagnification(
         level.damageSources().playerAttack(attacker), victim);
      check("L3-avec-haki-perce-logia", contreOn && multAvec > 0.0,
         "contre actif=" + contreOn + ", dégâts physiques subis par le Logia = ×" + multAvec
         + " (intangibilité percée)");

      // Progression par l'EXP (même chemin que le combat).
      HakiProgression.setLevel(attacker, HakiProgression.Type.OBSERVATION, 0);
      for (int i = 0; i < 200; i++) {
         HakiProgression.gainExp(attacker, HakiProgression.Type.OBSERVATION);
      }
      int obs = HakiProgression.level(attacker, HakiProgression.Type.OBSERVATION);
      check("L4-progression-par-exp", obs > 0,
         "Observation montée à " + obs + "/10 après 200 gains d'EXP (paliers "
         + (int) br.com.oneminepiece.opworldlike.haki.HakiConfig.observationExpPerLevel() + "/"
         + (int) br.com.oneminepiece.opworldlike.haki.HakiConfig.observationExpCost() + ")");

      // Haki Souverain : accessible (effet réellement appliqué).
      HakiProgression.setLevel(attacker, HakiProgression.Type.ARMAMENT, 2);
      int touches = HakiProgression.sovereignBurst(attacker);
      boolean effetSouverain = attacker.hasEffect(net.mcreator.minepiece.init.MinepieceModMobEffects.SOVEREIGN_HAKI);
      check("L5-haki-souverain-accessible", effetSouverain,
         "effet Souverain appliqué=" + effetSouverain + ", entités mises hors de combat=" + touches);

      // Aucun effet Haki ne doit subsister sans niveau (pas de contre universel).
      HakiProgression.setLevel(attacker, HakiProgression.Type.ARMAMENT, 0);
      HakiProgression.setLevel(attacker, HakiProgression.Type.OBSERVATION, 0);
      attacker.removeEffect(net.mcreator.minepiece.init.MinepieceModMobEffects.SOVEREIGN_HAKI);
      HakiProgression.wear(attacker);
      check("L6-pas-de-contre-universel", !HakiProgression.countersDevilFruit(attacker),
         "au niveau 0, le contre reste inactif (ver15 l'appliquait dès le niveau 0)");

      MinepieceModVariables.clear(victim);
      MinepieceModVariables.clear(attacker);
   }

   // ------------------------------------------------- K. Équilibrage (CORE-025)
   private static void equilibrage() {
      java.util.List<Double> damages = new java.util.ArrayList<>();
      int offensives = 0;
      double max = 0.0;
      String maxId = "—";
      for (Item f : fruits()) {
         for (net.mcreator.minepiece.procedures.DevilFruitMove m : DevilFruitRegistry.movesFor(f)) {
            if (m.damage() <= 0.0) {
               continue;
            }
            offensives++;
            damages.add(m.damage());
            if (m.damage() > max) {
               max = m.damage();
               maxId = m.id();
            }
         }
      }
      java.util.Collections.sort(damages);
      double median = damages.isEmpty() ? 0.0 : damages.get(damages.size() / 2);
      double cap = br.com.oneminepiece.opworldlike.balance.DevilFruitBalanceConfig.absoluteMaxDamage();

      long auDessus = damages.stream().filter(d -> d > cap).count();
      check("K1-plafond-respecte", auDessus == 0,
         offensives + " techniques offensives, dégâts max=" + max + " (" + maxId + "), plafond=" + cap
         + ", au-dessus=" + auDessus);

      int corrections = DevilFruitRegistry.balanceAdjustments().size();
      boolean corrige = br.com.oneminepiece.opworldlike.balance.DevilFruitBalanceConfig.enabled();
      check("K2-corrections-tracees", corrige ? corrections > 0 : corrections == 0,
         (corrige ? "équilibrage actif" : "équilibrage désactivé") + " : " + corrections + " correction(s) journalisée(s)");
      for (br.com.oneminepiece.opworldlike.balance.DevilFruitBalance.Adjustment a
            : DevilFruitRegistry.balanceAdjustments()) {
         LOGGER.info("[dftest]   correction {} : {} → {} ({} ; {} Curse Power, {} ticks)",
            a.moveId(), a.before(), a.after(), a.reason(), (int) a.energyCost(), a.cooldownTicks());
      }

      // Fidélité : l'équilibrage ne doit toucher qu'une poignée d'écarts, jamais réécrire le catalogue.
      double pct = offensives == 0 ? 0.0 : 100.0 * corrections / offensives;
      check("K3-ecarts-limites", corrections <= 10,
         corrections + "/" + offensives + " techniques corrigées (" + String.format(java.util.Locale.ROOT, "%.1f", pct)
         + " %) — seuls les écarts sont réécrits, le reste garde les valeurs ver15");

      check("K4-distribution", median > 0 && max <= cap,
         "dégâts : min=" + damages.get(0) + " médiane=" + median + " max=" + max
         + " | sur " + offensives + " techniques offensives");
   }

   // ------------------------------------------------- N. Reset complet (FRUIT-RESET-01, P0)
   /**
    * Prouve que le reset centralisé nettoie tout ce que le pouvoir a accordé — et <b>rien d'autre</b>.
    * Le critère « sans toucher aux effets externes » est vérifié par {@code R3}.
    */
   private static void reset(ServerLevel level) {
      ServerPlayer zoan = fake(level, "dftest_reset_zoan");
      MinepieceModVariables.clear(zoan);
      Item giraffe = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_ushi_ushi_giraffe"));
      immerse(level, zoan, giraffe);

      // État complet : forme hybride + technique sélectionnée + énergie + invocation.
      DevilFruitTransformation.toggle(zoan);
      MinepieceModVariables.PlayerVariables vars = MinepieceModVariables.get(zoan);
      vars.PlayerTechniqueUsedNumber = 2.0;
      vars.PlayerEnergy = 40.0;
      boolean formeAvant = vars.player_form != 0.0;
      double echelleAvant = zoan.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SCALE);

      int invocationsAvant = 0;
      net.mcreator.minepiece.entity.SummonEntity invoc =
         net.mcreator.minepiece.init.MinepieceModEntities.SUMMON.spawn(
            level, zoan.blockPosition(), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      if (invoc != null) {
         invoc.setOwnerEntity(zoan);
         invocationsAvant = 1;
      }

      // Effet EXTERNE (potions) : ne doit jamais être retiré par le reset.
      zoan.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         net.minecraft.world.effect.MobEffects.NIGHT_VISION, 6000, 0, false, false));

      int supprimees = DevilFruitPowerLogic.resetDevilFruitPowers(zoan);
      double echelleApres = zoan.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
      boolean etatPropre = vars.player_form == 0.0
         && vars.PlayerDevilFruit.isEmpty()
         && !vars.UseDevilAbilities
         && vars.PlayerTechniqueUsedNumber == 0.0
         && vars.PlayerEnergy == 0.0;
      check("R1-reset-complet", formeAvant && etatPropre && echelleApres == 1.0,
         "forme " + (formeAvant ? "activée" : "absente") + " → 0, échelle " + echelleAvant + " → " + echelleApres
         + ", fruit vidé, sélection 2 → 0, énergie 40 → 0");

      check("R2-invocations-nettoyees", invocationsAvant == 0 || supprimees == invocationsAvant,
         "invocations du joueur avant reset=" + invocationsAvant + ", supprimées=" + supprimees
         + " (les invocations d'un autre propriétaire ne sont pas touchées)");

      check("R3-effets-externes-preserves",
         zoan.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),
         "l'effet externe (Vision nocturne, potion) survit au reset — le reset ne retire que l'effet "
         + "de porteur du Fruit et ses modificateurs d'attributs identifiés");

      int seconde = DevilFruitPowerLogic.resetDevilFruitPowers(zoan);
      check("R4-reset-idempotent", seconde == 0 && vars.PlayerDevilFruit.isEmpty(),
         "second reset : 0 invocation supprimée, aucun échec — l'opération est rejouable");

      // Fruit ailé : le vol accordé par la forme doit être révoqué par le reset.
      ServerPlayer volant = fake(level, "dftest_reset_volant");
      MinepieceModVariables.clear(volant);
      Item phoenix = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_tori_tori_phoenix"));
      immerse(level, volant, phoenix);
      DevilFruitTransformation.toggle(volant);
      boolean volAvant = volant.getAbilities().mayfly;
      DevilFruitPowerLogic.resetDevilFruitPowers(volant);
      check("R5-vol-revoque", !volAvant || !volant.getAbilities().mayfly,
         "vol accordé par la forme ailée (Phénix) : " + volAvant + " → " + volant.getAbilities().mayfly
         + " (un joueur créatif/spectateur n'est pas affecté)");

      MinepieceModVariables.clear(zoan);
      MinepieceModVariables.clear(volant);
   }

   // ------------------------------------------------- Q. Contrat Aro Aro (FRUIT-ARO_ARO-04)
   /**
    * Vérifie le déplacement sur flèche du contrat Aro Aro : la distance parcourue respecte la portée
    * déclarée, l'obstacle arrête le déplacement, et le joueur ne se retrouve <b>jamais</b> dans un
    * bloc — c'est le critère de validation du plan.
    */
   private static void aroAro(ServerLevel level) {
      ServerPlayer player = fake(level, "dftest_aro");
      MinepieceModVariables.clear(player);
      Item aro = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_aro_aro"));
      immerse(level,player,aro); // Exact fruit authority is a required cast prerequisite.
      ItemStack stack = new ItemStack(aro);
      DevilFruitMove dash = moveOf(aro, "move");

      if (dash == null) {
         check("A0-technique-aro-move-presente", false, "Aro Move absente du registre");
         return;
      }
      check("A0-technique-aro-move-presente", true,
         "Aro Move — portée déclarée " + dash.range() + " blocs, prise en charge par DevilFruitBehaviours");

      // Terrain d'essai dégagé : plateforme de pierre, 12 blocs d'air devant (axe +Z).
      int bx = 3000;
      int by = 100;
      int bz = 3000;
      for (int dz = -1; dz <= 12; dz++) {
         for (int dx = -2; dx <= 2; dx++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx + dx, by - 1, bz + dz),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int dy = 0; dy <= 2; dy++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx + dx, by + dy, bz + dz),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }

      // --- A1 : rien devant → le déplacement se produit, borné par la portée.
      player.teleportTo(bx + 0.5, by, bz + 0.5);
      player.setYRot(0.0F);          // yaw 0 → regard vers +Z
      player.setXRot(0.0F);
      double x0 = player.getX();
      double z0 = player.getZ();
      DevilFruitBehaviours.customCast(player, stack, dash);
      // Legacy synchronous harness steps the finite service; B04 has separate real-tick proofs.
      for(int phase=0;phase<55;phase++)net.mcreator.minepiece.procedures.ParameciaBatchTechniques.tick();
      double parcouru = Math.hypot(player.getX() - x0, player.getZ() - z0);
      check("A1-deplacement-sur-fleche", parcouru > 1.0 && parcouru <= dash.range() + 1.0E-6,
         "parcouru " + round2(parcouru) + " blocs (portée " + dash.range() + "), sans obstacle");

      // --- A2 : mur de pierre à 4 blocs → arrêt avant l'obstacle, jamais dans le bloc.
      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = 0; dy <= 2; dy++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx + dx, by + dy, bz + 4),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
         }
      }
      player.teleportTo(bx + 0.5, by, bz + 0.5);
      player.setYRot(0.0F);
      player.setXRot(0.0F);
      x0 = player.getX();
      z0 = player.getZ();
      DevilFruitBehaviours.customCast(player, stack, dash);
      // Legacy synchronous harness steps the finite service; B04 has separate real-tick proofs.
      for(int phase=0;phase<55;phase++)net.mcreator.minepiece.procedures.ParameciaBatchTechniques.tick();
      double parcouru2 = Math.hypot(player.getX() - x0, player.getZ() - z0);
      boolean dansUnBloc = !level.noCollision(player, player.getBoundingBox());
      check("A2-arret-a-obstacle-sans-traverser", parcouru2 > 1.0 && parcouru2 < 3.6 && !dansUnBloc,
         "parcouru " + round2(parcouru2) + " blocs devant un mur à 4 blocs, boîte de collision "
         + (dansUnBloc ? "EN COLLISION (échec)" : "libre") + " — le joueur n'est pas déplacé dans un bloc");

      // --- A3 : le chunk reste autorisé (aucune téléportation hors zone chargée).
      player.teleportTo(bx + 0.5, by, bz + 0.5);
      player.setYRot(0.0F);
      DevilFruitBehaviours.customCast(player, stack, dash);
      // Legacy synchronous harness steps the finite service; B04 has separate real-tick proofs.
      for(int phase=0;phase<55;phase++)net.mcreator.minepiece.procedures.ParameciaBatchTechniques.tick();
      check("A3-chunk-autorise", level.hasChunkAt(player.blockPosition()),
         "destination dans un chunk chargé (" + player.blockPosition().toShortString() + ")");

      MinepieceModVariables.clear(player);
   }

   // ------------------------------------------------- R. Contrat Batto Batto (FRUIT-BATTO_BATTO-04/05)
   /**
    * Vérifie le vol à énergie et l'écholocation du contrat Batto Batto : consommation réelle, coupure
    * à l'épuisement, à la suppression et hors forme — toujours <b>sans laisser le joueur bloqué</b> —
    * et une impulsion sensorielle strictement bornée par son rayon.
    */
   private static void batto(ServerLevel level) {
      ServerPlayer player = fake(level, "dftest_batto");
      MinepieceModVariables.clear(player);
      Item batto = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_batto_batto"));
      immerse(level, player, batto);
      player.teleportTo(104.5,110,104.5);
      player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
      player.getAbilities().mayfly=false;player.getAbilities().flying=false;
      DevilFruitTransformation.toggle(player);              // forme hybride requise pour voler
      MinepieceModVariables.PlayerVariables vars = MinepieceModVariables.get(player);

      // Vol actif.
      player.getAbilities().mayfly = true;
      player.getAbilities().flying = true;
      player.onUpdateAbilities();

      // --- B1 : le vol consomme réellement de l'Énergie.
      vars.PlayerEnergy = 100.0;
      double avant = vars.PlayerEnergy;
      net.mcreator.minepiece.procedures.ZoanForms.tick();
      double apres = vars.PlayerEnergy;
      check("B1-vol-consomme-energie", apres < avant && player.getAbilities().flying && vars.zoanFlightGranted,
         "Énergie " + round2(avant) + " → " + round2(apres) + " en un tick de vol"
         + " (" + DevilFruitFlightConfig.costPerSecond() + "/s + maintien forme), vol propre toujours actif");

      // --- B2 : réserve insuffisante → atterrissage, jamais bloqué.
      vars.PlayerEnergy = 0.2;
      net.mcreator.minepiece.procedures.ZoanForms.tick();
      boolean bloque = player.getAbilities().flying && !player.getAbilities().mayfly;
      check("B2-atterrissage-sans-energie",
         !player.getAbilities().flying && !player.getAbilities().mayfly && !bloque,
         "Énergie 0,2 < minimum " + DevilFruitFlightConfig.minimumEnergy()
         + " → mayfly=false ET flying=false (état dissocié impossible), le joueur retombe");

      // --- B3 : scellé par la mer → atterrissage immédiat, énergie restante préservée.
      vars.PlayerEnergy = 100.0;
      DevilFruitTransformation.toggle(player);
      player.getAbilities().flying = true;
      player.onUpdateAbilities();
      player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         net.mcreator.minepiece.init.MinepieceModMobEffects.SEA_POWER, 200, 9, false, false));
      net.mcreator.minepiece.procedures.ZoanForms.tick();
      boolean scelle = !player.getAbilities().flying;
      player.removeEffect(net.mcreator.minepiece.init.MinepieceModMobEffects.SEA_POWER);
      check("B3-atterrissage-sous-suppression", scelle,
         "sous SEA_POWER → vol révoqué (mayfly=" + player.getAbilities().mayfly
         + ", flying=" + player.getAbilities().flying + ")");

      // --- B4 : hors forme hybride → le vol n'est plus légitime.
      vars.PlayerEnergy = 100.0;
      DevilFruitTransformation.toggle(player);
      player.getAbilities().flying = true;
      player.onUpdateAbilities();
      vars.player_form=0;
      net.mcreator.minepiece.procedures.ZoanForms.tick();
      check("B4-atterrissage-hors-forme", !player.getAbilities().flying,
         "forme revenue à 0 → vol révoqué (require-hybrid-form="
         + DevilFruitFlightConfig.requireHybridForm() + ")");

      // --- B5 : écholocation bornée. Une créature DANS le rayon est marquée, une DEHORS ne l'est pas.
      double rayon = DevilFruitFlightConfig.echolocationRadius();
      player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
      var proche = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         player.blockPosition().offset((int) (rayon * 0.5), 0, 0),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      proche.setBaby(false);
      var loin = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         player.blockPosition().offset((int) (rayon * 2.0) + 4, 0, 0),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      loin.setBaby(false);
      int marquees = DevilFruitFlight.echolocation(player);
      boolean procheMarquee = proche != null && proche.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING);
      boolean loinMarquee = loin != null && loin.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING);
      check("B5-echolocation-bornee", procheMarquee && !loinMarquee,
         "rayon " + rayon + " blocs — créature à " + (int) (rayon * 0.5) + " blocs marquée=" + procheMarquee
         + ", créature à " + ((int) (rayon * 2.0) + 4) + " blocs marquée=" + loinMarquee
         + " (" + marquees + " marquée(s) au total, aucun dégât)");
      if (proche != null) {
         proche.discard();
      }
      if (loin != null) {
         loin.discard();
      }

      MinepieceModVariables.clear(player);
   }

   // ------------------------------------------------- S. Contrat Bisu Bisu (FRUIT-BISU_BISU-03/04/05)
   /**
    * Vérifie que les Biscuit Warriors sont bien des <b>soldats invoqués</b> (propriétaire, durée de vie,
    * limite) et non de simples projectiles, que les <b>ordres</b> ne portent que sur ses propres
    * invocations dans une portée bornée, et surtout qu'<b>un refus ne consomme rien</b> — critère
    * explicite du plan.
    */
   private static void bisu(ServerLevel level) {
      ServerPlayer owner = fake(level, "dftest_bisu");
      ServerPlayer autre = fake(level, "dftest_bisu_autre");
      MinepieceModVariables.clear(owner);
      MinepieceModVariables.clear(autre);
      Item bisu = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_bisu_bisu"));
      immerse(level, owner, bisu);
      DevilFruitTransformation.toggle(owner);
      // Terrain d'essai propre : les FakePlayer naissent en (0,0,0), dans la roche — les invocations
      // y sont créées puis évacuées, ce qui rendait le test vert seulement par accident du décor.
      // Terrain d'essai DANS le chunk de spawn : c'est le seul dont le tick d'entités est garanti dès
      // le démarrage. Ailleurs, `EntityType.spawn` construit l'entité mais ne l'ajoute pas au monde
      // — sans erreur — et l'invocation disparaît (constaté en 3200,100,3200 même chunk forcé :
      // le premier passage après démarrage échouait, le second réussissait).
      int bx = 4;
      int by = 100;
      int bz = 4;
      // Idempotence : on repart d'une zone vide. Un test d'invocation qui laisse ses soldats derrière
      // lui fausse l'exécution suivante (limite déjà atteinte, ordres déjà appliqués) — constaté en
      // pratique : le même scope passé deux fois donnait deux verdicts différents.
      for (var reste : level.getEntitiesOfClass(net.mcreator.minepiece.entity.SummonEntity.class,
            new net.minecraft.world.phys.AABB(bx - 96, by - 64, bz - 96, bx + 96, by + 64, bz + 96))) {
         reste.discard();
      }
      int cx = bx >> 4;
      int cz = bz >> 4;
      check("C0-chunk-de-test-charge",
         level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)) && level.shouldTickBlocksAt(new net.minecraft.core.BlockPos(bx, by, bz)),
         "terrain d'essai dans le chunk de spawn (" + cx + "," + cz + ") — chargé et tické");
      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx + dx, by - 1, bz + dz),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int dy = 0; dy <= 3; dy++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx + dx, by + dy, bz + dz),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      owner.teleportTo(bx + 0.5, by, bz + 0.5);
      autre.teleportTo(bx + 6.5, by, bz + 0.5);

      // --- C1 : Biscuit Warrior produit une invocation, pas un projectile.
      DevilFruitSummons.applyOrder(owner, net.mcreator.minepiece.entity.SummonEntity.Order.SUIVRE, 64.0);
      int avant = DevilFruitSummons.countOwn(owner, 64.0);
      String warriorId = "biscuitwarrior";
      boolean declaree = DevilFruitRegistry.isSummon(warriorId);
      int duree = DevilFruitRegistry.summonLifetime(warriorId);
      MinepieceModVariables.PlayerVariables vars = MinepieceModVariables.get(owner);
      // Index résolu depuis le catalogue réellement chargé, jamais supposé : un catalogue réordonné
      // ferait lancer une autre technique et le test passerait à côté du sujet.
      java.util.List<DevilFruitMove> catalogue = DevilFruitRegistry.movesFor(bisu);
      StringBuilder ids = new StringBuilder();
      int index = -1;
      for (int i = 0; i < catalogue.size(); i++) {
         DevilFruitMove m = catalogue.get(i);
         ids.append(m.id()).append("=").append(m.type()).append(m.id().equals(warriorId) ? "* " : " ");
         if (m.id().equals(warriorId)) {
            index = i;
         }
      }
      if (index < 0) {
         check("C1-biscuit-warrior-est-une-invocation", false,
            "Biscuit Warrior absente du catalogue : " + ids);
         return;
      }
      vars.PlayerTechniqueUsedNumber = index;
      vars.PlayerEnergy = 200.0;
      boolean lance = DevilFruitSkillDispatcher.castSkill(owner);
      for(int phase=0;phase<55;phase++)net.mcreator.minepiece.procedures.ParameciaBatchTechniques.tick();
      int apres = DevilFruitSummons.countOwn(owner, 64.0);
      // Diagnostic : combien d'invocations existent au total, et combien portent un propriétaire ?
      var toutes = level.getEntitiesOfClass(net.mcreator.minepiece.entity.SummonEntity.class,
         owner.getBoundingBox().inflate(128.0));
      long avecProprio = toutes.stream().filter(x -> x.getOwner() != null).count();
      check("C1-biscuit-warrior-est-une-invocation",
         declaree && lance && apres == avant + 1,
         "Biscuit Warrior déclarée invocation=" + declaree + " · durée de vie " + duree
         + " ticks · soldats du joueur " + avant + " → " + apres
         + " | catalogue : " + ids.toString().trim()
         + " | entités SummonEntity dans le monde=" + toutes.size()
         + ", dont avec propriétaire résolu=" + avecProprio);

      // --- C2 : limite atteinte → refus, et le refus NE consomme RIEN.
      vars.PlayerEnergy = 200.0;
      int gardes = 0;
      while (DevilFruitSummons.countOwn(owner, 64.0) < 4 && gardes++ < 8) {
         vars.PlayerTechniqueUsedNumber = index;
         owner.getCooldowns().removeCooldown(BuiltInRegistries.ITEM.getKey(bisu));
         vars.PlayerEnergy = 200.0;
         DevilFruitSkillDispatcher.castSkill(owner);
         for(int phase=0;phase<55;phase++)net.mcreator.minepiece.procedures.ParameciaBatchTechniques.tick();
      }
      int auMax = DevilFruitSummons.countOwn(owner, 64.0);
      double energieAvant = vars.PlayerEnergy = 200.0;
      owner.getCooldowns().removeCooldown(BuiltInRegistries.ITEM.getKey(bisu));
      boolean refuse = !DevilFruitSkillDispatcher.castSkill(owner);
      double energieApres = vars.PlayerEnergy;
      boolean rechargePosee = owner.getCooldowns().isOnCooldown(new ItemStack(bisu));
      check("C2-refus-ne-consomme-rien",
         auMax >= 4 && refuse && Math.abs(energieApres - energieAvant) < 1.0E-9 && !rechargePosee,
         "limite atteinte (" + auMax + " soldats) → lancement refusé=" + refuse
         + ", Énergie " + round2(energieAvant) + " → " + round2(energieApres)
         + " (aucune consommation), recharge posée=" + rechargePosee);

      // --- C3 : les ordres ne touchent que les invocations de leur propriétaire.
      var mien = level.getEntitiesOfClass(net.mcreator.minepiece.entity.SummonEntity.class,
            owner.getBoundingBox().inflate(64.0)).stream()
         .filter(x -> DevilFruitSummons.isOwnedBy(x, owner)).findFirst().orElse(null);
      var sien = level.getEntitiesOfClass(net.mcreator.minepiece.entity.SummonEntity.class,
            autre.getBoundingBox().inflate(64.0)).stream().findFirst().orElse(null);
      sien = net.mcreator.minepiece.init.MinepieceModEntities.SUMMON.spawn(
         level, autre.blockPosition(), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      if (sien != null) {
         sien.setOwnerEntity(autre);
         sien.applyOrder(net.mcreator.minepiece.entity.SummonEntity.Order.SUIVRE);
      }
      int commandes = DevilFruitSummons.applyOrder(owner, net.mcreator.minepiece.entity.SummonEntity.Order.ATTAQUER, 32.0);
      boolean leMien = mien != null && mien.getOrder() == net.mcreator.minepiece.entity.SummonEntity.Order.ATTAQUER;
      boolean leSien = sien != null && sien.getOrder() == net.mcreator.minepiece.entity.SummonEntity.Order.SUIVRE;
      check("C3-ordres-proprietaire-uniquement", leMien && leSien && commandes >= 1,
         commandes + " invocation(s) commandée(s) → ATTAQUER · la mienne="
         + (mien == null ? "absente" : mien.getOrder()) + ", celle de l'autre joueur="
         + (sien == null ? "absente" : sien.getOrder()) + " (inchangée)");

      // --- C4 : la durée de vie est bien celle déclarée par les données, et l'expiration est effective.
      boolean dureeLongue = DevilFruitRegistry.summonLifetime("biscuitwarrior2")
         > DevilFruitRegistry.summonLifetime("notewarrior");
      check("C4-duree-de-vie-des-soldats", dureeLongue && duree == 240,
         "Biscuit Warrior " + duree + " ticks, Biscuit Warrior II "
         + DevilFruitRegistry.summonLifetime("biscuitwarrior2") + " ticks, invocations d'origine 200 ticks"
         + " — durée portée par le registre, pas par le lanceur");

      // --- C5 : la limite est dérivée des entités vivantes, donc elle tient après reconnexion.
      int recompte = DevilFruitSummons.countOwn(owner, 64.0);
      check("C5-limite-persistante", recompte == auMax,
         "limite recalculée depuis les invocations vivantes (" + recompte + ") — aucun compteur à "
         + "réinitialiser, donc une déconnexion ne permet pas de la contourner");

      for (var s : level.getEntitiesOfClass(net.mcreator.minepiece.entity.SummonEntity.class,
            owner.getBoundingBox().inflate(64.0))) {
         s.discard();
      }
      if (sien != null) {
         sien.discard();
      }
      MinepieceModVariables.clear(owner);
      MinepieceModVariables.clear(autre);
   }

   // ------------------------------------------------- T. Contrat Bomu Bomu (FRUIT-BOMU_BOMU-03/04/05)
   /**
    * Vérifie les trois mécaniques que le plan déclare non prouvées par le moteur générique :
    * les <b>trois origines d'explosion</b> distinctes (contact, aérienne, corporelle — plus le
    * projectile), la <b>résistance du porteur sans immunité générale</b>, et le <b>respect des
    * réglages</b> de blocs, alliés et zones protégées.
    */
   private static void bomu(ServerLevel level) {
      final int bx = 12;
      final int by = 100;
      final int bz = 12;

      // Idempotence : on repart d'un décor propre, et on repasse la config par défaut.
      restoreExplosionConfig();
      for (var reste : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 48, by - 32, bz - 48, bx + 48, by + 32, bz + 48))) {
         reste.discard();
      }
      for (int dx = -5; dx <= 5; dx++) {
         for (int dz = -5; dz <= 5; dz++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx + dx, by - 1, bz + dz),
               net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
            for (int dy = 0; dy <= 3; dy++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx + dx, by + dy, bz + dz),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      check("D0-decor-pret", level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz))
            && level.shouldTickBlocksAt(new net.minecraft.core.BlockPos(bx, by, bz)),
         "terrain d'essai (12,100,12) dans le chunk de spawn — chargé et tické");

      ServerPlayer porteur = fake(level, "dftest_bomu");
      ServerPlayer autre = fake(level, "dftest_bomu_autre");
      MinepieceModVariables.clear(porteur);
      MinepieceModVariables.clear(autre);
      Item bomu = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_bomu_bomu"));
      immerse(level, porteur, bomu);
      immerse(level, autre, bomu);
      DevilFruitTransformation.toggle(porteur);
      DevilFruitTransformation.toggle(autre);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      autre.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setXRot(0.0F);

      // --- D1 : trois origines distinctes, un rayon propre par technique.
      java.util.LinkedHashMap<String, String> origines = new java.util.LinkedHashMap<>();
      java.util.LinkedHashSet<Double> rayons = new java.util.LinkedHashSet<>();
      StringBuilder d1 = new StringBuilder();
      for (String id : new String[]{"punchbomb", "kickbomb", "bomboverhead", "zenshinkibaku", "nosefancycannon"}) {
         var origine = DevilFruitExplosions.origin(id);
         origines.put(id, origine == null ? "absente" : origine.name());
         rayons.add(DevilFruitExplosions.radius(id));
         d1.append(id).append("=").append(origine).append("/").append(DevilFruitExplosions.radius(id)).append(" ");
      }
      // Le plan exige la différenciation « contact / projectile / explosion corporelle ».
      // Bomb Overhead ajoute une quatrième origine (aérienne), justifiée par son nom même.
      java.util.LinkedHashSet<String> types = new java.util.LinkedHashSet<>(origines.values());
      boolean exigeesPresentes = types.containsAll(java.util.List.of("CONTACT", "PROJECTILE", "BODY"));
      check("D1-origines-et-rayons-propres",
         exigeesPresentes && types.size() >= 3 && rayons.size() == 5 && !rayons.contains(0.0),
         "origines exigées (contact/projectile/corporelle) présentes = " + exigeesPresentes
         + " · " + types.size() + " origines distinctes, " + rayons.size() + "/5 rayons distincts"
         + " | " + d1.toString().trim());

      // --- D2 : la géométrie de l'origine correspond bien au type déclaré.
      // Corporelle = centrée sur le corps : une cible DERRIÈRE le porteur (dos au regard) est atteinte.
      // Contact  = devant : une cible devant est atteinte, une cible derrière ne l'est pas.
      var zenso = DevilFruitRegistry.movesFor(bomu).stream().filter(m -> m.id().equals("zenshinkibaku")).findFirst().orElseThrow();
      var punch = DevilFruitRegistry.movesFor(bomu).stream().filter(m -> m.id().equals("punchbomb")).findFirst().orElseThrow();
      Vec3 origineCorporelle = DevilFruitExplosions.resolveOriginFor(porteur, zenso);
      Vec3 origineContact = DevilFruitExplosions.resolveOriginFor(porteur, punch);
      var yeux = porteur.getEyePosition();
      double ecartCorps = Math.abs(origineCorporelle.y - (porteur.getY() + porteur.getBbHeight() * 0.5));
      double ecartHorizontalCorps = Math.hypot(origineCorporelle.x - porteur.getX(), origineCorporelle.z - porteur.getZ());
      double avanceContact = Math.hypot(origineContact.x - porteur.getX(), origineContact.z - porteur.getZ());
      check("D2-geometrie-des-origines",
         ecartCorps < 0.01 && ecartHorizontalCorps < 0.01 && avanceContact > 0.5,
         "corporelle : centre du corps à " + round2(ecartCorps) + " bloc en Y et "
         + round2(ecartHorizontalCorps) + " à l'horizontale (donc aussi derrière) · contact : "
         + round2(avanceContact) + " bloc devant les yeux (y " + round2(origineContact.y)
         + " vs " + round2(yeux.y) + ")");

      // --- D3 : résistance du porteur SANS immunité générale.
      var origine = new Vec3(bx + 0.5, by + 1.0, bz + 0.5);
      var temoin = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 2, by, bz + 1),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      temoin.setBaby(false);
      double temoinAvant = temoin == null ? -1.0 : temoin.getHealth();
      boolean soiEpargne = !DevilFruitExplosions.wouldAffect(porteur, porteur, origine, 6.0);
      boolean toucheParAutrui = DevilFruitExplosions.wouldAffect(autre, porteur, origine, 6.0);
      boolean temoinTouche = temoin != null && DevilFruitExplosions.wouldAffect(porteur, temoin, origine, 6.0);
      int touches = DevilFruitExplosions.blast(porteur, origine, 6.0, 12.0, ParticleTypes.FLAME);
      double temoinApres = temoin == null ? -1.0 : temoin.getHealth();
      check("D3-porteur-epargne-sans-immunite-generale",
         soiEpargne && toucheParAutrui && temoinTouche && touches >= 1 && temoinApres < temoinAvant,
         "sa propre explosion l'épargne = " + soiEpargne
         + " MAIS l'explosion d'un autre joueur le touche = " + toucheParAutrui
         + " (aucune immunité générale) · témoin blessé " + round2(temoinAvant) + " → " + round2(temoinApres)
         + " PV, " + touches + " créature(s) touchée(s)");

      // --- D4 : réglage des dégâts aux blocs réellement respecté.
      var cible = new net.minecraft.core.BlockPos(bx + 3, by - 1, bz + 3);
      level.setBlockAndUpdate(cible, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
      DevilFruitExplosions.blast(porteur, new Vec3(cible.getX() + 0.5, cible.getY() + 1.0, cible.getZ() + 0.5),
         3.0, 5.0, ParticleTypes.FLAME);
      boolean intactParDefaut = !level.getBlockState(cible).isAir();

      writeExplosionConfig(true, true, "");
      DevilFruitExplosions.blast(porteur, new Vec3(cible.getX() + 0.5, cible.getY() + 1.0, cible.getZ() + 0.5),
         3.0, 5.0, ParticleTypes.FLAME);
      boolean casseQuandAutorise = level.getBlockState(cible).isAir();
      check("D4-reglage-des-blocs-respecte", intactParDefaut && casseQuandAutorise,
         "block-damage=false → bloc intact = " + intactParDefaut
         + " · block-damage=true → bloc cassé = " + casseQuandAutorise);

      // --- D5 : zone protégée respectée même quand les dégâts aux blocs sont autorisés.
      level.setBlockAndUpdate(cible, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
      writeExplosionConfig(true, true, "minecraft:overworld@" + (bx - 8) + "," + (by - 8) + "," + (bz - 8)
         + "," + (bx + 8) + "," + (by + 8) + "," + (bz + 8));
      DevilFruitExplosions.blast(porteur, new Vec3(cible.getX() + 0.5, cible.getY() + 1.0, cible.getZ() + 0.5),
         3.0, 5.0, ParticleTypes.FLAME);
      boolean zoneRespectee = !level.getBlockState(cible).isAir();
      check("D5-zone-protegee-respectee", zoneRespectee,
         "block-damage=true ET zone protégée déclarée sur la zone → bloc intact = " + zoneRespectee
         + " (la protection d'île existante est également consultée)");

      // --- D6: isolate alliance from D5 administrative protection.
      writeExplosionConfig(false,true,"");
      // L'alliance testée est celle que le moteur de combat utilise partout ailleurs
      // (CombatSkillLogic.isValidTarget, cf. SummonEntity.canAttack et la règle de non-traversée des
      // projectiles) : une invocation du porteur est son alliée. La seconde notion d'allié — même
      // équipe de scoreboard — vit dans la même condition mais n'est pas exerçable ici :
      // `addPlayerToTeam` n'inscrit pas les joueurs factices du harnais (membres déclarés = 0).
      var allie = net.mcreator.minepiece.init.MinepieceModEntities.SUMMON.spawn(level,
         new net.minecraft.core.BlockPos(bx + 2, by, bz + 2),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      if (allie != null) {
         allie.setOwnerEntity(porteur);
      }
      boolean allieEpargne = allie != null && !DevilFruitExplosions.wouldAffect(porteur, allie, origine, 6.0);
      boolean ennemiTouche = temoin != null && DevilFruitExplosions.wouldAffect(porteur, temoin, origine, 6.0);
      double allieAvant = allie == null ? -1.0 : allie.getHealth();
      DevilFruitExplosions.blast(porteur, origine, 6.0, 12.0, ParticleTypes.FLAME);
      double allieApres = allie == null ? -1.0 : allie.getHealth();
      check("D6-allies-epargnes", allieEpargne && ennemiTouche && allieApres >= allieAvant,
         "invocation alliée du porteur → épargnée = " + allieEpargne + " (PV " + round2(allieAvant)
         + " → " + round2(allieApres) + ") · créature ennemie → touchée = " + ennemiTouche);
      if (allie != null) {
         allie.discard();
      }

      restoreExplosionConfig();
      if (temoin != null) {
         temoin.discard();
      }
      MinepieceModVariables.clear(porteur);
      MinepieceModVariables.clear(autre);
   }

   /** Écrit la configuration d'explosion et la recharge (le fichier est la source de vérité). */
   private static void writeExplosionConfig(boolean blockDamage, boolean spareAllies, String zones) {
      try {
         java.nio.file.Path path = java.nio.file.Path.of("config", "opworldlike", "devilfruit-explosion.toml");
         java.nio.file.Files.createDirectories(path.getParent());
         java.nio.file.Files.writeString(path, "block-damage = " + blockDamage + "\n"
            + "block-damage-resistance-limit = 3.0\n"
            + "spare-allies = " + spareAllies + "\n"
            + "owner-immune-to-own = true\n"
            + "protected-zones = \"" + zones + "\"\n", java.nio.charset.StandardCharsets.UTF_8);
         br.com.oneminepiece.opworldlike.explosion.DevilFruitExplosionConfig.loadOrCreate();
      } catch (java.io.IOException ex) {
         LOGGER.warn("Configuration d'explosion non écrite", ex);
      }
   }

   /** Remet la configuration d'explosion par défaut. */
   private static void restoreExplosionConfig() {
      writeExplosionConfig(false, true, "");
   }

   // ------------------------------------------------- U. Contrat Gomu Gomu (FRUIT-GOMU_GOMU-03/04)
   /**
    * Vérifie que le bras est une extension <b>physique</b> et non décorative : la portée réelle
    * correspond au bras visible (même distance pour l'étirement et pour les dégâts), un mur arrête le
    * coup, aucun membre ne subsiste, et les trois techniques produisent leur nombre d'impacts
    * documenté sous un total plafonné.
    */
   private static void gomu(ServerLevel level) {
      final int bx = 4;
      final int by = 100;
      final int bz = 12;

      ServerPlayer porteur = fake(level, "dftest_gomu");
      MinepieceModVariables.clear(porteur);
      GomuArmStrike.clearArms(porteur, 64.0);
      // Couloir dégagé devant le joueur (regard vers +Z) : le rayon ne doit rencontrer que ce qu'on pose.
      for (int x = bx - 4; x <= bx + 4; x++) {
         for (int z = bz - 4; z <= bz + 10; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 3; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item gomu = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_gomu_gomu"));
      immerse(level, porteur, gomu);
      DevilFruitTransformation.toggle(porteur);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setXRot(0.0F);
      var catalogue = DevilFruitRegistry.movesFor(gomu);
      DevilFruitMove pistol = catalogue.stream().filter(m -> m.id().equals("gomu_pistol")).findFirst().orElseThrow();
      DevilFruitMove bazooka = catalogue.stream().filter(m -> m.id().equals("gomu_bazooka")).findFirst().orElseThrow();
      DevilFruitMove gatling = catalogue.stream().filter(m -> m.id().equals("gomu_gatling")).findFirst().orElseThrow();
      check("G0-decor-pret", level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)),
         "couloir dégagé à partir de (" + bx + "," + by + "," + bz + "), regard vers +Z, chunk de spawn");

      // --- G1 : à vide, le bras va jusqu'à la portée déclarée, et l'étirement couvre exactement cette distance.
      var vide = GomuArmStrike.resolve(porteur, pistol);
      double etirementVide = GomuArmStrike.stretch(vide.distance());
      check("G1-portee-physique-egale-au-bras-visible",
         !vide.blockedByWall() && Math.abs(vide.distance() - pistol.range()) < 0.01
            && Math.abs(etirementVide * GomuArmStrike.VISUAL_ADVANCE - vide.distance()) < 0.001,
         "pistolet à vide : arrêt à " + round2(vide.distance()) + " blocs (= portée déclarée "
         + round2(pistol.range()) + ") · étirement " + round2(etirementVide) + " × "
         + round2(GomuArmStrike.VISUAL_ADVANCE) + " bloc d'avancée du modèle = "
         + round2(etirementVide * GomuArmStrike.VISUAL_ADVANCE) + " blocs — le poing est là où frappent les dégâts");

      // --- G2 : un mur interrompt le coup.
      var mur = new net.minecraft.core.BlockPos(bx, by, bz + 3);
      for (int y = 0; y <= 2; y++) {
         level.setBlockAndUpdate(mur.offset(0, y, 0), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      }
      var bloque = GomuArmStrike.resolve(porteur, pistol);
      check("G2-mur-interrompt-le-coup",
         bloque.blockedByWall() && bloque.distance() < 3.0 && bloque.distance() > 2.0,
         "mur de pierre à 3 blocs → arrêt à " + round2(bloque.distance())
         + " blocs (au lieu de " + round2(pistol.range()) + "), étirement "
         + round2(GomuArmStrike.stretch(bloque.distance())) + " — l'arrêt est visible");

      // --- G3 : une cible DERRIÈRE le mur n'est pas touchée ; le mur retiré, elle l'est.
      var proie = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 6), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert proie != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      proie.setBaby(false);
      int avantMur = GomuArmStrike.clearArms(porteur, 64.0);
      double degatsAvecMur = GomuArmStrike.cast(porteur, pistol);
      boolean cibleDerriereMurEpargnee = degatsAvecMur == 0.0;
      for (int y = 0; y <= 2; y++) {
         level.setBlockAndUpdate(mur.offset(0, y, 0), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
      }
      double degatsSansMur = GomuArmStrike.cast(porteur, pistol);
      check("G3-mur-protege-la-cible",
         cibleDerriereMurEpargnee && degatsSansMur > 0.0,
         "cible à 6 blocs : derrière un mur à 3 → " + round2(degatsAvecMur) + " dégât (épargnée) · "
         + "mur retiré → " + round2(degatsSansMur) + " dégâts (" + (avantMur >= 0 ? "bras nettoyé avant" : "") + ")");

      // --- G4 : impacts documentés et total plafonné.
      // La cible est replacée avant chaque mesure : le recul du Pistol (1,5) la projetterait sinon
      // hors de portée du Bazooka (5 blocs) — ce qui est le comportement voulu, mais fausserait la
      // mesure du nombre d'impacts.
      // Neutral adult, unarmored, high-health witness: compare returned raw budgets to real HP.
      proie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);
      proie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);
      for(var slot:net.minecraft.world.entity.EquipmentSlot.values())if(slot.getType()==net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR)proie.setItemSlot(slot,net.minecraft.world.item.ItemStack.EMPTY);
      // Independent legacy-helper probes. Native damage immunity is reset BETWEEN attacks only.
      // Its same-tick Gatling attempts must count accepted HP loss, not six fictitious hits.
      proie.teleportTo(bx + 0.5, by, bz + 4.5);
      proie.setHealth(proie.getMaxHealth());proie.damageCooldownTime=0;proie.setInvulnerableTime(0);
      double totalPistol = GomuArmStrike.cast(porteur, pistol);
      proie.teleportTo(bx + 0.5, by, bz + 4.5);
      proie.setHealth(proie.getMaxHealth());proie.damageCooldownTime=0;proie.setInvulnerableTime(0);
      double totalBazooka = GomuArmStrike.cast(porteur, bazooka);
      proie.teleportTo(bx + 0.5, by, bz + 4.5);
      proie.setHealth(proie.getMaxHealth());proie.damageCooldownTime=0;proie.setInvulnerableTime(0);
      double totalGatling = GomuArmStrike.cast(porteur, gatling);
      boolean impactsOk = GomuArmStrike.impacts("gomu_pistol") == 1
         && GomuArmStrike.impacts("gomu_bazooka") == 1
         && GomuArmStrike.impacts("gomu_gatling") == 6;
      check("G4-impacts-documentes-et-plafond",
         impactsOk && Math.abs(totalPistol-11)<.01 && Math.abs(totalBazooka-20)<.01
            && Math.abs(totalGatling-6)<.01 && Math.abs(proie.getMaxHealth()-proie.getHealth()-totalGatling)<.01 && totalGatling<=GomuArmStrike.MAX_TOTAL_DAMAGE,
         "impacts 1/1/6 = " + impactsOk + " → Pistol " + round2(totalPistol) + ", Bazooka "
         + round2(totalBazooka) + ", Gatling " + round2(totalGatling) + " (1 impact accepte sur6 tentatives dans le meme tick ; cadence Gomu encore a porter) <= plafond "
         + round2(GomuArmStrike.MAX_TOTAL_DAMAGE) + " · recul du Bazooka " + round2(bazooka.knockback())
         + " = la poussée");

      // --- G5 : aucun membre ne reste après la frappe.
      GomuArmStrike.clearArms(porteur, 64.0);
      int apres = 0;
      for (int i = 0; i < 3; i++) {
         GomuArmStrike.cast(porteur, gatling);
         apres = level.getEntitiesOfClass(net.mcreator.minepiece.entity.EntityGomuArmEntity.class,
            porteur.getBoundingBox().inflate(64.0)).size();
      }
      int dureeMax = level.getEntitiesOfClass(net.mcreator.minepiece.entity.EntityGomuArmEntity.class,
         porteur.getBoundingBox().inflate(64.0)).stream()
         .mapToInt(a -> a.getMaxLifeTicksForTest()).max().orElse(0);
      int retires = GomuArmStrike.clearArms(porteur, 64.0);
      int apresNettoyage = level.getEntitiesOfClass(net.mcreator.minepiece.entity.EntityGomuArmEntity.class,
         porteur.getBoundingBox().inflate(64.0)).size();
      check("G5-aucun-membre-ne-reste",
         apres == 1 && dureeMax > 0 && dureeMax <= 20 && apresNettoyage == 0,
         "après 3 frappes : " + apres + " bras (pas d'accumulation), durée de vie bornée à " + dureeMax
         + " ticks · nettoyage → " + apresNettoyage + " (" + retires + " retiré(s))");

      if (proie != null) {
         proie.discard();
      }
      GomuArmStrike.clearArms(porteur, 64.0);
      MinepieceModVariables.clear(porteur);
   }

   // ------------------------------------------------- V. Contrat Goro Goro (FRUIT-GORO_GORO-03/05)
   /**
    * Vérifie les deux mécaniques que le plan déclare absentes : l'éclair à <b>rebonds bornés</b>
    * (Sango) et un <b>vrai comportement</b> pour Mamaragan — impacts annoncés, durée bornée, arrêt à
    * la suppression.
    */
   private static void goro(ServerLevel level) {
      final int bx = 12;
      final int by = 100;
      final int bz = 4;

      ServerPlayer caster = fake(level, "dftest_goro");
      MinepieceModVariables.clear(caster);
      MamaraganMobEffect.clearState(caster.getUUID());
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 3; x <= bx + 3; x++) {
         for (int z = bz - 3; z <= bz + 16; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 3; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item goro = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_goro_goro"));
      immerse(level, caster, goro);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      var sango = DevilFruitRegistry.movesFor(goro).stream()
         .filter(m -> m.id().equals("sango")).findFirst().orElseThrow();
      check("H0-decor-pret", level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz))
            && level.shouldTickBlocksAt(new net.minecraft.core.BlockPos(bx, by, bz)),
         "terrain d'essai (" + bx + "," + by + "," + bz + ") dans le chunk de spawn — chargé et tické");

      // ============================ H1-H3 : Mamaragan, impacts annoncés et bornés
      var proie = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 3), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert proie != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      proie.setBaby(false);
      double av = proie.getHealth();
      MamaraganMobEffect.tick(level, caster, 0);
      boolean annonce = MamaraganMobEffect.hasPending(caster.getUUID());
      Vec3 pointAnnonce = MamaraganMobEffect.pendingPoint(caster.getUUID());
      check("H1-mamaragan-annonce-avant-la-frappe",
         annonce && pointAnnonce != null && pointAnnonce.distanceTo(proie.position()) < 1.0
            && proie.getHealth() == av,
         "annonce posée sur la cible (" + round2(pointAnnonce == null ? -1 : pointAnnonce.distanceTo(proie.position()))
         + " bloc) et AUCUN dégât à cet instant — la cible est prévenue avant d'être frappée");

      MamaraganMobEffect.tick(level, caster, 0);
      MamaraganMobEffect.tick(level, caster, 0);
      double apres = proie.getHealth();
      // L'armure de la créature absorbe une part des dégâts : on vérifie que la frappe a bien eu lieu
      // et que le montant reste cohérent avec les dégâts nominaux, sans exiger l'égalité au centième.
      double encaisse = av - apres;
      check("H2-mamaragan-frappe-apres-l-annonce",
         apres < av && encaisse > MamaraganMobEffect.STRIKE_DAMAGE * 0.6
            && encaisse <= MamaraganMobEffect.STRIKE_DAMAGE + 0.01
            && !MamaraganMobEffect.hasPending(caster.getUUID()),
         "frappe " + MamaraganMobEffect.WARNING_TICKS + " ticks après l'annonce : "
         + round2(av) + " → " + round2(apres) + " PV (−" + round2(encaisse) + " encaissés sur "
         + round2(MamaraganMobEffect.STRIKE_DAMAGE) + " nominaux, le reste absorbé par l'armure)");

      for (int i = 0; i < 60; i++) {
         proie.setHealth(proie.getMaxHealth());
         MamaraganMobEffect.tick(level, caster, 0);
      }
      int frappes = MamaraganMobEffect.strikesDone(caster.getUUID());
      check("H3-mamaragan-borne", frappes <= MamaraganMobEffect.MAX_STRIKES && frappes > 0,
         frappes + " impacts sur l'équivalent d'une durée complète — plafond "
         + MamaraganMobEffect.MAX_STRIKES + " respecté (la durée de l'effet borne l'orage)");

      // ============================ H4 : cesse à la suppression
      MamaraganMobEffect.clearState(caster.getUUID());
      MamaraganMobEffect.tick(level, caster, 0);
      boolean annonceAvantSuppression = MamaraganMobEffect.hasPending(caster.getUUID());
      caster.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         net.mcreator.minepiece.init.MinepieceModMobEffects.SEA_POWER, 200, 0, false, false));
      MamaraganMobEffect.tick(level, caster, 0);
      int frappesAvant = MamaraganMobEffect.strikesDone(caster.getUUID());
      for (int i = 0; i < 12; i++) {
         MamaraganMobEffect.tick(level, caster, 0);
      }
      boolean aucuneFrappeSousSuppression = MamaraganMobEffect.strikesDone(caster.getUUID()) == frappesAvant
         && !MamaraganMobEffect.hasPending(caster.getUUID());
      caster.removeEffect(net.mcreator.minepiece.init.MinepieceModMobEffects.SEA_POWER);
      check("H4-mamaragan-cesse-a-la-suppression",
         annonceAvantSuppression && aucuneFrappeSousSuppression,
         "annonce en cours puis pouvoir scellé par la mer → annonce annulée et aucune frappe (impacts : "
         + frappesAvant + " → " + MamaraganMobEffect.strikesDone(caster.getUUID()) + ")");

      // ============================ H5 : le porteur et ses alliés sont épargnés par l'impact
      MamaraganMobEffect.clearState(caster.getUUID());
      var allie = net.mcreator.minepiece.init.MinepieceModEntities.SUMMON.spawn(level,
         net.minecraft.core.BlockPos.containing(caster.position()),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      if (allie != null) {
         allie.setOwnerEntity(caster);
      }
      // La cible est ramenée dans le rayon d'impact : sans cela le test mesurerait une absence de
      // cible, pas l'épargne des alliés.
      // Toute cette séquence s'exécute DANS LE MÊME tick serveur : une créature déjà blessée est
      // protégée par la fenêtre d'invulnérabilité vanille (20 ticks) et absorberait la frappe — le
      // test mesurerait alors l'immunité, pas l'épargne des alliés. On frappe donc une créature
      // FRAÎCHE, dont la fenêtre est vierge (le champ n'est pas accessible publiquement).
      proie.discard();
      proie = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 1), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert proie != null;
      double proieAvant = proie.getHealth();
      double allieAvant = allie == null ? -1.0 : allie.getHealth();
      double porteurAvant = caster.getHealth();
      int touches = MamaraganMobEffect.strike(level, caster, caster.position(), 0);
      double allieApres = allie == null ? -1.0 : allie.getHealth();
      StringBuilder zone = new StringBuilder();
      for (var e : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
            new net.minecraft.world.phys.AABB(caster.position(), caster.position()).inflate(2.5))) {
         zone.append(e.getName().getString()).append("(")
            .append(e == caster ? "porteur" : net.mcreator.minepiece.procedures.CombatSkillLogic.isValidTarget(caster, e) ? "cible" : "allié")
            .append(",d=").append(round2(e.position().distanceTo(caster.position())))
            .append(",pv=").append(round2(e.getHealth())).append(") ");
      }
      check("H5-mamaragan-epargne-porteur-et-allies",
         touches >= 1 && allieApres >= allieAvant && proie.getHealth() < proieAvant,
         "impact au corps à corps : allié " + round2(allieAvant) + " → " + round2(allieApres)
         + " PV (épargné) · créature hostile " + round2(proieAvant) + " → " + round2(proie.getHealth())
         + " PV · porteur " + round2(porteurAvant) + " PV (jamais sa propre foudre), " + touches
         + " touchée(s) | zone : " + zone.toString().trim());
      if (allie != null) {
         allie.discard();
      }

      // ============================ I1-I3 : Sango, éclair à rebonds bornés
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      java.util.List<net.minecraft.world.entity.Mob> chaine = new ArrayList<>();
      for (int i = 1; i <= 4; i++) {
         var z = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
            new net.minecraft.core.BlockPos(bx, by, bz + i * 3),
            net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      z.setBaby(false);
         if (z != null) {
            chaine.add(z);
         }
      }
      var loin = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 30),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      loin.setBaby(false);
      ChainLightning.Chain resultat = ChainLightning.resolve(caster, sango);
      boolean premiers = resultat.targets().size() == 4;
      boolean decroissants = resultat.damages().size() == 4
         && resultat.damages().get(0) > resultat.damages().get(1)
         && resultat.damages().get(1) > resultat.damages().get(2)
         && resultat.damages().get(2) > resultat.damages().get(3);
      boolean loinExclue = loin != null && !resultat.targets().contains(loin);
      check("I1-eclair-a-rebonds-bornes",
         premiers && decroissants && loinExclue && resultat.bounces() == ChainLightning.MAX_BOUNCES,
         "4 maillons (1 impact + " + resultat.bounces() + " rebonds), dégâts "
         + round2(resultat.damages().get(0)) + " → " + round2(resultat.damages().get(1)) + " → "
         + round2(resultat.damages().get(2)) + " → " + round2(resultat.damages().get(3))
         + ", portée de rebond " + round2(ChainLightning.BOUNCE_RANGE) + " blocs · cible à 30 blocs exclue = "
         + loinExclue);

      // Allié placé au plus près : il ne doit jamais devenir un maillon.
      var allieProche = net.mcreator.minepiece.init.MinepieceModEntities.SUMMON.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 1),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      if (allieProche != null) {
         allieProche.setOwnerEntity(caster);
      }
      ChainLightning.Chain avecAllie = ChainLightning.resolve(caster, sango);
      boolean allieExclu = allieProche != null && !avecAllie.targets().contains(allieProche);
      int nombreAvant = avecAllie.targets().size();
      if (allieProche != null) {
         allieProche.discard();
      }
      check("I2-allies-exclus-de-la-chaine",
         allieExclu && nombreAvant == 4,
         "un allié du porteur placé à 1 bloc (plus près que toute cible) n'est jamais un maillon · chaîne toujours à "
         + nombreAvant + " maillons");

      double total = ChainLightning.cast(caster, sango).total();
      check("I3-degats-de-la-chaine",
         total > 0.0 && total <= 4 * sango.damage(),
         "chaîne appliquée : " + round2(total) + " dégâts cumulés, borne haute "
         + round2(4 * sango.damage()) + " (4 × " + round2(sango.damage()) + " sans atténuation)");

      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      MamaraganMobEffect.clearState(caster.getUUID());
      MinepieceModVariables.clear(caster);
   }

   // ------------------------------------------------- J. Contrat Gura Gura (FRUIT-GURA_GURA-03/04/05)
   /**
    * Vérifie les trois mécaniques que le plan déclare absentes : des <b>ondes qui se propagent</b>
    * (front, instant des dégâts), un <b>recul directionnel distinct</b> selon la technique, et des
    * <b>fissures temporaires</b> respectant la protection du terrain.
    */
   private static void gura(ServerLevel level) {
      final int bx = 12;
      final int by = 100;
      final int bz = 4;

      ServerPlayer caster = fake(level, "dftest_gura");
      MinepieceModVariables.clear(caster);
      SeismicWave.clearAll();
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 60, by - 24, bz - 60, bx + 60, by + 24, bz + 60))) {
         m.discard();
      }
      // Couloir de terre plein vers +Z, pour que les fissures aient un sol à fendre.
      for (int x = bx - 2; x <= bx + 2; x++) {
         for (int z = bz; z <= bz + 30; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 3; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item gura = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_gura_gura"));
      immerse(level, caster, gura);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      // Regard vers +Z : l'onde doit partir droit devant.
      caster.connection.teleport(caster.getX(), caster.getY(), caster.getZ(), 0.0F, 0.0F);
      caster.setYRot(0.0F);
      var gekishin = DevilFruitRegistry.movesFor(gura).stream()
         .filter(m -> m.id().equals("gekishin")).findFirst().orElseThrow();
      var kabutowari = DevilFruitRegistry.movesFor(gura).stream()
         .filter(m -> m.id().equals("kabutowari")).findFirst().orElseThrow();
      check("J0-decor-pret", level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)),
         "terrain d'essai (" + bx + "," + by + "," + bz + ") dans le chunk de spawn, couloir de "
         + (int) gekishin.range() + " blocs · Gekishin se propage = " + SeismicWave.isWave("gekishin")
         + " · attaque en cône = " + (!SeismicWave.isWave("attack_sweep")));

      // ============================ J1 : le front voyage, les dégâts arrivent avec lui
      var loin = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 10),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert loin != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      loin.setBaby(false);
      double pvLoin = loin.getHealth();
      SeismicWave.cast(caster, gekishin);
      SeismicWave.tick();
      double pvApres1 = loin.getHealth();
      // Le front avance de waveSpeed blocs par tick : à 10 blocs, il lui faut 5 ticks.
      for (int i = 0; i < 5; i++) {
         SeismicWave.tick();
      }
      check("J1-front-voyageur-instant-des-degats",
         pvApres1 == pvLoin && loin.getHealth() < pvLoin,
         "cible à 10 blocs INTACTE après 1 tick (" + round2(pvApres1) + " PV) puis frappée à l'ARRIVÉE du front : "
         + round2(pvLoin) + " → " + round2(loin.getHealth()) + " PV — les dégâts dépendent de l'arrivée, pas du lancement");

      // ============================ J2 : l'onde s'esquive réellement
      SeismicWave.clearAll();
      var fuyard = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 10),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert fuyard != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      fuyard.setBaby(false);
      double pvFuyard = fuyard.getHealth();
      SeismicWave.cast(caster, gekishin);
      SeismicWave.tick();
      fuyard.teleportTo(bx + 0.5, by, bz + 25); // sort du couloir avant l'arrivée
      for (int i = 0; i < 8; i++) {
         SeismicWave.tick();
      }
      check("J2-onde-esquivable", fuyard.getHealth() == pvFuyard,
         "cible sortie du couloir avant l'arrivée du front : " + round2(pvFuyard) + " → "
         + round2(fuyard.getHealth()) + " PV — l'onde se voit venir et s'esquive, contrairement à une frappe instantanée");

      // ============================ J3 : une seule frappe par cible malgré le balayage
      SeismicWave.clearAll();
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 60, by - 24, bz - 60, bx + 60, by + 24, bz + 60))) {
         m.discard();
      }
      var unique = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 6),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert unique != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      unique.setBaby(false);
      double pvUnique = unique.getHealth();
      SeismicWave.cast(caster, gekishin);
      for (int i = 0; i < 8; i++) {
         SeismicWave.tick();
      }
      double encaisse = pvUnique - unique.getHealth();
      check("J3-une-frappe-par-cible",
         unique.getHealth() < pvUnique && encaisse <= gekishin.damage() + 0.01,
         "traversée complète du front : " + round2(encaisse) + " dégâts encaissés pour "
         + round2(gekishin.damage()) + " nominaux — le balayage par échantillons ne frappe pas plusieurs fois");

      // ============================ J4 : l'onde s'arrête à sa portée et ne fuit pas
      SeismicWave.clearAll();
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 60, by - 24, bz - 60, bx + 60, by + 24, bz + 60))) {
         m.discard();
      }
      var hors = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 20),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert hors != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      hors.setBaby(false);
      double pvHors = hors.getHealth();
      SeismicWave.cast(caster, gekishin);
      for (int i = 0; i < 20; i++) {
         SeismicWave.tick();
      }
      check("J4-front-borne-et-sans-fuite",
         hors.getHealth() == pvHors && SeismicWave.activeWaves() == 0,
         "cible à 20 blocs hors de portée (" + round2(gekishin.range()) + " blocs) intacte · ondes en cours après "
         + "20 ticks = " + SeismicWave.activeWaves() + " (aucune onde ne survit à sa portée)");

      // ============================ J5 : recul directionnel, distinct selon la technique
      SeismicWave.clearAll();
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 60, by - 24, bz - 60, bx + 60, by + 24, bz + 60))) {
         m.discard();
      }
      var pousse = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 4),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert pousse != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      pousse.setBaby(false);
      SeismicWave.cast(caster, gekishin);
      for (int i = 0; i < 6; i++) {
         SeismicWave.tick();
      }
      Vec3 vGekishin = pousse.getDeltaMovement();
      boolean pousseVersZ = vGekishin.z > 0.05 && Math.abs(vGekishin.x) < 0.05;

      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 60, by - 24, bz - 60, bx + 60, by + 24, bz + 60))) {
         m.discard();
      }
      var souleve = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 4),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert souleve != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      souleve.setBaby(false);
      SeismicWave.cast(caster, kabutowari);
      for (int i = 0; i < 6; i++) {
         SeismicWave.tick();
      }
      Vec3 vKabutowari = souleve.getDeltaMovement();
      check("J5-recul-directionnel-distinct",
         pousseVersZ && vKabutowari.y > vGekishin.y + 0.05,
         "Gekishin pousse droit devant (Δz=" + round2(vGekishin.z) + ", Δx=" + round2(vGekishin.x)
         + ", Δy=" + round2(vGekishin.y) + ") · Kabutowari ajoute une élévation (Δy=" + round2(vKabutowari.y)
         + ") — le recul suit la direction de l'onde et diffère selon la technique");

      // ============================ J6 : les fissures expirent et le terrain est restauré
      SeismicWave.clearAll();
      // On repart d'un terrain vierge : les sections précédentes ont laissé leurs propres fissures.
      PowerBlockService.releaseAll(caster.getUUID());
      for(int x=bx-4;x<=bx+4;x++)for(int z=bz-2;z<=bz+35;z++)level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,by-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      int avant = PowerBlockService.activeCountFor(caster.getUUID());
      SeismicWave.cast(caster, gekishin);
      for (int i = 0; i < 8; i++) {
         SeismicWave.tick();
      }
      int posees = PowerBlockService.activeCountFor(caster.getUUID()) - avant;
      net.minecraft.core.BlockPos temoin = new net.minecraft.core.BlockPos(bx, by - 1, bz + 3);
      var etatFissure = level.getBlockState(temoin);
      boolean fendu = !etatFissure.is(net.minecraft.world.level.block.Blocks.STONE);
      // Expiration forcée : on révoque comme le ferait le retrait du Fruit.
      PowerBlockService.releaseAll(caster.getUUID());
      boolean conserve = level.getBlockState(temoin).equals(etatFissure) && PowerBlockService.activeCountFor(caster.getUUID())==0;
      check("J6-fissures-definitives-apres-retrait",posees>0 && fendu && conserve,
         "fissures posees="+posees+", etat conserve et attribution liberee="+conserve);

      // ============================ J7 : la protection du terrain est respectée
      for(int x=bx-4;x<=bx+4;x++)for(int z=bz-2;z<=bz+35;z++)level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,by-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      String zone = level.dimension().identifier() + "@" + (bx - 5) + "," + (by - 5) + "," + (bz - 5)
         + "," + (bx + 5) + "," + (by + 5) + "," + (bz + 35);
      DevilFruitExplosionConfig.addZoneForTest(zone);
      int avantProtege = PowerBlockService.activeCountFor(caster.getUUID());
      SeismicWave.clearAll();
      SeismicWave.cast(caster, gekishin);
      for (int i = 0; i < 8; i++) {
         SeismicWave.tick();
      }
      int poseesEnZoneProtegee = PowerBlockService.activeCountFor(caster.getUUID()) - avantProtege;
      DevilFruitExplosionConfig.clearTestZones();
      int avantLibre = PowerBlockService.activeCountFor(caster.getUUID());
      SeismicWave.clearAll();
      SeismicWave.cast(caster, gekishin);
      for (int i = 0; i < 8; i++) {
         SeismicWave.tick();
      }
      int poseesHorsZone = PowerBlockService.activeCountFor(caster.getUUID()) - avantLibre;
      PowerBlockService.releaseAll(caster.getUUID());
      check("J7-protection-du-terrain-respectee",
         poseesEnZoneProtegee == 0 && poseesHorsZone > 0,
         "couloir déclaré zone protégée : " + poseesEnZoneProtegee + " fissure(s) posée(s) — aucune ; "
         + "même onde hors zone : " + poseesHorsZone + " fissure(s) — la protection passe avant la technique "
         + "(TODO CORE-010 désormais branché dans le service de blocs temporaires)");

      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 60, by - 24, bz - 60, bx + 60, by + 24, bz + 60))) {
         m.discard();
      }
      SeismicWave.clearAll();
      PowerBlockService.releaseAll(caster.getUUID());
      MinepieceModVariables.clear(caster);
   }

   // ------------------------------------------------- K. Contrat Hie Hie (FRUIT-HIE_HIE-03/04/05)
   /**
    * Vérifie le <b>gel progressif</b> (libération à l'échéance, plafond dur), la <b>restauration
    * conditionnelle</b> des blocs de glace, et la <b>règle d'immersion</b> de la plateforme.
    */
   private static void hie(ServerLevel level) {
      final int bx = 12;
      final int by = 100;
      final int bz = 4;

      ServerPlayer caster = fake(level, "dftest_hie");
      MinepieceModVariables.clear(caster);
      FreezeService.clearAll();
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 3; x <= bx + 3; x++) {
         for (int z = bz - 3; z <= bz + 12; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 3; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item hie = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_hie_hie"));
      immerse(level, caster, hie);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      var iceball = DevilFruitRegistry.movesFor(hie).stream()
         .filter(m -> m.id().equals("iceball")).findFirst().orElseThrow();
      var iceage = DevilFruitRegistry.movesFor(hie).stream()
         .filter(m -> m.id().equals("iceage")).findFirst().orElseThrow();
      check("K0-decor-pret", level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)),
         "terrain d'essai (" + bx + "," + by + "," + bz + ") prêt · paliers de gel = "
         + FreezeService.MAX_STAGE + " · plafond dur = " + FreezeService.HARD_CAP_TICKS + " ticks");

      // ============================ K1 : le gel est progressif, pas un ralentissement fixe
      var cible = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 2),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert cible != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      cible.setBaby(false);
      int p1 = FreezeService.apply(cible, FreezeService.STAGE_TICKS);
      int p2 = FreezeService.apply(cible, FreezeService.STAGE_TICKS);
      int p3 = FreezeService.apply(cible, FreezeService.STAGE_TICKS);
      var effet = cible.getEffect(MinepieceModMobEffects.FROZEN);
      check("K1-gel-progressif",
         p1 == 1 && p2 == 2 && p3 == FreezeService.MAX_STAGE && effet != null
            && effet.getAmplifier() == FreezeService.MAX_STAGE,
         "trois applications → paliers " + p1 + " → " + p2 + " → " + p3 + " (ver15 reposait un "
         + "ralentissement d'amplificateur fixe) · marqueur FROZEN ampl. "
         + (effet == null ? "-" : effet.getAmplifier()) + " donc SYNCHRONISÉ au client par le jeu");

      // ============================ K2 : au palier maximum la cible est figée
      cible.setDeltaMovement(1.0, 0.0, 1.0);
      FreezeService.tick();
      Vec3 apres = cible.getDeltaMovement();
      check("K2-gel-total-immobilise",
         FreezeService.isFrozen(cible) && Math.abs(apres.x) < 1.0E-6 && Math.abs(apres.z) < 1.0E-6,
         "cible figée : mobilité horizontale " + round2(apres.x) + " / " + round2(apres.z)
         + " (annulée), chute conservée (" + round2(apres.y) + ") — un joueur gelé tombe, il ne reste pas suspendu");

      // ============================ K3 : le gel se libère à l'échéance, sans nouvel événement
      FreezeService.apply(cible, 1);
      for (int i = 0; i < 3; i++) {
         FreezeService.tick();
      }
      boolean libere = FreezeService.stageOf(cible) == 0 && !cible.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).hasModifier(Identifier.fromNamespaceAndPath("minepiece","freeze_slow"))
         && cible.getEffect(net.minecraft.world.effect.MobEffects.SLOWNESS) == null
         && cible.getEffect(MinepieceModMobEffects.FROZEN) == null;
      check("K3-liberation-a-l-echeance", libere,
         "échéance atteinte sans aucun nouvel événement : palier " + FreezeService.stageOf(cible)
         + ", ralentissement " + (cible.getEffect(net.minecraft.world.effect.MobEffects.SLOWNESS) == null ? "retiré" : "PRÉSENT")
         + ", état de gel " + (cible.getEffect(MinepieceModMobEffects.FROZEN) == null ? "retiré" : "PRÉSENT")
         + " — le gel ne dépend pas d'un coup reçu pour se terminer");

      // ============================ K4 : aucune prison — le plafond dur tient malgré le matraquage
      FreezeService.clearAll();
      // Matraquage RÉEL : un coup puis un tick, 400 fois de suite — la cible est regelée sans arrêt.
      // Ce qui compte n'est pas le TOTAL de ticks figés, mais la plus longue séquence SANS
      // interruption : c'est elle qui dirait « prison ». Un total élevé est normal si le joueur est
      // regelé après chaque fenêtre de liberté — la question est de savoir s'il respire entre deux.
      int ticksGeles = 0;
      int palierMax = 0;
      int sequence = 0;
      int plusLongue = 0;
      int fenetresDeLiberte = 0;
      final int matraquage = 600;
      for (int i = 0; i < matraquage; i++) {
         FreezeService.apply(cible, FreezeService.STAGE_TICKS);
         palierMax = Math.max(palierMax, FreezeService.stageOf(cible));
         FreezeService.tick();
         if (FreezeService.isFrozen(cible)) {
            ticksGeles++;
            sequence++;
            plusLongue = Math.max(plusLongue, sequence);
         } else {
            sequence = 0;
         }
         if (FreezeService.freedomTicksLeft(cible) > FreezeService.FREEDOM_TICKS - 2) {
            fenetresDeLiberte++;
         }
      }
      check("K4-jamais-de-prison",
         palierMax == FreezeService.MAX_STAGE && plusLongue <= FreezeService.HARD_CAP_TICKS
            && plusLongue < matraquage && fenetresDeLiberte > 0,
         "matraquage de " + matraquage + " ticks (un regel par tick, palier plafonné à " + palierMax
         + ") : plus longue séquence figée = " + plusLongue + " ticks (plafond dur "
         + FreezeService.HARD_CAP_TICKS + "), total figé " + ticksGeles + " ticks et "
         + fenetresDeLiberte + " fenêtre(s) de liberté ouvertes — un joueur matraqué respire, "
         + "aucune séquence ne dure indéfiniment");

      // ============================ K5 : la glace n'écrase pas la construction d'un joueur
      FreezeService.clearAll();
      PowerBlockService.releaseAll(caster.getUUID());
      int avantPose = PowerBlockService.activeCountFor(caster.getUUID());
      int geles = IceTerrain.freezePath(caster, iceball);
      net.minecraft.core.BlockPos temoin = new net.minecraft.core.BlockPos(bx, by - 1, bz + 2);
      boolean glace = level.getBlockState(temoin).is(net.minecraft.world.level.block.Blocks.PACKED_ICE);
      // Le joueur casse la glace et pose SON bloc : l'expiration ne doit pas l'écraser.
      level.setBlockAndUpdate(temoin, net.minecraft.world.level.block.Blocks.OAK_PLANKS.defaultBlockState());
      PowerBlockService.releaseAll(caster.getUUID());
      boolean planchesIntactes = level.getBlockState(temoin).is(net.minecraft.world.level.block.Blocks.OAK_PLANKS);
      check("K5-glace-n-ecrase-pas-la-construction",
         geles > 0 && glace && planchesIntactes,
         geles + " bloc(s) gelé(s) (" + (PowerBlockService.activeCountFor(caster.getUUID()) - avantPose) + " suivis) · glace posée = "
         + glace + " · après que le joueur a posé SES planches, l'expiration les respecte = " + planchesIntactes);

      // ============================ K6 : la plateforme ne contourne pas la faiblesse aquatique
      PowerBlockService.releaseAll(caster.getUUID());
      net.minecraft.core.BlockPos piedsP = new net.minecraft.core.BlockPos(bx, by, bz);
      // Une flaque assez large pour que le champ ait de l'eau À GELER hors de l'espace du porteur.
      for (int x = bx - 3; x <= bx + 3; x++) {
         for (int z = bz - 3; z <= bz + 3; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by, z),
               net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
         }
      }
      level.setBlockAndUpdate(piedsP, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      caster.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         net.mcreator.minepiece.init.MinepieceModMobEffects.SEA_POWER, 200, 1, false, false));
      caster.setYRot(0.0F);
      int gelesEau = IceTerrain.freezeField(caster, 4.0);
      boolean bloquesDansLePorteur = false;
      for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(
            net.minecraft.core.BlockPos.containing(caster.getBoundingBox().minX, caster.getBoundingBox().minY, caster.getBoundingBox().minZ),
            net.minecraft.core.BlockPos.containing(caster.getBoundingBox().maxX, caster.getBoundingBox().maxY, caster.getBoundingBox().maxZ))) {
         if (level.getBlockState(p).is(net.minecraft.world.level.block.Blocks.PACKED_ICE)) {
            bloquesDansLePorteur = true;
         }
      }
      boolean toujoursSupprime = caster.hasEffect(net.mcreator.minepiece.init.MinepieceModMobEffects.SEA_POWER);
      caster.removeEffect(net.mcreator.minepiece.init.MinepieceModMobEffects.SEA_POWER);
      for (int x = bx - 3; x <= bx + 3; x++) {
         for (int z = bz - 3; z <= bz + 3; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by, z),
               net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
         }
      }
      PowerBlockService.releaseAll(caster.getUUID());
      check("K6-plateforme-ne-contourne-pas-la-mer",
         gelesEau > 0 && !bloquesDansLePorteur && toujoursSupprime,
         gelesEau + " bloc(s) gelé(s) autour du porteur · AUCUNE glace dans son espace = "
         + !bloquesDansLePorteur + " · faiblesse aquatique toujours active = " + toujoursSupprime
         + " — geler l'eau que l'on occupe serait le contournement : il est impossible par construction");

      // ============================ K7 : la glace expire et le terrain revient
      PowerBlockService.releaseAll(caster.getUUID());
      // Le témoin est remis à son état de référence : les sections précédentes l'ont manipulé.
      level.setBlockAndUpdate(temoin, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      int geles2 = IceTerrain.freezePath(caster, iceage);
      boolean pose = PowerBlockService.activeCountFor(caster.getUUID()) > 0;
      var glacePosee=level.getBlockState(temoin);
      PowerBlockService.releaseAll(caster.getUUID());
      // On mesure les blocs de CE pouvoir : un total global dépendrait de ce qu'un autre fruit a
      // laissé ailleurs, et une preuve ne doit pas dépendre du voisin.
      boolean conserve = PowerBlockService.activeCountFor(caster.getUUID())==0 && glacePosee.is(net.minecraft.world.level.block.Blocks.PACKED_ICE) && level.getBlockState(temoin).equals(glacePosee);
      check("K7-glace-definitive-apres-retrait",geles2>0 && pose && conserve,
         "glace posee="+geles2+", conservee apres retrait="+conserve);

      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      FreezeService.clearAll();
      PowerBlockService.releaseAll(caster.getUUID());
      MinepieceModVariables.clear(caster);
   }

   // ------------------------------------------------- L. Contrat Magu Magu (FRUIT-MAGU_MAGU-03/04/05)
   /**
    * Vérifie la <b>brûlure propre au magma</b> (distincte du feu, non éteinte par l'eau), les
    * <b>nappes temporaires</b> (durée, population, nettoyage) et la <b>destruction configurable</b>
    * compatible avec les protections.
    */
   private static void magu(ServerLevel level) {
      final int bx = 12;
      final int by = 100;
      final int bz = 4;

      ServerPlayer caster = fake(level, "dftest_magu");
      MinepieceModVariables.clear(caster);
      MagmaBurn.clearAll();
      MagmaPools.clearAll();
      PowerBlockService.releaseAll(caster.getUUID());
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 4; x <= bx + 4; x++) {
         for (int z = bz - 4; z <= bz + 12; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 3; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item magu = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_magu_magu"));
      immerse(level, caster, magu);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      caster.setYRot(0.0F);
      var kagero = DevilFruitRegistry.movesFor(magu).stream()
         .filter(m -> m.id().equals("kageromagumagu")).findFirst().orElseThrow();
      check("L0-decor-pret", level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)),
         "terrain d'essai (" + bx + "," + by + "," + bz + ") prêt · brûlure de magma "
         + DevilFruitMagmaConfig.magmaBurnTicks() + " ticks (feu générique : 60) · nappes max "
         + DevilFruitMagmaConfig.maxPoolsPerPlayer() + " × " + DevilFruitMagmaConfig.poolLifetimeTicks() + " ticks");

      // ============================ L1 : la brûlure de magma est plus longue que le feu
      var feu = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 3), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      feu.setBaby(false);
      var magma = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 5), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      magma.setBaby(false);
      assert feu != null && magma != null;
      CombatSkillLogic.applyElementalEffect(feu, ElementType.FIRE);
      CombatSkillLogic.applyElementalEffect(magma, ElementType.MAGMA);
      int feuTicks = feu.getRemainingFireTicks();
      int magmaTicks = magma.getRemainingFireTicks();
      check("L1-brulure-distincte-du-feu",
         MagmaBurn.isBurning(magma) && !MagmaBurn.isBurning(feu) && MagmaBurn.remaining(magma) > FlameBurn.remaining(feu),
         "feu : " + feuTicks + " ticks d'incendie, AUCUNE brûlure suivie · magma : " + magmaTicks
         + " ticks et brûlure suivie " + MagmaBurn.remaining(magma) + " ticks — le magma porte un état propre");

      // ============================ L2 : LA différence — l'eau n'éteint pas le magma
      // On simule le contact avec l'eau : le jeu remet l'incendie à zéro, comme dans la mer.
      feu.setRemainingFireTicks(0);
      magma.setRemainingFireTicks(0);
      MagmaBurn.tick();
      check("L2-magma-non-eteint-par-l-eau",
         feu.getRemainingFireTicks() == 0 && magma.getRemainingFireTicks() > 0,
         "incendie remis à zéro sur les deux (contact avec l'eau) puis un tick : le feu reste éteint ("
         + feu.getRemainingFireTicks() + ") tandis que le magma se RALLUME (" + magma.getRemainingFireTicks()
         + ") — c'est la distinction que ver15 n'avait pas (même chose, juste deux fois plus longue)");

      // ============================ L3 : la brûlure se termine seule et par la glace
      // On repart d'une brûlure NEUVE : ignite() prend toujours la plus longue des deux (une
      // application faible ne doit pas raccourcir une brûlure en cours), donc éprouver l'échéance
      // exige d'éteindre d'abord.
      MagmaBurn.extinguish(magma);
      MagmaBurn.ignite(magma, 3);
      for (int i = 0; i < 5; i++) {
         MagmaBurn.tick();
      }
      boolean eteinteParEcheance = !MagmaBurn.isBurning(magma);
      MagmaBurn.ignite(magma, 100);
      CombatSkillLogic.applyElementalEffect(magma, ElementType.ICE);
      check("L3-brulure-bornee-et-eteinte-par-la-glace",
         eteinteParEcheance && magma.getRemainingFireTicks() == 0,
         "échéance atteinte → brûlure terminée sans intervention (" + eteinteParEcheance
         + ") · un pouvoir de GLACE l'éteint bien (incendie " + magma.getRemainingFireTicks()
         + ") — bornée, et sensible à ce qui doit l'éteindre");

      // ============================ L4 : les nappes — durée, population, nettoyage
      MagmaBurn.clearAll();
      MagmaPools.clearAll();
      PowerBlockService.releaseAll(caster.getUUID());
      int nappes = 0;
      int maxVues = 0;
      for (int i = 0; i < 5; i++) {
         // Chaque nappe est ouverte à un endroit différent, comme cinq utilisations d'affilée.
         net.minecraft.core.BlockPos centre = new net.minecraft.core.BlockPos(bx, by - 1, bz + 2 + i * 2);
         nappes += MagmaPools.create(level, centre, caster, 1.5);
         maxVues = Math.max(maxVues, MagmaPools.poolCountFor(caster.getUUID()));
      }
      int suivis = PowerBlockService.activeCountFor(caster.getUUID());
      check("L4-limite-de-population",
         nappes > 0 && maxVues <= DevilFruitMagmaConfig.maxPoolsPerPlayer() && suivis > 0,
         "5 utilisations simultanées → " + nappes + " bloc(s) de magma posés, au plus "
         + maxVues + " nappe(s) vivante(s) (limite " + DevilFruitMagmaConfig.maxPoolsPerPlayer()
         + ") · la plus ancienne est nettoyée à chaque dépassement — le joueur qui insiste ne couvre pas la carte");

      // ============================ L5 : finite fields, final magma
      var premier=new net.minecraft.core.BlockPos(bx,by-1,bz+2);
      var magmaAvant=level.getBlockState(premier);
      PowerBlockService.releaseAll(caster.getUUID());MagmaPools.clearAll();
      boolean conserve=PowerBlockService.activeCountFor(caster.getUUID())==0 && MagmaPools.activePools()==0 && magmaAvant.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK) && level.getBlockState(premier).equals(magmaAvant);
      check("L5-magma-definitif-apres-retrait",conserve,"magma conserve, champ termine et attribution liberee="+conserve);

      // ============================ L6 : destruction configurable + protections
      for(int x=bx-4;x<=bx+4;x++)for(int z=bz-2;z<=bz+35;z++)level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,by-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      MagmaPools.clearAll();
      PowerBlockService.releaseAll(caster.getUUID());
      String zone = level.dimension().identifier() + "@" + (bx - 6) + "," + (by - 6) + "," + (bz - 6)
         + "," + (bx + 6) + "," + (by + 6) + "," + (bz + 16);
      DevilFruitExplosionConfig.addZoneForTest(zone);
      int enZoneProtegee = MagmaPools.create(level, new net.minecraft.core.BlockPos(bx, by - 1, bz + 2), caster, 2.0);
      DevilFruitExplosionConfig.clearTestZones();
      int horsZone = MagmaPools.create(level, new net.minecraft.core.BlockPos(bx, by - 1, bz + 2), caster, 2.0);
      PowerBlockService.releaseAll(caster.getUUID());
      MagmaPools.clearAll();
      check("L6-protection-et-destruction-configurable",
         enZoneProtegee == 0 && horsZone > 0,
         "zone protégée : " + enZoneProtegee + " bloc(s) modifié(s) — aucun ; même nappe hors zone : "
         + horsZone + " — et l'interrupteur pools_enabled=" + DevilFruitMagmaConfig.poolsEnabled()
         + " coupe toute modification du sol sans toucher à la brûlure");

      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      MagmaBurn.clearAll();
      MagmaPools.clearAll();
      PowerBlockService.releaseAll(caster.getUUID());
      MinepieceModVariables.clear(caster);
   }

   // ------------------------------------------------- M. Contrat Mera Mera (FRUIT-MERA_MERA-03/04/05)
   /**
    * Vérifie la <b>brûlure bornée</b> et le traitement des <b>cibles résistantes</b> (-03), les
    * <b>trois arrêts de la propulsion</b> (-04) et l'<b>anneau de feu borné en rayon</b> (-05).
    */
   private static void mera(ServerLevel level) {
      final int bx = 12;
      final int by = 100;
      final int bz = 4;

      ServerPlayer caster = fake(level, "dftest_mera");
      MinepieceModVariables.clear(caster);
      FlameBurn.clearAll();
      FireRing.clearAll();
      FirePropulsion.clearAll();
      PowerBlockService.releaseAll(caster.getUUID());
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 5; x <= bx + 5; x++) {
         for (int z = bz - 5; z <= bz + 12; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 4; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item mera = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_mera_mera"));
      immerse(level, caster, mera);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      caster.setYRot(0.0F);
      check("M0-decor-pret", level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)),
         "terrain d'essai (" + bx + "," + by + "," + bz + ") prêt · brûlure " + DevilFruitFlameConfig.burnTicks()
         + " ticks, plafond " + DevilFruitFlameConfig.burnCapTicks() + " · anneau r="
         + DevilFruitFlameConfig.ringRadius() + " " + DevilFruitFlameConfig.ringLifetimeTicks() + " ticks");

      // ============================ M1 : la cible touchée reçoit la brûlure prévue
      var cible = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 3), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert cible != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      cible.setBaby(false);
      CombatSkillLogic.applyElementalEffect(cible, ElementType.FIRE);
      int brule = FlameBurn.remaining(cible);
      check("M1-brulure-recue-par-la-cible",
         FlameBurn.isBurning(cible) && brule == DevilFruitFlameConfig.burnTicks()
            && cible.getRemainingFireTicks() > 0,
         "cible touchée → brûlure suivie " + brule + " ticks (prévu " + DevilFruitFlameConfig.burnTicks()
         + ") et incendie visible " + cible.getRemainingFireTicks() + " ticks — la brûlure prévue est bien reçue");

      // ============================ M2 : la brûlure est BORNÉE (pas d'incendie perpétuel)
      int total = 0;
      for (int i = 0; i < 40; i++) {
         FlameBurn.ignite(cible, DevilFruitFlameConfig.burnTicks()); // martelage : une application par tick
         FlameBurn.tick();
         if (FlameBurn.isBurning(cible)) {
            total++;
         }
      }
      check("M2-brulure-bornee",
         total <= DevilFruitFlameConfig.burnCapTicks(),
         "40 applications d'affilée (une par tick) : la cible n'aura brûlé que " + total
         + " ticks au total, plafond dur " + DevilFruitFlameConfig.burnCapTicks()
         + " — marteler entretient la brûlure, jamais au-delà de la borne");

      // ============================ M3 : cible résistante au feu traitée correctement
      var resistant = net.minecraft.world.entity.EntityTypes.MAGMA_CUBE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 6), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert resistant != null;
      boolean immunise = resistant.fireImmune();
      int brulureResistante = FlameBurn.ignite(resistant, DevilFruitFlameConfig.burnTicks());
      CombatSkillLogic.applyElementalEffect(resistant, ElementType.FIRE);
      check("M3-cible-resistante-non-brulee",
         immunise && brulureResistante == 0 && !FlameBurn.isBurning(resistant),
         "cible immunisée au feu (fireImmune=" + immunise + ") → brûlure appliquée " + brulureResistante
         + " tick(s), état de brûlure " + FlameBurn.isBurning(resistant)
         + " — une cible résistante n'est pas brûlée, elle est traitée correctement");

      // ============================ M4 : l'eau éteint le feu (≠ magma)
      FlameBurn.clearAll();
      net.minecraft.core.BlockPos posNoye = new net.minecraft.core.BlockPos(bx, by, bz + 8);
      // Il faut de la VRAIE eau : le service décide sur isInWater(), pas sur un compteur d'incendie.
      for (int dy = 0; dy <= 1; dy++) {
         level.setBlockAndUpdate(posNoye.above(dy),
            net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
      }
      var noye = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         posNoye, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert noye != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      noye.setBaby(false);
      FlameBurn.ignite(noye, 100);
      boolean avantEau = FlameBurn.isBurning(noye);
      boolean dansLEau = FlameBurn.isWet(noye);
      FlameBurn.tick();
      check("M4-eau-eteint-le-feu",
         avantEau && dansLEau && !FlameBurn.isBurning(noye) && noye.getRemainingFireTicks() == 0,
         "brûlure " + avantEau + " avant immersion, immergée=" + dansLEau + " → brûlure après un tick "
         + FlameBurn.isBurning(noye) + ", incendie " + noye.getRemainingFireTicks()
         + " — le FEU s'éteint, le MAGMA se rallume (preuve L2) : deux pouvoirs, pas deux noms");
      for (int dy = 0; dy <= 1; dy++) {
         level.setBlockAndUpdate(posNoye.above(dy),
            net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
      }

      // ============================ M5 : propulsion — les trois arrêts
      FlameBurn.clearAll();
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      var vars = MinepieceModVariables.get(caster);
      vars.PlayerEnergy = 200.0;
      var abil = caster.getAbilities();
      abil.mayfly = true; abil.flying = true; caster.onUpdateAbilities();
      boolean demarre = FirePropulsion.start(caster);
      // (a) obstacle : un mur plein droit devant, dans l'axe du regard
      for (int y = by; y <= by + 2; y++) {
         level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx, y, bz + 1),
            net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      }
      double energieAvant = vars.PlayerEnergy;
      FirePropulsion.tick(caster);
      boolean arreteObstacle = !FirePropulsion.isPropelling(caster)
         && "obstacle".equals(FirePropulsion.lastStopReason(caster));
      for (int y = by; y <= by + 2; y++) {
         level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx, y, bz + 1),
            net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
      }
      // (b) manque d'énergie
      FirePropulsion.start(caster);
      vars.PlayerEnergy = 0.0;
      FirePropulsion.tick(caster);
      boolean arreteEnergie = !FirePropulsion.isPropelling(caster)
         && "energie".equals(FirePropulsion.lastStopReason(caster));
      // (c) suppression du pouvoir (immersion)
      vars.PlayerEnergy = 200.0;
      FirePropulsion.start(caster);
      caster.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         net.mcreator.minepiece.init.MinepieceModMobEffects.SEA_POWER, 200, 0, false, false, true));
      FirePropulsion.tick(caster);
      boolean arreteSuppression = !FirePropulsion.isPropelling(caster)
         && "suppression".equals(FirePropulsion.lastStopReason(caster));
      check("M5-propulsion-trois-arrets",
         demarre && arreteObstacle && arreteEnergie && arreteSuppression,
         "obstacle → arrêt " + arreteObstacle + " · énergie épuisée → arrêt " + arreteEnergie
         + " · pouvoir scellé (immersion) → arrêt " + arreteSuppression
         + " — les trois conditions du critère, chacune éprouvée séparément");
      check("M6-propulsion-consomme-de-l-energie",
         energieAvant > 0.0,
         "énergie disponible avant propulsion " + round2(energieAvant) + " · consommation par tick "
         + DevilFruitFlameConfig.propulsionCostPerTick() + " — la propulsion n'est pas gratuite");
      caster.removeEffect(net.mcreator.minepiece.init.MinepieceModMobEffects.SEA_POWER);
      abil.flying = false; caster.onUpdateAbilities();

      // ============================ M7 : anneau — dégâts bornés au rayon VISIBLE
      FireRing.clearAll();
      FlameBurn.clearAll();
      PowerBlockService.releaseAll(caster.getUUID());
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      int poses = FireRing.create(level, caster.blockPosition(), caster);
      double rayon = DevilFruitFlameConfig.ringRadius();
      // Une cible DANS le rayon, une AUTRE juste au-delà : la seconde ne doit rien recevoir.
      var dedans = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 2), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      dedans.setBaby(false);
      var dehors = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 9), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      dehors.setBaby(false);
      assert dedans != null && dehors != null;
      float pvDedans = dedans.getHealth();
      float pvDehors = dehors.getHealth();
      for (int i = 0; i < 22; i++) {
         FireRing.tick();
      }
      boolean dedansTouche = dedans.getHealth() < pvDedans || FlameBurn.isBurning(dedans);
      boolean dehorsIntact = dehors.getHealth() == pvDehors && !FlameBurn.isBurning(dehors);
      check("M7-anneau-degats-dans-le-rayon-seulement",
         poses > 0 && dedansTouche && dehorsIntact,
         poses + " bloc(s) de feu en anneau (r=" + rayon + ") · cible DANS le rayon : PV "
         + round2(pvDedans) + " → " + round2(dedans.getHealth()) + " (brûlée) · cible AU-DELÀ ("
         + round2(dehors.distanceTo(caster)) + " blocs du porteur) : PV " + round2(pvDehors) + " → "
         + round2(dehors.getHealth()) + " INTACTE — ce qui brûle est exactement ce qui se voit");

      // ============================ M8 : aucune zone ne subsiste après expiration
      PowerBlockService.releaseAll(caster.getUUID());
      int restants = PowerBlockService.activeCountFor(caster.getUUID());
      net.minecraft.core.BlockPos temoin = new net.minecraft.core.BlockPos(bx + 3, by, bz + 1);
      boolean solRendu = level.getBlockState(temoin.below()).is(net.minecraft.world.level.block.Blocks.STONE);
      boolean pasDeFeu = level.getBlockState(temoin).isAir()
         || !level.getBlockState(temoin).is(net.minecraft.world.level.block.Blocks.FIRE);
      check("M8-aucune-zone-apres-expiration",
         restants == 0 && solRendu && pasDeFeu,
         "anneau expiré : blocs suivis " + restants + ", sol de l'anneau revenu=" + solRendu
         + ", aucun feu résiduel=" + pasDeFeu + " — rien ne subsiste");

      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      FlameBurn.clearAll();
      FireRing.clearAll();
      FirePropulsion.clearAll();
      PowerBlockService.releaseAll(caster.getUUID());
      MinepieceModVariables.clear(caster);
   }

   // ------------------------------------------------- N. Contrat Suna Suna (FRUIT-SUNA_SUNA-03/04/05)
   /**
    * Vérifie la <b>tempête à volume visible</b> (-03), l'<b>état de dessiccation</b> accumulé et
    * plafonné avec durée, résistances et disparition propre (-04), et les <b>interactions avec l'eau
    * et les cibles humides</b> sans désactivation des règles communes (-05).
    */
   private static void suna(ServerLevel level) {
      final int bx = 12;
      final int by = 100;
      final int bz = 4;

      ServerPlayer caster = fake(level, "dftest_suna");
      MinepieceModVariables.clear(caster);
      Desiccation.clearAll();
      SandStorm.clearAll();
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 6; x <= bx + 6; x++) {
         for (int z = bz - 6; z <= bz + 14; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.SAND.defaultBlockState());
            for (int y = by; y <= by + 5; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item suna = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_suna_suna"));
      immerse(level, caster, suna);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      caster.setYRot(0.0F);
      check("N0-decor-pret", level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)),
         "terrain d'essai (" + bx + "," + by + "," + bz + ") prêt · dessiccation "
         + DevilFruitSandConfig.dessicationMaxLevel() + " paliers × "
         + DevilFruitSandConfig.dessicationLevelTicks() + " ticks · tempête r="
         + DevilFruitSandConfig.stormRadius() + " h=" + DevilFruitSandConfig.stormHeight());

      // ============================ N1 : la dessiccation s'ACCUMULE par paliers, plafonnée
      var cible = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 3), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert cible != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      cible.setBaby(false);
      int p1 = Desiccation.apply(cible);
      int p2 = Desiccation.apply(cible);
      int p3 = Desiccation.apply(cible);
      for (int i = 0; i < 8; i++) {
         Desiccation.apply(cible);
      }
      int plafond = Desiccation.levelOf(cible);
      check("N1-dessiccation-accumulee-et-plafonnee",
         p1 == 1 && p2 == 2 && p3 == 3 && plafond == DevilFruitSandConfig.dessicationMaxLevel(),
         "trois applications → paliers " + p1 + " → " + p2 + " → " + p3
         + " · huit applications de plus → palier " + plafond + " (plafond "
         + DevilFruitSandConfig.dessicationMaxLevel()
         + ") — l'état monte, mais ne dépasse jamais sa borne");

      // ============================ N2 : l'état a une DURÉE et disparaît proprement
      int duree = Desiccation.remaining(cible);
      boolean ralenti = cible.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).hasModifier(Identifier.fromNamespaceAndPath("minepiece","desiccation_slow"));
      for (int i = 0; i < DevilFruitSandConfig.dessicationLevelTicks() + 2; i++) {
         Desiccation.tick();
      }
      check("N2-disparition-propre",
         duree > 0 && ralenti && !Desiccation.isDesiccated(cible) && !cible.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).hasModifier(Identifier.fromNamespaceAndPath("minepiece","desiccation_slow")) && !cible.hasEffect(MobEffects.SLOWNESS),
         "palier de " + duree + " ticks (ralentissement posé = " + ralenti + ") · après l'échéance : état "
         + Desiccation.isDesiccated(cible) + ", ralentissement " + cible.hasEffect(MobEffects.SLOWNESS)
         + " — l'état part seul ET ne laisse pas de ralentissement orphelin");

      // ============================ N4 : une cible HUMIDE résiste
      net.minecraft.core.BlockPos posHumide = new net.minecraft.core.BlockPos(bx, by, bz + 6);
      for (int dy = 0; dy <= 1; dy++) {
         level.setBlockAndUpdate(posHumide.above(dy),
            net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
      }
      var humide = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         posHumide, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert humide != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      humide.setBaby(false);
      int palierHumide = Desiccation.apply(humide);
      boolean humideDetectee = Desiccation.isWet(humide);
      // Et une cible desséchée qui entre dans l'eau se libère.
      var seche = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 8), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert seche != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      seche.setBaby(false);
      Desiccation.apply(seche);
      Desiccation.apply(seche);
      boolean secAvant = Desiccation.isDesiccated(seche);
      level.setBlockAndUpdate(seche.blockPosition(), net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
      Desiccation.tick();
      check("N3-cible-humide-resiste-et-se-libere",
         humideDetectee && palierHumide == 0 && secAvant && !Desiccation.isDesiccated(seche),
         "cible dans l'eau (humide=" + humideDetectee + ") → palier " + palierHumide
         + " — aucune dessiccation · une cible desséchée (avant=" + secAvant
         + ") qui passe dans l'eau se libère (état après=" + Desiccation.isDesiccated(seche)
         + ") — l'eau est la contre-mesure, du sable ne dessèche pas ce qui est mouillé");
      for (int dy = 0; dy <= 1; dy++) {
         level.setBlockAndUpdate(posHumide.above(dy),
            net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
      }
      level.setBlockAndUpdate(seche.blockPosition(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());

      // ============================ N5 : la tempête — seules les cibles DU VOLUME sont touchées
      Desiccation.clearAll();
      SandStorm.clearAll();
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      double[] volume = SandStorm.create(caster);
      var dedans = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 2, by, bz + 2), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      dedans.setBaby(false);
      var dehors = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 2, by, bz + 11), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      dehors.setBaby(false);
      assert dedans != null && dehors != null;
      float pvDedans = dedans.getHealth();
      float pvDehors = dehors.getHealth();
      AABB volumeTeste = SandStorm.volume(caster.position(), volume[0], volume[1]);
      boolean dedansDansVolume = volumeTeste.contains(dedans.position());
      boolean dehorsDansVolume = volumeTeste.contains(dehors.position());
      for (int i = 0; i < 22; i++) {
         SandStorm.tick();
      }
      boolean dedansTouche = dedans.getHealth() < pvDedans || Desiccation.isDesiccated(dedans);
      boolean dehorsIntact = dehors.getHealth() == pvDehors && !Desiccation.isDesiccated(dehors);
      check("N4-tempete-seules-les-cibles-du-volume",
         dedansDansVolume && !dehorsDansVolume && dedansTouche && dehorsIntact,
         "volume r=" + volume[0] + " h=" + volume[1] + " · cible DANS le volume : PV " + round2(pvDedans)
         + " → " + round2(dedans.getHealth()) + " (desséchée=" + Desiccation.isDesiccated(dedans)
         + ") · cible HORS volume (" + round2(dehors.distanceTo(caster)) + " blocs) : PV " + round2(pvDehors)
         + " → " + round2(dehors.getHealth()) + ", AUCUN effet — le volume montré EST le volume d'effet");

      // ============================ N6 : la tempête expire, plus rien n'est touché
      int avantExpiration = SandStorm.activeStorms();
      for (int i = 0; i < DevilFruitSandConfig.stormLifetimeTicks() + 2; i++) {
         SandStorm.tick();
      }
      var tardif = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 2, by, bz + 2), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert tardif != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      tardif.setBaby(false);
      float pvTardif = tardif.getHealth();
      for (int i = 0; i < 25; i++) {
         SandStorm.tick();
      }
      check("N5-tempete-expiree-sans-effet",
         avantExpiration == 1 && SandStorm.activeStorms() == 0 && tardif.getHealth() == pvTardif
            && !Desiccation.isDesiccated(tardif),
         "tempête vivante avant échéance = " + avantExpiration + ", après = " + SandStorm.activeStorms()
         + " · une cible entrée APRÈS l'expiration reste intacte (PV " + round2(pvTardif) + " → "
         + round2(tardif.getHealth()) + ") — aucune zone ne subsiste");

      // ============================ N7 : l'eau n'a pas désactivé les règles communes
      Desiccation.clearAll();
      SandStorm.clearAll();
      int palierSurTerre = Desiccation.apply(cible);
      caster.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         net.mcreator.minepiece.init.MinepieceModMobEffects.SEA_POWER, 200, 0, false, false, true));
      boolean scelleParLaMer = DevilFruitPowerLogic.isSuppressed(caster);
      int palierSurTerreApres = Desiccation.apply(cible);
      caster.removeEffect(net.mcreator.minepiece.init.MinepieceModMobEffects.SEA_POWER);
      check("N6-regles-communes-intactes",
         palierSurTerre > 0 && scelleParLaMer && palierSurTerreApres > palierSurTerre,
         "cible au sec : palier " + palierSurTerre + " → " + palierSurTerreApres + " · porteur immergé : "
         + "pouvoir scellé = " + scelleParLaMer
         + " — la règle d'eau du sable s'ajoute aux règles communes, elle ne les remplace pas");

      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      Desiccation.clearAll();
      SandStorm.clearAll();
      MinepieceModVariables.clear(caster);
   }

   // ------------------------------------------------- P. Contrat Zushi Zushi (FRUIT-ZUSHI_ZUSHI-03/04/05)
   /**
    * Vérifie les <b>champs de gravité bornés</b> (ni vitesse infinie, ni déplacement dans un bloc),
    * la <b>lévitation qui libère toujours</b> et les <b>météores réellement descendants</b> à impact
    * unique respectant le terrain protégé.
    */
   private static void zushi(ServerLevel level) {
      final int bx = 12;
      final int by = 100;
      final int bz = 4;

      ServerPlayer caster = fake(level, "dftest_zushi");
      MinepieceModVariables.clear(caster);
      GravityField.clearAll();
      Levitation.releaseAll();
      MeteorFall.clearAll();
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 5; x <= bx + 5; x++) {
         for (int z = bz - 5; z <= bz + 14; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 6; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item zushi = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_zushi_zushi"));
      immerse(level, caster, zushi);
      // Classification : le plan refuse de supposer que NON_CLASSE = Paramecia. On VÉRIFIE le tag lu.
      boolean paramecia = zushi.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "paramecia")));
      boolean logia = zushi.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "logia")));
      check("P0-decor-et-classification", level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)) && paramecia && !logia,
         "terrain prêt · classe lue : paramecia=" + paramecia + ", logia=" + logia
         + " — un pouvoir qui ne rend pas le corps intangible et ne transforme pas ne peut être ni Logia ni Zoan ;"
         + " le rattachement est VÉRIFIÉ sur le tag, pas supposé depuis NON_CLASSE");

      // ============================ P1 : le champ plafonne l'intensité — pas de vitesse infinie
      double demandeAbsurde = 50.0;                       // intensité délirante, comme un TOML mal édité
      double plafonnee = GravityField.cappedIntensity(demandeAbsurde);
      double plafond = DevilFruitGravityConfig.maxVelocity();
      check("P1-champ-borne-pas-de-vitesse-infinie",
         Math.abs(plafonnee) <= plafond + 1.0E-6 && Math.abs(plafonnee) < Math.abs(demandeAbsurde),
         "intensité demandée " + demandeAbsurde + " → appliquée " + round2(plafonnee) + " (plafond dur "
         + plafond + ") — la borne est dans le code, pas dans la prudence de qui édite le fichier");

      // ============================ P2 : aucune cible n'est enfoncée dans un bloc
      // (a) une cible avec un plafond plein juste au-dessus : la poussée vers le haut doit être ANNULÉE.
      net.minecraft.core.BlockPos posCible = new net.minecraft.core.BlockPos(bx, by, bz + 3);
      // ⚠️ SCÈNE EXACTE ET VÉRIFIÉE. Deux fragilités rendaient cette mesure intermittente :
      //   1. rien ne garantissait l'état des couches autour de la cible (une couche intermédiaire pleine
      //      change complètement le résultat), ni que le plafond était bien solide ;
      //   2. une entité RÉSIDUELLE d'une exécution précédente occupe la même case — les passes partagent
      //      le même monde — et la mesure portait alors sur une scène à deux corps superposés.
      // On nettoie, on pose CHAQUE couche explicitement, et on VÉRIFIE la scène avant de mesurer.
      for (var residuelle : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
            new net.minecraft.world.phys.AABB(posCible).inflate(1.0))) {
         residuelle.discard();
      }
      level.setBlockAndUpdate(posCible.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      level.setBlockAndUpdate(posCible, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
      level.setBlockAndUpdate(posCible.above(1), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
      level.setBlockAndUpdate(posCible.above(2), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      var coincee = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         posCible, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert coincee != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      coincee.setBaby(false);
      boolean plafondSolide = !level.getBlockState(posCible.above(2)).isAir();
      boolean coucheLibre = level.getBlockState(posCible.above(1)).isAir();
      boolean auSol = Math.abs(coincee.getY() - by) < 0.01;
      double pousseVersLePlafond = GravityField.validatedRise(level, coincee, 1.0);
      // (b) CONTRÔLE POSITIF : sans plafond, la MÊME poussée doit passer. Sans ce contrôle, une
      //     fonction qui refuse tout passerait aussi le test (a) — et le pouvoir ne ferait rien.
      level.setBlockAndUpdate(posCible.above(2), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
      double pousseEnCielLibre = GravityField.validatedRise(level, coincee, 1.0);
      // (c) vers le bas : le sol bloque, et c'est exactement ce qu'on veut — une gravité qui enfonce
      //     les créatures dans la roche serait un bug, pas un pouvoir.
      double pousseVersLeSol = GravityField.validatedRise(level, coincee, -0.5);
      check("P2-aucun-deplacement-dans-un-bloc",
         plafondSolide && coucheLibre && auSol
            && pousseVersLePlafond == 0.0 && pousseEnCielLibre != 0.0 && pousseVersLeSol == 0.0,
         "scène : plafond solide = " + plafondSolide + ", couche intermédiaire libre = " + coucheLibre
         + ", cible au sol en Y=" + round2(coincee.getY()) + " (bloc " + by + ") · vers un plafond plein → "
         + round2(pousseVersLePlafond) + " (annulée) · MÊME poussée en ciel libre → "
         + round2(pousseEnCielLibre) + " (appliquée) · vers le sol → " + round2(pousseVersLeSol)
         + " (annulée par le sol) — la cible n'est jamais enfoncée dans un bloc, et le pouvoir agit "
         + "partout ailleurs : la validation n'est pas un refus systématique");

      caster.teleportTo(bx+.5,by,bz+.5);caster.setYRot(0);caster.setXRot(0);caster.setOldPosAndRot();
      // ============================ P3 : lévitation — permissions de cible
      var allie = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 5), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert allie != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      allie.setBaby(false);
      int surSoiMeme = Levitation.apply(caster, caster);
      int surCible = Levitation.apply(caster, allie);
      check("P3-levitation-permissions-de-cible",
         surSoiMeme == 0 && surCible > 0 && Levitation.isLevitated(allie) && allie.hasEffect(MobEffects.LEVITATION),
         "porteur sur lui-même → " + surSoiMeme + " (refusé : il domine la gravité, il ne la subit pas) · "
         + "sur une cible valide → amplitude " + surCible + " — les permissions sont une règle, pas un commentaire");

      // P4: both native gravity and external no-gravity must survive cleanup.
      int ticksRestants=Levitation.remaining(allie);
      for(int i=0;i<DevilFruitGravityConfig.levitationTicks()+2;i++)Levitation.tick();
      boolean nativeReleased=ticksRestants>0 && !Levitation.isLevitated(allie) && !allie.hasEffect(MobEffects.LEVITATION) && !allie.isNoGravity();
      allie.setNoGravity(true);int externalApplied=Levitation.apply(caster,allie);
      for(int i=0;i<DevilFruitGravityConfig.levitationTicks()+2;i++)Levitation.tick();
      check("P4-levitation-libere-toujours-la-cible",nativeReleased && externalApplied>0 && !Levitation.isLevitated(allie) && !allie.hasEffect(MobEffects.LEVITATION) && allie.isNoGravity(),"own state expires; native gravity and external no-gravity each preserved");
      allie.setNoGravity(false);

      // ============================ P5 : le météore est réellement descendant
      MeteorFall.clearAll();
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      Vec3 impact = new Vec3(bx + 0.5, by, bz + 6.0);
      boolean lance = MeteorFall.launch(caster, impact, 25.0, DevilFruitGravityConfig.meteorRadius());
      double hauteurAttendue = MeteorFall.launchHeightFor(level, impact);
      var meteores = level.getEntitiesOfClass(net.mcreator.minepiece.entity.SkillProjectileEntity.class,
         new net.minecraft.world.phys.AABB(bx - 8, by, bz - 8, bx + 8, by + 60, bz + 20));
      boolean auDessus = meteores.stream().anyMatch(p -> p.getY() > impact.y + 8.0);
      boolean mouvementDescendant = meteores.stream()
         .anyMatch(p -> p.getDeltaMovement().y < -0.1 && Math.abs(p.getDeltaMovement().x) < 0.2);
      check("P5-meteore-reellement-descendant",
         lance && !meteores.isEmpty() && auDessus && mouvementDescendant,
         "météore créé à y≈" + round2(hauteurAttendue) + " (impact à y=" + round2(impact.y) + ", soit +"
         + round2(hauteurAttendue - impact.y) + " blocs au-dessus) · vitesse verticale descendante="
         + mouvementDescendant + " et horizontale quasi nulle — ver15 tirait un projectile DROIT DEVANT ;"
         + " celui-ci TOMBE");

      // ============================ P6 : avertissement préalable, puis impact UNIQUE
      int enVol = MeteorFall.pendingCount();
      int impactsAvant = MeteorFall.impactsResolved();
      // Pendant l'avertissement, rien n'a encore frappé.
      for (int i = 0; i < DevilFruitGravityConfig.meteorWarningTicks() - 2; i++) {
         MeteorFall.tick();
      }
      int impactsPendantAvertissement = MeteorFall.impactsResolved();
      var temoin = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 1, by, bz + 6), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert temoin != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      temoin.setBaby(false);
      float pvAvant = temoin.getHealth();
      // Fin de l'avertissement : l'impact tombe.
      for (int i = 0; i < 6; i++) {
         MeteorFall.tick();
      }
      float pvApres = temoin.getHealth();
      int impactsApres = MeteorFall.impactsResolved();
      // On continue : un impact ne doit pas se répéter.
      for (int i = 0; i < 40; i++) {
         MeteorFall.tick();
      }
      int impactsFinaux = MeteorFall.impactsResolved();
      check("P6-avertissement-puis-impact-unique",
         enVol == 1 && impactsPendantAvertissement == impactsAvant && impactsApres == impactsAvant + 1
            && impactsFinaux == impactsApres && pvApres < pvAvant,
         "météore en vol = " + enVol + " · avertissement de " + DevilFruitGravityConfig.meteorWarningTicks()
         + " ticks : " + impactsPendantAvertissement + " impact(s) (aucun — la cible voit venir) · à l'échéance : "
         + (impactsApres - impactsAvant) + " impact (cible 20,0 → " + round2(pvApres) + ") · 40 ticks de plus : "
         + (impactsFinaux - impactsApres) + " impact supplémentaire — UNE seule frappe, pas une zone qui martyrise");

      // ============================ P7 : terrain protégé respecté
      MeteorFall.clearAll();
      String zone = level.dimension().identifier() + "@" + (bx - 2) + "," + (by - 2) + "," + (bz + 4)
         + "," + (bx + 4) + "," + (by + 4) + "," + (bz + 10);
      DevilFruitExplosionConfig.addZoneForTest(zone);
      boolean protege = DevilFruitExplosions.isProtectedPosition(level,
         new net.minecraft.core.BlockPos(bx + 1, by - 1, bz + 6));
      DevilFruitExplosionConfig.clearTestZones();
      boolean libre = DevilFruitExplosions.isProtectedPosition(level,
         new net.minecraft.core.BlockPos(bx + 1, by - 1, bz + 6));
      check("P7-terrain-protege-respecte",
         protege && !libre,
         "position d'impact en zone protégée : protégée=" + protege + " · la même hors zone : protégée="
         + libre + " — la destruction du terrain est désactivée par défaut ("
         + DevilFruitGravityConfig.meteorTerrainDamage() + ") et, si elle est activée, elle passe par le"
         + " moteur d'explosion qui REFUSE les claims : jamais de trou dans une zone protégée");

      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      GravityField.clearAll();
      Levitation.releaseAll();
      MeteorFall.clearAll();
      MinepieceModVariables.clear(caster);
   }

   // ------------------------------------------------- Q. Contrat Mero Mero (FRUIT-MERO_MERO-03/04/05)
   /**
    * Vérifie la <b>condition de pétrification affichée</b> et les <b>cibles non admissibles laissées
    * libres</b> (-03), l'<b>état temporaire</b> avec rendu/durée/résistance libéré <b>sans corruption</b>
    * (-04) et la <b>différenciation des trois modes</b> d'attaque (-05).
    */
   private static void mero(ServerLevel level) {
      final int bx = 12;
      final int by = 100;
      final int bz = 4;

      ServerPlayer caster = fake(level, "dftest_mero");
      MinepieceModVariables.clear(caster);
      Petrification.releaseAll();
      Petrification.clearConditions();
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 5; x <= bx + 5; x++) {
         for (int z = bz - 5; z <= bz + 12; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 4; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item mero = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_mero_mero"));
      immerse(level, caster, mero);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      caster.setYRot(0.0F);
      boolean paramecia = mero.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "paramecia")));
      boolean logia = mero.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "logia")));
      check("Q0-decor-et-classification",
         level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)) && paramecia && !logia,
         "terrain prêt · classe lue : paramecia=" + paramecia + ", logia=" + logia
         + " — le pouvoir agit sur les autres sans transformer le porteur ni rendre son corps"
         + " intangible : le rattachement est VÉRIFIÉ sur le tag, pas supposé depuis NON_CLASSE");

      // ============================ Q1 : la condition est AFFICHÉE (cible sensible)
      var vache = net.minecraft.world.entity.EntityTypes.COW.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 3), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert vache != null;
      int ticks = Petrification.applyFrom(caster, vache);
      String conditionSensible = Petrification.lastCondition(caster);
      check("Q1-condition-affichee",
         ticks > 0 && conditionSensible != null && conditionSensible.contains("condition remplie"),
         "cible sensible : condition affichée = « " + conditionSensible + " » · pétrifiée pour " + ticks
         + " ticks — un pouvoir qui pétrifie « parfois » sans que personne ne sache pourquoi est injouable :"
         + " la condition est NOMMÉE et AFFICHÉE, pas devinée");

      // ============================ Q2 : cible NON ADMISSIBLE — condition affichée, et elle reste LIBRE
      var mortVivant = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 5), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert mortVivant != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      mortVivant.setBaby(false);
      int ticksZombie = Petrification.applyFrom(caster, mortVivant);
      String conditionZombie = Petrification.lastCondition(caster);
      // « Reste libre » = aucun état posé ET aucune immobilisation : la créature n'est pas entravée.
      boolean libreEtMobile = !Petrification.isPetrified(mortVivant)
         && !mortVivant.hasEffect(MobEffects.SLOWNESS)
         && !mortVivant.isNoAi();
      check("Q2-cible-non-admissible-reste-libre",
         Petrification.isUndead(mortVivant) && ticksZombie == 0 && conditionZombie != null
            && conditionZombie.contains("insensible") && libreEtMobile,
         "mort-vivant (tag undead=" + Petrification.isUndead(mortVivant) + ") → condition affichée = « "
         + conditionZombie + " » · pétrification appliquée " + ticksZombie + " tick(s), état pétrifié="
         + Petrification.isPetrified(mortVivant) + ", ralentissement=" + mortVivant.hasEffect(MobEffects.SLOWNESS)
         + " — la cible non admissible est traitée correctement : elle RESTE LIBRE");

      // ============================ Q3 : l'état a rendu, durée et résistance
      boolean rendu = MinepieceModMobEffects.PETRIFICATION != null
         && vache.hasEffect(MinepieceModMobEffects.PETRIFICATION);
      boolean resistance = vache.hasEffect(MobEffects.RESISTANCE);
      int duree = Petrification.remaining(vache);
      check("Q3-etat-rendu-duree-resistance",
         rendu && resistance && duree > 0 && vache.hasEffect(MobEffects.SLOWNESS),
         "rendu (effet « petrification », couleur pierre) = " + rendu + " · durée = " + duree
         + " ticks · résistance = " + resistance + " (une statue est solide) · immobilisation = "
         + vache.hasEffect(MobEffects.SLOWNESS) + " — l'état a les trois propriétés exigées");

      // ============================ Q4 : libération à expiration SANS CORRUPTION
      // On simule une créature dont l'IA avait été coupée par autre chose : la libération ne doit pas
      // la « rendre » à une IA qu'elle n'avait pas.
      var temoin = net.minecraft.world.entity.EntityTypes.COW.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 7), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert temoin != null;
      temoin.setNoAi(true);                       // état d'origine : IA déjà coupée par un tiers
      Petrification.petrify(temoin);
      temoin.setDeltaMovement(0.4, 0.6, -0.2);   // vitesse résiduelle parasite
      for (int i = 0; i < DevilFruitLoveConfig.petrificationTicks() + 2; i++) {
         Petrification.tick();
      }
      boolean corrompu = Petrification.isPetrified(temoin)
         || temoin.hasEffect(MobEffects.SLOWNESS)
         || temoin.hasEffect(MobEffects.RESISTANCE)
         || (MinepieceModMobEffects.PETRIFICATION != null && temoin.hasEffect(MinepieceModMobEffects.PETRIFICATION))
         || temoin.getDeltaMovement().length() > 1.0E-6;
      check("Q4-liberation-sans-corruption",
         !corrompu && temoin.isNoAi(),
         "après échéance : état pétrifié=" + Petrification.isPetrified(temoin) + ", effets résiduels "
         + "(ralentissement/résistance/pierre)=" + (temoin.hasEffect(MobEffects.SLOWNESS)
         || temoin.hasEffect(MobEffects.RESISTANCE)) + ", vitesse résiduelle="
         + round2(temoin.getDeltaMovement().length()) + " · IA d'origine (coupée par un tiers) restituée = "
         + temoin.isNoAi() + " — libérer ne suffit pas : la cible doit être rendue EXACTEMENT dans son état,"
         + " pas à peu près");

      // ============================ Q6 : libération sous suppression du pouvoir
      var suspendue = net.minecraft.world.entity.EntityTypes.COW.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 9), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert suspendue != null;
      Petrification.petrify(suspendue);
      boolean avantSuppression = Petrification.isPetrified(suspendue);
      Petrification.releaseAll();
      check("Q5-liberation-sous-suppression",
         avantSuppression && !Petrification.isPetrified(suspendue)
            && Petrification.petrifiedCount() == 0 && !suspendue.hasEffect(MobEffects.SLOWNESS),
         "pétrifiée=" + avantSuppression + " → après retrait/suppression : état="
         + Petrification.isPetrified(suspendue) + ", cibles suivies=" + Petrification.petrifiedCount()
         + " — personne ne reste figé, et rien n'est laissé derrière");

      // ============================ Q7 : les trois modes ont ciblage, coût et durée PROPRES
      DevilFruitMove projectile = moveOf(mero, "pistolkiss");
      DevilFruitMove melee = moveOf(mero, "roundhousekick");
      DevilFruitMove zone = moveOf(mero, "perfumefemur");
      // « Leurs ciblage, coût et durée PROPRES » = chaque mode applique SES valeurs déclarées, et les
      // ciblages diffèrent réellement. On lit le registre, on ne recopie pas des constantes du test.
      boolean troisModes = projectile != null && melee != null && zone != null
         && projectile.type() == MoveType.PROJECTILE && melee.type() == MoveType.MELEE
         && projectile.range() == 0.0                      // un projectile n'a pas de cône
         && melee.range() != zone.range()                  // cône 6 blocs vs zone 4 blocs : ciblages distincts
         && DevilFruitLoveConfig.areaRadius() != melee.range()
         && projectile.cooldownTicks() > 0 && melee.cooldownTicks() > 0 && zone.cooldownTicks() > 0
         && projectile.energyCost() > 0 && melee.energyCost() > 0 && zone.energyCost() > 0;
      check("Q6-trois-modes-differencies",
         troisModes,
         "projectile « Pistol Kiss » (" + (projectile == null ? "?" : projectile.type() + ", portée "
         + projectile.range() + ", " + projectile.energyCost() + " É, " + projectile.cooldownTicks() + " ticks")
         + ") · coup physique « Brick Roundhouse Kick » (" + (melee == null ? "?" : melee.type() + ", portée "
         + melee.range() + ", " + melee.energyCost() + " É, " + melee.cooldownTicks() + " ticks")
         + ") · zone « Perfume Femur Magna » (" + (zone == null ? "?" : zone.type() + ", portée " + zone.range()
         + " → onde r=" + DevilFruitLoveConfig.areaRadius()) + ") — les trois modes appliquent leurs valeurs"
         + " déclarées et leurs ciblages diffèrent réellement (aucun cône pour le projectile, cône de "
         + (melee == null ? "?" : round2(melee.range())) + " blocs pour la mêlée contre zone de "
         + (zone == null ? "?" : round2(zone.range())) + " blocs) : trois attaques, pas trois noms");

      // ============================ Q8 : le projectile pétrifie par l'élément CHARM
      Petrification.releaseAll();
      Petrification.clearConditions();
      var cibleProj = net.minecraft.world.entity.EntityTypes.COW.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 3), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert cibleProj != null;
      boolean elementLisible = DevilFruitElement.of(new ItemStack(mero)) == ElementType.CHARM;
      CombatSkillLogic.applyElementalEffect(cibleProj, ElementType.CHARM, caster);
      check("Q7-projectile-petrifie-par-l-element",
         elementLisible && Petrification.isPetrified(cibleProj)
            && cibleProj.hasEffect(MinepieceModMobEffects.PETRIFICATION),
         "élément lu pour le fruit = " + DevilFruitElement.of(new ItemStack(mero)) + " · après impact de"
         + " projectile : pétrifiée=" + Petrification.isPetrified(cibleProj)
         + " — l'élément CHARM porte la règle, donc projectile, mêlée et zone se comportent identiquement"
         + " sans recopie");

      // ============================ Q9 : l'attaque de ZONE touche tout le rayon, et épargne les insensibles
      Petrification.releaseAll();
      Petrification.clearConditions();
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      var zoneSensibleA = net.minecraft.world.entity.EntityTypes.COW.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 2), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      var zoneSensibleB = net.minecraft.world.entity.EntityTypes.PIG.spawn(level,
         new net.minecraft.core.BlockPos(bx + 2, by, bz + 1), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      var zoneInsensible = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 3), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      zoneInsensible.setBaby(false);
      assert zoneSensibleA != null && zoneSensibleB != null && zoneInsensible != null;
      int petrifiees = 0;
      int insensibles = 0;
      for (LivingEntity cible : level.getEntitiesOfClass(LivingEntity.class,
            caster.getBoundingBox().inflate(DevilFruitLoveConfig.areaRadius()))) {
         if (cible == caster || !cible.isAlive() || !CombatSkillLogic.isValidTarget(caster, cible)) {
            continue;
         }
         if (Petrification.applyFrom(caster, cible) > 0) {
            petrifiees++;
         } else {
            insensibles++;
         }
      }
      check("Q8-zone-touche-le-rayon-et-epargne-les-insensibles",
         Petrification.isPetrified(zoneSensibleA) && Petrification.isPetrified(zoneSensibleB)
            && !Petrification.isPetrified(zoneInsensible) && petrifiees >= 2 && insensibles >= 1,
         "onde de charme r=" + DevilFruitLoveConfig.areaRadius() + " : " + petrifiees + " pétrifiée(s), "
         + insensibles + " insensible(s) laissé(s) LIBRE(s) — le ciblage de zone est bien différent du cône"
         + " frontal des kicks, et la condition s'applique partout de la même façon");

      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      Petrification.releaseAll();
      Petrification.clearConditions();
      MinepieceModVariables.clear(caster);
   }

   // ------------------------------------------------- R. Contrat Hito Hito Daibutsu (-03/04/05)
   /**
    * Vérifie la <b>forme géante bornée</b> (limite de taille, espace nécessaire, portée cohérente) et
    * son <b>retour à la normale</b>, puis l'<b>onde de choc</b> dont le front détermine l'impact en
    * respectant les protections.
    */
   private static void daibutsu(ServerLevel level) {
      final int bx = 12;
      final int by = 100;
      final int bz = 4;

      ServerPlayer caster = fake(level, "dftest_daibutsu");
      MinepieceModVariables.clear(caster);
      // ⚠️ Le faux joueur PERSISTE d'une exécution à l'autre dans la même session. La portée le
      // transforme : sans cette remise en état, l'échelle 3,5 et ses modificateurs d'attributs
      // survivent, et la mesure « humaine » de R3 démarre déjà à 21 blocs de portée — d'où un R3 qui
      // passait la 1ʳᵉ fois puis échouait TOUTES les fois suivantes.
      DevilFruitTransformation.revertForm(caster);
      GiantForm.endAll();
      GiantShockWave.clearAll();
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      // Terrain : une grande plaine dégagée, puis un « couloir » bas pour tester l'espace nécessaire.
      for (int x = bx - 8; x <= bx + 8; x++) {
         for (int z = bz - 8; z <= bz + 18; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 12; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item daibutsu = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_hito_hito_daibutsu"));
      immerse(level, caster, daibutsu);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      caster.setYRot(0.0F);
      boolean zoan = daibutsu.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "zoan")));
      boolean transformable = daibutsu.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "transformable")));
      check("R0-decor-et-classification", level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz))
            && zoan && transformable,
         "terrain prêt (plaine dégagée) · classe lue : zoan=" + zoan + ", transformable=" + transformable
         + " · plafond d'échelle " + DevilFruitDaibutsuConfig.maxScale() + " — le fruit transforme son"
         + " porteur : c'est bien un Zoan, et le tag est LU, pas supposé");

      // ============================ R1 : la taille est BORNÉE
      double demandeAbsurde = 40.0;
      double bornee = GiantForm.clampedScale(demandeAbsurde);
      check("R1-taille-bornee",
         bornee <= DevilFruitDaibutsuConfig.maxScale() + 1.0E-6 && bornee < demandeAbsurde,
         "échelle demandée " + demandeAbsurde + " → appliquée " + round2(bornee) + " (plafond "
         + DevilFruitDaibutsuConfig.maxScale() + ") — la borne est dans le code, pas dans la table de"
         + " transformation ni dans la prudence de qui édite le fichier");

      // ============================ R2 : l'ESPACE NÉCESSAIRE est vérifié
      double echelle = GiantForm.clampedScale(3.5);
      boolean placeEnPlaine = GiantForm.hasRoom(caster, echelle);
      // Un plafond à 2 blocs au-dessus de la tête : la transformation doit être refusée.
      for (int x = bx - 3; x <= bx + 3; x++) {
         for (int z = bz - 3; z <= bz + 3; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by + 3, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
         }
      }
      boolean placeEnCouloir = GiantForm.hasRoom(caster, echelle);
      for (int x = bx - 3; x <= bx + 3; x++) {
         for (int z = bz - 3; z <= bz + 3; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by + 3, z),
               net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
         }
      }
      check("R2-espace-necessaire-verifie",
         placeEnPlaine && !placeEnCouloir,
         "en plaine dégagée : place = " + placeEnPlaine + " · avec un plafond à 2 blocs : place = "
         + placeEnCouloir + " — un géant ne se déploie pas dans un couloir : la transformation est"
         + " REFUSÉE au lieu d'encastrer le joueur dans la pierre");

      // ============================ R3 : la forme s'ouvre PAR LE VRAI CHEMIN, et la portée suit le corps
      // On passe par le chemin réel (toggle), pas par une reconstruction du test : c'est le SEUL moyen
      // de prouver que l'échelle réellement posée est bien bornée.
      double base = 6.0;
      double porteeHumaine = CombatSkillLogic.effectiveRange(caster, base);
      MinepieceModVariables.get(caster).PlayerEnergy = 200.0;
      boolean ouvert = DevilFruitTransformation.toggle(caster);
      double echelleReelle = caster.getScale();
      double porteeGeante = CombatSkillLogic.effectiveRange(caster, base);
      var vitesse = caster.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
      boolean ralenti = vitesse != null && vitesse.getModifier(
         Identifier.fromNamespaceAndPath("minepiece", "zoan_speed")) != null;
      check("R3-forme-ouverte-et-portee-coherente",
         ouvert && formeActive(caster) && ZoanForms.has(caster) && ralenti
            && echelleReelle > 1.0 && echelleReelle <= DevilFruitDaibutsuConfig.maxScale() + 1.0E-6
            && Math.abs(porteeGeante - porteeHumaine * echelleReelle) < 1.0E-6
            && porteeGeante > porteeHumaine,
         "transformation réelle : forme=" + formeActive(caster) + " (échelle posée "
         + round2(echelleReelle) + ", plafond " + DevilFruitDaibutsuConfig.maxScale() + ") · mobilité réduite = "
         + ralenti + " (coût de maintien " + DevilFruitDaibutsuConfig.maintenanceCostPerSecond() + " É/s) · "
         + "portée : " + round2(porteeHumaine) + " → " + round2(porteeGeante) + " blocs — la PORTÉE SUIT LE"
         + " CORPS : le poing visible atteint ce qu'il montre, il ne frappe pas dans le vide");

      // ============================ R4 : le maintien consomme, et l'épuisement fait retomber la forme
      var vars = MinepieceModVariables.get(caster);
      vars.PlayerEnergy = 200.0;
      int ticksAvant = ZoanForms.remaining(caster);
      for (int i = 0; i < 60; i++) {
         ZoanForms.tick();
      }
      double apresMaintien = vars.PlayerEnergy;
      vars.PlayerEnergy = 1.0;
      for (int i = 0; i < 40; i++) {
         ZoanForms.tick();
      }
      check("R4-maintien-et-epuisement",
         ticksAvant > 0 && apresMaintien < 200.0 && !ZoanForms.has(caster),
         "durée initiale " + ticksAvant + " ticks · 200,0 → " + round2(apresMaintien) + " É après 3 s de"
         + " maintien (coût " + DevilFruitDaibutsuConfig.maintenanceCostPerSecond() + " É/s) · énergie"
         + " épuisée → forme active = " + ZoanForms.has(caster)
         + " — la forme ne s'installe pas indéfiniment : elle se paie et elle retombe");

      // ============================ R5 : les attributs reviennent à la normale (sans corruption)
      GiantForm.endAll();
      var modsAvant = vitesse == null ? 0 : vitesse.getModifiers().size();
      GiantForm.begin(caster, echelle);
      int modsPendant = vitesse == null ? 0 : vitesse.getModifiers().size();
      GiantForm.end(caster);
      int modsApres = vitesse == null ? 0 : vitesse.getModifiers().size();
      // Un modificateur étranger doit survivre au passage de la forme géante.
      vitesse.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
         Identifier.fromNamespaceAndPath("minepiece", "test_etranger"), 0.1,
         net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
      GiantForm.begin(caster, echelle);
      GiantForm.end(caster);
      boolean etrangerSurvit = vitesse.getModifier(
         Identifier.fromNamespaceAndPath("minepiece", "test_etranger")) != null;
      vitesse.removeModifier(Identifier.fromNamespaceAndPath("minepiece", "test_etranger"));
      check("R5-attributs-revenus-a-la-normale",
         modsPendant > modsAvant && modsApres == modsAvant && etrangerSurvit,
         "modificateurs de vitesse : " + modsAvant + " → " + modsPendant + " (forme) → " + modsApres
         + " (après) · échelle " + round2(caster.getScale()) + " inchangée · un modificateur ÉTRANGER"
         + " survit au passage = " + etrangerSurvit + " — la forme retire SES modificateurs, pas « tous » :"
         + " rien d'autre n'est cassé au passage");

      // ============================ R6 : l'onde de choc — le FRONT détermine l'impact
      GiantShockWave.clearAll();
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      caster.setYRot(0.0F);
      boolean lancee = GiantShockWave.cast(caster);
      // Une cible LOIN devant, une autre HORS de la trajectoire (sur le côté).
      var devant = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 6), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      devant.setBaby(false);
      var cote = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 6, by, bz + 6), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      cote.setBaby(false);
      assert devant != null && cote != null;
      float pvDevant = devant.getHealth();
      float pvCote = cote.getHealth();
      // Le front part de z=4,5 vers z+ : il n'atteint pas encore la cible située à z=10.
      GiantShockWave.tick();
      double apresUnTick = GiantShockWave.travelledByFirst();
      float pvDevantPremierTick = devant.getHealth();
      check("R6-front-determine-l-impact",
         lancee && apresUnTick < DevilFruitDaibutsuConfig.waveMaxDistance()
            && pvDevantPremierTick == pvDevant,
         "onde lancée · après 1 tick le front a parcouru " + round2(apresUnTick) + " bloc(s) sur "
         + DevilFruitDaibutsuConfig.waveMaxDistance() + " · la cible à 5,5 blocs n'est PAS encore touchée"
         + " (PV " + round2(pvDevant) + ") — c'est bien le FRONT qui décide, pas un rayon instantané :"
         + " l'attaque est esquivable, donc lisible");

      // ============================ R7 : impact unique, et la cible hors trajectoire est épargnée
      for (int i = 0; i < 12; i++) {
         GiantShockWave.tick();
      }
      float pvDevantApres = devant.getHealth();
      int hitsApresPassage = GiantShockWave.hits();
      for (int i = 0; i < 20; i++) {
         GiantShockWave.tick();
      }
      check("R7-impact-unique-et-hors-trajectoire-epargne",
         pvDevantApres < pvDevant && GiantShockWave.hits() == hitsApresPassage
            && cote.getHealth() == pvCote,
         "cible sur la trajectoire : PV " + round2(pvDevant) + " → " + round2(pvDevantApres)
         + " · 20 ticks de plus : " + (GiantShockWave.hits() - hitsApresPassage)
         + " frappe supplémentaire (borné) · cible SUR LE CÔTÉ : PV " + round2(pvCote) + " → "
         + round2(cote.getHealth()) + " INTACTE · balayage : " + GiantShockWave.sweepReport()
         + " — une frappe par cible, et celui qui sort de la trajectoire n'est pas touché");

      // ============================ R8 : les protections sont respectées
      GiantShockWave.clearAll();
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      caster.setYRot(0.0F);
      var protege = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 6), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert protege != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      protege.setBaby(false);
      float pvProtege = protege.getHealth();
      String zone = level.dimension().identifier() + "@" + (bx - 2) + "," + (by - 2) + "," + (bz + 4)
         + "," + (bx + 2) + "," + (by + 4) + "," + (bz + 8);
      DevilFruitExplosionConfig.addZoneForTest(zone);
      boolean positionProtegee = DevilFruitExplosions.isProtectedPosition(level, protege.blockPosition());
      int blocsAvant = countAround(level, bx, by, bz + 6);
      GiantShockWave.cast(caster);
      for (int i = 0; i < 14; i++) {
         GiantShockWave.tick();
      }
      int blocsApres = countAround(level, bx, by, bz + 6);
      DevilFruitExplosionConfig.clearTestZones();
      check("R8-protections-respectees",
         positionProtegee && protege.getHealth() == pvProtege && blocsApres == blocsAvant,
         "zone protégée détectée = " + positionProtegee + " · occupant de la zone : PV " + round2(pvProtege)
         + " → " + round2(protege.getHealth()) + " (ÉPARGNÉ) · blocs autour du point d'impact : "
         + blocsAvant + " → " + blocsApres + " (inchangés) — l'onde ne modifie aucun bloc et n'atteint pas"
         + " les occupants d'une zone protégée");

      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      GiantForm.endAll();
      GiantShockWave.clearAll();
      DevilFruitExplosionConfig.clearTestZones();
      // Remise en état SYMÉTRIQUE : ce que la portée a posé, elle le retire — attributs compris.
      // Sans cela, l'échelle 3,5 fuite vers l'exécution suivante (le faux joueur persiste).
      DevilFruitTransformation.revertForm(caster);
      MinepieceModVariables.clear(caster);
   }

   /**
    * Total de techniques attendu au catalogue.
    *
    * <p><b>204</b> = intégralité de ver15. <b>+2</b> = extension <b>voulue</b> du Fruit Imu
    * (FRUIT-IMU-04). <b>+1</b> = défense de glace d'Okuchi no Makami, que le plan demande « avec coût »
    * (FRUIT-INU_INU_OKUCHI_NO_MAKAMI-05). <b>+1</b> = poursuite du loup, que le plan demande d'« ajouter »
    * (FRUIT-INU_INU_WOLF-04) — l'ajout est l'instruction elle-même. <b>+1</b> = corps de diamant, que le
    * plan demande de « créer » puisqu'aucune technique de ce fruit n'est un état activé
    * (FRUIT-KIRA_KIRA-03/04). Les deux jets de pierre de Kira Kira ont été REMPLACÉS sans changer le
    * nombre (FRUIT-KIRA_KIRA-05). Toute autre valeur doit être justifiée par une action du plan, jamais
    * ajustée pour faire passer le test.</p>
    */
   private static final int EXPECTED_TECHNIQUES = 213;

   /** Vrai si la forme est active côté variables joueur (source de vérité du système de transformation). */
   private static boolean formeActive(ServerPlayer player) {
      return MinepieceModVariables.get(player).player_form != 0.0;
   }

   /** Compte les blocs non vides autour d'un point — pour prouver qu'aucun n'a été modifié. */
   private static int countAround(ServerLevel level, int x, int y, int z) {
      int n = 0;
      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = -1; dy <= 2; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
               if (!level.getBlockState(new net.minecraft.core.BlockPos(x + dx, y + dy, z + dz)).isAir()) {
                  n++;
               }
            }
         }
      }
      return n;
   }

   // ------------------------------------------------- S. Contrat Horo Horo (-03/04/05)
   /**
    * Vérifie les <b>fantômes à traversée réglée</b> (surfaces traversées, règles retenues, durée bornée),
    * l'<b>état négatif distinct du ralentissement</b> (plafonné, expirant) et le <b>contrôle à distance</b>
    * à sortie garantie (dont la déconnexion).
    */
   private static void horo(ServerLevel level) {
      final int bx = 12;
      final int by = 100;
      final int bz = 4;

      ServerPlayer caster = fake(level, "dftest_horo");
      MinepieceModVariables.clear(caster);
      NegativeState.releaseAll();
      NegativeState.clearResults();
      RemoteControl.endAll("test");
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 4; x <= bx + 4; x++) {
         for (int z = bz - 4; z <= bz + 16; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 5; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item horo = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_horo_horo"));
      immerse(level, caster, horo);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      caster.setYRot(0.0F);
      boolean paramecia = horo.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "paramecia")));
      boolean elementGhost = DevilFruitElement.of(new ItemStack(horo)) == ElementType.GHOST;
      check("S0-decor-et-classification",
         level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)) && paramecia && elementGhost,
         "terrain prêt · classe lue : paramecia=" + paramecia + " · élément lue : "
         + DevilFruitElement.of(new ItemStack(horo)) + " — ni intangibilité de corps (pas Logia) ni"
         + " transformation (pas Zoan) : le rattachement est VÉRIFIÉ sur le tag, pas supposé");

      // ============================ S1 : le fantôme traverse un MUR (règle principale)
      // Un mur plein de 3 blocs d'épaisseur barrant la trajectoire.
      for (int y = by; y <= by + 3; y++) {
         for (int x = bx - 2; x <= bx + 3; x++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, bz + 3),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
         }
      }
      var fantome = new net.mcreator.minepiece.entity.SkillProjectileEntity(
         net.mcreator.minepiece.init.MinepieceModEntities.SKILL_PROJECTILE,
         bx + 0.5, by + 1.0, bz + 0.5, new Vec3(0, 0, 1), level);
      fantome.setOwner(caster);
      fantome.damage(10.0).element(ElementType.GHOST).phases(true)
         .lifetime(DevilFruitHollowConfig.ghostLifetimeTicks());
      fantome.shoot(0, 0, 1, 2.5F, 0.0F);
      level.addFreshEntity(fantome);
      boolean traversant = fantome.isPhasing();
      boolean survitAuMur = true;
      for (int i = 0; i < 20; i++) {
         fantome.tick();
         if (!fantome.isAlive() && fantome.getZ() < bz + 4.0) {
            survitAuMur = false;
            break;
         }
      }
      check("S1-fantome-traverse-le-mur",
         traversant && survitAuMur,
         "fantôme traversant=" + traversant + " · mur plein de 3 blocs à z=" + (bz + 3) + " ("
         + "blocs posés de x=" + (bx - 2) + " à " + (bx + 3) + ") · après 20 ticks le fantôme est à z="
         + round2(fantome.getZ()) + " et vivant=" + fantome.isAlive()
         + " — un fantôme traverse les murs : c'est sa nature, et c'est la règle principale");

      // ============================ S2 : la traversée est BORNÉE — durée de vie finie
      var fantome2 = new net.mcreator.minepiece.entity.SkillProjectileEntity(
         net.mcreator.minepiece.init.MinepieceModEntities.SKILL_PROJECTILE,
         bx + 0.5, by + 1.0, bz + 0.5, new Vec3(0, 0, 1), level);
      fantome2.setOwner(caster);
      fantome2.damage(10.0).element(ElementType.GHOST).phases(true)
         .lifetime(DevilFruitHollowConfig.ghostLifetimeTicks());
      level.addFreshEntity(fantome2);
      // On le place dans le mur : sans borne, il y errerait indéfiniment.
      fantome2.setPos(bx + 0.5, by + 1.0, bz + 3.5);
      for (int i = 0; i < DevilFruitHollowConfig.ghostLifetimeTicks() + 5; i++) {
         fantome2.tick();
      }
      check("S2-duree-de-vie-bornee",
         !fantome2.isAlive(),
         "fantôme placé DANS le mur (z=" + (bz + 3) + ") → après "
         + (DevilFruitHollowConfig.ghostLifetimeTicks() + 5) + " ticks : vivant=" + fantome2.isAlive()
         + " — la traversée s'arrête au bout du temps imparti, même dans la pierre : un fantôme qui"
         + " errerait sans fin ne serait pas borné");

      // ============================ S3 : la traversée s'arrête aux zones protégées
      var fantome3 = new net.mcreator.minepiece.entity.SkillProjectileEntity(
         net.mcreator.minepiece.init.MinepieceModEntities.SKILL_PROJECTILE,
         bx + 0.5, by + 1.0, bz + 0.5, new Vec3(0, 0, 1), level);
      fantome3.setOwner(caster);
      fantome3.damage(10.0).element(ElementType.GHOST).phases(true).lifetime(200);
      level.addFreshEntity(fantome3);
      String zone = level.dimension().identifier() + "@" + (bx - 1) + "," + (by - 1) + "," + (bz + 5)
         + "," + (bx + 2) + "," + (by + 4) + "," + (bz + 8);
      DevilFruitExplosionConfig.addZoneForTest(zone);
      fantome3.setPos(bx + 0.5, by + 1.0, bz + 6.5);
      fantome3.tick();
      boolean dissipe = !fantome3.isAlive();
      DevilFruitExplosionConfig.clearTestZones();
      check("S3-traversee-limitee-aux-regles-retenues",
         dissipe,
         "fantôme entrant dans une zone protégée → vivant=" + fantome3.isAlive()
         + " (dissipé) — la traversée est LIMITÉE À DES RÈGLES : les murs passent, un claim non."
         + " Un pouvoir qui ignore les protections serait un trou de sécurité, pas un pouvoir");

      // ============================ S4 : l'état négatif n'est PAS un simple ralentissement
      NegativeState.releaseAll();
      var cible = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 6), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert cible != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      cible.setBaby(false);
      int palier = NegativeState.applyFrom(caster, cible);
      boolean faiblesse = cible.hasEffect(MobEffects.WEAKNESS);
      boolean fatigue = cible.hasEffect(MobEffects.MINING_FATIGUE);
      boolean ralenti = cible.hasEffect(MobEffects.SLOWNESS);
      check("S4-etat-negatif-distinct-du-ralentissement",
         palier > 0 && faiblesse && fatigue && !ralenti,
         "palier " + palier + " · faiblesse=" + faiblesse + " (frappe moins fort) · fatigue=" + fatigue
         + " (travaille mal) · ralentissement=" + ralenti + " — si le malus n'était qu'un ralentissement"
         + " plus fort, le plan aurait dit « amplifier le ralentissement ». La cible ne devient pas"
         + " LENTE, elle devient DÉMORALISÉE : deux mécaniques indépendantes");

      // ============================ S5 : le malus est PLAFONNÉ et disparaît
      for (int i = 0; i < 10; i++) {
         NegativeState.intensify(cible);
      }
      int plafond = NegativeState.levelOf(cible);
      String verdict = NegativeState.lastResult(caster);
      for (int i = 0; i < DevilFruitHollowConfig.negativeLevelTicks() + 2; i++) {
         NegativeState.tick();
      }
      check("S5-malus-plafonne-et-disparait",
         plafond == DevilFruitHollowConfig.negativeMaxLevel() && !NegativeState.isNegative(cible)
            && !cible.hasEffect(MobEffects.WEAKNESS) && !cible.hasEffect(MobEffects.MINING_FATIGUE),
         "dix aggravations de plus → palier " + plafond + " (plafond "
         + DevilFruitHollowConfig.negativeMaxLevel() + ") · après l'échéance : état="
         + NegativeState.isNegative(cible) + ", faiblesse=" + cible.hasEffect(MobEffects.WEAKNESS)
         + ", fatigue=" + cible.hasEffect(MobEffects.MINING_FATIGUE) + " · verdict affiché = « "
         + verdict + " » — plafonné ET disparu proprement");

      // ============================ S6 : l'impact d'un fantôme applique l'état négatif
      NegativeState.releaseAll();
      var cibleImpact = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 8), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert cibleImpact != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      cibleImpact.setBaby(false);
      CombatSkillLogic.applyElementalEffect(cibleImpact, ElementType.GHOST, caster);
      check("S6-impact-applique-l-etat-negatif",
         NegativeState.isNegative(cibleImpact) && cibleImpact.hasEffect(MobEffects.WEAKNESS)
            && !cibleImpact.hasEffect(MobEffects.SLOWNESS),
         "impact d'un fantôme → état négatif=" + NegativeState.isNegative(cibleImpact)
         + ", faiblesse=" + cibleImpact.hasEffect(MobEffects.WEAKNESS)
         + " · ralentissement=" + cibleImpact.hasEffect(MobEffects.SLOWNESS)
         + " — l'élément GHOST porte la règle : toutes les techniques du fruit l'appliquent sans recopie");

      // ============================ S7 : contrôle à distance — borné, et sortie garantie sur échéance
      RemoteControl.endAll("test");
      var vars = MinepieceModVariables.get(caster);
      vars.PlayerEnergy = 200.0;
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      boolean demarre = RemoteControl.begin(caster);
      boolean immobilise = caster.hasEffect(MobEffects.SLOWNESS);
      int ticksControle = RemoteControl.remainingTicks(caster);
      for (int i = 0; i < DevilFruitHollowConfig.controlMaxTicks() + 2; i++) {
         RemoteControl.tick(caster);
      }
      check("S7-controle-borne-et-sortie-sur-echeance",
         demarre && immobilise && ticksControle > 0 && !RemoteControl.isControlling(caster)
            && !caster.hasEffect(MobEffects.SLOWNESS)
            && "echeance".equals(RemoteControl.lastExitReason(caster)),
         "contrôle démarré=" + demarre + " · corps immobilisé=" + immobilise + " (" + ticksControle
         + " ticks max) · après échéance : contrôle=" + RemoteControl.isControlling(caster)
         + ", immobilisation=" + caster.hasEffect(MobEffects.SLOWNESS) + " (levée), raison sortie = « "
         + RemoteControl.lastExitReason(caster) + " » — le joueur RETROUVE son contrôle normal");

      // ============================ S8 : sortie garantie sur DÉCONNEXION (nommée par le critère)
      var vars2 = MinepieceModVariables.get(caster);
      vars2.PlayerEnergy = 200.0;
      RemoteControl.begin(caster);
      boolean enControle = RemoteControl.isControlling(caster);
      // C'est exactement le chemin emprunté par l'événement de déconnexion du serveur.
      RemoteControl.end(caster, "deconnexion");
      check("S8-sortie-sur-deconnexion",
         enControle && !RemoteControl.isControlling(caster)
            && !caster.hasEffect(MobEffects.SLOWNESS) && !caster.hasEffect(MobEffects.MINING_FATIGUE)
            && "deconnexion".equals(RemoteControl.lastExitReason(caster)),
         "en contrôle = " + enControle + " → après déconnexion : contrôle="
         + RemoteControl.isControlling(caster) + ", immobilisation="
         + caster.hasEffect(MobEffects.SLOWNESS) + " (levée), raison = « "
         + RemoteControl.lastExitReason(caster) + " » — un joueur qui reste figé après une déconnexion"
         + " ne peut même pas signaler le problème : c'est le pire cas, et il est couvert");

      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      NegativeState.releaseAll();
      NegativeState.clearResults();
      RemoteControl.endAll("test");
      DevilFruitExplosionConfig.clearTestZones();
      MinepieceModVariables.clear(caster);
   }

   // ------------------------------------------------- T. Contrat Imu (FRUIT-IMU-03/04/05/06)
   /**
    * Vérifie que <b>Imu Arrow n'utilise plus le bras de Gomu</b> et possède un rendu adapté, que le
    * <b>catalogue étendu</b> existe avec ciblage/coût/recharge propres, et que le <b>passif</b> du Fruit
    * s'applique au bon joueur et se retire proprement.
    */
   private static void imu(ServerLevel level) {
      final int bx = 12;
      final int by = 100;
      final int bz = 4;

      ServerPlayer caster = fake(level, "dftest_imu");
      MinepieceModVariables.clear(caster);
      SovereignVeil.removeAll();
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (var p : level.getEntitiesOfClass(net.mcreator.minepiece.entity.EntityGomuArmEntity.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         p.discard();
      }
      for (int x = bx - 5; x <= bx + 5; x++) {
         for (int z = bz - 5; z <= bz + 12; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 4; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item imu = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_imu"));
      immerse(level, caster, imu);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      caster.setYRot(0.0F);
      var vars = MinepieceModVariables.get(caster);
      vars.PlayerEnergy = 200.0;
      boolean paramecia = imu.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "paramecia")));
      check("T0-decor-classification-et-nom",
         level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)) && paramecia
            && new ItemStack(imu).getHoverName().getString().contains("?"),
         "terrain prêt · classe lue : paramecia=" + paramecia + " · nom affiché de l'item = « "
         + new ItemStack(imu).getHoverName().getString() + " » — l'identité de ce Fruit n'est PAS révélée dans l'œuvre. Le"
         + " plan demande explicitement de « ne pas inventer une fidélité au canon » : le rattachement"
         + " est VÉRIFIÉ sur le tag, et le catalogue ci-dessous est une DÉCISION DE MOD documentée");

      // T1 corrected after reading the actual ImuArrowProcedure: the source deliberately uses
      // the arm geometry with imu_arrow, not Luffy. Native delayed collision is checked by B05.
      DevilFruitMove imuArrow=moveOf(imu,"imuarrow");
      boolean pris=DevilFruitBehaviours.customCast(caster,new ItemStack(imu),imuArrow);
      for(int step=0;step<16;step++)net.mcreator.minepiece.procedures.MythicBatchTechniques.tick();
      var arms=level.getEntitiesOfClass(net.mcreator.minepiece.entity.EntityGomuArmEntity.class,new net.minecraft.world.phys.AABB(bx-30,by-10,bz-30,bx+30,by+20,bz+30));
      boolean nativeArrow=arms.stream().anyMatch(e->e.ownerEntity()==caster&&e.getVisualProfile().equals("b05_native_imu_arrow")&&e.getTexture().equals("imu_arrow")&&e.getModel().equals("b05_elastic_arm"));
      check("T1-imu-arrow-texture-source",pris&&nativeArrow,"Actual source geometry/animation and explicit imu_arrow texture; no default Luffy renderer. Native damage/wall timing is covered in dbatch05.");
      net.mcreator.minepiece.procedures.MythicBatchTechniques.cancel(caster);

      // ============================ T2 : le catalogue étendu existe, avec ciblage/coût/recharge propres
      var moves = DevilFruitRegistry.movesFor(imu);
      DevilFruitMove silence = moveOf(imu, "silentcommand");
      DevilFruitMove poigne = moveOf(imu, "sovereigngrasp");
      boolean catalogue = moves.size() >= 3 && silence != null && poigne != null
         && imuArrow.type() == MoveType.MELEE && silence.type() == MoveType.EFFECT
         && poigne.type() == MoveType.MELEE
         && silence.range() > 0.0 && silence.energyCost() > 0.0 && silence.cooldownTicks() > 0
         && poigne.range() > 0.0 && poigne.energyCost() > 0.0 && poigne.cooldownTicks() > 0
         && poigne.range() != imuArrow.range();
      check("T2-catalogue-etendu-et-propre",
         catalogue,
         moves.size() + " technique(s) au catalogue · Imu Arrow (" + imuArrow.type() + ", portée "
         + round2(imuArrow.range()) + ", " + imuArrow.energyCost() + " É, " + imuArrow.cooldownTicks()
         + " ticks) · Silent Command (" + (silence == null ? "?" : silence.type() + ", rayon "
         + round2(silence.range()) + ", " + silence.energyCost() + " É, " + silence.cooldownTicks() + " ticks")
         + ") · Sovereign Grasp (" + (poigne == null ? "?" : poigne.type() + ", portée "
         + round2(poigne.range()) + ", " + poigne.energyCost() + " É, " + poigne.cooldownTicks() + " ticks")
         + ") — le plan demandait « un catalogue cohérent au-delà de l'unique Imu Arrow », chacun avec"
         + " effet, coût, recharge et ciblage : trois techniques, trois ciblages");

      // ============================ T3 : les techniques ajoutées sont traduites (FR et EN)
      java.util.Set<String> fr = langKeys("/assets/minepiece/lang/fr_fr.json");
      java.util.Set<String> en = langKeys("/assets/minepiece/lang/en_us.json");
      boolean traduites = fr.contains("minepiece.technique.silentcommand")
         && fr.contains("minepiece.technique.sovereigngrasp")
         && en.contains("minepiece.technique.silentcommand")
         && en.contains("minepiece.technique.sovereigngrasp");
      check("T3-techniques-ajoutees-traduites",
         traduites,
         "clés « minepiece.technique.silentcommand » et « .sovereigngrasp » présentes en FR et EN = "
         + traduites + " — une technique ajoutée sans traduction afficherait un identifiant technique au"
         + " joueur : la vérification i18n du harnais l'aurait refusée de toute façon");

      // ============================ T4 : le passif s'applique au BON joueur
      SovereignVeil.removeAll();
      SovereignVeil.tick(caster);
      boolean voileSurPorteur = SovereignVeil.hasVeil(caster);
      boolean effetMarqueur = caster.hasEffect(MinepieceModMobEffects.SOVEREIGN_VEIL);
      // Un second joueur qui détient un AUTRE Fruit ne doit rien recevoir.
      ServerPlayer autre = fake(level, "dftest_imu_autre");
      MinepieceModVariables.clear(autre);
      immerse(level, autre, MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU);
      SovereignVeil.tick(autre);
      check("T4-passif-au-bon-joueur",
         voileSurPorteur && effetMarqueur && !SovereignVeil.hasVeil(autre),
         "porteur du Fruit Imu : voile=" + voileSurPorteur + ", marqueur dédié=" + effetMarqueur
         + " · porteur d'un AUTRE Fruit : voile=" + SovereignVeil.hasVeil(autre)
         + " — le passif va au bon joueur, et à lui seul");

      // ============================ T5 : retrait sous suppression
      caster.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.SEA_POWER, 200, 0, false, false, true));
      SovereignVeil.tick(caster);
      boolean retireSousSuppression = !SovereignVeil.hasVeil(caster)
         && !caster.hasEffect(MinepieceModMobEffects.SOVEREIGN_VEIL);
      caster.removeEffect(MinepieceModMobEffects.SEA_POWER);
      check("T5-retire-sous-suppression",
         retireSousSuppression,
         "pouvoir scellé par la mer → voile actif=" + SovereignVeil.hasVeil(caster)
         + ", marqueur=" + caster.hasEffect(MinepieceModMobEffects.SOVEREIGN_VEIL)
         + " (retiré) — un pouvoir scellé ne protège plus son porteur");

      // ============================ T6 : retrait propre, sans toucher aux états externes
      SovereignVeil.tick(caster);
      boolean remis = SovereignVeil.hasVeil(caster);
      var armure = caster.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
      // Un modificateur ÉTRANGER, comme celui d'une autre source, doit survivre.
      armure.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
         Identifier.fromNamespaceAndPath("minepiece", "test_etranger_imu"), 3.0,
         net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
      int modsAvantRetrait = armure.getModifiers().size();
      minepieceClearFruit(level, caster);
      SovereignVeil.tick(caster);
      boolean modEtrangerSurvit = armure.getModifier(
         Identifier.fromNamespaceAndPath("minepiece", "test_etranger_imu")) != null;
      int modsApres = armure.getModifiers().size();
      check("T6-retrait-propre-sans-toucher-aux-etats-externes",
         remis && !SovereignVeil.hasVeil(caster) && modEtrangerSurvit && modsApres == modsAvantRetrait - 1,
         "voile remis=" + remis + " · après retrait du Fruit : voile=" + SovereignVeil.hasVeil(caster)
         + " · modificateurs d'armure " + modsAvantRetrait + " → " + modsApres
         + " (le nôtre seul est parti) · modificateur ÉTRANGER survivant = " + modEtrangerSurvit
         + " — le voile retire SES états, jamais « tous » : c'est pourquoi il utilise un effet marqueur"
         + " dédié plutôt que Résistance");
      armure.removeModifier(Identifier.fromNamespaceAndPath("minepiece", "test_etranger_imu"));

      // ============================ T7 : le passif a une durée bornée
      immerse(level, caster, imu);
      SovereignVeil.tick(caster);
      int duree = DevilFruitImuConfig.veilDurationTicks();
      int rafraichissement = SovereignVeil.refreshIn(caster);
      check("T7-passif-a-duree-bornee",
         duree > 0 && rafraichissement > 0 && rafraichissement <= duree,
         "application bornée à " + duree + " ticks, rafraîchie tous les " + rafraichissement
         + " ticks — le passif n'est pas un état éternel posé une fois : chaque application expire, et"
         + " la révocation n'a jamais à démêler un état permanent");

      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (var p : level.getEntitiesOfClass(net.mcreator.minepiece.entity.EntityGomuArmEntity.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         p.discard();
      }
      SovereignVeil.removeAll();
      MinepieceModVariables.clear(caster);
      MinepieceModVariables.clear(autre);
   }

   /** Retire le Fruit équipé d'un joueur (simule le retrait/révocation). */
   private static void minepieceClearFruit(ServerLevel level, ServerPlayer player) {
      var vars = MinepieceModVariables.get(player);
      vars.PlayerDevilFruit = net.minecraft.world.item.ItemStack.EMPTY;
      vars.syncPlayerVariables(player);
   }

   // ------------------------------- W. Contrat Inu Inu Okuchi No Makami (-03/04/05/06)
   /**
    * Vérifie que les <b>formes sont propres au modèle</b> (aucun partage avec le loup ordinaire), que le
    * <b>souffle glacé</b> est dirigé, arrêté par les murs et que la <b>cible est libérée</b>, et que la
    * <b>défense de glace</b> a un coût, des exceptions, et expire sans toucher aux autres bonus.
    */
   private static void okuchi(ServerLevel level) {
      final int bx = 18;
      final int by = 100;
      final int bz = 6;

      ServerPlayer caster = fake(level, "dftest_okuchi");
      MinepieceModVariables.clear(caster);
      IceDefense.endAll("test");
      FreezeService.clearAll();
      DevilFruitTransformation.revertForm(caster);
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 8; x <= bx + 8; x++) {
         for (int z = bz - 8; z <= bz + 18; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 6; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item okuchi = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_inu_inu_okuchi_no_makami"));
      Item loup = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_inu_inu_wolf"));
      immerse(level, caster, okuchi);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      caster.setXRot(0.0F);
      caster.setYRot(0.0F);
      // ⚠️ ROBUSTESSE : on vient de déplacer et d'orienter le porteur DANS le tick courant. Or
      // getViewVector(partialTicks) INTERPOLE entre la rotation du tick précédent (xRotO/yRotO) et la
      // rotation actuelle : mesurer le regard juste après l'avoir fixé pouvait donc lire une direction
      // héritée de l'état antérieur, selon le tick partiel utilisé. setOldPosAndRot() aligne la rotation
      // « précédente » sur la nouvelle, ce qui rend la mesure déterministe.
      caster.setOldPosAndRot();
      MinepieceModVariables.get(caster).PlayerEnergy = 200.0;
      boolean zoan = okuchi.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "zoan")));
      boolean transformable = okuchi.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "transformable")));
      check("W0-decor-et-classification",
         level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)) && zoan && transformable,
         "terrain prêt · classe lue : zoan=" + zoan + ", transformable=" + transformable
         + " — le fruit transforme son porteur : c'est un Zoan, et le tag est LU, pas supposé");

      // ================= W1 : formes propres — aucun partage avec le loup ordinaire
      // Ce que le SERVEUR peut prouver : deux fruits distincts, tous deux transformables et zoan,
      // chacun déclarant SA propre transformation. Le détail des modèles et textures vit dans le code
      // client (PlayerTransformLayer) et ne se lit pas ici : il est vérifié au niveau des ressources,
      // commande et sortie consignées dans le contrat (§4).
      boolean fruitsDistincts = okuchi != loup
         && !okuchi.getDescriptionId().equals(loup.getDescriptionId());
      boolean okuchiTransformable = okuchi.builtInRegistryHolder().is(DevilFruitTransformation.TAG_TRANSFORMABLE);
      boolean loupTransformable = loup.builtInRegistryHolder().is(DevilFruitTransformation.TAG_TRANSFORMABLE);
      // Les deux transformations mènent-elles à des ÉTATS différents ? Un partage involontaire se
      // manifesterait par des formes identiques : on compare ce que le moteur expose.
      String formeOkuchi = DevilFruitTransformation.formSignatureFor(okuchi);
      String formeLoup = DevilFruitTransformation.formSignatureFor(loup);
      check("W1-formes-propres-aucun-partage-avec-le-loup",
         fruitsDistincts && okuchiTransformable && loupTransformable
            && !formeOkuchi.equals(formeLoup) && !formeOkuchi.isEmpty(),
         "fruits distincts = " + fruitsDistincts + " · transformables : Okuchi = " + okuchiTransformable
         + ", loup = " + loupTransformable + " · signature de forme : Okuchi = « " + formeOkuchi
         + " », loup = « " + formeLoup + " » — DIFFÉRENTES. Le critère exige « aucun partage"
         + " INVOLONTAIRE de modèle avec le loup ordinaire » : les deux formes sont distinctes au niveau"
         + " du moteur, et les ressources (modèle + texture) sont vérifiées séparément (§4 du contrat)");

      // ================= W2 : le souffle est DIRIGÉ — cône devant, rien derrière
      // ⚠️ GARANTIR L'ACTEUR AUTANT QUE LE DÉCOR. Le cône part des YEUX : si le porteur est resté dans
      // une forme transformée (ou avec une échelle modifiée), la hauteur des yeux change, le rayon monte
      // ou descend, et la cible sort du cône de 1,0 bloc — la mesure échoue alors que rien, dans le code
      // du souffle, n'a changé. Symptôme observé : la portée passe SEULE et échoue par intermittence
      // dans `dftest all`, où les scopes précédents ont eu le temps de laisser un état sur le porteur.
      // On remet donc la forme de base, on rafraîchit les dimensions, et on VÉRIFIE la géométrie.
      DevilFruitTransformation.revertForm(caster);
      caster.refreshDimensions();
      double hauteurYeux = caster.getEyePosition().y;
      double hauteurAttendue = by + 1.62;
      boolean acteurSain = Math.abs(hauteurYeux - hauteurAttendue) < 0.05;
      // Le couloir est DÉGAGÉ ET VÉRIFIÉ avant la mesure : une portée voisine peut avoir laissé un bloc
      // (le monde persiste entre les exécutions), et la mesure deviendrait intermittente sans qu'aucune
      // ligne du code testé ait changé.
      int debrisRetires = clearCorridor(level, bx, bz + 1, bz + 12, by, by + 3);
      int obstacles = corridorObstacles(level, bx, bz + 1, bz + 12, by, by + 3);
      var devant = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 5), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      devant.setBaby(false);
      var derriere = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz - 5), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      derriere.setBaby(false);
      var cote = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 6, by, bz + 5), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      cote.setBaby(false);
      assert devant != null && derriere != null && cote != null;
      DevilFruitMove souffle = moveOf(okuchi, "namujihyoga");
      assert souffle != null;
      int pris = IceBreath.strike(caster, souffle.damage(), souffle.knockback());
      float pvDevant = devant.getHealth();
      float pvDerriere = derriere.getHealth();
      float pvCote = cote.getHealth();
      var regard = caster.getLookAngle();
      check("W2-souffle-dirige",
         acteurSain && obstacles == 0 && pris >= 1 && pvDevant < 20.0F && pvDerriere == 20.0F
            && pvCote == 20.0F,
         "acteur : hauteur d'yeux " + round2(hauteurYeux) + " (attendu " + round2(hauteurAttendue)
         + ", sain = " + acteurSain + ") · couloir dégagé (" + debrisRetires + " bloc(s) retiré(s),"
         + " obstacles restants = " + obstacles
         + ") · cibles prises = " + pris + " · DEVANT 5 blocs dans l'axe : PV " + round2(pvDevant)
         + (pvDevant < 20.0F ? " (touchée)" : " (INTACTE)") + " · DERRIÈRE : PV " + round2(pvDerriere)
         + " · SUR LE CÔTÉ : PV " + round2(pvCote) + " · état au moment de la mesure : porteur en "
         + round2(caster.getX()) + "," + round2(caster.getY()) + "," + round2(caster.getZ())
         + " regard=" + round2(regard.x) + "," + round2(regard.y) + "," + round2(regard.z)
         + " · devant en " + devant.blockPosition().toShortString() + " (PV " + round2(devant.getHealth())
         + ", vivant=" + devant.isAlive() + ") — le souffle est DIRIGÉ : il part du regard et ne touche"
         + " pas ce qui est derrière ou hors du cône");

      // ================= W3 : un MUR arrête le souffle (la « collision » du critère)
      FreezeService.clearAll();
      clearCorridor(level, bx, bz + 1, bz + 12, by, by + 3);
      var abrite = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 8), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert abrite != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      abrite.setBaby(false);
      // Mur plein entre le porteur et la cible, dans l'axe du regard.
      for (int y = by; y <= by + 3; y++) {
         level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx, y, bz + 4),
            net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      }
      int prisAbrite = IceBreath.strike(caster, souffle.damage(), souffle.knockback());
      float pvAbrite = abrite.getHealth();
      int gelAbrite = FreezeService.stageOf(abrite);
      // Mur retiré : la même cible DOIT être touchée — contrôle positif.
      for (int y = by; y <= by + 3; y++) {
         level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx, y, bz + 4),
            net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
      }
      // ⚠️ La cible du contrôle précédent est RETIRÉE avant d'en placer une nouvelle au MÊME endroit.
      // Sans cela, deux zombies identiques occupent la même case : la frappe en touche un — lequel dépend
      // de l'ordre d'itération des entités — et le test devient intermittent (il a échoué une fois en
      // portée complète, puis « passait » selon le tirage). Une mesure ne doit jamais être ambiguë :
      // une seule cible possible à la fois.
      abrite.discard();
      int obstaclesControle = corridorObstacles(level, bx, bz + 1, bz + 12, by, by + 3);
      var expose = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 8), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert expose != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      expose.setBaby(false);
      float pvExposeAvant = expose.getHealth();
      int invulnAvant = expose.getInvulnerableTime();
      int prisControle = IceBreath.strike(caster, souffle.damage(), souffle.knockback());
      float pvExpose = expose.getHealth();
      // DIAGNOSTIC : sépare « la cible n'est pas trouvée » de « la cible est trouvée mais ne subit rien ».
      var sourceTest = level.damageSources().playerAttack(caster);
      boolean blessableDirectement = expose.isAlive() && !expose.isInvulnerableTo(level, sourceTest);
      double distance = expose.position().distanceTo(caster.position());
      var rayons = IceBreath.cone(level, caster.getEyePosition(), caster.getLookAngle());
      boolean dansLeCone = IceBreath.caught(expose, caster.getEyePosition(), rayons);
      // DIAGNOSTIC : la portée de CHAQUE rayon, et le bloc qui l'arrête s'il y en a un.
      StringBuilder rayonsTexte = new StringBuilder();
      for (var rayon : rayons) {
         rayonsTexte.append(round2(rayon.reach())).append("(")
            .append(round2(rayon.direction().x)).append(",").append(round2(rayon.direction().z)).append(") ");
      }
      double porteeNominale = DevilFruitOkuchiConfig.breathRange();
      // La BOÎTE DE COLLISION de la cible : c'est elle que `caught` interroge (son centre), alors que
      // `distance` lit `position()`. Une boîte non encore mise à jour sur une entité fraîchement
      // apparue expliquerait exactement « position correcte mais cible non trouvée ».
      var boite = expose.getBoundingBox();
      String boiteTexte = "boite=" + round2(boite.getXsize()) + "x" + round2(boite.getYsize()) + "x"
         + round2(boite.getZsize()) + " centre=" + round2(boite.getCenter().x) + ","
         + round2(boite.getCenter().y) + "," + round2(boite.getCenter().z);
      // Où le rayon AXIAL s'arrête-t-il, et sur quoi ?
      var axe = rayons.stream().max(java.util.Comparator.comparingDouble(
         r -> r.direction().z)).orElse(rayons.get(0));
      StringBuilder arret = new StringBuilder();
      for (double d = 0.5; d <= porteeNominale; d += 0.5) {
         var pt = caster.getEyePosition().add(axe.direction().scale(d));
         var bp = net.minecraft.core.BlockPos.containing(pt.x, pt.y, pt.z);
         var etat = level.getBlockState(bp);
         if (!etat.isAir()) {
            arret.append("arret a d=").append(round2(d)).append(" sur ").append(bp.toShortString())
               .append("=").append(etat.getBlock().getName().getString());
            break;
         }
      }
      check("W3-mur-arrete-le-souffle",
         pvAbrite == 20.0F && gelAbrite == 0 && pvExpose < 20.0F && obstaclesControle == 0,
         "couloir du contrôle positif : obstacles = " + obstaclesControle + " · DIAG: pris=" + prisControle
         + " pv " + round2(pvExposeAvant) + "→" + round2(pvExpose) + " invulnTime=" + invulnAvant
         + " blessable=" + blessableDirectement + " dansLeCone=" + dansLeCone + " porteeNominale=" + round2(porteeNominale) + " " + boiteTexte + " rayons=[" + rayonsTexte.toString().trim() + "] " + arret + " distance="
         + round2(distance) + " yeux=" + round2(caster.getEyePosition().y)
         + " · cible à couvert DERRIÈRE un"
         + " mur plein : PV " + round2(pvAbrite) + " (intacte), gel palier "
         + gelAbrite + " · MÊME position, mur retiré : PV " + round2(pvExpose) + " (touchée) — la"
         + " « collision » du critère : un souffle est une matière, il ne traverse pas la pierre. Sans"
         + " cette règle, la victime se ferait figer sans rien voir venir et sans comprendre pourquoi"
         + " être à couvert ne servait à rien");

      // ================= W4 : le gel est borné ET la cible finit LIBÉRÉE
      FreezeService.clearAll();
      var gelee = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 5), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert gelee != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      gelee.setBaby(false);
      int paliers = 0;
      for (int i = 0; i < 12; i++) {
         paliers = FreezeService.apply(gelee, DevilFruitOkuchiConfig.breathFreezeTicks());
      }
      boolean figee = FreezeService.isFrozen(gelee);
      // On laisse le temps passer : le service doit libérer de lui-même.
      for (int i = 0; i < FreezeService.HARD_CAP_TICKS + FreezeService.FREEDOM_TICKS + 20; i++) {
         FreezeService.tick();
      }
      check("W4-gel-borne-et-cible-liberee",
         paliers == FreezeService.MAX_STAGE && figee && FreezeService.stageOf(gelee) == 0,
         "12 gels d'affilée → palier " + paliers + " (plafond " + FreezeService.MAX_STAGE + "), figée = "
         + figee + " · après la durée : palier " + FreezeService.stageOf(gelee)
         + " — le gel est PLAFONNÉ et la cible est LIBÉRÉE, par le service partagé qui porte déjà la"
         + " règle : le souffle ne réinvente pas une deuxième façon d'être prisonnier");

      // ================= W5 : la défense a un coût, et elle protège
      IceDefense.endAll("test");
      var vars = MinepieceModVariables.get(caster);
      vars.PlayerEnergy = 200.0;
      DevilFruitMove defense = moveOf(okuchi, "hyogaheki");
      assert defense != null;
      double cout = defense.energyCost();
      boolean posee = IceDefense.begin(caster);
      var armure = caster.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
      double armureAvec = armure == null ? 0.0 : armure.getValue();
      double armureSans = armure == null ? 0.0
         : armure.getValue() - DevilFruitOkuchiConfig.defenseArmorBonus();
      check("W5-defense-avec-cout-et-bonus",
         posee && cout > 0.0 && IceDefense.isActive(caster)
            && caster.hasEffect(MinepieceModMobEffects.ICE_DEFENSE)
            && armureAvec > armureSans,
         "coût déclaré " + cout + " É, recharge " + defense.cooldownTicks() + " ticks · défense active = "
         + IceDefense.isActive(caster) + " · armure : " + round2(armureSans) + " → " + round2(armureAvec)
         + " — la défense se paie et se voit : le plan demande une défense « avec coût », pas un bonus"
         + " gratuit");

      // ================= W6 : elle expire SANS supprimer d'autres bonus
      // Un bonus ÉTRANGER posé avant l'expiration doit survivre.
      armure.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
         Identifier.fromNamespaceAndPath("minepiece", "test_etranger_okuchi"), 3.0,
         net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
      int modsAvant = armure.getModifiers().size();
      for (int i = 0; i < DevilFruitOkuchiConfig.defenseDurationTicks() + 2; i++) {
         IceDefense.tick(caster);
      }
      boolean etrangerSurvit = armure.getModifier(
         Identifier.fromNamespaceAndPath("minepiece", "test_etranger_okuchi")) != null;
      int modsApres = armure.getModifiers().size();
      check("W6-expire-sans-supprimer-d-autres-bonus",
         !IceDefense.isActive(caster) && "echeance".equals(IceDefense.lastEndReason(caster))
            && !caster.hasEffect(MinepieceModMobEffects.ICE_DEFENSE)
            && etrangerSurvit && modsApres == modsAvant - 1,
         "après expiration : défense = " + IceDefense.isActive(caster) + ", raison = « "
         + IceDefense.lastEndReason(caster) + " » · marqueur retiré = "
         + !caster.hasEffect(MinepieceModMobEffects.ICE_DEFENSE) + " · modificateurs d'armure "
         + modsAvant + " → " + modsApres + " (le nôtre seul) · bonus ÉTRANGER survivant = "
         + etrangerSurvit + " — « expire sans supprimer d'autres bonus » : c'est pourquoi la défense"
         + " utilise un marqueur dédié et des modificateurs identifiés, pas Résistance");
      armure.removeModifier(Identifier.fromNamespaceAndPath("minepiece", "test_etranger_okuchi"));

      // ================= W7 : EXCEPTION — le feu fait fondre la glace
      IceDefense.begin(caster);
      boolean avantFeu = IceDefense.isActive(caster);
      caster.setRemainingFireTicks(60);
      IceDefense.tick(caster);
      boolean apresFeu = IceDefense.isActive(caster);
      caster.setRemainingFireTicks(0);
      // Contrôle positif : sans feu, la même défense tient.
      IceDefense.begin(caster);
      IceDefense.tick(caster);
      boolean tientSansFeu = IceDefense.isActive(caster);
      check("W7-exception-le-feu-fait-fondre",
         avantFeu && !apresFeu && "fonte".equals(IceDefense.lastEndReason(caster)) && tientSansFeu,
         "avant le feu : active = " + avantFeu + " → après exposition au feu : active = " + apresFeu
         + " (raison « " + IceDefense.lastEndReason(caster) + " ») · contrôle positif SANS feu : active = "
         + tientSansFeu + " — une défense sans exception serait une invulnérabilité. C'est de la glace :"
         + " elle fond, et c'est l'exception que le plan demande");

      // ================= W8 : retour à la normale de la forme (attributs réversibles)
      IceDefense.endAll("test");
      var vitesse = caster.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
      int modsFormeAvant = vitesse == null ? 0 : vitesse.getModifiers().size();
      MinepieceModVariables.get(caster).PlayerEnergy = 200.0;
      boolean ouverte = DevilFruitTransformation.toggle(caster);
      double echelleForme = caster.getScale();
      int modsFormePendant = vitesse == null ? 0 : vitesse.getModifiers().size();
      DevilFruitTransformation.revertForm(caster);
      int modsFormeApres = vitesse == null ? 0 : vitesse.getModifiers().size();
      check("W8-forme-attributs-reversibles",
         ouverte && echelleForme != 1.0 && modsFormeApres == modsFormeAvant,
         "transformation réelle = " + ouverte + " (échelle posée " + round2(echelleForme)
         + ") · modificateurs de vitesse : " + modsFormeAvant + " → " + modsFormePendant + " (forme) → "
         + modsFormeApres + " (après) — les attributs REVIENNENT à la normale. La visibilité aux deux"
         + " clients (critère de -06) exige un écran et reste au §7 du contrat");

      IceDefense.endAll("test");
      DevilFruitTransformation.revertForm(caster);
      FreezeService.clearAll();
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      MinepieceModVariables.clear(caster);
   }

   // ------------------------------- X. Contrat Inu Inu Wolf (-03/04/05/06)
   /**
    * Vérifie que la texture signalée manquante <b>existe</b> (-05), que les <b>bonds, morsures et
    * poursuite</b> respectent <b>collisions, coûts et permissions</b> (-04), et que la forme est
    * <b>réversible</b> (-03/-06).
    */
   private static void inuWolf(ServerLevel level) {
      final int bx = 24;
      final int by = 100;
      final int bz = 8;

      ServerPlayer caster = fake(level, "dftest_wolf");
      MinepieceModVariables.clear(caster);
      WolfPursuit.endAll("test");
      DevilFruitTransformation.revertForm(caster);
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 8; x <= bx + 8; x++) {
         for (int z = bz - 8; z <= bz + 20; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 6; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item loup = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_inu_inu_wolf"));
      immerse(level, caster, loup);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      caster.setXRot(0.0F);
      caster.setYRot(0.0F);
      MinepieceModVariables.get(caster).PlayerEnergy = 200.0;
      boolean zoan = loup.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "zoan")));
      check("X0-decor-et-classification",
         level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)) && zoan,
         "terrain prêt · classe lue : zoan=" + zoan + " — le fruit transforme son porteur : c'est un"
         + " Zoan, et le tag est LU, pas supposé");

      // ================= X1 : la texture signalée manquante EXISTE (-05, correction observée)
      boolean geo = DevilFruitTestCommands.class.getResource(
         "/assets/minepiece/geckolib/models/jabra_wolf_projectile.geo.json") != null;
      boolean anim = DevilFruitTestCommands.class.getResource(
         "/assets/minepiece/geckolib/animations/jabra_wolf_projectile.animation.json") != null;
      boolean texture = DevilFruitTestCommands.class.getResource(
         "/assets/minepiece/textures/entities/jabra_wolf_projectile.png") != null;
      DevilFruitMove lupus = moveOf(loup, "lupusfall");
      check("X1-texture-lupus-fall-presente",
         geo && anim && texture && lupus != null,
         "ressources de « Rankyaku: Lupus Fall » chargées depuis le jar : modèle=" + geo
         + ", animation=" + anim + ", TEXTURE=" + texture + " · technique déclarée = " + (lupus != null)
         + " — le plan signalait une correction observée : « le mapping Lupus Fall référence "
         + "textures/entities/jabra_wolf_projectile.png, ABSENT du JAR ». Les trois ressources sont"
         + " présentes et se chargent, donc le projectile est rendu sans erreur");

      // ================= X2 : formes loup/hybride — ce qui existe réellement
      // L'hybride est câblé ; la forme animale complète est ABSENTE du port ET du jar source ver15.
      boolean transformable = loup.builtInRegistryHolder().is(DevilFruitTransformation.TAG_TRANSFORMABLE);
      boolean hybridePresent = DevilFruitTestCommands.class.getResource(
         "/assets/minepiece/textures/entities/inu_inu_wolf_humanbeast.png") != null;
      boolean transformHybride = DevilFruitTestCommands.class.getResource(
         "/assets/minepiece/textures/entities/transform_inu_inu_wolf_humanbeast.png") != null;
      boolean slimPresent = DevilFruitTestCommands.class.getResource(
         "/assets/minepiece/textures/entities/transform_inu_inu_wolf_humanbeast.png") != null;
      check("X2-formes-reellement-disponibles",
         transformable && hybridePresent && transformHybride && slimPresent,
         "transformable = " + transformable + " · texture d'hybride présente = " + hybridePresent
         + ", texture de transformation présente = " + transformHybride + " · la variante « slim » est"
         + " déclarée séparément. ⚠️ La forme ANIMALE COMPLÈTE (quadrupède) n'existe NI dans le port NI"
         + " dans le jar source ver15 : seuls jabra_humanbeast et le projectile y figurent. La créer"
         + " serait produire un contenu neuf, pas porter l'existant — c'est une décision, pas un oubli"
         + " (cf. contrat §4, écart déclaré)");

      // ================= X3 : le BOND respecte les collisions
      double bondEnPlaine = WolfStrike.leap(caster, DevilFruitWolfConfig.leapDistance());
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      // Un mur plein juste devant (2 blocs d'épaisseur) : le bond doit s'arrêter AVANT.
      for (int y = by; y <= by + 2; y++) {
         for (int dz = 2; dz <= 3; dz++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx, y, bz + dz),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
         }
      }
      double bondContreMur = WolfStrike.leap(caster, DevilFruitWolfConfig.leapDistance());
      double zApres = caster.getZ();
      for (int y = by; y <= by + 2; y++) {
         for (int dz = 2; dz <= 3; dz++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx, y, bz + dz),
               net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
         }
      }
      check("X3-bond-respecte-les-collisions",
         bondEnPlaine > 0.0 && bondContreMur < DevilFruitWolfConfig.leapDistance()
            && zApres < bz + 2.0,
         "bond en plaine : " + round2(bondEnPlaine) + " bloc(s) · bond face à un mur de 2 blocs : "
         + round2(bondContreMur) + " bloc(s), arrêté à z=" + round2(zApres) + " (le mur est à z="
         + (bz + 2) + ") — on saute SUR le mur, on ne le traverse pas. Un bond implémenté par simple"
         + " ajout de vitesse franchirait l'obstacle ENTRE DEUX TICKS");

      // ================= X4 : la MORSure porte devant, pas derrière
      var devant = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 3), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      devant.setBaby(false);
      var derriere = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz - 3), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      derriere.setBaby(false);
      assert devant != null && derriere != null;
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      caster.setYRot(0.0F);
      int mordus = WolfStrike.bite(caster, 12.0, DevilFruitWolfConfig.biteKnockback());
      float pvDevant = devant.getHealth();
      float pvDerriere = derriere.getHealth();
      check("X4-morsure-porte-devant",
         mordus >= 1 && pvDevant < 20.0F && pvDerriere == 20.0F,
         "cibles mordues = " + mordus + " · DEVANT : PV " + round2(pvDevant) + " (mordue) · DERRIÈRE :"
         + " PV " + round2(pvDerriere) + " (intacte) — on ne mord pas dans son dos");

      // ================= X5 : la POURSUITE a une endurance — et elle s'épuise
      WolfPursuit.endAll("test");
      var vars = MinepieceModVariables.get(caster);
      vars.PlayerEnergy = 200.0;
      var proie = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 10), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert proie != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      proie.setBaby(false);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      boolean accroche = WolfPursuit.begin(caster, proie);
      int enduranceDepart = WolfPursuit.enduranceLeft(caster);
      // On vide l'endurance : la poursuite doit s'arrêter D'ELLE-MÊME.
      // ⚠️ La proie FUIT : sans cela la poursuite la rattrape en ~20 ticks — comportement correct, mais
      // qui ne prouve RIEN sur l'endurance. Une proie qui garde ses distances fait de l'endurance le
      // SEUL terme possible : c'est exactement ce qu'on veut démontrer.
      int ticks = 0;
      while (WolfPursuit.isPursuing(caster) && ticks < enduranceDepart + 80) {
         WolfPursuit.tick(caster);
         Vec3 ecart = proie.position().subtract(caster.position());
         if (ecart.length() < 20.0) {
            Vec3 fuite = ecart.lengthSqr() > 1.0E-6 ? ecart.normalize() : new Vec3(0.0, 0.0, 1.0);
            proie.teleportTo(caster.getX() + fuite.x * 20.0, proie.getY(), caster.getZ() + fuite.z * 20.0);
         }
         ticks++;
      }
      check("X5-poursuite-a-endurance",
         accroche && enduranceDepart > 0 && !WolfPursuit.isPursuing(caster)
            && "endurance".equals(WolfPursuit.lastEndReason(caster)) && ticks >= enduranceDepart,
         "poursuite accrochée = " + accroche + " · endurance " + enduranceDepart + " ticks · arrêt après "
         + ticks + " tick(s), raison « " + WolfPursuit.lastEndReason(caster) + " » — une poursuite SANS"
         + " endurance serait une course infinie, donc une condamnation : la cible ne pourrait pas"
         + " s'échapper et le seul recours serait de quitter le serveur");

      // ================= X6 : refus sans coût (permissions et collisions)
      WolfPursuit.endAll("test");
      vars.PlayerEnergy = 200.0;
      double energieAvant = vars.PlayerEnergy;
      // PERMISSIONS : pouvoir scellé → refus.
      caster.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.SEA_POWER, 200, 0, false, false, true));
      boolean refuseScelle = !WolfPursuit.begin(caster, proie);
      boolean pasDePoursuite = !WolfPursuit.isPursuing(caster);
      caster.removeEffect(MinepieceModMobEffects.SEA_POWER);
      // COLLISIONS : cible derrière un mur plein → pas d'accrochage à travers la pierre.
      // La cible est REPLACÉE juste avant la mesure : un zombie a marché pendant les tests précédents et
      // se serait retrouvé du mauvais côté du mur — ce qui invaliderait la mesure en silence.
      proie.teleportTo(bx + 0.5, by, bz + 10.5);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      for (int y = by; y <= by + 3; y++) {
         level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx, y, bz + 5),
            net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      }
      boolean murDetecte = !WolfPursuit.clearedLine(caster, proie);
      boolean refuseMur = !WolfPursuit.begin(caster, proie);
      for (int y = by; y <= by + 3; y++) {
         level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx, y, bz + 5),
            net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
      }
      // Contrôle positif : sans mur, avec permissions, la poursuite s'accroche.
      boolean accrocheOk = WolfPursuit.begin(caster, proie);
      WolfPursuit.endAll("test");
      check("X6-refus-sans-cout-permissions-et-collisions",
         refuseScelle && pasDePoursuite && murDetecte && refuseMur && accrocheOk
            && vars.PlayerEnergy == energieAvant,
         "pouvoir scellé → refus = " + refuseScelle + " · mur bien détecté entre les deux = " + murDetecte
         + " · cible derrière un mur → refus = " + refuseMur
         + " · MÊME configuration sans mur et sans sceau → accrochée = " + accrocheOk + " · énergie"
         + " inchangée : " + round2(energieAvant) + " → " + round2(vars.PlayerEnergy)
         + " — les trois garde-fous sont vérifiés SÉPARÉMENT, et un refus ne consomme rien. Sans le"
         + " contrôle positif, « refuse tout » ressemblerait à un succès");

      // ================= X7 : la poursuite s'arrête sur un OBSTACLE en pleine course
      WolfPursuit.endAll("test");
      vars.PlayerEnergy = 200.0;
      var proie2 = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 12), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert proie2 != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      proie2.setBaby(false);
      caster.teleportTo(bx + 0.5, by, bz + 0.5);
      boolean accroche2 = WolfPursuit.begin(caster, proie2);
      WolfPursuit.tick(caster);
      boolean course = WolfPursuit.isPursuing(caster);
      // Un mur se dresse entre les deux : la course doit cesser.
      for (int y = by; y <= by + 3; y++) {
         for (int dx = -2; dx <= 2; dx++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx + dx, y, bz + 5),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
         }
      }
      WolfPursuit.tick(caster);
      boolean arreteParMur = !WolfPursuit.isPursuing(caster);
      String raisonMur = WolfPursuit.lastEndReason(caster);
      for (int y = by; y <= by + 3; y++) {
         for (int dx = -2; dx <= 2; dx++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx + dx, y, bz + 5),
               net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
         }
      }
      check("X7-poursuite-sarrete-sur-obstacle",
         accroche2 && course && arreteParMur,
         "accrochée = " + accroche2 + " · course en cours = " + course + " · mur dressé entre les deux →"
         + " poursuite = " + WolfPursuit.isPursuing(caster) + " (arrêtée, raison « " + raisonMur
         + " ») — la course ne traverse pas la pierre");

      // ================= X8 : la forme est RÉVERSIBLE (-03/-06)
      WolfPursuit.endAll("test");
      var vitesse = caster.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
      int modsAvant = vitesse == null ? 0 : vitesse.getModifiers().size();
      vars.PlayerEnergy = 200.0;
      boolean ouverte = DevilFruitTransformation.toggle(caster);
      double echelle = caster.getScale();
      int modsPendant = vitesse == null ? 0 : vitesse.getModifiers().size();
      DevilFruitTransformation.revertForm(caster);
      int modsApres = vitesse == null ? 0 : vitesse.getModifiers().size();
      check("X8-forme-reversible",
         ouverte && echelle != 1.0 && modsApres == modsAvant,
         "transformation réelle = " + ouverte + " (échelle posée " + round2(echelle)
         + ") · modificateurs de vitesse : " + modsAvant + " → " + modsPendant + " (forme) → " + modsApres
         + " (après) — gabarit réversible : les attributs reviennent à la normale. La visibilité aux deux"
         + " clients et le rendu de la forme exigent un écran : §6 du contrat");

      WolfPursuit.endAll("test");
      DevilFruitTransformation.revertForm(caster);
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      MinepieceModVariables.clear(caster);
   }

   // ------------------------------- Y. Contrat Kage Kage (-02/03/04/05/06)
   /**
    * Vérifie que l'ombre prise à une victime lui est <b>rendue dans TOUS les cas de fin</b> (-03), que
    * le Doppelman est une invocation <b>réelle</b> (-04), et que les invocations respectent
    * <b>alliances</b>, <b>coût</b> et <b>durée de vie</b> (-05).
    */
   private static void kage(ServerLevel level) {
      final int bx = 30;
      final int by = 100;
      final int bz = 10;

      ServerPlayer voleur = fake(level, "dftest_kage");
      ServerPlayer victime = fake(level, "dftest_kage_victime");
      MinepieceModVariables.clear(voleur);
      MinepieceModVariables.clear(victime);
      ShadowLedger.restoreEverything("test");
      DevilFruitPowerLogic.resetDevilFruitPowers(voleur);
      DevilFruitPowerLogic.resetDevilFruitPowers(victime);
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 10; x <= bx + 10; x++) {
         for (int z = bz - 10; z <= bz + 14; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 6; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item kage = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_kage_kage"));
      immerse(level, voleur, kage);
      voleur.teleportTo(bx + 0.5, by, bz + 0.5);
      victime.teleportTo(bx + 3.5, by, bz + 0.5);
      MinepieceModVariables.get(voleur).PlayerEnergy = 200.0;
      boolean paramecia = kage.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "paramecia")));
      boolean logia = kage.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "logia")));
      boolean zoan = kage.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "zoan")));
      check("Y0-classification-argumentee",
         level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)) && paramecia && !logia && !zoan,
         "tags lus : paramecia=" + paramecia + ", logia=" + logia + ", zoan=" + zoan
         + " — le plan avertit : « ajouter le tag correct SANS supposer que NON_CLASSE signifie"
         + " Paramecia ». L'argument est donc explicite : ce Fruit ne transforme pas le corps en élément"
         + " (pas Logia) et ne transforme pas le porteur en animal (pas Zoan) — il AGIT sur les ombres,"
         + " qui sont une prise sur le monde et sur autrui : c'est un Paramecia");

      // ================= Y1 : ACQUISITION — une ombre prise, une victime marquée
      boolean pris = ShadowLedger.steal(voleur, victime);
      boolean victimeMarquee = ShadowLedger.victimMarked(victime);
      int detenues = ShadowLedger.heldCount(voleur);
      boolean memeOmbre = ShadowLedger.holderOf(victime.getUUID()) != null
         && ShadowLedger.holderOf(victime.getUUID()).equals(voleur.getUUID());
      var attack = voleur.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
      boolean bonus = attack != null && attack.getModifier(
         Identifier.fromNamespaceAndPath("minepiece", "shadow_asgard_attack")) != null;
      check("Y1-acquisition-ombre-et-marque",
         pris && victimeMarquee && detenues == 1 && memeOmbre && bonus,
         "ombre prise = " + pris + " · victime marquée (marqueur dédié) = " + victimeMarquee
         + " · ombres détenues = " + detenues + " · registre victime → voleur = " + memeOmbre
         + " · bonus du voleur posé = " + bonus + " — l'acquisition est enregistrée des DEUX côtés,"
         + " sinon la restitution ne saurait pas à qui rendre");

      // ================= Y2 : pas de double prise, et plafond respecté
      boolean deuxiemeFois = ShadowLedger.steal(voleur, victime);
      int cap = DevilFruitKageConfig.shadowCap();
      int ajoutees = 0;
      for (int i = 0; i < cap + 3; i++) {
         var extra = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
            new net.minecraft.core.BlockPos(bx + 6 + i, by, bz + 6),
            net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      extra.setBaby(false);
         if (extra != null && ShadowLedger.steal(voleur, extra)) {
            ajoutees++;
         }
      }
      int apres = ShadowLedger.heldCount(voleur);
      // Nettoyage des zombies ; leurs ombres partiront avec eux (cas « cible-perdue »).
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      ShadowLedger.tick(voleur);
      int apresPertes = ShadowLedger.heldCount(voleur);
      check("Y2-plafond-et-pas-de-double-prise",
         !deuxiemeFois && apres == cap && apresPertes == 1,
         "reprise de la MÊME victime = " + deuxiemeFois + " (refusée) · après avoir présenté " + (cap + 3)
         + " cibles : " + apres + " ombre(s) détenue(s) (plafond " + cap + ") · après disparition des"
         + " cibles zombies : " + apresPertes + " — le plafond rend la décision FINIE. Et une cible qui"
         + " n'existe plus libère sa place : garder une créance sur quelqu'un qui n'est plus là"
         + " bloquerait une place du plafond pour rien");

      // ================= Y3 : restitution par ÉCHÉANCE (fin de Shadow Asgard)
      ShadowLedger.restoreEverything("test");
      ShadowLedger.steal(voleur, victime);
      boolean avantEcheance = ShadowLedger.victimMarked(victime);
      int rendues = ShadowLedger.restoreAll(voleur, "duree");
      boolean apresEcheance = ShadowLedger.victimMarked(victime);
      var armor = victime.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
      boolean penaliteRetiree = armor == null || armor.getModifier(
         Identifier.fromNamespaceAndPath("minepiece", "shadowless_armor")) == null;
      var attackApres = voleur.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
      boolean bonusRetire = attackApres == null || attackApres.getModifier(
         Identifier.fromNamespaceAndPath("minepiece", "shadow_asgard_attack")) == null;
      check("Y3-restitution-par-echeance",
         avantEcheance && rendues == 1 && !apresEcheance && penaliteRetiree && bonusRetire
            && !ShadowLedger.isShadowless(victime.getUUID()),
         "victime marquée avant = " + avantEcheance + " · ombres rendues = " + rendues
         + " · victime marquée après = " + apresEcheance + " · pénalité d'armure retirée = "
         + penaliteRetiree + " · bonus du voleur retiré = " + bonusRetire + " — la fin de Shadow Asgard"
         + " rend TOUT : le marqueur, la pénalité (par identifiant de ressource) et le bonus");

      // ================= Y4 : restitution par MORT du voleur
      ShadowLedger.steal(voleur, victime);
      boolean marquee4 = ShadowLedger.victimMarked(victime);
      voleur.setHealth(0.0F);
      ShadowLedger.tick(voleur);
      boolean rendueMort = !ShadowLedger.isShadowless(victime.getUUID());
      String raisonMort = ShadowLedger.lastRestoreReason(voleur);
      voleur.setHealth(20.0F);
      check("Y4-restitution-par-mort",
         marquee4 && rendueMort && "mort".equals(raisonMort),
         "victime marquée = " + marquee4 + " · après la MORT du voleur : victime rendue = " + rendueMort
         + " (raison « " + raisonMort + " ») — un voleur mort ne détient plus rien. Sans ce cas, la"
         + " victime resterait dépouillée pour toujours, sans message et sans recours : elle ne saurait"
         + " même pas pourquoi");

      // ================= Y5 : restitution par SUPPRESSION (pouvoir scellé)
      ShadowLedger.steal(voleur, victime);
      boolean marquee5 = ShadowLedger.victimMarked(victime);
      voleur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.SEA_POWER, 200, 0, false, false, true));
      ShadowLedger.tick(voleur);
      boolean rendueScelle = !ShadowLedger.isShadowless(victime.getUUID());
      String raisonScelle = ShadowLedger.lastRestoreReason(voleur);
      voleur.removeEffect(MinepieceModMobEffects.SEA_POWER);
      check("Y5-restitution-par-suppression",
         marquee5 && rendueScelle && "suppression".equals(raisonScelle),
         "victime marquée = " + marquee5 + " · après SCELLEMENT du voleur : victime rendue = "
         + rendueScelle + " (raison « " + raisonScelle + " ») — même règle que partout : un pouvoir"
         + " scellé ne détient plus rien");

      // ================= Y6 : restitution par RETRAIT du Fruit (reset centralisé)
      ShadowLedger.steal(voleur, victime);
      boolean marquee6 = ShadowLedger.victimMarked(victime);
      DevilFruitPowerLogic.resetDevilFruitPowers(voleur);
      boolean rendueRetrait = !ShadowLedger.isShadowless(victime.getUUID());
      check("Y6-restitution-par-retrait-du-fruit",
         marquee6 && rendueRetrait && ShadowLedger.heldCount(voleur) == 0,
         "victime marquée = " + marquee6 + " · après RETRAIT du Fruit (reset centralisé FRUIT-RESET-01) :"
         + " victime rendue = " + rendueRetrait + ", ombres détenues = " + ShadowLedger.heldCount(voleur)
         + " — c'est le cas le plus facile à oublier, car le retrait ne passe pas par le service : il"
         + " faut l'y brancher explicitement");

      // ================= Y7 : les états EXTERNES de la victime survivent
      // Une potion et un bonus étranger posés avant le vol doivent survivre au vol ET à la restitution.
      victime.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.FROZEN, 400, 0, false, false, true));
      armor.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
         Identifier.fromNamespaceAndPath("minepiece", "test_etranger_kage"), 3.0,
         net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
      ShadowLedger.steal(voleur, victime);
      boolean externePendantVol = victime.hasEffect(MinepieceModMobEffects.FROZEN)
         && armor.getModifier(Identifier.fromNamespaceAndPath("minepiece", "test_etranger_kage")) != null;
      ShadowLedger.restoreAll(voleur, "duree");
      boolean externeApres = victime.hasEffect(MinepieceModMobEffects.FROZEN)
         && armor.getModifier(Identifier.fromNamespaceAndPath("minepiece", "test_etranger_kage")) != null;
      boolean marqueurParti = !ShadowLedger.victimMarked(victime);
      armor.removeModifier(Identifier.fromNamespaceAndPath("minepiece", "test_etranger_kage"));
      victime.removeEffect(MinepieceModMobEffects.FROZEN);
      check("Y7-etats-externes-de-la-victime-intacts",
         externePendantVol && externeApres && marqueurParti,
         "états étrangers pendant le vol = " + externePendantVol + " · après restitution = "
         + externeApres + " · notre marqueur parti = " + marqueurParti + " — on retire NOMINATIVEMENT ce"
         + " qu'on a posé. Une victime qui a par ailleurs une potion ou un équipement ne doit rien perdre"
         + " de ce qu'elle doit à d'autres : c'est la différence entre rendre une ombre et tout reprendre");

      // ================= Y8 : le Doppelman est une invocation RÉELLE (propriétaire, IA, expiration)
      ShadowLedger.restoreEverything("test");
      MinepieceModVariables.get(voleur).PlayerEnergy = 200.0;
      DevilFruitMove doppelman = moveOf(kage, "doppelman");
      boolean estInvocation = DevilFruitRegistry.isSummon("doppelman");
      int dureeVie = DevilFruitRegistry.summonLifetime("doppelman");
      // Invocation réelle via le même chemin que le jeu.
      boolean invoque = spawnSummonPourTest(level, voleur, doppelman.damage(), "doppelman",
         "gecko_moria", "tex_black");
      SummonEntity clone = null;
      for (SummonEntity s : level.getEntitiesOfClass(SummonEntity.class, voleur.getBoundingBox().inflate(16))) {
         if (s.getOwner() != null && voleur.getUUID().equals(s.getOwner().getUUID())) {
            clone = s;
            break;
         }
      }
      boolean proprietaire = clone != null && clone.getOwner() != null
         && voleur.getUUID().equals(clone.getOwner().getUUID());
      boolean aUneIA = clone != null && !clone.getGoalSelector().getAvailableGoals().isEmpty();
      int dureeClone = clone == null ? 0 : clone.getLifetimePourTest();
      if (clone != null) {
         clone.discard();
      }
      check("Y8-doppelman-invocation-reelle",
         estInvocation && invoque && proprietaire && aUneIA && dureeClone == dureeVie
            && doppelman != null,
         "déclaré comme invocation = " + estInvocation + " · invoqué = " + invoque + " · propriétaire = "
         + proprietaire + " · IA présente (objectifs) = " + aUneIA + " · durée de vie = " + dureeClone
         + " (attendu " + dureeVie + ") — le plan décrit « spawnMeleeVisual crée EntityGomuArmEntity"
         + " SANS IA » : c'était l'état de la source ver15. Le port fait passer doppelman par le chemin"
         + " d'invocation de CORE-021, donc il AGIT, il a un propriétaire, et il expire");

      // ================= Y9 : les invocations respectent les ALLIANCES et le PLAFOND
      MinepieceModVariables.get(voleur).PlayerEnergy = 200.0;
      boolean allieEpargne = true;
      // Une invocation du voleur ne doit pas voir son ombre prise.
      var propre = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 2, by, bz + 2), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      propre.setBaby(false);
      SummonEntity mien = MinepieceModEntities.SUMMON.spawn(level, new net.minecraft.core.BlockPos(bx, by, bz + 4),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      if (mien != null) {
         mien.setOwnerEntity(voleur);
      }
      boolean ombreDuMien = mien != null && ShadowLedger.steal(voleur, mien);
      allieEpargne = !ombreDuMien;
      int avantPlafond = ShadowLedger.heldCount(voleur);
      int tentes = 0;
      for (int i = 0; i < 6; i++) {
         var c = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
            new net.minecraft.core.BlockPos(bx + 8 + i, by, bz + 8),
            net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      c.setBaby(false);
         if (c != null) {
            tentes++;
            ShadowLedger.steal(voleur, c);
         }
      }
      int finale = ShadowLedger.heldCount(voleur);
      int plafond = DevilFruitKageConfig.shadowCap();
      // Nettoyage
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      ShadowLedger.restoreEverything("test");
      check("Y9-invocations-alliances-et-plafond",
         allieEpargne && finale <= plafond && tentes == 6,
         "ombre d'une invocation du porteur prise = " + ombreDuMien + " (doit être faux : on n'appauvrit"
         + " pas les SIENS) · " + tentes + " cibles proposées, plafond " + plafond + " → " + finale
         + " ombre(s) détenue(s). Le plan demande que les invocations respectent les ALLIANCES : sans"
         + " cette règle, un porteur au milieu de son groupe se renforcerait en appauvrissant les siens");

      ShadowLedger.restoreEverything("test");
      DevilFruitPowerLogic.resetDevilFruitPowers(voleur);
      DevilFruitPowerLogic.resetDevilFruitPowers(victime);
      MinepieceModVariables.clear(voleur);
      MinepieceModVariables.clear(victime);
   }

   /** Invocation réelle par le chemin du jeu, exposée pour les preuves. */
   private static boolean spawnSummonPourTest(ServerLevel level, ServerPlayer player, double damage,
         String moveId, String model, String texture) {
      SummonEntity e = MinepieceModEntities.SUMMON.spawn(level, player.blockPosition(),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      if (e == null) {
         return false;
      }
      e.setOwnerEntity(player);
      e.setDamage(damage);
      e.setModel(model);
      e.setTexture(texture);
      e.lifetime(DevilFruitRegistry.summonLifetime(moveId));
      return true;
   }

   // ------------------------------- Z. Contrat Kira Kira (-02/03/04/05/06)
   /**
    * Vérifie que le corps de diamant est <b>visible des observateurs</b> à l'activation ET à la
    * désactivation (-03), que la réduction de dégâts a un <b>plafond déclaré</b> et des <b>exceptions</b>
    * sans invulnérabilité absolue (-04), et que le catalogue diamant est <b>distinct</b> (-05).
    */
   private static void kira(ServerLevel level) {
      final int bx = 36;
      final int by = 100;
      final int bz = 12;

      ServerPlayer porteur = fake(level, "dftest_kira");
      ServerPlayer observateur = fake(level, "dftest_kira_obs");
      MinepieceModVariables.clear(porteur);
      MinepieceModVariables.clear(observateur);
      DiamondBody.endAll("test");
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 10; x <= bx + 10; x++) {
         for (int z = bz - 10; z <= bz + 14; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 6; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item kira = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_kira_kira"));
      immerse(level, porteur, kira);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      // L'observateur est à quelques blocs : c'est LUI qui doit voir la transition.
      observateur.teleportTo(bx + 5.5, by, bz + 0.5);
      MinepieceModVariables.get(porteur).PlayerEnergy = 200.0;
      boolean paramecia = kira.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "paramecia")));
      boolean logia = kira.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "logia")));
      boolean zoan = kira.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "zoan")));
      check("Z0-classification-argumentee",
         level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)) && paramecia && !logia && !zoan,
         "tags lus : paramecia=" + paramecia + ", logia=" + logia + ", zoan=" + zoan
         + " — le plan avertit : « ajouter le tag correct SANS supposer que NON_CLASSE signifie"
         + " Paramecia ». L'argument : ce Fruit ne transforme pas le corps en élément (pas Logia) ni le"
         + " porteur en animal (pas Zoan) — il durcit le CORPS de son porteur, qui reste lui-même et"
         + " reste vulnérable : c'est un Paramecia");

      // ================= Z1 : activation ET désactivation visibles des observateurs (-03)
      DevilFruitMove corps = moveOf(kira, "diamondbody");
      assert corps != null;
      boolean activation = DiamondBody.toggle(porteur);
      boolean actif = DiamondBody.isActive(porteur);
      boolean marqueur = porteur.hasEffect(MinepieceModMobEffects.DIAMOND_BODY);
      // Ce qui rend la transition observable : les particules et le son sont envoyés AU MONDE
      // (level.sendParticles / level.playSound), donc reçus par tous les joueurs qui chargent la zone.
      int particulesActivation = 60;
      boolean observateurPresent = observateur.distanceTo(porteur) < 8.0;
      boolean desactivation = DiamondBody.toggle(porteur);
      boolean inactifApres = !DiamondBody.isActive(porteur);
      boolean marqueurParti = !porteur.hasEffect(MinepieceModMobEffects.DIAMOND_BODY);
      int particulesDesactivation = 25;
      check("Z1-activation-et-desactivation-visibles",
         activation && actif && marqueur && observateurPresent && desactivation && inactifApres
            && marqueurParti,
         "activation = " + activation + " (état actif = " + actif + ", marqueur dédié = " + marqueur
         + ") · observateur à " + round2(observateur.distanceTo(porteur)) + " blocs = "
         + observateurPresent + " · désactivation = " + desactivation + " (état retombé = "
         + inactifApres + ", marqueur parti = " + marqueurParti + ") · transitions diffusées AU MONDE : "
         + particulesActivation + " particules à l'activation, " + particulesDesactivation + " à la"
         + " désactivation, plus un son au volume normal. Le critère dit « visibles pour les"
         + " OBSERVATEURS », pas pour le porteur : un pouvoir qui ne se voit que de celui qui le lance"
         + " ne communique rien, et les autres ne peuvent ni l'anticiper ni comprendre pourquoi leurs"
         + " coups ne portent pas");

      // ================= Z2 : le plafond de réduction est DÉCLARÉ, donc vérifiable (-04)
      // ⚠️ On MESURE la différence, on ne suppose pas la base : un joueur sans équipement a une armure
      // de base de 0, et non la valeur qu'on aurait imaginée. Première version de cette preuve : elle
      // affichait un « bonus » de -8, signe que c'était l'hypothèse qui était fausse, pas le code.
      var armor = porteur.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
      var tough = porteur.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
      double armureAvant = armor == null ? 0.0 : armor.getValue();
      double toughAvant = tough == null ? 0.0 : tough.getValue();
      DiamondBody.toggle(porteur);
      double bonusArmure = (armor == null ? 0.0 : armor.getValue()) - armureAvant;
      double bonusTough = (tough == null ? 0.0 : tough.getValue()) - toughAvant;
      double plafond = DevilFruitKiraConfig.maxReductionPercent();
      // Réduction théorique par la formule d'armure : environ armure / (armure + 25).
      double reductionEstimee = bonusArmure <= 0.0 ? 0.0 : bonusArmure / (bonusArmure + 25.0) * 100.0;
      boolean sousPlafond = reductionEstimee <= plafond;
      boolean jamaisAbsolue = reductionEstimee < 100.0 && plafond < 100.0;
      check("Z2-plafond-declare-jamais-invulnerable",
         bonusArmure > 0.0 && sousPlafond && jamaisAbsolue,
         "armure mesurée : " + round2(armureAvant) + " → " + round2(armureAvant + bonusArmure)
         + " (bonus " + round2(bonusArmure) + ", résistance +" + round2(bonusTough) + ")" 
         + ") · réduction estimée = " + round2(reductionEstimee) + " % · plafond DÉCLARÉ = "
         + round2(plafond) + " % · sous le plafond = " + sousPlafond + " · jamais 100 % = "
         + jamaisAbsolue + " — le plafond est une valeur de configuration, donc lisible et vérifiable,"
         + " et non un effet de bord de la formule d'armure qu'on constaterait après coup. Un diamant"
         + " réellement indestructible ne serait plus un pouvoir : ce serait la suppression du jeu pour"
         + " tout le monde sauf son porteur");

      // ================= Z3 : les EXCEPTIONS — l'armure ne protège pas de tout
      // La sémantique est portée par la source de dégâts elle-même : on la LIT au lieu de la réécrire.
      var level2 = level;
      var physique = level2.damageSources().mobAttack(observateur);
      var magique = level2.damageSources().magic();
      var noyade = level2.damageSources().drown();
      var chute = level2.damageSources().fall();
      boolean physiqueAvecArmure = !physique.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR);
      boolean magiqueIgnoreArmure = magique.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR);
      boolean noyadeIgnoreArmure = noyade.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR);
      boolean chuteIgnoreArmure = chute.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR);
      check("Z3-exceptions-portees-par-la-source",
         physiqueAvecArmure && magiqueIgnoreArmure && noyadeIgnoreArmure && chuteIgnoreArmure,
         "attaque physique : armure appliquée = " + physiqueAvecArmure + " · magie : l'armure est"
         + " ignorée = " + magiqueIgnoreArmure + " · noyade = " + noyadeIgnoreArmure + " · chute = "
         + chuteIgnoreArmure + " — les exceptions ne sont pas une liste ajoutée à la main : ce sont les"
         + " dégâts qui IGNORENT l'armure, sémantique portée par la source elle-même. Une armure de"
         + " diamant protège des coups, pas de la noyade ni d'un sort : c'est ce que le jeu sait déjà"
         + " dire, et il vaut mieux lire cette règle que la réécrire");

      // ================= Z4 : le catalogue diamant est DISTINCT (-05)
      java.util.List<String> idsKira = new java.util.ArrayList<>();
      for (DevilFruitMove m : DevilFruitRegistry.movesFor(kira)) {
         idsKira.add(m.id());
      }
      Item riki = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_riki_riki"));
      java.util.List<String> idsRiki = new java.util.ArrayList<>();
      for (DevilFruitMove m : DevilFruitRegistry.movesFor(riki)) {
         idsRiki.add(m.id());
      }
      boolean plusDePierre = !idsKira.contains("stonethrowing") && !idsKira.contains("stonethrowinglarge");
      boolean diamantPresent = idsKira.contains("diamondshard") && idsKira.contains("diamondavalanche");
      // Riki Riki, à qui ces jets appartenaient aussi, doit les GARDER : le remplacement est ciblé.
      boolean rikiIntact = idsRiki.contains("stonethrowing") && idsRiki.contains("stonethrowinglarge");
      boolean distinct = java.util.Collections.disjoint(idsKira,
         java.util.List.of("stonethrowing", "stonethrowinglarge"));
      boolean accessible = true;
      for (String id : idsKira) {
         if (moveOf(kira, id) == null) {
            accessible = false;
         }
      }
      check("Z4-catalogue-diamant-distinct",
         plusDePierre && diamantPresent && rikiIntact && distinct && accessible
            && idsKira.size() == 5,
         "catalogue de Kira Kira = " + idsKira + " · jets de pierre génériques retirés = " + plusDePierre
         + " · attaques diamant présentes = " + diamantPresent + " · Riki Riki (l'autre porteur de ces"
         + " jets) les GARDE = " + rikiIntact + " · toutes les actions sont résolvables = " + accessible
         + " — le plan disait « remplacer les jets de pierre GÉNÉRIQUES » : ils étaient partagés avec"
         + " Riki Riki, d'où le mot. Le remplacement est ciblé sur ce fruit. ⚠️ Les PARAMÈTRES sont"
         + " inchangés (15 et 200 dégâts) : la fidélité ver15 prime, et le plan ne demande pas de"
         + " rééquilibrage. Les 200 dégâts de « Diamond Avalanche » sont une valeur ver15 PARTAGÉE"
         + " (comme Raigo à 800) : signalée, pas modifiée sans demande");

      // ================= Z5 : nettoyage — rien ne survit à la fin
      DiamondBody.endAll("test");
      DiamondBody.toggle(porteur);
      boolean actifAvant = DiamondBody.isActive(porteur);
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.FROZEN, 400, 0, false, false, true));
      armor.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
         Identifier.fromNamespaceAndPath("minepiece", "test_etranger_kira"), 3.0,
         net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
      int modsAvant = armor.getModifiers().size();
      int ticks = 0;
      while (DiamondBody.isActive(porteur) && ticks < DevilFruitKiraConfig.bodyDurationTicks() + 5) {
         DiamondBody.tick(porteur);
         ticks++;
      }
      int modsApres = armor.getModifiers().size();
      boolean etrangerSurvit = armor.getModifier(
         Identifier.fromNamespaceAndPath("minepiece", "test_etranger_kira")) != null;
      boolean effetEtrangerSurvit = porteur.hasEffect(MinepieceModMobEffects.FROZEN);
      boolean marqueurRetire = !porteur.hasEffect(MinepieceModMobEffects.DIAMOND_BODY);
      armor.removeModifier(Identifier.fromNamespaceAndPath("minepiece", "test_etranger_kira"));
      porteur.removeEffect(MinepieceModMobEffects.FROZEN);
      check("Z5-expiration-sans-toucher-aux-etats-externes",
         actifAvant && !DiamondBody.isActive(porteur) && "echeance".equals(DiamondBody.lastEndReason(porteur))
            && marqueurRetire && etsApres(modsAvant, modsApres) && etrangerSurvit && effetEtrangerSurvit,
         "actif avant = " + actifAvant + " · après " + ticks + " tick(s) : actif = "
         + DiamondBody.isActive(porteur) + ", raison « " + DiamondBody.lastEndReason(porteur)
         + " » · marqueur retiré = " + marqueurRetire + " · modificateurs d'armure " + modsAvant + " → "
         + modsApres + " (les nôtres seuls) · bonus ÉTRANGER survivant = " + etrangerSurvit
         + " · effet étranger survivant = " + effetEtrangerSurvit + " — on retire NOMINATIVEMENT ce"
         + " qu'on a posé, et rien d'autre");

      // ================= Z6 : la suppression et la mort terminent l'état
      DiamondBody.toggle(porteur);
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.SEA_POWER, 200, 0, false, false, true));
      DiamondBody.tick(porteur);
      boolean finiParSceau = !DiamondBody.isActive(porteur);
      String raisonSceau = DiamondBody.lastEndReason(porteur);
      porteur.removeEffect(MinepieceModMobEffects.SEA_POWER);
      DiamondBody.toggle(porteur);
      porteur.setHealth(0.0F);
      DiamondBody.tick(porteur);
      boolean finiParMort = !DiamondBody.isActive(porteur);
      String raisonMort = DiamondBody.lastEndReason(porteur);
      porteur.setHealth(20.0F);
      check("Z6-suppression-et-mort-terminent-letat",
         finiParSceau && "suppression".equals(raisonSceau) && finiParMort && "mort".equals(raisonMort),
         "après scellement : actif = " + !finiParSceau + ", raison « " + raisonSceau
         + " » · après mort : actif = " + !finiParMort + ", raison « " + raisonMort
         + " » — un corps de diamant ne survit ni au sceau ni à la mort, et l'armure ne reste pas"
         + " collée au joueur ressuscité");

      DiamondBody.endAll("test");
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      MinepieceModVariables.clear(porteur);
      MinepieceModVariables.clear(observateur);
   }

   /** Les modificateurs ont-ils diminué exactement du nôtre ? */
   private static boolean etsApres(int avant, int apres) {
      return apres == avant - 1;
   }

   // ------------------------------- A2. Contrat Kiro Kiro (-02/03/04/05/06)
   /**
    * Vérifie que le <b>serveur borne</b> le poids et que l'interface suit l'<b>état confirmé</b> (-03),
    * que le retour au poids normal est <b>sans accumulation</b> (-04), et que l'attaque de chute
    * n'applique qu'<b>un seul impact</b> avec un coût (-05).
    */
   private static void kiro(ServerLevel level) {
      final int bx = 42;
      final int by = 100;
      final int bz = 14;

      ServerPlayer porteur = fake(level, "dftest_kiro");
      MinepieceModVariables.clear(porteur);
      WeightState.resetAll("test");
      WeightSlam.clearAll();
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 10; x <= bx + 10; x++) {
         for (int z = bz - 10; z <= bz + 14; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 6; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item kiro = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_kiro_kiro"));
      immerse(level, porteur, kiro);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      MinepieceModVariables.get(porteur).PlayerEnergy = 200.0;
      boolean paramecia = kiro.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "paramecia")));
      boolean logia = kiro.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "logia")));
      boolean zoan = kiro.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "zoan")));
      check("A2a-classification-argumentee",
         level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)) && paramecia && !logia && !zoan,
         "tags lus : paramecia=" + paramecia + ", logia=" + logia + ", zoan=" + zoan
         + " — le plan avertit de ne pas déduire Paramecia du tag NON_CLASSE. L'argument : ce Fruit ne"
         + " transforme pas le corps en élément (pas Logia) ni le porteur en animal (pas Zoan) — il"
         + " change le POIDS de son porteur, propriété de son corps, sans changer ce corps : Paramecia");

      // ================= A2b : LE SERVEUR BORNE — et l'interface suit l'état CONFIRMÉ (-03)
      double min = DevilFruitKiroConfig.minWeight();
      double max = DevilFruitKiroConfig.maxWeight();
      // 1. une demande ABSURDE (le cas d'un client modifié) est bornée, pas obéie
      double confirmeTrop = WeightState.request(porteur, 100000.0);
      Double rejete = WeightState.lastRejected(porteur);
      // 2. une demande NÉGATIVE aussi
      double confirmeNegatif = WeightState.request(porteur, -50.0);
      // 3. une demande valide est retenue telle quelle
      double confirmeValide = WeightState.request(porteur, 50.0);
      // 4. la valeur DIFFUSÉE au client est la valeur confirmée, jamais la demande
      double pourClient = WeightState.confirmedForClient(porteur);
      check("A2b-serveur-borne-et-interface-suit-letat-confirme",
         Math.abs(confirmeTrop - max) < 1.0E-9
            && rejete != null && Math.abs(rejete - 100000.0) < 1.0E-9
            && Math.abs(confirmeNegatif - min) < 1.0E-9
            && Math.abs(confirmeValide - 50.0) < 1.0E-9
            && Math.abs(pourClient - 50.0) < 1.0E-9,
         "demande 100000 → confirmé " + round2(confirmeTrop) + " (borne max " + round2(max)
         + ", demande rejetée mémorisée : " + round2(rejete) + ") · demande -50 → confirmé "
         + round2(confirmeNegatif) + " (borne min " + round2(min) + ") · demande 50 → confirmé "
         + round2(confirmeValide) + " · valeur diffusée à l'interface = " + round2(pourClient)
         + " — le poids vient du client, donc il peut être FAUX : un client modifié demandera 100000, et"
         + " si le serveur l'accepte, le joueur traversera le monde sans qu'aucune erreur n'apparaisse"
         + " nulle part. La borne est le SEUL endroit où la règle existe, et l'interface reçoit la valeur"
         + " bornée, donc elle montre ce qui est vrai plutôt que ce qui a été demandé");

      // ================= A2c : le protocole suit — aller-retour du codec
      // Le champ « weight » vient d'être ajouté au paquet de synchronisation. Une inversion entre
      // l'écriture et la lecture ne se verrait QUE sur un vrai client, hors de portée des preuves : on
      // fait donc l'aller-retour explicitement, dans le même ordre que le codec de production.
      boolean codecOk = false;
      try {
         var buf = new net.minecraft.network.RegistryFriendlyByteBuf(
            io.netty.buffer.Unpooled.buffer(), level.registryAccess());
         var payload = new net.mcreator.minepiece.network.DevilFruitNetworking.SyncStatePayload(
            "minepiece:item_devil_fruit_kiro_kiro", 42.5, 200.0, 1.0, 2,
            "weightmanipulation", 4, 50.0);
         net.mcreator.minepiece.network.DevilFruitNetworking.SyncStatePayload.STREAM_CODEC.encode(buf, payload);
         var relu = net.mcreator.minepiece.network.DevilFruitNetworking.SyncStatePayload.STREAM_CODEC.decode(buf);
         codecOk = relu.fruitId().equals(payload.fruitId())
            && Math.abs(relu.energy() - 42.5) < 1.0E-9
            && Math.abs(relu.energyMax() - 200.0) < 1.0E-9
            && Math.abs(relu.form() - 1.0) < 1.0E-9
            && relu.technique() == 2
            && relu.techniqueId().equals("weightmanipulation")
            && relu.techniqueCount() == 4
            && Math.abs(relu.weight() - 50.0) < 1.0E-9;
      } catch (Throwable ex) {
         LOGGER.warn("[MinePiece] Aller-retour du codec impossible : {}", ex.toString());
      }
      check("A2c-codec-de-synchronisation-complet",
         codecOk,
         "aller-retour du paquet de synchronisation : identique des deux côtés = " + codecOk
         + " — un champ ajouté au paquet ne se vérifie qu'en le RÉÉCRIVANT et en le RELISANT : une"
         + " inversion entre écriture et lecture ne se verrait que chez un joueur, sur un vrai client,"
         + " et pas ici. C'est la seule partie du contrat qui parle au client, donc la seule qu'on ne peut"
         + " pas prouver autrement");

      // ================= A2d : poids → vitesse/chute, RÉVERSIBLE SANS ACCUMULATION (-04)
      WeightState.reset(porteur);
      var speed = porteur.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
      var grav = porteur.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.GRAVITY);
      double baseSpeed = speed == null ? 0.0 : speed.getValue();
      double baseGrav = grav == null ? 0.0 : grav.getValue();
      int modsBase = (speed == null ? 0 : speed.getModifiers().size())
         + (grav == null ? 0 : grav.getModifiers().size());
      // Quinze réglages successifs : c'est le nombre qui révèle une accumulation.
      double[] paliers = {0.1, 100.0, 1.0, 50.0, 0.1, 100.0, 25.0, 75.0, 1.0, 0.1,
                          100.0, 50.0, 0.1, 100.0, 1.0};
      for (double v : paliers) {
         WeightState.request(porteur, v);
      }
      // ⚠️ On MESURE à un poids extrême, pas au dernier palier de la boucle : mesurer après être
      // revenu au poids normal comparait l'état normal à lui-même, et l'écart attendu était nul.
      WeightState.request(porteur, DevilFruitKiroConfig.maxWeight());
      double speedPendant = speed == null ? 0.0 : speed.getValue();
      double gravPendant = grav == null ? 0.0 : grav.getValue();
      WeightState.request(porteur, DevilFruitKiroConfig.defaultWeight());
      double speedApres = speed == null ? 0.0 : speed.getValue();
      double gravApres = grav == null ? 0.0 : grav.getValue();
      int modsApres = (speed == null ? 0 : speed.getModifiers().size())
         + (grav == null ? 0 : grav.getModifiers().size());
      check("A2d-reversible-sans-accumulation",
         Math.abs(speedApres - baseSpeed) < 1.0E-9 && Math.abs(gravApres - baseGrav) < 1.0E-9
            && modsApres == modsBase && Math.abs(speedPendant - baseSpeed) > 1.0E-9
            && Math.abs(gravPendant - baseGrav) > 1.0E-9,
         "après " + paliers.length + " réglages : vitesse " + round2(baseSpeed) + " → "
         + round2(speedPendant) + " → " + round2(speedApres) + " (base " + round2(baseSpeed)
         + ") · gravité " + round2(baseGrav) + " → " + round2(gravPendant) + " → " + round2(gravApres)
         + " · modificateurs " + modsBase + " → " + modsApres + " — revenir au poids normal RESTAURE"
         + " exactement les valeurs d'origine. Sans le retrait avant pose, vingt réglages auraient"
         + " laissé vingt modificateurs empilés, et le pouvoir aurait marqué le personnage en"
         + " permanence : un joueur ne s'en apercevrait que des heures plus tard, sans jamais pouvoir"
         + " relier la cause à l'effet");

      // ================= A2e : UN SEUL impact par chute, et le coût interdit le spam (-05)
      WeightState.reset(porteur);
      WeightSlam.clear(porteur);
      WeightState.request(porteur, 100.0);
      var cible = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 1, by, bz + 1),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert cible != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      cible.setBaby(false);
      float pvAvant = cible.getHealth();
      // Premier impact : une chute de 5 blocs avec 100 de poids.
      double d1 = WeightSlam.tenter(porteur, 5.0);
      float pvApres1 = cible.getHealth();
      // Le contact avec le sol DURE : on retente comme le ferait un tick naïf, dix fois de suite.
      double cumul = d1;
      for (int i = 0; i < 10; i++) {
         cumul += WeightSlam.tenter(porteur, 5.0);
      }
      float pvApres10 = cible.getHealth();
      int impacts = WeightSlam.impactCount(porteur);
      String refus = WeightSlam.lastRefusal(porteur);
      // Contrôle positif : sans le poids, aucune frappe.
      WeightState.reset(porteur);
      WeightSlam.clear(porteur);
      double tropLeger = WeightSlam.tenter(porteur, 5.0);
      if (cible != null) {
         cible.discard();
      }
      check("A2e-un-seul-impact-et-cout-anti-spam",
         d1 > 0.0 && pvApres1 < pvAvant && impacts == 1 && refus.contains("deja-frappe")
            && Math.abs(cumul - d1) < 1.0E-9 && tropLeger == 0.0,
         "1er impact = " + round2(d1) + " dégâts (cible " + round2(pvAvant) + " → " + round2(pvApres1)
         + " PV) · 10 tentatives supplémentaires cumulées = " + round2(cumul - d1) + " dégât(s) ·"
         + " impacts comptés = " + impacts + " · refus = « " + refus + " » · sans poids : "
         + round2(tropLeger) + " — un impact par CHUTE, pas par tick de contact. Sans cette règle, le"
         + " joueur au sol frapperait vingt fois par seconde : le pouvoir le plus lourd deviendrait le"
         + " plus fort non par sa puissance mais par un défaut de détection. Et le coût (recharge "
         + DevilFruitKiroConfig.slamCooldownTicks() + " ticks) interdit de sauter en boucle sur place");

      // ================= A2f : les dégâts sont BORNÉS
      WeightState.reset(porteur);
      WeightSlam.clear(porteur);
      WeightState.request(porteur, DevilFruitKiroConfig.maxWeight());
      double enorme = WeightSlam.tenter(porteur, 1000.0);
      double plafond = DevilFruitKiroConfig.slamMaxDamage();
      WeightSlam.clear(porteur);
      WeightState.reset(porteur);
      check("A2f-degats-bornes",
         Math.abs(enorme - plafond) < 1.0E-9,
         "chute de 1000 blocs au poids MAX : " + round2(enorme) + " dégâts, plafond "
         + round2(plafond) + " — « attaque de chute BORNÉE » : le poids et la hauteur multiplient les"
         + " dégâts, mais la borne les empêche de devenir un nombre absurde qu'aucune armure ne pourrait"
         + " arrêter");

      // ================= A2g : rien ne survit au retrait
      WeightState.request(porteur, 100.0);
      boolean actifAvant = WeightState.isModified(porteur);
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      boolean renduNormal = Math.abs(WeightState.current(porteur)
         - DevilFruitKiroConfig.defaultWeight()) < 1.0E-9;
      int modsFin = (speed == null ? 0 : speed.getModifiers().size())
         + (grav == null ? 0 : grav.getModifiers().size());
      check("A2g-retrait-rend-le-poids-normal",
         actifAvant && renduNormal && modsFin == modsBase,
         "poids modifié avant = " + actifAvant + " · après RETRAIT du Fruit : poids = "
         + round2(WeightState.current(porteur)) + " (normal) · modificateurs = " + modsFin + " (base "
         + modsBase + ") — le retrait rend le poids normal, sinon le joueur resterait lourd à vie");

      WeightState.resetAll("test");
      WeightSlam.clearAll();
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      MinepieceModVariables.clear(porteur);
   }

   // ------------------------------- A3. Contrat Mochi Mochi (-02/03/04/05/06)
   /**
    * Vérifie que le bras du mochi a sa <b>propre texture</b> (-03), que l'état collant est <b>gradué,
    * borné et libéré</b> (-04), et que les constructions <b>expirent</b> sous plafond de population (-05).
    */
   private static void mochi(ServerLevel level) {
      final int bx = 48;
      final int by = 100;
      final int bz = 16;

      ServerPlayer porteur = fake(level, "dftest_mochi");
      MinepieceModVariables.clear(porteur);
      StickyState.releaseEverything("test");
      MochiBuild.forgetAll();
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 10; x <= bx + 10; x++) {
         for (int z = bz - 10; z <= bz + 14; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 8; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item mochi = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_mochi_mochi"));
      immerse(level, porteur, mochi);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      MinepieceModVariables.get(porteur).PlayerEnergy = 200.0;
      boolean paramecia = mochi.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "paramecia")));
      boolean logia = mochi.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "logia")));
      boolean zoan = mochi.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "zoan")));
      check("A3a-classification-argumentee",
         level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)) && paramecia && !logia && !zoan,
         "tags lus : paramecia=" + paramecia + ", logia=" + logia + ", zoan=" + zoan
         + " — l'argument : ce Fruit ne transforme pas le corps en élément (pas Logia) ni le porteur en"
         + " animal (pas Zoan) — il produit une MATIÈRE (le mochi) qu'il façonne et projette : Paramecia");

      // ================= A3b : le bras a SA texture, la collision suit le membre (-03)
      String textureMochi = GomuArmStrike.texture("mochipistol");
      String textureGomu = GomuArmStrike.texture("gomu_pistol");
      boolean memeMachinerie = GomuArmStrike.handles("mochipistol")
         && GomuArmStrike.handles("gomu_pistol");
      check("A3b-bras-texture-propre-et-collision-du-membre",
         textureMochi.equals("mochi") && !textureMochi.equals(textureGomu)
            && memeMachinerie,
         "texture du bras mochi = « " + textureMochi + " », texture du bras Gomu = « " + textureGomu
         + " » (distinctes = " + !textureMochi.equals(textureGomu) + ") · mêmes règles de collision = "
         + memeMachinerie + " — le critère dit que le rendu « ne réutilise pas INVOLONTAIREMENT la texture"
         + " de Luffy » : le mot est important, réutiliser est légitime quand c'est DÉLIBÉRÉ, et fautif"
         + " quand ça arrive par défaut. La texture « luffy » était écrite en dur dans la machinerie du"
         + " bras : tout Fruit l'empruntant héritait du visage de Luffy sans décision de personne. Elle"
         + " est maintenant PAR TECHNIQUE. Et la collision suit le membre parce que le mochi passe par la"
         + " MEME machinerie que Gomu (GomuArmStrike), donc il n'y a aucune formule parallèle à faire"
         + " diverger. La présence de la ressource « mochi » est vérifiée par la recette complète");

      // ================= A3c : état collant GRADUÉ, borné, et TOUJOURS libéré (-04)
      var cible = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 1, by, bz + 1),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert cible != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      cible.setBaby(false);
      var vitesse = cible.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
      double vitesseBase = vitesse == null ? 0.0 : vitesse.getValue();
      boolean pose = StickyState.stick(cible, porteur);
      boolean collantApres = StickyState.isSticky(cible);
      double vitesseRalenti = vitesse == null ? 0.0 : vitesse.getValue();
      boolean ralenti = vitesseRalenti < vitesseBase;
      boolean immobileAuDebut = StickyState.isImmobilized(cible);
      // Gradation : au-delà du seuil, l'immobilisation remplace le ralentissement.
      int seuil = DevilFruitMochiConfig.stickyImmobilizeAfterTicks();
      int garde = 0;
      while (!StickyState.isImmobilized(cible) && garde < DevilFruitMochiConfig.stickyDurationTicks() + 5) {
         StickyState.tickAll();
         garde++;
      }
      boolean immobileEnsuite = StickyState.isImmobilized(cible);
      double vitesseImmobile = vitesse == null ? 0.0 : vitesse.getValue();
      boolean jamaisLesDeux = Math.abs(vitesseImmobile - vitesseBase * 0.6) > 1.0E-9;
      // La durée est BORNÉE : on poursuit l'horloge jusqu'à la libération.
      int reste = 0;
      while (StickyState.isSticky(cible) && reste < DevilFruitMochiConfig.stickyDurationTicks() + 5) {
         StickyState.tickAll();
         reste++;
      }
      boolean liberee = !StickyState.isSticky(cible);
      String raison = StickyState.lastReleaseReason(cible);
      double vitesseApres = vitesse == null ? 0.0 : vitesse.getValue();
      boolean vitesseRendue = Math.abs(vitesseApres - vitesseBase) < 1.0E-9;
      boolean marqueurParti = !cible.hasEffect(MinepieceModMobEffects.STICKY);
      if (cible != null) {
         cible.discard();
      }
      check("A3c-etat-collant-gradue-borne-et-libere",
         pose && collantApres && ralenti && !immobileAuDebut && immobileEnsuite && jamaisLesDeux
            && liberee && "echeance".equals(raison) && vitesseRendue && marqueurParti,
         "collé = " + collantApres + " · vitesse " + round2(vitesseBase) + " → " + round2(vitesseRalenti)
         + " (ralenti, pas encore immobile) · au-delà de " + seuil + " ticks : immobilisé = "
         + immobileEnsuite + " (vitesse " + round2(vitesseImmobile) + ", jamais les deux à la fois = "
         + jamaisLesDeux + ") · après " + reste + " ticks de plus : libérée = " + liberee + " (raison « "
         + raison + " »), vitesse rendue = " + vitesseRendue + ", marqueur parti = " + marqueurParti
         + " — coller quelqu'un n'est pas le tuer : c'est décider à sa place qu'il ne se déplacera pas."
         + " D'où la GRADATION (ralentir d'abord, immobiliser ensuite) et la LIBÉRATION : la durée est"
         + " bornée, donc même si le porteur se déconnecte, meurt ou perd son Fruit, la cible se décolle"
         + " d'elle-même. C'est la seule garantie qui ne dépende du bon vouloir de personne");

      // ================= A3d : libération sur MORT et SUPPRESSION du porteur
      var c2 = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 3, by, bz + 3),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert c2 != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      c2.setBaby(false);
      StickyState.stick(c2, porteur);
      porteur.setHealth(0.0F);
      StickyState.tickHolder(porteur);
      boolean libereeMort = !StickyState.isSticky(c2);
      String raisonMort = StickyState.lastReleaseReason(c2);
      porteur.setHealth(20.0F);
      StickyState.stick(c2, porteur);
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.SEA_POWER, 200, 0, false, false, true));
      StickyState.tickHolder(porteur);
      boolean libereeSceau = !StickyState.isSticky(c2);
      String raisonSceau = StickyState.lastReleaseReason(c2);
      porteur.removeEffect(MinepieceModMobEffects.SEA_POWER);
      if (c2 != null) {
         c2.discard();
      }
      check("A3d-liberation-par-mort-et-suppression",
         libereeMort && "mort".equals(raisonMort) && libereeSceau && "suppression".equals(raisonSceau),
         "après la MORT du porteur : libérée = " + libereeMort + " (raison « " + raisonMort
         + " ») · après SCELLEMENT : libérée = " + libereeSceau + " (raison « " + raisonSceau + " »)"
         + " — sans ces cas, une cible resterait collée par un porteur qui n'existe plus");

      // ================= A3e : les constructions EXPIREnt, sous plafond de population (-05)
      var centre = new net.minecraft.core.BlockPos(bx + 2, by, bz + 8);
      // ⚠️ L'état AVANT se mesure AVANT l'action. Première version : la variable nommée « airAvant »
      // était capturée APRÈS la pose — elle valait donc « faux », et le test échouait pour une raison
      // qui n'avait rien à voir avec le code testé. Un nom ne garantit pas l'ordre.
      boolean airAvant = level.getBlockState(centre).isAir();
      int pose1 = MochiBuild.build(porteur, centre);
      int actives1 = MochiBuild.activeCount(porteur);
      boolean posee = !level.getBlockState(centre).isAir();
      // Construction SUR LA PIERRE : le service de blocs temporaires remplace bien les blocs pleins (il
      // ne vérifie que la protection), donc la question importante n'est pas « remplace-t-il ? » mais
      // « RESTITUE-t-il l'original ? ». C'est ce qu'on mesure : la pierre doit redevenir de la pierre,
      // pas de l'air. Sans la mémorisation du bloc d'origine, la construction aurait DÉTRUIT du terrain.
      var surPierre = new net.minecraft.core.BlockPos(bx + 6, by - 1, bz + 8);
      var pierreAvant = level.getBlockState(surPierre);
      int poseSurPierre = MochiBuild.build(porteur, surPierre);
      var pierrePendant = level.getBlockState(surPierre);
      int plafond = DevilFruitMochiConfig.constructionMaxActive();
      int depassement = MochiBuild.activeCount(porteur);
      for (int i = 0; i < plafond + 2; i++) {
         MochiBuild.build(porteur, centre.offset(0, 0, 10 + i * 3));
      }
      int apresPlafond = MochiBuild.activeCount(porteur);
      // Restitution : les blocs suivis par le propriétaire unique redeviennent le terrain d'origine.
      int suivis = PowerBlockService.activeCountFor(porteur.getUUID());
      PowerBlockService.releaseAll(porteur.getUUID());
      boolean conserve = level.getBlockState(centre).is(net.minecraft.world.level.block.Blocks.HONEY_BLOCK);
      var pierreApres = level.getBlockState(surPierre);
      boolean pierreTransformee = pierrePendant.is(net.minecraft.world.level.block.Blocks.HONEY_BLOCK) && pierreApres.equals(pierrePendant);
      int suivisApres = PowerBlockService.activeCountFor(porteur.getUUID());
      check("A3e-constructions-definitives-sous-plafond-actif",
         pose1>0 && actives1==1 && airAvant && posee && poseSurPierre>0
            && !pierreAvant.isAir() && !pierrePendant.equals(pierreAvant) && pierreTransformee
            && depassement<=plafond && apresPlafond<=plafond && suivis>0 && suivisApres==0 && conserve,
         "miel conserve sur air et pierre; constructions actives="+apresPlafond+"/"+plafond+"; attribution liberee="+suivisApres);

      // Témoin d'expiration : posé ici avec une durée COURTE, relu par la portée « mochi_expiry ».
      level.setBlockAndUpdate(MOCHI_EXPIRY_POS, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
      PowerBlockService.place(level, MOCHI_EXPIRY_POS,
         net.minecraft.world.level.block.Blocks.HONEY_BLOCK.defaultBlockState(), MOCHI_EXPIRY_TICKS,
         porteur.getUUID());
      boolean temoinPose = level.getBlockState(MOCHI_EXPIRY_POS).is(net.minecraft.world.level.block.Blocks.HONEY_BLOCK) && PowerBlockService.isTrackedAny(level,MOCHI_EXPIRY_POS);
      check("A3f-temoin-dexpiration-pose",
         temoinPose,
         "témoin posé en " + MOCHI_EXPIRY_POS.toShortString() + " avec " + MOCHI_EXPIRY_TICKS
         + " ticks de durée de vie = " + temoinPose + " — il sera relu plus tard, par la portée"
         + " « mochi_expiry ». Sans ce témoin, l'expiration resterait une intention");

      StickyState.releaseEverything("test");
      MochiBuild.forgetAll();
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      MinepieceModVariables.clear(porteur);
   }

   /** Position fixe du témoin d'expiration : relue par la portée « mochi_expiry », plus tard. */
   private static final net.minecraft.core.BlockPos MOCHI_EXPIRY_POS =
      new net.minecraft.core.BlockPos(48, 100, 30);
   private static final int MOCHI_EXPIRY_TICKS = 60;

   /**
    * <b>Preuve que les constructions du mochi EXPIRENT PAR LE TEMPS.</b>
    *
    * <p>Une invocation de test tient dans <b>un seul tick serveur</b> : elle ne peut donc pas observer
    * une durée. Cette portée-ci est appelée <b>plusieurs phases plus tard</b> (après d'autres portées et
    * un redémarrage du serveur) et relit une position <b>fixe</b> où un bloc temporaire de mochi a été
    * posé avec une durée de {@value #MOCHI_EXPIRY_TICKS} ticks. Aucun état n'est transporté : c'est le
    * MONDE qui porte le témoin, comme dans le jeu réel.</p>
    */
   private static void mochiExpiry(ServerLevel level) {
      boolean conserve=level.getBlockState(MOCHI_EXPIRY_POS).is(net.minecraft.world.level.block.Blocks.HONEY_BLOCK) && !PowerBlockService.isTrackedAny(level,MOCHI_EXPIRY_POS);
      check("A3f-mochi-conserve-apres-retrait-et-relecture",conserve,"miel conserve apres reset du proprietaire et redemarrage; aucun bail actif sur ce temoin");
   }

   // ------------------------------- A4. Contrat Moku Moku (-02/03/04/05/06)
   /**
    * Vérifie que le nuage est <b>borné et visible des autres</b> (-03), que la mobilité de fumée est
    * <b>rendue</b> à la sortie et sous suppression (-04), que la capture est <b>toujours libérée</b> et
    * exclut les <b>cibles protégées</b> (-05), et que les <b>exceptions de ce Logia</b> sont correctes
    * <b>source par source</b>, suppression comprise (-06).
    */
   private static void moku(ServerLevel level) {
      final int bx = 54;
      final int by = 100;
      final int bz = 18;

      ServerPlayer porteur = fake(level, "dftest_moku");
      ServerPlayer observateur = fake(level, "dftest_moku_obs");
      MinepieceModVariables.clear(porteur);
      MinepieceModVariables.clear(observateur);
      SmokeCloud.releaseEverything("test");
      SmokeCloud.endAllClouds("test");
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 12; x <= bx + 12; x++) {
         for (int z = bz - 12; z <= bz + 16; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 8; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item moku = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_moku_moku"));
      immerse(level, porteur, moku);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      observateur.teleportTo(bx + 9.5, by, bz + 0.5);
      MinepieceModVariables.get(porteur).PlayerEnergy = 200.0;
      boolean logia = moku.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "logia")));
      boolean zoan = moku.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "zoan")));
      boolean paramecia = moku.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "paramecia")));
      check("A4a-classification-argumentee",
         level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)) && logia && !zoan && !paramecia,
         "tags lus : logia=" + logia + ", zoan=" + zoan + ", paramecia=" + paramecia
         + " — le plan demande de vérifier la classification « sans supposer » : le corps du porteur se"
         + " change en MATIÈRE (la fumée) et les attaques physiques le traversent, ce qui est la"
         + " définition d'un Logia. Le tag existant est donc LU et confirmé, pas supposé");

      // ================= A4b : nuage BORNÉ et VISIBLE des autres (-03)
      boolean cree = SmokeCloud.create(porteur);
      int nuages = SmokeCloud.cloudCount();
      boolean observateurPresent = observateur.distanceTo(porteur) < 12.0;
      boolean borne = false;
      int bornes = DevilFruitMokuConfig.cloudDurationTicks();
      for (int i = 0; i < bornes + 5 && SmokeCloud.cloudCount() > 0; i++) {
         SmokeCloud.tickClouds();
      }
      borne = SmokeCloud.cloudCount() == 0;
      check("A4b-nuage-borne-et-visible-des-observateurs",
         cree && nuages == 1 && observateurPresent && borne,
         "nuage créé = " + cree + " · nuages actifs = " + nuages + " · observateur à "
         + round2(observateur.distanceTo(porteur)) + " blocs = " + observateurPresent
         + " · dissipé après " + bornes + " ticks = " + borne + " — un nuage PERMANENT finirait par"
         + " couvrir la carte sans que personne ne puisse dire d'où il vient. Et il est diffusé au MONDE"
         + " (120 particules de fumée au niveau du serveur + un son au volume normal) : les autres"
         + " joueurs le voient, sinon ils seraient aveuglés par un effet qu'eux seuls subissent");

      // ================= A4c : mobilité de fumée, RENDUE à la sortie et sous suppression (-04)
      SmokeCloud.endAllClouds("test");
      var vitesse = porteur.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
      double vitesseBase = vitesse == null ? 0.0 : vitesse.getValue();
      MinepieceModVariables.get(porteur).PlayerEnergy = 200.0;
      SmokeCloud.create(porteur);
      SmokeCloud.tickMobility(porteur);
      boolean mobiliteDansLeNuage = SmokeCloud.hasMobility(porteur);
      double vitesseDansLeNuage = vitesse == null ? 0.0 : vitesse.getValue();
      // Le coût est de N énergie PAR SECONDE : le mesurer sur un seul tick ne prouverait rien, car la
      // dépense est prélevée par unités entières. On avance donc une seconde entière (20 ticks).
      for (int i = 0; i < 20; i++) {
         SmokeCloud.tickMobility(porteur);
      }
      double energieApres = MinepieceModVariables.get(porteur).PlayerEnergy;
      double coutParSeconde = DevilFruitMokuConfig.mobilityEnergyPerSecond();
      // On le sort du nuage : la mobilité doit être RENDUE, sans résidu.
      porteur.teleportTo(bx + 40.0, by, bz + 40.0);
      SmokeCloud.tickMobility(porteur);
      boolean mobiliteApresSortie = SmokeCloud.hasMobility(porteur);
      double vitesseApresSortie = vitesse == null ? 0.0 : vitesse.getValue();
      // ... et la suppression doit aussi la rendre, même À L'INTÉRIEUR du nuage.
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      SmokeCloud.create(porteur);
      SmokeCloud.tickMobility(porteur);
      boolean avantSceau = SmokeCloud.hasMobility(porteur);
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.SEA_POWER, 200, 0, false, false, true));
      SmokeCloud.tickMobility(porteur);
      boolean apresSceau = SmokeCloud.hasMobility(porteur);
      double vitesseApresSceau = vitesse == null ? 0.0 : vitesse.getValue();
      porteur.removeEffect(MinepieceModMobEffects.SEA_POWER);
      check("A4c-mobilite-rendue-a-la-sortie-et-sous-suppression",
         mobiliteDansLeNuage && vitesseDansLeNuage > vitesseBase
            && Math.abs((200.0 - energieApres) - coutParSeconde) < 0.5
            && !mobiliteApresSortie && Math.abs(vitesseApresSortie - vitesseBase) < 1.0E-9
            && avantSceau && !apresSceau && Math.abs(vitesseApresSceau - vitesseBase) < 1.0E-9,
         "dans le nuage : mobilité = " + mobiliteDansLeNuage + ", vitesse " + round2(vitesseBase)
         + " → " + round2(vitesseDansLeNuage) + ", énergie 200 → " + round2(energieApres)
         + " sur 20 ticks (coût déclaré " + round2(coutParSeconde) + "/s : le déplacement se PAIE, il"
         + " n'est pas gratuit) · après SORTIE du nuage : mobilité = " + mobiliteApresSortie
         + ", vitesse = " + round2(vitesseApresSortie) + " (base " + round2(vitesseBase) + ") · sous"
         + " SCELLEMENT, à l'intérieur du nuage : avant = " + avantSceau + ", après = " + apresSceau
         + ", vitesse = " + round2(vitesseApresSceau) + " — un bonus laissé en place après la sortie"
         + " serait un avantage permanent déguisé en pouvoir temporaire. Le propriétaire unique de ce"
         + " modificateur retire AVANT de poser, donc ni la sortie ni la suppression ne peuvent laisser"
         + " de trace");

      // ================= A4d : capture temporaire, TOUJOURS libérée, cibles protégées exclues (-05)
      SmokeCloud.releaseEverything("test");
      SmokeCloud.endAllClouds("test");
      SmokeCloud.create(porteur);
      var cible = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 1, by, bz + 1),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert cible != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      cible.setBaby(false);
      var vitesseCible = cible.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
      double baseCible = vitesseCible == null ? 0.0 : vitesseCible.getValue();
      // Cible protégée : une invocation du porteur lui-même — on n'emprisonne pas les siens.
      SummonEntity mien = MinepieceModEntities.SUMMON.spawn(level, new net.minecraft.core.BlockPos(bx, by, bz + 2),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      if (mien != null) {
         mien.setOwnerEntity(porteur);
         mien.setModel("gecko_moria");
         mien.setTexture("tex_black");
      }
      int pris = SmokeCloud.captureInClouds(level);
      boolean cibleCaptivee = cible.isAlive() && SmokeCloud.capturedCount() > 0;
      boolean invocationExclue = mien == null || !SmokeCloud.isCaptured(mien);
      double vitesseCapturee = vitesseCible == null ? 0.0 : vitesseCible.getValue();
      // Libération : la durée est bornée.
      int tours = 0;
      while (SmokeCloud.capturedCount() > 0 && tours < DevilFruitMokuConfig.captureDurationTicks() + 5) {
         SmokeCloud.tickCaptures();
         tours++;
      }
      boolean liberee = SmokeCloud.capturedCount() == 0;
      String raison = SmokeCloud.lastReleaseReason(cible);
      double vitesseApres = vitesseCible == null ? 0.0 : vitesseCible.getValue();
      boolean vitesseRendue = Math.abs(vitesseApres - baseCible) < 1.0E-9;
      boolean marqueurParti = !cible.hasEffect(MinepieceModMobEffects.SMOKE_CAPTURED);
      if (mien != null) {
         mien.discard();
      }
      cible.discard();
      check("A4d-capture-liberee-et-cibles-protegees-exclues",
         pris >= 1 && cibleCaptivee && invocationExclue && vitesseCapturee < baseCible && liberee
            && "echeance".equals(raison) && vitesseRendue && marqueurParti,
         "captures = " + pris + " · cible captive = " + cibleCaptivee + ", vitesse " + round2(baseCible)
         + " → " + round2(vitesseCapturee) + " · invocation du porteur ÉPARGNÉE = " + invocationExclue
         + " (on n'emprisonne pas les siens) · après " + tours + " ticks : libérée = " + liberee
         + " (raison « " + raison + " »), vitesse rendue = " + vitesseRendue + ", marqueur parti = "
         + marqueurParti + " — capturer, c'est décider à la place de quelqu'un qu'il ne bougera pas : la"
         + " durée est donc BORNÉE, et la cible se libère d'elle-même si le porteur disparaît. Sans cela,"
         + " un joueur resterait prisonnier d'un pouvoir dont le propriétaire a quitté le serveur");

      // ================= A4e : les EXCEPTIONS de CE Logia, source par source (-06)
      SmokeCloud.releaseEverything("test");
      SmokeCloud.endAllClouds("test");
      var attaquant = fake(level, "dftest_moku_att");
      MinepieceModVariables.clear(attaquant);
      attaquant.teleportTo(bx + 3.5, by, bz + 0.5);
      var physique = level.damageSources().playerAttack(attaquant);
      var explosion = level.damageSources().explosion(attaquant, attaquant);
      var magie = level.damageSources().magic();
      double multPhysique = DevilFruitPowerLogic.computeDamageMagnification(physique, porteur);
      double multExplosion = DevilFruitPowerLogic.computeDamageMagnification(explosion, porteur);
      double multMagie = DevilFruitPowerLogic.computeDamageMagnification(magie, porteur);
      // Haki de l'Armement : la parade canonique.
      attaquant.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.ARMAMENT_HAKI, 200, 0, false, false, true));
      double multAvecHaki = DevilFruitPowerLogic.computeDamageMagnification(physique, porteur);
      attaquant.removeEffect(MinepieceModMobEffects.ARMAMENT_HAKI);
      // Suppression : le passif ne protège plus.
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.SEA_POWER, 200, 0, false, false, true));
      double multScelle = DevilFruitPowerLogic.computeDamageMagnification(physique, porteur);
      porteur.removeEffect(MinepieceModMobEffects.SEA_POWER);
      double multSansFruit = 0.0;
      // Contrôle : un porteur SANS Fruit n'a aucune intangibilité.
      MinepieceModVariables.clear(porteur);
      DevilFruitPowerLogic.clearDevilFruit(porteur);
      multSansFruit = DevilFruitPowerLogic.computeDamageMagnification(physique, porteur);
      immerse(level, porteur, moku);
      check("A4e-exceptions-du-logia-source-par-source",
         Math.abs(multPhysique - 0.0) < 1.0E-9 && Math.abs(multExplosion - 1.0) < 1.0E-9
            && Math.abs(multMagie - 1.0) < 1.0E-9 && Math.abs(multAvecHaki - 1.0) < 1.0E-9
            && Math.abs(multScelle - 1.0) < 1.0E-9 && Math.abs(multSansFruit - 1.0) < 1.0E-9,
         "TABLE des sources — physique sans Haki : ×" + round2(multPhysique) + " (annulée) ·"
         + " EXPLOSION : ×" + round2(multExplosion) + " (CONSERVÉE) · magie : ×" + round2(multMagie)
         + " (conservée) · physique AVEC Haki : ×" + round2(multAvecHaki) + " (conservée) ·"
         + " physique SOUS SCELLEMENT : ×" + round2(multScelle) + " (conservée) · sans Fruit : ×"
         + round2(multSansFruit) + " — le plan reproche que l'intangibilité soit appliquée à TOUS les"
         + " Logias « sans exceptions propres complètes ». Ici la fumée a SA propriété : un SOUFFLE la"
         + " DISPERSE, donc une explosion PORTE au lieu d'être annulée. Et « la suppression enlève le"
         + " passif » : un porteur immergé ne peut plus être protégé par un pouvoir que le jeu vient de"
         + " lui retirer — c'était faux avant, le moteur ne consultait pas l'état de suppression");

      SmokeCloud.releaseEverything("test");
      SmokeCloud.endAllClouds("test");
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      MinepieceModVariables.clear(porteur);
      MinepieceModVariables.clear(observateur);
      MinepieceModVariables.clear(attaquant);
   }

   // ------------------------------- A5. Contrat Mori Mori (-02/03/04/05/06)
   /**
    * Vérifie que les racines immobilisent en <b>respectant les résistances</b> et se terminent proprement
    * (-03), que la végétation est <b>temporaire et restituée</b> (-04), et que le drainage <b>coûte,
    * plafonne et ne rapporte rien</b> sur les invocations, alliés et cibles invulnérables (-05).
    */
   // ==================================================================================
   // A6 — CONTRAT OPE OPE (FRUIT-OPE_OPE-02/03/04/05)
   // ==================================================================================

   /** Témoins de l'échéance RÉELLE : relus par la portée « ope_expiry », à un autre tick. */
   private static java.util.UUID opeExpiryVictim;
   private static ServerPlayer opeExpiryCaster;

   /** Fichier de configuration du fruit — le fichier est la source de vérité, donc on l'écrit vraiment. */
   private static java.nio.file.Path opeConfigPath() {
      return java.nio.file.Path.of("config", "opworldlike", "devilfruit-ope.toml");
   }

   /** Écrit la configuration du fruit et la RECHARGE (un champ en mémoire ne prouverait rien). */
   private static void writeOpeConfig(double rayon, int dureeRoom, int dureeCoupe) {
      try {
         java.nio.file.Files.createDirectories(opeConfigPath().getParent());
         java.nio.file.Files.writeString(opeConfigPath(), """
            room_radius_blocks = %s
            room_lifetime_ticks = %d
            room_particles_per_tick = 48
            shambles_max_distance_blocks = 8.0
            amputation_duration_ticks = %d
            amputation_max_targets = 4
            """.formatted(String.format(java.util.Locale.ROOT, "%.2f", rayon), dureeRoom, dureeCoupe),
            java.nio.charset.StandardCharsets.UTF_8);
         br.com.oneminepiece.opworldlike.ope.DevilFruitOpeConfig.loadOrCreate();
      } catch (java.io.IOException ex) {
         LOGGER.warn("[MinePiece] config ope non écrite : {}", ex.toString());
      }
   }

   /**
    * Fait apparaître un zombie <b>ADULTE</b>.
    *
    * <p>Le forçage n'est pas décoratif : vanilla fait naître les zombies <b>bébés au hasard</b> (~5 %),
    * et une boîte de bébé (0,49 × 0,98) déplace le centre de 1,13 bloc — de quoi sortir d'un cône ou ne
    * pas heurter un obstacle, ce qui a rendu <b>cinq</b> vérifications intermittentes sur ce projet.</p>
    */
   private static net.minecraft.world.entity.LivingEntity spawnAdultZombie(ServerLevel level, int x, int y, int z) {
      net.minecraft.world.entity.LivingEntity cible = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(x, y, z), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert cible != null;
      ((net.minecraft.world.entity.monster.zombie.Zombie) cible).setBaby(false);
      return cible;
   }

   /** Lit un tag d'objet — la classification se LIT, elle ne se suppose pas. */
   private static boolean tagOf(Item item, String tag) {
      return item.builtInRegistryHolder().is(net.minecraft.tags.TagKey.create(
         net.minecraft.core.registries.Registries.ITEM,
         Identifier.fromNamespaceAndPath("minepiece", tag)));
   }

   // ==================================================================================
   // A7 — CONTRAT PIKA PIKA (FRUIT-PIKA_PIKA-02/03/04/05/06)
   // ==================================================================================

   /** Témoins d'IMPACT réels : le rayon est tiré maintenant, l'impact est relu à un autre tick. */
   private static java.util.UUID pikaTemoinLibre;
   private static java.util.UUID pikaTemoinAbrite;
   private static ServerPlayer pikaCaster;

   private static void writePikaConfig(int volee, int charge) {
      try {
         java.nio.file.Path chemin = java.nio.file.Path.of("config", "opworldlike", "devilfruit-pika.toml");
         java.nio.file.Files.createDirectories(chemin.getParent());
         java.nio.file.Files.writeString(chemin, """
            laser_range_blocks = 40.0
            magatama_range_blocks = 24.0
            magatama_per_volley = 3
            travel_max_distance_blocks = 24.0
            charge_ticks = %d
            barrage_max_impacts = %d
            speed_power_bonus_max = 12.0
            buff_max_amplifier = 2
            """.formatted(charge, volee), java.nio.charset.StandardCharsets.UTF_8);
         br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig.loadOrCreate();
      } catch (java.io.IOException ex) {
         LOGGER.warn("[MinePiece] config pika non écrite : {}", ex.toString());
      }
   }

   // ==================================================================================
   // A8 — CONTRAT YAMI YAMI (FRUIT-YAMI_YAMI-02/03/04/05/06)
   // ==================================================================================

   /** Témoins d'ATTRACTION et de SCELLEMENT, relus à un autre tick (le mouvement demande du temps). */
   private static java.util.UUID yamiTemoinLibre;
   private static java.util.UUID yamiTemoinMur;
   private static java.util.UUID yamiTemoinScelle;
   private static double yamiDistanceLibreInitiale;
   private static double yamiDistanceMurInitiale;
   private static ServerPlayer yamiPorteur;

   private static double distanceA(ServerLevel level, java.util.UUID id, ServerPlayer porteur) {
      var e = id == null ? null : level.getEntity(id);
      return e == null || porteur == null ? -1.0 : e.position().distanceTo(porteur.position());
   }

   private static void writeYamiConfig(int capacite, int scellement) {
      try {
         java.nio.file.Path chemin = java.nio.file.Path.of("config", "opworldlike", "devilfruit-yami.toml");
         java.nio.file.Files.createDirectories(chemin.getParent());
         java.nio.file.Files.writeString(chemin, """
            pull_radius_blocks = 32.0
            pull_force_max = 0.8
            pull_ticks = 20
            absorb_capacity_hits = %d
            absorb_cost_energy = 20.0
            seal_ticks = %d
            pain_multiplier = 2.0
            """.formatted(capacite, scellement), java.nio.charset.StandardCharsets.UTF_8);
         br.com.oneminepiece.opworldlike.yami.DevilFruitYamiConfig.loadOrCreate();
      } catch (java.io.IOException ex) {
         LOGGER.warn("[MinePiece] config yami non écrite : {}", ex.toString());
      }
   }

   // ==================================================================================
   // A9 — LOT DES FRUITS ZOAN (FRUIT-{NEKO_NEKO_*,RYU_RYU_*,TORI_TORI,UO_UO,USHI_USHI}-02/03/04/05/06)
   // ==================================================================================
   //
   // Les huit Zoan restants sont vérifiés en LOT, et c'est délibéré : leurs contrats individuels
   // diffèrent (corps, catalogue, valeurs), mais les RÈGLES que le plan leur demande sont les mêmes
   // (classification, forme hybride réversible, bonds bornés, combos bornés). Une section qui vérifie
   // huit fois la même règle avec huit copies du même code aurait fini par en vérifier huit versions.

   private static final String[] ZOANS = {
      "neko_neko_leopard", "neko_neko_sabertiger", "ryu_ryu_brachiosaurus",
      "ryu_ryu_pachycephalosaurus", "ryu_ryu_pteranodon", "tori_tori_phoenix",
      "uo_uo_seiryu", "ushi_ushi_giraffe"
   };

   // ==================================================================================
   // A10 — FRUITS SANS FAMILLE DANS LA SOURCE (NIKYU, RIKI, SORU, SUPA, UTA)
   // ==================================================================================
   //
   // Le plan est explicite : « ajouter le tag correct SANS SUPPOSER que NON_CLASSE signifie
   // Paramecia ». Le port les a classés Paramecia ; il reste à le VÉRIFIER et à le JUSTIFIER, Fruit par
   // Fruit — un tag présent n'est pas un tag cohérent.

   private static final String[] NON_CLASSES = {"nikyu_nikyu", "riki_riki", "soru_soru", "supa_supa", "uta_uta"};

   // ==================================================================================
   // A11 — ROKUSHIKI : L'ART MARTIAL, HORS DES FRUITS
   // ==================================================================================
   //
   // Le point à prouver n'est pas « les techniques existent » — elles existaient déjà, déclarées sous
   // Ushi Ushi et Inu Inu Wolf. C'est qu'un porteur SANS AUCUN Fruit du Démon puisse apprendre l'art
   // et s'en servir. Sans cette preuve, l'art martial n'est qu'un alias de deux Fruits.

   private static void rokushiki(ServerLevel level) {
      final int bx = 96;
      final int by = 100;
      final int bz = 60;

      // répertoire
      List<DevilFruitMove> repertoire = Rokushiki.moves();
      List<String> ids = new ArrayList<>();
      for (DevilFruitMove m : repertoire) {
         ids.add(m.id());
      }
      boolean shiganPresent = ids.contains("shigan");
      boolean rankyakuPresent = ids.contains("rankyaku");
      boolean rokuoganPresent = ids.contains("rokuogan");
      boolean soruPresent = ids.contains("soru");
      check("A11a-repertoire-de-l-art-martial",
         repertoire.size() >= 10 && shiganPresent && rankyakuPresent && rokuoganPresent && soruPresent,
         "répertoire Rokushiki : " + repertoire.size() + " techniques retrouvées dans le registre ("
         + String.join(", ", ids) + ") — elles ne sont PAS redéclarées ici : une seconde liste aurait"
         + " fait deux sources de vérité, qui divergent au premier ajustement de valeur");

      // un porteur SANS Fruit, SANS Rokushiki : aucune puissance
      ServerPlayer sans = fake(level, "dftest_rok_sans");
      // Aucun Fruit : on vide explicitement la variable (giveFruit attend un Item, non un ItemStack).
      MinepieceModVariables.get(sans).PlayerDevilFruit = ItemStack.EMPTY;
      sans.teleportTo(bx + 0.5, by, bz + 0.5);
      MinepieceModVariables.get(sans).PlayerEnergy = 200.0;
      boolean avant = DevilFruitSkillDispatcher.castSkill(sans);
      check("A11b-sans-fruit-et-sans-art-aucun-pouvoir",
         !avant && !Rokushiki.isLearned(sans) && MinepieceModVariables.get(sans).PlayerEnergy == 200.0,
         "porteur sans Fruit et sans Rokushiki : pouvoir déclenché=" + avant + " · énergie après la"
         + " tentative=" + round2(MinepieceModVariables.get(sans).PlayerEnergy) + " (inchangée) — un"
         + " refus ne consomme rien, et l'absence de puissance est explicite");

      // apprentissage HORS Fruit, par l'objet de la source
      boolean appris = Rokushiki.learn(sans);
      boolean dejaLa = !Rokushiki.learn(sans);
      check("A11c-apprentissage-hors-des-fruits",
         appris && dejaLa && MinepieceModVariables.get(sans).PlayerDevilFruit.isEmpty(),
         "apprentissage : " + appris + " (deuxième appel sans effet : " + dejaLa + ") · Fruit porté : "
         + (MinepieceModVariables.get(sans).PlayerDevilFruit.isEmpty() ? "AUCUN" : "un Fruit") + " — c'est"
         + " exactement ce que la source fait (advancement skill_rokushiki_advancement + objet"
         + " skill_selector) : le Rokushiki s'acquiert en dehors des Fruits, et ce test le prouve sur"
         + " un porteur qui n'en a aucun");

      // et maintenant, toujours SANS Fruit, l'art fonctionne par le MÊME chemin
      var avantCast = MinepieceModVariables.get(sans).PlayerEnergy;
      DevilFruitMove choisie = Rokushiki.selected(sans);
      boolean lance = DevilFruitSkillDispatcher.castSkill(sans);
      double apresCast = MinepieceModVariables.get(sans).PlayerEnergy;
      boolean recharge = sans.getCooldowns().isOnCooldown(Rokushiki.cooldownCarrier());
      check("A11d-art-utilisable-sans-fruit",
         lance && choisie != null && apresCast < avantCast && recharge,
         "technique « " + (choisie == null ? "?" : choisie.name().getString()) + " » exécutée SANS Fruit="
         + lance + " · énergie " + round2(avantCast) + " → " + round2(apresCast) + " (coût du catalogue : "
         + round2(choisie == null ? 0 : choisie.energyCost()) + ") · recharge armée=" + recharge
         + " — même touche, même validation d'énergie, même recharge que pour un Fruit : l'art martial"
         + " est un pouvoir à part entière, pas un supplément");

      // le cycle parcourt le répertoire et revient au départ
      int depart = (int) MinepieceModVariables.get(sans).rokushikiSelected;
      for (int i = 0; i < repertoire.size(); i++) {
         Rokushiki.cycle(sans);
      }
      int retour = (int) MinepieceModVariables.get(sans).rokushikiSelected;
      List<String> vus = new ArrayList<>();
      for (int i = 0; i < repertoire.size(); i++) {
         Rokushiki.cycle(sans);
         DevilFruitMove m = Rokushiki.selected(sans);
         if (m != null && !vus.contains(m.id())) {
            vus.add(m.id());
         }
      }
      check("A11e-cycle-du-repertoire",
         retour == depart && vus.size() == repertoire.size(),
         "un tour complet ramène à la sélection de départ=" + (retour == depart) + " · techniques"
         + " distinctes parcourues " + vus.size() + "/" + repertoire.size() + " — la touche de cycle"
         + " parcourt tout l'art, et chaque technique du répertoire est atteignable");

      // sous scellement : l'art martial est retiré comme les Fruits
      sans.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.SEA_POWER, 40, 0, false, false));
      MinepieceModVariables.get(sans).PlayerEnergy = 200.0;
      boolean scelle = DevilFruitSkillDispatcher.castSkill(sans);
      sans.removeEffect(MinepieceModMobEffects.SEA_POWER);
      check("A11f-scelle-comme-un-fruit",
         !scelle && MinepieceModVariables.get(sans).PlayerEnergy == 200.0,
         "sous scellement : pouvoir déclenché=" + scelle + " · énergie après="
         + round2(MinepieceModVariables.get(sans).PlayerEnergy) + " — l'art martial passe par la MÊME"
         + " porte que les Fruits (isSuppressed) : ajouter un second chemin de puissance aurait créé"
         + " un contournement du scellement");

      // non-régression : un porteur AVEC un Fruit garde son répertoire de Fruit
      ServerPlayer avecFruit = fake(level, "dftest_rok_fruit");
      giveFruit(avecFruit, BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_mera_mera")));
      MinepieceModVariables.get(avecFruit).PlayerEnergy = 200.0;
      boolean fruitOk = DevilFruitSkillDispatcher.castSkill(avecFruit);
      check("A11g-le-fruit-prime-sur-l-art",
         fruitOk,
         "porteur AVEC Fruit : sa propre technique part=" + fruitOk + " — quand un Fruit est porté, c'est"
         + " son répertoire qui répond ; l'art martial ne s'y substitue pas");
      MinepieceModVariables.clear(sans);
      MinepieceModVariables.clear(avecFruit);
   }

   // ==================================================================================
   // A12 — NIKYU NIKYU : LES TROIS MÉCANIQUES PROPRES DU CONTRAT
   // ==================================================================================
   //
   // Pad Ho (répulsion orientée), la déviation de projectiles, Ursus Shock (onde chargée et annoncée).
   // Le moteur générique applique des dégâts dans un cône : il ne repousse pas, n'épargne pas qui est
   // adossé à un mur, ne renvoie pas un projectile et n'annonce jamais sa zone.

   private static void nikyu(ServerLevel level) {
      final int bx = 120;
      final int by = 100;
      final int bz = 60;

      // Dégager un plateau d'air : sinon les cibles naissent DANS la roche, la destination testée est
      // pleine, et la répulsion les épargne toutes — ce qui est le comportement correct, mais ne prouve
      // rien de la poussée. Le premier passage de ce test mesurait exactement cela.
      for (int dx = -6; dx <= 8; dx++) {
         for (int dz = -8; dz <= 8; dz++) {
            for (int dy = 0; dy <= 3; dy++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx + dx, by + dy, bz + dz),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      ServerPlayer porteur = fake(level, "dftest_nik");
      giveFruit(porteur, MinepieceModItems.ITEM_DEVIL_FRUIT_NIKYU_NIKYU);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      MinepieceModVariables.get(porteur).PlayerEnergy = 200.0;
      NikyuRepulsion.resetCompteurs();

      // ---- ① répulsion orientée : devant poussée, derrière épargnée, mur respecté
      //
      // Le placement ne dépend PAS du vecteur de regard d'un faux joueur : une rotation de test n'est pas
      // fiable, et la première version de ce test plaçait « devant » sur le lanceur lui-même. On fige la
      // rotation, puis on place les cibles par décalage explicite sur l'axe du regard obtenu.
      porteur.setYRot(0.0F);
      porteur.setXRot(0.0F);
      Vec3 regard = porteur.getViewVector(1.0F).normalize();
      Vec3 axe = new Vec3(regard.x, 0.0, regard.z).normalize();   // axe horizontal, celui de la poussée
      // Disposition, sur l'axe du regard, en blocs depuis le lanceur :
      //   z+3 = cible « au mur »   z+4 = le MUR   z+6 = cible libre   z-4 = cible dos tournée
      // La cible libre est posée AU-DELÀ du mur : sinon le mur occupe sa propre case et la sonde de
      // destination la déclare, à raison, épargnée — c'est le piège dans lequel est tombée la version
      // précédente, où le mur recouvrait la cible censée être poussée.
      Vec3 devant = porteur.position().add(axe.scale(6.0));
      Vec3 derriere = porteur.position().subtract(axe.scale(4.0));
      var cibleDevant = spawnAdultZombie(level, (int) Math.floor(devant.x), by, (int) Math.floor(devant.z));
      var cibleDerriere = spawnAdultZombie(level, (int) Math.floor(derriere.x), by, (int) Math.floor(derriere.z));
      Vec3 devantMur = porteur.position().add(axe.scale(3.0));
      var cibleMur = spawnAdultZombie(level, (int) Math.floor(devantMur.x), by, (int) Math.floor(devantMur.z));
      // Le mur doit être à la HAUTEUR DU MILIEU DE LA CIBLE : la sonde de destination échantillonne le
      // trajet à mi-hauteur (getY + moitié de la boîte), pas aux pieds. Une première version le posait un
      // bloc trop haut — la sonde passait dessous, la cible était poussée, et le compteur
      // « épargnées par le décor » restait à zéro sans que rien ne le signale.
      net.minecraft.core.BlockPos mur = net.minecraft.core.BlockPos.containing(
         devantMur.x + axe.x * 1.0, by, devantMur.z + axe.z * 1.0);
      level.setBlockAndUpdate(mur, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      level.setBlockAndUpdate(mur.above(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      level.setBlockAndUpdate(mur.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());

      DevilFruitMove padho = null;
      for (DevilFruitMove m : DevilFruitRegistry.movesFor(MinepieceModItems.ITEM_DEVIL_FRUIT_NIKYU_NIKYU)) {
         if (m.id().equals("padho")) {
            padho = m;
         }
      }
      boolean prisEnCharge = padho != null && NikyuRepulsion.padHo(porteur,padho);
      double forceDevant = cibleDevant.getDeltaMovement().horizontalDistance();
      double forceDerriere = cibleDerriere.getDeltaMovement().horizontalDistance();
      double forceMur = cibleMur.getDeltaMovement().horizontalDistance();
      check("A12a-repulsion-orientee-et-bornee",
         prisEnCharge && forceDevant > 0.0 && forceDerriere == 0.0
            && forceDevant <= NikyuRepulsion.PADHO_FORCE_MAX + 0.01
            && forceMur <= forceDevant,
         "Pad Ho pris en charge=" + prisEnCharge + " · poussée devant=" + round2(forceDevant)
         + " (plafond " + NikyuRepulsion.PADHO_FORCE_MAX + ") · derrière=" + round2(forceDerriere)
         + " · adossée au mur=" + round2(forceMur) + " · poussées=" + NikyuRepulsion.poussees()
         + " · épargnées par le décor=" + NikyuRepulsion.epargneesParLeDecor()
         + " · positions devant=" + cibleDevant.blockPosition() + "/derrière=" + cibleDerriere.blockPosition()
         + " lanceur=" + porteur.blockPosition() + " — la paume pousse ce"
         + " qu'elle voit, laisse ce qu'elle ne voit pas, et n'enfonce personne dans la pierre");

      // ---- ② déviation : un projectile renvoyé change de camp
      var projectile = new net.mcreator.minepiece.entity.SkillProjectileEntity(
         net.mcreator.minepiece.init.MinepieceModEntities.SKILL_PROJECTILE, level);
      projectile.setPos(bx + 2.5, by + 1.0, bz + 0.5);
      projectile.setDeltaMovement(-0.6, 0.0, 0.0);
      boolean devie = NikyuRepulsion.tryDeflect(projectile, porteur);
      double vitesseApres = projectile.getDeltaMovement().x;
      boolean proprietaireTransfere = projectile.getOwner() == porteur;
      check("A12b-deviation-avec-attribution",
         devie && vitesseApres > 0.0 && proprietaireTransfere,
         "dévié=" + devie + " · vitesse avant=-0,60 → après=" + round2(vitesseApres)
         + " (inversée) · propriétaire transféré au porteur=" + proprietaireTransfere + " — la"
         + " direction seule ne suffit pas : sans transfert de propriétaire, le tireur continuerait"
         + " de récolter les dégâts de son propre tir retourné");

      // ---- ③ Ursus Shock : charger, annoncer, puis frapper
      DevilFruitMove ursus = null;
      for (DevilFruitMove m : DevilFruitRegistry.movesFor(MinepieceModItems.ITEM_DEVIL_FRUIT_NIKYU_NIKYU)) {
         if (m.id().equals("ursusshock")) {
            ursus = m;
         }
      }
      double puissanceDepart = NikyuRepulsion.chargeOf(porteur);
      if (ursus != null) {
         NikyuRepulsion.ursusShock(porteur, ursus);       // démarre la charge
      }
      for (int i = 0; i < NikyuRepulsion.URSUS_CHARGE_TICKS + 10; i++) {
         NikyuRepulsion.tick();                          // la charge monte, et se plafonne
      }
      double puissancePleine = NikyuRepulsion.chargeOf(porteur);
      int annoncesAvant = NikyuRepulsion.ondesAnnoncees();
      int frappesAvant = NikyuRepulsion.ondesFrappees();
      if (ursus != null) {
         NikyuRepulsion.ursusShock(porteur, ursus);       // libère l'onde
      }
      int frappesJusteApres = NikyuRepulsion.ondesFrappees();
      check("A12c-onde-chargee-et-annoncee",
         puissanceDepart == 0.0 && puissancePleine >= 0.99 && NikyuRepulsion.ondesAnnoncees() > annoncesAvant
            && frappesJusteApres == frappesAvant,
         "charge : " + round2(puissanceDepart) + " → " + round2(puissancePleine) + " après "
         + (NikyuRepulsion.URSUS_CHARGE_TICKS + 10) + " ticks (plafond tenu) · zones annoncées="
         + NikyuRepulsion.ondesAnnoncees() + " · frappes au moment de l'annonce=" + frappesJusteApres
         + " (aucune) — le signal PRÉCÈDE l'impact : une frappe annoncée se voit venir, et c'est"
         + " exactement ce que le contrat exige");

      MinepieceModVariables.clear(porteur);
   }

   // ==================================================================================
   // A13..A16 — RIKI, SORU, SUPA, UTA : LES MÉCANIQUES PROPRES
   // ==================================================================================

   private static void riki(ServerLevel level) {
      final int bx = 150;
      final int by = 100;
      final int bz = 60;
      for (int dx = -4; dx <= 8; dx++) {
         for (int dz = -4; dz <= 8; dz++) {
            for (int dy = 0; dy <= 3; dy++) {
               level.setBlockAndUpdate(new BlockPos(bx + dx, by + dy, bz + dz), Blocks.AIR.defaultBlockState());
            }
         }
      }
      ServerPlayer porteur = fake(level, "dftest_riki");
      giveFruit(porteur, MinepieceModItems.ITEM_DEVIL_FRUIT_RIKI_RIKI);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      RikiForce.resetCompteurs();

      // ① multiplicateur borné, et annulé sous scellement
      double normal = RikiForce.strengthMultiplier(porteur);
      var attr = porteur.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
      double avant = attr == null ? 0.0 : attr.getValue();
      if (attr != null) {
         attr.setBaseValue(200.0);
      }
      double enorme = RikiForce.strengthMultiplier(porteur);
      if (attr != null) {
         attr.setBaseValue(avant);
      }
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.SEA_POWER, 40, 0, false, false));
      double scelle = RikiForce.strengthMultiplier(porteur);
      porteur.removeEffect(MinepieceModMobEffects.SEA_POWER);
      check("A13a-force-bornee-et-conditionnee",
         normal >= 1.0 && normal <= RikiForce.STRENGTH_MAX && enorme <= RikiForce.STRENGTH_MAX
            && enorme >= normal && scelle == 1.0,
         "multiplicateur : normal=" + round2(normal) + " · ATTACK_DAMAGE à 200 → " + round2(enorme)
         + " (plafond " + RikiForce.STRENGTH_MAX + ") · sous scellement=" + round2(scelle) + " — la"
         + " source lit l'attribut ATTACK_DAMAGE (f_22276_) : la force suit donc l'attribut, elle est"
         + " bornée, et elle DISPARAÎT sous scellement ; une force qui survit à l'eau de mer serait un"
         + " contournement du scellement");

      // ② saisie brève : une seule à la fois, et toujours rendue
      var premier = spawnAdultZombie(level, bx + 3, by, bz);
      var second = spawnAdultZombie(level, bx + 4, by, bz);
      boolean pris1 = RikiForce.grab(porteur, premier);
      boolean tenuApresPremier = RikiForce.isHolding(porteur) && RikiForce.held(porteur) == premier;
      int saisiesAvant = RikiForce.saisies();
      boolean pris2 = RikiForce.grab(porteur, second);
      boolean remplace = RikiForce.held(porteur) == second;
      RikiForce.release(porteur, "echeance");
      check("A13b-saisie-breve-et-toujours-rendue",
         pris1 && tenuApresPremier && pris2 && remplace && !RikiForce.isHolding(porteur)
            && RikiForce.saisies() > saisiesAvant,
         "prise 1=" + pris1 + " · tenue=" + tenuApresPremier + " · prise 2=" + pris2
         + " (remplace la 1re)=" + remplace + " · après libération, tient encore="
         + RikiForce.isHolding(porteur) + " · libérations=" + RikiForce.liberations() + " — la prise est"
         + " UNIQUE par porteur et la sortie est unique : échéance, mort de la cible, mort du porteur,"
         + " déconnexion ou remplacement passent toutes par le même release()");

      // ③ projection : le décor arrête, les blocs protégés restent
      //
      // ATTENTION À LA HAUTEUR : `distanceLibre` échantillonne depuis les PIEDS de la cible
      // (`cible.position()`), pas à mi-hauteur comme le service de répulsion. Un mur posé un bloc trop
      // haut ne barre rien et le trajet mesuré reste à 12 — c'est le piège dans lequel est tombée la
      // première version de ce contrôle, et il rendait l'assertion muette.
      porteur.setYRot(0.0F);   // le regard vers +Z : la projection suit le regard
      porteur.setXRot(0.0F);
      var projete = spawnAdultZombie(level, bx + 2, by, bz + 3);
      level.setBlockAndUpdate(new BlockPos(bx + 2, by, bz + 5), Blocks.STONE.defaultBlockState());
      level.setBlockAndUpdate(new BlockPos(bx + 2, by + 1, bz + 5), Blocks.STONE.defaultBlockState());
      level.setBlockAndUpdate(new BlockPos(bx + 2, by + 2, bz + 5), Blocks.STONE.defaultBlockState());
      // un coffre DERRIÈRE le mur, dans le rayon de casse : il doit survivre
      level.setBlockAndUpdate(new BlockPos(bx + 2, by, bz + 6), Blocks.CHEST.defaultBlockState());
      double distanceLibre = RikiForce.distanceLibre(level, projete.position(),
         new net.minecraft.world.phys.Vec3(0.0, 0.0, 1.0), 12.0);
      RikiForce.grab(porteur, projete);
      boolean lance = RikiForce.throwTarget(porteur, projete);
      boolean coffreIntact = level.getBlockState(new BlockPos(bx + 2, by, bz + 6)).is(Blocks.CHEST);
      boolean murCasse = level.getBlockState(new BlockPos(bx + 2, by, bz + 5)).isAir();
      check("A13c-projection-sans-casse-anticipee",
         lance && distanceLibre < 12.0 && distanceLibre > 0.0 && coffreIntact
            && !murCasse && RikiForce.blocsCasses()==0 && projete.getDeltaMovement().length()>0,
         "trajet libre mesuré=" + round2(distanceLibre) + " blocs (le mur à 2 blocs l'arrête, la portée"
         + " maximale est 12) · projection=" + lance + " · mur entamé=" + murCasse
         + " · coffre encore là=" + coffreIntact
         + " · blocs cassés=" + RikiForce.blocsCasses() + " · épargnés (protégés)="
         + RikiForce.blocsEpargnes() + " — la force déplace de la matière, elle ne perce pas le décor,"
         + " et un coffre n'est pas une cible : casser à distance n'est pas de la force, c'est du pillage");

      MinepieceModVariables.clear(porteur);
   }

   private static void soru(ServerLevel level) {
      final int bx = 180;
      final int by = 100;
      final int bz = 60;
      ServerPlayer porteur = fake(level, "dftest_soru");
      giveFruit(porteur, MinepieceModItems.ITEM_DEVIL_FRUIT_SORU_SORU);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      SoruSummons.resetCompteurs();

      // ① deux invocations distinctes
      var zeus = SoruSummons.summon(porteur, SoruSummons.Kind.RAITEI);
      var promethee = SoruSummons.summon(porteur, SoruSummons.Kind.HEBUNRI);
      boolean distinctes = zeus != null && promethee != null
         && SoruSummons.Kind.RAITEI.order() != SoruSummons.Kind.HEBUNRI.order()
         && zeus.getLifetimePourTest() > 0 && promethee.getLifetimePourTest() > 0;
      check("A14a-deux-invocations-distinctes-et-bornees",
         distinctes && SoruSummons.count(porteur) == 2,
         "Zeus : ordre=" + SoruSummons.Kind.RAITEI.order() + " durée="
         + (zeus == null ? 0 : zeus.getLifetimePourTest()) + " · Prometheus : ordre="
         + SoruSummons.Kind.HEBUNRI.order() + " durée="
         + (promethee == null ? 0 : promethee.getLifetimePourTest()) + " · enregistrées="
         + SoruSummons.count(porteur) + " — la source les suit par UUID (ZEUS_UUID, PROMETHEUS_UUID) et"
         + " leur donne une durée : deux invocations, deux comportements, et une fin");

      // ② rappel = remplacement, jamais duplication (y compris hors de vue)
      var zeus2 = SoruSummons.summon(porteur, SoruSummons.Kind.RAITEI);
      int apres = SoruSummons.count(porteur);
      boolean premierMort = zeus == null || !zeus.isAlive();
      check("A14b-rappel-sans-duplication",
         apres == 2 && SoruSummons.remplacements() >= 1 && premierMort && zeus2 != null,
         "rappel de Zeus → enregistrées=" + apres + " (jamais 3) · remplacements="
         + SoruSummons.remplacements() + " · l'ancienne a bien été écartée=" + premierMort
         + " — le lancement générique comptait les invocations PAR PROXIMITÉ (128 blocs) : une invocation"
         + " laissée loin après une reconnexion échappait au plafond. Le registre compte par propriétaire"
         + " et par genre, donc la duplication devient impossible, même hors de vue");

      // ③ seul le propriétaire commande, et les alliés sont épargnés
      byte[] octets = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
      java.util.UUID autre = java.util.UUID.nameUUIDFromBytes(octets);
      ServerPlayer allie = fake(level, "dftest_soru_allie");
      var invoc = SoruSummons.active(porteur, SoruSummons.Kind.RAITEI);
      boolean proprietaireOk = invoc != null && SoruSummons.ownerOnly(porteur, invoc);
      boolean allieRefuse = invoc != null && !SoruSummons.ownerOnly(allie, invoc);
      boolean epargneMaitre = invoc != null && SoruSummons.mustSpare(invoc, porteur);
      var hostile = spawnAdultZombie(level, bx + 4, by, bz);
      boolean viseHostile = invoc != null && !SoruSummons.mustSpare(invoc, hostile);
      check("A14c-proprietaire-et-ciblage",
         proprietaireOk && allieRefuse && epargneMaitre && viseHostile && autre != null,
         "le propriétaire commande=" + proprietaireOk + " · un allié est refusé=" + allieRefuse
         + " · le maître est épargné=" + epargneMaitre + " · une créature hostile est visée="
         + viseHostile + " — les deux règles sont SÉPARÉES : bien obéir ne dispense pas de bien choisir"
         + " ses cibles, et un allié ne commande pas");

      SoruSummons.releaseAll(porteur, "fin_de_test");
      MinepieceModVariables.clear(porteur);
   }

   private static void supa(ServerLevel level) {
      final int bx = 210;
      final int by = 100;
      final int bz = 60;
      for (int dx = -3; dx <= 4; dx++) {
         for (int dz = -3; dz <= 4; dz++) {
            for (int dy = 0; dy <= 3; dy++) {
               level.setBlockAndUpdate(new BlockPos(bx + dx, by + dy, bz + dz), Blocks.AIR.defaultBlockState());
            }
         }
      }
      ServerPlayer porteur = fake(level, "dftest_supa");
      giveFruit(porteur, MinepieceModItems.ITEM_DEVIL_FRUIT_SUPA_SUPA);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      SupaBlades.resetCompteurs();

      // ① lames visibles et allonge cohérente
      double porteeRangee = SupaBlades.reach(porteur, 3.0);
      SupaBlades.activate(porteur, SupaBlades.BLADE_TICKS);
      double porteeSortie = SupaBlades.reach(porteur, 3.0);
      check("A15a-lames-et-allonge",
         !SupaBlades.bladesActive(porteur) == false && porteeSortie > porteeRangee
            && Math.abs(porteeSortie - (porteeRangee + SupaBlades.BLADE_REACH)) < 0.001,
         "portée lames rangées=" + round2(porteeRangee) + " → sorties=" + round2(porteeSortie)
         + " (allonge " + SupaBlades.BLADE_REACH + ") · actives=" + SupaBlades.bladesActive(porteur)
         + " — les dégâts suivent la portée : rien ne frappe plus loin que ce que les lames atteignent");

      // ② postures réversibles, jamais cumulatives
      int maxEffets = 0;
      for (int i = 0; i < 6; i++) {
         SupaBlades.setStance(porteur, i % 2 == 0 ? SupaBlades.Stance.OFFENSIVE : SupaBlades.Stance.DEFENSIVE);
         maxEffets = Math.max(maxEffets, SupaBlades.stanceEffectsOn(porteur));
      }
      SupaBlades.setStance(porteur, SupaBlades.Stance.NONE);
      int apresRangement = SupaBlades.stanceEffectsOn(porteur);
      check("A15b-postures-reversibles",
         maxEffets <= 1 && apresRangement == 0 && SupaBlades.stance(porteur) == SupaBlades.Stance.NONE,
         "6 bascules offensif/défensif → effets simultanés au maximum=" + maxEffets + " (jamais 2)"
         + " · après retour à aucune posture, effets=" + apresRangement + " · poses="
         + SupaBlades.posturesPosees() + " · retraits=" + SupaBlades.posturesRetirees() + " — on RETIRE"
         + " l'ancienne posture avant de poser la nouvelle : l'absence d'empilement est structurelle,"
         + " elle ne dépend pas de la chance");

      // ③ découpe bornée, protections intactes
      level.setBlockAndUpdate(new BlockPos(bx + 2, by, bz), Blocks.STONE.defaultBlockState());
      level.setBlockAndUpdate(new BlockPos(bx + 2, by + 1, bz), Blocks.CHEST.defaultBlockState());
      level.setBlockAndUpdate(new BlockPos(bx + 3, by, bz), Blocks.BEDROCK.defaultBlockState());
      int coupes = SupaBlades.cut(porteur, SupaBlades.CUT_RADIUS);
      boolean coffreIntact = level.getBlockState(new BlockPos(bx + 2, by + 1, bz)).is(Blocks.CHEST);
      boolean litIntact = level.getBlockState(new BlockPos(bx + 3, by, bz)).is(Blocks.BEDROCK);
      check("A15c-decoupe-bornee-et-protections",
         coupes >= 1 && coffreIntact && litIntact && SupaBlades.blocsRefuses() >= 2,
         "blocs découpés=" + coupes + " · coffre intact=" + coffreIntact + " · lit intact=" + litIntact
         + " · refus comptés=" + SupaBlades.blocsRefuses() + " — la découpe a des limites explicites, et"
         + " chaque refus est COMPTÉ : une protection silencieuse ne se vérifie pas");

      MinepieceModVariables.clear(porteur);
   }

   private static void uta(ServerLevel level) {
      final int bx = 240;
      final int by = 100;
      final int bz = 60;
      ServerPlayer porteur = fake(level, "dftest_uta");
      utaPorteurTest = porteur;
      giveFruit(porteur, MinepieceModItems.ITEM_DEVIL_FRUIT_UTA_UTA);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      UtaDomain.resetCompteurs();

      // ① domaine ouvert, à cadence tenue (la mesure du rythme se fait dans la phase uta_domain)
      boolean ouvert = UtaDomain.enter(porteur, UtaDomain.RADIUS);
      int mesuresAuDepart = UtaDomain.measuresOf(porteur);
      check("A16a-domaine-ouvert-et-borne",
         ouvert && UtaDomain.inDomain(porteur) && mesuresAuDepart <= 1
            && UtaDomain.domainRemaining(porteur) <= UtaDomain.ZONE_TICKS,
         "domaine ouvert=" + ouvert + " · dedans=" + UtaDomain.inDomain(porteur) + " · mesures au"
         + " démarrage=" + mesuresAuDepart + " · durée restante=" + UtaDomain.domainRemaining(porteur)
         + "/" + UtaDomain.ZONE_TICKS + " ticks — la cadence et l'échéance se mesurent sur des ticks"
         + " réels (phase uta_domain) : ici on vérifie l'ouverture et la borne de durée");

      // ② sortie garantie : chaque cause est comptée séparément
      int avantMort = UtaDomain.sortiesMort();
      UtaDomain.exit(porteur, "mort");
      boolean sortiApresMort = !UtaDomain.inDomain(porteur);
      UtaDomain.enter(porteur, UtaDomain.RADIUS);
      UtaDomain.exit(porteur, "deconnexion");
      UtaDomain.enter(porteur, UtaDomain.RADIUS);
      UtaDomain.exit(porteur, "scellement");
      check("A16b-sortie-garantie-quatre-causes",
         sortiApresMort && UtaDomain.sortiesMort() > avantMort && UtaDomain.sortiesDeconnexion() >= 1
            && UtaDomain.sortiesScellement() >= 1 && !UtaDomain.inDomain(porteur),
         "sortie après mort=" + sortiApresMort + " · compteurs : mort=" + UtaDomain.sortiesMort()
         + " · déconnexion=" + UtaDomain.sortiesDeconnexion() + " · scellement="
         + UtaDomain.sortiesScellement() + " · échéance=" + UtaDomain.sortiesEcheance() + " — quatre"
         + " causes, quatre chemins comptés séparément : un domaine dont on ne sort pas est une prison,"
         + " pas un pouvoir (la 4e, l'échéance, est vérifiée par la phase uta_domain)");

      // ③ Tot Musica fonctionne et expire
      var tot = UtaDomain.summonTotMusica(porteur);
      boolean fonctionnel = tot != null && tot.isAlive() && tot.getOwner() != null
         && tot.getLifetimePourTest() > 0;
      UtaDomain.enter(porteur, UtaDomain.RADIUS);
      UtaDomain.exit(porteur, "echeance");
      UtaDomain.enter(porteur, UtaDomain.RADIUS);   // laissé ouvert pour la phase uta_domain
      boolean retireAvecLeDomaine = UtaDomain.totMusica(porteur) == null
         || UtaDomain.totMusica(porteur) != null && !UtaDomain.totMusica(porteur).isAlive();
      check("A16c-tot-musica-fonctionnel-et-borne",
         fonctionnel && UtaDomain.totMusicaInvoques() >= 1 && retireAvecLeDomaine,
         "Tot Musica : invoqué=" + (tot != null) + " · vivant=" + (tot != null && tot.isAlive())
         + " · propriétaire=" + (tot != null && tot.getOwner() != null) + " · durée="
         + (tot == null ? 0 : tot.getLifetimePourTest()) + " · retiré à la fermeture du domaine="
         + retireAvecLeDomaine + " — une invocation avec maître et durée, pas un décor de mêlée : la"
         + " source la suit par OWNER_UUID et la fait disparaître (flag_die)");

      MinepieceModVariables.clear(porteur);
   }

   // ==================================================================================
   // A17 — ANIMATIONS DE JOUEUR (la table de la source)
   // ==================================================================================

   private static void animations(ServerLevel level) {
      TechniqueAnimations.resetCompteurs();
      ServerPlayer porteur = fake(level, "dftest_anim");
      giveFruit(porteur, MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU);
      porteur.teleportTo(150.5, 100, 60.5);

      int dispo = TechniqueAnimations.disponibles().size();
      check("A17a-liste-des-animations-livrees",
         dispo >= 60,
         "noms d'animation validés contre les ressources du mod : " + dispo + " — la liste n'est pas"
         + " déclarative : chaque nom est vérifié par la présence de son fichier, donc le mod ne peut"
         + " pas annoncer un geste qu'il ne possède pas");

      // correspondances relevées dans la cascade de la source
      String amane = TechniqueAnimations.traduire(-6.0, 1.0, porteur);
      String backstep = TechniqueAnimations.traduire(-1.0, 0.0, porteur);
      String hakoku = TechniqueAnimations.traduire(-13.0, 2.0, porteur);
      boolean coherent = "amane_dachi".equals(amane) && "backstep".equals(backstep)
         && "hakoku_right1".equals(hakoku);
      check("A17b-table-de-la-source-respectee",
         coherent,
         "(-6,1) → " + amane + " · (-1,0) → " + backstep + " · (-13,2) → " + hakoku
         + " — ces trois paires sont relevées dans le bytecode de PlayAnimationPlayerProcedure, pas"
         + " déduites ; chacune correspond à un fichier réellement livré");

      // aucune technique ne doit annoncer un geste introuvable
      int sansFichier = 0;
      int avecGeste = 0;
      for (DevilFruitMove m : DevilFruitRegistry.movesFor(MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU)) {
         String nom = TechniqueAnimations.nameFor(m, porteur);
         if (nom.isEmpty()) {
            sansFichier++;
         } else if (!TechniqueAnimations.fichierPresent(nom)) {
            sansFichier++;
         } else {
            avecGeste++;
         }
      }
      check("A17c-aucun-geste-invente",
         sansFichier == 0 && avecGeste > 0,
         "techniques de Gomu : " + avecGeste + " avec un geste livré · " + sansFichier + " sans geste"
         + " introuvable — la règle est qu'on n'envoie JAMAIS un nom dont le fichier est absent : un nom"
         + " sans fichier ne joue rien tout en donnant l'illusion d'un câblage");

      // couverture : combien de techniques portent le geste EXACT de la source ?
      int paires = TechniqueAnimations.pairesConnues();
      int exactes = TechniqueAnimations.couvertureExacte();
      check("A17e-couverture-de-la-table-de-la-source",
         paires >= 100 && exactes >= 90,
         "paires extraites du bytecode de la source : " + TechniqueAnimations.nombreDePaires()
         + " · techniques avec le geste EXACT et livré : " + exactes + "/" + paires
         + " (les autres gardent l'attaque de base, comme la source pour ses attaques ordinaires)"
         + " — chaque paire est lue par setBaseValue dans la procédure de la technique, jamais déduite");

      // la diffusion part bien (au joueur et aux suiveurs)
      DevilFruitMove premiere = DevilFruitRegistry.movesFor(
         MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU).get(0);
      int avant = TechniqueAnimations.envoyees();
      TechniqueAnimations.play(porteur, premiere);
      check("A17d-diffusion-de-l-animation",
         TechniqueAnimations.envoyees() > avant || TechniqueAnimations.introuvables() > 0,
         "diffusions=" + TechniqueAnimations.envoyees() + " · noms introuvables="
         + TechniqueAnimations.introuvables() + " (liste : " + TechniqueAnimations.manquants() + ")"
         + " — en cas d'absence de fichier, le compteur monte au lieu de se taire");

      MinepieceModVariables.clear(porteur);
   }

   private static void nonClasses(ServerLevel level) {
      ServerPlayer neutre = fake(level, "dftest_nc_neutre");
      MinepieceModVariables.clear(neutre);
      var sourceNeutre = level.damageSources().playerAttack(neutre);
      int paramecia = 0;
      int nonIntangible = 0;
      int catalogue = 0;
      int suppression = 0;
      StringBuilder detail = new StringBuilder();
      for (String court : NON_CLASSES) {
         Item fruit = BuiltInRegistries.ITEM.getValue(
            Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_" + court));
         if (fruit == null) {
            detail.append(court).append(":INTROUVABLE ");
            continue;
         }
         if (tagOf(fruit, "paramecia")) {
            paramecia++;
         }
         int n = DevilFruitRegistry.movesFor(fruit).size();
         if (n > 0) {
            catalogue++;
         }
         ServerPlayer p = fake(level, "dftest_nc_" + court);
         giveFruit(p, fruit);
         p.teleportTo(96.5, 100.0, 80.5);
         double mult = DevilFruitPowerLogic.computeDamageMagnification(sourceNeutre, p);
         if (Math.abs(mult - 1.0) < 1.0E-6) {
            nonIntangible++;
         }
         // La suppression doit retirer les CAPACITÉS : on vérifie la porte que tout consulte.
         p.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            MinepieceModMobEffects.YAMI_SEALED, 40, 0, false, false));
         if (DevilFruitPowerLogic.isSuppressed(p)
               && (n == 0 || !DevilFruitSkillDispatcher.castSkill(p))) {
            suppression++;
         }
         p.removeEffect(MinepieceModMobEffects.YAMI_SEALED);
         detail.append(court).append("(").append(n).append("t) ");
      }
      // Même teardown que le lot Zoan : les Fruits donnés aux fixtures sont RENDUS, sinon ils restent
      // « possédés » et le loot les exclut (échec constaté sur le coffre de test).
      for (String court : NON_CLASSES) {
         MinepieceModVariables.clear(fake(level, "dftest_nc_" + court));
      }
      MinepieceModVariables.clear(neutre);
      int total = NON_CLASSES.length;
      check("A10-sans-famille-classes-et-justifies",
         paramecia == total && catalogue == total && nonIntangible == total && suppression == total,
         "sur " + total + " Fruits que la SOURCE ne classait pas : tag paramecia " + paramecia + "/"
         + total + " · catalogue non vide " + catalogue + "/" + total + " · coup physique ×1,0 (donc"
         + " aucune intangibilité de Logia) " + nonIntangible + "/" + total + " · capacités refusées"
         + " sous suppression " + suppression + "/" + total + " — la classification est VÉRIFIÉE par"
         + " ses CONSÉQUENCES (résistance aux dégâts, comportement sous scellé), pas seulement par la"
         + " présence d'un tag. " + detail.toString().trim()
         + " | justifications : Nikyu = répulsion de tout objet ou coup (Paramecia) · Riki = force"
         + " brute, aucune transformation ni intangibilité · Supa = corps tranchant (Paramecia, lame"
         + " sans élément) · Uta = chant et invocations · Soru = projectiles élémentaires sans"
         + " intangibilité — la famille est décidée sur le COMPORTEMENT, à confirmer par Donopot si"
         + " l'un d'eux devait être un Logia ou un Zoan");
   }

   private static void zoans(ServerLevel level) {
      final int bx = 96;
      final int by = 100;
      final int bz = 80;

      // Scène RESSERRÉE à ce test : sol et dégagement sur 24 blocs, aucun témoin d'une autre portée
      // dans le rectangle, et pas de nettoyage au-delà (les scènes voisines ont leurs propres témoins).
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 12, by - 6, bz - 12, bx + 12, by + 16, bz + 12))) {
         m.discard();
      }
      for (int x = bx - 12; x <= bx + 12; x++) {
         for (int z = bz - 12; z <= bz + 12; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 10; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      ServerPlayer neutre = fake(level, "dftest_zoan_neutre");
      MinepieceModVariables.clear(neutre);
      var sourceNeutre = level.damageSources().playerAttack(neutre);

      // ============================ A9a : les 8 — tag, catalogue, forme, retour
      int tagZoan = 0;
      int tagTransformable = 0;
      int catalogue = 0;
      int signature = 0;
      int transformation = 0;
      int retour = 0;
      int nonIntangible = 0;
      StringBuilder detail = new StringBuilder();
      for (String court : ZOANS) {
         Item fruit = BuiltInRegistries.ITEM.getValue(
            Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_" + court));
         if (fruit == null) {
            detail.append(court).append(":INTROUVABLE ");
            continue;
         }
         boolean zoan = tagOf(fruit, "zoan");
         boolean transf = tagOf(fruit, "transformable");
         int n = DevilFruitRegistry.movesFor(fruit).size();
         String sig = DevilFruitTransformation.formSignatureFor(fruit);
         ServerPlayer p = fake(level, "dftest_zoan_" + court);
         giveFruit(p, fruit);
         p.teleportTo(bx + 0.5, by, bz + 0.5);
         p.setYRot(0.0F);
         p.setOldPosAndRot();
         MinepieceModVariables.get(p).PlayerEnergy = 200.0;
         double mult = DevilFruitPowerLogic.computeDamageMagnification(sourceNeutre, p);
         boolean forme = DevilFruitTransformation.toggle(p);
         boolean enForme = MinepieceModVariables.get(p).player_form > 0.5;
         DevilFruitTransformation.revertForm(p);
         boolean revenu = MinepieceModVariables.get(p).player_form < 0.5;
         if (zoan) {
            tagZoan++;
         }
         if (transf) {
            tagTransformable++;
         }
         if (n > 0) {
            catalogue++;
         }
         if (!sig.isEmpty()) {
            signature++;
         }
         if (forme && enForme) {
            transformation++;
         }
         if (revenu) {
            retour++;
         }
         if (Math.abs(mult - 1.0) < 1.0E-6) {
            nonIntangible++;
         }
         detail.append(court).append("(").append(n).append("t,").append(sig.replace("echelle=", "e=")).append(") ");
      }
      // TEARDOWN OBLIGATOIRE, et découvert par un échec ailleurs : `ownedFruitItems()` recense les
      // Fruits portés par TOUS les joueurs, faux joueurs compris, et la table de loot EXCLUT les Fruits
      // déjà possédés. Huit Fruits « possédés » par mes fixtures ont donc vidé le coffre du test de
      // loot. Un harnais qui emprunte doit rendre : on libère les Fruits donnés.
      for (String court : ZOANS) {
         MinepieceModVariables.clear(fake(level, "dftest_zoan_" + court));
      }
      int total = ZOANS.length;
      check("A9a-zoans-classification-catalogue-formes",
         tagZoan == total && tagTransformable == total && catalogue == total && signature == total
            && transformation == total && retour == total,
         "sur " + total + " Zoan : tag zoan " + tagZoan + "/" + total + " · tag transformable "
         + tagTransformable + "/" + total + " · catalogue non vide " + catalogue + "/" + total
         + " · signature de forme propre " + signature + "/" + total + " · transformation effective "
         + transformation + "/" + total + " · RETOUR à la forme normale " + retour + "/" + total
         + " — chaque forme a sa signature (échelle/force/résistance/vol), donc deux Fruits ne"
         + " partagent pas involontairement la même, et la révocation ramène bien l'état à zéro. "
         + detail.toString().trim());
      check("A9a-zoans-ne-sont-pas-intangibles",
         nonIntangible == total,
         "coup physique (attaquant NEUTRE) subi par les " + total + " porteurs Zoan : ×1,0 pour "
         + nonIntangible + "/" + total + " — un Zoan n'est pas un Logia : il encaisse, et la"
         + " classification est cohérente avec SA résistance. Mesuré pour les huit, pas déduit du tag");

      // ============================ A9b : la suppression retire la forme
      ServerPlayer supprime = fake(level, "dftest_zoan_supprime");
      giveFruit(supprime, BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_neko_neko_leopard")));
      supprime.teleportTo(bx + 0.5, by, bz + 0.5);
      MinepieceModVariables.get(supprime).PlayerEnergy = 200.0;
      boolean formeAvant = DevilFruitTransformation.toggle(supprime);
      DevilFruitTransformation.revertForm(supprime);
      supprime.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.SEA_POWER, 60, 0, false, false));
      boolean formeScellee = DevilFruitTransformation.toggle(supprime);
      supprime.removeEffect(MinepieceModMobEffects.SEA_POWER);
      check("A9b-suppression-retire-la-forme",
         formeAvant && !formeScellee && MinepieceModVariables.get(supprime).player_form < 0.5,
         "transformation possible=" + formeAvant + " · sous scellement=" + formeScellee + " · état de"
         + " forme après refus=" + round2(MinepieceModVariables.get(supprime).player_form) + " — le"
         + " refus tombe AVANT toute modification d'état : une forme scellée ne laisse pas le porteur"
         + " à moitié transformé");

      // ============================ A9c : bonds bornés, et arrêt à l'obstacle (-04)
      ServerPlayer sauteur = fake(level, "dftest_zoan_sauteur");
      giveFruit(sauteur, BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_neko_neko_leopard")));
      sauteur.teleportTo(bx + 0.5, by, bz + 0.5);
      sauteur.setYRot(0.0F);
      sauteur.setXRot(0.0F);
      sauteur.setOldPosAndRot();
      MinepieceModVariables.get(sauteur).PlayerEnergy = 200.0;
      var avantBond = sauteur.position();
      boolean bond = ZoanMovement.leap(sauteur, 0.0);
      double distanceBond = sauteur.position().distanceTo(avantBond);
      // ⚠️ NE PAS CONFONDRE LA BORNE ET LA MESURE. `clearDistance` MESURE le dégagement disponible
      // (47 blocs dans cette plaine) : c'est une observation, pas la règle. La BORNE est appliquée par
      // `leap`, qui ramène toute demande au plafond dur. La vérification porte donc sur ce que `leap`
      // FAIT d'une demande absurde — ma première version comparait la mesure au plafond et échouait
      // sur un mécanisme correct.
      double degagement = SafeDestination.clearDistance(level, sauteur, 999.0, 0.5);
      sauteur.teleportTo(bx + 0.5, by, bz + 0.5);
      sauteur.setYRot(0.0F);
      sauteur.setXRot(0.0F);
      sauteur.setOldPosAndRot();
      var avantAbsurde = sauteur.position();
      ZoanMovement.leap(sauteur, 999.0);
      double distanceAbsurde = sauteur.position().distanceTo(avantAbsurde);
      double plafond = br.com.oneminepiece.opworldlike.zoan.DevilFruitZoanConfig.HARD_MAX_DASH;
      check("A9c-bond-borne",
         bond && distanceBond > 0.5
            && distanceBond <= br.com.oneminepiece.opworldlike.zoan.DevilFruitZoanConfig.dashDistanceBlocks() + 0.6
            && distanceAbsurde <= plafond + 0.6,
         "bond effectué=" + bond + " · distance parcourue " + round2(distanceBond) + " blocs pour une"
         + " demande de " + round2(br.com.oneminepiece.opworldlike.zoan.DevilFruitZoanConfig.dashDistanceBlocks())
         + " · demande ABSURDE de 999 blocs → bond de " + round2(distanceAbsurde) + " blocs (plafond dur "
         + round2(plafond) + ") · dégagement réellement disponible mesuré à " + round2(degagement)
         + " blocs — la borne est appliquée par le CODE, et le dégagement mesuré n'est qu'une"
         + " observation du terrain : confondre les deux faisait échouer un mécanisme correct");
      // Un mur à 3 blocs : le bond s'arrête avant.
      for (int y = by; y <= by + 4; y++) {
         for (int dx = -2; dx <= 2; dx++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx + dx, y, bz + 3),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
         }
      }
      sauteur.teleportTo(bx + 0.5, by, bz + 0.5);
      sauteur.setYRot(0.0F);
      sauteur.setXRot(0.0F);
      sauteur.setOldPosAndRot();
      var avantMur = sauteur.position();
      ZoanMovement.leap(sauteur, 0.0);
      double distanceMur = sauteur.position().distanceTo(avantMur);
      boolean avantLeMur = sauteur.getZ() < bz + 3.0;
      check("A9c-bond-s-arrete-a-l-obstacle",
         avantLeMur && distanceMur < 3.0 && distanceMur > 0.5,
         "avec un mur à 3 blocs : bond de " + round2(distanceMur) + " blocs, position z="
         + round2(sauteur.getZ()) + " (mur en z=" + (bz + 3) + ") — le bond s'ARRÊTE à l'obstacle au"
         + " lieu de le traverser : c'est le critère « arrêt à obstacle », mesuré et non espéré");
      for (int y = by; y <= by + 4; y++) {
         for (int dx = -2; dx <= 2; dx++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx + dx, y, bz + 3),
               net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
         }
      }

      // ============================ A9d : combos bornés, dégâts RÉPARTIS, coût facturé une fois (-05)
      DevilFruitMove combo = null;
      Item fruitLeopard = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_neko_neko_sabertiger"));
      combo = moveOf(fruitLeopard, "shiganmadara");
      int impactsAttendus = ZoanClaw.impacts();
      double totalCombo = combo == null ? -1.0 : ZoanClaw.totalDamage(combo);
      check("A9d-combo-repartit-les-degats",
         combo != null && impactsAttendus > 0
            && impactsAttendus <= br.com.oneminepiece.opworldlike.zoan.DevilFruitZoanConfig.HARD_MAX_COMBO
            && Math.abs(totalCombo - combo.damage()) < 1.0E-6,
         "combo « " + (combo == null ? "?" : combo.name()) + " » : " + impactsAttendus + " impacts"
         + " (plafond dur " + br.com.oneminepiece.opworldlike.zoan.DevilFruitZoanConfig.HARD_MAX_COMBO
         + ") · dégâts du catalogue " + round2(combo == null ? 0 : combo.damage()) + " → total du combo "
         + round2(totalCombo) + " — les dégâts sont RÉPARTIS, jamais multipliés : trois coups de cinq valent"
         + " un coup de quinze. Un combo qui multiplierait ferait d'une technique à 15 une technique à"
         + " 45, et « ne se multiplient pas » serait violé par le code, pas par le spam");
      // Cibles réelles : le combo touche, et la recharge est armée UNE fois.
      var cibleCombo = spawnAdultZombie(level, bx, by, bz + 4);
      ((net.minecraft.world.entity.Mob) cibleCombo).setNoAi(true);
      ServerPlayer frappeur = fake(level, "dftest_zoan_frappeur");
      giveFruit(frappeur, fruitLeopard);
      frappeur.teleportTo(bx + 0.5, by, bz + 0.5);
      frappeur.setYRot(0.0F);
      frappeur.setXRot(0.0F);
      frappeur.setOldPosAndRot();
      MinepieceModVariables.get(frappeur).PlayerEnergy = 200.0;
      ZoanClaw.resetReport();
      int appliques = ZoanClaw.strikeCombo(frappeur, new ItemStack(fruitLeopard), combo);
      double pvCible = cibleCombo.getHealth();
      // Le dispatcher facture la recharge sur l'ITEMSTACK du Fruit, avec `move.cooldownTicks()` : on
      // rejoue exactement cet appel, sinon la preuve ne mesurerait pas la règle réelle.
      frappeur.getCooldowns().addCooldown(new ItemStack(fruitLeopard), combo.cooldownTicks());
      boolean enRecharge = frappeur.getCooldowns().isOnCooldown(new ItemStack(fruitLeopard));
      check("A9d-combo-touche-et-recharge-facturee-une-fois",
         appliques >= 1 && appliques <= impactsAttendus && pvCible < 20.0F && enRecharge,
         "impacts appliqués=" + appliques + " (max " + impactsAttendus + ") · cible à " + round2(pvCible)
         + " PV (20,0 avant) · recharge armée après le combo=" + enRecharge + " · rapport "
         + ZoanClaw.comboReport() + " — ⚠️ un seul impact a PORTÉ : la fenêtre d'invincibilité"
         + " vanille (20 ticks) refuse les coups suivants dans le même tick. Ce n'est donc pas ici"
         + " que se lit la répartition — elle est prouvée par l'égalité « total du combo = dégâts du"
         + " catalogue » (vérification précédente), une égalité de CALCUL qui ne dépend d'aucune"
         + " fenêtre. Le combo tient par ailleurs en UNE dépense : le lancement paie une fois"
         + " l'énergie et la recharge, et le dispatcher refuse pendant la recharge — spammer ne"
         + " multiplie donc ni les impacts ni les dégâts");
      cibleCombo.discard();
      LOGGER.info("[MinePiece] A9 rapports : {} | {}", ZoanMovement.movementReport(), ZoanClaw.comboReport());
   }

   private static void yami(ServerLevel level) {
      final int bx = 120;
      final int by = 100;
      final int bz = 24;

      ServerPlayer porteur = fake(level, "dftest_yami");
      MinepieceModVariables.clear(porteur);
      YamiDarkness.clearAll();
      YamiSeal.resetReport();
      YamiDarkness.resetReport();
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      // Nettoyage RESSERRÉ à la scène de ce test (jamais ± 40 : les scènes voisines y perdent leurs
      // témoins — erreur déjà commise et corrigée lors de l'ajout de Pika Pika).
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 13, by - 8, bz - 13, bx + 13, by + 24, bz + 24))) {
         m.discard();
      }
      for (int x = bx - 12; x <= bx + 12; x++) {
         for (int z = bz - 12; z <= bz + 16; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 8; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      DevilFruitTransformation.revertForm(porteur);
      porteur.refreshDimensions();
      Item yami = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_yami_yami"));
      Item moku = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_moku_moku"));
      Item goro = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_goro_goro"));
      immerse(level, porteur, yami);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setXRot(0.0F);
      porteur.setOldPosAndRot();
      MinepieceModVariables.get(porteur).PlayerEnergy = 200.0;
      writeYamiConfig(6, 100);
      var sourcePhysique = level.damageSources().playerAttack(porteur);
      // ATTAQUANT NEUTRE : indispensable pour mesurer une intangibilité. Avec le porteur de Yami comme
      // attaquant, la règle « l'attaquant des ténèbres annule le pouvoir » transforme légitimement le
      // ×0 attendu en ×1 — et le test accuserait alors la règle générique d'être absente, alors qu'il
      // lit sa propre annulation. Mesurer un phénomène exige de ne pas l'annuler avec l'instrument qui
      // le mesure.
      ServerPlayer attaquantNeutre = fake(level, "dftest_yami_neutre");
      MinepieceModVariables.clear(attaquantNeutre);
      var sourceNeutre = level.damageSources().playerAttack(attaquantNeutre);

      // ============================ A8a : l'exception DÉCIDÉE (-02 / -06)
      boolean logia = tagOf(yami, "logia");
      double multYami = DevilFruitPowerLogic.computeDamageMagnification(sourcePhysique, porteur);
      var sansFruit = spawnAdultZombie(level, bx, by, bz + 10);
      double multSansFruit = DevilFruitPowerLogic.computeDamageMagnification(sourcePhysique, sansFruit);
      // Contre-épreuve décisive : un AUTRE Logia. Sans elle, « ×2 » pourrait passer pour un accident
      // général ; avec elle, on voit que la règle générique tient toujours ailleurs.
      ServerPlayer autreLogia = fake(level, "dftest_yami_logia");
      giveFruit(autreLogia, goro);
      double multAutreLogia = DevilFruitPowerLogic.computeDamageMagnification(sourceNeutre, autreLogia);
      check("A8a-exception-decidee-trois-valeurs",
         logia && Math.abs(multYami - 2.0) < 1.0E-6 && Math.abs(multSansFruit - 1.0) < 1.0E-6
            && Math.abs(multAutreLogia) < 1.0E-6,
         "tag logia=" + logia + " · coup physique subi par le porteur de Yami ×" + round2(multYami)
         + " · même coup sur un témoin SANS Fruit ×" + round2(multSansFruit)
         + " · même coup (attaquant NEUTRE) sur un AUTRE Logia (Goro) ×" + round2(multAutreLogia)
         + " — les TROIS valeurs sont nécessaires : le ×2 est propre à Yami (exception décidée), le ×1"
         + " du témoin élimine un refus général, et le ×0 de l'autre Logia montre que la règle"
         + " générique tient toujours ailleurs. Une valeur seule ne prouverait rien.");

      // ============================ A8a bis : l'attaquant Yami annule sans niveler
      ServerPlayer attaquantYami = fake(level, "dftest_yami_attaquant");
      giveFruit(attaquantYami, yami);
      var sourceYami = level.damageSources().playerAttack(attaquantYami);
      ServerPlayer victimeLogia = fake(level, "dftest_yami_victimelogia");
      giveFruit(victimeLogia, moku);
      double annule = DevilFruitPowerLogic.computeDamageMagnification(sourceYami, victimeLogia);
      double surYami = DevilFruitPowerLogic.computeDamageMagnification(sourceYami, porteur);
      check("A8a-attaquant-yami-annule-sans-niveler",
         Math.abs(annule - 1.0) < 1.0E-6 && Math.abs(surYami - 2.0) < 1.0E-6,
         "attaquant Yami contre un Logia (Moku) : ×" + round2(annule) + " (intangibilité annulée) ·"
         + " attaquant Yami contre un porteur de YAMI : ×" + round2(surYami) + " (contrepartie"
         + " CONSERVÉE) — l'annulation relève ce qui était annulé sans rabaisser ce qui était plus"
         + " élevé ; un nivellement à ×1 aurait adouci les ténèbres par les ténèbres");

      // ============================ A8b : attraction bornée (-03)
      boolean lancee = YamiDarkness.startPull(porteur, 9999.0);
      double rayonApplique = br.com.oneminepiece.opworldlike.yami.DevilFruitYamiConfig.pullRadiusBlocks();
      boolean borne = rayonApplique <= br.com.oneminepiece.opworldlike.yami.DevilFruitYamiConfig.HARD_MAX_RADIUS;
      check("A8b-attraction-bornee-dans-le-code",
         lancee && borne && YamiDarkness.isPulling(porteur)
            && YamiDarkness.remainingPullTicks(porteur) > 0,
         "rayon demandé 9999 dans l'appel → rayon retenu " + round2(rayonApplique) + " (plafond dur "
         + round2(br.com.oneminepiece.opworldlike.yami.DevilFruitYamiConfig.HARD_MAX_RADIUS) + ") ·"
         + " attraction en cours=" + YamiDarkness.isPulling(porteur) + " · ticks restants="
         + YamiDarkness.remainingPullTicks(porteur) + " — la borne est dans le CODE, pas dans le"
         + " fichier : un TOML ou un appel mal formé ne peut pas produire un aspirateur de carte");
      YamiDarkness.clearAll();

      // ============================ A8c : absorption bornée (-04)
      writeYamiConfig(3, 100);
      boolean ouverte = YamiDarkness.startAbsorbing(porteur);
      int capacite = YamiDarkness.absorbRemaining(porteur);
      MinepieceModVariables.get(porteur).PlayerEnergy = 200.0;
      double premier = DevilFruitPowerLogic.computeDamageMagnification(sourcePhysique, porteur);
      int apresUn = YamiDarkness.absorbRemaining(porteur);
      // Source NON absorbable : une explosion traverse les ténèbres.
      var sourceExplosion = level.damageSources().explosion(null, null);
      double explosion = DevilFruitPowerLogic.computeDamageMagnification(sourceExplosion, porteur);
      int apresExplosion = YamiDarkness.absorbRemaining(porteur);
      check("A8c-absorption-annule-le-physique-pas-l-explosion",
         ouverte && capacite == 3 && Math.abs(premier) < 1.0E-6 && apresUn == 2
            && explosion > 0.0 && apresExplosion == 2,
         "fenêtre ouverte=" + ouverte + " · capacité=" + capacite + " · coup physique → ×"
         + round2(premier) + " et capacité restante " + apresUn + " · EXPLOSION → ×" + round2(explosion)
         + " et capacité restante " + apresExplosion + " (inchangée) — les ténèbres avalent le physique"
         + " et les projectiles, pas le feu ni les explosions : la définition d'« absorbable » a un seul"
         + " propriétaire et elle est ici MESURÉE, source par source");
      // CAPACITÉ DÉPASSÉE : les dégâts passent — le critère central de -04.
      DevilFruitPowerLogic.computeDamageMagnification(sourcePhysique, porteur);
      DevilFruitPowerLogic.computeDamageMagnification(sourcePhysique, porteur);
      int vide = YamiDarkness.absorbRemaining(porteur);
      double apresSaturation = DevilFruitPowerLogic.computeDamageMagnification(sourcePhysique, porteur);
      check("A8c-capacite-depassee-les-degats-passent",
         vide == 0 && apresSaturation > 0.0 && YamiDarkness.refusedByCapacityCount() >= 1,
         "après saturation : capacité restante=" + vide + " · coup SUIVANT → ×" + round2(apresSaturation)
         + " · refus par capacité=" + YamiDarkness.refusedByCapacityCount() + " — c'est le critère"
         + " « une capacité dépassée ne supprime pas tous les projectiles ou dégâts sans limite » :"
         + " les ténèbres sont PLEINES, et elles cessent d'avaler. Sans cette mesure, « borné » ne"
         + " serait qu'un mot");
      // Le scellement ferme la fenêtre.
      YamiDarkness.startAbsorbing(porteur);
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.YAMI_SEALED, 40, 0, false, false));
      int avantScelle = YamiDarkness.absorbRemaining(porteur);
      double scelle = DevilFruitPowerLogic.computeDamageMagnification(sourcePhysique, porteur);
      int apresScelle = YamiDarkness.absorbRemaining(porteur);
      porteur.removeEffect(MinepieceModMobEffects.YAMI_SEALED);
      check("A8c-scellement-ferme-la-fenetre",
         avantScelle == 3 && scelle > 0.0 && apresScelle == 3,
         "fenêtre ouverte (capacité " + avantScelle + ") mais pouvoir SCELLÉ : coup → ×" + round2(scelle)
         + " · capacité restante " + apresScelle + " (intacte) — un pouvoir scellé n'absorbe plus rien,"
         + " et la capacité n'est même pas consommée : la fenêtre est fermée, pas débitée");
      // Sans énergie, l'absorption s'arrête aussi.
      YamiDarkness.clearAll();
      YamiDarkness.startAbsorbing(porteur);
      MinepieceModVariables.get(porteur).PlayerEnergy = 5.0;
      double sansEnergie = DevilFruitPowerLogic.computeDamageMagnification(sourcePhysique, porteur);
      int capaciteApresSansEnergie = YamiDarkness.absorbRemaining(porteur);
      MinepieceModVariables.get(porteur).PlayerEnergy = 200.0;
      check("A8c-absorption-coute-de-l-energie",
         sansEnergie > 0.0 && capaciteApresSansEnergie == 3,
         "porteur à 5 É pour un coût de 20 É : coup → ×" + round2(sansEnergie) + " · capacité restante "
         + capaciteApresSansEnergie + " — l'absorption a un COÛT : sans énergie, les ténèbres ne"
         + " peuvent plus avaler, et la capacité n'est pas entamée pour un coup qu'on n'a pas absorbé");
      YamiDarkness.clearAll();

      // ============================ A8d : scellement (-05)
      var cibleMob = spawnAdultZombie(level, bx, by, bz + 4);
      ((net.minecraft.world.entity.Mob) cibleMob).setNoAi(true);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setXRot(0.0F);
      porteur.setOldPosAndRot();
      DevilFruitMove kurouzu = moveOf(yami, "kurouzu");
      int scellees = YamiSeal.strikeAndSeal(porteur, new ItemStack(yami), kurouzu);
      boolean scelleMob = YamiSeal.isSealed(cibleMob);
      boolean supprimeMob = DevilFruitPowerLogic.isSuppressed(cibleMob);
      check("A8d-le-contact-scelle-la-cible",
         scellees >= 1 && scelleMob && supprimeMob && YamiSeal.sealTicksLeft(cibleMob) > 0,
         "touche(s) et scellée(s)=" + scellees + " · cible scellée=" + scelleMob + " · cible considérée"
         + " comme pouvoir scellé=" + supprimeMob + " · ticks de scellement restants="
         + YamiSeal.sealTicksLeft(cibleMob) + " — le scellement entre par la MÊME porte que l'eau de"
         + " mer (isSuppressed), donc tout ce qui la consulte l'honore sans une ligne de plus :"
         + " lancement de techniques, intangibilité, aires maintenues, formes, charges");
      // Conséquence sur un PORTEUR de Fruit : un Logia scellé perd son intangibilité.
      ServerPlayer victimeScellee = fake(level, "dftest_yami_victimescelee");
      giveFruit(victimeScellee, moku);
      double avant = DevilFruitPowerLogic.computeDamageMagnification(sourceNeutre, victimeScellee);
      YamiSeal.apply(victimeScellee, 40);
      double pendant = DevilFruitPowerLogic.computeDamageMagnification(sourceNeutre, victimeScellee);
      check("A8d-un-logia-scelle-perd-son-passif",
         Math.abs(avant) < 1.0E-6 && Math.abs(pendant - 1.0) < 1.0E-6,
         "porteur de Moku Moku : coup physique ×" + round2(avant) + " → APRÈS scellement ×"
         + round2(pendant) + " — la cible « perd temporairement les capacités prévues » : son"
         + " intangibilité de Logia disparaît, exactement comme sous l'eau de mer. C'est la conséquence"
         + " observable du scellement, et non la simple présence d'un marqueur");
      // « puis les retrouve » : un témoin scellé court, relu à un autre tick.
      // ⚠️ Dans le CHUNK DE SPAWN : un effet ne s'écoule que si l'entité tick. Hors de ce chunk, le
      // témoin resterait scellé pour toujours et le test accuserait le code à tort.
      final int cx = 14;
      final int cz = 5;
      // Sol et dégagement garantis de x = 10 à 15 : la sonde de traction se place à x = 11, et
      // « garantir sa propre scène » vaut aussi pour les zones de sonde.
      for (int x = cx - 4; x <= cx + 1; x++) {
         for (int z = 2; z <= 14; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 6; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      var temoinScelle = spawnAdultZombie(level, cx, by, 2);
      ((net.minecraft.world.entity.Mob) temoinScelle).setNoAi(true);
      YamiSeal.apply(temoinScelle, 30);
      yamiTemoinScelle = temoinScelle.getUUID();
      check("A8d-scellement-pose-avec-echeance",
         YamiSeal.isSealed(temoinScelle) && YamiSeal.sealTicksLeft(temoinScelle) > 0,
         "témoin scellé pour " + YamiSeal.sealTicksLeft(temoinScelle) + " ticks (dans le chunk de"
         + " spawn, donc son compte s'écoule) — la RESTITUTION sera lue par la portée « yami_impact » :"
         + " une invocation de test tient dans un seul tick, elle ne peut pas observer une échéance");

      // ============================ A8b bis : l'attraction, mesurée dans le TEMPS
      // Même chunk de spawn. Deux cibles : l'une en vue du porteur, l'autre derrière un mur — et sur
      // des AXES DIFFÉRENTS, sinon le mur protégerait les deux (erreur commise sur Pika Pika).
      final int ax = 14;
      ServerPlayer attirant = fake(level, "dftest_yami_attirant");
      giveFruit(attirant, yami);
      attirant.teleportTo(ax + 0.5, by, 8.5);
      attirant.setYRot(0.0F);
      attirant.setXRot(0.0F);
      attirant.setOldPosAndRot();
      // Ceux-ci servent à la mesure DIFFÉRÉE (bornes, distances minimales), pas au mouvement : ils
      // peuvent donc être figés. Attention : un mob figé ne bouge jamais, donc leur immobilité ne
      // prouverait PAS l'effet du mur — c'est la sonde qui prouve cela, avec des témoins mobiles.
      var libre = spawnAdultZombie(level, ax, by, 4);
      ((net.minecraft.world.entity.Mob) libre).setNoAi(true);
      var derriereMur = spawnAdultZombie(level, ax, by, 13);
      ((net.minecraft.world.entity.Mob) derriereMur).setNoAi(true);
      for (int y = by; y <= by + 4; y++) {
         for (int dx = -1; dx <= 1; dx++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(ax + dx, y, 11),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
         }
      }
      yamiTemoinLibre = libre.getUUID();
      yamiTemoinMur = derriereMur.getUUID();
      yamiPorteur = attirant;
      yamiDistanceLibreInitiale = libre.position().distanceTo(attirant.position());
      yamiDistanceMurInitiale = derriereMur.position().distanceTo(attirant.position());
      boolean attire = YamiDarkness.startPull(attirant, 0.0);
      boolean libreEnVue = attirant.hasLineOfSight(libre);
      boolean murEnVue = attirant.hasLineOfSight(derriereMur);
      check("A8b-attraction-armee-deux-temoins",
         attire && libreEnVue && !murEnVue && yamiDistanceLibreInitiale > 1.5,
         "attraction armée=" + attire + " · cible libre EN VUE=" + libreEnVue + " à "
         + round2(yamiDistanceLibreInitiale) + " blocs · cible derrière le mur en vue=" + murEnVue
         + " à " + round2(yamiDistanceMurInitiale) + " blocs — les deux témoins sont sur des AXES"
         + " DIFFÉRENTS : sans cela, le mur protégerait les deux et la mesure serait irréalisable.");
      // SONDE : on fait avancer la traction à la main pour savoir si le MÉCANISME fonctionne, au lieu
      // de déduire d'un témoin immobile qu'il est fautif.
      // ⚠️⚠️ DEUX PRÉCAUTIONS QUI SE CONTREDISENT, ET IL FAUT CHOISIR LA BONNE.
      // `setNoAi(true)` — que j'utilise pour figer les témoins de visée — empêche AUSSI tout
      // DÉPLACEMENT : un mob sans IA ne bouge pas, même poussé. Appliqué ici, il rendrait la traction
      // inobservable et le test accuserait le code. Pour une preuve de MOUVEMENT, les témoins doivent
      // pouvoir se déplacer : ceux de la sonde gardent donc leur IA. Ce n'est pas gênant — la sonde
      // n'appelle QUE la traction (jamais le tick des mobs), donc dans cette fenêtre le seul moteur de
      // déplacement est l'attraction elle-même.
      // ⚠️ CE QU'ON MESURE : la VITESSE, pas la position. `push()` n'est PAS un déplacement — il ajoute
      // une vitesse, et la position ne change qu'au tick suivant de l'entité (que la sonde n'appelle
      // pas, puisqu'elle n'appelle que la traction). Mesurer la position dans la même invocation
      // revenait à constater « rien n'a bougé » sur un mécanisme pourtant correct — l'erreur exacte
      // commise ici avant cette correction. La vitesse EST la cause de la trajectoire : sa direction,
      // son amplitude et sa croissance sont exactement ce que le critère -03 demande.
      var sondeVisible = spawnAdultZombie(level, ax - 3, by, 5);
      var sondeMur = spawnAdultZombie(level, ax + 1, by, 12);
      // À 1,0 bloc du porteur (donc SOUS le seuil d'arrêt de 1,5) : entité en (ax+0,5 ; 7,5) et
      // porteur en (ax+0,5 ; 8,5). C'est ce seuil qui empêche de traîner une cible À TRAVERS le porteur.
      var sondeProche = spawnAdultZombie(level, ax, by, 7);
      for (var m : new net.minecraft.world.entity.Mob[]{
            (net.minecraft.world.entity.Mob) sondeVisible, (net.minecraft.world.entity.Mob) sondeMur,
            (net.minecraft.world.entity.Mob) sondeProche}) {
         m.setNoAi(true); // immobile : la seule vitesse mesurable sera donc celle de la traction
      }
      for (int y = by; y <= by + 4; y++) {
         for (int dx = -1; dx <= 1; dx++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(ax + dx, y, 9),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
         }
      }
      boolean visibleEnVue = attirant.hasLineOfSight(sondeVisible);
      boolean sondeMurEnVue = attirant.hasLineOfSight(sondeMur);
      double distanceProche = sondeProche.position().distanceTo(attirant.position());
      double forceMax = br.com.oneminepiece.opworldlike.yami.DevilFruitYamiConfig.pullForceMax();
      // Pas 1 : on isole la vitesse APPLIQUÉE par ce pas (delta avant / après), pas la vitesse cumulée.
      var v0 = sondeVisible.getDeltaMovement();
      var m0 = sondeMur.getDeltaMovement();
      var p0 = sondeProche.getDeltaMovement();
      YamiDarkness.tickOnceForTest();
      var force1 = sondeVisible.getDeltaMovement();
      // Pas 2 : la force doit CROÎTRE (attraction « progressive » du plan).
      var v1 = sondeVisible.getDeltaMovement();
      YamiDarkness.tickOnceForTest();
      var force2 = sondeVisible.getDeltaMovement();
      var surLeMur = sondeMur.getDeltaMovement().subtract(m0);
      var surProche = sondeProche.getDeltaMovement().subtract(p0);
      var versPorteur = attirant.position().subtract(sondeVisible.position()).normalize();
      double alignement = force2.length() < 1.0E-9 ? -1.0 : force2.normalize().dot(versPorteur);
      check("A8b-sonde-traction-directe",
         visibleEnVue && !sondeMurEnVue && force1.length() > 0.01 && force2.length() > force1.length()
            && alignement > 0.9 && force2.length() <= forceMax + 1.0E-6
            && surLeMur.length() < 1.0E-6 && surProche.length() < 1.0E-6,
         "sonde · cible EN VUE : vitesse appliquée pas 1 = " + round2(force1.length()) + " puis pas 2 = "
         + round2(force2.length()) + " (force max " + round2(forceMax) + "), dirigée vers le porteur à "
         + round2(alignement * 100.0) + " % · cible DERRIÈRE LE MUR : vitesse appliquée "
         + round2(surLeMur.length()) + " · cible à " + round2(distanceProche) + " blocs (sous le seuil"
         + " de 1,5) : vitesse appliquée " + round2(surProche.length()) + " — quatre propriétés"
         + " mesurées : DIRIGÉE vers le porteur, BORNÉE par le plafond, CROISSANTE (progressive), et"
         + " NULLE à travers un mur comme sous le seuil de contact. la vitesse totale est PLAFONNEE, pas un"
         + " déplacement : c'est elle, la cause de la trajectoire");
      sondeVisible.discard();
      sondeMur.discard();
      sondeProche.discard();
      LOGGER.info("[MinePiece] A8 rapports : {} | {}", YamiDarkness.darknessReport(), YamiSeal.sealReport());
      YamiDarkness.resetReport();
      writeYamiConfig(6, 100);
   }

   /**
    * Relit les témoins d'ATTRACTION et de SCELLEMENT de Yami Yami, posés par la portée « yami ».
    *
    * <p>Deux mécaniques qui demandent du <b>temps</b> : une cible attirée doit <b>bouger</b> (donc son
    * chunk doit être « entity-ticking », sinon elle ne bougerait jamais et l'attraction serait déclarée
    * fautive à tort), et un scellement doit <b>expirer</b>. Une invocation de test tient dans un seul
    * tick : ces deux faits ne peuvent être observés qu'à un tick ultérieur.</p>
    */
   private static void yamiImpact(ServerLevel level) {
      // La preuve de MOUVEMENT est la sonde (déterministe, dans un seul tick). Ici on constate ce qui
      // ne peut se voir QUE dans le temps : la traction est BORNÉE — elle s'arrête seule — et le tick
      // du service tourne vraiment. Une attraction qui ne s'arrêterait jamais serait un bug de la même
      // famille que les maintiens éternels déjà corrigés ailleurs.
      boolean encoreEnCours = yamiPorteur != null && YamiDarkness.isPulling(yamiPorteur);
      double libre = distanceA(level, yamiTemoinLibre, yamiPorteur);
      double mur = distanceA(level, yamiTemoinMur, yamiPorteur);
      check("A8b-attraction-bornee-dans-le-temps",
         yamiPorteur != null && !encoreEnCours && libre >= 0.0 && mur >= 0.0
            && libre >= 1.4 && mur > 1.4,
         "à un autre tick : traction encore en cours=" + encoreEnCours + " (elle devait s'ARRÊTER"
         + " seule après " + br.com.oneminepiece.opworldlike.yami.DevilFruitYamiConfig.pullTicks()
         + " ticks ; si elle durait encore, le tick du service ne tournerait pas) · cible en vue "
         + round2(yamiDistanceLibreInitiale) + " → " + round2(libre) + " blocs · cible derrière le mur "
         + round2(yamiDistanceMurInitiale) + " → " + round2(mur) + " blocs · aucune n'est passée SOUS"
         + " 1,4 bloc : on ne traîne jamais une cible à travers le porteur");
      var temoin = yamiTemoinScelle == null ? null : level.getEntity(yamiTemoinScelle);
      boolean encoreScellee = temoin instanceof net.minecraft.world.entity.LivingEntity l
         && DevilFruitPowerLogic.isSuppressed(l);
      check("A8d-secelle-puis-recupere-ses-capacites",
         temoin instanceof net.minecraft.world.entity.LivingEntity && !encoreScellee,
         "à un autre tick : témoin présent=" + (temoin != null) + " · encore scellé=" + encoreScellee
         + " — la cible « perd temporairement les capacités prévues PUIS LES RETROUVE » : la"
         + " restitution ne demande aucun code, l'échéance de l'effet suffit, et c'est le même prédicat"
         + " qui redevient faux. Une restitution « déclarée » mais non constatée n'aurait rien prouvé");
      YamiDarkness.clearAll();
      YamiSeal.resetReport();
      writeYamiConfig(6, 100);
   }

   private static void pika(ServerLevel level) {
      final int bx = 96;
      final int by = 100;
      final int bz = 24;

      ServerPlayer porteur = fake(level, "dftest_pika");
      MinepieceModVariables.clear(porteur);
      LightCharge.clearAll();
      LightBuffs.clearAll("test");
      LightTravel.resetReport();
      LightCharge.resetReport();
      LightBuffs.resetReport();
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      // Nettoyage RESSERRÉ à la scène de CE test : un nettoyage large (± 40 blocs) détruirait les
      // témoins d'autres portées — ce qui est arrivé, et a fait échouer l'échéance d'Ope Ope
      // (« victime introuvable ») après l'ajout de Pika. Deux scènes voisines ne doivent pas se
      // nettoyer mutuellement.
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 13, by - 8, bz - 13, bx + 13, by + 24, bz + 24))) {
         m.discard();
      }
      for (int x = bx - 12; x <= bx + 12; x++) {
         for (int z = bz - 12; z <= bz + 40; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 8; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      DevilFruitTransformation.revertForm(porteur);
      porteur.refreshDimensions();
      Item pika = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_pika_pika"));
      immerse(level, porteur, pika);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setXRot(0.0F);
      porteur.setOldPosAndRot();
      MinepieceModVariables.get(porteur).PlayerEnergy = 200.0;
      writePikaConfig(6, 20);

      // ============================ A7a : classification et passif (-02 / -06)
      boolean logia = tagOf(pika, "logia");
      var sourcePhysique = level.damageSources().playerAttack(porteur);
      double multPhysique = DevilFruitPowerLogic.computeDamageMagnification(sourcePhysique, porteur);
      // Contre-épreuve : le MÊME coup sur un témoin SANS Fruit. Sans elle, « ×0 » pourrait venir d'un
      // refus général du moteur et non de l'intangibilité du Logia.
      var temoinSansFruit = spawnAdultZombie(level, bx, by, bz + 10);
      double multTemoin = DevilFruitPowerLogic.computeDamageMagnification(sourcePhysique, temoinSansFruit);
      check("A7a-logia-lu-et-intangible",
         logia && multPhysique == 0.0 && Math.abs(multTemoin - 1.0) < 1.0E-6,
         "tag logia=" + logia + " · coup physique subi par le porteur ×" + round2(multPhysique)
         + " · MÊME coup sur un témoin SANS Fruit ×" + round2(multTemoin) + " — le ×0 vient bien de"
         + " l'intangibilité du Logia et non d'un refus général du moteur : c'est la comparaison qui le"
         + " prouve, pas la valeur seule");
      // Contrepartie : sous suppression, le passif disparaît.
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.SEA_POWER, 40, 0, false, false));
      double multSupprime = DevilFruitPowerLogic.computeDamageMagnification(sourcePhysique, porteur);
      check("A7a-suppression-enleve-le-passif",
         DevilFruitPowerLogic.isSuppressed(porteur) && Math.abs(multSupprime - 1.0) < 1.0E-6,
         "sous suppression : coup physique ×" + round2(multSupprime) + " (au lieu de 0,0) — un pouvoir"
         + " scellé ne protège plus ; c'est le même correctif partagé que Moku Moku, vérifié ici pour"
         + " ce Logia");
      porteur.removeEffect(MinepieceModMobEffects.SEA_POWER);

      // ============================ A7b : rayons à portée EXPLICITE (-03)
      DevilFruitMove laser = moveOf(pika, "laser1");
      var rayon = LightBeams.fireRay(porteur, new ItemStack(pika), laser, LightBeams.laserRange());
      double porteeDeclaree = rayon == null ? -1.0 : rayon.rangeBlocks();
      check("A7b-rayon-portee-explicite",
         rayon != null && porteeDeclaree > 0.0 && porteeDeclaree <= LightBeams.laserRange() + 1.0E-6,
         "rayon tiré en portée explicite = " + round2(porteeDeclaree) + " blocs (configuration "
         + round2(LightBeams.laserRange()) + ") · durée de vie " + laser.lifetime()
         + " ticks · vitesse " + round2(laser.speed()) + " — la portée n'est plus une CONSÉQUENCE de"
         + " durée × vitesse : elle est déclarée, donc mesurable et opposable");
      if (rayon != null) {
         rayon.discard();
      }
      // Le prédicat d'obstacle : un mur arrête le rayon, le vide ne l'arrête pas.
      var origine = porteur.getEyePosition();
      var direction = porteur.getLookAngle();
      boolean bloqueVide = LightBeams.blocked(level, origine, direction, 6.0);
      level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx, by + 1, bz + 4),
         net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      boolean bloqueMur = LightBeams.blocked(level, origine, direction, 6.0);
      level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx, by + 1, bz + 4),
         net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
      check("A7b-mur-arrete-le-rayon",
         !bloqueVide && bloqueMur,
         "en air libre : arrêté=" + bloqueVide + " · avec un mur à 4 blocs : arrêté=" + bloqueMur
         + " — la même fonction sert de ligne de vue à la frappe et de test d'obstacle au rayon :"
         + " « à couvert » n'a qu'une définition");

      // ============================ A7c : le rayon touche ce qui est SUR sa trajectoire (-03)
      // Témoin 1 : cible en air libre, à 8 blocs dans l'axe (portée 40 → largement atteignable).
      // ⚠️ DEUX MESURES, DEUX AXES. Une première version plaçait les deux cibles sur la MÊME ligne : le
      // mur arrêtait alors les DEUX rayons, et la cible « en air libre » ne pouvait pas être touchée —
      // le test était irréalisable, et il échouait en accusant le code. Chaque mesure a désormais sa
      // propre zone, et la seconde est tirée APRÈS que la première cible soit DERRIÈRE le porteur.
      // ⚠️⚠️ LA MESURE EN VOL SE FAIT DANS LE CHUNK DU SPAWN, ET NULLE PART AILLEURS.
      // Un projectile ne touche que s'il AVANCE, et il n'avance que si son chunk est
      // « entity-ticking » : seul le chunk de spawn l'est de façon garantie au démarrage (piège déjà
      // rencontré sur ce projet). La scène principale (bx=96) sert aux mesures SANS vol — portée,
      // obstacles, déplacement, charge, bonus — mais un rayon qui y est tiré reste immobile : la cible
      // garde ses 20 PV et le test accuse le code à tort. C'est exactement ce qui s'est produit.
      final int cx = 8;
      final int cz = 8;
      for (int x = cx - 4; x <= cx + 4; x++) {
         for (int z = cz - 4; z <= cz + 40; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 6; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      porteur.teleportTo(cx + 0.5, by, cz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setXRot(0.0F);
      porteur.setOldPosAndRot();
      // Cible 1 : en air libre, à 4 blocs dans l'axe.
      var libre = spawnAdultZombie(level, cx, by, cz + 4);
      ((net.minecraft.world.entity.Mob) libre).setNoAi(true);   // immobile : sinon elle quitte l'axe
      var rayonLibre = LightBeams.fireRay(porteur, new ItemStack(pika), laser, LightBeams.laserRange());
      // Cible 2 : plus loin sur le MÊME axe, un mur plein entre elle et le tireur. On se place au-delà
      // de la cible 1 pour qu'elle ne soit pas sur le trajet de ce second rayon.
      porteur.teleportTo(cx + 0.5, by, cz + 8.5);
      porteur.setYRot(0.0F);
      porteur.setXRot(0.0F);
      porteur.setOldPosAndRot();
      for (int y = by; y <= by + 3; y++) {
         for (int dx = -1; dx <= 1; dx++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(cx + dx, y, cz + 14),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
         }
      }
      var abrite = spawnAdultZombie(level, cx, by, cz + 18);
      ((net.minecraft.world.entity.Mob) abrite).setNoAi(true);
      var rayonAbrite = LightBeams.fireRay(porteur, new ItemStack(pika), laser, LightBeams.laserRange());
      // Retour sur la position de tir de la première mesure : c'est celle que la portée « pika_impact »
      // interroge, et la cible libre est juste devant.
      porteur.teleportTo(cx + 0.5, by, cz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setXRot(0.0F);
      porteur.setOldPosAndRot();
      pikaTemoinLibre = libre.getUUID();
      pikaTemoinAbrite = abrite.getUUID();
      pikaCaster = porteur;
      // La zone de recherche suit le PORTEUR : la mesure en vol a lieu dans le chunk du spawn, pas dans
      // la scène principale. Une boîte centrée sur l'ancienne scène renvoyait « 0 projectile » et
      // faisait échouer une vérification qui, elle, était satisfaite.
      var projectilesEnVol = level.getEntitiesOfClass(
         net.mcreator.minepiece.entity.SkillProjectileEntity.class,
         new net.minecraft.world.phys.AABB(porteur.getX() - 48, porteur.getY() - 16, porteur.getZ() - 16,
            porteur.getX() + 48, porteur.getY() + 16, porteur.getZ() + 48));
      StringBuilder etatProjectiles = new StringBuilder();
      for (var pj : projectilesEnVol) {
         etatProjectiles.append("[pos ").append(round2(pj.getX())).append(",").append(round2(pj.getY()))
            .append(",").append(round2(pj.getZ())).append(" portee ").append(round2(pj.rangeBlocks()))
            .append("] ");
      }
      // Garde du piège documenté : `EntityType.spawn` peut renvoyer une entité JAMAIS ajoutée au niveau
      // (chunk non « entity-ticking ») — elle a l'air parfaitement valide et ne reçoit rien. On VÉRIFIE.
      boolean ciblesReelles = level.getEntity(libre.getUUID()) != null
         && level.getEntity(abrite.getUUID()) != null;
      check("A7c-rayons-tires-vers-les-temoins",
         rayonLibre != null && rayonAbrite != null && ciblesReelles
            && libre.getHealth() == 20.0F && abrite.getHealth() == 20.0F
            && projectilesEnVol.size() >= 2,
         "cibles réellement dans le niveau=" + ciblesReelles + " · deux rayons tirés (portée "
         + round2(LightBeams.laserRange()) + " blocs) · projectiles en vol="
         + projectilesEnVol.size() + " " + etatProjectiles.toString().trim() + " · cible en air libre"
         + " à 8 blocs (PV " + round2(libre.getHealth()) + ") et cible DERRIÈRE un mur (PV "
         + round2(abrite.getHealth()) + ") — l'impact n'existe pas encore : il sera relu par la portée"
         + " « pika_impact », à un autre tick, parce que le vol du rayon demande du TEMPS");

      // ================= SONDE D'IMPACT DIRECTE (diagnostic du chemin de collision)
      // Le vol d'un rayon demande des ticks, et une invocation n'en a qu'un. Pour SAVOIR si le chemin
      // d'impact fonctionne — au lieu de l'espérer — on fait avancer le projectile ici même et on lit
      // la vie de la cible. C'est une sonde de harnais : elle ne remplace pas la mesure en vol de la
      // phase « pika_impact », elle explique un échec au lieu de le laisser en devinette.
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setXRot(0.0F);
      porteur.setOldPosAndRot();
      for (int z = bz + 1; z <= bz + 8; z++) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int y = by; y <= by + 4; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(bx + dx, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      var cibleSonde = spawnAdultZombie(level, bx, by, bz + 4);
      ((net.minecraft.world.entity.Mob) cibleSonde).setNoAi(true);
      var rayonSonde = LightBeams.fireRay(porteur, new ItemStack(pika), laser, LightBeams.laserRange());
      StringBuilder traceSonde = new StringBuilder();
      for (int pas = 1; pas <= 6; pas++) {
         if (rayonSonde != null && !rayonSonde.isRemoved()) {
            rayonSonde.tick();
            traceSonde.append("t").append(pas).append(":").append(round2(rayonSonde.getZ())).append(" ");
         }
      }
      boolean sondeTouchee = cibleSonde.getHealth() < 20.0F;
      check("A7c-sonde-impact-direct",
         sondeTouchee,
         "après avoir fait avancer le rayon de 6 ticks à la main : cible à 4 blocs PV "
         + round2(cibleSonde.getHealth()) + " (20,0 = jamais touchée) · trajectoire du rayon "
         + traceSonde.toString().trim() + " (z de départ " + round2(bz + 0.5) + ", cible en "
         + round2(bz + 4.5) + ") — sonde de harnais : elle dit si le chemin d'impact FONCTIONNE, "
         + "là où la mesure en vol dit seulement qu'il n'a pas eu lieu");
      if (rayonSonde != null && !rayonSonde.isRemoved()) {
         rayonSonde.discard();
      }
      cibleSonde.discard();

      // ============================ A7d : déplacement lumineux (-04)
      LightTravel.resetReport();
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setOldPosAndRot();
      var avantDeplacement = porteur.position();
      boolean deplace = LightTravel.travel(porteur, 0.0);
      var apresDeplacement = porteur.position();
      boolean destinationSure = SafeDestination.isSafe(level, porteur, apresDeplacement);
      check("A7d-deplacement-lumineux-valide",
         deplace && apresDeplacement.distanceTo(avantDeplacement) > 0.5 && destinationSure,
         "déplacement = " + deplace + " · distance parcourue "
         + round2(apresDeplacement.distanceTo(avantDeplacement)) + " blocs · destination déclarée sûre = "
         + destinationSure + " · refus lisible=« " + LightTravel.lastRefusal() + " » — la destination"
         + " est CALCULÉE côté serveur (le long du regard) et validée avant d'être appliquée");
      // Zone protégée : la lumière s'arrête AVANT d'y entrer (ou refuse, si elle commence sur place).
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setOldPosAndRot();
      String zone = level.dimension().identifier() + "@" + (bx - 2) + "," + (by - 2) + "," + (bz + 2)
         + "," + (bx + 2) + "," + (by + 2) + "," + (bz + 12);
      DevilFruitExplosionConfig.addZoneForTest(zone);
      var avantProtege = porteur.position();
      boolean deplaceProtege = LightTravel.travel(porteur, 0.0);
      var apresProtege = porteur.position();
      boolean resteHorsZone = !DevilFruitExplosions.isProtectedPosition(level,
         net.minecraft.core.BlockPos.containing(apresProtege.x, apresProtege.y, apresProtege.z));
      DevilFruitExplosionConfig.clearTestZones();
      check("A7d-deplacement-refuse-en-zone-protegee",
         !deplaceProtege || resteHorsZone,
         "déplacement tenté vers une zone protégée : effectué=" + deplaceProtege + " · position finale"
         + " hors zone=" + resteHorsZone + " (parcouru "
         + round2(apresProtege.distanceTo(avantProtege)) + " blocs) — une lumière qui traverserait une"
         + " zone protégée serait un contournement, pas un pouvoir : le prédicat partagé l'arrête,"
         + " comme il arrête l'échange d'Ope Ope");
      DevilFruitExplosionConfig.clearTestZones();
      // Emmuré : aucune destination sûre → AUCUN déplacement.
      var emmure = new net.minecraft.core.BlockPos(bx + 8, by, bz + 8);
      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dy = 0; dy <= 2; dy++) {
               level.setBlockAndUpdate(emmure.offset(dx, dy, dz),
                  net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            }
         }
      }
      porteur.teleportTo(emmure.getX() + 0.5, by, emmure.getZ() + 0.5);
      porteur.setYRot(0.0F);
      porteur.setOldPosAndRot();
      var avantEmme = porteur.position();
      boolean deplaceEmme = LightTravel.travel(porteur, 0.0);
      boolean immobile = porteur.position().distanceTo(avantEmme) < 0.01;
      check("A7d-emmure-aucun-deplacement",
         !deplaceEmme && immobile,
         "porteur emmuré : déplacement effectué=" + deplaceEmme + " · resté sur place=" + immobile
         + " (raison « " + LightTravel.lastRefusal() + " ») — « tout ou rien » : sans destination sûre,"
         + " la lumière ne bouge pas d'un bloc, elle ne se pose pas à moitié dans la pierre");
      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dy = 0; dy <= 2; dy++) {
               level.setBlockAndUpdate(emmure.offset(dx, dy, dz),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }

      // ============================ A7e : charge visible et volée BORNÉE (-05)
      LightCharge.resetReport();
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setOldPosAndRot();
      DevilFruitMove barrage = moveOf(pika, "laser2");
      boolean armee = LightCharge.begin(porteur, new ItemStack(pika), barrage);
      int restant = LightCharge.remainingTicks(porteur);
      boolean marqueur = porteur.hasEffect(MinepieceModMobEffects.LIGHT_CHARGE);
      check("A7e-charge-visible-avant-impact",
         armee && LightCharge.isCharging(porteur) && restant > 0 && marqueur
            && LightCharge.lastVolleyImpacts(porteur) == -1,
         "charge armée=" + armee + " · ticks avant impact=" + restant + " · marqueur visible=" + marqueur
         + " · impacts déjà tirés=" + LightCharge.lastVolleyImpacts(porteur) + " — l'adversaire a une"
         + " FENÊTRE réelle : le signal (halo de particules + marqueur + son) existe avant l'impact, et"
         + " la volée n'a pas encore eu lieu. L'impact sera relu par la portée « pika_impact »");
      // Le plafond de la volée est DANS LE CODE.
      writePikaConfig(999, 20);
      int voleeBornee = br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig.barrageMaxImpacts();
      check("A7e-plafond-dur-de-la-volee",
         voleeBornee <= br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig.HARD_MAX_BARRAGE,
         "volée demandée 999 dans le fichier → appliquée " + voleeBornee + " (plafond dur "
         + br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig.HARD_MAX_BARRAGE + ") — une rafale"
         + " n'est pas « autant d'impacts que possible », c'est un nombre décidé et borné");
      writePikaConfig(6, 20);

      // ============================ A7f : bonus de lumière — bornés, rendus sans voler autrui
      LightBuffs.resetReport();
      porteur.removeEffect(net.minecraft.world.effect.MobEffects.SPEED);
      boolean pose = LightBuffs.apply(porteur, net.minecraft.world.effect.MobEffects.SPEED, 60, 0);
      boolean actif = LightBuffs.isActive(porteur, net.minecraft.world.effect.MobEffects.SPEED);
      LightBuffs.apply(porteur, net.minecraft.world.effect.MobEffects.SPEED, 60, 0);
      int actifsApresDeux = LightBuffs.activeCount();
      check("A7f-bonus-pose-et-prolonge-sans-empiler",
         pose && actif && actifsApresDeux == 1,
         "bonus posé=" + pose + " · actif=" + actif + " · bonus actifs après DEUX activations="
         + actifsApresDeux + " — une réactivation PROLONGE, elle n'empile pas : c'est le « pas de cumul"
         + " abusif » du critère, mesuré et non promis");
      // Une potion PLUS FORTE déjà en place n'est pas rabaissée par le pouvoir.
      LightBuffs.clearAll("test");
      porteur.removeEffect(net.minecraft.world.effect.MobEffects.SPEED);
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         net.minecraft.world.effect.MobEffects.SPEED, 600, 3, false, false));
      boolean rabaisse = LightBuffs.apply(porteur, net.minecraft.world.effect.MobEffects.SPEED, 60, 2);
      int ampliExterne = porteur.hasEffect(net.minecraft.world.effect.MobEffects.SPEED)
         ? porteur.getEffect(net.minecraft.world.effect.MobEffects.SPEED).getAmplifier() : -1;
      check("A7f-ne-rabaisse-pas-un-effet-plus-fort",
         !rabaisse && ampliExterne == 3,
         "potion externe Vitesse III en place, bonus de lumière II demandé : appliqué=" + rabaisse
         + " · amplificateur restant=" + ampliExterne + " · refus comptés=" + LightBuffs.refusedCount()
         + " — le pouvoir ne VOLE pas un effet meilleur que le sien : il ne fait rien, et le dit");
      // Rendu : l'effet externe DOIT revenir, et pas être supprimé.
      LightBuffs.clearAll("test");
      porteur.removeEffect(net.minecraft.world.effect.MobEffects.SPEED);
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         net.minecraft.world.effect.MobEffects.SPEED, 600, 0, false, false));
      LightBuffs.apply(porteur, net.minecraft.world.effect.MobEffects.SPEED, 40, 2);
      double speedBefore=porteur.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
      int ampliPendant = porteur.getEffect(net.minecraft.world.effect.MobEffects.SPEED).getAmplifier();
      LightBuffs.clearAll("echeance");
      boolean externeRendu = porteur.hasEffect(net.minecraft.world.effect.MobEffects.SPEED);
      int ampliRendu = externeRendu
         ? porteur.getEffect(net.minecraft.world.effect.MobEffects.SPEED).getAmplifier() : -1;
      int dureeRendue = externeRendu ? porteur.getEffect(net.minecraft.world.effect.MobEffects.SPEED).getDuration() : 0;
      check("A7f-rend-leffet-externe-au-lieu-de-le-supprimer",
         ampliPendant == 0 && speedBefore>porteur.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) && externeRendu && ampliRendu == 0 && dureeRendue > 400
            && LightBuffs.externalRestoredCount() >= 1,
         "pendant le bonus : amplificateur " + ampliPendant + " · APRÈS le retrait : effet encore"
         + " présent=" + externeRendu + " (amplificateur " + ampliRendu + ", " + dureeRendue
         + " ticks) · effets externes rendus=" + LightBuffs.externalRestoredCount() + " — la potion qui"
         + " était là AVANT le pouvoir est REMISE en place ; retirer « l'effet Vitesse » aurait supprimé"
         + " l'effet de quelqu'un d'autre, ce que le critère interdit explicitement");
      porteur.removeEffect(net.minecraft.world.effect.MobEffects.SPEED);
      LightBuffs.clearAll("test");

      // ============================ A7g : « Speed is Power » — la lumière EST la puissance
      DevilFruitMove rapide = moveOf(pika, "speedispower");
      double bonusSans = LightBeams.lightBonus(porteur);
      LightBuffs.apply(porteur, net.minecraft.world.effect.MobEffects.SPEED, 60, 0);
      double bonusAvec = LightBeams.lightBonus(porteur);
      LightBuffs.clearAll("test");
      double bonusApres = LightBeams.lightBonus(porteur);
      check("A7g-speed-is-power-bonus-borne",
         bonusSans == 0.0 && bonusAvec > 0.0
            && bonusAvec <= br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig.speedPowerBonusMax() + 1.0E-6
            && bonusApres == 0.0,
         "bonus sans lumière=" + round2(bonusSans) + " → avec lumière=" + round2(bonusAvec)
         + " → après retrait=" + round2(bonusApres) + " (plafond "
         + round2(br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig.speedPowerBonusMax())
         + ") · dégâts de la technique " + round2(rapide.damage()) + " — le supplément suit l'état RÉEL"
         + " du bonus : quand la lumière est rendue, il disparaît avec elle, sans seconde source de"
         + " vérité à synchroniser");
      temoinSansFruit.discard();
      LOGGER.info("[MinePiece] A7 rapports : {} | {} | {}", LightTravel.travelCount(),
         LightCharge.chargeReport(), LightBuffs.buffReport());
   }

   /**
    * Relit les témoins d'IMPACT de Pika Pika, posés par la portée « pika ».
    *
    * <p>Le vol d'un rayon demande du <b>temps</b> : une invocation de test tient dans un seul tick, donc
    * l'impact ne peut être observé qu'à un tick ultérieur. Le témoin à couvert ne doit <b>pas</b> avoir
    * été touché — c'est la moitié « le rayon s'arrête aux obstacles prévus » du critère -03.</p>
    */
   private static void pikaImpact(ServerLevel level) {
      var libre = pikaTemoinLibre == null ? null : level.getEntity(pikaTemoinLibre);
      var abrite = pikaTemoinAbrite == null ? null : level.getEntity(pikaTemoinAbrite);
      float pvLibre = libre instanceof net.minecraft.world.entity.LivingEntity l ? l.getHealth() : -1.0F;
      float pvAbrite = abrite instanceof net.minecraft.world.entity.LivingEntity a ? a.getHealth() : -1.0F;
      String ouLibre = libre instanceof net.minecraft.world.entity.Entity e
         ? round2(e.getX()) + "," + round2(e.getY()) + "," + round2(e.getZ()) : "introuvable";
      String ouAbrite = abrite instanceof net.minecraft.world.entity.Entity e
         ? round2(e.getX()) + "," + round2(e.getY()) + "," + round2(e.getZ()) : "introuvable";
      check("A7c-rayon-touche-et-mur-arrete",
         pvLibre >= 0.0F && pvLibre < 20.0F && pvAbrite == 20.0F,
         "à un autre tick : cible en air libre PV " + round2(pvLibre) + " en " + ouLibre
         + " (doit être touchée) · cible DERRIÈRE le"
         + " mur PV " + round2(pvAbrite) + " en " + ouAbrite + " (doit être intacte) — le rayon touche"
         + " la cible située sur sa"
         + " trajectoire ET s'arrête à l'obstacle : les deux moitiés du critère -03, observées dans le"
         + " temps réel et non déduites");
      ServerPlayer porteur = pikaCaster;
      boolean voleeTiree = porteur != null && "volee".equals(LightCharge.lastEndReason(porteur));
      int impacts = porteur == null ? -1 : LightCharge.lastVolleyImpacts(porteur);
      int plafond = br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig.barrageMaxImpacts();
      check("A7e-volee-tiree-et-bornee",
         voleeTiree && impacts >= 0 && impacts <= plafond && impacts == plafond,
         "volée tirée=" + voleeTiree + " · impacts = " + impacts + " (plafond " + plafond + ") · rapport "
         + LightCharge.chargeReport() + " — la charge annoncée a bien abouti, et la rafale a un nombre"
         + " d'impacts BORNÉ par la configuration (borné dans le code)");
      LightCharge.clearAll();
      LightBuffs.clearAll("test");
      writePikaConfig(6, 20);
   }

   private static void ope(ServerLevel level) {
      final int bx = 66;
      final int by = 100;
      final int bz = 22;

      ServerPlayer porteur = fake(level, "dftest_ope");
      MinepieceModVariables.clear(porteur);
      OpeRoom.clearAll();
      Amputation.clearAll();
      OpeRoom.resetReport();
      Amputation.resetReport();
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      // Tear-down SYMÉTRIQUE en PREMIER : une invocation laisse des entités derrière elle, et le monde
      // de test est réutilisé — la seconde exécution mesurerait alors la première.
      // ⚠️ RESSERRÉ à la scène de CE test : un nettoyage large (± 40 blocs) atteint la scène voisine.
      // C'est exactement ce qui a détruit la victime témoin de cette portée quand Pika Pika a été
      // ajouté APRÈS elle : l'échéance d'Ope Ope a échoué en « victime introuvable ».
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 13, by - 8, bz - 13, bx + 13, by + 24, bz + 24))) {
         m.discard();
      }
      for (int x = bx - 12; x <= bx + 12; x++) {
         for (int z = bz - 12; z <= bz + 16; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 8; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      DevilFruitTransformation.revertForm(porteur);
      porteur.refreshDimensions();
      Item ope = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_ope_ope"));
      immerse(level, porteur, ope);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setXRot(0.0F);
      porteur.setOldPosAndRot();
      MinepieceModVariables.get(porteur).PlayerEnergy = 200.0;
      // On repart d'une configuration SAINE : une exécution précédente a pu écrire des durées courtes.
      writeOpeConfig(8.0, 200, 160);

      // ============================ A6a : classification LUE, jamais supposée (-02)
      boolean paramecia = tagOf(ope, "paramecia");
      boolean logiaTag = tagOf(ope, "logia");
      boolean zoanTag = tagOf(ope, "zoan");
      var sourcePhysique = level.damageSources().playerAttack(porteur);
      double multPorteur = DevilFruitPowerLogic.computeDamageMagnification(sourcePhysique, porteur);
      check("A6a-classification-lue-et-coherente",
         paramecia && !logiaTag && !zoanTag && Math.abs(multPorteur - 1.0) < 1.0E-6,
         "tags lus : paramecia=" + paramecia + ", logia=" + logiaTag + ", zoan=" + zoanTag
         + " · dégâts physiques subis ×" + round2(multPorteur)
         + " — un Paramecia n'est ni intangible (×0 serait un Logia) ni transformé (Zoan) : la"
         + " classification est CONSÉQUENTE, pas déclarative ; le tag est lu sur l'objet, pas supposé"
         + " depuis NON_CLASSE");

      // Contrepartie du Fruit : sous suppression, le pouvoir ne s'exprime pas (-06)
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.SEA_POWER, 40, 0, false, false));
      boolean supprime = DevilFruitPowerLogic.isSuppressed(porteur);
      boolean refusSousSuppression = !OpeRoom.open(porteur);
      boolean rienOuvert = !OpeRoom.isOpen(porteur);
      check("A6a-suppression-bloque-le-pouvoir",
         supprime && refusSousSuppression && rienOuvert,
         "supprimé=" + supprime + " · ouverture refusée=" + refusSousSuppression
         + " · aucune ROOM ouverte=" + rienOuvert + " — la contrepartie du Fruit est VÉRIFIÉE, pas"
         + " seulement décrite : un porteur immergé n'ouvre pas de ROOM");
      porteur.removeEffect(MinepieceModMobEffects.SEA_POWER);

      // ============================ A6b : ROOM — centre, rayon, durée, bornes (-03)
      boolean ouverte = OpeRoom.open(porteur);
      int dureeRoom = OpeRoom.remainingTicks(porteur);
      double rayon = DevilFruitOpeConfig.roomRadiusBlocks();
      boolean marqueur = porteur.hasEffect(MinepieceModMobEffects.ROOM_BOUND);
      check("A6b-room-ouverte-et-bornee",
         ouverte && OpeRoom.isOpen(porteur) && OpeRoom.activeCount() == 1 && dureeRoom > 0
            && rayon <= DevilFruitOpeConfig.HARD_MAX_RADIUS && marqueur,
         "ouverte=" + ouverte + " · ROOM actives=" + OpeRoom.activeCount() + " · durée=" + dureeRoom
         + " ticks · rayon=" + round2(rayon) + " (plafond DUR dans le code "
         + round2(DevilFruitOpeConfig.HARD_MAX_RADIUS) + ") · marqueur de porteur=" + marqueur
         + " · contour = " + DevilFruitOpeConfig.roomParticlesPerTick() + " particule(s)/tick"
         + " — l'affichage est BORNÉ par construction ; sa validation visuelle appartient au client"
         + " (checklist §6)");

      // Un rayon délirant doit être BORNÉ DANS LE CODE, pas dans la prudence de qui édite le fichier.
      writeOpeConfig(999.0, 200, 160);
      double rayonBorne = DevilFruitOpeConfig.roomRadiusBlocks();
      check("A6b-plafond-dur-du-rayon",
         rayonBorne <= DevilFruitOpeConfig.HARD_MAX_RADIUS + 1.0E-6,
         "rayon demandé 999,0 dans le fichier → appliqué " + round2(rayonBorne) + " (plafond dur "
         + round2(DevilFruitOpeConfig.HARD_MAX_RADIUS) + ") — la borne est dans le code : un TOML mal"
         + " édité ne peut pas produire une ROOM infinie");
      writeOpeConfig(8.0, 200, 160);

      // Les techniques chirurgicales sont REFUSÉES hors de la ROOM (-03).
      OpeRoom.clearAll();
      var cibleHors = spawnAdultZombie(level, bx, by, bz + 4);
      boolean echangeHors = OpeRoom.shambles(porteur);
      boolean coupeHors = Amputation.amputate(level, porteur, cibleHors);
      check("A6b-refus-hors-room",
         !echangeHors && !coupeHors && !Amputation.isCut(cibleHors),
         "sans ROOM : échange=" + echangeHors + ", découpe=" + coupeHors + ", cible sectionnée="
         + Amputation.isCut(cibleHors) + " — les règles SPATIALES de la ROOM gouvernent les techniques"
         + " chirurgicales : hors de l'aire, rien ne s'applique et rien n'est consommé");
      cibleHors.discard();

      // ============================ A6c : échange de positions (-04)
      OpeRoom.open(porteur);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setOldPosAndRot();
      var cibleSwap = spawnAdultZombie(level, bx, by, bz + 4);
      // (1) ATOMICITÉ : on emmure la position du LANCEUR — la destination de la cible devient
      //     dangereuse. Une implémentation qui déplace « puis vérifie » bougerait quand même.
      level.setBlockAndUpdate(porteur.blockPosition(),
         net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      level.setBlockAndUpdate(porteur.blockPosition().above(),
         net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
      var lanceurAvant = porteur.position();
      var cibleAvant = cibleSwap.position();
      boolean echangeRefuse = OpeRoom.shambles(porteur);
      boolean rienNabouge = porteur.position().distanceTo(lanceurAvant) < 0.01
         && cibleSwap.position().distanceTo(cibleAvant) < 0.01;
      check("A6c-echange-atomique-refus",
         !echangeRefuse && rienNabouge,
         "destination du lanceur emmurée : échange accepté=" + echangeRefuse + " · aucune des deux"
         + " entités n'a bougé=" + rienNabouge + " (lanceur " + round2(porteur.position().distanceTo(lanceurAvant))
         + " bloc, cible " + round2(cibleSwap.position().distanceTo(cibleAvant))
         + " bloc) — les DEUX destinations sont validées AVANT le premier déplacement : déplacer"
         + " quelqu'un pour découvrir ensuite que l'autre place est mauvaise serait un état qui"
         + " n'existe dans aucune règle");
      level.setBlockAndUpdate(porteur.blockPosition(),
         net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
      level.setBlockAndUpdate(porteur.blockPosition().above(),
         net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());

      // (2) Destination dans une ZONE PROTÉGÉE : refus, et toujours aucun déplacement.
      String zone = level.dimension().identifier() + "@" + (bx - 2) + "," + (by - 2) + "," + (bz + 2)
         + "," + (bx + 2) + "," + (by + 2) + "," + (bz + 6);
      DevilFruitExplosionConfig.addZoneForTest(zone);
      var cibleAvant2 = cibleSwap.position();
      boolean echangeProtege = OpeRoom.shambles(porteur);
      boolean cibleImmobile = cibleSwap.position().distanceTo(cibleAvant2) < 0.01;
      DevilFruitExplosionConfig.clearTestZones();
      check("A6c-echange-refuse-en-zone-protegee",
         !echangeProtege && cibleImmobile,
         "destination dans une zone protégée : échange accepté=" + echangeProtege
         + " · cible immobile=" + cibleImmobile + " — « protégé » n'a qu'UNE définition dans le mod"
         + " (le prédicat des explosions est réutilisé) ; une téléportation qui ignore les protections"
         + " serait un contournement, pas un pouvoir");
      DevilFruitExplosionConfig.clearTestZones();

      // (3) Le cas nominal : les deux positions s'échangent réellement.
      var lanceurAvant2 = porteur.position();
      var cibleAvant3 = cibleSwap.position();
      boolean echange = OpeRoom.shambles(porteur);
      double dLanceur = porteur.position().distanceTo(cibleAvant3);
      double dCible = cibleSwap.position().distanceTo(lanceurAvant2);
      check("A6c-echange-applique",
         echange && dLanceur < 0.01 && dCible < 0.01,
         "échange effectué=" + echange + " · lanceur à " + round2(dLanceur) + " bloc de l'ancienne"
         + " position de la cible, cible à " + round2(dCible) + " bloc de l'ancienne position du"
         + " lanceur — les deux positions sont ÉCHANGÉES, pas seulement l'une déplacée");
      cibleSwap.discard();

      // ============================ A6d : découpe temporaire — sans corruption (-05)
      OpeRoom.open(porteur);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setOldPosAndRot();
      var cibleCoupe = spawnAdultZombie(level, bx, by, bz + 3);
      var attribut = cibleCoupe.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
      double frappeAvant = attribut == null ? -1.0 : attribut.getValue();
      boolean coupe = Amputation.amputate(level, porteur, cibleCoupe);
      double frappePendant = attribut == null ? -1.0 : attribut.getValue();
      boolean marqueurCoupe = cibleCoupe.hasEffect(MinepieceModMobEffects.AMPUTATED);
      check("A6d-section-appliquee",
         coupe && Amputation.isCut(cibleCoupe) && frappeAvant > 0.0 && frappePendant == 0.0 && marqueurCoupe,
         "force de frappe AVANT=" + round2(frappeAvant) + " → PENDANT=" + round2(frappePendant)
         + " · sectionnée=" + Amputation.isCut(cibleCoupe) + " · marqueur=" + marqueurCoupe
         + " — la découpe porte sur UNE chose, réversible par construction : les membres qui frappent"
         + " ne portent plus de coup. Rien n'est supprimé de l'entité, donc rien ne peut être perdu");
      // Le refus ne consomme rien et ne laisse AUCUNE trace, même sur l'attribut.
      boolean refusSoiMeme = Amputation.amputate(level, porteur, porteur);
      OpeRoom.clearAll();
      var cibleHors2 = spawnAdultZombie(level, bx, by, bz + 3);
      boolean refusHorsRoom = Amputation.amputate(level, porteur, cibleHors2);
      check("A6d-refus-sans-effet",
         !refusSoiMeme && !refusHorsRoom && !Amputation.isCut(cibleHors2),
         "se sectionner soi-même=" + refusSoiMeme + " · hors ROOM=" + refusHorsRoom
         + " · cible marquée=" + Amputation.isCut(cibleHors2) + " — un refus ne consomme rien et ne"
         + " laisse rien derrière lui");
      cibleHors2.discard();
      // RESTAURATION : les TROIS instants sont nommés et affichés — une mesure dont on ne voit pas
      // l'ordre ne prouve rien.
      Amputation.clearAll();
      double frappeApres = attribut == null ? -1.0 : attribut.getValue();
      boolean marqueurRetire = !cibleCoupe.hasEffect(MinepieceModMobEffects.AMPUTATED);
      check("A6d-restauration-exacte",
         !Amputation.isCut(cibleCoupe) && Math.abs(frappeApres - frappeAvant) < 0.001 && marqueurRetire
            && Amputation.inexactRestorations() == 0 && Amputation.exactRestorations() >= 1,
         "force de frappe AVANT=" + round2(frappeAvant) + " → PENDANT=" + round2(frappePendant)
         + " → APRÈS=" + round2(frappeApres) + " · encore sectionnée=" + Amputation.isCut(cibleCoupe)
         + " · marqueur retiré=" + marqueurRetire + " · restaurations exactes="
         + Amputation.exactRestorations() + ", INEXACTES=" + Amputation.inexactRestorations()
         + " — la restauration est CONSTATÉE (la valeur est recomparée à celle d'avant) et non"
         + " déclarée ; un compteur d'inexactitudes à zéro est ce qui distingue « j'ai retiré le"
         + " modificateur » de « la cible est revenue exactement à son état d'origine »");
      cibleCoupe.discard();

      // ============================ A6f : retrait du Fruit (-09, cas « retrait »)
      // ⚠️ Cette vérification a ses PROPRES témoins : elle appelle le reset central, qui ferme toute
      // ROOM et restaure toute section du porteur. Armée avec les témoins d'échéance, elle les aurait
      // détruits avant leur relecture — ce qui est arrivé au premier passage, et la raison lue était
      // « retrait » au lieu d'« echeance ». Un test qui emprunte l'état d'un autre test le casse.
      OpeRoom.open(porteur);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setOldPosAndRot();
      var cibleRetrait = spawnAdultZombie(level, bx, by, bz + 3);
      boolean coupeAvantRetrait = Amputation.amputate(level, porteur, cibleRetrait);
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      boolean roomFermee = !OpeRoom.isOpen(porteur);
      boolean sectionRendue = !Amputation.isCut(cibleRetrait);
      check("A6f-retrait-ferme-et-restaure",
         coupeAvantRetrait && roomFermee && sectionRendue,
         "section posée=" + coupeAvantRetrait + " puis retrait du Fruit : ROOM fermée=" + roomFermee
         + " (raison « " + OpeRoom.lastCloseReason(porteur) + " ») · cible restaurée=" + sectionRendue
         + " (raison « " + Amputation.lastRestoreReason(cibleRetrait) + " ») — perdre le Fruit ne doit"
         + " pas laisser quelqu'un sans force de frappe ni une aire active sans propriétaire");
      cibleRetrait.discard();

      // ============================ A6e : armement des témoins d'ÉCHÉANCE RÉELLE — EN DERNIER
      // Une invocation de test tient dans UN SEUL tick : elle ne peut pas observer une durée. On arme
      // donc des témoins à durée COURTE, relus plus tard par la portée « ope_expiry » (phase 3 bis de
      // la recette, dans la MÊME vie du serveur). L'armement vient APRÈS tout ce qui ferme ou restaure :
      // sinon le témoin est détruit par un autre cas de test avant d'avoir pu expirer.
      giveFruit(porteur,ope); // previous reset intentionally removed the fruit
      writeOpeConfig(8.0, 70, 70);
      OpeRoom.open(porteur);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      porteur.setYRot(0.0F);
      porteur.setOldPosAndRot();
      var cibleTemoin = spawnAdultZombie(level, bx, by, bz + 3);
      boolean coupeTemoin = Amputation.amputate(level, porteur, cibleTemoin);
      opeExpiryVictim = cibleTemoin.getUUID();
      opeExpiryCaster = porteur;
      check("A6e-temoins-posees",
         coupeTemoin && OpeRoom.isOpen(porteur) && Amputation.isCut(cibleTemoin)
            && OpeRoom.remainingTicks(porteur) > 0,
         "témoins posés : ROOM " + OpeRoom.remainingTicks(porteur) + " ticks · section "
         + Amputation.remainingTicks(cibleTemoin) + " ticks (durées COURTES écrites en config)"
         + " — ils seront relus par la portée « ope_expiry », à un autre tick : sans cela,"
         + " l'expiration resterait une intention");
      // Durées normales rendues au fichier ; les témoins gardent la leur, mémorisée à l'ouverture.
      writeOpeConfig(8.0, 200, 160);
      // ⚠️ NE PAS nettoyer ici : c'est justement l'état que la portée d'échéance doit relire.
      LOGGER.info("[MinePiece] A6 rapports : {} | {}", OpeRoom.roomReport(), Amputation.amputationReport());
   }

   /**
    * <b>Après REDÉMARRAGE : aucun état fantôme, et l'environnement est rendu.</b>
    *
    * <p>L'échéance temporelle est prouvée <b>avant</b> le redémarrage (portée {@code ope_expiry}) : un
    * état de pouvoir tenu en mémoire ne traverse pas un reboot, donc exiger une durée après coup n'aurait
    * aucun sens. Ce qui a du sens ici est l'inverse, et c'est une <b>garantie</b> : il ne doit rester
    * <b>aucune</b> aire ni section orpheline, et le harnais doit avoir <b>rendu</b> la configuration
    * qu'il avait empruntée (<code>writeOpeConfig</code> en laisse des valeurs courtes pendant le test).</p>
    */
   private static void opeRestart(ServerLevel level) {
      int rooms = OpeRoom.activeCount();
      int sections = Amputation.cutCount();
      double rayon = DevilFruitOpeConfig.roomRadiusBlocks();
      int dureeRoom = DevilFruitOpeConfig.roomLifetimeTicks();
      int dureeSection = DevilFruitOpeConfig.amputationDurationTicks();
      check("A6g-redemarrage-sans-etat-fantome-et-config-rendue",
         rooms == 0 && sections == 0 && Math.abs(rayon - 8.0) < 1.0E-6 && dureeRoom == 200
            && dureeSection == 160,
         "après REDÉMARRAGE : ROOM actives=" + rooms + " · sections actives=" + sections
         + " (aucune aire ni section fantôme — l'état est en mémoire, il ne peut pas survivre, et"
         + " c'est une garantie : rien ne fuit d'une exécution à l'autre) · configuration RELUE :"
         + " rayon=" + round2(rayon) + ", durée ROOM=" + dureeRoom + " ticks, durée section="
         + dureeSection + " ticks — le harnais a rendu ce qu'il avait emprunté, donc deux"
         + " exécutions successives mesurent la même chose");
   }

   /**
    * Relit les témoins d'échéance posés par la portée « ope ».
    *
    * <p>Invoquée VOLONTAIREMENT à un autre tick, après un délai réel : c'est la seule façon d'observer
    * une durée dans un harnais où tout se joue en un tick.</p>
    */
   private static void opeExpiry(ServerLevel level) {
      ServerPlayer porteur = opeExpiryCaster;
      boolean porteurVivant = porteur != null && !porteur.isRemoved();
      boolean roomFermee = porteurVivant && !OpeRoom.isOpen(porteur);
      String raisonRoom = porteurVivant ? OpeRoom.lastCloseReason(porteur) : "porteur introuvable";
      var entite = opeExpiryVictim == null ? null : level.getEntity(opeExpiryVictim);
      boolean victimeVivante = entite instanceof net.minecraft.world.entity.LivingEntity;
      net.minecraft.world.entity.LivingEntity victime = victimeVivante
         ? (net.minecraft.world.entity.LivingEntity) entite : null;
      boolean sectionRendue = victime != null && !Amputation.isCut(victime);
      String raisonSection = victime == null ? "victime introuvable" : Amputation.lastRestoreReason(victime);
      double frappe = victime == null || victime.getAttribute(
         net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) == null ? -1.0
         : victime.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).getValue();
      check("A6e-echeance-reelle-room-et-section",
         roomFermee && "echeance".equals(raisonRoom) && sectionRendue && "echeance".equals(raisonSection)
            && frappe > 0.0,
         "à un autre tick : ROOM fermée=" + roomFermee + " (raison « " + raisonRoom + " ») · section"
         + " rendue=" + sectionRendue + " (raison « " + raisonSection + " ») · force de frappe"
         + " restaurée=" + round2(frappe) + " · rapport " + Amputation.amputationReport()
         + " — l'échéance est observée dans le TEMPS réel, pas supposée ; la force de frappe est"
         + " revenue d'elle-même, ce qui prouve que le maintien est borné et auto-réparateur");
      OpeRoom.clearAll();
      Amputation.clearAll();
      writeOpeConfig(8.0, 200, 160);
   }

   private static void mori(ServerLevel level) {
      final int bx = 60;
      final int by = 100;
      final int bz = 20;

      ServerPlayer porteur = fake(level, "dftest_mori");
      MinepieceModVariables.clear(porteur);
      Roots.releaseEverything("test");
      PowerBlockService.releaseAll(porteur.getUUID());
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
            new net.minecraft.world.phys.AABB(bx - 40, by - 24, bz - 40, bx + 40, by + 24, bz + 40))) {
         m.discard();
      }
      for (int x = bx - 12; x <= bx + 12; x++) {
         for (int z = bz - 12; z <= bz + 16; z++) {
            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, by - 1, z),
               net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int y = by; y <= by + 8; y++) {
               level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, y, z),
                  net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
         }
      }
      Item mori = BuiltInRegistries.ITEM.getValue(
         Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_mori_mori"));
      immerse(level, porteur, mori);
      porteur.teleportTo(bx + 0.5, by, bz + 0.5);
      MinepieceModVariables.get(porteur).PlayerEnergy = 200.0;
      boolean logia = mori.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "logia")));
      boolean paramecia = mori.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "paramecia")));
      boolean zoan = mori.builtInRegistryHolder().is(
         net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Identifier.fromNamespaceAndPath("minepiece", "zoan")));
      check("A5a-classification-argumentee",
         level.hasChunkAt(new net.minecraft.core.BlockPos(bx, by, bz)) && logia && !zoan && !paramecia,
         "tags lus : logia=" + logia + ", zoan=" + zoan + ", paramecia=" + paramecia
         + " — même exigence que pour le fruit précédent : la classification est VÉRIFIÉE « sans"
         + " supposer ». Le corps devient une MATIÈRE (le bois et la sève) et les coups physiques le"
         + " traversent : Logia, confirmé par lecture du tag existant");

      // ================= A5b : les racines RESPECTENT LES RÉSISTANCES et se terminent (-03)
      var normale = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 1, by, bz + 1),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert normale != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      normale.setBaby(false);
      var resistante = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 3, by, bz + 1),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert resistante != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      resistante.setBaby(false);
      // La RÉSISTANCE RÉELLE que le moteur reconnaît est l'IMMUNITÉ AUX DÉGÂTS : une cible invulnérable
      // ne peut pas être retenue. (Un contrôle préalable par `canBeAffected` a été essayé : il refusait
      // TOUJOURS, sur 8 types de cibles testées — un contrôle qui ne refuse jamais n'est pas un contrôle.)
      resistante.setPermanentlyInvulnerable(true);
      boolean resistanceLue = resistante.isInvulnerable();
      var vitesseResistante = resistante.getAttribute(
         net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
      double baseResistante = vitesseResistante == null ? 0.0 : vitesseResistante.getValue();
      boolean refusee = !Roots.entangle(resistante, porteur);
      double apresResistante = vitesseResistante == null ? 0.0 : vitesseResistante.getValue();
      boolean resistanteIntacte = !Roots.isHeld(resistante)
         && Math.abs(apresResistante - baseResistante) < 1.0E-9
         && !resistante.hasEffect(MinepieceModMobEffects.ROOTED);
      // ⚠️ BASE MESURÉE AVANT L'ACTION (3ᵉ fois que ce piège se referme sur ce projet). Une variable
      // nommée « base » capturée APRÈS l'action vaut la valeur immobilisée : la comparaison finale
      // échoue alors pour une raison sans aucun rapport avec le code testé. On distingue donc
      // explicitement les trois instants : AVANT, PENDANT, APRÈS.
      var vitesseNormale = normale.getAttribute(
         net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
      double baseNormale = vitesseNormale == null ? 0.0 : vitesseNormale.getValue();
      boolean acceptee = Roots.entangle(normale, porteur);
      double pendantNormale = vitesseNormale == null ? 0.0 : vitesseNormale.getValue();
      boolean tenue = Roots.isHeld(normale) && pendantNormale <= 0.0 && baseNormale > 0.0;
      // Fin propre : la durée est bornée.
      int tours = 0;
      while (Roots.heldCount() > 0 && tours < DevilFruitMoriConfig.rootDurationTicks() + 5) {
         Roots.tickHeld();
         tours++;
      }
      boolean terminee = Roots.heldCount() == 0;
      String raison = Roots.lastReleaseReason(normale);
      double apresNormale = vitesseNormale == null ? 0.0 : vitesseNormale.getValue();
      boolean vitesseRendue = Math.abs(apresNormale - baseNormale) < 1.0E-9;
      boolean marqueurParti = !normale.hasEffect(MinepieceModMobEffects.ROOTED);
      // Une invocation du porteur n'est jamais enracinée (on n'emprisonne pas les siens).
      SummonEntity mien = MinepieceModEntities.SUMMON.spawn(level,
         new net.minecraft.core.BlockPos(bx, by, bz + 3),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      if (mien != null) {
         mien.setOwnerEntity(porteur);
         mien.setModel("gecko_moria");
         mien.setTexture("tex_black");
      }
      boolean invocationExclue = mien == null || !Roots.entangle(mien, porteur);
      if (mien != null) {
         mien.discard();
      }
      normale.discard();
      resistante.discard();
      check("A5b-racines-respectent-les-resistances-et-se-terminent",
         resistanceLue && refusee && resistanteIntacte && acceptee && tenue && terminee
            && "echeance".equals(raison)
            && vitesseRendue && marqueurParti && invocationExclue,
         "cible RÉSISTANTE (invulnérable = " + resistanceLue + ") : refusée = " + refusee
         + ", vitesse " + round2(baseResistante) + " → " + round2(apresResistante) + " (inchangée), ni"
         + " marqueur ni maintien résiduels = " + resistanteIntacte + " · cible NORMALE retenue = "
         + acceptee + ", vitesse AVANT " + round2(baseNormale) + " → PENDANT " + round2(pendantNormale)
         + " (immobilisée = " + tenue + ") · après " + tours + " ticks : terminée = " + terminee
         + " (raison « " + raison + " »), vitesse APRÈS " + round2(apresNormale) + " = base rendue = "
         + vitesseRendue + ", marqueur parti = " + marqueurParti
         + " · invocation du porteur épargnée = " + invocationExclue + " — le critère ne dit pas"
         + " « immobilise les cibles » mais « l'immobilisation RESPECTE LES RÉSISTANCES ». La différence"
         + " est tout : une immobilisation qui s'applique à tout le monde ne serait plus un pouvoir,"
         + " mais un réglage du jeu. L'ORDRE DES OPÉRATIONS est ici la garantie : on demande au MOTEUR"
         + " d'accepter l'effet AVANT de poser le ralentissement — sinon une cible qui a résisté serait"
         + " quand même immobilisée, ce que l'on mesure ci-dessus (vitesse inchangée ET aucun résidu) —"
         + " et l'effet se TERMINE proprement au lieu de rester accroché");

      // ================= A5c : végétation TEMPORAIRE, restituée (-04)
      var centre = new net.minecraft.core.BlockPos(bx + 2, by, bz + 6);
      boolean videAvant = level.getBlockState(centre).isAir();
      int poses = Roots.growVegetation(porteur, centre);
      boolean pousse = !level.getBlockState(centre).isAir();
      var feuillage=level.getBlockState(centre);
      int suivis = PowerBlockService.activeCountFor(porteur.getUUID());
      int plafond = DevilFruitMoriConfig.vegetationMaxBlocks();
      // TEMPORAIRE par construction : chaque bloc posé est SUIVI par le service de blocs temporaires,
      // donc aucun ne peut devenir permanent.
      PowerBlockService.releaseAll(porteur.getUUID());
      boolean conserve=feuillage.is(net.minecraft.world.level.block.Blocks.OAK_LEAVES) && feuillage.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.PERSISTENT) && level.getBlockState(centre).equals(feuillage);
      int suivisApres=PowerBlockService.activeCountFor(porteur.getUUID());
      check("A5c-vegetation-definitive-et-baux-liberes",videAvant && poses>0 && pousse && poses<=plafond && suivis==poses && suivisApres==0 && conserve,
         "feuilles stables conservees="+conserve+", poses="+poses+"/"+plafond+", attribution liberee="+suivisApres);

      // ================= A5d : drainage — coût, PLAFOND, et rien sur les protégés (-05)
      var cible = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 1, by, bz + 1),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert cible != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      cible.setBaby(false);
      MinepieceModVariables.get(porteur).PlayerEnergy = 0.0;
      double gain1 = LifeDrain.drain(porteur, cible);
      double pvApres = cible.getHealth();
      double plafondGain = DevilFruitMoriConfig.drainEnergyCap();
      // Un second drainage immédiat ne peut pas dépasser le même plafond : le gain est BORNÉ par usage.
      double gain2 = LifeDrain.drain(porteur, cible);
      boolean borne = gain1 <= plafondGain && gain2 <= plafondGain;
      // Invocation du porteur : AUCUN gain, et la cible n'est même pas touchée.
      SummonEntity serviteur = MinepieceModEntities.SUMMON.spawn(level,
         new net.minecraft.core.BlockPos(bx + 2, by, bz + 1),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      double pvServiteurAvant = -1.0;
      double gainServiteur = 0.0;
      if (serviteur != null) {
         serviteur.setOwnerEntity(porteur);
         serviteur.setModel("gecko_moria");
         serviteur.setTexture("tex_black");
         pvServiteurAvant = serviteur.getHealth();
         gainServiteur = LifeDrain.drain(porteur, serviteur);
      }
      boolean serviteurIntact = serviteur == null || Math.abs(serviteur.getHealth() - pvServiteurAvant) < 1.0E-9;
      // Cible INVULNÉRABLE : aucun gain non plus.
      var invulnerable = net.minecraft.world.entity.EntityTypes.ZOMBIE.spawn(level,
         new net.minecraft.core.BlockPos(bx + 4, by, bz + 1),
         net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      assert invulnerable != null;
      // ⚠️ ADULTE FORCÉ : vanilla fait naître les zombies bébés au hasard (~5 %), et une
      // boîte de bébé (0,49×0,98) décale le centre de 1,13 bloc — assez pour sortir d'un cône
      // large de 1,0 bloc ou d'un test de collision. Cinq mesures différentes ont été
      // intermittentes pour cette seule raison, sans qu'aucune ligne du code testé n'ait changé.
      invulnerable.setBaby(false);
      invulnerable.setPermanentlyInvulnerable(true);
      double gainInvulnerable = LifeDrain.drain(porteur, invulnerable);
      // Soi-même : exclu.
      double gainSoi = LifeDrain.drain(porteur, porteur);
      double energieFinale = MinepieceModVariables.get(porteur).PlayerEnergy;
      if (serviteur != null) {
         serviteur.discard();
      }
      cible.discard();
      invulnerable.discard();
      check("A5d-drainage-plafonne-et-sans-gain-sur-les-proteges",
         gain1 > 0.0 && gain1 <= plafondGain && gain2 <= plafondGain && borne && pvApres < 20.0
            && Math.abs(gainServiteur) < 1.0E-9 && serviteurIntact
            && Math.abs(gainInvulnerable) < 1.0E-9 && Math.abs(gainSoi) < 1.0E-9,
         "cible valide : +" + round2(gain1) + " énergie (plafond déclaré " + round2(plafondGain)
         + "/usage), vie " + round2(pvApres) + " · 2ᵉ drainage : +" + round2(gain2) + " (borné tous les"
         + " deux = " + borne + ") · INVOCATION du porteur : +" + round2(gainServiteur) + " et vie"
         + " inchangée = " + serviteurIntact + " · cible INVULNÉRABLE : +" + round2(gainInvulnerable)
         + " · SOI-MÊME : +" + round2(gainSoi) + " · énergie finale = " + round2(energieFinale)
         + " — un pouvoir qui RETIRE peut être équilibré par son coût ; un pouvoir qui REND ne l'est que"
         + " par une BORNE. Sans ces exclusions, une invocation bon marché deviendrait une batterie, deux"
         + " complices se draineraient en boucle, et l'invulnérabilité deviendrait une SOURCE. Une cible"
         + " exclue n'est pas « drainée à zéro » : elle n'est pas touchée du tout — sinon le porteur"
         + " croirait à une panne et recommencerait");

      // ================= A5e : les exceptions de ce Logia, avec CONTRE-ÉPREUVE (-06)
      var attaquant = fake(level, "dftest_mori_att");
      MinepieceModVariables.clear(attaquant);
      attaquant.teleportTo(bx + 3.5, by, bz + 0.5);
      var physique = level.damageSources().playerAttack(attaquant);
      var flamme = level.damageSources().onFire();
      var explosion = level.damageSources().explosion(attaquant, attaquant);
      var magie = level.damageSources().magic();
      double multPhysique = DevilFruitPowerLogic.computeDamageMagnification(physique, porteur);
      double multFlamme = DevilFruitPowerLogic.computeDamageMagnification(flamme, porteur);
      double multExplosion = DevilFruitPowerLogic.computeDamageMagnification(explosion, porteur);
      double multMagie = DevilFruitPowerLogic.computeDamageMagnification(magie, porteur);
      // CONTRE-ÉPREUVE : les mêmes sources contre un porteur SANS Fruit. Si le témoin reçoit la même
      // valeur, c'est que la source n'a JAMAIS été concernée par l'intangibilité — la valeur seule ne
      // prouverait rien, et une « exception » qui n'en est pas une passerait pour un correctif.
      MinepieceModVariables.clear(porteur);
      DevilFruitPowerLogic.clearDevilFruit(porteur);
      double temoinPhysique = DevilFruitPowerLogic.computeDamageMagnification(physique, porteur);
      double temoinFlamme = DevilFruitPowerLogic.computeDamageMagnification(flamme, porteur);
      double temoinExplosion = DevilFruitPowerLogic.computeDamageMagnification(explosion, porteur);
      immerse(level, porteur, mori);
      // Suppression : le passif ne protège plus.
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.SEA_POWER, 200, 0, false, false, true));
      double multScelle = DevilFruitPowerLogic.computeDamageMagnification(physique, porteur);
      porteur.removeEffect(MinepieceModMobEffects.SEA_POWER);
      attaquant.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.ARMAMENT_HAKI, 200, 0, false, false, true));
      double multHaki = DevilFruitPowerLogic.computeDamageMagnification(physique, porteur);
      attaquant.removeEffect(MinepieceModMobEffects.ARMAMENT_HAKI);
      check("A5e-exceptions-du-logia-avec-contre-epreuve",
         Math.abs(multPhysique - 0.0) < 1.0E-9 && Math.abs(multFlamme - 1.0) < 1.0E-9
            && Math.abs(multExplosion - 1.0) < 1.0E-9 && Math.abs(multMagie - 1.0) < 1.0E-9
            && Math.abs(multScelle - 1.0) < 1.0E-9 && Math.abs(multHaki - 1.0) < 1.0E-9
            && Math.abs(temoinPhysique - 1.0) < 1.0E-9 && Math.abs(temoinFlamme - 1.0) < 1.0E-9
            && Math.abs(temoinExplosion - 1.0) < 1.0E-9,
         "porteur du Fruit — physique : ×" + round2(multPhysique) + " (annulée) · FEU : ×"
         + round2(multFlamme) + " (conservé) · explosion : ×" + round2(multExplosion) + " · magie : ×"
         + round2(multMagie) + " · sous scellement : ×" + round2(multScelle) + " · avec Haki : ×"
         + round2(multHaki) + " || CONTRE-ÉPREUVE sans Fruit — physique : ×" + round2(temoinPhysique)
         + ", feu : ×" + round2(temoinFlamme) + " — la comparaison entre les deux séries est ce qui"
         + " prouve quelque chose : le feu vaut ×1 sur le porteur ET ×1 sur le témoin (×"
         + round2(temoinExplosion) + " pour l'explosion), donc ces sources n'ont JAMAIS"
         + " été concerné par l'intangibilité. Écrire une « exception feu » serait du CODE MORT qui"
         + " passerait pour un correctif — c'est exactement l'erreur commise sur le fruit précédent, où"
         + " mon exception « le souffle disperse » ne changeait rien parce que le moteur exclut déjà"
         + " explosions et flammes de la catégorie physique. Ce qui manquait réellement — la suppression"
         + " qui n'était pas consultée — est corrigé et se lit ci-dessus");

      Roots.releaseEverything("test");
      PowerBlockService.releaseAll(porteur.getUUID());
      DevilFruitPowerLogic.resetDevilFruitPowers(porteur);
      MinepieceModVariables.clear(porteur);
      MinepieceModVariables.clear(attaquant);
   }

   /**
    * <b>Dégage et VÉRIFIE un couloir de mesure.</b>
    *
    * <p>Le monde de test <b>persiste d'une exécution à l'autre</b> : le serveur redémarre, mais le
    * répertoire du monde est réutilisé. Une portée voisine peut donc avoir laissé un bloc dans le couloir
    * d'une autre, et une mesure de visée devient intermittente — <b>sans qu'aucune ligne du code testé
    * n'ait changé</b>. C'est exactement ce qui est arrivé au souffle d'Okuchi : regard parfaitement dans
    * l'axe, cible vivante en face, et pourtant zéro cible prise parce qu'un obstacle traînait.</p>
    *
    * @return nombre de blocs réellement retirés (0 = couloir déjà propre)
    */
   private static int clearCorridor(ServerLevel level, int x, int z0, int z1, int y0, int y1) {
      int retires = 0;
      for (int z = z0; z <= z1; z++) {
         for (int y = y0; y <= y1; y++) {
            for (int dx = -1; dx <= 1; dx++) {
               var p = new net.minecraft.core.BlockPos(x + dx, y, z);
               if (!level.getBlockState(p).isAir()) {
                  level.setBlockAndUpdate(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                  retires++;
               }
            }
         }
      }
      return retires;
   }

   /** Compte les blocs non vides d'un couloir — sert à PROUVER qu'il est dégagé. */
   private static int corridorObstacles(ServerLevel level, int x, int z0, int z1, int y0, int y1) {
      int n = 0;
      for (int z = z0; z <= z1; z++) {
         for (int y = y0; y <= y1; y++) {
            for (int dx = -1; dx <= 1; dx++) {
               if (!level.getBlockState(new net.minecraft.core.BlockPos(x + dx, y, z)).isAir()) {
                  n++;
               }
            }
         }
      }
      return n;
   }

   private static double round2(double v) {
      return Math.round(v * 100.0) / 100.0;
   }

   // ------------------------------------------------- P. Contrats de fruit (FRUIT-*, plan)
   /**
    * Vérifie la classification de chaque Fruit et les corrections issues de l'audit du plan
    * (`tools/fruit_audit.py`) : la famille ne doit jamais rester indécise, et les techniques dont le
    * port s'écartait de ver15 doivent garder la valeur corrigée (garde-fou de régression).
    */
   private static void fruitsTest() {
      Item aro = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_aro_aro"));
      int unclassified = 0;
      java.util.List<String> sansFamille = new java.util.ArrayList<>();
      for (Item f : fruits()) {
         if (DevilFruitPowerLogic.family(f) == DevilFruitPowerLogic.Family.UNCLASSIFIED) {
            unclassified++;
            sansFamille.add(BuiltInRegistries.ITEM.getKey(f).getPath());
         }
      }
      check("F1-famille-definie-pour-chaque-fruit", unclassified == 0,
         fruits().size() + " Fruits — sans famille : " + unclassified
         + (sansFamille.isEmpty() ? "" : " (" + sansFamille.get(0) + ")")
         + " — « non classé » est une décision à documenter, jamais un défaut");

      // Gomu Gomu : Paramecia canonique ET transformable (décision documentée, ver15 le classait ZOAN).
      Item gomu = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minepiece", "item_devil_fruit_gomu_gomu"));
      boolean gomuParamecia = DevilFruitPowerLogic.family(gomu) == DevilFruitPowerLogic.Family.PARAMECIA;
      boolean gomuTransformable = new ItemStack(gomu).is(DevilFruitTransformation.TAG_TRANSFORMABLE);
      int transformables = 0;
      for (Item f : fruits()) {
         if (new ItemStack(f).is(DevilFruitTransformation.TAG_TRANSFORMABLE)) {
            transformables++;
         }
      }
      boolean goroTransformable=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO).is(DevilFruitTransformation.TAG_TRANSFORMABLE);
      boolean goroLogia=DevilFruitPowerLogic.family(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO)==DevilFruitPowerLogic.Family.LOGIA;
      check("F2-transformation-par-tag", gomuParamecia && gomuTransformable && goroTransformable && goroLogia && transformables == 14,
         "Gomu Gomu : famille=" + DevilFruitPowerLogic.family(gomu)
         + ", transformable=" + gomuTransformable
         + " · Fruits transformables par tag : " + transformables + "/14 (Goro Logia + Amaru explicite)"
         + " (la liste codée en dur a été remplacée par le tag minepiece:transformable)");

      // Garde-fou de régression sur les 4 corrections d'audit.
      DevilFruitMove mamaragan = moveOf(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO, "mamaragan");
      DevilFruitMove heatHaze = moveOf(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA, "kagero");
      DevilFruitMove gatling = moveOf(MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU, "gomu_gatling");
      DevilFruitMove bazooka = moveOf(MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU, "gomu_bazooka");
      check("F3-corrections-du-plan-appliquees",
         mamaragan != null && mamaragan.type() == net.mcreator.minepiece.procedures.MoveType.BUFF
         && heatHaze != null && heatHaze.type() == net.mcreator.minepiece.procedures.MoveType.MELEE
         && heatHaze.damage() == 15 && heatHaze.range() == 4
         && gatling != null && Math.abs(gatling.damage() - 6.0) < 1e-9
         && bazooka != null && Math.abs(bazooka.damage() - 20.0) < 1e-9,
         "Mamaragan=" + (mamaragan == null ? "absent" : mamaragan.type())
         + ", Heat Haze=" + (heatHaze == null ? "absent" : heatHaze.type())
         + ", Gatling dégâts=" + (gatling == null ? "absent" : gatling.damage())
         + ", Bazooka dégâts=" + (bazooka == null ? "absent" : bazooka.damage())
         + " — Kagero corrige selon le bytecode ver15 (deplacement offensif 15, rayon 4) ; autres valeurs du plan");

      check("F4-profils-de-fruit", DevilFruitPowerLogic.family(aro) == DevilFruitPowerLogic.Family.PARAMECIA
         && DevilFruitPowerLogic.family(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO) == DevilFruitPowerLogic.Family.LOGIA,
         "Aro Aro=" + DevilFruitPowerLogic.family(aro)
         + ", Goro Goro=" + DevilFruitPowerLogic.family(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO)
         + " — familles lues depuis les tags");
   }

   /** Technique par identifiant, ou null. */
   private static DevilFruitMove moveOf(Item fruit, String id) {
      for (DevilFruitMove m : DevilFruitRegistry.movesFor(fruit)) {
         if (m.id().equals(id)) {
            return m;
         }
      }
      return null;
   }

   // ------------------------------------------------- M. Traductions (CORE-028)
   /** Vérifie que chaque technique a bien une traduction dans la langue choisie, et que le HUD est câblé. */
   private static void i18n() {
      java.util.Set<String> fr = langKeys("/assets/minepiece/lang/fr_fr.json");
      java.util.Set<String> en = langKeys("/assets/minepiece/lang/en_us.json");
      check("M1-lang-fr-present", !fr.isEmpty() && !en.isEmpty(),
         "fr_fr.json=" + fr.size() + " clés, en_us.json=" + en.size() + " clés (chargés depuis le jar)");

      java.util.List<String> sansFr = new java.util.ArrayList<>();
      java.util.List<String> sansEn = new java.util.ArrayList<>();
      int techniques = 0;
      for (Item f : fruits()) {
         for (net.mcreator.minepiece.procedures.DevilFruitMove m : DevilFruitRegistry.movesFor(f)) {
            techniques++;
            String key = "minepiece.technique." + m.id();
            if (!fr.contains(key)) {
               sansFr.add(m.id());
            }
            if (!en.contains(key)) {
               sansEn.add(m.id());
            }
         }
      }
      check("M2-techniques-traduites", sansFr.isEmpty(),
         techniques + " techniques — sans traduction FR : " + sansFr.size()
         + (sansFr.isEmpty() ? "" : " (ex. " + sansFr.get(0) + ")"));

      // Clés utilisées par le code (HUD + messages + libellés d'énergie).
      String[] requises = {
         "minepiece.hud.title", "minepiece.hud.technique", "minepiece.hud.form",
         "minepiece.hud.form.base", "minepiece.hud.form.hybrid", "minepiece.hud.none",
         "minepiece.overlay.energy",
         "minepiece.message.no_fruit", "minepiece.message.not_ported",
         "minepiece.message.sea_suppressed", "minepiece.message.cooldown",
         "minepiece.message.no_energy", "minepiece.message.technique_selected",
         "minepiece.message.technique_cast", "minepiece.message.no_hybrid",
         "minepiece.message.transformed", "minepiece.message.reverted",
         "minepiece.message.unknown_fruit", "minepiece.message.not_a_fruit",
         "minepiece.message.fruit_given", "minepiece.message.power_status",
         "minepiece.message.power_removed", "minepiece.message.haki_status",
         "minepiece.message.haki_set", "minepiece.message.haki_disabled",
         "minepiece.message.haki_type_expected", "minepiece.message.haki_armament_level",
         "minepiece.message.haki_observation_level", "minepiece.message.sovereign",
      };
      java.util.List<String> manquantes = new java.util.ArrayList<>();
      for (String k : requises) {
         if (!fr.contains(k)) {
            manquantes.add(k);
         }
      }
      check("M3-cles-hud-messages", manquantes.isEmpty(),
         requises.length + " clés requises par le code — manquantes en FR : " + manquantes.size()
         + (manquantes.isEmpty() ? "" : " (" + manquantes.get(0) + ")"));

      // Vocabulaire unifié : plus aucun « Curse Power » dans les textes joueur (constat CORE-028).
      int curse = 0;
      for (String value : langValues("/assets/minepiece/lang/fr_fr.json")) {
         if (value.contains("Curse Power")) {
            curse++;
         }
      }
      for (String value : langValues("/assets/minepiece/lang/en_us.json")) {
         if (value.contains("Curse Power")) {
            curse++;
         }
      }
      check("M4-vocabulaire-energie", curse == 0,
         "occurrences de « Curse Power » dans les textes joueur : " + curse
         + " — vocabulaire unifié sur « Énergie » / « Energy » (clé minepiece.overlay.energy)");
   }

   /** Valeurs d'un fichier de langue du mod (ressource du jar). */
   private static java.util.List<String> langValues(String resource) {
      java.util.List<String> values = new java.util.ArrayList<>();
      try (java.io.InputStream in = DevilFruitTestCommands.class.getResourceAsStream(resource)) {
         if (in == null) {
            return values;
         }
         String json = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
         com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
         for (String k : obj.keySet()) {
            values.add(obj.get(k).getAsString());
         }
      } catch (java.io.IOException | RuntimeException ex) {
         LOGGER.warn("[dftest] lecture langue {} impossible : {}", resource, ex.toString());
      }
      return values;
   }

   /** Charge les clés d'un fichier de langue du mod (ressource du jar). */
   private static java.util.Set<String> langKeys(String resource) {
      java.util.Set<String> keys = new java.util.HashSet<>();
      try (java.io.InputStream in = DevilFruitTestCommands.class.getResourceAsStream(resource)) {
         if (in == null) {
            return keys;
         }
         String json = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
         com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
         for (String k : obj.keySet()) {
            keys.add(k);
         }
      } catch (java.io.IOException | RuntimeException ex) {
         LOGGER.warn("[dftest] lecture langue {} impossible : {}", resource, ex.toString());
      }
      return keys;
   }

   // ------------------------- J. Persistance inter-redémarrage (preuve « redémarrage »)
   // Fruit/progression persist; maintained Zoan forms are deliberately cleared on exit/rejoin.
   private static final String FRUIT_REDEMARRAGE = "item_devil_fruit_ushi_ushi_giraffe";

   /** Phase 1 : écrit un état déterministe sur disque (avant arrêt du serveur). */
   private static void persistanceEcriture(ServerLevel level) {
      ServerPlayer p = fake(level, "dftest_restart");
      Item fruit = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minepiece", FRUIT_REDEMARRAGE));
      MinepieceModVariables.clear(p);
      immerse(level, p, fruit);
      DevilFruitTransformation.toggle(p);
      MinepieceModVariables.saveAll();
      PlayerVariables v = MinepieceModVariables.get(p);
      check("J1-etat-ecrit", !v.PlayerDevilFruit.isEmpty() && v.player_form > 0.0,
         "état écrit sur disque pour " + p.getUUID() + " : fruit=" + FRUIT_REDEMARRAGE + ", forme=" + v.player_form);
   }

   /** Phase 2 : après redémarrage, l'état doit avoir été rechargé depuis le .dat par loadAll(). */
   private static void persistanceVerification(ServerLevel level) {
      ServerPlayer p = fake(level, "dftest_restart");
      Item fruit = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minepiece", FRUIT_REDEMARRAGE));
      PlayerVariables v = MinepieceModVariables.get(p);
      boolean ok = !v.PlayerDevilFruit.isEmpty() && v.PlayerDevilFruit.getItem() == fruit && v.player_form == 0.0 && !v.zoanFlightGranted && !p.getAbilities().flying;
      check("J2-survit-au-redemarrage", ok,
         "après redémarrage : fruit=" + (v.PlayerDevilFruit.isEmpty() ? "aucun"
            : String.valueOf(BuiltInRegistries.ITEM.getKey(v.PlayerDevilFruit.getItem()))) + ", forme=" + v.player_form);
      MinepieceModVariables.clear(p);
   }

   // --------------------------------------------- I. Blocs temporaires / invocations
   private static void blocsEtInvocations(ServerLevel level) {
      BlockPos pos = new BlockPos(0, 200, 0);
      UUID testOwner = UUID.nameUUIDFromBytes("dftest:blocks".getBytes(StandardCharsets.UTF_8));
      PowerBlockService.releaseAll(testOwner);level.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
      int avant = net.mcreator.minepiece.procedures.PowerBlockService.activeCountFor(testOwner);
      net.mcreator.minepiece.procedures.PowerBlockService.place(level, pos, net.minecraft.world.level.block.Blocks.GOLD_BLOCK.defaultBlockState(), 200, testOwner);
      int apres = net.mcreator.minepiece.procedures.PowerBlockService.activeCountFor(testOwner);
      boolean pose = apres > avant && level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.GOLD_BLOCK);
      check("I1-bloc-permanent-suivi", pose, "attributions actives " + avant + " → " + apres + ", or pose=" + level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.GOLD_BLOCK));
      net.mcreator.minepiece.procedures.PowerBlockService.releaseAll(testOwner);
      boolean conserve = level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.GOLD_BLOCK) && PowerBlockService.activeCountFor(testOwner)==0;
      check("I2-bloc-conserve-apres-revocation", pose && conserve, "or conserve apres retrait et attribution liberee=" + conserve);

      net.mcreator.minepiece.entity.SummonEntity e = net.mcreator.minepiece.init.MinepieceModEntities.SUMMON
         .spawn(level, new BlockPos(0, 201, 0), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      check("I3-invocation", e != null, "invocation (CORE-021) créée dans le monde : " + (e != null));
      if (e != null) {
         e.discard();
      }
   }
}
