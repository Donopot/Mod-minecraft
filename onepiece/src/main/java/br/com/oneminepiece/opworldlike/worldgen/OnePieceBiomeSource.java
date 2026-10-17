package br.com.oneminepiece.opworldlike.worldgen;

import br.com.oneminepiece.opworldlike.OmpWorld;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate.Sampler;

public final class OnePieceBiomeSource extends BiomeSource {
   public static final MapCodec<OnePieceBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            OnePieceBiomeSource.CoreBiomes.CODEC.forGetter(OnePieceBiomeSource::coreBiomes),
            OnePieceBiomeSource.IceBiomes.CODEC.forGetter(OnePieceBiomeSource::iceBiomes)
         )
         .apply(instance, OnePieceBiomeSource::new)
   );
   private final Holder<Biome> ocean;
   private final Holder<Biome> deepOcean;
   private final Holder<Biome> redLine;
   private final Holder<Biome> plains;
   private final Holder<Biome> forest;
   private final Holder<Biome> jungle;
   private final Holder<Biome> savanna;
   private final Holder<Biome> desert;
   private final Holder<Biome> snowyPlains;
   private final Holder<Biome> snowySlopes;
   private final Holder<Biome> frozenPeaks;
   private final Holder<Biome> jaggedPeaks;
   private final Holder<Biome> iceSpikes;
   private final Holder<Biome> frozenOcean;
   private final Holder<Biome> mangroveSwamp;
   private final Holder<Biome> cherryGrove;
   private final Holder<Biome> meadow;
   private final Holder<Biome> stonyShore;
   private final Holder<Biome> stonyPeaks;
   private final Holder<Biome> calmBelt;
   private volatile long worldSeed;

   private OnePieceBiomeSource(OnePieceBiomeSource.CoreBiomes core, OnePieceBiomeSource.IceBiomes ice) {
      this(
         core.ocean(),
         core.deepOcean(),
         core.redLine(),
         core.plains(),
         core.forest(),
         core.jungle(),
         core.savanna(),
         core.desert(),
         core.snowyPlains(),
         core.snowySlopes(),
         ice.frozenPeaks(),
         ice.jaggedPeaks(),
         ice.iceSpikes(),
         ice.frozenOcean(),
         core.mangroveSwamp(),
         core.cherryGrove(),
         core.meadow(),
         core.stonyShore(),
         core.stonyPeaks(),
         core.calmBelt()
      );
   }

   public OnePieceBiomeSource(
      Holder<Biome> ocean,
      Holder<Biome> deepOcean,
      Holder<Biome> redLine,
      Holder<Biome> plains,
      Holder<Biome> forest,
      Holder<Biome> jungle,
      Holder<Biome> savanna,
      Holder<Biome> desert,
      Holder<Biome> snowyPlains,
      Holder<Biome> snowySlopes,
      Optional<Holder<Biome>> frozenPeaks,
      Optional<Holder<Biome>> jaggedPeaks,
      Optional<Holder<Biome>> iceSpikes,
      Optional<Holder<Biome>> frozenOcean,
      Holder<Biome> mangroveSwamp,
      Holder<Biome> cherryGrove,
      Holder<Biome> meadow,
      Holder<Biome> stonyShore,
      Holder<Biome> stonyPeaks,
      Holder<Biome> calmBelt
   ) {
      this.ocean = ocean;
      this.deepOcean = deepOcean;
      this.redLine = redLine;
      this.plains = plains;
      this.forest = forest;
      this.jungle = jungle;
      this.savanna = savanna;
      this.desert = desert;
      this.snowyPlains = snowyPlains;
      this.snowySlopes = snowySlopes;
      this.frozenPeaks = frozenPeaks.orElse(snowySlopes);
      this.jaggedPeaks = jaggedPeaks.orElse(snowySlopes);
      this.iceSpikes = iceSpikes.orElse(snowyPlains);
      this.frozenOcean = frozenOcean.orElse(deepOcean);
      this.mangroveSwamp = mangroveSwamp;
      this.cherryGrove = cherryGrove;
      this.meadow = meadow;
      this.stonyShore = stonyShore;
      this.stonyPeaks = stonyPeaks;
      this.calmBelt = calmBelt;
   }

   private OnePieceBiomeSource.CoreBiomes coreBiomes() {
      return new OnePieceBiomeSource.CoreBiomes(
         this.ocean,
         this.deepOcean,
         this.redLine,
         this.plains,
         this.forest,
         this.jungle,
         this.savanna,
         this.desert,
         this.snowyPlains,
         this.snowySlopes,
         this.mangroveSwamp,
         this.cherryGrove,
         this.meadow,
         this.stonyShore,
         this.stonyPeaks,
         this.calmBelt
      );
   }

   private OnePieceBiomeSource.IceBiomes iceBiomes() {
      return new OnePieceBiomeSource.IceBiomes(
         Optional.of(this.frozenPeaks), Optional.of(this.jaggedPeaks), Optional.of(this.iceSpikes), Optional.of(this.frozenOcean)
      );
   }

   protected MapCodec<? extends BiomeSource> codec() {
      return CODEC;
   }

   protected Stream<Holder<Biome>> collectPossibleBiomes() {
      return Stream.of(
         this.ocean,
         this.deepOcean,
         this.redLine,
         this.plains,
         this.forest,
         this.jungle,
         this.savanna,
         this.desert,
         this.snowyPlains,
         this.snowySlopes,
         this.frozenPeaks,
         this.jaggedPeaks,
         this.iceSpikes,
         this.frozenOcean,
         this.mangroveSwamp,
         this.cherryGrove,
         this.meadow,
         this.stonyShore,
         this.stonyPeaks,
         this.calmBelt
      );
   }

   public BiomeResolver createResolver(Sampler sampler) {
      return (biomeX, biomeY, biomeZ) -> this.getNoiseBiome(biomeX, biomeY, biomeZ, sampler);
   }

   private Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Sampler sampler) {
      int x = biomeX << 2;
      int z = biomeZ << 2;
      RegionType region = OnePieceGeography.regionAt(x, z);
      if (OnePieceGeography.isReverseMountainArea(x, z) || region == RegionType.RED_LINE) {
         return this.redLine;
      } else {
         return OnePieceGeography.isCalmBelt(x, z)
            ? this.calmBelt
            : OnePieceGeography.islandAt(this.worldSeed, x, z)
               .map(island -> this.biomeForIsland(island, x, z))
               .orElse(region == RegionType.GRAND_LINE ? this.deepOcean : this.ocean);
      }
   }

   void setWorldSeed(long worldSeed) {
      this.worldSeed = worldSeed;
   }

   private Holder<Biome> biomeForIsland(IslandDefinition island, int x, int z) {
      Holder<Biome> loreBiome = this.loreBiomeForIsland(island, x, z);
      if (loreBiome != null) {
         return loreBiome;
      }

      if (island.layer() == IslandLayer.SKY) {
         return this.meadow;
      }

      return switch (island.theme()) {
         case STARTER -> this.starterBiome(island.region());
         case NORMAL -> this.normalBiome(island);
         case ALABASTA -> this.alabastaBiome(island, x, z);
         case DRUM -> this.drumBiome(island, x, z);
         case SABAODY -> island.distanceRatio(x, z) > 0.72 ? this.mangroveSwamp : this.jungle;
         case WHOLE_CAKE -> this.variant(x, z, 5) == 0 ? this.forest : this.cherryGrove;
         case WANO -> this.wanoBiome(island, x, z);
         case ZOU -> ZouLayout.isBackIsland(island, x, z) ? this.jungle : this.stonyShore;
         case FISHMAN_ISLAND -> this.ocean;
         case SKYPIEA -> this.meadow;
         case MARINEFORD, MARIEJOIS, BALTIGO -> this.stonyShore;
         case WOLF_BRIDGE -> this.stonyShore;
         case NETHER_GATEWAY -> this.stonyPeaks;
         case END_GATEWAY -> this.stonyShore;
         case PUNK_HAZARD -> this.punkHazardBiome(island, x, z);
      };
   }

   private Holder<Biome> loreBiomeForIsland(IslandDefinition island, int x, int z) {
      double ratio = island.distanceRatio(x, z);
      int variant = this.variant(Math.floorDiv(x, 96), Math.floorDiv(z, 96), 8);

      return switch (island.id()) {
         case "baratie" -> ratio > 0.72 ? this.ocean : this.plains;
         case "tequila_wolf" -> this.stonyShore;
         case "cactus_island", "alabasta" -> !(ratio > 0.8) && variant > 1 ? this.savanna : this.desert;
         case "little_garden", "jaya", "green_bit", "amazon_lily" -> ratio > 0.84 ? this.mangroveSwamp : this.jungle;
         case "long_ring_long_land" -> this.meadow;
         case "water_7" -> ratio > 0.82 ? this.ocean : this.meadow;
         case "enies_lobby", "kuraigana", "hachinosu", "end_gateway" -> ratio < 0.56 ? this.stonyPeaks : this.stonyShore;
         case "thriller_bark" -> variant <= 1 ? this.stonyShore : this.forest;
         case "dressrosa" -> variant <= 1 ? this.meadow : this.cherryGrove;
         case "whole_cake" -> variant <= 2 ? this.cherryGrove : this.forest;
         case "wano" -> this.wanoBiome(island, x, z);
         case "egghead" -> ratio < 0.58 ? this.snowySlopes : this.snowyPlains;
         case "elbaph" -> variant <= 1 ? this.forest : this.jungle;
         case "lodestar" -> ratio < 0.58 ? this.meadow : this.stonyShore;
         default -> null;
      };
   }

   private Holder<Biome> punkHazardBiome(IslandDefinition island, int x, int z) {
      double ratio = island.distanceRatio(x, z);
      int variant = this.variant(Math.floorDiv(x, 96), Math.floorDiv(z, 96), 6);
      if (x < island.centerX()) {
         if (ratio > 0.88) {
            return this.frozenOcean;
         } else if (ratio > 0.78) {
            return variant <= 1 ? this.snowyPlains : this.iceSpikes;
         } else {
            int mountainVariant = this.variant(Math.floorDiv(x, 148), Math.floorDiv(z, 148), 7);
            if (mountainVariant <= 1) {
               return this.frozenPeaks;
            } else if (mountainVariant == 2) {
               return this.jaggedPeaks;
            } else if (mountainVariant == 3) {
               return this.iceSpikes;
            } else {
               return variant <= 2 ? this.snowySlopes : this.snowyPlains;
            }
         }
      } else {
         return ratio > 0.84 ? this.stonyShore : this.stonyPeaks;
      }
   }

   private Holder<Biome> alabastaBiome(IslandDefinition island, int x, int z) {
      double ratio = island.distanceRatio(x, z);
      int variant = this.variant(Math.floorDiv(x, 128), Math.floorDiv(z, 128), 7);
      if (ratio > 0.86) {
         return this.desert;
      } else if (variant == 0 && ratio < 0.68) {
         return this.savanna;
      } else {
         return variant != 1 && variant != 2 ? this.desert : this.stonyShore;
      }
   }

   private Holder<Biome> drumBiome(IslandDefinition island, int x, int z) {
      double ratio = island.distanceRatio(x, z);
      int variant = this.variant(Math.floorDiv(x, 112), Math.floorDiv(z, 112), 5);
      if (ratio > 0.82) {
         return this.snowyPlains;
      } else {
         return variant == 0 ? this.forest : this.snowySlopes;
      }
   }

   private Holder<Biome> wanoBiome(IslandDefinition island, int x, int z) {
      double ratio = island.distanceRatio(x, z);
      if (ratio > 0.78) {
         return this.forest;
      }

      int variant = this.variant(Math.floorDiv(x, 96), Math.floorDiv(z, 96), 4);
      return variant == 0 ? this.meadow : this.cherryGrove;
   }

   private Holder<Biome> starterBiome(RegionType region) {
      return switch (region) {
         case NORTH_BLUE -> this.forest;
         case SOUTH_BLUE -> this.savanna;
         case WEST_BLUE -> this.jungle;
         default -> this.plains;
      };
   }

   private Holder<Biome> normalBiome(IslandDefinition island) {
      if (island.size() != IslandSize.LARGE && island.size() != IslandSize.HUGE) {
         return switch (island.region()) {
            case NORTH_BLUE -> this.forest;
            case SOUTH_BLUE -> this.savanna;
            case WEST_BLUE -> this.jungle;
            case GRAND_LINE -> this.plains;
            default -> this.plains;
         };
      } else {
         return island.region() == RegionType.SOUTH_BLUE ? this.savanna : this.jungle;
      }
   }

   private int variant(int x, int z, int modulo) {
      return Math.floorMod(x * 73428767 ^ z * 91293481, modulo);
   }

   private record CoreBiomes(
      Holder<Biome> ocean,
      Holder<Biome> deepOcean,
      Holder<Biome> redLine,
      Holder<Biome> plains,
      Holder<Biome> forest,
      Holder<Biome> jungle,
      Holder<Biome> savanna,
      Holder<Biome> desert,
      Holder<Biome> snowyPlains,
      Holder<Biome> snowySlopes,
      Holder<Biome> mangroveSwamp,
      Holder<Biome> cherryGrove,
      Holder<Biome> meadow,
      Holder<Biome> stonyShore,
      Holder<Biome> stonyPeaks,
      Holder<Biome> calmBelt
   ) {
      private static final MapCodec<OnePieceBiomeSource.CoreBiomes> CODEC = RecordCodecBuilder.mapCodec(
         instance -> instance.group(
               Biome.CODEC.fieldOf("ocean").forGetter(OnePieceBiomeSource.CoreBiomes::ocean),
               Biome.CODEC.fieldOf("deep_ocean").forGetter(OnePieceBiomeSource.CoreBiomes::deepOcean),
               Biome.CODEC.fieldOf("red_line").forGetter(OnePieceBiomeSource.CoreBiomes::redLine),
               Biome.CODEC.fieldOf("plains").forGetter(OnePieceBiomeSource.CoreBiomes::plains),
               Biome.CODEC.fieldOf("forest").forGetter(OnePieceBiomeSource.CoreBiomes::forest),
               Biome.CODEC.fieldOf("jungle").forGetter(OnePieceBiomeSource.CoreBiomes::jungle),
               Biome.CODEC.fieldOf("savanna").forGetter(OnePieceBiomeSource.CoreBiomes::savanna),
               Biome.CODEC.fieldOf("desert").forGetter(OnePieceBiomeSource.CoreBiomes::desert),
               Biome.CODEC.fieldOf("snowy_plains").forGetter(OnePieceBiomeSource.CoreBiomes::snowyPlains),
               Biome.CODEC.fieldOf("snowy_slopes").forGetter(OnePieceBiomeSource.CoreBiomes::snowySlopes),
               Biome.CODEC.fieldOf("mangrove_swamp").forGetter(OnePieceBiomeSource.CoreBiomes::mangroveSwamp),
               Biome.CODEC.fieldOf("cherry_grove").forGetter(OnePieceBiomeSource.CoreBiomes::cherryGrove),
               Biome.CODEC.fieldOf("meadow").forGetter(OnePieceBiomeSource.CoreBiomes::meadow),
               Biome.CODEC.fieldOf("stony_shore").forGetter(OnePieceBiomeSource.CoreBiomes::stonyShore),
               Biome.CODEC.fieldOf("stony_peaks").forGetter(OnePieceBiomeSource.CoreBiomes::stonyPeaks),
               Biome.CODEC.fieldOf("calm_belt").forGetter(OnePieceBiomeSource.CoreBiomes::calmBelt)
            )
            .apply(instance, OnePieceBiomeSource.CoreBiomes::new)
      );
   }

   private record IceBiomes(
      Optional<Holder<Biome>> frozenPeaks, Optional<Holder<Biome>> jaggedPeaks, Optional<Holder<Biome>> iceSpikes, Optional<Holder<Biome>> frozenOcean
   ) {
      private static final MapCodec<OnePieceBiomeSource.IceBiomes> CODEC = RecordCodecBuilder.mapCodec(
         instance -> instance.group(
               Biome.CODEC.optionalFieldOf("frozen_peaks").forGetter(OnePieceBiomeSource.IceBiomes::frozenPeaks),
               Biome.CODEC.optionalFieldOf("jagged_peaks").forGetter(OnePieceBiomeSource.IceBiomes::jaggedPeaks),
               Biome.CODEC.optionalFieldOf("ice_spikes").forGetter(OnePieceBiomeSource.IceBiomes::iceSpikes),
               Biome.CODEC.optionalFieldOf("frozen_ocean").forGetter(OnePieceBiomeSource.IceBiomes::frozenOcean)
            )
            .apply(instance, OnePieceBiomeSource.IceBiomes::new)
      );
   }
}
