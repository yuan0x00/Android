package com.example.router;

import com.example.router.service.ILoginService;

public class RouterService {

    public static ILoginService loginService;

    public static void init() {
        if (loginService == null) {
            throw new RuntimeException("ILoginService not initialized!");
        }
    }
}