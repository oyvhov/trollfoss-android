<#
.SYNOPSIS
    Teiknar logo, ordmerke, banner og sosialbilete for GitHub-sida med spelet sin eigen teikne-kode.

.DESCRIPTION
    Grafikken ligg som debug-kode i app/src/debug (LogoActivity + logo/*.kt) og kjem ikkje med i
    release-APK-en. Skriptet bygg debug-APK-en (pakkenamn app.trollfoss.logo, så han ikkje kolliderer med
    app.trollfoss.debug på den delte emulatoren), installerer han på nettbrettemulatoren (emulator-5562 i
    WSL) og lèt aktiviteten teikne kvar figur med dei same teikne-funksjonane som spelet.

    Standard: aktiviteten teiknar på ei gjennomsiktig bitmap i eksakt pikselmål og skriv ei PNG som
    skriptet hentar. Med -Screencap blir i staden emulatorskjermen sett til nøyaktig pikselmål
    (wm size), og aktiviteten teiknar full skjerm. Gjennomsiktige bilete blir då teikna to gonger, på
    svart og på kvit botn, og alfa blir rekna ut frå skilnaden:
        alfa = 1 - (kvit - svart) / 255,   farge = svart / alfa
    Skjermmålet blir då alltid nullstilt til slutt (wm size reset, wm density reset).

    Alt blir teikna i dobbel storleik og skalert ned, så kantane blir glatte. Skriptet rører berre dei fire
    biletfilene under; trollfoss-skjermbilete.png og Make-GitHubImages.ps1 blir ikkje rørt.

    Filer i docs\images:  logo.png (1024x1024), logo-wordmark.png (ca. 2400 brei), banner.png (2400x600),
    trollfoss-sosial.png (1280x640).

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\Render-Logos.ps1
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\Render-Logos.ps1 -Art emblem,wordmark -SkipBuild
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\Render-Logos.ps1 -Screencap
#>
param(
    [string[]]$Art = @('emblem', 'wordmark', 'banner', 'social'),
    [switch]$SkipBuild,
    [switch]$Screencap,
    [string]$Serial = 'emulator-5562',
    [string]$OutDir,
    [int]$WaitSeconds = 7,
    [string]$GradleHome = $(if (Test-Path 'C:\JellyBin\.gradle-home') { 'C:\JellyBin\.gradle-home' } else { 'C:\topa\.gradle-home' })
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

# -Art kan vere ei kommaliste òg når skriptet blir starta med -File.
$Art = @($Art | ForEach-Object { $_ -split ',' } | Where-Object { $_ })
foreach ($name in $Art) {
    if ($name -notin @('emblem', 'wordmark', 'banner', 'social', 'icon')) { throw "Ukjend figur: $name (emblem, wordmark, banner eller social)" }
}

$root = Split-Path -Parent $PSScriptRoot
if (-not $OutDir) { $OutDir = Join-Path $root 'docs\images' }
$shots = Join-Path $root 'screenshots\logo'
New-Item -ItemType Directory -Force $OutDir | Out-Null
New-Item -ItemType Directory -Force $shots | Out-Null

$adbPath = '/home/oyvhov/Android/Sdk/platform-tools/adb'

function ToWsl([string]$path) {
    $p = $path -replace '\\', '/'
    '/mnt/' + $p.Substring(0, 1).ToLower() + $p.Substring(2)
}

function Adb([string]$command) {
    & wsl.exe -d Ubuntu -u root --exec bash -c "$adbPath -s $Serial $command"
    if ($LASTEXITCODE -ne 0) { throw "adb $command feila ($LASTEXITCODE)" }
}

function AdbLog([string]$filter) {
    $log = & wsl.exe -d Ubuntu -u root --exec bash -c "$adbPath -s $Serial logcat -d $filter"
    $log -join ' '
}

Add-Type -ReferencedAssemblies System.Drawing -TypeDefinition @'
using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.Runtime.InteropServices;

public static class LogoPixels
{
    static byte[] Read(Bitmap bmp, out int stride)
    {
        Rectangle r = new Rectangle(0, 0, bmp.Width, bmp.Height);
        BitmapData d = bmp.LockBits(r, ImageLockMode.ReadOnly, PixelFormat.Format32bppArgb);
        stride = d.Stride;
        byte[] bytes = new byte[Math.Abs(d.Stride) * bmp.Height];
        Marshal.Copy(d.Scan0, bytes, 0, bytes.Length);
        bmp.UnlockBits(d);
        return bytes;
    }

    static Bitmap Write(byte[] bytes, int w, int h)
    {
        Bitmap res = new Bitmap(w, h, PixelFormat.Format32bppArgb);
        Rectangle r = new Rectangle(0, 0, w, h);
        BitmapData d = res.LockBits(r, ImageLockMode.WriteOnly, PixelFormat.Format32bppArgb);
        Marshal.Copy(bytes, 0, d.Scan0, bytes.Length);
        res.UnlockBits(d);
        return res;
    }

    static byte Clamp(double v)
    {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return (byte)Math.Round(v);
    }

    // The same art drawn on black and on white gives the true colour and alpha of every pixel.
    public static Bitmap Recover(Bitmap black, Bitmap white)
    {
        int w = black.Width, h = black.Height, s1, s2;
        byte[] b = Read(black, out s1);
        byte[] wh = Read(white, out s2);
        byte[] o = new byte[b.Length];
        for (int i = 0; i < b.Length; i += 4)
        {
            double d = ((wh[i] - b[i]) + (wh[i + 1] - b[i + 1]) + (wh[i + 2] - b[i + 2])) / 3.0;
            double a = 1.0 - d / 255.0;
            if (a < 0) a = 0;
            if (a > 1) a = 1;
            if (a < 0.004) { o[i] = 0; o[i + 1] = 0; o[i + 2] = 0; o[i + 3] = 0; continue; }
            o[i] = Clamp(b[i] / a);
            o[i + 1] = Clamp(b[i + 1] / a);
            o[i + 2] = Clamp(b[i + 2] / a);
            o[i + 3] = Clamp(a * 255.0);
        }
        return Write(o, w, h);
    }

    // Exactly half the size: the mean of every 2 x 2 block, with the colour weighted by alpha.
    public static Bitmap Half(Bitmap src)
    {
        int w = src.Width / 2, h = src.Height / 2, stride;
        byte[] s = Read(src, out stride);
        byte[] o = new byte[w * h * 4];
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                double a = 0, r = 0, g = 0, bl = 0;
                for (int k = 0; k < 4; k++)
                {
                    int i = (2 * y + (k >> 1)) * stride + (2 * x + (k & 1)) * 4;
                    double al = s[i + 3];
                    a += al;
                    bl += s[i] * al;
                    g += s[i + 1] * al;
                    r += s[i + 2] * al;
                }
                int j = (y * w + x) * 4;
                if (a > 0)
                {
                    o[j] = Clamp(bl / a);
                    o[j + 1] = Clamp(g / a);
                    o[j + 2] = Clamp(r / a);
                    o[j + 3] = Clamp(a / 4.0);
                }
            }
        }
        return Write(o, w, h);
    }

    // The box around everything that is not transparent.
    public static Rectangle Bounds(Bitmap src, int alphaMin)
    {
        int stride;
        byte[] s = Read(src, out stride);
        int minX = src.Width, minY = src.Height, maxX = -1, maxY = -1;
        for (int y = 0; y < src.Height; y++)
        {
            for (int x = 0; x < src.Width; x++)
            {
                if (s[y * stride + x * 4 + 3] >= alphaMin)
                {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < 0) return new Rectangle(0, 0, src.Width, src.Height);
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    public static Bitmap Crop(Bitmap src, Rectangle area, int pad)
    {
        Bitmap res = new Bitmap(area.Width + 2 * pad, area.Height + 2 * pad, PixelFormat.Format32bppArgb);
        using (Graphics g = Graphics.FromImage(res))
        {
            g.Clear(Color.Transparent);
            g.DrawImage(src, new Rectangle(pad, pad, area.Width, area.Height), area, GraphicsUnit.Pixel);
        }
        return res;
    }
}
'@

# What to draw: the pixel size to render at (double the final size), whether it has transparency, and
# where the result goes. The wordmark is cropped to its letters.
$jobs = @{
    emblem   = @{ W = 2048; H = 2048; Transparent = $true; File = 'logo.png' }
    wordmark = @{ W = 4800; H = 1800; Transparent = $true; File = 'logo-wordmark.png'; Crop = $true }
    banner   = @{ W = 4800; H = 1200; Transparent = $false; File = 'banner.png' }
    social   = @{ W = 2560; H = 1280; Transparent = $false; File = 'trollfoss-sosial.png' }
    icon     = @{ W = 1960; H = 760; Transparent = $false; File = 'launcher-icon-sheet.png'; Preview = $true }
}

# Default: the activity draws on a transparent bitmap and writes a PNG in its private folder; fetch it with run-as.
function Fetch([string]$art, $job) {
    $file = Join-Path $shots "$art.png"
    if (Test-Path $file) { Remove-Item $file }
    Adb "logcat -c"
    Adb "shell am start -S -W -n $activity --es mode file --es art $art --es bg none --ei w $($job.W) --ei h $($job.H)" | Out-Null
    $done = $false
    for ($i = 0; $i -lt 240 -and -not $done; $i++) {
        Start-Sleep -Milliseconds 500
        $text = AdbLog '-s LogoArt:I'
        if ($text -match "failed $art") { throw "${art}: teikninga feila, sjå logcat (LogoArt)" }
        if ($text -match "wrote $art") { $done = $true }
    }
    if (-not $done) { throw "${art}: aktiviteten blei aldri ferdig" }
    Adb "exec-out run-as $package cat files/$art.png > $(ToWsl $file)"
    $img = New-Object System.Drawing.Bitmap $file
    $same = ($img.Width -eq $job.W -and $img.Height -eq $job.H)
    $dims = "$($img.Width)x$($img.Height)"
    $img.Dispose()
    if (-not $same) { throw "${art}: biletet er $dims, venta $($job.W)x$($job.H)" }
    $file
}

# -Screencap: the activity fills the display, which is set to the exact pixel size; capture it.
function Capture([string]$art, [string]$bg, $job) {
    $file = Join-Path $shots "$art-$bg.png"
    if (Test-Path $file) { Remove-Item $file }
    Adb "logcat -c"
    Adb "shell am start -S -W -n $activity --es art $art --es bg $bg" | Out-Null
    # Wait until the activity says it has drawn, then until two screenshots in a row are identical.
    $drawn = $false
    for ($i = 0; $i -lt 120 -and -not $drawn; $i++) {
        Start-Sleep -Milliseconds 500
        if ((AdbLog '-s LogoArt:I') -match "drawn $art $bg") { $drawn = $true }
    }
    if (-not $drawn) { throw "$art ($bg): aktiviteten melde aldri at ho var ferdig" }
    $previous = ''
    for ($try = 0; $try -lt 12; $try++) {
        Start-Sleep -Milliseconds ($WaitSeconds * 150)
        Adb "exec-out screencap -p > $(ToWsl $file)"
        $hash = (Get-FileHash $file -Algorithm MD5).Hash
        if ($hash -eq $previous) { break }
        $previous = $hash
    }
    $img = New-Object System.Drawing.Bitmap $file
    $same = ($img.Width -eq $job.W -and $img.Height -eq $job.H)
    $dims = "$($img.Width)x$($img.Height)"
    $img.Dispose()
    if (-not $same) { throw "$art ($bg): skjermbiletet er $dims, venta $($job.W)x$($job.H)" }
    $file
}

function Save-Png([System.Drawing.Bitmap]$bmp, [string]$path) {
    if (Test-Path $path) { Remove-Item $path }
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
}

# Opaque art is shrunk with bicubic filtering; art with alpha with a premultiplied box filter.
function Shrink-Opaque([System.Drawing.Bitmap]$src, [int]$w, [int]$h) {
    $dst = New-Object System.Drawing.Bitmap $w, $h, ([System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
    $g = [System.Drawing.Graphics]::FromImage($dst)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.DrawImage($src, (New-Object System.Drawing.Rectangle 0, 0, $w, $h), 0, 0, $src.Width, $src.Height, [System.Drawing.GraphicsUnit]::Pixel)
    $g.Dispose()
    $dst
}

if (-not $SkipBuild) {
    $temp = Join-Path $root '.gradle-tmp'
    New-Item -ItemType Directory -Force $temp | Out-Null
    $env:TEMP = $temp
    $env:TMP = $temp
    Push-Location $root
    try {
        & .\gradlew.bat --gradle-user-home $GradleHome --init-script (Join-Path $PSScriptRoot 'logo-package.init.gradle') ':app:assembleDebug' --console=plain -q
        if ($LASTEXITCODE -ne 0) { throw "Gradle feila med kode $LASTEXITCODE" }
    } finally {
        Pop-Location
    }
}

$apk = Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
if (-not (Test-Path $apk)) { throw "Fann ikkje $apk. Køyr utan -SkipBuild." }
# Debug-APK-en for logoteikninga har pakkenamnet app.trollfoss.logo (sjå logo-package.init.gradle), så han
# ikkje kolliderer med app.trollfoss.debug på den delte emulatoren. Pakkenamnet står i output-metadata.json.
$meta = Get-Content (Join-Path $root 'app\build\outputs\apk\debug\output-metadata.json') -Raw | ConvertFrom-Json
$package = $meta.applicationId
$activity = "$package/app.trollfoss.LogoActivity"

try {
    Write-Host "Installerer $package på $Serial ..."
    Adb "install -r $(ToWsl $apk)" | Out-Null
    if ($Screencap) {
        Adb "shell input keyevent KEYCODE_WAKEUP"
        Adb "shell wm dismiss-keyguard"
    }
    foreach ($name in $Art) {
        $job = $jobs[$name]
        Write-Host "Teiknar $name ($($job.W)x$($job.H)) ..."
        $out = Join-Path $OutDir $job.File
        if ($job.Preview) { $out = Join-Path $shots $job.File }
        $half = $null
        if (-not $Screencap) {
            $file = Fetch $name $job
            $full = New-Object System.Drawing.Bitmap $file
            if ($job.Preview) { $half = New-Object System.Drawing.Bitmap $full } elseif ($job.Transparent) { $half = [LogoPixels]::Half($full) } else { $half = Shrink-Opaque $full ($job.W / 2) ($job.H / 2) }
            $full.Dispose()
        } elseif ($job.Transparent) {
            Adb "shell wm size $($job.W)x$($job.H)"
            Adb "shell wm density 420"
            Start-Sleep -Seconds 1
            $blackFile = Capture $name 'black' $job
            $whiteFile = Capture $name 'white' $job
            $black = New-Object System.Drawing.Bitmap $blackFile
            $white = New-Object System.Drawing.Bitmap $whiteFile
            $full = [LogoPixels]::Recover($black, $white)
            $black.Dispose()
            $white.Dispose()
            $half = [LogoPixels]::Half($full)
            $full.Dispose()
        } else {
            Adb "shell wm size $($job.W)x$($job.H)"
            Adb "shell wm density 420"
            Start-Sleep -Seconds 1
            $file = Capture $name 'none' $job
            $src = New-Object System.Drawing.Bitmap $file
            $half = Shrink-Opaque $src ($job.W / 2) ($job.H / 2)
            $src.Dispose()
        }
        if ($job.Crop) {
            $box = [LogoPixels]::Bounds($half, 8)
            $cropped = [LogoPixels]::Crop($half, $box, 40)
            $half.Dispose()
            $half = $cropped
        }
        Save-Png $half $out
        $half.Dispose()
        $kb = [math]::Round((Get-Item $out).Length / 1KB)
        Write-Host "  -> $out ($kb KB)"
    }
} finally {
    if ($Screencap) {
        & wsl.exe -d Ubuntu -u root --exec bash -c "$adbPath -s $Serial shell wm size reset; $adbPath -s $Serial shell wm density reset; $adbPath -s $Serial shell input keyevent KEYCODE_HOME" | Out-Null
        Write-Host 'Skjermmål nullstilt.'
    }
}
