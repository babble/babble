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

import org.jruby.ast.Node;

import ed.appserver.AppRequest;
import ed.appserver.adapter.cgi.EnvMap;
import ed.io.StreamUtil;
import ed.js.JSFunction;
import ed.appserver.adapter.cgi.CGIAdapter;
import ed.util.Dependency;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Set;

public class RubyCGIAdapter extends CGIAdapter {

    protected File file;
    protected Node node;
    protected long lastCompile;

    public RubyCGIAdapter(File f) {
        file = f;
    }

    protected String getContent() throws IOException {
        return StreamUtil.readFully(file);
    }

    protected InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public JSFunction getFunction() throws IOException {
        throw new RuntimeException("PROGRAMMER ERROR : it appears that we're being invoked as JxpSource, not a CGI adapter");
    }

    public long lastUpdated(Set<Dependency> visitedDeps) {
        return file.lastModified();
    }

    public String getName() {
        return file.toString();
    }

    public File getFile() {
        return file;
    }

    public void handleCGI(EnvMap env, InputStream stdin, OutputStream stdout, AppRequest ar) {
        RuntimeEnvironment runenv = RuntimeEnvironmentPool.getInstance(ar.getContext());
        try {
            runenv.setup(ar.getScope(), stdin, stdout);
            runenv.addCGIEnv(env);
            runenv.commonRun(getAST());
        }
        catch (IOException e) {
            System.err.println("RubyCGIAdapter.handle: " + e);
        }
        finally {
            RuntimeEnvironmentPool.releaseInstance(runenv);
        }
    }

    protected synchronized Node getAST() throws IOException {
        final long lastModified = file.lastModified();
        if (node == null || lastCompile < lastModified) {
            node = parseContent(file.getPath());
            lastCompile = lastModified;
        }
        return node;
    }

    protected Node parseContent(String filePath) throws IOException {
        return RuntimeEnvironment.parse(getContent(), filePath);
    }
}
