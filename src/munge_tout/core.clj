(ns munge-tout.core
  (:import (java.beans Introspector)
           (java.util List ArrayList HashSet Set Map HashMap TreeSet SortedSet))
  (:require [munge-tout.util :refer :all]
            [munge-tout.generics :refer :all]))

(defn leave?
  [v]
  (or (instance? Boolean v)
      (number? v)
      (string? v)))

(declare from-java*)
(defn from-java
  ([jovo]
   (from-java jovo {}))
  ([jovo conf]
   (from-java* jovo conf)))

(defn obj-to-java
  [jovo conf]
  (let [ignorable? (into #{:class} (:exclusions conf))]
    (if (leave? jovo)
      jovo
      (into {}
            (for
              [[k v] (bean jovo) :when (not (ignorable? k))]
              [(keyword (camel-case-to-hyphenated (name k))) (from-java* v conf)])))))

(defprotocol Mungeable (from-java* [jovo conf]))

(extend-protocol Mungeable
  nil
  (from-java*
    [_ _]
    nil)
  Enum
  (from-java*
    [enum _]
    (.name enum))
  Map
  (from-java*
    [m _]
    (into {} m))
  Set
  (from-java*
    [s conf]
    (into #{} (map #(from-java % conf) s)))
  Iterable
  (from-java*
    [s _]
    (map from-java (seq s)))
  Object
  (from-java*
    [jovo conf]
    (obj-to-java jovo conf)))

;; Reverse magic mapping from a map into a java class.

(defn prop-descriptors [clazz]
  (.getPropertyDescriptors (Introspector/getBeanInfo clazz)))

(defn prop-name-munge [m]
  (-> m .getName camel-case-to-hyphenated keyword))

(defn unmunge-keyword [k]
  (-> k str (subs 1)))

(defn unmunge-prop-name [n]
  (-> n unmunge-keyword hyphenated-to-camel-case))

(defn prop-descriptors->setter-map [props]
  (reduce #(assoc %1 (prop-name-munge %2) (.getWriteMethod %2))
          {} props))

(defn get-declared-field
  [cls name]
  (try
    (.getDeclaredField cls name)
    (catch NoSuchFieldException _ nil)))

(defn get-field
  [cls name]
  (try
    (.getField cls name)
    (catch NoSuchFieldException _ nil)))

(defn is-public?
  [field]
  (pos? (bit-or (java.lang.reflect.Member/PUBLIC)
                (.getModifiers field))))

(defn get-all-fields
  "Returns all fields in the class hierarchy."
  [cls]
  (let [super-class (.getSuperclass cls)]
    (if super-class
      (concat (.getDeclaredFields cls) (get-all-fields super-class))
      [])))

(defn find-field
  "Try to find the named field first with the get methods, then searching all available including private fields."
  [cls name]
  (or
    (get-field cls name)
    (get-declared-field cls name)

    (first (filter #(equals-ignore-case name (.getName %)) (get-all-fields cls)))
    nil))


(declare find-ctor
         find-setter)

(defn to-java*
  "Java conversion providing generic type information."
  [type value conf]
  (if-not (nil? value)
    (let [ctor (find-ctor type conf)
          setter (find-setter type conf)]
      (doto (ctor value)
        (setter value)))))

(defn just-set-field! [obj field val conf]
  (when (:accessible-hack? conf)
    (.setAccessible field true))
  (when (or (:accessible-hack? conf)
            (is-public? field))
    (.set field obj
          (to-java* (derive-field-type field) val conf))))

(defn set-field!
  [cls obj k v conf]
  (if-let [field (find-field cls (unmunge-prop-name k))]
    (just-set-field! obj field v conf)
    (if (:strict-mode conf)
      (throw (IllegalArgumentException. (str "No property for found for " k))))))

(defn invoke-setter!
  [method obj val conf]
  (let [params (to-java* (derive-setter-param-type method) val conf)]
    (try
      (.invoke method obj (into-array Object [params]))
      (catch IllegalArgumentException e (println (str (.getMessage e) " : Choked on " method " -> " val))))))

(defn erased-type
  [dtype]
  (if (vector? dtype)
    (first dtype)
    dtype))

(defn nth-type-param
  [dtype n]
  (if (vector? dtype)
    (nth dtype (inc n))
    nil))

(defmulti find-ctorm (fn [dtype conf] (erased-type dtype)))
(defmethod find-ctorm :default
  [dtype conf]
  (fn [v]
    (if (leave? v)
      v
      (.newInstance (erased-type dtype)))))

(defn find-ctor
  [dtype conf]
  (or (get-in conf [:ctor dtype])
      (get-in conf [:ctor (erased-type dtype)])
      (find-ctorm dtype conf)))

(defmulti find-setterm (fn [dtype conf] (erased-type dtype)))

(defmethod find-setterm :default
  [dtype conf]
  (fn [obj val]
    (when (and (not (leave? val)) (map? val))
      (let [cls (erased-type dtype)
            prop-map (prop-descriptors->setter-map (prop-descriptors cls))]
        (doseq [[k v] val]
          (if-let [method (prop-map k)]
            (invoke-setter! method obj v conf)
            (set-field! cls obj k v conf)))))))

(defn find-setter
  [dtype conf]
  ;;look in map, find a function that takes value and populates an existing instance of `class`
  (or (get-in conf [:setter dtype])
      (get-in conf [:setter (erased-type dtype)])
      (find-setterm dtype conf)))

(defn to-java
  "Take a value `val` and convert it into an object `class`.  The structure of value will be
  converted into an object graph with `class` at the root.  For example, a map {:bar \"hello\"}
  converted into class Foo, would create a Foo with the property `bar` set to \"hello \".
  Default conversions are provided for Strings, primitive properties, maps as POJOdefault, and nested
  maps are applied to POJOs recursively.

  Constructors can be provided in the conf map if required:
  `to-java {:ctor {Foo (fn [_] (Foo. 123))}} ...`

  Options:
      :strict-mode if true, `to-java` will throw an `IllegalArgumentException` when one of the properties in
  an input map cannot be located in the java class.
      :accessible-hack if true, will break encapsulation and set private properties of the target class.  Use
      this for example, for classes that do not conform to the bean spec."
  ([dtype val conf]
   (to-java* dtype val conf))
  ([dtype val]
   (to-java* dtype val {})))

(defmethod find-ctorm String
  [_ _]
  str)

(defmethod find-ctorm Integer/TYPE
  [_ _]
  int)

(defmethod find-ctorm Integer
  [_ _]
  #(Integer/valueOf %))

(defmethod find-ctorm BigInteger
  [_ _]
  #(BigInteger/valueOf %))

(defmethod find-ctorm List
  [dtype conf]
  (let [type-param (or (nth-type-param dtype 0) Object)]
    (fn [v]
      (ArrayList. (mapv #(to-java type-param % conf) v)))))

(defmethod find-ctorm Set
  [dtype conf]
  (let [type-param (or (nth-type-param dtype 0) Object)]
    (fn [v]
      (HashSet. (mapv #(to-java type-param % conf) v)))))

(defmethod find-ctorm SortedSet
  [dtype conf]
  (let [type-param (or (nth-type-param dtype 0) Object)]
    (fn [v]
      (TreeSet. (mapv #(to-java type-param % conf) v)))))

(defmethod find-ctorm Map
  [dtype conf]
  (let [ktype (or (nth-type-param dtype 0) Object)
        vtype (or (nth-type-param dtype 1) Object)]
    (fn [m]
      (HashMap. (into {} (for [[k v] m] [(to-java ktype k conf)
                                         (to-java vtype v conf)]))))))
(defmethod find-ctorm Enum
  [dtype conf]
  (fn [s]
    (Enum/valueOf dtype s)))

(defn array-dim-and-type
  [dtype]
  (let [part (partition-by #(= :array %) (flatten dtype))]
    [(count (first part)) (first (second part))]))

(declare magic-arr*)

(defmethod find-ctorm :array
  [dtype conf]
  (let [[dim type] (array-dim-and-type dtype)
        array-maker #(apply make-array type (count %) (repeat (dec dim) 0))]
    (fn [val]
      (magic-arr* dtype dim val (array-maker val) conf))))

;; Array creation magic

(defn- magic-arr*
  "Create an array of type `dtype` with `n` dimensions, filling it with the values in `vec`/
  The array dimensions may be ragged. "
  ([dtype n vec arr]
   (magic-arr* dtype n vec arr {}))
  ([dtype n vec arr conf]
   (if (= n 1)
     (doseq [[i x] (map-indexed vector vec)]
       (aset arr i (to-java (second dtype) x conf)))
     (doseq [[i x] (map-indexed vector vec)
             :let [xarr (apply make-array dtype (count x) (repeat (- n 2) 0))]]
       (aset arr i xarr)
       (magic-arr* (second dtype) (dec n) x xarr conf)))
   arr))
