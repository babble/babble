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

import java.io.File;
import java.io.IOException;

import org.jruby.Ruby;
import org.jruby.ast.Node;

import ed.appserver.JSFileLibrary;
import ed.js.engine.Scope;

/**
 * Handles .erb and .rhtml files by turning the file into an ERB template and
 * modifying the JavaScript "print" function so that it directs output to the
 * ERB output collector.
 */
public class RubyErbSource extends RubyJxpSource {

    /**
     * Wraps <var>content</var> with code that turns it into an ERB template
     * and modifies the built-in JavaScript "print" function so that it
     * directs output to the ERB output collector.
     * <p>
     * This is public static so it can be used separately during testing.
     */
    public static String wrap(String content) {
	return
	    "_erbout = nil\n" +
	    "require 'erb'\n" +
	    "__p = Proc.new { |str| _erbout.concat(str) }\n" +
	    "$scope.set('print', __p)\n" +
	    "$scope.set('puts', __p)\n" +
	    "$scope.set('__puts__', __p)\n" +
	    "$scope.set('SYSOUT', __p)\n" +
	    "template = ERB.new <<-XGEN_ERB_TEMPLATE_EOF\n" +
	    content.replace("\\", "\\\\").replace("#", "\\#") + '\n' +
	    "XGEN_ERB_TEMPLATE_EOF\n" +
	    "puts template.result(binding)\n";
    }

    public RubyErbSource(File f , JSFileLibrary lib) {
	super(f, lib);
    }

    /** For testing. */
    protected RubyErbSource(Ruby runtime) {
	super(runtime);
    }
    
    /**
     * @see {#wrap}
     */
    protected String getContent() throws IOException {
	return wrap(super.getContent());
    }

    protected Object _doCall(Node code, Scope s, Object unused[]) {
	Object print = s.get("print");
	Object puts = s.get("puts");
	Object __puts__ = s.get("__puts__");
	Object SYSOUT = s.get("SYSOUT");
	try {
	    return super._doCall(code, s, unused);
	}
	finally {
	    s.put("print", print);
	    s.put("puts", puts);
	    s.put("__puts__", __puts__);
	    s.put("SYSOUT", SYSOUT);
	}
    }
}
