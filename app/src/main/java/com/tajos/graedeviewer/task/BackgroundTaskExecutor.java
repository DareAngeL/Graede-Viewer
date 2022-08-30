package com.tajos.graedeviewer.task;

import java.util.concurrent.Executor;

public class BackgroundTaskExecutor implements Executor {
    @Override
    public void execute(Runnable command) {
        new Thread(command).start();
    }
}