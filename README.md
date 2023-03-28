# TKOM — Interpretowany język programowania

Projekt z przedmiotu Techniki Kompilacji

Spis treści:

[[_TOC_]]

## Cechy języka

* Dynamiczny
* Obiektowy
* Silnie typowany

## Gramatyka

Składnia:

```
var_stmt           = "var", identifier, ["=", expr], ";";
func_def           = "def", identifier, "(", [parameters], ")", code_block;
class_def          = "class", class_id, class_body;
cond_stmt          = "if", "(", expr, ")", code_block, ["else", code_block];
while_stmt         = "while", "(", expr, ")", code_block;
for_stmt           = "for", "(", identifier, "in", expr, ")", code_block;
switch_stmt        = "switch", "(", (expr), ")", "{", { (type | class_id), "->", code_block } ,"}";

program            = { func_def | class_def };
code_block         = "{", { non_ret_stmt | ["return"], expr, ["=", expr], ";" }, "}";
parameters         = identifier, {",", identifier };
non_ret_stmt       = var_stmt | cond_stmt | while_stmt | for_stmt | switch_stmt;
class_body         = "{", { func_def | var_stmt }, "}";
fun_call_stmt      = identifier, "(", [args], ")";
class_init         = "new", class_id, "(", [args], ")";
args               = expr, {",", expr }
obj_method         = (identifier | fun_call_stmt), { ".", fun_call_stmt }
expr               = or_op_arg, { "or", or_op_arg };
or_op_arg          = and_op_arg, { "and", and_op_arg };
and_op_arg         = cond_op_arg, [cond_operator, cond_op_arg];
cond_op_arg        = term, { add_op, term };
term               = factor, {mult_op, factor};
factor             = ["not"], (factor_inner | "(", expr, ")"), ["as", (type | class_id)];
factor_inner       = constant | fun_call_stmt | obj_method | identifier | string | class_init;
```

Konwencje leksykalne:

```
letter             = "a-z" | uppercase_letter;
uppercase_letter   = "A-Z";
digit              = "0" | positive_digit;
positive_digit     = "1-9";
identifier         = letter, { letter | digit };
class_id           = uppercase_letter, { letter | digit };
constant           = integer_const | float_const;
integer_const      = positive_digit, { digit };
float_const        = positive_digit, { digit }, ".", { digit };
cond_operator      = "==" | "!=" | "<" | "<=" | ">" | ">=";
add_op             = "+" | "-"
mult_op            = "*" | "/"
type               = "int" | "float";
comment            = "//", { inline_char }, newline;
inline_char        = letter | digit | special_character | inline_whitespace;
inline_whitespace  = " " | "\t";
whitespace         = inline_whitespace | newline;
string             = """, { inline_char |  }, """;
special_character  = "*" | "$" | "$" | "," | "." | ";" | ":" (* etc... *);
newline            = "\n" | "\r\n" | "\r" | "\n\r";
```

## Priorytety operatorów

1. ()
2. .
3. not
4. as
5. \* /
6. \+ \-
7. < <= \> \>= == !=
8. and
9. or
10. =


## Przykłady konstrukcji języka

### Komentarze

```js
// comment
var a = 5; // comment
```

### Tworzenie zmiennych / przypisanie

```js
var a = 1;
a = 2;
a = 3.0; // dynamic type
```

### Warunki: if, else

```js
if(a == 4) {
    a = a + 1 * 2;
} else {
    a = (a + 1 ) * 2;
}
```

### Operatory logiczne

* a `and` b — prawda tylko dla a = b = 1. W pozostałych wypadkach fałsz.
* a `or` b — fałsz tylko dla a = b = 0. W pozostałych wypadkach prawda.

Operator `and` ma wyższy priorytet niż `or`. Oba te operatory są łączne.

```js
var t = true;
var f = false;
var res;
res = t and f; // res == false
res = t or f; // res == true
res = t and t and t; // res == true
res = t and f == f or f; // res == true
res = t and t or t and f == (t and t) or (t and f) // res == true
```

### Rzutowanie typów

```js
var a = 2.0;
var b = 2;
var isEq = (int)a == b;
```

### Pętla while

```js
var a = 5;
while(a > 0) {
    print(a);
    a = a - 1;
}
```

### Funkcje

```js
def f1(x, y, z){
    if(x == y) { return 1.0; }
    if(x == z) { return 1; }
    return z;
}

def fun(x) {
    x = x + 1;
    return x;
}

def main() { // <- this is the entry point of an application
    var res;
    var x = 5;
    res = fun(5);     // res = 6, x = 5
    res = fun(x);     // res = 6, x = 5
    res = fun(ref x); // res = 6, x = 6
    return 0;
}

```

### Listy

```js
var lst = list(1, 2, 3);
var lst2 = list(1, 2.0, 3); // non-homogeneous list
```

### Pętla for-in

```js
var lst = list(1, 2, 3);

for(el in lst) {
    print(el);
}

for(el in list(1, 2)) {
    print(el);
}

```

### Switch (pattern matching na typie)

```js
var lst = list(1, 2.0, 3);
for(el in lst) {
    switch(el) {
        int -> { print("integer"); }
        float -> { print("float"); }
    }
}
```

### Klasy

```js
class Circle {
    var r;

    def init(radius) {
        r = radius;
    }

    def printRadius() {
        print(r);
    }

}

var circle = new Circle(5);
circle.printRadius();

def circle_builder(){
    return new Circle(5);
}

circle_builder().printRadius();

class CircleWrapper {
    var circle;
    
    def init(r){
        circle = new Circle(r);
    }
    
    def newCircle(r){
        return new Circle(r);
    }
}

var circleWrapper = new CircleWrapper(2);
circleWrapper.newCircle(1).printRadius(); // 1
circleWrapper.circle.printRadius(); // 2
circleWrapper.circle = new Circle(6) // error, class properties are read only
```
