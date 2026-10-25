@echo off
chcp 936 >nul
title 星海视频社交平台 - 一键启动器
setlocal enabledelayedexpansion

rem ============================================================
rem 星海视频社交平台 一键启动脚本（Windows）
rem 作用：依次检查并启动 MySQL、Redis、后端、三个前端
rem 用法：双击运行本文件即可；各服务会在独立的黑色窗口里运行
rem ============================================================

set ROOT=F:\AI_full-stack_project\XingHai
set PHPPSTUDY=D:\phpstudy_pro\COM\phpstudy_pro.exe
set REDIS_SERVER=F:\Redis\redis-server.exe

echo ============================================================
echo            星海视频社交平台 一键启动
echo ============================================================
echo.

rem ---------- 1. 检查 MySQL（端口 3306） ----------
echo [1/4] 检查 MySQL（端口 3306）...
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul
if errorlevel 1 (
    echo   [X] 未检测到 MySQL，正在尝试打开 phpStudy...
    if exist "%PHPPSTUDY%" (
        start "" "%PHPPSTUDY%"
    )
    echo   请在弹出的 phpStudy 窗口里点击「启动」MySQL，等 MySQL 变绿后，
    echo   回到本窗口按任意键继续...
    pause
    netstat -ano | findstr ":3306" | findstr "LISTENING" >nul
    if errorlevel 1 (
        echo   [X] MySQL 仍未启动，脚本退出。启动 MySQL 后请重新运行本脚本。
        pause
        exit /b 1
    )
)
echo   [OK] MySQL 已运行
echo.

rem ---------- 2. 检查 Redis（端口 6379） ----------
echo [2/4] 检查 Redis（端口 6379）...
netstat -ano | findstr ":6379" | findstr "LISTENING" >nul
if errorlevel 1 (
    echo   [-] Redis 未运行，正在启动...
    if exist "%REDIS_SERVER%" (
        start "Redis缓存" "%REDIS_SERVER%"
        timeout /t 3 >nul
    ) else (
        echo   [!] 未找到 %REDIS_SERVER%，请手动启动 Redis（没有 Redis 也能运行，只是缓存不生效）
    )
) else (
    echo   [OK] Redis 已运行
)
echo.

rem ---------- 3. 启动后端（端口 8080） ----------
echo [3/4] 检查后端（端口 8080）...
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul
if errorlevel 1 (
    echo   [-] 正在新窗口启动后端（首次启动约 20-40 秒，请耐心等待）...
    start "XingHai后端-8080" cmd /k "cd /d %ROOT%\backend && mvn spring-boot:run"
) else (
    echo   [OK] 后端已在运行
)
echo.

rem ---------- 4. 启动三个前端 ----------
echo [4/4] 检查三个前端...
netstat -ano | findstr ":5173" | findstr "LISTENING" >nul
if errorlevel 1 start "用户端-5173" cmd /k "cd /d %ROOT%\web-user && npm run dev"
netstat -ano | findstr ":5174" | findstr "LISTENING" >nul
if errorlevel 1 start "创作者端-5174" cmd /k "cd /d %ROOT%\web-creator && npm run dev"
netstat -ano | findstr ":5175" | findstr "LISTENING" >nul
if errorlevel 1 start "管理端-5175" cmd /k "cd /d %ROOT%\web-admin && npm run dev"
echo   [OK] 前端启动命令已发出（窗口标题分别为 用户端/创作者端/管理端）
echo.

echo ============================================================
echo   全部启动命令已发出！
echo.
echo   请等待「后端」窗口出现  Started XingHaiApplication 字样
echo   （约 20-40 秒），然后在浏览器打开：
echo.
echo     用户端     http://localhost:5173
echo     创作者端   http://localhost:5174
echo     管理端     http://localhost:5175
echo.
echo.
echo   关闭服务：把弹出的黑色窗口全部关掉即可。
echo ============================================================
echo.
pause
