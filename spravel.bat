@echo off
setlocal enabledelayedexpansion

REM ── Base Paths ─────────────────────────────────────────────
set DIR=%~dp0
set MVNW=%DIR%mvnw.cmd
set FRONTEND=%DIR%frontend
set PNPM=npx -y pnpm

REM ── Command Argument ───────────────────────────────────────
set CMD=%1

REM ── Frontend ──────────────────────────────────────────────
if "%CMD%"=="frontend:install" (
    echo Installing frontend dependencies...
    cd /d "%FRONTEND%"
    %PNPM% install --frozen-lockfile
    exit /b
)

if "%CMD%"=="frontend:build" (
    echo Building frontend...
    cd /d "%FRONTEND%"
    %PNPM% install --frozen-lockfile
    %PNPM% run build
    exit /b
)

if "%CMD%"=="frontend:dev" (
    echo Starting Vite dev server...
    cd /d "%FRONTEND%"
    %PNPM% run dev
    exit /b
)

if "%CMD%"=="frontend:clean" (
    echo Cleaning frontend build output...
    rmdir /s /q "%DIR%src\main\resources\static\assets" 2>nul
    del /f /q "%DIR%src\main\resources\static\index.html" 2>nul
    echo Done.
    exit /b
)

REM ── Backend ───────────────────────────────────────────────
if "%CMD%"=="backend:dev" (
    echo Starting Spring Boot...
    "%MVNW%" spring-boot:run -Dexec.skip=true
    exit /b
)

if "%CMD%"=="backend:build" (
    echo Compiling backend...
    "%MVNW%" compile -Dexec.skip=true -q
    exit /b
)

if "%CMD%"=="backend:test" (
    echo Running backend tests...
    "%MVNW%" test -Dexec.skip=true
    exit /b
)

if "%CMD%"=="backend:clean" (
    echo Cleaning backend build...
    "%MVNW%" clean -q
    exit /b
)

REM ── Fullstack ─────────────────────────────────────────────
if "%CMD%"=="full:build" (
    echo Building frontend + backend JAR...
    "%MVNW%" package -DskipTests
    exit /b
)

if "%CMD%"=="full:clean" (
    echo Cleaning everything...
    rmdir /s /q "%DIR%src\main\resources\static\assets" 2>nul
    del /f /q "%DIR%src\main\resources\static\index.html" 2>nul
    "%MVNW%" clean -q
    echo Done.
    exit /b
)

if "%CMD%"=="full:dev" (
    echo Starting Vite + Spring Boot in parallel...
    echo Close both windows to stop.

    start "Vite Dev Server" cmd /k "cd /d %FRONTEND% && %PNPM% run dev"
    start "Spring Boot" cmd /k ""%MVNW%" spring-boot:run -Dexec.skip=true"

    exit /b
)

REM ── Help ──────────────────────────────────────────────────
echo.
echo  Spravel CLI — Usage: spravel.bat ^<command^>
echo.
echo  Frontend:
echo    frontend:install   Install dependencies (pnpm)
echo    frontend:build     Build Vue
echo    frontend:dev       Vite dev server
echo    frontend:clean     Clean build output
echo.
echo  Backend:
echo    backend:dev        Spring Boot dev server
echo    backend:build      Compile backend only
echo    backend:test       Run tests
echo    backend:clean      Clean build
echo.
echo  Fullstack:
echo    full:build         Frontend + backend JAR
echo    full:clean         Clean everything
echo    full:dev           Vite + Spring Boot (parallel)
echo.