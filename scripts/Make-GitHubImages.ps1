<#
.SYNOPSIS
    Lagar skjermbilete-collagen (docs\images\trollfoss-skjermbilete.png) til GitHub-sida.

.DESCRIPTION
    Les screenshots\gh\*.png (teke med adb frå debug-appen på emulatoren) og set ni scener saman i ei rute.
    Logo, banner og sosialbilete blir ikkje laga her, men av scripts\Render-Logos.ps1.
#>
Add-Type -AssemblyName System.Drawing
$root = Split-Path -Parent $PSScriptRoot
$out = Join-Path $root 'docs\images'
$shots = Join-Path $root 'screenshots\gh'
New-Item -ItemType Directory -Force $out | Out-Null

function Load($n) { [System.Drawing.Image]::FromFile((Join-Path $shots "$n.png")) }

function RoundedPath($x, $y, $w, $h) {
    $d = [Math]::Min(36, [Math]::Min($w, $h) / 4) * 2
    $p = New-Object System.Drawing.Drawing2D.GraphicsPath
    $p.AddArc($x, $y, $d, $d, 180, 90)
    $p.AddArc($x + $w - $d, $y, $d, $d, 270, 90)
    $p.AddArc($x + $w - $d, $y + $h - $d, $d, $d, 0, 90)
    $p.AddArc($x, $y + $h - $d, $d, $d, 90, 90)
    $p.CloseFigure()
    $p
}

$ink = [System.Drawing.Color]::FromArgb(255, 43, 33, 64)

# A whole screenshot in a rounded inked frame. The frame has the screenshot's own shape, so nothing is
# cropped or squeezed.
function Scene($name, $x, $y, $w, $h, $g) {
    $img = Load $name
    $src = New-Object System.Drawing.Rectangle 0, 0, $img.Width, $img.Height
    $p = RoundedPath $x $y $w $h
    $g.SetClip($p)
    $g.DrawImage($img, (New-Object System.Drawing.Rectangle $x, $y, $w, $h), $src, 'Pixel')
    $g.ResetClip()
    $pen = New-Object System.Drawing.Pen $ink, 5
    $g.DrawPath($pen, $p)
    $pen.Dispose(); $p.Dispose(); $img.Dispose()
}

$names = 'home', 'tivoli', 'underwater', 'farm', 'stage', 'forest-night', 'heileberget', 'space', 'map'
$cw = 640; $ch = 288; $gap = 18; $cols = 3; $rows = 3
$b = New-Object System.Drawing.Bitmap ($cols * $cw + ($cols + 1) * $gap), ($rows * $ch + ($rows + 1) * $gap)
$g = [System.Drawing.Graphics]::FromImage($b)
$g.SmoothingMode = 'AntiAlias'; $g.InterpolationMode = 'HighQualityBicubic'
$rect = New-Object System.Drawing.Rectangle 0, 0, $b.Width, $b.Height
$bg = New-Object System.Drawing.Drawing2D.LinearGradientBrush $rect, ([System.Drawing.Color]::FromArgb(255, 59, 42, 107)), ([System.Drawing.Color]::FromArgb(255, 31, 24, 64)), 90
$g.FillRectangle($bg, $rect)
$i = 0
foreach ($n in $names) {
    $x = $gap + ($i % $cols) * ($cw + $gap)
    $y = $gap + [Math]::Floor($i / $cols) * ($ch + $gap)
    Scene $n $x $y $cw $ch $g
    $i++
}
$b.Save((Join-Path $out 'trollfoss-skjermbilete.png'))
$g.Dispose(); $b.Dispose()

Get-ChildItem $out | Select-Object Name, Length
