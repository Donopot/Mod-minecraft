using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using System.Collections.Generic;
using System.Runtime.Serialization;
using System.Xml;

namespace VoidDynasty.RogueTrader {
    // Separate read-only diagnostic channel. Never accepts a delivery command.
    // The game thread publishes serialized snapshots; the worker sees strings only.
    public sealed class DiagnosticServer : IDisposable {
        public const int Port = 28766;
        private volatile string snapshot;
        private volatile string lastError = "Starting";
        private volatile bool stopping;
        private readonly string token;
        private readonly int port;
        private TcpListener listener;
        private TcpClient client;
        private readonly object gate = new object();
        private Thread worker;

        public DiagnosticServer(string token, int port = Port) {
            if (token == null || token.Length != 64) throw new ArgumentException("Expected a 64-character token");
            if (port < 1 || port > 65535) throw new ArgumentException("Invalid port");
            this.token = token;
            this.port = port;
        }

        public string Status { get { return lastError; } }
        public void Publish(string json) {
            if (Encoding.UTF8.GetByteCount(json) > 65535) throw new ArgumentException("Snapshot too large");
            snapshot = json;
        }

        public static string LoadOrCreateToken() {
            var directory = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "VoidDynastyBridge");
            Directory.CreateDirectory(directory);
            var path = Path.Combine(directory, "diagnostic.local.json");
            if (File.Exists(path)) {
                if (new FileInfo(path).Length > 4096) throw new InvalidDataException("Diagnostic configuration too large");
                var existing = DiagnosticJson.ReadPrimitives(File.ReadAllText(path), new Dictionary<string, string> {
                    { "protocol_version", "number" }, { "port", "number" }, { "token", "string" }
                });
                var value = existing["token"];
                if (existing["protocol_version"] != "1" || existing["port"] != Port.ToString(System.Globalization.CultureInfo.InvariantCulture) || !IsToken(value))
                    throw new InvalidDataException("Invalid diagnostic configuration");
                return value;
            }
            var bytes = new byte[32];
            using (var rng = RandomNumberGenerator.Create()) rng.GetBytes(bytes);
            var generated = BitConverter.ToString(bytes).Replace("-", "").ToLowerInvariant();
            // CreateNew avoids overwriting a secret established by another process.
            using (var stream = new FileStream(path, FileMode.CreateNew, FileAccess.Write, FileShare.None))
            using (var writer = new StreamWriter(stream, new UTF8Encoding(false))) {
                writer.Write(DiagnosticJson.Serialize(new DiagnosticConfiguration { token = generated }));
            }
            return generated;
        }

        private static bool IsToken(string value) {
            if (value == null || value.Length != 64) return false;
            foreach (char ch in value) if (!((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f'))) return false;
            return true;
        }

        private bool Authenticated(string line) {
            try {
                var value = DiagnosticJson.ReadPrimitives(line, new Dictionary<string, string> {
                    { "protocol_version", "number" }, { "type", "string" }, { "token", "string" }
                });
                if (value["protocol_version"] != "1" || value["type"] != "diagnostic_hello") return false;
                var supplied = value["token"];
                if (!IsToken(supplied)) return false;
                int difference = 0;
                for (int i = 0; i < token.Length; i++) difference |= supplied[i] ^ token[i];
                return difference == 0;
            } catch (XmlException) { return false; }
            catch (SerializationException) { return false; }
            catch (InvalidDataException) { return false; }
            catch (IOException) { return false; }
            catch (ArgumentException) { return false; }
            catch (InvalidCastException) { return false; }
            catch (OverflowException) { return false; }
            catch (FormatException) { return false; }
        }

        private static string ReadHello(NetworkStream stream) {
            var bytes = new MemoryStream();
            // Absolute five-second handshake deadline: partial bytes cannot extend it.
            var timer = System.Diagnostics.Stopwatch.StartNew();
            while (bytes.Length < 4096) {
                int remaining = 5000 - (int)timer.ElapsedMilliseconds;
                if (remaining <= 0) throw new IOException("Handshake expired");
                stream.ReadTimeout = remaining;
                int next = stream.ReadByte();
                if (next < 0) throw new IOException("Disconnected");
                if (next == 10) return new UTF8Encoding(false, true).GetString(bytes.ToArray());
                bytes.WriteByte((byte)next);
            }
            throw new IOException("Hello too large");
        }

        public void Start() {
            if (worker != null) throw new InvalidOperationException("Already started");
            listener = new TcpListener(IPAddress.Loopback, port);
            listener.Start(1);
            worker = new Thread(Run) { IsBackground = true, Name = "VoidDynasty diagnostic" };
            worker.Start();
        }

        private void Run() {
            while (!stopping) {
                try {
                    lastError = "Listening on loopback (diagnostic only)";
                    var accepted = listener.AcceptTcpClient();
                    lock (gate) {
                        if (stopping) { accepted.Close(); break; }
                        client = accepted;
                    }
                    using (accepted) {
                        accepted.NoDelay = true;
                        accepted.SendTimeout = 2000;
                        var stream = accepted.GetStream();
                        if (!Authenticated(ReadHello(stream))) { lastError = "Diagnostic authentication rejected"; continue; }
                        lastError = "Minecraft diagnostic connected";
                        while (!stopping) {
                            var current = snapshot;
                            if (current != null) {
                                var data = Encoding.UTF8.GetBytes(current + "\n");
                                stream.Write(data, 0, data.Length);
                            }
                            Thread.Sleep(1000);
                        }
                    }
                } catch (SocketException) { lastError = "Diagnostic disconnected"; }
                catch (IOException) { lastError = "Diagnostic disconnected"; }
                catch (DecoderFallbackException) { lastError = "Diagnostic UTF-8 rejected"; }
                catch (ObjectDisposedException) { if (stopping) break; lastError = "Diagnostic stopped"; }
                catch (ThreadInterruptedException) { if (stopping) break; }
                catch (Exception failure) { lastError = "Diagnostic request rejected: " + failure.GetType().Name; }
                finally { lock (gate) { client = null; } }
            }
        }

        public void Dispose() {
            stopping = true;
            if (listener != null) listener.Stop();
            lock (gate) { if (client != null) client.Close(); }
            if (worker != null && worker.IsAlive) { worker.Interrupt(); worker.Join(1000); }
            lastError = "Stopped";
        }
    }
}
