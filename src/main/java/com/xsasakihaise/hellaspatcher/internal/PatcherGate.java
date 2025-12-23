package com.xsasakihaise.hellaspatcher.internal;

public final class PatcherGate {

    public static volatile boolean ENABLED = false;
    public static volatile String DISABLE_REASON = "Not initialized";

    private PatcherGate() {
    }
}
