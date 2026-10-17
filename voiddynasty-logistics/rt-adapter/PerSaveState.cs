using System;
using System.Collections;
using System.IO;
using System.IO.Compression;
using System.Runtime.Serialization.Json;
using System.Text;
using System.Xml;

namespace VoidDynasty.RogueTrader {
    public sealed class DeliveryReceipt {
        public string delivery_id;
        public string payload_hash;
        public int sequence;
        public string mission_id;
        public string reward_kind;
        public string native_cargo_id;
        public string native_blueprint_id;
    }
    public sealed class BridgeSaveState {
        public int schema_version = 1;
        public string bridge_campaign_id;
        public string native_campaign_id;
        public string probe_receipt;
        public bool persistence_verified;
        public int logistics_reserve;
        public string pair_id;
        public string world_id;
        public string checkpoint_id;
        public string delivery_id;
        public string delivery_state;
        public string payload_hash;
        public int ledger_version;
        public System.Collections.Generic.List<DeliveryReceipt> receipts;
        public string mission_id;
    }

    public static class PerSaveState {
        public const string Key = "voiddynasty_bridge_state_v1";
        public static BridgeSaveState Decode(string json) {
            if (json == null || Encoding.UTF8.GetByteCount(json) > 131072) throw new InvalidDataException("Invalid bridge state");
            using (var stream = new MemoryStream(Encoding.UTF8.GetBytes(json))) {
                var value = (BridgeSaveState)new DataContractJsonSerializer(typeof(BridgeSaveState)).ReadObject(stream);
                Guid campaign, receipt;
                if (value == null || value.schema_version != 1 || !Guid.TryParse(value.bridge_campaign_id, out campaign)
                        || !Guid.TryParse(value.probe_receipt, out receipt) || String.IsNullOrEmpty(value.native_campaign_id)
                        || value.logistics_reserve < 0) throw new InvalidDataException("Invalid bridge state");
                return value;
            }
        }
        public static BridgeSaveState Get(IDictionary settings) {
            if (settings == null || !settings.Contains(Key)) return null;
            var json = settings[Key] as string;
            if (json == null) throw new InvalidDataException("Bridge state is not a string");
            return Decode(json);
        }
        public static void Put(IDictionary settings, BridgeSaveState state) {
            if (settings == null) throw new InvalidDataException("No loaded save settings");
            settings[Key] = DiagnosticJson.Serialize(state);
        }
        public static string ReadArchive(string filename) {
            using (var archive = ZipFile.OpenRead(filename)) {
                var entry = archive.GetEntry("settings.json");
                if (entry == null || entry.Length > 1048576) throw new InvalidDataException("No bounded settings entry");
                using (var stream = entry.Open())
                using (var reader = JsonReaderWriterFactory.CreateJsonReader(stream, new XmlDictionaryReaderQuotas {
                    MaxDepth = 32, MaxStringContentLength = 1048576, MaxArrayLength = 100000,
                    MaxBytesPerRead = 4096, MaxNameTableCharCount = 65536
                })) {
                    reader.MoveToContent();
                    if (reader.LocalName != "root" || reader.GetAttribute("type") != "object") throw new InvalidDataException("Invalid settings root");
                    reader.ReadStartElement("root");
                    reader.MoveToContent();
                    if (reader.LocalName != "List" || reader.GetAttribute("type") != "object") throw new InvalidDataException("Invalid settings list");
                    reader.ReadStartElement("List");
                    string found = null;
                    while (reader.MoveToContent() == XmlNodeType.Element) {
                        if (reader.LocalName == Key) {
                            if (found != null || reader.GetAttribute("type") != "string") throw new InvalidDataException("Invalid saved bridge state");
                            found = reader.ReadElementContentAsString();
                        } else reader.Skip();
                    }
                    return found;
                }
            }
        }
    }
}
