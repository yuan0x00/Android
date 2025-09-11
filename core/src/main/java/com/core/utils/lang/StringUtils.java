package com.core.utils.lang;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

/**
 * 字符串工具类（空安全、高效、常用方法全覆盖）
 * 所有方法均对 null 输入做保护
 */
public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("StringUtils cannot be instantiated");
    }

    // —————— 判空 ——————

    /**
     * 判断字符串是否为空（null 或 "" 或 纯空白）
     */
    public static boolean isEmpty(@Nullable CharSequence str) {
        return TextUtils.isEmpty(str) || TextUtils.getTrimmedLength(str) == 0;
    }

    /**
     * 判断字符串是否非空
     */
    public static boolean isNotEmpty(@Nullable CharSequence str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为 null
     */
    public static boolean isNull(@Nullable Object obj) {
        return obj == null;
    }

    /**
     * 判断字符串是否非 null
     */
    public static boolean isNotNull(@Nullable Object obj) {
        return obj != null;
    }

    // —————— 去空白 ——————

    /**
     * 去除首尾空白（null 安全）
     */
    @Nullable
    public static String trim(@Nullable String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 去除所有空白字符（包括中间）
     */
    @Nullable
    public static String removeAllWhitespace(@Nullable String str) {
        if (str == null) return null;
        return str.replaceAll("\\s+", "");
    }

    // —————— 截取 ——————

    /**
     * 截取指定长度（超出不报错，安全返回）
     */
    @Nullable
    public static String substring(@Nullable String str, int start) {
        if (str == null) return null;
        if (start < 0) start = 0;
        return start < str.length() ? str.substring(start) : "";
    }

    /**
     * 截取指定范围（安全版）
     */
    @Nullable
    public static String substring(@Nullable String str, int start, int end) {
        if (str == null) return null;
        if (start < 0) start = 0;
        if (end > str.length()) end = str.length();
        if (start > end) return "";
        return str.substring(start, end);
    }

    /**
     * 获取前 N 个字符（安全）
     */
    @Nullable
    public static String left(@Nullable String str, int len) {
        if (str == null || len < 0) return null;
        if (str.length() <= len) return str;
        return str.substring(0, len);
    }

    /**
     * 获取后 N 个字符（安全）
     */
    @Nullable
    public static String right(@Nullable String str, int len) {
        if (str == null || len < 0) return null;
        if (str.length() <= len) return str;
        return str.substring(str.length() - len);
    }

    // —————— 拼接 ——————

    /**
     * 拼接字符串（null 自动转 ""）
     */
    @NonNull
    public static String concat(@Nullable String... strs) {
        if (strs == null) return "";
        StringBuilder sb = new StringBuilder();
        for (String str : strs) {
            if (str != null) {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    /**
     * 用分隔符拼接（跳过 null 和空字符串）
     */
    @NonNull
    public static String join(@NonNull String separator, @Nullable String... strs) {
        if (strs == null || strs.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (String str : strs) {
            if (isNotEmpty(str)) {
                if (sb.length() > 0) {
                    sb.append(separator);
                }
                sb.append(str);
            }
        }
        return sb.toString();
    }

    // —————— 比较 ——————

    /**
     * 安全比较（忽略大小写，null 安全）
     */
    public static boolean equalsIgnoreCase(@Nullable String str1, @Nullable String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equalsIgnoreCase(str2);
    }

    /**
     * 安全比较（区分大小写）
     */
    public static boolean equals(@Nullable String str1, @Nullable String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equals(str2);
    }

    // —————— 格式化 ——————

    /**
     * 格式化字符串（null 安全）
     */
    @NonNull
    public static String format(@NonNull String format, @Nullable Object... args) {
        if (args == null) return format;
        try {
            return String.format(Locale.getDefault(), format, args);
        } catch (Exception e) {
            return format; // 降级返回原格式串
        }
    }

    // —————— 转换 ——————

    /**
     * 转为整数（失败返回默认值）
     */
    public static int toInt(@Nullable String str, int defaultValue) {
        if (isEmpty(str)) return defaultValue;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 转为长整型
     */
    public static long toLong(@Nullable String str, long defaultValue) {
        if (isEmpty(str)) return defaultValue;
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 转为浮点数
     */
    public static float toFloat(@Nullable String str, float defaultValue) {
        if (isEmpty(str)) return defaultValue;
        try {
            return Float.parseFloat(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 转为布尔值（支持 "true", "1", "yes", "on"）
     */
    public static boolean toBoolean(@Nullable String str, boolean defaultValue) {
        if (isEmpty(str)) return defaultValue;
        String lower = str.trim().toLowerCase(Locale.getDefault());
        return "true".equals(lower) || "1".equals(lower) || "yes".equals(lower) || "on".equals(lower);
    }

    // —————— 包含/匹配 ——————

    /**
     * 是否包含子串（null 安全）
     */
    public static boolean contains(@Nullable String str, @Nullable String searchStr) {
        if (str == null || searchStr == null) return false;
        return str.contains(searchStr);
    }

    /**
     * 是否以某字符串开头
     */
    public static boolean startsWith(@Nullable String str, @Nullable String prefix) {
        if (str == null || prefix == null) return false;
        return str.startsWith(prefix);
    }

    /**
     * 是否以某字符串结尾
     */
    public static boolean endsWith(@Nullable String str, @Nullable String suffix) {
        if (str == null || suffix == null) return false;
        return str.endsWith(suffix);
    }

    // —————— 编码 ——————

    /**
     * URL 编码（UTF-8）
     */
    @Nullable
    public static String urlEncode(@Nullable String str) {
        if (str == null) return null;
        try {
            return java.net.URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            return str; // 降级
        }
    }

    /**
     * URL 解码（UTF-8）
     */
    @Nullable
    public static String urlDecode(@Nullable String str) {
        if (str == null) return null;
        try {
            return java.net.URLDecoder.decode(str, "UTF-8");
        } catch (Exception e) {
            return str; // 降级
        }
    }

    // —————— 其他实用方法 ——————

    /**
     * 获取字符串长度（null 安全）
     */
    public static int length(@Nullable CharSequence str) {
        return str == null ? 0 : str.length();
    }

    /**
     * 替换字符串（null 安全）
     */
    @Nullable
    public static String replace(@Nullable String str, @Nullable String oldStr, @Nullable String newStr) {
        if (str == null || oldStr == null || newStr == null) return str;
        return str.replace(oldStr, newStr);
    }

    /**
     * 隐藏手机号中间4位（如 138****1234）
     */
    @Nullable
    public static String maskPhone(@Nullable String phone) {
        if (isEmpty(phone) || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 隐藏邮箱用户名部分（如 a***b@example.com）
     */
    @Nullable
    public static String maskEmail(@Nullable String email) {
        if (isEmpty(email)) return email;
        int atPos = email.indexOf('@');
        if (atPos <= 1) return email;
        String username = email.substring(0, atPos);
        String domain = email.substring(atPos);
        if (username.length() <= 2) {
            return username + domain;
        }
        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + domain;
    }
}