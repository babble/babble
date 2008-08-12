
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

function myprint( s ){
    if ( ! s )
        return print( s );

    s = s + "";
    s = s.replace( /[\r\n\s]+/g , "" );
    print( s );
}

var sales = <sales vendor="John">
    <name>John Smith</name>
    <silly><foo>a</foo></silly>
    <item type="peas" price="4" quantity="6"/>
    <item type="carrot" price="3" quantity="10"/>
    <item type="chips" price="5" quantity="3"/>
    <zzz a="5" />
    <zzz a="6" />
    <zzz a="7" />
    <yyy x="a" />
    </sales>;


myprint( sales.item.length() );
myprint( sales.item.(@type == "carrot") == null );
myprint( sales.item.(@type == "carrot").@quantity );
myprint( sales.@vendor );
myprint( sales.name );

print( "---" );
myprint( sales..@x.length() );
myprint( sales..@price.length() );
myprint( sales..zzz );
