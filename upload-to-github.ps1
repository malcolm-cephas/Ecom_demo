# Automated GitHub Upload Script
Write-Host "========================================"
Write-Host "  Automated GitHub Upload Script"
Write-Host "========================================"
Write-Host ""

# Add all files
Write-Host "Adding files to Git..."
git add .

# Commit changes
Write-Host "Committing changes..."
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
git commit -m "Update: $timestamp"

# Push to GitHub
Write-Host ""
Write-Host "Pushing to GitHub..."
Write-Host "Note: You may be prompted to authenticate via browser"
Write-Host ""

git push -u origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================"
    Write-Host "  Successfully uploaded to GitHub!"
    Write-Host "========================================"
    Write-Host ""
    Write-Host "Repository: https://github.com/malcolm-cephas/Ecom_demo"
}
else {
    Write-Host ""
    Write-Host "Upload failed. Please authenticate and try again."
    Write-Host "Visit: https://github.com/settings/tokens for access token"
}
