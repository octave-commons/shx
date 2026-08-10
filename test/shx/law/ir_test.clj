(ns shx.law.ir-test
  (:require [clojure.test :refer [deftest is testing]]
            [shx.law.ir :as law]))

(def valid-program
  [[:export {:name "APP_ENV" :value "dev"}]
   [:path/prepend "~/.local/bin" {:if :dir-exists}]
   [:path/append "~/go/bin"]
   [:alias {:name "ll" :argv ["ls" "-la"]}]
   [:set {:name "HOST" :value [:capture [:exec {:argv ["hostname"]}]]}]
   [:if [:test "-d" "/tmp"]
    [:exec {:argv ["echo" "yes"]}]
    [:exec {:argv ["echo" "no"]}]]
   [:pipe [:exec {:argv ["ls"]}] [:exec {:argv ["grep" "x"]}]]
   [:exec {:argv ["git" "status"] :dir "/tmp" :env {:GIT_PAGER "cat"}}]
   [:source "~/.alias"]
   [:raw {:lang "bash" :text "echo hi"}]])

(deftest valid-program-test
  (is (law/valid-program? valid-program)))

(deftest valid-node-test
  (testing "one node validates on its own, without a program wrapper"
    (is (law/valid-node? [:export {:name "APP_ENV" :value "dev"}]))
    (is (law/valid-node? [:pipe [:exec {:argv ["ls"]}] [:exec {:argv ["wc" "-l"]}]])))
  (testing "a program is not a node"
    (is (not (law/valid-node? valid-program))))
  (testing "unknown head rejected"
    (is (not (law/valid-node? [:frobnicate {:a 1}])))))

(deftest invalid-programs-test
  (testing "unknown head rejected"
    (is (not (law/valid-program? [[:frobnicate {:a 1}]]))))
  (testing "missing required args rejected"
    (is (not (law/valid-program? [[:export {:name "X"}]]))))
  (testing "empty argv rejected"
    (is (not (law/valid-program? [[:exec {:argv []}]]))))
  (testing "non-vector rejected"
    (is (not (law/valid-program? ["just a string"]))))
  (testing "bad guard enum rejected"
    (is (not (law/valid-program? [[:path/prepend "~/x" {:if :magic}]])))))
