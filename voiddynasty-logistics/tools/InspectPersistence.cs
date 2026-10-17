using System;
using System.IO;
using System.Linq;
using System.Reflection;

// Reflection-only metadata. No game assemblies are executed.
internal static class InspectPersistence {
    public static void Main(string[] args) {
        string directory = Path.GetFullPath(args[0]);
        AppDomain.CurrentDomain.ReflectionOnlyAssemblyResolve += (sender, eventArgs) => {
            string path = Path.Combine(directory, new AssemblyName(eventArgs.Name).Name + ".dll");
            return File.Exists(path) ? Assembly.ReflectionOnlyLoadFrom(path) : Assembly.ReflectionOnlyLoad(eventArgs.Name);
        };
        foreach (string file in args[1].Split(';')) {
            var assembly = Assembly.ReflectionOnlyLoadFrom(Path.Combine(directory, file));
            Type[] types;
            try { types = assembly.GetTypes(); }
            catch (ReflectionTypeLoadException error) { types = error.Types.Where(t => t != null).ToArray(); }
            foreach (var type in types.Where(t => args.Skip(2).Any(filter => filter.StartsWith("=") ? t.FullName == filter.Substring(1) : t.FullName.IndexOf(filter, StringComparison.OrdinalIgnoreCase) >= 0))) {
                Console.WriteLine("TYPE " + type.FullName + " : " + type.BaseType);
                foreach (var attribute in CustomAttributeData.GetCustomAttributes(type)) Console.WriteLine(" ATTRIBUTE " + attribute);
                foreach (var member in type.GetMembers(BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Static | BindingFlags.DeclaredOnly)) {
                    try {
                        Console.WriteLine(" " + member);
                        foreach (var attribute in CustomAttributeData.GetCustomAttributes(member)) {
                            if (attribute.AttributeType.Name.Contains("Json") || attribute.AttributeType.Name.Contains("Serial")) Console.WriteLine("   " + attribute);
                        }
                    } catch (Exception ex) { Console.WriteLine(" " + member.Name + " [" + ex.GetType().Name + "]"); }
                }
            }
        }
    }
}
