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

import java.util.*;

import org.jruby.*;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import ed.appserver.JSFileLibrary;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import static ed.lang.ruby.RubyJxpSource.createNewClassesAndXGenMethods;
import static ed.lang.ruby.RubyObjectWrapper.isCallableJSFunction;

/**
 * Handles "require" and "load".
 */
public class Loader {

    static final Map<Ruby, Set<String>> _requiredJSFileLibFiles = new WeakHashMap<Ruby, Set<String>>();

    private Scope _scope;

    static void removeLoadedFiles(Ruby runtime) {
        _requiredJSFileLibFiles.remove(runtime);
    }

    public Loader(Scope scope) {
        _scope = scope;
    }

    public IRubyObject require(ThreadContext context, IRubyObject self, RubyModule module, String name, IRubyObject[] args, Block block) {
        Ruby runtime = self.getRuntime();
        String file = args[0].convertToString().toString();

        if (RubyObjectWrapper.DEBUG_FCALL)
            System.err.println("require " + file);
        try {
            return runtime.getLoadService().require(file) ? runtime.getTrue() : runtime.getFalse();
        }
        catch (RaiseException re) {
            if (_notAlreadyRequired(runtime, args[0])) {
                loadLibraryFile(_scope, runtime, self, file, re);
                _rememberAlreadyRequired(runtime, args[0]);
            }
            return runtime.getTrue();
        }
    }

    public IRubyObject load(ThreadContext context, IRubyObject self, RubyModule module, String name, IRubyObject[] args, Block block) {
        Ruby runtime = self.getRuntime();
        RubyString file = args[0].convertToString();
        boolean wrap = args.length == 2 ? args[1].isTrue() : false;

        if (RubyObjectWrapper.DEBUG_FCALL)
            System.err.println("load " + file);
        try {
            runtime.getLoadService().load(file.getByteList().toString(), wrap);
            return runtime.getTrue();
        }
        catch (RaiseException re) {
            return loadLibraryFile(_scope, runtime, self, file.toString(), re);
        }
    }

    protected IRubyObject loadLibraryFile(Scope scope, Ruby runtime, IRubyObject recv, String path, RaiseException re) {
        if (RubyObjectWrapper.DEBUG_FCALL)
            System.err.println("going to compile and run library file " + path + "; runtime = " + runtime);

        JSFileLibrary lib = getLibFromPath(path);
        if (lib == null)
            lib = (JSFileLibrary)_scope.get("local");
        else
            path = removeLibName(path);

        try {
            Object o = lib.getFromPath(path);
            if (isCallableJSFunction(o)) {
                try {
                    ((JSFunction)o).call(_scope, RubyJxpSource.EMPTY_OBJECT_ARRAY);
                    createNewClassesAndXGenMethods(_scope, runtime);
                }
                catch (Exception e) {
                    if (RubyObjectWrapper.DEBUG_SEE_EXCEPTIONS) {
                        System.err.println("problem loading JSFileLibrary file: " + e + "; going to raise Ruby error after printing the stack trace here");
                        e.printStackTrace();
                    }
                    recv.callMethod(runtime.getCurrentContext(), "raise", new IRubyObject[] {runtime.newString(e.toString())}, Block.NULL_BLOCK);
                }
                return runtime.getTrue();
            }
        }
        catch (Exception e) {
            if (RubyObjectWrapper.DEBUG_SEE_EXCEPTIONS) {
                System.err.println("problem loading JSFileLibrary file: " + e + "; going to re-throw original Ruby RaiseException after printing the stack trace here");
                e.printStackTrace();
            }
            /* fall through to throw re */
        }
        if (RubyObjectWrapper.DEBUG_FCALL)
            System.err.println("problem loading file " + path + " from lib " + lib + "; throwing original Ruby error " + re);
        throw re;
    }

    /**
     * Returns a JSFileLibrary named at the start of <var>path</var>, which is
     * something like "local/foo" or "/core/core/routes". The first word
     * ("local" or "core") must be the name of a JSFileLibrary that is in the
     * scope. Returns <code>null</code> if no library is found.
     */
    public JSFileLibrary getLibFromPath(String path) {
        String libName = libNameFromPath(path);
        return (JSFileLibrary)_scope.get(libName);
    }

    public String libNameFromPath(String path) {
        if (path.startsWith("/"))
            path = path.substring(1);
        int loc = path.indexOf("/");
        return (loc == -1) ? path : path.substring(0, loc);
    }

    /**
     * Returns a new copy of <var>path</var> with the first part of the path
     * stripped off.
     */
    public String removeLibName(String path) {
        if (path.startsWith("/"))
            path = path.substring(1);
        int loc = path.indexOf("/");
        return (loc == -1) ? "" : path.substring(loc + 1);
    }

    protected boolean _notAlreadyRequired(Ruby runtime, String file) {
        synchronized (_requiredJSFileLibFiles) {
            Set<String> reqs = _requiredJSFileLibFiles.get(runtime);
            return reqs == null || !reqs.contains(file);
        }
    }

    protected void _rememberAlreadyRequired(Ruby runtime, String file) {
        synchronized (_requiredJSFileLibFiles) {
            Set<String> reqs = _requiredJSFileLibFiles.get(runtime);
            if (reqs == null) {
                reqs = new HashSet<String>();
                _requiredJSFileLibFiles.put(runtime, reqs);
            }
            reqs.add(file);
        }
    }

}
