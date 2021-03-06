(ns token
  (:require [clojure.string :as str])
  (:use [clojure.contrib.duck-streams :only (read-lines)])
  (:use [opennlp.nlp :only (make-tokenizer)]))

;; These files should be in the resources/ folder of project root
;; (if not, fetch from the web with getmodels.sh)
(def tokfile "en-token.bin")
(def stopfile "english.stop")

;; *opennlp-tokenizer* must be bound to an OpenNLP Tokenizer, while
;; *opennlp-stoplist* is optional
(def *opennlp-tokenizer* nil)
(def *opennlp-stoplist* {})

;;
;; Helper functions
;;
(def whitespace-re #"\s+")
(def punc-re #"\W+")
(defn- de-punc
  "Replace punc with whitespace, then split on whitespace"
  [tok]
  (-> tok (str/replace ,,, punc-re " ")
      (str/split ,,, whitespace-re)))
         
(defn- accept-tok?
  "Is this a valid token?"
  [tok]
  (not (or (str/blank? tok)
           (contains? *opennlp-stoplist* tok))))

(defn- process-token
  "Process a single token: de-punc, stop filter, downcase"
  [tok]
  (->> tok str/lower-case de-punc (filter (bound-fn* accept-tok?))))

;;
;; process-text is the primary function
;;
;; given a string, seq of strings, seq of seqs of strings, etc
;; returns a seq of processed (downcased, de-punctuated, stoplisted) toks
;;
(defmulti process-text
  (fn [val] (cond (string? val) ::textstring       
                  (coll? val) ::textcoll
                  (nil? val) ::textnil)))

(defmethod process-text ::textstring
  [text] {:pre [(not (nil? *opennlp-tokenizer*))]}
  (->> text *opennlp-tokenizer* (mapcat (bound-fn* process-token))))  

(defmethod process-text ::textcoll
  [textseq] (mapcat (bound-fn* process-text) textseq))

(defmethod process-text ::textnil [_] '())  

(defmethod process-text :default
  [unknown]
  (throw (Throwable. (format "process-text called on non-string/coll: %s"
                             unknown))))

(defn count-tokens
  "Return count map of processed tokens"
  [text]
  (->> text ((bound-fn* process-text)) frequencies))

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
