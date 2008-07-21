
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

try {
    throw 5;
}
catch ( e if e == 5  ){
    print( e );
}



try {
    throw 5;
}
catch ( e if e == 6 ){
    print( "a" );
}
catch ( e if e == 5 ){
    print( "b" );
}



try {
    throw 5;
}
catch ( e if e == 5 ){
    print( "c" );
}
catch ( e if e > 2 ){
    print( "d" );
}


try {
    throw 5;
}
catch ( e if e == 6 ){
    print( "e" );
}
catch ( e ){
    print( "f" );
}


try {
    try {
        throw 5;
    }
    catch ( e if e == 6 ){
        print( "blah1" );
    }
}
catch ( foo ){
    print( "blah2" );
}
