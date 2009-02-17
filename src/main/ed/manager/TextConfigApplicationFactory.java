// TextConfigApplicationFactory.java


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

package ed.manager;

import java.io.*;
import java.util.*;

import ed.log.*;
import ed.util.*;

public class TextConfigApplicationFactory extends ConfigurableApplicationFactory {

    TextConfigApplicationFactory( File f ){
        super( Long.MAX_VALUE );
        _file = f;
    }

    protected SimpleConfig getConfig()
        throws IOException {
        return TextSimpleConfig.read( _file );
    }

    final File _file;
}
