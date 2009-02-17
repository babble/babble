// OneTimeApplicationFactory.java

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

import java.util.*;

public class OneTimeApplicationFactory implements ApplicationFactory {

    OneTimeApplicationFactory( Application ... apps ){
        _apps = new LinkedList<Application>();
        for ( Application a : apps )
            _apps.add( a );
    }

    public String textView(){
        return _apps.toString();
    }
    
    public List<Application> getApplications(){
        return _apps;
    }
    
    public long timeBetweenRefresh(){
        return Long.MAX_VALUE;
    }

    public boolean runGridApplication(){
        return false;
    }

    final List<Application> _apps;
}
