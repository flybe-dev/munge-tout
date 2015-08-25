(ns munge-tout.munge-test
  (:require [clojure.test :refer :all]
            [munge-tout.core :refer :all])
  (:import (munge.tout Colour Quux Ottokar)))

(def munge-conf {:ctor {Quux (fn [_] (Quux. "a quux"))}})

(deftest munge-enums
  "munging enums"
  (is (= "RED"
         (from-java Colour/RED)))
  (is (= Colour/RED
         (to-java Colour (from-java Colour/RED))))
  (is (= Colour/MAUVE
         (to-java Colour (from-java Colour/MAUVE)))))

(deftest munge-maps
  (let [a-map {99 "red" 4 "yellow" 16 "green"}
        quux (to-java Quux {:things a-map} munge-conf)
        munged (from-java quux)]
    (is (= (:things munged)
           a-map))
    (is (map? (:things munged)))))

(deftest munge-sets
  (let [a-set #{"Gödel" "Schiller" "Bach"}
        quux (to-java Quux {:items a-set} munge-conf)
        munged (from-java quux)]
    (is (= (:items munged
             a-set)))
    (is (set? (:items munged)))))

(deftest munge-sorted-sets
  (let [a-set #{"Gödel" "Schiller" "Bach"}
        quux (to-java Quux {:sorted-items a-set} munge-conf)
        munged (from-java quux)]
    (is (= (:sorted-items munged
             a-set)))
    (is (set? (:sorted-items munged)))
    (is (= "Bach"
          (.first (.getSortedItems quux))))
    (is (= "Schiller"
           (.last (.getSortedItems quux))))
    (is (= (apply sorted-set a-set)
           (:sorted-items munged)))))


(deftest munge-lists
  (let [a-list ["eins" "zwei" "drei"]
        jovo (to-java Ottokar {:pets a-list} {:accessible-hack? true})
        munged (from-java jovo)]
    (is (= (:pets munged)
           a-list))
    (is (seq? (:pets munged)))))