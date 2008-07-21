
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

x = {}

try {
    print( x.a.b );
}
catch ( e ){
    assert( e.getStackTrace() );
}

try {
    throw 5;
}
catch ( e ){
    assert( 5 == e );
    assert( scope.currentException() );
    assert( scope.currentException().getStackTrace() );
    assert( scope.currentException().getMessage() == "5" );
}


    

try {
    throw 5;
}
catch ( e ){
    assert( 5 == e );
    assert( scope.currentException() );
    assert( scope.currentException().getStackTrace() );
    assert( scope.currentException().getMessage() == "5" );

    try {
        throw 9;
    }
    catch ( z ){
        assert( z == 9 );
        assert( scope.currentException().getMessage() == "9" );
    }

    assert( 5 == e );
    assert( scope.currentException() );
    assert( scope.currentException().getStackTrace() );
    assert( scope.currentException().getMessage() == "5" );
}


    
