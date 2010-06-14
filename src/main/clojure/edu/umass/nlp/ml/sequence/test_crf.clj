(ns edu.umass.nlp.ml.sequence.test-crf
  (:gen-class)
  (:import [edu.umass.nlp.exec Execution]
    [edu.umass.nlp.utils BasicValued Counters MapCounter]
    [edu.umass.nlp.io IOUtils]
    [edu.umass.nlp.ml.sequence CRF]
    edu.umass.nlp.ml.sequence.CRF$InfMode)
  (:require [clojure.contrib [command-line :as cli]
             [duck-streams :as ds]
             [string :as str]]))

(defn read-data [input-file]
  (filter #(not (empty? %))
    (for [seq-str (str/partition #"\n\n" (ds/slurp* input-file))]
      (for [elem (str/split-lines seq-str)]
        (str/split #"\s+" elem)))))

(defn read-loss [loss-file]
  (when loss-file
    (->> (ds/read-lines loss-file)
      (reduce
       (fn [res line]
          (let [[truth guess loss] (.split line "\\s+")
                res (assoc res truth (get res truth (MapCounter.)))]
            (.incCount (get res truth) guess (Double/parseDouble loss))
            res))
        {}))))

(defn get-tag-fn [model-file inf-mode loss-file]
  (let [#^CRF crf (IOUtils/readObject model-file)
        inf-mode
	  (case inf-mode
	      "VITERBI" CRF$InfMode/VITERBI
	      "MINRISK" CRF$InfMode/MINRISK)
        losses (read-loss loss-file)]
    (fn [input]
      (.getTagging crf inf-mode input losses))))

(defn -main [& args]
  (Execution/init)
  (cli/with-command-line args
    "test_crf.clj -- test-file model-file"
    [[lossFile "file to read losses from"]
     [infMode "[VITERBI|MINRISK]" "VITERBI"]
     remaining]
    (let [[model-file test-file] args
          tag-fn (get-tag-fn (first remaining) infMode lossFile)]
      (doseq [input-seq (read-data test-file)]
        (doseq [tag (tag-fn input-seq)]
          (println tag))
        (println)))))

(when *command-line-args* (apply -main *command-line-args*))