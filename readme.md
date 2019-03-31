λ calculus evaluator
===

Base evaluator and command line REPL* for lambda calculus

---
\* Coming soon

## Syntax

Supports standard lambda notation:

- x - variables
- λx.M - abstractions
- (M N) - applications

### Variables

Variable name can be either *one* lowercase 'a'..'z' or list of uppercase 'A'..'Z' characters.

`x`, `y`, `z`, `ID`, `TRUE` and `FALSE` are all valid identifiers.

`x1`, `yD` or `FalSE` are not. In fact `FalSE` will be parsed as sequence of applications `(F)(a)(l)(SE)` 

### Abstractions

λ can be replaced with `\\`. Abstractions of form `λxy.(...)` will be automatically
normalized to `λx.λy.(...)`.

It is highly recommended to enclose abstractions in parenthesis to avoid confusing parser.

### Applications

Any two terms appearing one after another are parsed as application.

Note that parenthesis are recommended to avoid confusion.

`λx.xy` will be parsed as 

    - Abstraction:
      - parameter: x
      - body:
        - Application:
          - x
          - y

To parse it as Application use parenthesis.

`(λx.x)y` will be parsed as

    - Application:        
        - Abstraction:
          - parameter: x
          - body:
              - x
        - y
        
        
### Evaluation

Evaluator will perform beta-reduction on expression until it is no longer reducible. Evaluator also keeps track of all the reductions, so it is possible to trace back all the steps.

**Assignments**

One can use `:=` to bind abstractions to names. Only lambda functions without free variables can be bound to a name*

    ID := λx.x

Bound abstractions can be used in other expressions, i.e.

    ID(a)
    
will be evaluated to `a`.


Evaluator will automatically perform alpha-conversion and rename conflicting parameters**.
    
---
    
\* To be implemented\
\** To be implemented as well


## Project structure

* lambda-core - lexer, parser and evaluator of lambda expressions
* lambda-repl - CLI-based REPL for lambda expressions*
* lambda-web  - Web-based REPL for lambda expressions**

---
\* To be implemented\
\** To be implemented later