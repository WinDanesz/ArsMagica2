<#
.SYNOPSIS
    Bumps the mod version everywhere it is declared.

.EXAMPLE
    ./scripts/bump-version.ps1 1.6.7
#>
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$Version
)

$ErrorActionPreference = 'Stop'

if ($Version -notmatch '^\d+\.\d+\.\d+$') {
    throw "Version must look like 1.6.7 (got '$Version')"
}

$root = Split-Path -Parent $PSScriptRoot

# file (relative to repo root) -> regex with the version in group 2; groups 1 and 3 are kept
$targets = @(
    @{ File = 'gradle.properties';                 Pattern = '(?m)^(mod_version\s*=\s*)\d+\.\d+\.\d+()' },
    @{ File = 'gradle.properties';                 Pattern = '(?m)^(version\s*=\s*)\d+\.\d+\.\d+()' },
    @{ File = 'tags.properties';                   Pattern = '(?m)^(VERSION\s*=\s*)\d+\.\d+\.\d+()' },
    @{ File = '.forge/update.json';                Pattern = '("1\.12\.2-(?:latest|recommended)"\s*:\s*")\d+\.\d+\.\d+(")' },
    @{ File = 'src/main/resources/mcmod.info';     Pattern = '("version"\s*:\s*")\d+\.\d+\.\d+(")' }
)

$utf8 = New-Object System.Text.UTF8Encoding($false)
$contents = @{}

foreach ($t in $targets) {
    $path = Join-Path $root $t.File
    if (-not $contents.ContainsKey($path)) {
        $contents[$path] = [System.IO.File]::ReadAllText($path)
    }
    $before = $contents[$path]
    $after = [regex]::Replace($before, $t.Pattern, { param($m) $m.Groups[1].Value + $Version + $m.Groups[2].Value })
    if ($after -eq $before -and $before -notmatch [regex]::Escape($Version)) {
        throw "Pattern for $($t.File) did not match anything: $($t.Pattern)"
    }
    $contents[$path] = $after
}

foreach ($path in $contents.Keys) {
    [System.IO.File]::WriteAllText($path, $contents[$path], $utf8)
    Write-Host "Updated $([System.IO.Path]::GetRelativePath($root, $path)) -> $Version"
}
