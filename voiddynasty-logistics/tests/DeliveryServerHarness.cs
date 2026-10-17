using System;
using System.Threading;
using VoidDynasty.RogueTrader;
internal static class DeliveryServerHarness {
    public static void Main(string[] args) {
        try {using(var server=new DeliveryServer(args[0],Int32.Parse(args[1]))) {
            server.Start();Console.WriteLine("READY");DeliveryJob held=null;string command;
            while((command=Console.ReadLine())!=null && command!="STOP") {
                if(command=="TAKE") {
                    var timer=System.Diagnostics.Stopwatch.StartNew();while(!server.Take(out held)&&timer.ElapsedMilliseconds<3000)Thread.Sleep(10);
                    if(held==null)throw new Exception("No job");Console.WriteLine("PENDING "+held.Message["type"]);
                } else if(command=="DURABLE") {
                    held.Reply.SetResult(DiagnosticJson.Serialize(new DeliveryReply{reply_to=held.Message["message_id"],session_id="00000000-0000-4000-8000-000000000001",campaign_id="00000000-0000-4000-8000-000000000002",state="REWARD_COMMITTED",reserve=10,completed_count=1,last_receipt_id="00000000-0000-4000-8000-000000000005",available=true}));held=null;Console.WriteLine("ACK");
                }
            }
        }} catch(Exception e){Console.Error.WriteLine(e);Environment.ExitCode=1;}
    }
}
