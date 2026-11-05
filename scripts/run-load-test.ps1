param(
    [int]$Users = 100,
    [int]$Concurrency = 10,
    [string]$Phases = 'register,refresh,logout',
    [string]$BaseUrl = 'http://localhost:8080',
    [int]$WarmupSeconds = 3,
    [string]$OutputPath = 'target/load-test-summary.json',
    [string]$MonitorOutput = 'target/load-test-monitor.jsonl',
    [string]$RunLog = 'target/load-test-run.log'
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location $root

$targetDirectory = Join-Path $root 'target'
New-Item -ItemType Directory -Path $targetDirectory -Force | Out-Null

try {
    $health = Invoke-WebRequest -Uri 'http://localhost:8081/actuator/health' -UseBasicParsing -TimeoutSec 5
} catch {
    throw "Application health check failed: $($_.Exception.Message)"
}
if ($health.StatusCode -ne 200) {
    throw "Application is not healthy (actuator status $($health.StatusCode))"
}

$stopFile = Join-Path $targetDirectory 'load-test-monitor.stop'
$monitorScript = Join-Path $root 'scripts\monitor-load.ps1'
$nodeScript = Join-Path $root 'scripts\load-test.mjs'
Remove-Item -LiteralPath $stopFile -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $MonitorOutput, $RunLog -ErrorAction SilentlyContinue

$monitorJob = Start-Job -ScriptBlock {
    param($script, $output, $stop)
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $script `
        -OutputPath $output -IntervalSeconds 2 -StopFile $stop
} -ArgumentList $monitorScript, (Join-Path $root $MonitorOutput), $stopFile

$exitCode = 1
try {
    Start-Sleep -Seconds $WarmupSeconds
    $previousErrorAction = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    & node $nodeScript --users $Users --concurrency $Concurrency --phases $Phases --base-url $BaseUrl --output $OutputPath *>> $RunLog
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = $previousErrorAction
} finally {
    New-Item -ItemType File -Path $stopFile -Force | Out-Null
    Wait-Job $monitorJob -Timeout 30 | Out-Null
    Receive-Job $monitorJob *>> $RunLog
    Stop-Job $monitorJob -ErrorAction SilentlyContinue
    Remove-Job $monitorJob -Force -ErrorAction SilentlyContinue
}

"loadExitCode=$exitCode"
