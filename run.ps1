param(
    [switch]$Check
)

$ErrorActionPreference = 'Stop'

# Use installed tools without changing the user's permanent PATH.
$javaCandidates = @()
if ($env:JAVA_HOME) { $javaCandidates += $env:JAVA_HOME }
$javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
if ($javaCommand) { $javaCandidates += Split-Path (Split-Path $javaCommand.Source -Parent) -Parent }
$javaCandidates += Get-ChildItem -Path "$env:USERPROFILE/.jdks/*" -Directory -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending | Select-Object -ExpandProperty FullName
if ($env:ProgramFiles) {
    $javaCandidates += Get-ChildItem -Path "$env:ProgramFiles/JetBrains/IntelliJ IDEA */jbr" -Directory -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending | Select-Object -ExpandProperty FullName
}

$javaHomeForTravel = $null
foreach ($candidate in $javaCandidates) {
    $releaseFile = Join-Path $candidate 'release'
    if ((Test-Path (Join-Path $candidate 'bin/javac.exe')) -and (Test-Path $releaseFile)) {
        $versionLine = Get-Content -LiteralPath $releaseFile | Where-Object { $_ -match '^JAVA_VERSION=' } | Select-Object -First 1
        if ($versionLine -match '^JAVA_VERSION="(\d+)' -and [int]$Matches[1] -ge 21) {
            $javaHomeForTravel = $candidate
            break
        }
    }
}
if (-not $javaHomeForTravel) { throw 'JDK 21+ was not found. Install a JDK and set JAVA_HOME to its directory.' }

$mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
$mavenForTravel = if ($mavenCommand) { $mavenCommand.Source } else { $null }
if (-not $mavenForTravel -and $env:ProgramFiles) {
    $mavenForTravel = Get-ChildItem -Path "$env:ProgramFiles/JetBrains/IntelliJ IDEA */plugins/maven*/lib/maven3/bin/mvn.cmd" -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1 -ExpandProperty FullName
}
if (-not $mavenForTravel) { throw 'Maven was not found. Install Maven 3.9+ and add its bin directory to PATH, or run javafx:run from the IntelliJ Maven tool window.' }

$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
$travelExitCode = 1
Push-Location -LiteralPath $PSScriptRoot
try {
    $env:JAVA_HOME = $javaHomeForTravel
    $env:PATH = (Join-Path $javaHomeForTravel 'bin') + [IO.Path]::PathSeparator + $previousPath
    Write-Host "JDK:   $javaHomeForTravel"
    Write-Host "Maven: $mavenForTravel"
    if ($Check) { & $mavenForTravel --version }
    else { & $mavenForTravel javafx:run }
    $travelExitCode = $LASTEXITCODE
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
    Pop-Location
}
exit $travelExitCode
