
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
    print("a");
}

if ( 5 == 4 ){
    print( "1" );
    function foo(){
        print( "b" );
    }
}

foo();



function bar(){
    print("1");
}    

bar();

function bar(){
    print( "2" );
}



function good(){
    print( "g1" );
}

good();

if ( 5 == 5 ){
    function good(){
        print( "g2" );
    }
}

good();

function good(){
    print( "g3" );
}

good();
