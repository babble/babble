// OneTimeApplicationFactory.java

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
