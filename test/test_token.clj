(ns test-token
  (:use [clojure.test :only (testing deftest is are use-fixtures)])
  (:require [clojure.string :as str])
  (:use [token :only (with-token process-text
                       count-tokens *opennlp-stoplist*)]))
                       

(use-fixtures :once (fn [f] (with-token (f))))

(def simple "foo-Bar BAZ the CaT!!")
(def simpletoks ["foo" "bar" "baz" "cat"])
(def simpletoksnostop ["foo" "bar" "baz" "the" "cat"])

(deftest test-string-inputs
  (testing "Simple string"
    (is (= (process-text simple))
        simpletoks))
  (testing
      "Simple string without stoplist"
    (is (= (binding [*opennlp-stoplist* {}]
             (process-text simple))
           simpletoksnostop))))

(deftest test-collection-inputs
  (testing "Simple collection with newline"  
    (is (= (process-text
            (str/join "   \n " [simple simple]))
           (concat simpletoks simpletoks))))
  (testing "Extreme collection nesting"
    (is (= (process-text
            [[["Boat"] "moaT"] ["smotE"] "coat"
             ["throat"] "tote    rote"])
           ["boat" "moat" "smote" "coat" "throat" "tote" "rote"]))))

(deftest test-bad-inputs
  (testing "Bad input: int"
    (is (thrown-with-msg? Throwable #"process-text called"
          (process-text 1000))))
  (testing "Bad input: int within collection"
    (is (thrown-with-msg? Throwable #"process-text called"
          (process-text ["foo bar buzz" 1000])))))

(deftest test-empty-results
  (testing "Result has no tokens"  
    (is (empty? (process-text "and the of"))))
  (testing "Input is empty coll"
    (is (empty? (process-text '())))))

(deftest test-count
  "Count tokens"
  (is (= (count-tokens "buzz buZZ bat cat")
         {"buzz" 2 "bat" 1 "cat" 1})))

