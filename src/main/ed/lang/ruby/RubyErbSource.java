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

import ed.appserver.JSFileLibrary;
import ed.js.engine.Scope;

public class RubyErbSource extends RubyJxpSource {

    public RubyErbSource(File f , JSFileLibrary lib) {
	super(f, lib);
    }

    /** For testing. */
    protected RubyErbSource(org.jruby.Ruby runtime) {
	super(runtime);
    }
    
    protected String getContent() throws IOException {
	return "$scope.print = Proc.new { |str| _erbout.concat(str) }\n" +
	    "require 'erb'\n" +
	    "template = ERB.new <<-XGEN_ERB_TEMPLATE_EOF\n" +
	    super.getContent() + '\n' +
	    "XGEN_ERB_TEMPLATE_EOF\n" +
	    "puts template.result(binding)\n";
    }
}
