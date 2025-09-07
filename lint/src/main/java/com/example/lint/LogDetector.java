package com.example.lint;

import com.android.tools.lint.detector.api.*;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;

import java.util.Arrays;
import java.util.List;

public class LogDetector extends Detector implements Detector.UastScanner {
    public static final Issue ISSUE = Issue.create(
            "NoLog",
            "避免直接使用android.util.Log",
            "避免直接使用android.util.Log",
            Category.SECURITY,
            5,
            Severity.ERROR,
            new Implementation(LogDetector.class, Scope.JAVA_FILE_SCOPE)
    );

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList("v", "d", "i", "w", "e");
    }

    @Override
    public void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression node, @NotNull PsiMethod method) {
        if (context.getEvaluator().isMemberInClass(method, "android.util.Log")) {
            context.report(ISSUE, node, context.getLocation(node), "请勿直接调用Log，应该使用统一Log工具类");
        }
    }
}
