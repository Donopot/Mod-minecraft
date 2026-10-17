using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace VoidDynasty.RogueTrader {
    public sealed class DeliveryJob {
        public Dictionary<string,string> Message;
        public readonly TaskCompletionSource<string> Reply = new TaskCompletionSource<string>();
    }
    public sealed class DeliveryReply {
        public int protocol_version=4;
        public string reply_to="", code="OK", session_id="", campaign_id="", world_id="", pair_id="", checkpoint_id="";
        public string delivery_id="", state="", payload_hash="", zone="", campaign_name="";
        public int reserve;
        public int completed_count;
        public int native_count;
        public string mission_id="logistics_first_delivery";
        public string last_receipt_id="";
        public bool available;
    }
    public sealed class DeliveryServer : IDisposable {
        public const int Port=28765;
        private readonly string token;
        private readonly int port;
        private readonly ConcurrentQueue<DeliveryJob> queue = new ConcurrentQueue<DeliveryJob>();
        private TcpListener listener;
        private TcpClient client;
        private Thread worker;
        private volatile bool stopped;
        public DeliveryServer(string token,int port=Port) { this.token=token;this.port=port; }
        public bool Take(out DeliveryJob job) { return queue.TryDequeue(out job); }
        public void Start() { listener=new TcpListener(IPAddress.Loopback,port);listener.Start(1);worker=new Thread(Run){IsBackground=true,Name="VoidDynasty delivery"};worker.Start(); }
        public static readonly Dictionary<string,string> Fields=new Dictionary<string,string> {
            {"protocol_version","number"},{"type","string"},{"message_id","string"},{"token","string"},
            {"session_id","string"},{"campaign_id","string"},{"world_id","string"},{"pair_id","string"},{"checkpoint_id","string"},
            {"delivery_id","string"},{"known_state","string"},{"mission_hash","string"},{"payload_hash","string"},{"item_id","string"},{"count","number"},
            {"completed_count","number"},{"last_receipt_id","string"},{"mission_id","string"}
        };
        private void Run() {
            while (!stopped) {
                try {
                    using (var c=listener.AcceptTcpClient()) {
                        client=c;c.ReceiveTimeout=3000;c.SendTimeout=3000;c.NoDelay=true;
                        var stream=c.GetStream();var bytes=new List<byte>();var timer=System.Diagnostics.Stopwatch.StartNew();
                        while(true) { if(timer.ElapsedMilliseconds>3000 || bytes.Count>=4095) throw new IOException();int b=stream.ReadByte();if(b<0) throw new IOException();if(b==10)break;bytes.Add((byte)b); }
                        var m=DiagnosticJson.ReadPrimitives(new UTF8Encoding(false,true).GetString(bytes.ToArray()),Fields);
                        if(m["protocol_version"]!="4" || m["token"].Length!=token.Length)throw new IOException();
                        int diff=0;for(int i=0;i<token.Length;i++)diff|=token[i]^m["token"][i];if(diff!=0)throw new IOException();
                        if(!DeliveryLedger.Uuid(m["message_id"]) || queue.Count>=1)throw new IOException();
                        var job=new DeliveryJob{Message=m};queue.Enqueue(job);
                        if(!job.Reply.Task.Wait(125000))throw new IOException();
                        byte[] result=Encoding.UTF8.GetBytes(job.Reply.Task.Result+"\n");stream.Write(result,0,result.Length);
                    }
                } catch(Exception) { if(stopped)break; }
                finally { client=null; }
            }
        }
        public void Dispose() { stopped=true;if(listener!=null)listener.Stop();if(client!=null)client.Close();if(worker!=null)worker.Join(1000); }
    }
}
