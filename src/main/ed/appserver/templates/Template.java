// Template.java

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

package ed.appserver.templates;

import ed.lang.*;
import ed.appserver.*;

public class Template {

    public Template( String name , String content , Language sourceLanguage ){
        _name = name;
        _content = content;
        _extension = MimeTypes.getExtension( _name );
        _sourceLanguage = sourceLanguage;
    }
    
    public String getName(){
        return _name;
    }

    public String getExtension(){
        return _extension;
    }
    
    public String getContent(){
        return _content;
    }

    public Language getSourceLanguage(){
        return _sourceLanguage;
    }

    final String _name;
    final String _content;
    final String _extension;
    final Language _sourceLanguage;
}
