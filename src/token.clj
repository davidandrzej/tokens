(ns token
  (:require [clojure.string :as str])
  (:use [clojure.contrib.duck-streams :only (read-lines)])
  (:use [opennlp.nlp :only (make-tokenizer)]))

;; These files should be in the resources/ folder of project root
;; (if not, fetch from the web with getmodels.sh)
(def tokfile "en-token.bin")
(def stopfile "english.stop")

;; *opennlp-tokenizer* must be bound to return value of get-tokenizer
(def *opennlp-tokenizer* nil)
;; ;; Similar to *opennlp-tokenizer* but optional
(def *opennlp-stoplist* {})

(def whitespace-re #"\s+")
(def punc-re #"\W+")
(defn de-punc
  "Replace punc with whitespace, then split on whitespace"
  [tok]
  (-> tok (str/replace ,,, punc-re " ")
      (str/split ,,, whitespace-re)))
         
(defn accept-tok?
  "Is this a valid token?"
  [stoplist tok]
  (not (or (str/blank? tok)
           (contains? stoplist tok))))

(defn process-token
  "Process a single token: de-punc, stop filter, downcase"
  [tok]
  (->> tok str/lower-case de-punc
       (filter (partial accept-tok? *opennlp-stoplist*))))
                                                   
(defn process-text
  "Process a text string using tokenizer and process-token"
  [doctxt]
  (->> doctxt *opennlp-tokenizer* (mapcat process-token)))

(defn count-tokens
  "Return count map of processed tokens in text string"
  [doctxt]
  (-> doctxt process-text frequencies))

;;
;; Functions for loading the tokenizer and stoplist
;;
(defn get-stoplist
  "Read newline-delimited stoplist file"
  ([]
     (get-stoplist
      (.getResourceAsStream (clojure.lang.RT/baseLoader) stopfile)))
  ([filestream]
     (->> (read-lines filestream) (mapcat de-punc) set)))
          
(defn get-tokenizer
  "Instantiate OpenNLP tokenizer"
  ([]
     (get-tokenizer (.getResourceAsStream
                     (clojure.lang.RT/baseLoader) tokfile)))
  ([filestream]
     (make-tokenizer filestream)))

(defmacro with-token
  "Convenience macro to simultaneously bind stoplist and tokenizer"
  [& body]  
  `(binding [*opennlp-stoplist* (get-stoplist)
             *opennlp-tokenizer* (get-tokenizer)]
     ~@body))
