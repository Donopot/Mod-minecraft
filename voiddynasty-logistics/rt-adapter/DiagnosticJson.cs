using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.Serialization.Json;
using System.Text;
using System.Xml;

namespace VoidDynasty.RogueTrader {
    // Small diagnostic objects use the runtime serializer, with strict primitive
    // reads for authentication/configuration. No external JSON DLL is needed.
    public static class DiagnosticJson {
        public static string Serialize<T>(T value) {
            using (var stream = new MemoryStream()) {
                new DataContractJsonSerializer(typeof(T)).WriteObject(stream, value);
                return Encoding.UTF8.GetString(stream.ToArray());
            }
        }

        public static Dictionary<string, string> ReadPrimitives(string json, Dictionary<string, string> expected) {
            var quotas = new XmlDictionaryReaderQuotas { MaxDepth = 4, MaxStringContentLength = 4096,
                MaxArrayLength = 8, MaxBytesPerRead = 4096, MaxNameTableCharCount = 1024 };
            var result = new Dictionary<string, string>();
            using (var reader = JsonReaderWriterFactory.CreateJsonReader(Encoding.UTF8.GetBytes(json), quotas)) {
                reader.MoveToContent();
                if (reader.LocalName != "root" || reader.GetAttribute("type") != "object") throw new InvalidDataException("Expected object");
                reader.ReadStartElement("root");
                while (reader.MoveToContent() == XmlNodeType.Element) {
                    var name = reader.LocalName;
                    if (result.ContainsKey(name) || !expected.ContainsKey(name) || reader.GetAttribute("type") != expected[name])
                        throw new InvalidDataException("Unexpected field");
                    result.Add(name, reader.ReadElementContentAsString());
                }
                reader.ReadEndElement();
                if (reader.MoveToContent() != XmlNodeType.None || result.Count != expected.Count) throw new InvalidDataException("Invalid object");
            }
            return result;
        }
    }

    public sealed class DiagnosticConfiguration {
        public int protocol_version = 1;
        public int port = DiagnosticServer.Port;
        public string token;
    }

    public sealed class DiagnosticSnapshot {
        public int protocol_version = 1;
        public string type = "diagnostic_snapshot";
        public bool diagnostic_only = true;
        public bool delivery_enabled = false;
        public string session_id;
        public long sequence;
        public bool campaign_loaded;
        public string native_campaign_id;
        public string zone;
        public string game_mode;
        public string save_state;
        public string status;
    }
}
