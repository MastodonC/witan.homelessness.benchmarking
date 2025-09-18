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
  "Load dataset using tab name from homelessness statistics data using the name
   of the tab and the spreadsheet row numbers where the headers exist between"
  [dataset-name first-row-with-headers last-row-with-headers
   & {::keys [resource-file-name file-path]
      :or    {resource-file-name assessments-2025-file}}]
  (let [raw (get (->map-of-datasets resource-file-name file-path) dataset-name)
        headers-range (range (dec first-row-with-headers) last-row-with-headers)
        cols (-> raw
                 (tc/select-rows headers-range)
                 (tc/info)
                 (tc/select-rows #(or (#{:column-0 :column-1} (:col-name %))
                                      (> (count headers-range) (:n-missing %))))
                 :col-name
                 distinct
                 vec)
        col-titles (as-> raw $
                     (tc/select-rows $ headers-range)
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
        (tc/drop-rows (range 0 (inc last-row-with-headers)))
        (tc/select-columns cols)
        (tc/rename-columns (reduce #(assoc %1 (first %2) (second %2)) {} (map vector cols col-titles)))
        (tc/drop-missing :code)
        (tc/select-rows #(s/starts-with? (:code %) "E")))))


(def A1-data
  (->ds "A1" 5 7))

(def A2P-data
  (->ds "A2P" 4 7))

(def A2R-data
  (->ds "A2R" 5 7))

(def A3-data
  (->ds "A3" 4 5))

(def A4P-data
  (->ds "A4P" 4 6))

(def A4R-data
  (->ds "A4R" 4 6))

(def A5P-data
  (->ds "A5P" 4 5))

(def A5R-data
  (->ds "A5R" 4 5))

(def A6-data
  (->ds "A6" 4 4))

(def A7-data
  (->ds "A7" 6 8))

(def A8-data
  (->ds "A8" 4 6))

(def A10-data
  (->ds "A10" 4 6))

(def A12-data
  (->ds "A12" 2 6))

(def A13-data
  (->ds "A13" 5 5))

(def P1
  (->ds "P1" 5 6))

(def P2
  (->ds "P2" 4 6))

(def P3
  (->ds "P3" 4 4))

(def P5
  (->ds "P5" 4 5))

(def R1
  (->ds "R1" 5 6))

(def R2
  (->ds "R2" 4 6))

(def R3
  (->ds "R3" 4 4))

(def R5
  (->ds "R5" 4 5))

(def MD1
  (->ds "MD1" 6 6))

(def MD2
  (->ds "MD2" 6 7))

(def MD3
  (->ds "MD3" 6 7))

(def TA1
  (->ds "TA1" 6 7))

(def TA2
  (->ds "TA2" 7 8))

(def TA3
  (->ds "TA3" 5 7))
