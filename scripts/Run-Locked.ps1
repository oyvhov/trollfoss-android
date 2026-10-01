<#
.SYNOPSIS
    Runs a command while holding a named, machine-wide lock. Helpers use it to take turns on the shared
    phone emulator: install, start, wait and take screenshots in ONE call, so nobody steals the screen.

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Run-Locked.ps1 -Name emulator -Command "& C:\trollfoss-wt\stova\check.ps1"
#>
param(
    [Parameter(Mandatory = $true)][string]$Name,
    [Parameter(Mandatory = $true)][string]$Command,
    [int]$TimeoutMinutes = 30
)

$mutex = New-Object System.Threading.Mutex($false, "Global\Trollfoss_$Name")
$got = $false
try {
    $got = $mutex.WaitOne([TimeSpan]::FromMinutes($TimeoutMinutes))
} catch [System.Threading.AbandonedMutexException] {
    $got = $true
}
if (-not $got) {
    Write-Host "Timed out after $TimeoutMinutes minutes waiting for the '$Name' lock."
    exit 2
}
try {
    Invoke-Expression $Command
    exit $LASTEXITCODE
} finally {
    $mutex.ReleaseMutex()
}
