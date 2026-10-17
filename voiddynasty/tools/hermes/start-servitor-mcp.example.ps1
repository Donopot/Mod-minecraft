# Example only: edit paths and place this on the Minecraft Windows host.
$ErrorActionPreference = 'Stop'
$servitorRepo = 'C:\VoidDynasty\Mod-minecraft'
$servitorPython = Join-Path $servitorRepo '.venv\Scripts\python.exe'
$servitorMcp = Join-Path $servitorRepo 'voiddynasty\tools\mcp_vd.py'
$env:VD_SERVER_DIR = 'C:\Minecraft\voiddynasty-server'
if (-not (Test-Path -LiteralPath $servitorPython -PathType Leaf)) { throw 'Missing MCP Python environment' }
if (-not (Test-Path -LiteralPath $servitorMcp -PathType Leaf)) { throw 'Missing servitor MCP script' }
if (-not (Test-Path -LiteralPath (Join-Path $env:VD_SERVER_DIR 'server.properties') -PathType Leaf)) { throw 'Missing Minecraft server.properties' }
# stdout is reserved for MCP JSON-RPC. Do not add banners or Write-Output.
& $servitorPython -u $servitorMcp --servitor-only
exit $LASTEXITCODE
