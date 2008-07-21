
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

function eval( host , type , domain ){
    print( "host: " + host + " type: " + type );
    
    if ( type == "A" || type == "CNAME" ){
        add( host , "A" , 30 , local );
    }

    add( domain , "NS" , 7200 , "ns1.10gen.com." );
    add( domain , "NS" , 7200 , "ns2.10gen.com." );

}
