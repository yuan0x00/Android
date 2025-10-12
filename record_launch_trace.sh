#!/bin/bash
set -e

# ==============================
# 配置区（按需修改）
# ==============================
# 应用包名
PACKAGE_NAME="com.rapid.android"
# Activity包名
MAIN_ACTIVITY=".ui.feature.main.MainActivity"
# 录制时长 支持 5s / 10s / 1m 等
TRACE_DURATION="5s"
# 输出文件名
OUTPUT_FILE="record_launch_trace.perfetto"
# atrace 类别，空格分隔
ATRACE_CATEGORIES="am wm view gfx"
# 启动 App 前等待录制初始化的时间（秒）
LAUNCH_DELAY=0.3

# ==============================
# 自动下载 record_android_trace（如不存在）
# ==============================
if [ ! -f "./record_android_trace" ]; then
  echo "record_android_trace not found. Downloading from GitHub..."
  curl -fsSL -o record_android_trace https://raw.githubusercontent.com/google/perfetto/master/tools/record_android_trace
  chmod +x record_android_trace
fi

# ==============================
# 冷启动录制流程
# ==============================
echo "Force stopping $PACKAGE_NAME..."
adb shell am force-stop "$PACKAGE_NAME"

echo "Starting Perfetto trace for $PACKAGE_NAME (duration: $TRACE_DURATION)..."
./record_android_trace \
  -o "$OUTPUT_FILE" \
  -t "$TRACE_DURATION" \
  -a "$PACKAGE_NAME" \
  $ATRACE_CATEGORIES \
  --no-open &
RECORD_PID=$!

# 等待 trace 真正启动（关键！避免错过启动初期事件）
sleep "$LAUNCH_DELAY"

echo "Cold launching $PACKAGE_NAME$MAIN_ACTIVITY..."
adb shell am start "$PACKAGE_NAME/$MAIN_ACTIVITY"

# 等待录制结束
wait "$RECORD_PID"

echo
echo " Trace saved to: $OUTPUT_FILE"
echo " Open it at: https://ui.perfetto.dev"
