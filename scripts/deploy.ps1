# Cross-machine deploy for webshooter.
# Builds + installs the app on the Windows-local Android emulator and the
# Mac-remote iOS simulator in parallel. Runs only on Windows (PowerShell 7+).
#
# Usage:
#   pwsh -File scripts/deploy.ps1                 # both targets, staging
#   pwsh -File scripts/deploy.ps1 prod            # both targets, prod
#   pwsh -File scripts/deploy.ps1 ios             # iOS only, staging
#   pwsh -File scripts/deploy.ps1 android prod    # Android only, prod
#   pwsh -File scripts/deploy.ps1 -ForceWip       # auto-commit dirty Windows tree

param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Words = @(),
    [switch]$ForceWip
)

$ErrorActionPreference = "Stop"

# ---- Constants (edit if either machine moves the repo) ----
$WindowsRepo    = "D:\source\webshooter"
$MacRepo        = "/Users/carlemil/source/webshooter"
$MacHost        = "macmini"
$BootTimeoutSec = 120

# ---- Argument parsing ----
$Flavor  = "staging"
$Targets = [ordered]@{ android = $true; ios = $true }
$explicitTarget = $false

foreach ($w in $Words) {
    switch ($w.ToLower()) {
        "prod"    { $Flavor = "prod" }
        "staging" { $Flavor = "staging" }
        "ios" {
            if (-not $explicitTarget) { $Targets.android = $false; $explicitTarget = $true }
            $Targets.ios = $true
        }
        "android" {
            if (-not $explicitTarget) { $Targets.ios = $false; $explicitTarget = $true }
            $Targets.android = $true
        }
        default { Write-Error "Unknown arg: $w (expected: ios, android, prod, staging)"; exit 1 }
    }
}

# ---- Logging helpers ----
function Write-Step($msg)  { Write-Host "==> $msg" -ForegroundColor Yellow }
function Write-Ok($msg)    { Write-Host "    $msg" -ForegroundColor DarkGray }
function Write-Fail($msg)  { Write-Host "!!! $msg" -ForegroundColor Red }

# ---- Phase 1: Preflight ----

if (-not $IsWindows) { Write-Fail "deploy.ps1 only runs on Windows. Run it from your Windows machine."; exit 1 }
if (-not ($Targets.android -or $Targets.ios)) { Write-Fail "No targets selected."; exit 1 }

$selected = @()
if ($Targets.android) { $selected += "android" }
if ($Targets.ios)     { $selected += "ios" }
Write-Step "Targets: $($selected -join ', ')  Flavor: $Flavor"

Set-Location $WindowsRepo

$branch = (& git rev-parse --abbrev-ref HEAD).Trim()
if ($branch -ne "main") { Write-Fail "Windows on branch '$branch', expected main."; exit 5 }
Write-Ok "Windows on main"

$dirty = & git status --porcelain
if ($dirty) {
    if (-not $ForceWip) {
        Write-Fail "Uncommitted Windows changes. Commit them or rerun with -ForceWip."
        Write-Host $dirty
        exit 6
    }
    Write-Step "Auto-committing WIP (--ForceWip)"
    & git add -A
    & git commit -m "wip: deploy $(Get-Date -Format o)" | Out-Null
}

if ($Targets.android) {
    Write-Step "Android preflight"
    $adbOut = & adb devices 2>&1
    $deviceLines = @($adbOut | Select-Object -Skip 1 | Where-Object { $_ -match "\tdevice$" })

    if ($deviceLines.Count -eq 0) {
        Write-Step "No Android device connected; trying to auto-boot an AVD"
        $avds = @(& emulator -list-avds 2>$null | Where-Object { $_ })
        if ($avds.Count -eq 0) {
            Write-Fail "No Android emulator running and no AVD defined. Create one in Android Studio AVD Manager and retry."
            exit 2
        }
        $avd = $avds[0]
        Write-Ok "Booting AVD: $avd"
        Start-Process emulator -ArgumentList "-avd",$avd,"-no-snapshot-save" -WindowStyle Hidden | Out-Null

        $deadline = (Get-Date).AddSeconds($BootTimeoutSec)
        & adb wait-for-device | Out-Null
        $booted = $false
        while ((Get-Date) -lt $deadline) {
            $prop = (& adb shell getprop sys.boot_completed 2>$null) -replace "\s",""
            if ($prop -eq "1") { $booted = $true; break }
            Start-Sleep -Seconds 2
        }
        if (-not $booted) { Write-Fail "Emulator boot timed out ($BootTimeoutSec s)."; exit 2 }
        Write-Ok "Emulator booted"
    } elseif ($deviceLines.Count -gt 1) {
        Write-Fail "Multiple Android devices connected; disconnect or stop extras."
        $adbOut | ForEach-Object { Write-Host $_ }
        exit 2
    } else {
        Write-Ok "Android device ready"
    }
}

