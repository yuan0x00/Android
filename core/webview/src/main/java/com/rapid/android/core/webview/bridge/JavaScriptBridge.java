package com.rapid.android.core.webview.bridge;

import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JavaScript桥接器
 * 提供Java与JavaScript之间的双向通信能力
 */
public class JavaScriptBridge {
    private static final String BRIDGE_NAME = "WebViewBridge";
    private static final String TAG = "JSBridge";

    private final android.webkit.WebView webView;
    private static final String INTERNAL_CALLBACK_METHOD = "__callback__";

    private final Map<String, BridgeHandler> handlers = new ConcurrentHashMap<>();
    private final Map<String, ValueCallback<String>> callbacks = new ConcurrentHashMap<>();

    private BridgeListener listener;
    private boolean isEnabled = true;

    public JavaScriptBridge(@NonNull android.webkit.WebView webView) {
        this.webView = webView;
        setupBridge();
        registerInternalHandlers();
    }

    /**
     * 设置桥接监听器
     */
    public JavaScriptBridge setBridgeListener(BridgeListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 启用/禁用桥接功能
     */
    public JavaScriptBridge setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }

    /**
     * 注册处理器
     */
    public JavaScriptBridge registerHandler(@NonNull String handlerName, @NonNull BridgeHandler handler) {
        if (INTERNAL_CALLBACK_METHOD.equals(handlerName)) {
            throw new IllegalArgumentException("Handler name " + INTERNAL_CALLBACK_METHOD + " is reserved");
        }
        handlers.put(handlerName, handler);
        return this;
    }

    /**
     * 注销处理器
     */
    public JavaScriptBridge unregisterHandler(@NonNull String handlerName) {
        handlers.remove(handlerName);
        return this;
    }

    /**
     * 调用JavaScript方法
     */
    public void callJavaScript(@NonNull String methodName, @Nullable Object data) {
        callJavaScript(methodName, data, null);
    }

    /**
     * 调用JavaScript方法（带回调）
     */
    public void callJavaScript(@NonNull String methodName, @Nullable Object data,
                              @Nullable ValueCallback<String> callback) {
        if (!isEnabled) {
            if (callback != null) {
                callback.onReceiveValue("{\"error\":\"Bridge is disabled\"}");
            }
            return;
        }

        try {
            String script = buildJavaScriptCall(methodName, data);
            if (callback != null) {
                String callbackId = generateCallbackId();
                callbacks.put(callbackId, callback);
                script = wrapWithCallback(script, callbackId);
            }

            if (listener != null) {
                listener.onJavaCall(script);
            }

            webView.evaluateJavascript(script, callback);

        } catch (Exception e) {
            String error = "Error calling JavaScript: " + e.getMessage();
            if (listener != null) {
                listener.onBridgeError(error);
            }
            if (callback != null) {
                callback.onReceiveValue("{\"error\":\"" + error + "\"}");
            }
        }
    }

    /**
     * 注入桥接JavaScript代码
     */
    private void setupBridge() {
        String bridgeScript = buildBridgeScript();
        webView.evaluateJavascript(bridgeScript, null);
    }

    /**
     * 构建桥接JavaScript代码
     */
    private String buildBridgeScript() {
        return "javascript:(function() {" +
                "if (window." + BRIDGE_NAME + ") { return; }" +
                "window." + BRIDGE_NAME + " = {" +
                "  call: function(method, data, callbackId) {" +
                "    var message = {" +
                "      method: method," +
                "      data: data," +
                "      callbackId: callbackId" +
                "    };" +
                "    " + BRIDGE_NAME + ".onMessage(JSON.stringify(message));" +
                "  }," +
                "  onMessage: function(message) {" +
                "    if (window." + BRIDGE_NAME + "Android) {" +
                "      window." + BRIDGE_NAME + "Android.onMessage(message);" +
                "    }" +
                "  }," +
                "  callback: function(callbackId, result) {" +
                "    if (window." + BRIDGE_NAME + "Callbacks && window." + BRIDGE_NAME + "Callbacks[callbackId]) {" +
                "      window." + BRIDGE_NAME + "Callbacks[callbackId](result);" +
                "      delete window." + BRIDGE_NAME + "Callbacks[callbackId];" +
                "    }" +
                "  }" +
                "};" +
                "window." + BRIDGE_NAME + "Callbacks = {};" +
                "})();";
    }

