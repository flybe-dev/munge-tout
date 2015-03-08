(ns munge-tout.util
  (:require [clojure.string :as strs]))

(defn camel-case-to-hyphenated
  "Converts a camel case string to a hyphenated string.
  E.g 'fooBar' -> 'foo-bar'"
  [s]
  (strs/lower-case (strs/replace s
                                 #"\B[A-Z]"
                                 #(str "-" %1))))

(defn hyphenated-to-camel-case
  "Converts a hyphenated string to camel case.
  E.g. 'foo-bar' -> 'fooBar'"
  [s]
  (strs/replace s
                #"-(\w)"
                #(strs/upper-case (second %1))))

(defn prettify-keyword
  "Turns a keyword from idiomatic database naming into an idiomatic Clojure format,
  e.g. :foo_bar -> :foo-bar"
  [kw]
  (if (keyword? kw)
    (keyword (.replace (name kw) \_ \-))
    kw))

(defn string->keyword
  [k]
  (prettify-keyword (keyword (strs/lower-case k))))

(defn equals-ignore-case
  "String equality ignoring case."
  [s1 s2]
  (if (instance? String s1)
    (.equalsIgnoreCase s1 s2)
    (= s1 s2)))