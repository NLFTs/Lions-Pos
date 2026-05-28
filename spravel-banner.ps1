# spravel-banner.ps1
# Dipanggil oleh spravel.bat untuk menampilkan ASCII art + banner info
# Usage: spravel-banner.ps1 <frontend_dir> <app_properties_path>

param(
    [string]$FrontendDir = ".\frontend",
    [string]$AppPropertiesPath = ".\src\main\resources\application.properties"
)

# ── ASCII Art ──────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "Mari Memasak Tuan " -NoNewline
Write-Host "😈😈"
Write-Host ""
Write-Host "                                    " -ForegroundColor Green
Write-Host "         /\                         " -ForegroundColor Green
Write-Host "        /  \                        " -ForegroundColor Green
Write-Host "       / /\ \                       " -ForegroundColor Green
Write-Host "      / /  \ \                      " -ForegroundColor Green
Write-Host "     / / /\ \ \                     " -ForegroundColor Green
Write-Host "    /_/ /  \_\_\                    " -ForegroundColor Green
Write-Host "    \_\/    \/_/                    " -ForegroundColor Green
Write-Host ""

# ── Ambil Network IP ──────────────────────────────────────────────────────
$networkIP = "unknown"
try {
    $adapters = Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
        Where-Object { $_.IPAddress -notmatch '^127\.' -and $_.IPAddress -notmatch '^169\.254\.' } |
        Sort-Object -Property InterfaceIndex
    if ($adapters) {
        $networkIP = $adapters[0].IPAddress
    }
} catch {
    # fallback
    $ipOutput = ipconfig | Select-String "IPv4" | Select-Object -First 1
    if ($ipOutput) {
        $networkIP = ($ipOutput -split ":")[1].Trim()
    }
}

# ── Ambil port Vite dari .env.development ─────────────────────────────────
$vitePort = "5173"
$envFile = Join-Path $FrontendDir ".env.development"
if (Test-Path $envFile) {
    $portLine = Get-Content $envFile | Where-Object { $_ -match '^VITE_DEV_PORT=' }
    if ($portLine) {
        $vitePort = ($portLine -split "=")[1].Trim()
    }
}

# ── Ambil DB host dari application.properties ─────────────────────────────
$dbUrl = "localhost:5433"
if (Test-Path $AppPropertiesPath) {
    $dbLine = Get-Content $AppPropertiesPath | Where-Object { $_ -match '^spring\.datasource\.url=' }
    if ($dbLine) {
        $rawDb = ($dbLine -split "=", 2)[1].Trim()
        # Cek apakah pakai env var placeholder
        if ($rawDb -match '\$\{SPRING_DATASOURCE_URL:(.+)\}') {
            $rawDb = $Matches[1]
        }
        # Ekstrak host:port dari jdbc:postgresql://host:port/db
        if ($rawDb -match 'jdbc:postgresql://([^/]+)/') {
            $dbUrl = $Matches[1]
        }
    }
}

# ── Tampilkan Banner ──────────────────────────────────────────────────────
Write-Host "  " -NoNewline
Write-Host "▲ Spravel 2.1.0 (TwinTurbo)" -ForegroundColor Green
Write-Host ""
Write-Host "  - Local:         " -NoNewline; Write-Host "http://localhost:$vitePort" -ForegroundColor Cyan
Write-Host "  - Network:       " -NoNewline; Write-Host "http://${networkIP}:$vitePort" -ForegroundColor Cyan
Write-Host "  - Environments:  " -NoNewline; Write-Host ".env" -ForegroundColor Yellow
Write-Host "  - DB_Network:    " -NoNewline; Write-Host $dbUrl -ForegroundColor Yellow
Write-Host ""
Write-Host "  " -NoNewline
Write-Host "✓ Starting..." -ForegroundColor Green
Write-Host ""
