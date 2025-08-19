(ns witan.homelessness.benchmarking.assessments
  (:require
   [clojure.java.io :as io]
   [clojure.string :as s]
   [tablecloth.api :as tc]
   [tech.v3.libs.fastexcel :as fst]))

(def assessments-2025-file "./homelessness-statistics/Detailed_LA_202503.xlsx")

(defn ->map-of-datasets
  "Read homelessness statistics data from xlsx (converted from ods in
   LibreOffice Calc)"
  [& {::keys [resource-file-name file-path options]
      :or    {resource-file-name assessments-2025-file}}]
  (with-open [in (-> (or file-path (io/resource resource-file-name))
                     io/file
                     io/input-stream)]
    (as-> in $
      (fst/workbook->datasets $ (assoc options
                                       :header-row? false
                                       :key-fn      keyword))
      (reduce (fn [m coll] (assoc m (tc/dataset-name coll) coll)) {} $))))

(defn ->ds
  "Load dataset using tab name from homelessness statistics data"
  [dataset-name & {::keys [resource-file-name file-path]
                   :or    {resource-file-name assessments-2025-file}}]
  (get (->map-of-datasets resource-file-name file-path) dataset-name))

(defn A1-data []
  (-> (->ds "A1")
      (tc/drop-rows (range 0 7)))) ;; need to define column header maps and apply

(def A1
  (let [raw (->ds "A1")
        cols (-> raw
                 (tc/select-rows (range 4 7))
                 (tc/info)
                 (tc/select-rows #(or (#{:column-0 :column-1} (:col-name %))
                                      (> (count (range 4 7)) (:n-missing %))))
                 :col-name
                 distinct
                 vec)
        col-titles (as-> raw $
                     (tc/select-rows $ (range 4 7))
                     (tc/select-columns $ cols)
                     (tc/add-columns $ {:column-0 "code"
                                        :column-1 "name"})
                     (tc/replace-missing $)
                     (tc/last $)
                     (tc/rows $)
                     (first $)
                     (map #(-> %
                               (s/replace #"\n" " ")
                               (s/replace "1,2,6" "")
                               (s/replace #"\d$" "")
                               (s/replace "area5" "area")
                               (s/replace "/" "")
                               (s/replace "(000s)" "1000")
                               (s/replace " -  " " ")
                               (s/replace " - " " ")
                               (s/replace "\"" "")
                               (s/replace "," "")
                               (s/replace "(" "")
                               (s/replace ")" "")
                               (s/lower-case)
                               (s/replace " " "-")
                               keyword) $))]
    (-> raw
        (tc/drop-rows (range 0 7))
        (tc/select-columns cols)
        (tc/rename-columns (reduce #(assoc %1 (first %2) (second %2)) {} (map vector cols col-titles)))
        (tc/drop-missing :code)
        (tc/select-rows #(s/starts-with? (:code %) "E")))))

(def A2P
  (let [raw (->ds "A2P")
        cols (-> raw
                 (tc/select-rows (range 3 7))
                 (tc/info)
                 (tc/select-rows #(or (#{:column-0 :column-1} (:col-name %))
                                      (> (count (range 3 7)) (:n-missing %))))
                 :col-name
                 distinct
                 vec)
        col-titles (as-> raw $
                     (tc/select-rows $ (range 3 7))
                     (tc/select-columns $ cols)
                     (tc/add-columns $ {:column-0 "code"
                                        :column-1 "name"})
                     (tc/replace-missing $)
                     (tc/last $)
                     (tc/rows $)
                     (first $)
                     (map #(-> %
                               (s/replace #"\n" " ")
                               (s/replace "1,2,6" "")
                               (s/replace #"\d$" "")
                               (s/replace "area5" "area")
                               (s/replace "/" "")
                               (s/replace "(000s)" "1000")
                               (s/replace " -  " " ")
                               (s/replace " - " " ")
                               (s/replace "\"" "")
                               (s/replace "," "")
                               (s/replace "(" "")
                               (s/replace ")" "")
                               (s/lower-case)
                               (s/replace " " "-")
                               keyword) $))]
    (-> raw
        (tc/drop-rows (range 0 7))
        (tc/select-columns cols)
        (tc/rename-columns (reduce #(assoc %1 (first %2) (second %2)) {} (map vector cols col-titles)))
        (tc/drop-missing :code)
        (tc/select-rows #(s/starts-with? (:code %) "E")))))

#_(def A2R
    (let [raw (->ds "A2R")
          cols (-> raw
                   (tc/select-rows (range 4 7))
                   (tc/drop-columns #(= :boolean %) :datatype))
          col-titles (as-> cols $
                       (tc/add-columns $ {:column-0 "code"
                                          :column-1 "name"})
                       (tc/replace-missing $)
                       (tc/last $)
                       (tc/rows $)
                       (first $)
                       (map #(-> %
                                 (s/replace #"\n" " ")
                                 (s/replace "1,2,6" "")
                                 (s/replace #"3$" "")
                                 (s/replace #"4$" "")
                                 (s/replace "area5" "area")
                                 (s/replace "/" "")
                                 (s/replace "(000s)" "1000")
                                 (s/replace " -  " " ")
                                 (s/replace " - " " ")
                                 (s/lower-case)
                                 (s/replace " " "-")
                                 keyword) $))]
      (-> raw
          (tc/drop-rows (range 0 7))
          (tc/select-columns (tc/column-names cols))
          (tc/rename-columns (reduce #(assoc %1 (first %2) (second %2)) {} (map vector (tc/column-names cols) col-titles)))
          (tc/drop-missing :code)
          (tc/select-rows #(s/starts-with? (:code %) "E")))))
