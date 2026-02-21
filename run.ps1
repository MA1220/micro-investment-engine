# Stop any process using port 5477 so the app can start
$port = 5477
$conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
if ($conn) {
    $conn | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
    Write-Host "Stopped process(es) on port $port"
    Start-Sleep -Seconds 2
}

Set-Location $PSScriptRoot
Write-Host "Starting micro-investment-engine on port 5477..."
java -jar target\micro-investment-engine-1.0.0.jar
