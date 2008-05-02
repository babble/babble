find . -name ".git" -prune -o -name tinymce -prune -o -not -name "*~" -print | xargs grep "`echo -ne "\\t"`" | less
