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
