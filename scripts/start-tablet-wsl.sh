#!/usr/bin/env bash
# Startar Spole-testemulatoren (Spole_Instrumentation, port 5562) i WSL med nettbrettmål
# 1920 x 1200 / 240 dpi, same oppsett som Spole brukar for nettbrettkontroll, og installerer
# Trollfoss debug-APK. Rør aldri review-emulatoren med ekte kontoar (5560).
set -euo pipefail
SDK=/home/oyvhov/Android/Sdk
ADB="$SDK/platform-tools/adb"
PORT=5562
SERIAL="emulator-$PORT"
export ANDROID_AVD_HOME=/mnt/c/JellyBin/.spole-test-avds
mkdir -p -m 700 /run/user/0
# The emulator itself is started from Windows as a background wsl.exe process (see
# Start-TrollfossTablet.ps1); a process started here would die with this WSL session.
for attempt in $(seq 1 150); do
  if [ "$("$ADB" -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; then break; fi
  sleep 2
done
"$ADB" -s "$SERIAL" shell wm size 1920x1200
"$ADB" -s "$SERIAL" shell wm density 240
"$ADB" -s "$SERIAL" shell input keyevent KEYCODE_WAKEUP
"$ADB" -s "$SERIAL" shell wm dismiss-keyguard
if [ "${1:-}" != "--no-install" ]; then
  "$ADB" -s "$SERIAL" install -r /mnt/c/topa/app/build/outputs/apk/debug/app-debug.apk
fi
echo "Nettbrett klart: $SERIAL"
