; Source: https://github.com/alex-pinkus/tree-sitter-swift

; Punctuation
 [
  "\""
  ";"
  ":"
  ","
 ] @punctuation.delimiter

 [
  "("
  ")"
  "["
  "]"
  "{"
  "}"
 ] @punctuation.bracket

 ; Identifiers
 (type_identifier) @type

 [
  (self_expression)
  (super_expression)
 ] @variable.builtin

 ; Declarations
 [
  "func"
  "deinit"
 ] @keyword.function

 [
  (visibility_modifier)
  (member_modifier)
  (function_modifier)
  (property_modifier)
  (parameter_modifier)
  (inheritance_modifier)
  (mutation_modifier)
 ] @keyword.modifier

 (simple_identifier) @variable

 (function_declaration
  (simple_identifier) @function.method)

 (protocol_function_declaration
  name: (simple_identifier) @function.method)

 (init_declaration
  "init" @constructor)

 (parameter
  external_name: (simple_identifier) @variable.parameter)

 (parameter
  name: (simple_identifier) @variable.parameter)

 (type_parameter
  (type_identifier) @variable.parameter)

 ; Keywords
 [
  "protocol"
  "extension"
  "indirect"
  "nonisolated"
  "override"
  "convenience"
  "required"
  "some"
  "any"
  "weak"
  "unowned"
  "didSet"
  "willSet"
  "subscript"
  "let"
  "var"
  (throws)
  (where_keyword)
  (getter_specifier)
  (setter_specifier)
  (modify_specifier)
  (else)
  (as_operator)
 ] @keyword

 [
  "enum"
  "struct"
  "class"
  "typealias"
 ] @keyword.type

 [
  "async"
  "await"
 ] @keyword.coroutine

 ; Control Flow
 (for_statement "for" @keyword.repeat)
 (for_statement "in" @keyword.repeat)
 ["while" "repeat" "continue" "break"] @keyword.repeat
 (guard_statement "guard" @keyword.conditional)
 (if_statement "if" @keyword.conditional)
 (switch_statement "switch" @keyword.conditional)
 (switch_entry "case" @keyword)
 (switch_entry "fallthrough" @keyword)
 (switch_entry (default_keyword) @keyword)
 "return" @keyword.return
 (ternary_expression ["?" ":"] @keyword.conditional.ternary)

 ; Exception Handling
 [(try_operator) "do" (throw_keyword) (catch_keyword)] @keyword.exception

 ; Import
 (import_declaration "import" @keyword.import)

 ; Function Calls
 (call_expression (simple_identifier) @function.call)
 (call_expression (navigation_expression (navigation_suffix (simple_identifier) @function.call)))
 (call_expression (prefix_expression (simple_identifier) @function.call))

 ; Comments
 [(comment) (multiline_comment)] @comment

 ((comment) @comment.documentation
  (#match? @comment.documentation "^///[^/]"))

 ((comment) @comment.documentation
  (#match? @comment.documentation "^///$"))

 ((multiline_comment) @comment.documentation
  (#match? @comment.documentation "^/[*][*][^*].*[*]/$"))

 ; Strings
 (line_str_text) @string
 (str_escaped_char) @string.escape
 (multi_line_str_text) @string
 (raw_str_part) @string
 (raw_str_end_part) @string
 (regex_literal) @string.regexp

 ; Numbers
 [(integer_literal) (hex_literal) (oct_literal) (bin_literal)] @number
 (real_literal) @number.float

 ; Booleans & Literals
 (boolean_literal) @boolean
 "nil" @constant.builtin

 ; Operators
 [
  "+"
  "-"
  "*"
  "/"
  "%"
  "="
  "+="
  "-="
  "*="
  "/="
  "<"
  ">"
  "<<"
  ">>"
  "<="
  ">="
  "++"
  "--"
  "^"
  "&"
  "&&"
  "|"
  "||"
  "~"
  "%="
  "!="
  "!=="
  "=="
  "==="
  "?"
  "??"
  "->"
  "..<"
  "..."
  (bang)
 ] @operator
