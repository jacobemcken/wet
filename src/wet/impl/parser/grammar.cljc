(ns wet.impl.parser.grammar)

(def GRAMMAR "
  (* COMMON *)

  template ::= (b / raw-block / object-expr-block / tag-expression)*
  <body> ::= template
  b ::= #'(?s)((?!\\{\\{|\\{\\%).)*'
  s ::= #'[\\s\\n\\r]*'
  <ltag> ::= <'{%'> <s>
  <rtag> ::= <s> <'%}'>
  dquote ::= '\"'
  squote ::= '\\''
  <lparen> ::= <s> '(' <s>
  <rparen> ::= <s> ')' <s>
  <lbrace> ::= <s> '[' <s>
  <rbrace> ::= <s> ']' <s>

  raw-body ::= #'(?s)((?!\\{\\%\\s*endraw\\s*%}).)*'
  raw-block ::= ltag <'raw'> rtag raw-body ltag <'endraw'> rtag

  <token> ::= #'[a-zA-Z-_][a-zA-Z0-9-_]*'
  bool ::= 'true' | 'false'
  int ::= #'-?\\d+'
  float ::= #'-?\\d+\\.\\d+'

  (* STRINGS *)

  sq-str-set ::= #'((?!\\\\\\')[^\\'])*'
  sq-str-escape ::= #'\\\\*\\\\\\''
  <sq-string> ::= <squote> (sq-str-escape* sq-str-set)* <squote>

  dq-str-set ::= #'((?!\\\\\\\")[^\"])*'
  dq-str-escape ::= #'\\\\*\\\\\"'
  <dq-string> ::= <dquote> (dq-str-escape* dq-str-set)* <dquote>

  string ::= sq-string | dq-string

  index ::= <lbrace> (int | string | lookup) <rbrace> | <'.'> token
  lookup ::= token index*

  <object> ::= bool / int / float / string / lookup

  object-expr ::= <s> object <s> filter* <s>
  <object-expr-block> ::= <'{{'> object-expr <'}}'>

  filter ::= <'|'> <s> (token | token <s> <':'> <s> args) <s>
  <args> ::= object (<s> <','> <s> object)*

  <tag-expression> ::= assign
                     | break
                     | capture
                     | case
                     | comment
                     | continue
                     | decrement
                     | for
                     | if
                     | increment
                     | render
                     | unless

  (* TEMPLATES *)
  params ::= (<s> <','> <s> token <':'> <s> object)*
  render ::= ltag <'render '> <s> string params rtag
  comment ::= ltag <'comment'> rtag body? ltag <'endcomment'> rtag

  (* VARIABLES *)

  assign ::= ltag <'assign '> <s> token <s> <'='> <s> object-expr <s> rtag
  increment ::= ltag <'increment '> <s> token rtag
  decrement ::= ltag <'decrement '> <s> token rtag
  capture ::= ltag <'capture '> <s> token rtag
              body
              ltag <'endcapture'> rtag

  (* PREDICATES *)

  operator ::= '==' | '!=' | '>' | '<' | '>=' | '<=' | 'contains'
  empty ::= <'empty'>

  or_  ::= <s> 'or' <s>
  and_ ::= <s> 'and' <s>
  assertion ::= object | object <s> operator <s> (empty / object)
  or ::= and (<or_> and)*
  and ::= predicate (<and_> predicate)*
  predicate ::= or | <lparen> or <rparen> | assertion

  (* CONTROL FLOW *)

  case ::= ltag <'case '> object rtag when+ else? ltag <'endcase'> rtag
  if ::= ltag <'if '> <s> predicate rtag body elsif* else? ltag <'endif'> rtag
  unless ::= ltag <'unless '> <s> predicate rtag body elsif* else? ltag <'endunless'> rtag
  elsif ::= ltag <'elsif '> <s> predicate rtag body
  when ::= ltag <'when '> object rtag body
  else ::= ltag <'else '> <s> rtag body

  (* ITERATION *)

  break ::= ltag <'break'> rtag
  continue ::= ltag <'continue'> rtag

  range-start ::= int | lookup
  range-end ::= int | lookup
  range ::= <'('> range-start <'..'> range-end <')'>

  for-limit ::= <s> <'limit:'> <s> int <s>
  for-offset ::= <s> <'offset:'> <s> int <s>
  for-reversed ::= <s> <'reversed'> <s>
  for-opts ::= (for-limit |for-offset | for-limit for-offset | for-offset for-limit)? for-reversed?

  for ::= ltag <'for '> token <s> <'in '> (object / range) for-opts?
          rtag
          body
          ltag <'endfor'> rtag
")