(ns shx.shape.bash-test
  (:require [clojure.test :refer [deftest is testing]]
            [shx.shape.bash :as bash]))

(deftest emit-export-test
  (is (= "export APP_ENV=\"dev\"" (bash/emit [:export {:name "APP_ENV" :value "dev"}]))))

(deftest emit-path-prepend-test
  (testing "unguarded"
    (is (= "PATH=\"$HOME/x\"${PATH:+:$PATH}"
           (bash/emit [:path/prepend "~/x"]))))
  (testing "dir-exists guard"
    (is (= "[ -d \"$HOME/x\" ] && PATH=\"$HOME/x\"${PATH:+:$PATH}"
           (bash/emit [:path/prepend "~/x" {:if :dir-exists}])))))

(deftest emit-exec-test
  (testing "plain"
    (is (= "'ls' '-la'" (bash/emit [:exec {:argv ["ls" "-la"]}]))))
  (testing "dir wraps in subshell, env prefix stays INSIDE it"
    (is (= "(cd '/tmp' && GIT_PAGER='cat' 'git' 'status')"
           (bash/emit [:exec {:argv ["git" "status"] :dir "/tmp" :env {:GIT_PAGER "cat"}}])))))

(deftest emit-pipe-test
  (is (= "'ls' | 'grep' 'x'"
         (bash/emit [:pipe [:exec {:argv ["ls"]}] [:exec {:argv ["grep" "x"]}]]))))

(deftest emit-raw-test
  (testing "bash raw passes through verbatim"
    (is (= "echo hi" (bash/emit [:raw {:lang "bash" :text "echo hi"}]))))
  (testing "non-bash raw degrades to a comment"
    (is (= "# raw clojure block cannot be emitted as bash without translation"
           (bash/emit [:raw {:lang "clojure" :text "(+ 1 2)"}])))))

(deftest emit-program-validation-test
  (testing "invalid program throws with malli explanation"
    (is (thrown? clojure.lang.ExceptionInfo
                 (bash/emit-program [[:unknown-node {}]])))))
