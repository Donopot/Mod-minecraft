using System;
using System.IO;
using System.IO.Compression;
using System.Runtime.Serialization.Json;
using System.Xml;
namespace VoidDynasty.RogueTrader {
    // No Unity dependency: verify the actual native entity, not just our receipt.
    public static class NativeCargoArchive {
        public static bool Contains(string filename,string id,string blueprint){
            using(var archive=ZipFile.OpenRead(filename)){
                var entry=archive.GetEntry("player.json");
                if(entry==null||entry.Length>33554432)throw new InvalidDataException("NATIVE_CARGO_SAVE_INVALID");
                using(var stream=entry.Open())using(var reader=JsonReaderWriterFactory.CreateJsonReader(stream,new XmlDictionaryReaderQuotas{
                    MaxDepth=128,MaxArrayLength=1000000,MaxStringContentLength=4194304,MaxNameTableCharCount=1048576,MaxBytesPerRead=4096})){
                    reader.MoveToContent();reader.ReadStartElement("root");
                    while(reader.MoveToContent()==XmlNodeType.Element){
                        if(reader.LocalName!="CargoState"){reader.Skip();continue;}
                        reader.ReadStartElement();
                        while(reader.MoveToContent()==XmlNodeType.Element){
                            if(reader.LocalName!="m_CargoEntities"){reader.Skip();continue;}
                            reader.ReadStartElement();int found=0;bool match=false;
                            while(reader.MoveToContent()==XmlNodeType.Element){
                                reader.ReadStartElement();string entityId=null,asset=null;
                                while(reader.MoveToContent()==XmlNodeType.Element){
                                    if(reader.LocalName=="UniqueId")entityId=reader.ReadElementContentAsString();
                                    else if(reader.LocalName=="Blueprint")asset=reader.ReadElementContentAsString();
                                    else reader.Skip();
                                }
                                reader.ReadEndElement();if(entityId==id){found++;match=asset==blueprint;}
                            }
                            return found==1&&match;
                        }
                        return false;
                    }
                    return false;
                }
            }
        }
    }
}
