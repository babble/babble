/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.lang.ruby;

import java.util.HashMap;
import java.util.Map;

import ed.appserver.AppContext;
import ed.net.httpserver.HttpServer;
import ed.util.SimplePool;

/**
 * Provides a pool of {@link RuntimeEnvironment} objects, one pool for each {@link AppContext}.
 */
public class RuntimeEnvironmentPool extends SimplePool<RuntimeEnvironment> {

    /** Each AppContext has its own RuntimeEnvironment pool. */
    static Map<AppContext, RuntimeEnvironmentPool> POOLS = new HashMap<AppContext, RuntimeEnvironmentPool>();

    /** Returns an instance of RuntimeEnvironment from the pool. */
    public static RuntimeEnvironment getInstance(AppContext appContext) {
        if (appContext == null)
            throw new IllegalArgumentException("appContext must not be null");
        RuntimeEnvironmentPool pool = POOLS.get(appContext);
        if (pool == null) {
            pool = new RuntimeEnvironmentPool(appContext);
            POOLS.put(appContext, pool);
        }
        return pool.get();
    }

    public static void releaseInstance(RuntimeEnvironment runenv) {
        AppContext appContext = runenv.getAppContext();
        RuntimeEnvironmentPool pool = POOLS.get(appContext);
        if (pool == null)
            throw new IllegalArgumentException("Trying to release Ruby RuntimeEnvironment; unknown AppContext " + (appContext == null ? "null" : ("" + appContext.hashCode())));
        pool.done(runenv);
    }

    private AppContext appContext;

    private RuntimeEnvironmentPool(AppContext appContext) {
        super("RubyRunEnv-" + appContext.hashCode(), HttpServer.WORKER_THREADS, HttpServer.WORKER_THREAD_QUEUE_MAX);
        this.appContext = appContext;
    }

    protected RuntimeEnvironment createNew() { return new RuntimeEnvironment(appContext); }
}
