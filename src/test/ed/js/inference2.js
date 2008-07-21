
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

function good( num ){
    var x = 6;
    for ( var i=0; i<num ; i += 2 ){
        x += 2;
    }
    return x;
}

function bad( num ){
    var x = 6;
    var i = "abc"
    for ( var i=0; i<num; i += 2 ){
        x += 2;
    }
    return x;
}

var num = 60000;
var numCalls = 10;

assert( good( num ) == bad( num ) );

print( "inf2" );
for ( var i=0; i<5; i++ ){

    var a = Date.timeFunc( good , numCalls , num );
    var b = Date.timeFunc( bad , numCalls , num );

    b += Date.timeFunc( bad , numCalls , num );
    a += Date.timeFunc( good , numCalls , num );

    if ( i <= 1 )
        continue; // for jit
    
    print( "\t good: " + a  + " bad: " + b );
    //assert( a < b , "too slow");
}