if ($Targets.ios) {
    Write-Step "iOS preflight"

    & ssh -o BatchMode=yes -o ConnectTimeout=5 $MacHost 'echo ok' 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Fail "Cannot reach $MacHost over SSH. Check Windows ~/.ssh/config and Mac Remote Login."
        exit 4
    }
    Write-Ok "SSH to $MacHost ok"

    $macBranch = (& ssh $MacHost "cd '$MacRepo' && git rev-parse --abbrev-ref HEAD").Trim()
    if ($macBranch -ne "main") {
        Write-Fail "Mac on branch '$macBranch', expected main. Reconcile on the Mac and rerun."
        exit 7
    }
    Write-Ok "Mac on main"

    $simScript = @'
set -e
BOOTED=$(xcrun simctl list devices booted 2>/dev/null | grep -c "Booted" || true)
if [ "$BOOTED" -eq 0 ]; then
  echo "[boot] no simulator booted, picking first available"
  UDID=$(xcrun simctl list devices available -j | jq -r '[.devices[][] | select(.isAvailable) | .udid] | .[0]')
  if [ -z "$UDID" ] || [ "$UDID" = "null" ]; then
    echo "ERR_NO_SIM"
    exit 3
  fi
  xcrun simctl boot "$UDID"
  open -a Simulator
  xcrun simctl bootstatus "$UDID" -b
  echo "[boot] simulator $UDID ready"
fi
'@
    $simOut = $simScript | & ssh $MacHost 'tr -d "\r" | bash -s' 2>&1
    if ($LASTEXITCODE -ne 0) {
        if ($simOut -match "ERR_NO_SIM") {
            Write-Fail "No iOS simulator runtime available on the Mac. Install one via Xcode -> Settings -> Platforms."
        } else {
            Write-Fail "iOS simulator preflight failed:"
            $simOut | ForEach-Object { Write-Host $_ }
        }
        exit 3
    }
    $simOut | ForEach-Object { Write-Ok $_ }
}

# ---- Phase 2: Push Windows -> origin ----

Write-Step "Pushing to origin"
& git push origin HEAD
if ($LASTEXITCODE -ne 0) { Write-Fail "git push failed."; exit 8 }

# ---- Phase 3: Parallel build + install ----

$logDir = Join-Path $WindowsRepo "build\deploy"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$androidStatus = Join-Path $logDir "android.status"
$iosStatus     = Join-Path $logDir "ios.status"
Remove-Item -Force -ErrorAction SilentlyContinue $androidStatus, $iosStatus

$androidJob = $null
$iosJob     = $null

if ($Targets.android) {
    $androidJob = Start-Job -Name android -ScriptBlock {
        param($Repo, $Flavor, $StatusFile, $LogFile)
        $start = Get-Date
        $exitCode = 0
        try {
            Set-Location $Repo
            $task    = if ($Flavor -eq "prod") { ":app:installProdDebug" } else { ":app:installStagingDebug" }
            $pkg     = if ($Flavor -eq "prod") { "se.kjellstrand.webshooter" }    else { "se.kjellstrand.webshooter.staging" }

            Write-Output "Running: ./gradlew $task --no-daemon"
            & .\gradlew $task --no-daemon 2>&1 | Tee-Object -FilePath $LogFile -Append | ForEach-Object { Write-Output $_ }
            if ($LASTEXITCODE -ne 0) { throw "gradle install failed (exit $LASTEXITCODE)" }

            Write-Output "Launching $pkg"
            & adb shell monkey -p $pkg -c android.intent.category.LAUNCHER 1 2>&1 |
                Tee-Object -FilePath $LogFile -Append | ForEach-Object { Write-Output $_ }
            if ($LASTEXITCODE -ne 0) { throw "adb launch failed (exit $LASTEXITCODE)" }
        } catch {
            $exitCode = 1
            Write-Output "ERROR: $_"
        }
        $dur = [int]((Get-Date) - $start).TotalSeconds
        $status = if ($exitCode -eq 0) { "PASS" } else { "FAIL" }
        "$status,$dur" | Out-File -Encoding ASCII $StatusFile
        exit $exitCode
    } -ArgumentList $WindowsRepo, $Flavor, $androidStatus, (Join-Path $logDir "android.log")
}

if ($Targets.ios) {
    $iosScript = @'
set -e
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
export PATH=$JAVA_HOME/bin:/opt/homebrew/bin:$PATH

FLAVOR=$1
MAC_REPO=$2
case "$FLAVOR" in
  prod)    SCHEME=Prod;    CONFIG=Debug-Prod ;;
  staging) SCHEME=Staging; CONFIG=Debug-Staging ;;
  *)       echo "Unknown flavor: $FLAVOR"; exit 99 ;;
esac

cd "$MAC_REPO"
STASHED=0
if [ -n "$(git status --porcelain)" ]; then
  git stash push -u -m "deploy-autostash-$(date +%s)" && STASHED=1
fi

cleanup() {
  if [ $STASHED -eq 1 ]; then
    if ! git stash pop 2>/dev/null; then
      echo "STASH_POP_CONFLICT"
    fi
  fi
}
trap cleanup EXIT

