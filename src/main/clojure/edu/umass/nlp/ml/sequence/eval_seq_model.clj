(ns eval_crf
  (:gen-class
     :name edu.umass.nlp.ml.sequence.EvalSequenceModel)     
  (:use [mochi.core] [swank.core])
  (:require [clojure.contrib.duck-streams :as ds]))

(defrecord LabelStats [label tp fp fn])


              
(defn precision [{:keys [tp,fp]}] 
  (try-or-else (double (/ tp (+ tp fp))) 0.0))

(defn recall [{:keys [tp,fn]}] 
  (try-or-else (double (/ tp (+ tp fn))) 0.0))

(defn f1 [stats] 
  (let [p (precision stats) r (recall stats)]
    (try-or-else (double (/ (* 2 p r) (+ p r))) 0.0)))

(defn report-stats [stats]
  (format "%s -> f1: %.1f p: %.1f r: %.1f" 
    (:label stats) (* 100 (f1 stats)) 
    (* 100 (precision stats)) (* 100 (recall stats))))

(defn inc-stats [stats correct guess]  
  (let [{:keys [label,tp,fp,fn]} stats]
    (cond
      (= correct guess) (LabelStats. label (inc tp) fp fn)
      (= correct label) (LabelStats. label tp fp (inc fn))
      (= guess label)   (LabelStats. label tp (inc fp) fn))))

(defn update-stats [stats-map label-pairs]
  (reduce 
   (fn [stats-map [correct guess]]
     (if (= correct guess)
       (update-in stats-map [correct] inc-stats correct guess)
       (-> stats-map
	   (update-in [correct]  inc-stats correct guess)	        
	   (update-in [guess] inc-stats correct guess))))
   stats-map
   label-pairs))
    
(defn file-labels [f]
  (for [l (ds/read-lines f) 
         :when  (and (not (.isEmpty l))
                     (not= l "</S>")
                     (not= l "<S>"))]        
     (.trim l)))    
	  	  
(defn all-label-pairs [input-list-path]
  (mapcat                          
    (fn [line]
      (let [[correct guess] (.split line "\\s+")
            correct-lines (file-labels correct) guess-lines  (file-labels guess)]            
        (assert (= (count correct-lines) (count guess-lines)))
        (map vector correct-lines guess-lines)))
    (ds/read-lines input-list-path)))	 
        
(defn make-stat-maps [label-pairs]
  (let [labels (into (hash-set) (map first label-pairs))
        init-stat-maps
         (reduce 
            (fn [res label]              
              (assoc res label (LabelStats. label 0 0 0)))
            {} labels)]
    (-> (update-stats init-stat-maps label-pairs)        
        (dissoc "NONE")))) 

(defn avg [xs] (/ (reduce + xs) (count xs)))  
          
(defn add-avg [stat-map]
  (let [f (fn [k] (reduce + (map k (filter identity (vals stat-map)))))]
    (assoc stat-map "AVG"
      (LabelStats. "AVG" (f :tp) (f :fp) (f :fn)))))   
      
(defn add-binary [stat-map label-pairs]
  (let [to-binary (fn [x] (if (= "NONE" x) x "BINARY"))]
    (assoc stat-map "BINARY"
      (reduce
       (fn [res [correct guess]]
	 (assert (not (#{"<S>" "</S>"} correct)))
	 (assert (not (#{"<S>" "</S>"} guess)))
	 (if (or (not= correct "NONE") (not= guess "NONE"))
	   (inc-stats res (to-binary correct) (to-binary guess))
	   res))       
        (LabelStats. "BINARY" 0 0 0)
        label-pairs))))

(defn -eval [correct-labels guess-labels]
  (let [label-pairs (map vector correct-labels guess-labels)
        stat-maps (-> label-pairs make-stat-maps add-avg (add-binary label-pairs))]
    (doseq [[label stats] stat-maps]
      (println (report-stats stats)))))
               
(defn main [[input-list-path & _]]  
  (let [label-pairs (all-label-pairs input-list-path)
        stat-maps (-> label-pairs make-stat-maps add-avg (add-binary label-pairs))]
    (doseq [[label stats] stat-maps]
      (println (report-stats stats)))))

(defn -main [& args])

(when *command-line-args* (main *command-line-args*))
