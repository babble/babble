djang10 = djang10 || {};


var TokenTypes =
    djang10.TokenTypes = {
        
    TOKEN_TEXT : 0,
    TOKEN_VAR : 1,
    TOKEN_BLOCK : 2,
    TOKEN_COMMENT : 3
};


var Parser =
    djang10.Parser =
    function(tokens) {
        
        this.tokens = tokens;
        this.tags = {};
        this.filters = {};
};
Parser.prototype = {
    parse: function(parse_until) {
        parse_until = parse_until || [];
        var nodelist = this.create_nodelist();
        
        while(tokens.length > 0) {
            var token = this.next_token();
            
            if(token.token_type == TokenTypes.TOKEN_TEXT) {
                this.extend_nodelist(nodelist, new TextNode(token.contents), token);
            }
            else if(token.token_type == TokenTypes.TOKEN_VAR) {
                if(token.contents == "")
                    this.empty_variable(token);
                var filter_expression = this.compile_filter(token.contents);
                var var_node = this.create_variable_node(filter_expression);
                this.extend_nodelist(nodelist, var_node, token);
            }
            else if(token.token_type == TokenTypes.TOKEN_BLOCK) {
                if(parse_until.indexOf(token.contents) > -1) {
                    this.prepend_token(token);
                    return nodelist;
                }
                
                var command = token.contents.split(/\s/, 1)[0];
                if(!command)
                    this.empty_block_tag(token, command);
                
    
                this.enter_command(command, token);
    
                var compile_func = this.tags[command];
                if(compile_func == null)
                this.invalid_block_tag(token, command);
                
                var compiled_result = null;
                try {
                    compiled_result = compile_func(token);
                } catch(e) {
                    if(!this.compile_function_error(token, e))
                        throw e;
                }
                
                this.extend_nodelist(nodelist, compiled_result, token);
                this.exit_command();
            }
        }
        
        if(parse_until.length > 0)
            this.unclosed_block_tag(parse_until);
        
        return nodelist;
    },
    
    skip_past: function(endtag) {
        while(tokens.length > 0) {
            var token = this.next_token();
            if(token.token_type == TokenTypes.TOKEN_BLOCK && token.contents == endtag)
                return;
        }
        this.unclosed_block_tag([endtag]);
    },
    
    create_variable_node: function(filter_expression) {
        return new VariableNode(filter_expression);
    },
    
    create_nodelist: function() {
        return new NodeList();
    },
    
    extends_nodelist: function(nodelist, node, token) {
        if(node.must_be_first && nodelist != null && nodelist.length > 0) {
            if(nodelist.contains_nontext)
                throw new TemplateSyntaxError(node + "must be the first tag in the template");
        }
        if( (nodelist instanceof NodeList) && !(node instanceof TextNode) )
            nodelist.contains_nontext = true;
        
        nodelist.append(node);
    },
    
    enter_command: function(command, token) {
        //nooop
    },
    
    exit_command: function() {
        //noop
    },
    
    error: function(token, msg) {
        return new TemplateSyntaxError(msg);
    },
    
    empty_variable: function(token) {
        throw this.error(token, "Empty variable tag");
    },
    
    empty_block_tag: function(token) {
        throw this.error(token, "Empty block tag");
    },
    
    invalid_block_tag: function(token, command) {
        throw this.error(token, "Invalid block tag: " + command);
    },
    
    compile_function_error: function(token, e) {
        return false;
    },
    
    next_token: function() {
        return this.tokens.pop();
    },
    
    prepend_token: function(token) {
        this.tokens.push(token);
    },
    
    delete_first_token: function() {
        this.next_token();
    }


