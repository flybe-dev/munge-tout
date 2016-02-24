(ns munge-tout.unmunge-test
  (:import (munge.tout Foo Quux Svatopluk Colour ClassWithArray ClassWithIter)
           (java.util Arrays List ArrayList HashMap Map))
  (:require [clojure.test :refer :all]
            [munge-tout.core :refer :all]))

(deftest list-unmunging
  "unmunge a list of strings"
  (let [gorgons ["Stheno" "Euryale" "Medusa"]
        jovo-foo (to-java Foo {:bar gorgons})]
    (is (= ["Stheno" "Euryale" "Medusa"]
           (.bar jovo-foo)))))

(deftest iterator-unmunging
  (let [fates ["Clotho" "Lachesis" "Atropos"]
        jovo (to-java ClassWithIter {:things fates} {:accessible-hack? :yes})]
    (is (true? (.equals (ArrayList. (iterator-seq (.getThings jovo))) (ArrayList. fates))))
    (is (= ["Clotho" "Lachesis" "Atropos"]
           (from-java (.getThings jovo))))))

(def munge-conf {:ctor {Quux (fn [_] (Quux. "a quux"))}})

(deftest set-unmunging
  "munging and unmunging sets"
  (let [things #{"alimar" "binstow" "cafto"}
        quux (to-java Quux {:items things} munge-conf)]
    (is (= things
           (.getItems quux)))))

(deftest map-unmunging
  "munging and unmunging maps"
  (let [things {1 "jovo" 7 "ovoj" 6 "ojov"}
        quux (to-java Quux {:things things} munge-conf)]
    (is (= things
           (->> quux .getThings (into {}))))))

(deftest primitive-types
  "construction of primitive types"
  (let [integer 5
        long 1234567890
        a-float (float 1.2345)
        a-double 1.234567
        big-int (bigint 1234)
        bool true
        quux (to-java Quux
                      {:integar integer :looong long
                       :a-float a-float :a-double a-double
                       :bool    bool :big-int big-int}
                      munge-conf)]
    (are [exp act] (= exp act)
                   integer (.getIntegar quux)
                   long (.getLooong quux)
                   a-float (.getaFloat quux)
                   a-double (.getaDouble quux)
                   bool (.isBool quux)
                   big-int (.getBigInt quux))))

(deftest inherited-properties;
  (testing "that inherited fields can be set by the auto-munger"
    (let [subclass (to-java Svatopluk
                            {:pets ["Jarvis" "Henkle"]}
                            {:accessible-hack? :yes :strict-mode :yes})]
      (is (= ["Jarvis" "Henkle"] (.getPets subclass))))))

(deftest munging-enums
  (is (= Colour/RED (to-java Colour "RED"))))

(deftest munging-arrays
  (testing "munge a vector into a Java int (primitive) array."
    (is (Arrays/equals
          (int-array [1 2 3 2 1])
          (.getIntegerArray (to-java ClassWithArray {:integer-array [1 2 3 2 1]})))))

  (testing "munge an array of parametrics - it's a hideous type but the munger should be capable of this"
    (let [target-obj (ClassWithArray.)
          java-list (into-array List [(ArrayList. (map int [1 2 3])) (ArrayList. (map int [9 8 7]))])
          munged-obj (to-java ClassWithArray {:idiotic-list [[1 2 3] [9 8 7]]})]
      (set! (.idioticList target-obj) java-list)
      (is (.equals target-obj munged-obj))
      (is (= ArrayList (class (aget (.idioticList munged-obj) 0))))))

  (testing "munge an array of maps of Integer->arrays.  of course, java arrays do not support the equals method!"
    (let [target-obj (ClassWithArray.)
          target-map (HashMap.)
          string-array-a (into-array String ["foo" "bar"])
          string-array-b (into-array String ["quux" "x"])
          munged-obj (to-java ClassWithArray {:idiotic-map [{5 ["foo" "bar"] 7 ["quux" "x"]}]})]
      (.put target-map (Integer/valueOf 5) string-array-a)
      (.put target-map (Integer/valueOf 7) string-array-b)
      (set! (.idioticMap target-obj) (into-array Map [target-map]))
      (let [munged-map (aget (.idioticMap munged-obj) 0)]
        (is (Arrays/equals string-array-a (.get munged-map (Integer/valueOf 5))))
        (is (Arrays/equals string-array-b (.get munged-map (Integer/valueOf 7))))
        (is (.equals (.keySet target-map) (.keySet munged-map)))))))

