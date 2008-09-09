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

import org.testng.annotations.*;

import ed.lang.ruby.RubyErbSource;

/** Makes RubyErbSource testable by letting us control input and capture output. */
class TestRubyErbSource extends TestRubyJxpSource {
    public TestRubyErbSource(org.jruby.Ruby runtime) {
	super(runtime);
    }
    protected String getContent() {
	return RubyErbSource.wrap(_content);
    }
}

public class ErbSourceRunner extends SourceRunner {

    @BeforeMethod(groups={"ruby", "ruby.erbsource"})
    public void setUp() {
	super.setUp();
	source = new TestRubyErbSource(r);
    }
}
