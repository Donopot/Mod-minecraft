import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import net.fabricmc.loader.impl.lib.classtweaker.api.ClassTweaker;
import net.fabricmc.loader.impl.lib.classtweaker.api.ClassTweakerReader;
import net.fabricmc.loader.impl.lib.classtweaker.classvisitor.AccessWidenerClassVisitor;

/** Compilation-only view of the real game, applying the mod's declared access widener.
 * Never load this generated JAR in Minecraft and never package its classes in the mod.
 */
public final class CompileAccess {
    public static void main(String[] args) throws Exception {
        var tweaker=ClassTweaker.newInstance();
        ClassTweakerReader.create(tweaker).read(Files.readAllBytes(Path.of(args[2])),"official");
        Set<String> names=new TreeSet<>();
        for(String target:tweaker.getTargets()) {
            String name=target.replace('.','/');names.add(name);
            while(name.contains("$")) {name=name.substring(0,name.lastIndexOf('$'));names.add(name);}
        }
        try(var input=new JarFile(args[0]);var output=new JarOutputStream(Files.newOutputStream(Path.of(args[1])))) {
            for(String name:names) {
                var original=input.getJarEntry(name+".class");
                if(original==null) throw new IllegalArgumentException("Missing game class: "+name);
                var writer=new ClassWriter(0);
                new ClassReader(input.getInputStream(original)).accept(new AccessWidenerClassVisitor(Opcodes.ASM9,writer,tweaker),0);
                var entry=new JarEntry(name+".class");entry.setTime(0);output.putNextEntry(entry);output.write(writer.toByteArray());output.closeEntry();
            }
        }
        System.out.println("Compile-only access view: "+names.size()+" vanilla classes");
    }
}
