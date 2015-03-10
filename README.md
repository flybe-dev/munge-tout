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
(import java.awt.Point)
(def p1 (Point. 0 0))
(def p1-map (from-java p1 {:exclusions [:location]}))
(to-java Point (assoc p1-map :x 10))

=> #<Point java.awt.Point[x=10,y=0]>
```

### from-java conf

> `:exclusions` properties of the java class to omit from the map

### to-java conf
 
 > `:strict-mode` if true, `to-java` will throw an `IllegalArgumentException` when one of the properties in an input map cannot be located in the java class.
 
> `:accessible-hack` if true, will break encapsulation and set private properties of the target class.  Use this for example, for classes that do not conform to the bean spec.

## License

Copyright © 2015 Flybe Group plc

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
