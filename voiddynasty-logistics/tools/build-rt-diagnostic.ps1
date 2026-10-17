param([Parameter(Mandatory=$true)][string]$GamePath)
$ErrorActionPreference = 'Stop'
$workspace = Split-Path -Parent $PSScriptRoot
$gameRoot = (Resolve-Path -LiteralPath $GamePath).Path
$managed = Join-Path $gameRoot 'WH40KRT_Data\Managed'
$ummZip = Join-Path $gameRoot 'WH40KRT_Data\StreamingAssets\OwlcatUnityModManager.zip'
$references = Join-Path $workspace '.runtime\umm'
$output = Join-Path $workspace 'dist\VoidDynastyBridge'
$compiler = Join-Path $env:WINDIR 'Microsoft.NET\Framework64\v4.0.30319\csc.exe'
foreach ($required in @($compiler, $ummZip, (Join-Path $managed 'UnityEngine.CoreModule.dll'), (Join-Path $managed 'UnityEngine.IMGUIModule.dll'))) {
    if (-not (Test-Path -LiteralPath $required)) { throw "Missing local reference: $required" }
}
New-Item -ItemType Directory -Path $references, $output -Force | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead($ummZip)
try {
    $umm = $archive.Entries | Where-Object Name -eq 'UnityModManager.dll'
    if (@($umm).Count -ne 1) { throw 'Expected exactly one UMM reference in the game archive' }
    [System.IO.Compression.ZipFileExtensions]::ExtractToFile($umm, (Join-Path $references 'UnityModManager.dll'), $true)
} finally { $archive.Dispose() }
$compilerArgs = @('/nologo', '/target:library', '/optimize+', ('/out:' + (Join-Path $output 'VoidDynastyBridge.dll')),
    ('/reference:' + (Join-Path $references 'UnityModManager.dll')),
    ('/reference:' + (Join-Path $managed 'UnityEngine.CoreModule.dll')),
    ('/reference:' + (Join-Path $managed 'UnityEngine.IMGUIModule.dll')),
    ('/reference:' + (Join-Path $managed 'netstandard.dll')),
    '/reference:System.Runtime.Serialization.dll',
    '/reference:System.Xml.dll',
    '/reference:System.IO.Compression.dll',
    '/reference:System.IO.Compression.FileSystem.dll',
    (Join-Path $workspace 'rt-adapter\DiagnosticMod.cs'),
    (Join-Path $workspace 'rt-adapter\DiagnosticServer.cs'),
    (Join-Path $workspace 'rt-adapter\DiagnosticJson.cs'),
    (Join-Path $workspace 'rt-adapter\PerSaveState.cs'),
    (Join-Path $workspace 'rt-adapter\PersistenceProbe.cs'),
    (Join-Path $workspace 'rt-adapter\GameSaveOperations.cs'),
    (Join-Path $workspace 'rt-adapter\DeliveryLedger.cs'),
    (Join-Path $workspace 'rt-adapter\DeliveryServer.cs'),
    (Join-Path $workspace 'rt-adapter\DeliveryBridge.cs'),
    (Join-Path $workspace 'rt-adapter\DeliveryAvailability.cs'),
    (Join-Path $workspace 'rt-adapter\CargoCatalog.cs'),
    (Join-Path $workspace 'rt-adapter\NativeCargo.cs'),
    (Join-Path $workspace 'rt-adapter\NativeCargoArchive.cs'))
& $compiler @compilerArgs
if ($LASTEXITCODE -ne 0) { throw "Diagnostic compilation failed ($LASTEXITCODE)" }
Copy-Item -LiteralPath (Join-Path $workspace 'rt-adapter\Info.json') -Destination $output
$files = Get-ChildItem -LiteralPath $output -File | Where-Object Name -ne 'sha256.txt'
$hashes = $files | Get-FileHash -Algorithm SHA256 | ForEach-Object { $_.Hash.ToLowerInvariant() + '  ' + (Split-Path -Leaf $_.Path) }
$hashes | Set-Content -LiteralPath (Join-Path $output 'sha256.txt') -Encoding utf8
@{ gamePath = $gameRoot; managed = $managed; compiler = $compiler } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $workspace '.runtime\rt-build.local.json') -Encoding utf8
Write-Output "Built Rogue Trader bridge: $output"