    /**
     * 构建JavaScript调用
     */
    private String buildJavaScriptCall(String methodName, Object data) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("method", methodName);
        json.put("data", data);
        return "javascript:window." + BRIDGE_NAME + ".callback(null, " + json.toString() + ");";
    }

    /**
     * 包装带回调的调用
     */
    private String wrapWithCallback(String script, String callbackId) {
        return "javascript:(function() {" +
                "window." + BRIDGE_NAME + "Callbacks['" + callbackId + "'] = function(result) {" +
                BRIDGE_NAME + ".call('" + INTERNAL_CALLBACK_METHOD + "', result, '" + callbackId + "');" +
                "};" +
                script +
                "})();";
    }

    /**
     * 生成回调ID
     */
    private String generateCallbackId() {
        return "cb_" + System.currentTimeMillis() + "_" + Math.random();
    }

    /**
     * 获取JavaScript接口实例
     */
    public JavaScriptInterface getJavaScriptInterface() {
        return new JavaScriptInterface();
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        handlers.clear();
        callbacks.clear();
    }

    public interface BridgeHandler {
        /**
         * 处理来自JavaScript的调用
         * @param data 参数数据
         * @param callback 回调函数
         */
        void handle(@Nullable Object data, @Nullable ValueCallback<String> callback);
    }

    public interface BridgeListener {
        /**
         * JavaScript调用日志
         * @param method 方法名
         * @param data 参数数据
         */
        void onJavaScriptCall(@NonNull String method, @Nullable Object data);

        /**
         * Java调用JavaScript日志
         * @param script JavaScript代码
         */
        void onJavaCall(@NonNull String script);

        /**
         * 桥接错误
         * @param error 错误信息
         */
        void onBridgeError(@NonNull String error);
    }

    private void registerInternalHandlers() {
        handlers.put(INTERNAL_CALLBACK_METHOD, (data, callback) -> {
            // no-op placeholder, real handling happens inside JavaScriptInterface#onMessage
        });
    }

    private void handleInternalCallback(@Nullable String callbackId, @Nullable Object data) {
        if (callbackId == null) {
            return;
        }
        ValueCallback<String> callback = callbacks.remove(callbackId);
        if (callback != null) {
            String payload = data != null ? String.valueOf(data) : "null";
            callback.onReceiveValue(payload);
        }
    }

    /**
     * JavaScript接口类
     */
    public class JavaScriptInterface {
        @JavascriptInterface
        public void onMessage(String message) {
            if (!isEnabled) {
                return;
            }

            try {
                JSONObject json = new JSONObject(message);
                String method = json.optString("method");
                Object data = json.opt("data");
                String callbackId = json.optString("callbackId", null);

            if (INTERNAL_CALLBACK_METHOD.equals(method)) {
                handleInternalCallback(callbackId, data);
                return;
            }

            if (listener != null) {
                listener.onJavaScriptCall(method, data);
            }

            BridgeHandler handler = handlers.get(method);
            if (handler != null) {
                ValueCallback<String> bridgeCallback = callbackId != null ?
                        result -> sendCallback(callbackId, result) : null;
                handler.handle(data, bridgeCallback);
            } else {
                String error = "No handler found for method: " + method;
                if (listener != null) {
                    listener.onBridgeError(error);
                }
                if (callbackId != null) {
                    sendCallback(callbackId, "{\"error\":\"" + error + "\"}");
                }
            }

            } catch (JSONException e) {
                String error = "Error parsing JavaScript message: " + e.getMessage();
                if (listener != null) {
                    listener.onBridgeError(error);
                }
            }
        }

        private void sendCallback(String callbackId, String result) {
            String script = "javascript:window." + BRIDGE_NAME + ".callback('" + callbackId + "', " + result + ");";
            webView.evaluateJavascript(script, null);
        }
    }

}
