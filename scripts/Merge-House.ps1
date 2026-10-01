<#
.SYNOPSIS
    Merges the helper branches house/* (Storhuset, figures, map, seasons ...) into main one at a time.
    A branch that conflicts is aborted and listed, so it can be merged by hand afterwards.

.DESCRIPTION
    Run from anywhere; it works in C:\topa. It first sets up a LOCAL union-merge rule (in .git/info/attributes, not
    committed) for the shared list files where every helper added lines in its own block (enums, glimt, tasks,
    sounds, strings, music): a union merge keeps both sides' added lines. After merging, ALWAYS build and run the
    tests (scripts\Build-Locked.ps1 -Tasks ':app:testDebugUnitTest',':app:lintDebug',':app:assembleDebug') and fix what
    the union merge got wrong (duplicate lines, a missing comma). See docs/OVERLEVERING.md.

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Merge-House.ps1
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Merge-House.ps1 -Branches stova,oppe
#>
param([string[]]$Branches = @('figurar', 'stova', 'oppe', 'loft', 'kjeller', 'hage', 'kart', 'sesong', 'bygg'))

Set-Location C:\topa
$Branches = @($Branches | ForEach-Object { $_ -split '[,\s]+' } | Where-Object { $_ })

# Local union-merge rule for the shared lists (never committed).
$attr = Join-Path (Get-Location) '.git\info\attributes'
$base = 'app/src/main/java/app/trollfoss'
$rules = @(
    "$base/domain/Fixtures.kt merge=union", "$base/domain/Things.kt merge=union", "$base/domain/Secrets.kt merge=union",
    "$base/domain/Tasks.kt merge=union", "$base/audio/Synth.kt merge=union", "$base/audio/Music.kt merge=union",
    "$base/ui/Strings.kt merge=union"
)
New-Item -ItemType Directory -Force (Split-Path $attr) | Out-Null
[IO.File]::WriteAllText($attr, ($rules -join "`n") + "`n")

$dirty = git status --short
if ($dirty) { Write-Host 'main has uncommitted changes: commit or stash first.'; $dirty; exit 1 }

foreach ($n in $Branches) {
    $b = "house/$n"
    git rev-parse --verify --quiet $b | Out-Null
    if ($LASTEXITCODE -ne 0) { Write-Host "[$n] no branch"; continue }
    $ahead = (git log "main..$b" --oneline | Measure-Object).Count
    if ($ahead -eq 0) { Write-Host "[$n] nothing to merge"; continue }
    git merge --no-edit $b 2>&1 | Select-Object -Last 3 | ForEach-Object { Write-Host "[$n] $_" }
    $conflicts = git diff --name-only --diff-filter=U
    if ($conflicts) {
        Write-Host "[$n] CONFLICTS (merge aborted, do it by hand):"
        $conflicts | ForEach-Object { Write-Host "    $_" }
        git merge --abort
    } else {
        Write-Host "[$n] merged ($ahead commits)"
    }
}
git log --oneline | Select-Object -First 5
