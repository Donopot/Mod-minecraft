using System;
using System.IO;
using System.Linq;
using System.Reflection;

// Metadata only. ReflectionOnlyLoad never executes game code.
internal static class InspectAssemblies {
    public static void Main(string[] args) {
        var directory = Path.GetFullPath(args[0]);
        AppDomain.CurrentDomain.ReflectionOnlyAssemblyResolve += (sender, eventArgs) => {
            var path = Path.Combine(directory, new AssemblyName(eventArgs.Name).Name + ".dll");
            return File.Exists(path) ? Assembly.ReflectionOnlyLoadFrom(path) : Assembly.ReflectionOnlyLoad(eventArgs.Name);
        };
        foreach (var filename in args.Skip(1)) {
            var assembly = Assembly.ReflectionOnlyLoadFrom(Path.Combine(directory, filename));
            Console.WriteLine("ASSEMBLY " + assembly.FullName);
            Type[] types;
            try { types = assembly.GetTypes(); }
            catch (ReflectionTypeLoadException ex) { types = ex.Types.Where(t => t != null).ToArray(); }
            foreach (var type in types.Where(t => t.FullName == "Kingmaker.Game" || t.Name.Contains("KeyValue") || t.Name.Contains("ModEntry") || t.Name.Contains("PlayerData") || t.Name == "Player" || t.Name == "UnityModManager" || t.Name == "SaveInfo" || t.Name.Contains("SaveManager") || t.Name == "BlueprintArea" || t.Name == "Entity" || t.Name == "BaseUnitEntity" || t.Name == "Part" || t.Name == "EntityPart")) {
                Console.WriteLine("TYPE " + type.FullName + " : " + type.BaseType);
                foreach (var member in type.GetMembers(BindingFlags.Public | BindingFlags.Instance | BindingFlags.Static | BindingFlags.DeclaredOnly)) {
                    if (type.FullName == "Kingmaker.Game" || type.Name == "Player") {
                        if (!(member is PropertyInfo || member is FieldInfo) && !member.Name.Contains("Ensure") && !member.Name.Contains("Part")) continue;
                    }
                    try { Console.WriteLine("  " + member); }
                    catch (Exception ex) { Console.WriteLine("  " + member.Name + " [metadata unavailable: " + ex.GetType().Name + "]"); }
                }
            }
        }
    }
}
