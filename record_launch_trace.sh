#!/bin/bash
set -e

# 下载官方脚本
#curl -O https://raw.githubusercontent.com/google/perfetto/master/tools/record_android_trace
#chmod +x record_android_trace

# 1. 强制关闭 App（确保冷启动）
adb shell am force-stop com.rapid.android

# 2. 启动 Perfetto 录制（后台）
./record_android_trace -o record_launch_trace.perfetto -t 5s -a com.rapid.android am wm view gfx --no-open &
RECORD_PID=$!

# 3. 等待录制真正开始（关键延迟）
sleep 0.3

# 4. 冷启动 App
adb shell am start com.rapid.android/.ui.feature.main.MainActivity

# 5. 等待录制结束
wait $RECORD_PID

echo "Open .perfetto file at https://ui.perfetto.dev"