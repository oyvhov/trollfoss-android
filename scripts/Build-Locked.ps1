<#
.SYNOPSIS
    Runs a Gradle build in the current folder while holding a machine-wide lock, so that helpers working in
    parallel worktrees build one at a time (the machine has little free memory).

.EXAMPLE
    cd C:\trollfoss-wt\stova
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Build-Locked.ps1 -Tasks ':app:testDebugUnitTest',':app:lintDebug',':app:assembleDebug' -Suffix .stova
#>
param(
    [string[]]$Tasks = @(':app:assembleDebug'),
    # Debug package suffix, so that parallel builds never overwrite each other on the emulator.
    [string]$Suffix = '',
    [int]$TimeoutMinutes = 45,
    # Show Gradle's task output too (otherwise only warnings and errors).
    [switch]$Plain
)

# With -File, a list arrives as one string ("':a',':b'" or "':a :b'"): split it on commas and spaces and drop quotes.
$Tasks = @($Tasks | ForEach-Object { $_ -split '[,\s]+' } | ForEach-Object { $_.Trim("'", '"') } | Where-Object { $_ })

$env:TEMP = 'C:\topa\.gradle-tmp'
$env:TMP = 'C:\topa\.gradle-tmp'
$env:GRADLE_USER_HOME = 'C:\JellyBin\.gradle-home'

$mutex = New-Object System.Threading.Mutex($false, 'Global\TrollfossBuildLock')
$got = $false
try {
    $got = $mutex.WaitOne([TimeSpan]::FromMinutes($TimeoutMinutes))
} catch [System.Threading.AbandonedMutexException] {
    $got = $true
}
if (-not $got) {
    Write-Host "Timed out after $TimeoutMinutes minutes waiting for the build lock."
    exit 2
}
try {
    # One shared daemon, reused by every build (the lock makes sure only one runs at a time).
    $gradleArgs = @('--console=plain')
    if (-not $Plain) { $gradleArgs += '-q' }
    $gradleArgs += $Tasks
    if ($Suffix) { $gradleArgs += "-PtrollfossIdSuffix=$Suffix" }
    Write-Host "Building in $((Get-Location).Path): $($Tasks -join ' ')"
    & .\gradlew.bat @gradleArgs 2>&1 | ForEach-Object { $_.ToString() }
    $code = $LASTEXITCODE
    Write-Host "Build exit code: $code"
    exit $code
} finally {
    $mutex.ReleaseMutex()
}
