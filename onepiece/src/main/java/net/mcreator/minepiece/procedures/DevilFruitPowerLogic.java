package net.mcreator.minepiece.procedures;

import net.mcreator.minepiece.init.MinepieceModItems;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.network.MinepieceModVariables.PlayerVariables;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Cœur logique de la couche pouvoir des Fruits du Démon (port Fabric 26.3 de la couche procédurale
 * Forge Mine Piece ver15). Regroupe :
 * <ul>
 *   <li>la résolution du fruit possédé ({@link #getDevilFruit}) ;</li>
 *   <li>la malédiction de la mer ({@link #onDevilFruitUserTick}, port de
 *       {@code DevilFruitUserOnEffectActiveTickProcedure}) ;</li>
 *   <li>la magnification des dégâts Logia/Gomu ({@link #computeDamageMagnification}, port simplifié de
 *       {@code DevilFruitDamageMagnificationProcedure}).</li>
 * </ul>
 * Simplifications honnêtes (documentées) : la matrice de contres élémentaires complète et les hooks
 * Forge {@code forge:curse}/{@code forge:combat} + persistentData {@code skill} sont remplacés par une
 * heuristique « attaque physique » et le tag {@code minepiece:logia}. Les fruits des PNJ (map ~80 entités)
 * sont différés au portage des mobs.
 */
public final class DevilFruitPowerLogic {

   private static final TagKey<Item> TAG_LOGIA = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minepiece", "logia"));
   private static final TagKey<Item> TAG_ZOAN = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minepiece", "zoan"));
   private static final TagKey<Item> TAG_PARAMECIA = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minepiece", "paramecia"));
   private static final TagKey<Item> TAG_DEVIL_FRUIT = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minepiece", "devil_fruit"));

   /** Familles reconnues d'un Fruit du Démon (FRUIT-*-02). */
   public enum Family {
      LOGIA, ZOAN, PARAMECIA, UNCLASSIFIED
   }

   /**
    * Famille d'un Fruit, lue depuis les tags — et non déduite d'une supposition sur « non classé ».
    *
    * <p>Rôle de chaque famille dans le comportement :</p>
    * <ul>
    *   <li><b>LOGIA</b> — intangibilité aux attaques physiques (sauf Haki), cf
    *       {@code computeDamageMagnification} ; c'est la seule famille qui porte une résistance ;</li>
    *   <li><b>ZOAN</b> — famille animale ; la capacité de <i>transformation</i> est portée séparément
    *       par le tag {@code minepiece:transformable} ;</li>
    *   <li><b>PARAMECIA</b> — pouvoir non élémentaire, ni intangibilité ni transformation par défaut ;</li>
    *   <li><b>UNCLASSIFIED</b> — aucun tag : à décider et à documenter (jamais supposé Paramecia).</li>
    * </ul>
    */
   public static Family family(Item item) {
      if (item == null) {
         return Family.UNCLASSIFIED;
      }
      ItemStack stack = new ItemStack(item);
      if (stack.is(TAG_DEVIL_FRUIT)) {
         if (stack.is(TAG_LOGIA)) {
            return Family.LOGIA;
         }
         if (stack.is(TAG_ZOAN)) {
            return Family.ZOAN;
         }
         if (stack.is(TAG_PARAMECIA)) {
            return Family.PARAMECIA;
         }
         return Family.UNCLASSIFIED;
      }
      return Family.UNCLASSIFIED;
   }

   private DevilFruitPowerLogic() {
   }

   /** Résout le Fruit du Démon possédé par une entité (joueurs : variable persistée ; PNJ : non porté). */
   public static ItemStack getDevilFruit(Entity entity) {
      if (entity instanceof ServerPlayer sp) {
         return MinepieceModVariables.get(sp).PlayerDevilFruit;
      }
      return ItemStack.EMPTY;
   }

   private static boolean isItem(ItemStack stack, Item item) {
      return !stack.isEmpty() && stack.getItem() == item;
   }

   private static boolean isTag(ItemStack stack, TagKey<Item> tag) {
      return !stack.isEmpty() && stack.is(holder -> holder.is(tag));
   }

   /** Malédiction de la mer : eau → SEA_POWER + lenteur + anti-fly/anti-double-saut. + régénération d'énergie. */
   public static void onDevilFruitUserTick(LivingEntity entity) {
      ItemStack fruit = getDevilFruit(entity);

      // fruits de glace : éteignent le feu
      if (isItem(fruit, MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI)
            || isItem(fruit, MinepieceModItems.ITEM_DEVIL_FRUIT_HIE_HIE)) {
         if (entity.getRemainingFireTicks() > 0) {
            entity.setRemainingFireTicks(0);
         }
      }

      if (entity.isSwimming()) {
         entity.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER, 30, 9, false, false));
      } else if (entity.isInWater()) {
         entity.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER, 10, 1, false, false));
      }

      if (entity.hasEffect(MinepieceModMobEffects.SEA_POWER)) {
         int amp = entity.getEffect(MinepieceModMobEffects.SEA_POWER).getAmplifier();
         entity.removeEffect(MinepieceModMobEffects.FLY_EFFECT);
         entity.addEffect(new MobEffectInstance(MinepieceModMobEffects.COOLDOWN_TIME, 10, 0, false, false));
         entity.addEffect(new MobEffectInstance(MinepieceModMobEffects.COOLDOWN_TIME_SECOND, 10, 0, false, false));
         entity.addEffect(new MobEffectInstance(MinepieceModMobEffects.COOLDOWN_TIME_FLY, 10, 9, false, false));

         if (amp >= 1) {
            entity.removeEffect(MinepieceModMobEffects.DOUBLE_JUMP_EFFECT);
            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 10, amp, false, false));
            entity.addEffect(new MobEffectInstance(MinepieceModMobEffects.COOLDOWN_TIME_BACK_STEP, 10, 9, false, false));
            if (amp >= 4) {
               entity.addEffect(new MobEffectInstance(MinepieceModMobEffects.COOLDOWN_TIME_COMBAT, 10, 0, false, false));
            }
         }
      }

      // régénération d'énergie (« Curse Power ») — suspendue sous suppression (mer, CORE-007)
      if (entity instanceof ServerPlayer sp) {
         PlayerVariables vars = MinepieceModVariables.get(sp);
         if (!isSuppressed(sp) && !GoroAmaru.active(sp) && vars.PlayerEnergy < vars.PlayerEnergyMAX) {
            vars.PlayerEnergy = Math.min(vars.PlayerEnergy + 0.2, vars.PlayerEnergyMAX);
         }
      }
   }

   /**
    * Multiplicateur de dégâts subis par la victime (port simplifié de {@code DevilFruitDamageMagnificationProcedure}).
    * Intangibilité Logia (attaque physique sans haki = 0) et immunité Gomu (projectile = 0, contondant = ~0).
    */
   public static double computeDamageMagnification(DamageSource source, LivingEntity victim) {
      if(source.is(DamageTypes.HOT_FLOOR) && victim.level() instanceof net.minecraft.server.level.ServerLevel magmaLevel
            && (PowerBlockService.isTracked(magmaLevel,victim.blockPosition().below(),MagmaBurn.magmaBlock(),null)
               || PowerBlockService.isTracked(magmaLevel,victim.blockPosition(),MagmaBurn.magmaBlock(),null))) return 0.0;
      ItemStack fruit = getDevilFruit(victim);
      if (fruit.isEmpty() || !isTag(fruit, TAG_DEVIL_FRUIT)) {
         return 1.0;
      }

      Entity attacker = source.getEntity();
      Entity direct = source.getDirectEntity();
      ItemStack attackerFruit = (attacker instanceof LivingEntity le) ? getDevilFruit(le) : ItemStack.EMPTY;
      boolean attackerHasHaki = attacker instanceof LivingEntity le
            && (le.hasEffect(MinepieceModMobEffects.ARMAMENT_HAKI) || le.hasEffect(MinepieceModMobEffects.SOVEREIGN_HAKI));
      boolean physical = isPhysicalAttack(source);

      // A body made of flame resists ordinary fire, not lava/magma (Ace's counter).
      if (isItem(fruit, MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA) && !isSuppressed(victim)
            && (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE)
               || source.is(DamageTypes.FIREBALL) || source.is(DamageTypes.UNATTRIBUTED_FIREBALL))) return 0.0;
      if(isItem(fruit,MinepieceModItems.ITEM_DEVIL_FRUIT_MAGU_MAGU) && !isSuppressed(victim)
            && (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.FIREBALL)
               || source.is(DamageTypes.UNATTRIBUTED_FIREBALL) || source.is(DamageTypes.LAVA) || source.is(DamageTypes.HOT_FLOOR))) return 0.0;
      double mult = 1.0;
      // Lightning body: elemental resistance is revoked by the same suppression gate as skills.
      if(isItem(fruit,MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO) && !isSuppressed(victim)
            && source.is(DamageTypes.LIGHTNING_BOLT))return 0.0;
      if(isItem(fruit,MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU) && !isSuppressed(victim) && !attackerHasHaki
            && source.is(DamageTypes.LIGHTNING_BOLT))return 0.0;

      // Intangibilité Logia : attaque physique sans Haki ne touche pas.
      // FRUIT-MOKU_MOKU-06 : « la suppression ENLÈVE le passif ». Un pouvoir scellé (eau de mer, pierre
      // marine) ne protège plus : sans cette condition, un porteur immergé restait intangible, c'est-à-dire
      // protégé par un pouvoir que le jeu venait précisément de lui retirer. Le passif suit donc l'état du
      // pouvoir, comme les autres états du projet.
      boolean victimeYami = isItem(fruit, MinepieceModItems.ITEM_DEVIL_FRUIT_YAMI_YAMI);

      // ===== FRUIT-YAMI_YAMI-04 : ABSORPTION ACTIVE, bornée par capacité ET par énergie =====
      // Un coup ABSORBABLE (physique, ou projectile) porté sur un porteur dont la fenêtre est ouverte
      // est avalé. Passé la capacité ou sans énergie, les ténèbres sont PLEINES et les dégâts passent
      // normalement : c'est la seule lecture possible du critère « une capacité dépassée ne supprime
      // pas tous les projectiles ou dégâts sans limite ». Le scellement ferme la fenêtre : un pouvoir
      // scellé n'absorbe plus rien.
      if (victimeYami && !isSuppressed(victim) && absorbable(source, direct)
            && YamiDarkness.absorbIfAble(victim)) {
         return 0.0;
      }

      // ===== FRUIT-YAMI_YAMI-02/05/06 : L'EXCEPTION DÉCIDÉE — et elle est l'INVERSE des autres =====
      // Ce fruit porte bien le tag LOGIA (comme dans le JAR d'origine et dans le CSV), mais
      // l'intangibilité générique ne s'applique PAS : c'est le seul Logia qui ne laisse pas passer les
      // coups. Le plan demande de « décider explicitement l'exception à l'intangibilité Logia
      // générique » (-05) et que la classification soit « cohérente avec la résistance aux dégâts »
      // (-02). Décision : aucune intangibilité, et une contrepartie assumée — les ténèbres SUBISSENT
      // davantage (voir plus bas). Les tests mesurent les deux, source par source, contre un témoin
      // sans Fruit.
      if (isTag(fruit, TAG_LOGIA) && !victimeYami && physical && !attackerHasHaki && !isSuppressed(victim)) {
         mult = Math.min(mult, 0.0);
      }

      // Contrepartie de Yami : le prix de l'absence d'intangibilité. C'est une PÉNALITÉ, donc elle ne
      // dépend pas de la suppression — un pouvoir scellé ne rend pas la douleur plus douce.
      if (victimeYami && physical) {
         mult = Math.max(mult, br.com.oneminepiece.opworldlike.yami.DevilFruitYamiConfig.painMultiplier());
      }

      // ===== FRUIT-MOKU_MOKU-06 / MORI_MORI-06 : exceptions, et pourquoi il n'y en a pas à écrire =====
      // Première version : une exception « le souffle DISPERSE la fumée » remettait mult à 1.0 pour les
      // explosions. C'ÉTAIT DU CODE MORT : isPhysicalAttack() exclut déjà EXPLOSION, PLAYER_EXPLOSION,
      // FIREBALL, IN_FIRE et ON_FIRE, donc ces sources n'entraient jamais dans la branche Logia et
      // étaient DÉJÀ conservées. Le test passait — pour la mauvaise raison : il lisait 1.0 en croyant
      // lire mon exception, alors qu'il lisait le comportement générique.
      // La vraie exception de ces Logias n'est donc pas une liste de sources : c'est que la lecture
      // « physique / non physique » est DÉJÀ la bonne table, et que le jeu lui-même refuse toute source
      // sans entité directe (feu, noyade, suffocation, magie). Ce qui manquait réellement, et qui est
      // implémenté ci-dessus, c'est la SUPPRESSION : elle n'était pas consultée du tout.
      //
      // Contre-épreuve dans les tests (A4e / A5e) : chaque source est comparée à un témoin SANS Fruit.
      // Si le témoin reçoit aussi ×1.0, la source n'a jamais été concernée par l'intangibilité — et
      // c'est cette comparaison, pas la valeur seule, qui prouve qu'aucune exception n'est nécessaire.

      // Gomu Gomu : immunité aux projectiles + contondant (sans Haki)
      if (isItem(fruit, MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU) && !attackerHasHaki) {
         if (direct instanceof Projectile) {
            mult = Math.min(mult, 0.0);
         } else if (physical) {
            mult = Math.min(mult, 1.0E-4);
         }
      }

      // Yami Yami (Ténèbres) : neutralise le pouvoir adverse au contact
      if (!attackerFruit.isEmpty() && attackerFruit.getItem() == MinepieceModItems.ITEM_DEVIL_FRUIT_YAMI_YAMI) {
         // ANNULATION, pas nivellement : on relève ce qui était annulé (intangibilité Logia ×0,
         // immunité contondante de Gomu ≈ ×0) SANS rabaisser ce qui était déjà plus élevé — sinon un
         // porteur de Yami frappé par un autre Yami verrait sa contrepartie ×2 être RAMENÉE à ×1.
         mult = Math.max(mult, 1.0);
      }

      return mult;
   }

   private static boolean isPhysicalAttack(DamageSource source) {
      if (source.getDirectEntity() == null) {
         return false;
      }
      return !source.is(DamageTypes.MAGIC)
            && !source.is(DamageTypes.INDIRECT_MAGIC)
            && !source.is(DamageTypes.FIREBALL)
            && !source.is(DamageTypes.UNATTRIBUTED_FIREBALL)
            && !source.is(DamageTypes.EXPLOSION)
            && !source.is(DamageTypes.PLAYER_EXPLOSION)
            && !source.is(DamageTypes.LIGHTNING_BOLT)
            && !source.is(DamageTypes.LAVA)
            && !source.is(DamageTypes.FREEZE)
            && !source.is(DamageTypes.IN_FIRE)
            && !source.is(DamageTypes.ON_FIRE);
   }

   /** Vrai si l'entité possède un Fruit du Démon (port de {@code LogicDevilFruitUserProcedure}). */
   /**
    * Une attaque est-elle <b>absorbable</b> par les ténèbres ? (FRUIT-YAMI_YAMI-04)
    *
    * <p>Propriétaire unique de cette définition : les <b>coups physiques</b> et les <b>projectiles</b>.
    * La lumière, le feu, les explosions, la magie et toute source sans entité directe <b>traversent</b>
    * les ténèbres — c'est la règle de la source, et un test la mesure source par source contre un
    * témoin sans Fruit (une valeur seule ne prouverait pas que la source a été concernée).</p>
    */
   public static boolean absorbable(DamageSource source, Entity direct) {
      return isPhysicalAttack(source) || direct instanceof Projectile;
   }

   public static boolean isDevilFruitUser(Entity entity) {
      ItemStack fruit = getDevilFruit(entity);
      return !fruit.isEmpty() && isTag(fruit, TAG_DEVIL_FRUIT);
   }

   /** Suppression par l'eau (mer) : état centralisé à vérifier avant skill/transformation/passif/régén (CORE-007). */
   public static boolean isSuppressed(LivingEntity entity) {
      // FRUIT-YAMI_YAMI-05 : le scellement des ténèbres est un « pouvoir scellé » comme l'eau de mer.
      // Il entre donc par la MÊME porte, et tout ce qui consulte ce prédicat (lancement de techniques,
      // intangibilité, aires maintenues, formes, charges) l'honore sans une ligne de plus.
      return entity.hasEffect(MinepieceModMobEffects.SEA_POWER)
         || entity.hasEffect(MinepieceModMobEffects.YAMI_SEALED);
   }

   /**
    * <b>Pourquoi</b> le pouvoir est scellé : {@code "sea"}, {@code "yami"} ou {@code ""} (FRUIT-YAMI_YAMI-05).
    *
    * <p>Deux causes désormais, une seule porte. Sans cette fonction, une cible scellée par les ténèbres
    * lisait le message de <b>l'eau de mer</b> — un joueur aurait cherché la mer. Le message suit la
    * cause, la décision de blocage reste unique.</p>
    */
   public static String suppressionCause(LivingEntity entity) {
      if (entity.hasEffect(MinepieceModMobEffects.SEA_POWER)) {
         return "sea";
      }
      if (entity.hasEffect(MinepieceModMobEffects.YAMI_SEALED)) {
         return "yami";
      }
      return "";
   }

   /**
    * <b>Reset complet du pouvoir</b> (FRUIT-RESET-01) — point unique utilisé par l'objet Void Fruit
    * ({@code DevilFruitItem#finishUsingItem}), la règle du 2ᵉ fruit et {@code /devilfruit clear}.
    *
    * <p>Nettoie tout ce que le pouvoir a accordé :</p>
    * <ol>
    *   <li>forme hybride — modificateurs d'attributs (Force/Résistance/Vitesse/Échelle) + vol ;
    *       révocation <b>inconditionnelle</b>, donc effectuée même si {@code player_form} est déjà 0 ;</li>
    *   <li>effet de porteur {@code DEVIL_FRUIT_USER} ;</li>
    *   <li>invocations encore actives appartenant au joueur (CORE-021) ;</li>
    *   <li>état persistant : fruit, drapeau d'usage, sélection de technique, forme, énergie.</li>
    * </ol>
    *
    * <p><b>Sans toucher aux effets externes</b> : les potions et effets d'une autre origine ne sont
    * jamais retirés — seuls les modificateurs d'attributs identifiés (UUID de ressource, CORE-013) et
    * l'effet de porteur propre au Fruit le sont. Testé par {@code R3-effets-externes-preserves}.</p>
    *
    * @return nombre d'invocations supprimées
    */
   public static int resetDevilFruitPowers(ServerPlayer player) {
      PlayerVariables vars = MinepieceModVariables.get(player);

      // 1. Forme hybride : révocation inconditionnelle (attributs + vol), idempotente.
      ZoanClaw.cancel(player);
      DevilFruitTransformation.revertForm(player);

      // 2. Effet de porteur du Fruit.
      player.removeEffect(MinepieceModMobEffects.DEVIL_FRUIT_USER);

      // 3. Invocations du joueur encore actives (CORE-021).
      int summons = removeOwnSummons(player);

      // 3 bis. Ombres détenues (FRUIT-KAGE_KAGE-03) : la restitution est INCONDITIONNELLE.
      // C'est le cas de fin « retrait » du contrat : le porteur perd le Fruit, donc il perd le pouvoir
      // de détenir une ombre — et l'ombre retourne à son propriétaire légitime, qui n'a rien fait et
      // n'a aucun moyen de la réclamer lui-même.
      ShadowLedger.restoreAll(player, "retrait");

      // 3 ter. Poids (FRUIT-KIRO_KIRO-03/04) : retour au poids NORMAL, sans accumulation.
      WeightState.reset(player);

      // 3 quater. Mochi (FRUIT-MOCHI_MOCHI-04/05) : les cibles collées sont LIBÉRÉES et les
      // attribution des constructions libérée — le terrain reste définitif.
      StickyState.releaseAllFrom(player, "retrait");
      // 3 quinquies. Moku Moku (FRUIT-MOKU_MOKU-03/04/05) : nuages dissipés et captures libérées.
      SmokeCloud.releaseAllFrom(player, "retrait");
      SmokeCloud.endCloudsFrom(player, "retrait");
      // 3 sexies. Mori Mori (FRUIT-MORI_MORI-03/04) : racines libérées, végétation retirée.
      Roots.releaseAllFrom(player, "retrait");
      // 3 septies. Ope Ope (FRUIT-OPE_OPE-03/05) : la ROOM se ferme et les cibles sectionnées sont
      // RESTAURÉES — perdre le Fruit ne doit pas laisser quelqu'un sans force de frappe.
      OpeRoom.closeFrom(player, "retrait");
      Amputation.restoreAllFrom(player, "retrait");
      // 3 octies. Pika Pika (FRUIT-PIKA_PIKA-04/05) : charge annulée et bonus de lumière RENDUS — en
      // restituant ce qu'ils avaient déplacé, sans supprimer une potion qui n'appartenait pas au Fruit.
      LightCharge.cancelFrom(player, "retrait");
      LightBuffs.restoreAll(player, "retrait");LightSwords.clearFor(player);
      // 3 nonies. Yami Yami (FRUIT-YAMI_YAMI-03/04/05) : attraction et fenêtre d'absorption fermées.
      // Le scellement posé SUR AUTRUI n'est PAS retiré ici : il n'appartient pas à ce porteur et il
      // expire tout seul — le retirer priverait la victime du temps de récupération promis.
      YamiDarkness.clearFor(player, "retrait");
      MeraTechniques.cancel(player);
      MaguTechniques.cancel(player);
      MythicBatchTechniques.cancel(player);
      ZoanTechniqueEpisodes.cancel(player);MythicalZoanTechniques.cancel(player);
      ParameciaBatchTechniques.cancel(player);GoroTechniques.cancel(player);ElementalBatchTechniques.cancel(player);ControlledBatchTechniques.cancel(player);MaterialBatchTechniques.cancel(player);SeismicWave.clearFrom(player);
      FreezeService.clearFrom(player);Desiccation.clearFrom(player);SandStorm.clearFrom(player);
      MagmaPools.clearFrom(player);
      FireRing.clearFrom(player);
      FirePropulsion.stop(player, "retrait");
      PowerBlockService.releaseAll(player.getUUID());
      MochiBuild.clearFrom(player, "retrait");
      WeightSlam.clear(player);
      WolfPursuit.end(player, "retrait");
      DiamondBody.end(player, "retrait");
      IceDefense.end(player, "retrait");
      RemoteControl.end(player, "retrait");
      RikiForce.release(player, "retrait");
      SoruSummons.releaseAll(player, "retrait");
      UtaDomain.exit(player, "retrait");
      SupaBlades.retract(player);

      // 4. État persistant.
      vars.player_form = 0.0;
      vars.PlayerDevilFruit = ItemStack.EMPTY;
      FirePropulsion.reconcile(player);
      vars.UseDevilAbilities = false;
      vars.PlayerTechniqueUsedNumber = 0.0;
      vars.PlayerEnergy = 0.0;
      vars.syncPlayerVariables(player);
      return summons;
   }

   /** Supprime les invocations appartenant au joueur, dans la portée de suivi du lanceur. */
   private static int removeOwnSummons(ServerPlayer player) {
      if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) {
         return 0;
      }
      int removed = 0;
      for (net.mcreator.minepiece.entity.SummonEntity summon
            : level.getEntitiesOfClass(net.mcreator.minepiece.entity.SummonEntity.class,
               player.getBoundingBox().inflate(128))) {
         if (summon.getOwner() != null && player.getUUID().equals(summon.getOwner().getUUID())) {
            summon.discard();
            removed++;
         }
      }
      return removed;
   }

   /**
    * Ancien nom conservé pour les appels historiques : délègue au reset complet.
    *
    * @deprecated utiliser {@link #resetDevilFruitPowers(ServerPlayer)}
    */
   @Deprecated
   public static void clearDevilFruit(ServerPlayer player) {
      resetDevilFruitPowers(player);
   }
}
