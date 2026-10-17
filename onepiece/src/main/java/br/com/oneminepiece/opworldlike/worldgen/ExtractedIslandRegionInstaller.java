package br.com.oneminepiece.opworldlike.worldgen;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

public final class ExtractedIslandRegionInstaller {
   private static final String MARKER_FILE = "opworldlike_extracted_islands.installed";
   private static final String BUNDLED_REGION_PREFIX = "data/opworldlike/extracted_islands/region/";

   public static void register() {
      ServerLifecycleEvents.SERVER_STARTING.register(ExtractedIslandRegionInstaller::onServerStarting);
   }

   public static void onServerStarting(MinecraftServer server) {
      Path worldRoot = server.getWorldPath(LevelResource.ROOT);
      if (!installBundled(worldRoot)) {
         Path sourceRegionDir = FabricLoader.getInstance().getConfigDir().resolve("opworldlike").resolve("extracted_islands").resolve("region");
         installFromDirectory(worldRoot, sourceRegionDir);
      }
   }

   private static boolean installBundled(Path worldRoot) {
      try {
         List<ExtractedIslandRegionInstaller.RegionResource> regionFiles = listBundledRegionFiles();
         if (regionFiles.isEmpty()) {
            return false;
         }

         installFromResources(worldRoot, regionFiles);
         return true;
      } catch (IOException | URISyntaxException ex) {
         OmpWorld.LOGGER.error("Failed to install bundled extracted island region pack into {}.", worldRoot, ex);
         return true;
      }
   }

