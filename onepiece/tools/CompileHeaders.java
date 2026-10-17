import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import com.google.gson.JsonParser;
import net.fabricmc.loader.impl.lib.classtweaker.api.ClassTweaker;
import net.fabricmc.loader.impl.lib.classtweaker.api.ClassTweakerReader;
import org.objectweb.asm.*;

/** Apply installed mods' actual class tweakers to temporary javac headers only. Never a runtime artifact. */
public class CompileHeaders {
    public static void main(String[] args) throws Exception {
        var tweaker=ClassTweaker.newInstance();
        var reader=ClassTweakerReader.create(tweaker);
        int definitions=0;
        for(int i=2;i<args.length;i++) {
            try(var jar=new ZipFile(args[i])) {
                var meta=jar.getEntry("fabric.mod.json"); if(meta==null)continue;
                var json=JsonParser.parseString(new String(jar.getInputStream(meta).readAllBytes(),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
                if(!json.has("accessWidener"))continue;
                var entry=jar.getEntry(json.get("accessWidener").getAsString());
                if(entry==null)throw new IllegalStateException("Missing class tweaker in "+args[i]);
                reader.read(jar.getInputStream(entry).readAllBytes(),"official");definitions++;
            }
        }
        var result=new TreeMap<String,byte[]>();
        try(var game=new ZipFile(args[0])) {
            for(String target:tweaker.getTargets()) {
                String name=target.replace('.','/')+".class";
                var entry=game.getEntry(name);if(entry==null)continue;
                var writer=new ClassWriter(0);
                var visitor=tweaker.createClassVisitor(Opcodes.ASM9,writer,(n,b)->result.put(n+".class",b));
                new ClassReader(game.getInputStream(entry).readAllBytes()).accept(visitor,0);
                result.put(name,writer.toByteArray());
            }
        }
        Files.createDirectories(Path.of(args[1]).getParent());
        try(var zip=new ZipOutputStream(Files.newOutputStream(Path.of(args[1])))) {
            for(var entry:result.entrySet()) {zip.putNextEntry(new ZipEntry(entry.getKey()));zip.write(entry.getValue());zip.closeEntry();}
        }
        System.out.println("COMPILE_HEADERS "+definitions+" class tweakers; "+result.size()+" temporary classes");
    }
}
