package net.mcreator.minepiece.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.mcreator.minepiece.init.MinepieceModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;

/**
 * <b>Rendu client des §N§ particules reprises du JAR source.</b>
 *
 * <p>Répartition habituelle du projet : le <b>type</b> est enregistré côté serveur
 * (init/MinepieceModParticles — il doit exister des deux côtés pour circuler dans un paquet), son
 * <b>apparence</b> est enregistrée ici.</p>
 *
 * <h2>Les paramètres ne sont pas choisis, ils sont LUS</h2>
 * <p>Durée de vie (constante <b>ou</b> expression randomisée), gravité, frottement, taille, nombre
 * d'images et type de rendu viennent du <b>bytecode des classes source</b>
 * (ParticleXxxParticle.class), extraits par tools/particle_codegen.py : la définition JSON ne contient
 * que la liste des images, tout le reste vit dans le code compilé. Les inventer aurait produit §N§
 * particules approximatives, et personne n'aurait pu dire lesquelles étaient fidèles.</p>
 *
 * <p>La source animait ses sprites par index explicite
 * (setSprite(spriteSet.get(age / pas % images + 1, images))) ; ici l'avance est confiée à
 * setSpriteFromAge, strictement équivalente quand le pas vaut 1 — et c'est le cas de toutes les
 * particules du jar.</p>
 *
 * <p>Aucune teinte n'est appliquée : les textures sont déjà celles du mod, les recolorer éloignerait de
 * la source.</p>
 */
public final class MinepieceParticlesClient {

   private MinepieceParticlesClient() {
   }

   public static void register() {
      ParticleProviderRegistry registry = ParticleProviderRegistry.getInstance();
§ENREGISTREMENTS§
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
         return this.quadSize * Math.max(0.25F, reste);
      }

      @Override
      protected SingleQuadParticle.Layer getLayer() {
         // Le type de rendu vient de la SOURCE : la couche est translucide sauf pour les particules
         // que la source rendait en terrain opaque. Les textures du jar ont des bords fondus, et une
         // couche opaque les découperait en carrés visibles.
         return this.opaque ? SingleQuadParticle.Layer.OPAQUE
            : SingleQuadParticle.Layer.TRANSLUCENT;
      }
   }
}
