function Get-ProjectTree {
    param(
        [string]$Path = ".",
        [string[]]$Exclude = @(".git", "node_modules", "target", ".idea", "dist", "build", ".venv", ".next", ".gradle")
    )

    $items = Get-ChildItem -Path $Path -ErrorAction SilentlyContinue | Where-Object { 
        $name = $_.Name
        $excludeThis = $false
        foreach ($ex in $Exclude) {
            if ($name -eq $ex) { $excludeThis = $true; break }
        }
        -not $excludeThis
    }

    foreach ($item in $items) {
        if ($item.PSIsContainer) {
            Write-Output "Folder: $($item.FullName)"
            Get-ProjectTree -Path $item.FullName -Exclude $Exclude
        } else {
            Write-Output "File  : $($item.FullName)"
        }
    }
}

Get-ProjectTree -Path "d:\Malcolm\DSCE\Internship\SENSEI\ecommerce" | Out-File -Encoding ASCII project_structure_clean.txt
