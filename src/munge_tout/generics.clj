(ns munge-tout.generics
  "Functions for finding the parameterized types of Java classes, including array types."
  (:import (java.lang.reflect ParameterizedType GenericArrayType)))

(defmulti derive-args-of-type class)

(defmethod derive-args-of-type Class
  [type]
  (if (.isArray type)
    [:array (derive-args-of-type (.getComponentType type))]
    type))

(defmethod derive-args-of-type ParameterizedType
  [type]
  (vec (cons (.getRawType type) (map derive-args-of-type (.getActualTypeArguments type)))))

(defmethod derive-args-of-type GenericArrayType
  [type]
  [:array (derive-args-of-type (.getGenericComponentType type))])

(defn derive-setter-param-type
  [method]
  (derive-args-of-type (first (.getGenericParameterTypes method))))

(defn derive-field-type
  [field]
  (derive-args-of-type (.getGenericType field)))