
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

var r = /the/g;
print( r );
print( "abe and the cow".match( r ) );
print( "abe and th cow".match( r ) );
print( "abe and the cow".match( "the.[abcow]\+" ) );

print( "foo the the the cow".replace( /the/ , "___" ) );
print( "foo the the the cow".replace( /the/g , "___" ) );

print( "the 123 food".match( /\d+/ ) );
print( typeof ( "the 123 food".match( /\d+/ ) ) );
print( "the 123 food".replace( /(\d+)/ , "__$_$1__") );


print( "the 123 food".replace( /(\d+)/ ,
                               function( $0 ){
                                   return "* " + $0 + " *" ;
                               }
                               )
       );


print( "===a===".replace( /(?:^|\xB6)(={1,6})(.*?)[=]*(?=\xB6|$)/g,
                          function($0,$1,$2){
                              var h=$1.length;
                              return "<h"+h+">"+$2+"</h"+h+">";
                          } ) );


print( "H4" );
print( /123/.test( "the 123 is here" ) );
print( /.*123.*/.test( "the 123 is here" ) );
print( /123/.test( "the 12 is here" ) );
print( /(is)/.test( "the 12 is here" ) );

print( "the 123 is here".match(/123/).index );
print( /123/.exec( "the 123 is here" ).index );

print( "H5" );
p = /(\d)\.(\d)/g;
t = "abc 123.23 sj 41.2";
while ( result = p.exec( t ) ){
    print( result.index + " : " + result[0] + "," + result[1] + "," + result[2] );
}

ary = "a=".split("=");
print(ary[1] == null);

print( ".*+?^=!:${}()|]/\\".replace( /([.*+?^=!:${}()|[\]\/\\ ])/g , "\\$1" ) );

print("abc".match(/(a)(b)/));
print("abc ab".match(/ab/g));
print("abc".match(/(a)(b)/g));
