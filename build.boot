(set-env!
 :source-paths    #{"src/main"}
 :dependencies '[[org.clojure/clojure         "1.9.0-alpha19"  :scope "provided"]
                 [org.clojure/clojurescript   "1.9.908"        :scope "provided"
                  :classifier "aot"
                  :exclusions [org.clojure/clojure
                               org.clojure/data.json]]
                 [org.clojure/clojurescript   "1.9.908"        :scope "provided"
                  :exclusions [org.clojure/clojure
                               org.clojure/data.json]]
                 [org.clojure/data.json       "0.2.6"          :scope "provided"
                  :classifier "aot"]
                 [react                "16.0.0-0"]
                 [react-dom            "16.0.0-0"]
                 [com.cognitect/transit-clj   "0.8.300"]
                 [com.cognitect/transit-cljs  "0.8.239"]

                 [org.clojure/core.async      "0.3.443"        :scope "test"
                  :exclusions [org.clojure/tools.reader]]
                 [figwheel-sidecar            "0.5.10"         :scope "test"
                  :exclusions [org.clojure/clojurescript
                               org.clojure/tools.reader]]
                 [devcards                    "0.2.4-SNAPSHOT" :scope "test"
                  :exclusions [org.clojure/clojurescript]]
                 [com.cemerick/piggieback     "0.2.1"          :scope "test"
                  :exclusions [org.clojure/clojure
                               org.clojure/tools.nrepl
                               org.clojure/clojurescript]]
                 [pandeiro/boot-http          "0.8.3"          :scope "test"]
                 [adzerk/boot-cljs            "2.1.4"          :scope "test"
                  :exclusions [org.clojure/clojurescript]]
                 [adzerk/boot-cljs-repl       "0.3.3"          :scope "test"]
                 [adzerk/boot-test            "1.2.0"          :scope "test"]
                 [crisptrutski/boot-cljs-test "0.3.4"          :scope "test"
                  :exclusions [org.clojure/clojurescript]]
                 [adzerk/boot-reload          "0.5.2"          :scope "test"]
                 [org.clojure/tools.nrepl     "0.2.13"         :scope "test"]
                 [weasel                      "0.7.0"          :scope "test"]]
 :exclusions '[org.clojure/clojure org.clojure/clojurescript])

(require
 '[adzerk.boot-cljs            :refer [cljs]]
 '[adzerk.boot-cljs-repl       :as cr :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload          :refer [reload]]
 '[adzerk.boot-test            :as bt]
 '[crisptrutski.boot-cljs-test :as bct]
 '[pandeiro.boot-http          :refer [serve]])

(deftask devcards []
  (set-env! :source-paths #(conj % "src/devcards")
            :resource-paths #{"resources/public"})
  (comp
    ;; remove possible artifacts of Figwheel compilation
    (sift :include #{#"^devcards\/(out\/|main.js)"} :invert true)
    (serve :port 3449)
    (watch)
    (cljs-repl)
    (reload)
    (speak)
    (cljs :source-map true
          :optimizations :none
          :compiler-options {:devcards true
                             :main 'om.devcards.core
                             :verbose true
                             :parallel-build true}
          :ids #{"devcards/main"})
    (target)))

(deftask testing []
  (set-env! :source-paths #(conj % "src/test"))
  identity)

(ns-unmap 'boot.user 'test)

(deftask test-clj []
  (comp
    (testing)
    (bt/test)))

(deftask test-cljs
  [e exit?     bool  "Enable flag."]
  (let [exit? (cond-> exit?
                (nil? exit?) not)]
    (comp
      (testing)
      (bct/test-cljs
        :js-env :node
        :namespaces #{'om.next.tests}
        :cljs-opts {:parallel-build true
                    :target :nodejs
                    :asset-path "test_suite.out"}
        :exit? exit?
        :ids #{"lumo_test/test_suite"}))))

(deftask test
  [e exit?     bool  "Enable flag."]
  (let [exit? (cond-> exit?
                (nil? exit?) not)]
    (comp
      (test-clj)
      (test-cljs :exit? exit?))))

(deftask auto-test []
  (comp
    (watch)
    (speak)
    (test :exit? false)))
