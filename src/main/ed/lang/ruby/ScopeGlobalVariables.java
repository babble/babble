package ed.lang.ruby;

import java.util.Set;
import java.util.HashSet;

import org.jruby.Ruby;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.runtime.builtin.IRubyObject;

import ed.js.engine.Scope;
import ed.net.httpserver.HttpResponse;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;

public class ScopeGlobalVariables extends GlobalVariables {

    private Scope _scope;
    private Ruby _runtime;
    private GlobalVariables _oldies;
    private IRubyObject _defaultSeparator;

    public ScopeGlobalVariables(Scope scope, Ruby runtime) {
	super(runtime);
	_scope = scope;
	_runtime = runtime;

	_oldies = _runtime.getGlobalVariables();
	for (String name : _oldies.getNames()) {
	    if (!"$=".equals(name))
		_scope.put(nameToKey(name), toJS(_scope, _oldies.get(name)));
	}
    }

    public GlobalVariables getOldGlobalVariables() { return _oldies; }

    public boolean isDefined(String name) {
	if (_scope.get(nameToKey(name)) != null)
	    return true;
	return _oldies.isDefined(name);
    }

    public IRubyObject get(String name) {
	String key = nameToKey(name);
	Object o = _scope.get(key);
	if (o == null)
	    return _oldies.get(name);
	return toRuby(_scope, _runtime, o, key);
    }

    public IRubyObject set(String name, IRubyObject value) {
	IRubyObject val = super.set(name, value);
	_scope.put(nameToKey(name), toJS(_scope, val));
	return val;
    }

    public Set<String> getNames() {
	Set<String> names = new HashSet<String>(super.getNames());
	for (Object key : RubyScopeWrapper.jsKeySet(_scope))
	    names.add("$" + key.toString());
	return names;
    }

    public IRubyObject getDefaultSeparator() {
        return _oldies.getDefaultSeparator();
    }

    public void setDefaultSeparator(IRubyObject defaultSeparator) {
	_oldies.setDefaultSeparator(defaultSeparator);
    }

    private String nameToKey(String name) {
        assert name != null;
        assert name.startsWith("$");
	return name.substring(1);
    }
}