git pull --ff-only origin main
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 --no-daemon
(cd iosApp && xcodegen generate)
xcodebuild -project iosApp/iosApp.xcodeproj \
  -scheme "$SCHEME" -configuration "$CONFIG" \
  -destination 'platform=iOS Simulator,name=iPhone 17' build

APP=$(ls -td ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/${CONFIG}-iphonesimulator/iosApp.app 2>/dev/null | head -1)
if [ -z "$APP" ]; then echo "No iosApp.app found in DerivedData"; exit 14; fi
xcrun simctl install booted "$APP"
xcrun simctl terminate booted se.kjellstrand.webshooter 2>/dev/null || true
xcrun simctl launch booted se.kjellstrand.webshooter
'@

    $iosJob = Start-Job -Name ios -ScriptBlock {
        param($Script, $Flavor, $MacRepo, $MacHost, $StatusFile, $LogFile)
        $start = Get-Date
        $exitCode = 0
        $stashConflict = $false
        try {
            $out = $Script | & ssh $MacHost 'tr -d "\r" | bash -s' -- $Flavor $MacRepo 2>&1
            $sshExit = $LASTEXITCODE
            $out | Tee-Object -FilePath $LogFile -Append | ForEach-Object { Write-Output $_ }
            if ($out -match "STASH_POP_CONFLICT") { $stashConflict = $true }
            if ($sshExit -ne 0) { throw "iOS pipeline failed (ssh exit $sshExit)" }
        } catch {
            $exitCode = 1
            Write-Output "ERROR: $_"
        }
        $dur = [int]((Get-Date) - $start).TotalSeconds
        $status = if ($exitCode -ne 0) { "FAIL" } elseif ($stashConflict) { "PASS_STASH_CONFLICT" } else { "PASS" }
        "$status,$dur" | Out-File -Encoding ASCII $StatusFile
        exit $exitCode
    } -ArgumentList $iosScript, $Flavor, $MacRepo, $MacHost, $iosStatus, (Join-Path $logDir "ios.log")
}

# ---- Stream output ----
$jobs = @()
if ($androidJob) { $jobs += [pscustomobject]@{ Name="ANDROID"; Color="Green"; Job=$androidJob } }
if ($iosJob)     { $jobs += [pscustomobject]@{ Name="IOS";     Color="Cyan";  Job=$iosJob } }

Write-Step "Building ($($jobs.Count) target(s) in parallel)..."

while ($jobs.Job | Where-Object { $_.State -eq "Running" }) {
    foreach ($j in $jobs) {
        if ($j.Job.HasMoreData) {
            Receive-Job $j.Job 2>&1 | ForEach-Object {
                Write-Host "[$($j.Name)] $_" -ForegroundColor $j.Color
            }
        }
    }
    Start-Sleep -Milliseconds 200
}
foreach ($j in $jobs) {
    if ($j.Job.HasMoreData) {
        Receive-Job $j.Job 2>&1 | ForEach-Object {
            Write-Host "[$($j.Name)] $_" -ForegroundColor $j.Color
        }
    }
}

# ---- Phase 4: Summary ----

function Read-Status($file) {
    if (-not (Test-Path $file)) { return @{ Status="UNKNOWN"; Duration=0 } }
    $line = (Get-Content $file -Raw).Trim()
    $parts = $line.Split(",")
    return @{ Status=$parts[0]; Duration=[int]$parts[1] }
}

Write-Host ""
Write-Host "============================" -ForegroundColor Yellow
$allPass = $true
$stashWarning = $false
foreach ($name in $selected) {
    $statusFile = if ($name -eq "android") { $androidStatus } else { $iosStatus }
    $r = Read-Status $statusFile
    $label = $name.ToUpper().PadRight(8)
    if ($r.Status -eq "PASS") {
        Write-Host ("{0}: PASS ({1}s)" -f $label, $r.Duration) -ForegroundColor Green
    } elseif ($r.Status -eq "PASS_STASH_CONFLICT") {
        Write-Host ("{0}: PASS ({1}s)  WARNING: Mac stash pop conflicted." -f $label, $r.Duration) -ForegroundColor Yellow
        $stashWarning = $true
    } else {
        Write-Host ("{0}: FAIL ({1}s)" -f $label, $r.Duration) -ForegroundColor Red
        $allPass = $false
        $log = if ($name -eq "android") { Join-Path $logDir "android.log" } else { Join-Path $logDir "ios.log" }
        if (Test-Path $log) {
            Write-Host "--- last 40 lines of $log ---" -ForegroundColor DarkGray
            Get-Content $log -Tail 40 | ForEach-Object { Write-Host $_ }
        }
    }
}
Write-Host ""
Write-Host "Targets=$($selected -join ',')  Flavor=$Flavor" -ForegroundColor DarkGray
if ($stashWarning) {
    Write-Host "Mac stash preserved; run: ssh $MacHost ""cd '$MacRepo' && git stash list""" -ForegroundColor Yellow
}

# Clean up jobs
foreach ($j in $jobs) { Remove-Job $j.Job -Force | Out-Null }

if (-not $allPass) { exit 1 }
if ($stashWarning) { exit 20 }
exit 0
