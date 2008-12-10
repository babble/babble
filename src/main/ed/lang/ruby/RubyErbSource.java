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

/**
 * Handles .erb and .rhtml files by turning the file into an ERB template.
 */
public class RubyErbSource extends RubyJxpSource {

    /**
     * This is public static so it can be used separately during testing.
     * @see {#wrap(String, String)}
     */
    public static String wrap(String content) {
        return wrap(content, "dummy_erb_file_name");
    }

    /**
     * Wraps <var>content</var> with code that turns it into an ERB template
     * and modifies the built-in JavaScript "print" function so that it
     * directs output to the ERB output collector.
     */
    public static String wrap(String content, String fileName) {
        return
            "require 'erb'\n" +
            "template = ERB.new <<-XGEN_ERB_TEMPLATE_EOF\n" +
            content.replace("\\", "\\\\").replace("#", "\\#") + '\n' +
            "XGEN_ERB_TEMPLATE_EOF\n" +
            "template.filename = '" + fileName.replace("'", "\\'") + "'\n" +
            "puts template.result(binding)\n";
    }

    public RubyErbSource(File f) {
        super(f);
    }

    /** For testing. */
    protected RubyErbSource(File f, Ruby runtime) {
        super(f, runtime);
    }
    
    /**
     * @see {#wrap(String, String)}
     */
    protected String getContent() throws IOException {
        return wrap(super.getContent(), getName());
    }
}
