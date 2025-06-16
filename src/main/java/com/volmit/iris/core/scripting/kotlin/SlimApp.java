package com.volmit.iris.core.scripting.kotlin;

import io.github.slimjar.app.builder.ApplicationBuilder;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public final class SlimApp {
    private static final Logger LOGGER = Logger.getLogger("Iris");
    private static final ReentrantLock lock = new ReentrantLock();
    private static final AtomicBoolean loaded = new AtomicBoolean();

    public static void load() {
        if (loaded.get()) return;
        lock.lock();

        try {
            if (loaded.getAndSet(true)) return;
            ApplicationBuilder.appending("iris-scripting")
                    .logger((message, args) -> {
                        if (!message.startsWith("Loaded library ")) return;
                        LOGGER.info(message.formatted(args));
                    })
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

}
