package com.volmit.iris.core.scripting.kotlin;

import com.volmit.iris.core.loader.IrisData;
import com.volmit.iris.core.scripting.ExecutionEnvironment;
import com.volmit.iris.core.scripting.kotlin.environment.IrisExecutionEnvironment;
import com.volmit.iris.core.scripting.kotlin.environment.IrisPackExecutionEnvironment;
import com.volmit.iris.core.scripting.kotlin.environment.IrisSimpleExecutionEnvironment;
import com.volmit.iris.engine.framework.Engine;

public class IrisExecutionEnvironmentProvider implements ExecutionEnvironment.Provider {
    public IrisExecutionEnvironmentProvider() {
        SlimApp.load();
    }

    @Override
    public ExecutionEnvironment.Engine createEngine(Engine engine) {
        return new IrisExecutionEnvironment(engine);
    }

    @Override
    public ExecutionEnvironment.Pack createPack(IrisData data) {
        return new IrisPackExecutionEnvironment(data);
    }

    @Override
    public ExecutionEnvironment.Simple createSimple() {
        return new IrisSimpleExecutionEnvironment();
    }
}
