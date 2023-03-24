# TKOM — Interpretowany język programowania

Projekt z przedmiotu Techniki Kompilacji

Spis treści:

[[_TOC_]]

## Cechy języka

* Dynamiczny
* Obiektowy
* Silnie typowany

## Gramatyka

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

### Warunki: if, elif, else

```js
if(a == 4) {
    a++;
} elif (a == 3) { 
    a = a ** 2;
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
    print("aa");
}

var b = 5;
while(b--) {
    if(b == 4) { continue; }
    if(b == 2) { break; }
}
```

### Funkcje

```js
def fun(x) {
	if(x==0) return x;
	return x * fun(x-1);
}

var res1 = fun(5);
var res2 = fun(a); // pass copy

def fun2(ref x){
    x++;
}

var res3 = fun2(a); // pass reference
var res4 = fun2(5); // error
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

    Circle(radius){
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
