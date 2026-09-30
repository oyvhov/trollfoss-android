<#
.SYNOPSIS
    Lagar logo, banner, sosialbilete og skjermbilete-collage til GitHub-sida frå skjermbilete på emulatoren.

.DESCRIPTION
    Les screenshots\gh\*.png (teke med adb, sjå docs\RELEASE_WORKFLOW.md) og skriv docs\images\logo.png,
    banner.png, trollfoss-sosial.png og trollfoss-skjermbilete.png. Kjelde til logoen er splash.png.
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
$sun = [System.Drawing.Color]::FromArgb(255, 255, 200, 61)

# A scene cut from a screenshot, without the buttons in the corners, in a rounded inked frame.
function Scene($name, $x, $y, $w, $h, $g, [switch]$whole) {
    $img = Load $name
    $src = if ($whole) { New-Object System.Drawing.Rectangle 0, 0, $img.Width, $img.Height } else { New-Object System.Drawing.Rectangle 0, 216, $img.Width, 648 }
    $p = RoundedPath $x $y $w $h
    $g.SetClip($p)
    $g.DrawImage($img, (New-Object System.Drawing.Rectangle $x, $y, $w, $h), $src, 'Pixel')
    $g.ResetClip()
    $pen = New-Object System.Drawing.Pen $ink, 5
    $g.DrawPath($pen, $p)
    $pen.Dispose(); $p.Dispose(); $img.Dispose()
}

function NewCanvas($w, $h) {
    $b = New-Object System.Drawing.Bitmap $w, $h
    $g = [System.Drawing.Graphics]::FromImage($b)
    $g.SmoothingMode = 'AntiAlias'; $g.InterpolationMode = 'HighQualityBicubic'; $g.TextRenderingHint = 'AntiAliasGridFit'
    ,@($b, $g)
}

function Background($g, $w, $h, $c1, $c2, $angle) {
    $rect = New-Object System.Drawing.Rectangle 0, 0, $w, $h
    $br = New-Object System.Drawing.Drawing2D.LinearGradientBrush $rect, $c1, $c2, $angle
    $g.FillRectangle($br, $rect)
}

function Glow($g, $x, $y, $w, $h) {
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $path.AddEllipse($x, $y, $w, $h)
    $pb = New-Object System.Drawing.Drawing2D.PathGradientBrush $path
    $pb.CenterColor = [System.Drawing.Color]::FromArgb(110, 255, 200, 61)
    $pb.SurroundColors = @([System.Drawing.Color]::FromArgb(0, 255, 200, 61))
    $g.FillPath($pb, $path)
}

function Title($g, $text, $x, $y, $size) {
    $f = New-Object System.Drawing.Font 'Segoe UI Black', $size, ([System.Drawing.FontStyle]::Bold), ([System.Drawing.GraphicsUnit]::Pixel)
    $g.DrawString($text, $f, (New-Object System.Drawing.SolidBrush $ink), ($x + 6), ($y + 6))
    $g.DrawString($text, $f, [System.Drawing.Brushes]::White, $x, $y)
}

function Text($g, $text, $x, $y, $size, $color, $font = 'Segoe UI Semibold') {
    $f = New-Object System.Drawing.Font $font, $size, ([System.Drawing.FontStyle]::Regular), ([System.Drawing.GraphicsUnit]::Pixel)
    $g.DrawString($text, $f, (New-Object System.Drawing.SolidBrush $color), $x, $y)
}

# ---- logo.png: the troll circle from the splash screen
$sp = Load 'splash'
$logo = New-Object System.Drawing.Bitmap 512, 512
$g = [System.Drawing.Graphics]::FromImage($logo)
$g.SmoothingMode = 'AntiAlias'; $g.InterpolationMode = 'HighQualityBicubic'; $g.Clear([System.Drawing.Color]::Transparent)
$clip = New-Object System.Drawing.Drawing2D.GraphicsPath; $clip.AddEllipse(2, 2, 508, 508)
$g.SetClip($clip)
$g.DrawImage($sp, (New-Object System.Drawing.Rectangle 0, 0, 512, 512), (New-Object System.Drawing.Rectangle 988, 328, 424, 424), 'Pixel')
$g.ResetClip()
$logo.Save((Join-Path $out 'logo.png'))
$g.Dispose(); $sp.Dispose(); $logo.Dispose()

# ---- banner.png 1600 x 420
$c = NewCanvas 1600 420; $bn = $c[0]; $g = $c[1]
Background $g 1600 420 ([System.Drawing.Color]::FromArgb(255, 64, 44, 118)) ([System.Drawing.Color]::FromArgb(255, 28, 22, 60)) 35
Glow $g -40 -60 560 560
Scene 'tivoli' 860 80 330 250 $g
Scene 'underwater' 1060 40 330 250 $g
Scene 'stage' 1240 130 330 250 $g
$lg = [System.Drawing.Image]::FromFile((Join-Path $out 'logo.png')); $g.DrawImage($lg, 50, 70, 280, 280); $lg.Dispose()
Title $g 'Trollfoss' 350 80 104
Text $g 'Ei bygd under ein stor foss' 356 214 30 ([System.Drawing.Color]::White)
Text $g 'for barn 4–10 år · Android' 356 256 30 $sun
$bn.Save((Join-Path $out 'banner.png')); $g.Dispose(); $bn.Dispose()

# ---- trollfoss-sosial.png 1280 x 640 (GitHub social preview)
$c = NewCanvas 1280 640; $sc = $c[0]; $g = $c[1]
Background $g 1280 640 ([System.Drawing.Color]::FromArgb(255, 70, 48, 128)) ([System.Drawing.Color]::FromArgb(255, 26, 20, 56)) 40
Glow $g 20 20 560 600
Scene 'home' 620 70 560 330 $g
Scene 'forest-night' 700 290 520 300 $g
$lg = [System.Drawing.Image]::FromFile((Join-Path $out 'logo.png')); $g.DrawImage($lg, 70, 70, 250, 250); $lg.Dispose()
Title $g 'Trollfoss' 50 324 112
Text $g 'Ei bygd under ein stor foss' 54 462 34 $sun
Text $g "14 stader · humor · ønskjebobler · heimedesign`nutan reklame og konto" 54 514 27 ([System.Drawing.Color]::White) 'Segoe UI'
$sc.Save((Join-Path $out 'trollfoss-sosial.png')); $g.Dispose(); $sc.Dispose()

# ---- trollfoss-skjermbilete.png: nine scenes
$names = 'home', 'tivoli', 'underwater', 'farm', 'stage', 'forest-night', 'doctor', 'space', 'mountain'
$cw = 640; $ch = 288; $gap = 18; $cols = 3; $rows = 3
$c = NewCanvas ($cols * $cw + ($cols + 1) * $gap) ($rows * $ch + ($rows + 1) * $gap); $bmp = $c[0]; $g = $c[1]
Background $g $bmp.Width $bmp.Height ([System.Drawing.Color]::FromArgb(255, 59, 42, 107)) ([System.Drawing.Color]::FromArgb(255, 31, 24, 64)) 90
$i = 0
foreach ($n in $names) {
    $x = $gap + ($i % $cols) * ($cw + $gap)
    $y = $gap + [Math]::Floor($i / $cols) * ($ch + $gap)
    Scene $n $x $y $cw $ch $g -whole
    $i++
}
$bmp.Save((Join-Path $out 'trollfoss-skjermbilete.png')); $g.Dispose(); $bmp.Dispose()

Get-ChildItem $out | Select-Object Name, Length
