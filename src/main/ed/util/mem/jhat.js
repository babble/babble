
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

JHat = function( host ){
    this._host = host;
}

JHat.prototype._loadPage = function( page ){
    var x = XMLHttpRequest( "GET" , this._host + page );
    if ( ! x.send() )
	throw "error loading page";
    return x.responseText;
}

JHat.prototype._findSection = function( everything , section ){
    var sections = everything.split( "<h2>" );
    var found = null;
    for each ( s in sections ){
	if ( s.indexOf( section ) < 0 )
	    continue;
	if ( found )
	    throw "multiple matches for [" + section + "]";
	found = s;
    }
    if ( ! found )
	throw "can't find section [" + section + "]";
    
    return found;
}

JHat.prototype.referenceToThisClass = function( id ){
    return this.parseReferencesFromPage( "class/" + id );
}

JHat.prototype.referenceToThisObject = function( id ){
    return this.parseReferencesFromPage( "object/" + id );
}

JHat.prototype.parseReferencesFromPage = function( page ){
    var txt = this._loadPage( page );
    var section = this._findSection( txt , "References to this object" );
    
    var p = /<a href="(.*?)">(.*)<\/a>/g;
    var r = null;
    
    var all = [];
    
    while ( r = p.exec( section ) ){
	var link = { href : r[1].substring(3) };
	link.name = r[2];
	
	var foo = r[1].substring( 2 );
	assert( foo[0] == "/" );
	foo = foo.substring(1);
	foo = foo.split( "/" );
	assert( foo.length == 2 );
	link.type = foo[0];
	link.id = foo[1];
	
	all.add( link );
    }
    return all;
}


JHat.prototype.getInstancesOfClass = function( id ){
    var txt = this._loadPage( "allInstances/" + id );

    var p = /<a href="(.*?)">(.*)<\/a>/g;
    var r = null;
    
    var all = [];
    
    while ( r = p.exec( txt ) ){    
	var link = { href : r[1].substring(3) };
	link.name = r[2];
	
	var foo = r[1].substring( 2 );
	assert( foo[0] == "/" );
	foo = foo.substring(1);
	foo = foo.split( "/" );
	assert( foo.length == 2 );
	link.type = foo[0];
	link.id = foo[1];
	
	all.add( link );
    }
    
    return all;
}

JHat.prototype.findAllObjectsIdsInTree = function( page ){
    var l;
    
    var all = {};
    
    var toVisit = [];
    var visited = {};
    
    toVisit.add( page );
    while ( toVisit.length ){
	
	var next = toVisit.shift();
	
	if ( visited[next] )
	    continue;
	visited[next] = true;

	for each ( l in this.parseReferencesFromPage( next ) ){
	    if ( all[l.id] )
		continue;
	    all[l.id] = true;
	    toVisit.add( l.href )
	}

    }

    return all;
}


JHat.prototype.linksToMapOfId = function( links ){
    var all = {};
    for each ( l in links ){
	all[l.id] = true;
    }
    return all;
}
