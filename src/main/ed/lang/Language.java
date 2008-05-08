// Language.java

package ed.lang;

public enum Language {

    JS , RUBY , PHP;
    
    public static Language find( String file ){
        
        final int idx = file.lastIndexOf( "." );

        final String extension;
        if ( idx >= 0 )
            extension = file.substring( idx + 1 );
        else
            extension = file;
        
        if ( extension.equals( "js" )
             || extension.equals( "jxp" ) )
            return JS;

        if ( extension.equals( "rb" )
             || extension.equals( "rhtml" ) )
            return RUBY;

        if ( extension.equals( "php" ) )
            return PHP;
        
        return JS;
    }
        
}
