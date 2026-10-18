package net.mcreator.minepiece.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.mcreator.minepiece.init.MinepieceModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;

/**
 * <b>Rendu client des 42 particules reprises du JAR source.</b>
 *
 * <p>Répartition habituelle du projet : le <b>type</b> est enregistré côté serveur
 * (init/MinepieceModParticles — il doit exister des deux côtés pour circuler dans un paquet), son
 * <b>apparence</b> est enregistrée ici.</p>
 *
 * <h2>Les paramètres ne sont pas choisis, ils sont LUS</h2>
 * <p>Durée de vie (constante <b>ou</b> expression randomisée), gravité, frottement, taille, nombre
 * d'images et type de rendu viennent du <b>bytecode des classes source</b>
 * (ParticleXxxParticle.class), extraits par tools/particle_codegen.py : la définition JSON ne contient
 * que la liste des images, tout le reste vit dans le code compilé. Les inventer aurait produit 42
 * particules approximatives, et personne n'aurait pu dire lesquelles étaient fidèles.</p>
 *
 * <p>La source animait ses sprites par index explicite
 * (setSprite(spriteSet.get(age / pas % images + 1, images))). La même cadence cyclique est conservée :
 * setSpriteFromAge étirerait une seule séquence sur toute la durée et supprimerait les répétitions.</p>
 *
 * <p>Aucune teinte n'est appliquée : les textures sont déjà celles du mod, les recolorer éloignerait de
 * la source.</p>
 */
public final class MinepieceParticlesClient {

   private MinepieceParticlesClient() {
   }

   /** Nombre de particules dont l'apparence a été enregistrée — sert à l'auto-contrôle de démarrage. */
   public static int nombreEnregistre() {
      return MinepieceModParticles.tousLesTypes().size();
   }

