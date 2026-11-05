param(
    [string]$OutputPath = (Join-Path $PSScriptRoot '..\target\load-test-monitor.jsonl'),
    [int]$IntervalSeconds = 2,
    [string]$StopFile = (Join-Path $PSScriptRoot '..\target\load-test-monitor.stop')
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$OutputPath = [System.IO.Path]::GetFullPath($OutputPath)
$StopFile = [System.IO.Path]::GetFullPath($StopFile)
$targetDirectory = Split-Path -Parent $OutputPath
New-Item -ItemType Directory -Path $targetDirectory -Force | Out-Null

$app = Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
    Where-Object { $_.CommandLine -like '*proinsight-0.0.1-SNAPSHOT.jar*' } |
    Select-Object -First 1
if (-not $app) {
    throw 'ProInsight application process not found'
}

$propertiesPath = Join-Path $root 'src\main\resources\application-local.properties'
$mongoLine = Get-Content -LiteralPath $propertiesPath |
    Where-Object { $_ -match '^spring\.data\.mongodb\.uri=' }
$mongoBaseUri = ($mongoLine -split '=', 2)[1]
$mongoUri = $mongoBaseUri -replace '/proinsight_dev\?', '/proinsight_loadtest?'
$dbEval = @'
const s = db.stats();
const collections = {};
for (const name of db.getCollectionNames()) {
  const c = db.getCollection(name).stats();
  collections[name] = { count: c.count, size: c.size, storageSize: c.storageSize, indexSize: c.totalIndexSize };
}
JSON.stringify({ dataSize: s.dataSize, storageSize: s.storageSize, indexSize: s.indexSize, objects: s.objects, fsUsedSize: s.fsUsedSize, collections });
'@

$lastCpuMs = $null
$lastSampleAt = $null
$dbSampleCounter = 0

while (-not (Test-Path -LiteralPath $StopFile)) {
    $sampleAt = Get-Date
    $process = Get-Process -Id $app.ProcessId -ErrorAction SilentlyContinue
    if (-not $process) {
        break
    }

    $affinityMask = $process.ProcessorAffinity.ToInt64()
    $effectiveCores = 0
    for ($bit = 0; $bit -lt 64; $bit++) {
        if (($affinityMask -band ([int64]1 -shl $bit)) -ne 0) {
            $effectiveCores++
        }
    }
    if ($effectiveCores -eq 0) {
        $effectiveCores = [Environment]::ProcessorCount
    }

    $cpuMs = $process.TotalProcessorTime.TotalMilliseconds
    $cpuOneCorePct = $null
    $cpuMachinePct = $null
    if ($null -ne $lastCpuMs -and $null -ne $lastSampleAt) {
        $wallMs = ($sampleAt - $lastSampleAt).TotalMilliseconds
        if ($wallMs -gt 0) {
            $cpuOneCorePct = [math]::Round((($cpuMs - $lastCpuMs) / $wallMs) * 100, 2)
            $cpuMachinePct = [math]::Round($cpuOneCorePct / $effectiveCores, 2)
        }
    }
    $lastCpuMs = $cpuMs
    $lastSampleAt = $sampleAt

    $os = Get-CimInstance Win32_OperatingSystem
    $hostTotalMb = [math]::Round($os.TotalVisibleMemorySize / 1024, 1)
    $hostFreeMb = [math]::Round($os.FreePhysicalMemory / 1024, 1)

    $mongo = $null
    $mongoStatsRaw = & docker stats --no-stream --format '{{json .}}' proinsight-mongo 2>$null
    if ($LASTEXITCODE -eq 0 -and $mongoStatsRaw) {
        $parsed = ($mongoStatsRaw | Select-Object -Last 1) | ConvertFrom-Json
        $mongo = [ordered]@{
            cpuPct = $parsed.CPUPerc
            memUsage = $parsed.MemUsage
            memPct = $parsed.MemPerc
            pids = $parsed.PIDs
        }
    }

    $db = $null
    if (($dbSampleCounter % 5) -eq 0) {
        $dbRaw = & docker exec proinsight-mongo mongosh "$mongoUri" --quiet --eval $dbEval 2>$null
        if ($LASTEXITCODE -eq 0 -and $dbRaw) {
            $db = ($dbRaw | Select-Object -Last 1) | ConvertFrom-Json
        }
    }
    $dbSampleCounter++

    $sample = [ordered]@{
        timestamp = $sampleAt.ToString('o')
        app = [ordered]@{
            pid = $app.ProcessId
            cpuOneCorePct = $cpuOneCorePct
            cpuMachinePct = $cpuMachinePct
            effectiveCores = $effectiveCores
            workingSetMb = [math]::Round($process.WorkingSet64 / 1MB, 1)
            privateMb = [math]::Round($process.PrivateMemorySize64 / 1MB, 1)
            threads = $process.Threads.Count
            handles = $process.HandleCount
        }
        host = [ordered]@{
            totalMb = $hostTotalMb
            freeMb = $hostFreeMb
        }
        mongo = $mongo
        db = $db
    }

    $sample | ConvertTo-Json -Compress -Depth 8 | Add-Content -LiteralPath $OutputPath
    Start-Sleep -Seconds $IntervalSeconds
}
