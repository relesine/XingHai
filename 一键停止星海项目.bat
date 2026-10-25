@echo off
chcp 936 >nul
title XingHai Stop
setlocal enabledelayedexpansion

rem ============================================================
rem  星海视频社交平台 一键停止脚本
rem  关闭: 后端8080 用户端5173 创作者端5174 管理端5175 Redis6379
rem  只按端口精确停止本项目, 不会误杀其他 node/java 程序
rem  MySQL 由 phpStudy 管理, 本脚本不关闭它
rem ============================================================

echo ============================================================
echo            星海视频社交平台 一键停止
echo ============================================================
echo.

rem ---------- 第1步: 关闭一键启动脚本打开的命令行窗口 ----------
echo [1/3] 关闭一键启动脚本打开的窗口...
call :KillWin "XingHai后端-8080"
call :KillWin "用户端-5173"
call :KillWin "创作者端-5174"
call :KillWin "管理端-5175"
call :KillWin "Redis缓存"
echo.

timeout /t 2 >nul

rem ---------- 第2步: 优雅关闭Redis, 让它保存数据 ----------
echo [2/3] 关闭 Redis...
netstat -ano | findstr ":6379 " | findstr "LISTENING" >nul
if not errorlevel 1 (
    if exist "F:\Redis\redis-cli.exe" (
        "F:\Redis\redis-cli.exe" shutdown 2>nul
    )
)

rem ---------- 第3步: 按端口兜底强杀残留进程 ----------
echo [3/3] 按端口检查并停止残留进程...
echo.
call :KillPort 8080 后端
call :KillPort 5173 用户端
call :KillPort 5174 创作者端
call :KillPort 5175 管理端
call :KillPort 6379 Redis

echo.
echo ============================================================
echo   停止完成!
echo   已处理: 后端 三个前端 Redis
echo   未处理: MySQL, 需关闭请在 phpStudy 里点停止
echo   再次启动请双击: 一键启动星海项目.bat
echo ============================================================
echo.
pause
exit /b 0


rem ============================================================
rem 子程序: 按端口停止监听进程, /F强制 /T连子进程
rem ============================================================
:KillPort
set "PORT=%1"
set "LABEL=%2"
set "PID="
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%PORT% " ^| findstr "LISTENING"') do set "PID=%%p"
if defined PID (
    echo   [-] 正在停止 %LABEL%, 端口 %PORT%, PID !PID!
    taskkill /F /T /PID !PID! >nul 2>&1
    if errorlevel 1 (
        echo       [X] 停止 %LABEL% 失败, 可手动在任务管理器结束 PID !PID!
    ) else (
        echo       [OK] %LABEL% 已停止
    )
) else (
    echo   [OK] %LABEL% 端口 %PORT% 本来就没有运行
)
goto :eof


rem ============================================================
rem 子程序: 按窗口标题关闭命令行窗口
rem ============================================================
:KillWin
taskkill /F /T /FI "WINDOWTITLE eq %~1*" >nul 2>&1
goto :eof
