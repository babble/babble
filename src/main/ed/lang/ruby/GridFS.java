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

import java.io.IOException;

import org.jruby.*;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import ed.db.DBBase;
import ed.db.DBCollection;
import ed.js.*;
import ed.js.engine.Scope;
import static ed.lang.ruby.RubyObjectWrapper.toJS;

public class GridFS {

    public static void save(RubyJSObjectWrapper rscope, RubyJSObjectWrapper rdb, RubyObject gridFile) {
        final Scope scope = (Scope)rscope.getJSObject();
        final Ruby runtime = gridFile.getRuntime();
        final ThreadContext context = runtime.getCurrentContext();

        DBBase db = (DBBase)rdb.getJSObject();
        DBCollection _files = db.getCollectionFromString("_files");
        RubyObject gridFileName = (RubyObject)gridFile.instance_variable_get(context, runtime.newString("@name"));
        String name = gridFileName.toString();
        String str = gridFile.callMethod(context, "read", JSFunctionWrapper.EMPTY_IRUBY_OBJECT_ARRAY, Block.NULL_BLOCK).toString();
        RubyHash metadata = (RubyHash)gridFile.instance_variable_get(context, runtime.newString("@metadata"));

        try {
            final JSInputFile f = new JSInputFile(null, null, str);
            f.set("filename", name);
            metadata.visitAll(new RubyHash.Visitor() {
                    public void visit(IRubyObject key, IRubyObject value) {
                        if (!value.toString().startsWith("_"))
                            f.set(toJS(scope, key), toJS(scope, value));
                    }
                });

            remove(rdb, name);
            _files.save(scope, f);
        }
        catch (IOException e) {
            System.err.println("GridFS.save: " + e);
            RaiseException.createNativeRaiseException(runtime, e);
        }
    }

    public static void remove(RubyJSObjectWrapper rdb, String fileName) {
        DBBase db = (DBBase)rdb.getJSObject();

        JSObjectBase criteria = new JSObjectBase();
        criteria.set("filename", fileName.toString());
        JSDBFile f = (JSDBFile)db.getCollectionFromString("_files").findOne(criteria);
        if (f != null)
            f.remove();         // Removes the chunks, too
    }
}
