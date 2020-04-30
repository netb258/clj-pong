(defproject pong "0.1.0-SNAPSHOT"
  :description "The classic game Pong written in Clojure."
  :url "https://github.com/netb258/clj-pong"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [quil "2.7.1"]]
  :main ^:skip-aot pong.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
