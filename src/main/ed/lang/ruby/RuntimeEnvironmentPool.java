/**
*    Copyright (C) 2008 10gen Inc.
*
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