   private static void installFromDirectory(Path worldRoot, Path sourceRegionDir) {
      if (Files.isDirectory(sourceRegionDir)) {
         try {
            List<Path> regionFiles = listRegionFiles(sourceRegionDir);
            if (regionFiles.isEmpty()) {
               return;
            }

            Path targetRegionDir = targetRegionDirIfInstallable(worldRoot, sourceRegionDir.toString());
            if (targetRegionDir == null) {
               return;
            }

            int copied = 0;

            for (Path source : regionFiles) {
               Files.copy(source, targetRegionDir.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
               copied++;
            }

            writeMarker(
               worldRoot,
               "installedAt="
                  + Instant.now()
                  + System.lineSeparator()
                  + "source="
                  + sourceRegionDir
                  + System.lineSeparator()
                  + "regionFiles="
                  + copied
                  + System.lineSeparator()
            );
            OmpWorld.LOGGER.info("Installed {} extracted island region files into {}.", copied, targetRegionDir);
         } catch (IOException ex) {
            OmpWorld.LOGGER.error("Failed to install extracted island region pack from {} into {}.", new Object[]{sourceRegionDir, worldRoot, ex});
         }
      }
   }

   private static void installFromResources(Path worldRoot, List<ExtractedIslandRegionInstaller.RegionResource> regionFiles) throws IOException {
      Path targetRegionDir = targetRegionDirIfInstallable(worldRoot, "bundled jar resources");
      if (targetRegionDir != null) {
         ClassLoader loader = ExtractedIslandRegionInstaller.class.getClassLoader();
         int copied = 0;

         for (ExtractedIslandRegionInstaller.RegionResource source : regionFiles) {
            try (InputStream input = loader.getResourceAsStream(source.resourcePath())) {
               if (input == null) {
                  throw new IOException("Missing bundled region resource " + source.resourcePath());
               }

               Files.copy(input, targetRegionDir.resolve(source.fileName()), StandardCopyOption.REPLACE_EXISTING);
               copied++;
            }
         }

         writeMarker(
            worldRoot,
            "installedAt="
               + Instant.now()
               + System.lineSeparator()
               + "source=jar:data/opworldlike/extracted_islands/region/"
               + System.lineSeparator()
               + "regionFiles="
               + copied
               + System.lineSeparator()
         );
         OmpWorld.LOGGER.info("Installed {} bundled extracted island region files into {}.", copied, targetRegionDir);
      }
   }

   private static Path targetRegionDirIfInstallable(Path worldRoot, String sourceDescription) throws IOException {
      Path marker = worldRoot.resolve("opworldlike_extracted_islands.installed");
      if (Files.exists(marker)) {
         return null;
      } else {
         Path targetRegionDir = worldRoot.resolve("region");
         if (hasRegionFiles(targetRegionDir)) {
            OmpWorld.LOGGER
               .warn(
                  "Extracted island region pack found at {}, but {} already has region files. Skipping to avoid overwriting an existing world.",
                  sourceDescription,
                  targetRegionDir
               );
            return null;
         } else {
            Files.createDirectories(targetRegionDir);
            return targetRegionDir;
         }
      }
   }

   private static void writeMarker(Path worldRoot, String content) throws IOException {
      Files.writeString(
         worldRoot.resolve("opworldlike_extracted_islands.installed"),
         content,
         StandardCharsets.UTF_8,
         StandardOpenOption.CREATE,
         StandardOpenOption.TRUNCATE_EXISTING
      );
   }

   private static List<Path> listRegionFiles(Path regionDir) throws IOException {
      try (Stream<Path> stream = Files.list(regionDir)) {
         return stream.filter(x$0 -> Files.isRegularFile(x$0)).filter(ExtractedIslandRegionInstaller::isRegionFile).sorted().toList();
      }
   }

   private static List<ExtractedIslandRegionInstaller.RegionResource> listBundledRegionFiles() throws IOException, URISyntaxException {
      ClassLoader loader = ExtractedIslandRegionInstaller.class.getClassLoader();
      URL directory = loader.getResource("data/opworldlike/extracted_islands/region/");
      if (directory != null && "file".equals(directory.getProtocol())) {
         try (Stream<Path> stream = Files.list(Path.of(directory.toURI()))) {
            return stream.filter(x$0 -> Files.isRegularFile(x$0))
               .filter(ExtractedIslandRegionInstaller::isRegionFile)
               .sorted()
               .map(
                  path -> new ExtractedIslandRegionInstaller.RegionResource(
                     path.getFileName().toString(), "data/opworldlike/extracted_islands/region/" + path.getFileName()
                  )
               )
               .toList();
         }
      } else if (directory != null && "jar".equals(directory.getProtocol())) {
         JarURLConnection connection = (JarURLConnection)directory.openConnection();

         try (JarFile jar = connection.getJarFile()) {
            return bundledRegionFilesFromJar(jar);
         }
      } else {
         URL ownLocation = ExtractedIslandRegionInstaller.class.getProtectionDomain().getCodeSource().getLocation();
         if (ownLocation == null) {
            return List.of();
         }

         Path ownPath = Path.of(ownLocation.toURI());
         if (!Files.isRegularFile(ownPath)) {
            return List.of();
         }

         try (JarFile jar = new JarFile(ownPath.toFile())) {
            return bundledRegionFilesFromJar(jar);
         }
      }
   }

   private static List<ExtractedIslandRegionInstaller.RegionResource> bundledRegionFilesFromJar(JarFile jar) {
      List<ExtractedIslandRegionInstaller.RegionResource> files = new ArrayList<>();
      Enumeration<JarEntry> entries = jar.entries();

      while (entries.hasMoreElements()) {
         JarEntry entry = entries.nextElement();
         if (!entry.isDirectory() && entry.getName().startsWith("data/opworldlike/extracted_islands/region/")) {
            String fileName = entry.getName().substring(entry.getName().lastIndexOf(47) + 1);
            if (fileName.matches("r\\.-?\\d+\\.-?\\d+\\.mca")) {
               files.add(new ExtractedIslandRegionInstaller.RegionResource(fileName, entry.getName()));
            }
         }
      }

      files.sort(Comparator.comparing(ExtractedIslandRegionInstaller.RegionResource::fileName));
      return files;
   }

   private static boolean hasRegionFiles(Path regionDir) throws IOException {
      if (!Files.isDirectory(regionDir)) {
         return false;
      }

      try (Stream<Path> stream = Files.list(regionDir)) {
         return stream.anyMatch(path -> Files.isRegularFile(path) && isRegionFile(path));
      }
   }

   private static boolean isRegionFile(Path path) {
      return path.getFileName().toString().matches("r\\.-?\\d+\\.-?\\d+\\.mca");
   }

   private record RegionResource(String fileName, String resourcePath) {
   }
}