   public static void register() {
      ParticleProviderRegistry registry = ParticleProviderRegistry.getInstance();
      registry.register(MinepieceModParticles.BAT,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.200F, 24, 0.000F, 0.980F, 12, 1, false));
      registry.register(MinepieceModParticles.BIG_SMOKE,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 1.200F, 59, 0.000F, 0.980F, 12, 1, false));
      registry.register(MinepieceModParticles.BLUE,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.100F, 30, 0.000F, 0.980F, 15, 1, false));
      registry.register(MinepieceModParticles.BROKEN_GLASS,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.150F, 100, 0.100F, 0.980F, 0, 1, false));
      registry.register(MinepieceModParticles.BROKEN_GLASS_SMALL,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.070F, 100, 0.300F, 0.980F, 0, 1, false));
      registry.register(MinepieceModParticles.CLAWS,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 1.200F, 7, 0.000F, 0.980F, 8, 1, false));
      registry.register(MinepieceModParticles.DARK,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.800F, 15, 0.000F, 0.980F, 15, 1, false));
      registry.register(MinepieceModParticles.FANGS,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 1.200F, 7, 0.000F, 0.980F, 8, 1, false));
      registry.register(MinepieceModParticles.FIRE_FLY,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.100F, 10, 0.000F, 0.980F, 0, 1, false));
      registry.register(MinepieceModParticles.FIRE_SPARK,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.200F, Math.max(1, 10 + level.getRandom().nextInt(6) - 3), 0.100F, 0.980F, 14, 1, false));
      registry.register(MinepieceModParticles.FLAME,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 1.600F, 15, 0.000F, 0.980F, 10, 1, false).protectCamera());
      registry.register(MinepieceModParticles.FLAME_BLUE,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 1.600F, 15, 0.000F, 0.980F, 15, 1, false));
      registry.register(MinepieceModParticles.FLAME_GREEN,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 1.600F, 15, 0.000F, 0.980F, 10, 1, false));
      registry.register(MinepieceModParticles.FLAME_PURPLE,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.600F, 15, 0.000F, 0.980F, 15, 1, false));
      registry.register(MinepieceModParticles.FLAME_RED,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.600F, 15, 0.000F, 0.980F, 15, 1, false));
      registry.register(MinepieceModParticles.FLAME_SMALL,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.400F, 15, 0.000F, 0.980F, 10, 1, false).protectCamera());
      registry.register(MinepieceModParticles.FLAME_YELLOW,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.600F, 15, 0.000F, 0.980F, 15, 1, false));
      registry.register(MinepieceModParticles.FLASH_RED,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 8.000F, 5, 0.000F, 0.980F, 7, 1, false));
      registry.register(MinepieceModParticles.FLASH_RED_SMALL,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.800F, 5, 0.000F, 0.980F, 7, 1, false));
      registry.register(MinepieceModParticles.FLOWER,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.050F, Math.max(1, 60 + level.getRandom().nextInt(40) - 20), 0.010F, 0.980F, 0, 1, false));
      registry.register(MinepieceModParticles.FLOWER_PINK,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.050F, Math.max(1, 60 + level.getRandom().nextInt(40) - 20), 0.010F, 0.980F, 0, 1, false));
      registry.register(MinepieceModParticles.GRAVITY,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.600F, Math.max(1, 16 + level.getRandom().nextInt(8) - 4), 10.000F, 0.980F, 4, 1, false));
      registry.register(MinepieceModParticles.ICE,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.600F, 24, 0.000F, 0.980F, 12, 1, false));
      registry.register(MinepieceModParticles.LIGHT_LARGE,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 6.400F, 10, 0.000F, 0.980F, 5, 1, false));
      registry.register(MinepieceModParticles.LIGHT_LARGE_RED,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.100F, 11, 0.000F, 0.980F, 0, 1, false));
      registry.register(MinepieceModParticles.LIGHT_SMALL,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.500F, 5, 0.000F, 0.980F, 5, 1, false));
      registry.register(MinepieceModParticles.MAGMA,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.800F, 24, 0.100F, 0.980F, 12, 1, false));
      registry.register(MinepieceModParticles.NEGATIVE,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.500F, 20, 0.100F, 0.980F, 0, 1, false));
      registry.register(MinepieceModParticles.NOTE,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.500F, 20, 0.000F, 0.980F, 0, 1, false));
      registry.register(MinepieceModParticles.NOTE_BLACK,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 1.000F, 21, 0.000F, 0.980F, 10, 1, false));
      registry.register(MinepieceModParticles.PURPLE,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.600F, Math.max(1, 16 + level.getRandom().nextInt(8) - 4), 0.000F, 0.980F, 15, 1, false));
      registry.register(MinepieceModParticles.RED,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.100F, 30, 0.000F, 0.980F, 15, 1, false));
      registry.register(MinepieceModParticles.SAND,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.400F, 24, 0.000F, 0.980F, 12, 1, false));
      registry.register(MinepieceModParticles.SHOCK_HIT,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.800F, 6, 0.000F, 0.980F, 6, 1, false));
      registry.register(MinepieceModParticles.SHOCKWAVE,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 6.400F, 7, 0.000F, 0.980F, 8, 1, false));
      registry.register(MinepieceModParticles.SLASH,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 2.500F, 10, 0.000F, 0.980F, 10, 1, false));
      registry.register(MinepieceModParticles.THUNDER_BLACK,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.400F, Math.max(1, 16 + level.getRandom().nextInt(8) - 4), 0.000F, 0.980F, 22, 1, false));
      registry.register(MinepieceModParticles.THUNDER_BLUE,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.400F, Math.max(1, 16 + level.getRandom().nextInt(8) - 4), 0.000F, 0.980F, 22, 1, false));
      registry.register(MinepieceModParticles.THUNDER_DARK_BLUE,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.400F, Math.max(1, 16 + level.getRandom().nextInt(8) - 4), 0.000F, 0.980F, 22, 1, false));
      registry.register(MinepieceModParticles.THUNDER_LIGHT_BLUE,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.800F, Math.max(1, 16 + level.getRandom().nextInt(8) - 4), 0.000F, 0.980F, 22, 1, false));
      registry.register(MinepieceModParticles.WATER,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 0.400F, Math.max(1, 12 + level.getRandom().nextInt(8) - 4), 1.000F, 0.980F, 8, 1, false));
      registry.register(MinepieceModParticles.WATER_2,
         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->
            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, 1.200F, Math.max(1, 12 + level.getRandom().nextInt(8) - 4), 0.100F, 0.980F, 8, 1, false));
   }

   /**
    * Particule de sprite simple : image animée par l'âge, taille et durée fixées à la création.
    *
    * <p>La taille décroît avec l'âge : une particule qui garde sa taille pleine jusqu'à l'extinction
    * « claque » à la disparition, ce qui se voit surtout sur une traînée.</p>
    */
   private static final class SpriteParticle extends SingleQuadParticle {
      private final SpriteSet sprites;
      private final int images;
      private final boolean opaque;
      private final int frameStep;
      private boolean cameraFade;

      SpriteParticle protectCamera(){this.cameraFade=true;return this;}

      SpriteParticle(SpriteSet sprites, ClientLevel level, double x, double y, double z,
                     double vx, double vy, double vz, float taille, int duree, float gravite,
                     float frottement, int images, int pas, boolean opaque) {
         super(level, x, y, z, vx, vy, vz, sprites.first());
         this.sprites = sprites;
         this.images = images;
         this.opaque = opaque;
         this.frameStep = Math.max(1, pas);
         this.quadSize = taille;
         this.lifetime = duree;
         this.hasPhysics = false;
         this.gravity = gravite;
         this.friction = frottement;
      }

      @Override
      public void tick() {
         super.tick();
         if (this.images > 1) {
            this.setSprite(this.sprites.get((this.age / this.frameStep) % this.images + 1, this.images));
         }
      }

      @Override
      public float getQuadSize(float echelle) {
         float reste = 1.0F - (this.age / (float) this.lifetime);
         float size=this.quadSize * Math.max(0.25F, reste);
         var client=net.minecraft.client.Minecraft.getInstance();
         if(this.cameraFade){
            var camera=client.gameRenderer.mainCamera().position();
            double distance=camera.distanceTo(new net.minecraft.world.phys.Vec3(
               net.minecraft.util.Mth.lerp(echelle,this.xo,this.x),
               net.minecraft.util.Mth.lerp(echelle,this.yo,this.y),
               net.minecraft.util.Mth.lerp(echelle,this.zo,this.z)));
            float visibility=EffectCameraVisibility.flameScale(distance,size);
            MeraVisualLab.cameraFlame(visibility,client.options.getCameraType());
            size*=visibility;
         }
         return size;
      }

      @Override
      protected SingleQuadParticle.Layer getLayer() {
         // SpriteSet comes from the particle atlas. TERRAIN binds the block atlas,
         // making the same UVs sample unrelated block/entity-looking fragments.
         return this.opaque ? SingleQuadParticle.Layer.OPAQUE
            : SingleQuadParticle.Layer.TRANSLUCENT;
      }
   }
}
