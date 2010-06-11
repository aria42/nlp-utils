(ns edu.umass.nlp.ml.sequence.train-crf
  (:gen-class)
  (:import [edu.umass.nlp.ml.sequence CRF BasicLabelSeqDatum]
           [edu.umass.nlp.utils BasicPair]
           [edu.umass.nlp.io IOUtils]
           [edu.umass.nlp.exec Execution])
  (:require [clojure.contrib [command-line :as cli]       
	     [duck-streams :as ds]
	     [string :as str]]
	     [swank core]))

(defn train-crf [labeled-seqs sigma-squared num-iters]
  (doto (CRF.)
        (.setSigmaSquared sigma-squared)
        (.train labeled-seqs
          (let [opts (edu.umass.nlp.optimize.LBFGSMinimizer$Opts.)]
            (set! (.minIters opts) 100)
            (set! (.maxIters opts) 150)
            opts))))

(defn read-data [input-file]
  (for [seq-str (str/partition #"\n\n" (ds/slurp* input-file))
        :let [lines (str/split-lines seq-str)
              all-fields (map #(seq (str/split #"\s+" %)) lines)]
        :when (not (empty? lines))]
    (BasicLabelSeqDatum.
      (map butlast all-fields)
      (map last all-fields)
      1.0)))       

(defn -main [& args]
  (Execution/init)
  (cli/with-command-line args
    "train_crf -- train-file model-file"
    [[sigmaSquared "Sigma Squared" "0.5"]
     [numIters "Number of Iterations" "130"]
      args]
    (IOUtils/writeObject
      (train-crf (read-data (first args))
		    (Double/parseDouble sigmaSquared)
			  (Integer/parseInt numIters))
      (second args))))       

(when *command-line-args* (apply -main *command-line-args*))