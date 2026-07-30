$files = @(
    "app/src/main/res/drawable/ic_launcher_background.xml",
    "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
    "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml"
)

foreach ($file in $files) {
    $path = Join-Path (Get-Location) $file
    if (Test-Path $path) {
        $content = Get-Content $path -Raw
        # Search for XML declaration
        if ($content -match '<\?xml[^>]*\?>') {
            $xmlDecl = $Matches[0]
            # Remove the declaration from its current position
            $cleanContent = $content -replace '<\?xml[^>]*\?>', ''
            # Trim leading whitespace/newlines and prepend the XML declaration
            $cleanContent = $cleanContent.TrimStart()
            $newContent = "$xmlDecl`r`n$cleanContent"
            Set-Content -Path $path -Value $newContent -Encoding UTF8 -NoNewline
            Write-Host "✅ Fixed: $file"
        }
    }
}
