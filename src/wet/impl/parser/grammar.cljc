(ns wet.impl.parser.grammar)

(def GRAMMAR "
  (* COMMON *)

  template ::= (b / raw-block / object-expr-block / ltag tag-expression rtag)*
  <body> ::= template
  b ::= #'(?s)((?!\\{\\{|\\{\\%).)*'
  s ::= #'[\\s\\n\\r]*'
  trimbackward ::= <'-'>
  trimforward ::= <'-'>
  <ltag> ::= <'{%'> trimbackward? <s>
  <rtag> ::= <s> trimforward? <'%}'>
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
  <object-expr-block> ::= <'{{'> trimbackward? object-expr trimforward? <'}}'>

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
  render ::= <'render '> <s> string params
  comment ::= <'comment'> rtag body? ltag <'endcomment'>

  (* VARIABLES *)

  assign ::= <'assign '> <s> token <s> <'='> <s> object-expr
  increment ::= <'increment '> <s> token
  decrement ::= <'decrement '> <s> token
  capture ::= <'capture '> <s> token rtag
              body
              ltag <'endcapture'>

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

  case ::= <'case '> object rtag when+ else? ltag <'endcase'>
  if ::= <'if '> <s> predicate rtag body elsif* else? ltag <'endif'>
  unless ::= <'unless '> <s> predicate rtag body elsif* else? ltag <'endunless'>
  elsif ::= ltag <'elsif '> <s> predicate rtag body
  when ::= ltag <'when '> object rtag body
  else ::= ltag <'else '> <s> rtag body

  (* ITERATION *)

  break ::= <'break'>
  continue ::= <'continue'>

  range-start ::= int | lookup
  range-end ::= int | lookup
  range ::= <'('> range-start <'..'> range-end <')'>

  for-limit ::= <s> <'limit:'> <s> int <s>
  for-offset ::= <s> <'offset:'> <s> int <s>
  for-reversed ::= <s> <'reversed'> <s>
  for-opts ::= (for-limit |for-offset | for-limit for-offset | for-offset for-limit)? for-reversed?

  for ::= <'for '> token <s> <'in '> (object / range) for-opts?
          rtag
          body
          ltag <'endfor'>
")