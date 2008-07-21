

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

function foo(){
    return this.a;
}

print( foo.call( { a : 5 } ) );

function foo2( z ){
    return this.a + z;
}

print( foo2.call( { a : 5 } , 3 ) );

function bar(a, b, c){
    print(a);
    print(b);
    print(c);
    return(this.a);
}

print( bar.call( { a: 5}, 1, 2, 3));

print( bar.apply( {a: 5}, [1, 2, 3]));


function bar2(a, b, c){
    print(a == null);
    print(b == null);
    print(c == null);
}

bar2.apply( {a: 5}, [1]);
