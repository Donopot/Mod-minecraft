# Read-only diagnostic link — 0.2.0

This is a separate channel from delivery protocol v1. It proves live data transport without treating a native campaign ID as a paired bridge identity. It accepts no preparation, commit, cancellation, inventory or arbitrary command.

The message shape is formalized in `protocol/schemas/diagnostic.schema.json`.

RT listens on `127.0.0.1:28766`. Minecraft connects from a daemon worker. Authentication uses a random 32-byte secret encoded as 64 lowercase hexadecimal characters in `%LOCALAPPDATA%/VoidDynastyBridge/diagnostic.local.json`. RT creates it once; both processes read it. It is not included in packages or logs. No firewall opening is needed.

The client sends one UTF-8 JSON line:

```json
{"protocol_version":1,"type":"diagnostic_hello","token":"<64 lowercase hex characters from local configuration>"}
```

The hello must contain exactly those fields and types; repeated/unknown fields, wrong token, wrong version, invalid UTF-8 and input over 4096 bytes including LF are rejected. The five-second authentication deadline is absolute, so partial data cannot keep an unauthenticated connection alive indefinitely.

After authentication the server publishes `diagnostic_snapshot` once a second, up to 65,536 bytes including LF. Fields: `protocol_version`, `type`, `diagnostic_only: true`, `delivery_enabled: false`, `session_id`, advancing `sequence`, `campaign_loaded`, `native_campaign_id`, `zone`, `game_mode`, `save_state`, `status`. Unloaded campaign and zone are null.

The Unity callback performs all game reads and serializes a new snapshot. The worker sends immutable serialized strings only. The session changes when the player object changes or unloads. The game callback advances the sequence; the network worker cannot advance it. A frozen Unity loop cannot keep old campaign state fresh by retransmitting it.

Minecraft clears state on disconnect/invalid data and expires state after six seconds without a new advancing snapshot. Command handling requires a local single-player world. The command reads immutable data; the network thread never calls game APIs.

The server services one diagnostic connection at a time; additional connections wait in a small OS backlog and time out. Closing/unloading the mod stops the listener and active connection. Port conflict or invalid local configuration disables the link visibly in the RT panel. The C# diagnostic uses the installed runtime's built-in data-contract JSON serializer/reader and has no Newtonsoft.Json dependency.

Verification uses the actual compiled C# server, actual serialized DTOs and actual compiled Java client in standalone harnesses. These verify transport, rejection, stale-data expiry and menu-state clearing. They do not validate UMM/Fabric loading, native game reads, UI rendering or save persistence inside the engines.
