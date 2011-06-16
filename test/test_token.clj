(ns test-token
  (:use [clojure.test :only (deftest is are)])
  (:require [clojure.string :as str])
  (:use [token :only (with-token process-text count-tokens
                       *opennlp-tokenizer* get-tokenizer)]))


(def simple "foo-Bar BAZ the CaT!!")
(def simpletoks ["foo" "bar" "baz" "cat"])
(def simpletoksnostop ["foo" "bar" "baz" "the" "cat"])

(deftest test-string
  "Simple string"
  (is (= (with-token (process-text simple))
         simpletoks)))
         
(deftest test-nostoplist
  "Simple string without stoplist"
  (is (= (binding [*opennlp-tokenizer* (get-tokenizer)]
           (process-text simple))
         simpletoksnostop)))

(deftest test-coll
  "Simple collection with newline"
  (is (= (with-token (process-text
                      (str/join "   \n " [simple simple])))
         (concat simpletoks simpletoks))))

(deftest test-nesting
  "Extreme collection nesting"
  (is (= (with-token (process-text
                      [[["Boat"] "moaT"] ["smotE"] "coat"
                       ["throat"] "tote    rote"])
           ["boat" "moat" "smote" "coat" "throat" "tote" "rote"]))))

(deftest test-empty-result
  "Result has no tokens"
  (is (empty? (with-token (process-text "and the of")))))

(deftest test-empty-input
  "Input is empty coll"
  (is (empty? (with-token (process-text '())))))

(deftest test-count
  "Count tokens"
  (is (= (with-token (count-tokens "buzz buZZ bat cat"))
         {"buzz" 2 "bat" 1 "cat" 1})))         

(deftest test-bad1
  "non-string/collection input (just int)"
  (is (thrown-with-msg? Throwable #"process-text called"
        (with-token (process-text 1000)))))

(deftest test-bad1
  "non-string/collection input (collection with some strings)"
  (is (thrown-with-msg? Throwable #"process-text called"
        (with-token (process-text ["foo bar buzz" 1000])))))
