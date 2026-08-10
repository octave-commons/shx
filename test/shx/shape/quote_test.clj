(ns shx.shape.quote-test
  (:require [clojure.test :refer [deftest is testing]]
            [shx.shape.quote :as q]))

(deftest sq-test
  (testing "plain strings"
    (is (= "'hello'" (q/sq "hello"))))
  (testing "embedded single quotes are escaped"
    (is (= "'it'\\''s'" (q/sq "it's"))))
  (testing "empty string"
    (is (= "''" (q/sq "")))))

(deftest dq-test
  (testing "leading tilde becomes $HOME"
    (is (= "\"$HOME/.local/bin\"" (q/dq "~/.local/bin"))))
  (testing "double quotes are escaped"
    (is (= "\"say \\\"hi\\\"\"" (q/dq "say \"hi\""))))
  (testing "backticks are escaped"
    (is (= "\"no \\`exec\\`\"" (q/dq "no `exec`")))))

(deftest argv->bash-test
  (is (= "'ls' '-la'" (q/argv->bash ["ls" "-la"]))))

(deftest env-prefix-test
  (testing "nil and empty render nil"
    (is (nil? (q/env-prefix nil)))
    (is (nil? (q/env-prefix {}))))
  (testing "assignments end with a trailing space for command concatenation"
    (is (= "GIT_PAGER='cat' " (q/env-prefix {:GIT_PAGER "cat"})))))
