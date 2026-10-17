[CmdletBinding(SupportsShouldProcess=$true)]
param(
    [Parameter(Mandatory=$true)][string]$VoidDynastyJar,
    [string]$MinecraftPath = (Join-Path $env:APPDATA '.minecraft'),
    [string]$RogueTraderModsPath = (Join-Path $env:USERPROFILE 'AppData\LocalLow\Owlcat Games\Warhammer 40000 Rogue Trader\UnityModManager'),
    [switch]$RogueTraderOnly
)
$ErrorActionPreference = 'Stop'
$workspace = Split-Path -Parent $PSScriptRoot
$original = (Resolve-Path -LiteralPath $VoidDynastyJar).Path
$minecraftRoot = (Resolve-Path -LiteralPath $MinecraftPath).Path
$modsRoot = Join-Path $minecraftRoot 'mods'
$rtRoot = [IO.Path]::GetFullPath((Join-Path $RogueTraderModsPath 'VoidDynastyBridge'))
$processNames = if ($RogueTraderOnly) { @('WH40KRT') } else { @('WH40KRT', 'javaw') }
if (Get-Process -Name $processNames -ErrorAction SilentlyContinue) { throw 'Close the target game before installing diagnostics.' }
Add-Type -AssemblyName System.IO.Compression.FileSystem
function Get-FabricId([string]$JarPath) {
    $archive = [IO.Compression.ZipFile]::OpenRead($JarPath)
    try {
        $entry = $archive.GetEntry('fabric.mod.json')
        if ($null -eq $entry) { return $null }
        $reader = [IO.StreamReader]::new($entry.Open())
        try { return ($reader.ReadToEnd() | ConvertFrom-Json).id }
        finally { $reader.Dispose() }
    } finally { $archive.Dispose() }
}
if ((Get-FabricId $original) -ne 'voiddynasty') { throw 'The supplied JAR is not VOID DYNASTY.' }
function Get-ModDestination([string]$Id, [string]$DefaultName) {
    $installedCopies = @(Get-ChildItem -LiteralPath $modsRoot -Filter '*.jar' -File | Where-Object { (Get-FabricId $_.FullName) -eq $Id })
    if ($installedCopies.Count -gt 1) { throw "Multiple installed copies of $Id found; resolve the duplicate before installation." }
    if ($installedCopies.Count -eq 1) { return $installedCopies[0].FullName }
    return Join-Path $modsRoot $DefaultName
}
$plan = @(
    @{ source = (Join-Path $workspace 'dist\VoidDynastyBridge\VoidDynastyBridge.dll'); target = (Join-Path $rtRoot 'VoidDynastyBridge.dll') },
    @{ source = (Join-Path $workspace 'dist\VoidDynastyBridge\Info.json'); target = (Join-Path $rtRoot 'Info.json') }
)
if (-not $RogueTraderOnly) {
    $plan = @(
        @{ source = $original; target = (Get-ModDestination 'voiddynasty' 'voiddynasty-1.0.0.jar') },
        @{ source = (Join-Path $workspace 'dist\voiddynasty-bridge-0.7.0.jar'); target = (Get-ModDestination 'voiddynasty_bridge' 'voiddynasty-bridge-0.7.0.jar') }
    ) + $plan
}
foreach ($item in $plan) {
    if (-not (Test-Path -LiteralPath $item.source -PathType Leaf)) { throw "Missing artifact: $($item.source)" }
    $item.target = [IO.Path]::GetFullPath($item.target)
    $allowed = @($modsRoot, $rtRoot) | Where-Object { $item.target.StartsWith(([IO.Path]::GetFullPath($_).TrimEnd('\') + '\'), [StringComparison]::OrdinalIgnoreCase) }
    if (-not $allowed) { throw "Target outside intended mod folders: $($item.target)" }
    if ((Test-Path -LiteralPath $item.target) -and -not (Test-Path -LiteralPath $item.target -PathType Leaf)) { throw "Target is not a regular file: $($item.target)" }
}
$plan | ForEach-Object { Write-Output ($_.source + ' -> ' + $_.target) }
if (-not $PSCmdlet.ShouldProcess('Minecraft mods and Rogue Trader UMM folders', 'Install compiled diagnostics with backups; no game launch or save changes')) { return }
$backup = Join-Path $workspace ('.runtime\install-' + [Guid]::NewGuid().ToString())
New-Item -ItemType Directory -Path $backup -Force | Out-Null
$records = @()
for ($index = 0; $index -lt $plan.Count; $index++) {
    $item = $plan[$index]
    $previous = $null
    if (Test-Path -LiteralPath $item.target -PathType Leaf) {
        $previous = Join-Path $backup ($index.ToString() + '-' + [IO.Path]::GetFileName($item.target))
        Copy-Item -LiteralPath $item.target -Destination $previous
    }
    $records += @{ source = $item.source; target = $item.target; previous = $previous;
        installed_sha256 = (Get-FileHash -LiteralPath $item.source -Algorithm SHA256).Hash }
}
@{ status = 'prepared'; files = $records } | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $backup 'manifest.local.json') -Encoding utf8
$written = @()
try {
    foreach ($record in $records) {
        New-Item -ItemType Directory -Path ([IO.Path]::GetDirectoryName($record.target)) -Force | Out-Null
        $written += $record
        Copy-Item -LiteralPath $record.source -Destination $record.target -Force
        if ((Get-FileHash -LiteralPath $record.target -Algorithm SHA256).Hash -ne $record.installed_sha256) { throw "Installed hash mismatch: $($record.target)" }
    }
    @{ status = 'installed'; files = $records } | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $backup 'manifest.local.json') -Encoding utf8
} catch {
    foreach ($record in $written) {
        if ($record.previous) { Copy-Item -LiteralPath $record.previous -Destination $record.target -Force }
        elseif (Test-Path -LiteralPath $record.target -PathType Leaf) { Remove-Item -LiteralPath $record.target -Force }
    }
    throw
}
Write-Output ('Installed with verified hashes. Backup manifest: ' + (Join-Path $backup 'manifest.local.json'))
Write-Output 'Games were not launched. Existing delivery: reload its matching REWARD_COMMITTED save and world. New pair: use PERSISTANCE VALIDEE and /voidbridge.'
