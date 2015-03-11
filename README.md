# munge-tout

[![Continuous Integration status](https://travis-ci.org/flybe-dev/munge-tout.svg?branch=master)](http://travis-ci.org/flybe-dev/munge-tout)

A Clojure/Java interop library for converting clojure data structures into
Java object graphs and back again.  Unlike currently available bean maniupulation
libraries, it works with any Java class and allows custom object construction in the
general case, or for a specific object property.  Behaviour can be extended through 
multimethods or provided as configuration.

Supports Java generics and arrays, and has built-in support for Java primitives and the
Collections classes `List` `Map` and `Set`.

[![Clojars Project](http://clojars.org/munge-tout/latest-version.svg)](http://clojars.org/munge-tout)

## Usage

```
(require '[munge-tout.core :refer [from-java to-java]])
```

As a trivial example let us convert an instance of `Point` to a Clojure map.  The location property must be excluded or it will recurse infinitely.

```
(import java.awt.Point)
(def p1 (Point. 0 10))

(from-java p1 {:exclusions [:location]})
=> {:x 0 :y 10}
```

The reverse operation:

```
(to-java Point {:x 10 :y 0})
=> #<Point java.awt.Point[x=10,y=0]>
```

Point is a very simple class, however, it is possible to perform the same conversions between a deep object graph and nested associated Clojure structures. Try it and see.

### Options: from-java

> `:exclusions` list of Java class property names to omit from the map

### Options: to-java
 
 > `:strict-mode` if true, `to-java` will throw an `IllegalArgumentException` when one of the properties in an input map cannot be located in the java class.
 
> `:accessible-hack` breaks encapsulation and sets private properties of the target class.  Use this for example, for classes that do not conform to the bean spec.

> `:ctor` a map of property -> constructor function

TODO how-to override conversions by type, using the ctorm multimethod and Mungeable protocol. 

## License

Copyright © 2015 Flybe Group plc

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
