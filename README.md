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
var_stmt           = "var", identifier, ["=", rval] ,";";
func_def           = "def", identifier, "(", [argument, {",", argument }], ")", code_block;
class_def          = "class", class_identifier, class_body;
cond_stmt          = "if", "(", condition, ")", code_block, ["else", code_block];
while_stmt         = "while", "(", condition, ")", code_block;
for_stmt           = "for", "(", identifier, "in", (identifier | fun_call_stmt), ")", code_block;
switch_stmt        = "switch", "(", (type | class_identifier) , ")", ;

program            = { func_def | class_def | comment };
code_block         = "{", { non_ret_stmt | ["return"], stmt, ";" }, "}";
argument           = ["ref"], identifier;
stmt               = fun_call_stmt | identifier | condition;
non_ret_stmt       = var_stmt | assign_stmt | cond_stmt | while_stmt | for_stmt | switch_stmt;
class_body         = "{", { func_def | var_stmt } , "}";
fun_call_stmt      = identifier, (", [stmt, {",", stmt }], ")";
condition          = rval, cond_operator, rval;
assign_stmt        = identifier, "=", rval, ";";
rval               = ["(", type, ")"], (constant | fun_call_stmt | identifier | condition);
```

Konwencje leksykalne:

```
letter             = "a-z" | uppercase_letter;
uppercase_letter   = "A-Z";
digit              = "0" | positive_digit;
positive_digit     = "1-9";
identifier         = letter, { letter | digit};
class_identifier   = uppercase_letter, { letter | digit };
constant           = integer_const | float_const;
integer_const      = positive_digit, { digit };
float_const        = positive_digit, { digit }, ".", { digit };
cond_operator      = "==" | "!=" | "<" | "<=" | ">" | ">=";
type               = "int" | "float";
comment            = "//", { inline_char }, newline;
inline_char        = letter | digit | special_character | inline_whitespace;
inline_whitespace  = " " | "\t";
whitespace         = inline_whitespace | newline;
special_character  = "*" | "$" | "$" | "," | "." | ";" | ":" (* etc... *);
newline            = "\n" | "\r\n" | "\r" | "\n\r";
```

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
    a++;
} else {
    a = (a + 1 ) * 2;
}
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
while(a--) {
    print(a);
}
```

### Funkcje

```js
def fun(x) {
    if(x==0) return x;
    return x * fun(x-1);
}

def fun2(ref x){
    x++;
}

def main(){ // <- this is the entry point of an application
    var res1 = fun(5);
    var res2 = fun(a); // pass copy

    var res3 = fun2(a); // pass reference
    var res4 = fun2(5); // error
}

```

### Listy

```js
var lst = list(1, 2, 3);
var lst2 = list(1, 2.0, 3); // homogeneous list
```

### Pętla for-in

```js
var lst = list(1, 2, 3);

for(el in lst){
    print(el);
}

for(el in lst){
    if (el % 2 == 0){
        continue;
    }
    if (el % 2 == 0){
        break;
    }
}
```

### Switch (pattern matching na typie)

```js
var lst = list(1, 2.0, 3);
for(el in lst){
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

    def init(radius){
        r = radius;
    }

    def printRadius(){
        print(r);
    }

}

var circle = new Circle(5);
print(circle.r);
circle.printRadius();
```
