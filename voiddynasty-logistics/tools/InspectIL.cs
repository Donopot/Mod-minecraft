using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Reflection.Emit;

// Metadata/IL reader only. Write any game-derived output to private temporary storage.
internal static class InspectIL {
    private static readonly Dictionary<short, OpCode> Codes = typeof(OpCodes).GetFields(BindingFlags.Public | BindingFlags.Static)
        .Where(f => f.FieldType == typeof(OpCode)).Select(f => (OpCode)f.GetValue(null)).ToDictionary(c => c.Value);
    public static void Main(string[] args) {
        string directory = Path.GetFullPath(args[0]);
        AppDomain.CurrentDomain.ReflectionOnlyAssemblyResolve += (sender, eventArgs) => {
            string path = Path.Combine(directory, new AssemblyName(eventArgs.Name).Name + ".dll");
            return File.Exists(path) ? Assembly.ReflectionOnlyLoadFrom(path) : Assembly.ReflectionOnlyLoad(eventArgs.Name);
        };
        var assembly = Assembly.ReflectionOnlyLoadFrom(Path.Combine(directory, args[1]));
        foreach (string target in args.Skip(2)) {
            var split = target.Split(new[] { "::" }, StringSplitOptions.None);
            var type = assembly.GetType(split[0], true);
            IEnumerable<MethodBase> methods = type.GetMethods(BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Static | BindingFlags.Instance | BindingFlags.DeclaredOnly).Cast<MethodBase>()
                .Concat(type.GetConstructors(BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Static | BindingFlags.Instance));
            foreach (var method in methods.Where(m => split.Length == 1 || m.Name == split[1])) {
                Console.WriteLine("METHOD " + type.FullName + "::" + method.Name);
                try { Dump(method); } catch (Exception ex) { Console.WriteLine("Unavailable: " + ex.GetType().Name); }
            }
        }
    }
    private static void Dump(MethodBase method) {
        var body = method.GetMethodBody();
        if (body == null) return;
        var bytes = body.GetILAsByteArray();
        for (int index = 0; index < bytes.Length;) {
            int offset = index;
            short value = bytes[index++];
            if (value == 0xfe) value = (short)(0xfe00 | bytes[index++]);
            var op = Codes[value];
            object operand = null;
            int size = 0;
            switch (op.OperandType) {
                case OperandType.InlineNone: break;
                case OperandType.ShortInlineI: operand = (sbyte)bytes[index]; size = 1; break;
                case OperandType.ShortInlineVar: operand = bytes[index]; size = 1; break;
                case OperandType.InlineVar: operand = BitConverter.ToUInt16(bytes, index); size = 2; break;
                case OperandType.InlineI: operand = BitConverter.ToInt32(bytes, index); size = 4; break;
                case OperandType.InlineI8: operand = BitConverter.ToInt64(bytes, index); size = 8; break;
                case OperandType.ShortInlineR: operand = BitConverter.ToSingle(bytes, index); size = 4; break;
                case OperandType.InlineR: operand = BitConverter.ToDouble(bytes, index); size = 8; break;
                case OperandType.ShortInlineBrTarget: operand = "IL_" + (index + 1 + (sbyte)bytes[index]).ToString("x4"); size = 1; break;
                case OperandType.InlineBrTarget: operand = "IL_" + (index + 4 + BitConverter.ToInt32(bytes, index)).ToString("x4"); size = 4; break;
                case OperandType.InlineSwitch:
                    int count = BitConverter.ToInt32(bytes, index);
                    size = 4 + 4 * count; operand = "switch " + count; break;
                default:
                    int token = BitConverter.ToInt32(bytes, index); size = 4;
                    try { operand = op.OperandType == OperandType.InlineString ? (object)("\"" + method.Module.ResolveString(token) + "\"") : method.Module.ResolveMember(token, method.DeclaringType.GetGenericArguments(), method.IsGenericMethod ? method.GetGenericArguments() : Type.EmptyTypes); }
                    catch { operand = "token " + token.ToString("x8"); }
                    break;
            }
            index += size;
            try { Console.WriteLine(" IL_" + offset.ToString("x4") + " " + op.Name + " " + operand); }
            catch { Console.WriteLine(" IL_" + offset.ToString("x4") + " " + op.Name + " [unavailable type]"); }
        }
    }
}
