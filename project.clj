(defproject munge-tout "0.1.2"
            :description "Convert Java object graphs to/from clojure data structures."
            :url "http://github.com/flybe/munge-tout"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.6.0"]]
            :test-paths ["test/clj"]
            :profiles {:dev {:java-source-paths ["test/java"]}})
