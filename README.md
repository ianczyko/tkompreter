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
switch_stmt        = "switch", "(", (expr), ")", "{", { (type | class_id | "default"), "->", code_block } ,"}";

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

## Przykładowe konstrukcje języka

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
var lst = list(1, 2.0, new Circle(3), new Square(4));
for(el in lst) {
    switch(el) {
        int -> { print("integer"); }
        float -> { print("float"); }
        Circle -> { print("circle"); }
        default -> { print("other type"); }
    }
}

// prints: integer, float, circle, other type

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

## Przykładowe komunikaty błędów

### Brakujący średnik:

```js
// main.tkom
def main(){
    var x = 5;
    var y = 4
    var z = 3;
}
```

Komunikat o błędzie:

```
main.tkom:3:13: error: expected ';':
    var y = 4
             ^--- ';'
```

### Ponowna deklaracja zmiennej

```js
// main.tkom
def main(){
    var counter = 1;
    var counter = 2;
}
```

Komunikat o błędzie:

```
main.tkom:3:5: error: redeclaration of variable 'counter':
    var counter = 2;
        ~~~~~~~ 'counter' previously declared in: main.tkom:2:5
```

### Nieprawidłowy ciąg słów kluczowych

```js
// main.tkom
def main(){
    var x = new new Circle(5);
}
```

Komunikat o błędzie:

```
main.tkom:3:13: error: expected type identifier after 'new' keyword:
    var x = new new Circle(5);
                ~~~
```

### Wyjątek dzielenia przez 0

```js
// main.tkom
def extremeDivision(){
    var a = 1;
    var b = 0;
    var res = a / b;
}

def main(){
    extremeDivision();
}
```

Komunikat o błędzie:

```
main.tkom:4:13: runtime exception: Division by zero exception:
    var res = a / b;
              ~~~~~
    Traceback:
        main.tkom:6:1: main()
        main.tkom:7:1: extremeDivision()
        main.tkom:4:13: a / b
```

## Wymagania Funkcjonalne i Niefunkcjonalne

### Źródło

Wymagania funkcjonalne:
* Czytanie znaków ze źródła strumieniowego (STDIN / plik) znak po znaku
* Obsługa UTF-8 (chińskie znaki, emoji)
* Ujednolicanie znaków nowej linii
    * zwracane jest zawsze '\n' a CRLF również jest akceptowane
    * jeśli pierwsze zostanie wykryte CRLF, pojawienie się linii zakończonej np. LFCR jest błędem
* Śledzenie aktualnej pozycji znaku (dla modułu obsługi błędów)
* Buforowanie N ostatnich znaków (dla modułu obsługi błędów)

Wymagania niefunkcjonalne:
* Leniwe wczytywanie znaków
* Interfejs
  * `getCurrentCharacter()` - pobiera aktualny znak (ostatni pobrany)
  * `fetchCharacter()` - pobiera nowy znak
  * `isNotEOF()` - informuje czy wewnętrzny strumień się nie skończył
* Źródło zawiera wewnętrznie reader strumienia (STDIN / plik)

### Lexer

Wymagania funkcjonalne:
* Budowa tokenów na podstawie kolejnych znaków ze źródła
* Limit długości tokenów (np. identyfikatorów)
* Obsługa escape'owanych znaków wewnątrz stringów np. "abc \t \n \\" "
* Budowanie faktycznych wartości liczbowych stałych liczbowych (nie reprezentacji znakowej)

Wymagania niefunkcjonalne:
* Leniwe tworzenie tokenów
* Interfejs
  * `getToken()` - pobiera aktualny token
  * `getNextToken()` - pobiera następny token
  * (pomocniczo) interfejs (leniwego) strumienia `lexer.stream().filter(...).forEach(out::printLn)`
  * `Token` — zawiera typ tokenu i wartość
    * większość tokenów nie zawiera wartości i jest identyfikowana za pomocą typu (np. '+' -> `Token(type=TokenType.PLUS, value=null)`)
    * wartość posiadają tokeny jak: `IdentifierToken`, `StringToken`, `IntegerToken`, `FloatToken` (pochodne klasy bazowej Token z odpowiednim typem value). 

### Moduł obsługi błędów

Wymagania funkcjonalne:
* Dodawanie błędów do listy błędów
* Wyświetlanie błędów zawierające:
  * (jeśli wejściem jest plik) nazwę pliku 
  * linijkę i kolumnę, w której występuje błąd
  * fragment kodu zawierający błąd
  * wizualne podświetlenie miejsca, w którym występuje błąd
  * w przypadku oczywistych błędów sugestię naprawienia błędu (np. dla braku średnika)

Wymagania niefunkcjonalne:
* Limit błędów, dla których kontynuowana jest praca interpretera
* Interfejs
  * `addError(Error)`
  * `printErrors()`
* Moduł obsługi błędów potrzebuje dostępu do źródła (pobranie pozycji znaku i tekstu w okolicy błędu)

### Analizator składniowy

Przykład jak mogłaby wyglądać końcowa klasa realizująca while:

```java
class WhileExpression extends VoidNode {
    Expression condition;
    CodeBlock codeBlock;
    
    @Override
    public void execute(Context context) {
        while(condition.execute(context)){
            codeBlock.execute(context);
        }
    }
}
```

## Sposób testowania

TODO