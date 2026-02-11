$logPath = "D:\Malcolm\DSCE\Internship\SENSEI\ecommerce\Logs"
$files = @(
    "$logPath\spring_backend.log",
    "$logPath\react_frontend.log",
    "$logPath\mcp_server.log",
    "$logPath\mcp_client.log"
)

Write-Host "Monitoring logs from: $logPath"
Write-Host "Waiting for log files to be created..."

# Wait for at least one file to exist before starting
while ($true) {
    $existing = $files | Where-Object { Test-Path $_ }
    if ($existing.Count -gt 0) { 
        Write-Host "Found $($existing.Count) log files. Starting monitor..."
        break 
    }
    Start-Sleep -Seconds 2
}

Get-Content -Path $files -Wait -Tail 10 -ErrorAction SilentlyContinue
