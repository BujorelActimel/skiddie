; Comments
(line_comment) @comment

; Strings
(string_literal) @string

; Numbers
(integer_literal) @number
(real_literal) @number

; Keywords
"fun" @keyword
"val" @keyword
"var" @keyword
"class" @keyword
"object" @keyword
"interface" @keyword
"enum" @keyword
"if" @keyword
"else" @keyword
"when" @keyword
"for" @keyword
"while" @keyword
"return" @keyword
"import" @keyword
"package" @keyword
"try" @keyword
"catch" @keyword
"finally" @keyword
"throw" @keyword
"break" @keyword
"continue" @keyword
"do" @keyword
"as" @keyword
"in" @keyword
"is" @keyword
"data" @keyword
"sealed" @keyword
"abstract" @keyword
"open" @keyword
"override" @keyword
"private" @keyword
"public" @keyword
"protected" @keyword
"internal" @keyword
"companion" @keyword
"suspend" @keyword
"inline" @keyword
"infix" @keyword
"operator" @keyword

(function_declaration
  (simple_identifier) @function)

(type_identifier) @type

(simple_identifier) @variable
