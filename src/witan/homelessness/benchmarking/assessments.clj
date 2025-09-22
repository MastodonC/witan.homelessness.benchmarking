(ns witan.homelessness.benchmarking.assessments
  (:require
   [clojure.java.io :as io]
   [clojure.string :as s]
   [tablecloth.api :as tc]
   [tech.v3.libs.fastexcel :as fst]))

(def assessments-202503 {:resource-file-name "./homelessness-statistics/Detailed_LA_202503.xlsx"
                         :quarter "Jan-Mar"
                         :year 2025})
;; This is Jan - Mar 2025, hence the "03"

(def assessments-202412 {:resource-file-name "./homelessness-statistics/Detailed_LA_202412.xlsx"
                         :quarter "Oct-Dec"
                         :year 2024})

(def assessments-202409 {:resource-file-name "./homelessness-statistics/Detailed_LA_202409.xlsx"
                         :quarter "Jul-Sep"
                         :year 2024})

(def assessments-202406 {:resource-file-name "./homelessness-statistics/Detailed_LA_202406_fix.xlsx"
                         :quarter "Apr-Jun"
                         :year 2024})

(def assessments-202403 {:resource-file-name "./homelessness-statistics/Detailed_LA_202403.xlsx"
                         :quarter "Jan-Mar"
                         :year 2024})

(def assessments-202312 {:resource-file-name "./homelessness-statistics/Detailed_LA_202312.xlsx"
                         :quarter "Oct-Dec"
                         :year 2023})

(def assessments-202309 {:resource-file-name "./homelessness-statistics/Detailed_LA_202309_fixed.xlsx"
                         :quarter "Jul-Sep"
                         :year 2023})

(def assessments-202306 {:resource-file-name "./homelessness-statistics/Detailed_LA_202306_all_dropdowns_fix.xlsx"
                         :quarter "Apr-Jun"
                         :year 2023})

(def assessments-202303 {:resource-file-name "./homelessness-statistics/Detailed_LA_202303.xlsx"
                         :quarter "Jan-Dec"
                         :year 2023})

(def assessments-202212 {:resource-file-name "./homelessness-statistics/Detailed_LA_202212.xlsx"
                         :quarter "Oct-Dec"
                         :year 2022})

(def assessments-202209 {:resource-file-name "./homelessness-statistics/Detailed_LA_202209.xlsx"
                         :quarter "Jul-Sep"
                         :year 2022})

(def assessments-202206 {:resource-file-name "./homelessness-statistics/Detailed_LA_202206.xlsx"
                         :quarter "Apr-Jun"
                         :year 2022})

(def assessments-202203 {:resource-file-name "./homelessness-statistics/Detailed_LA_202203.xlsx"
                         :quarter "Jan-Mar"
                         :year 2022})

(def assessments-202112 {:resource-file-name "./homelessness-statistics/DetailedLA_202112.xlsx"
                         :quarter "Oct-Dec"
                         :year 2021})

(def assessments-202109 {:resource-file-name "./homelessness-statistics/DetailedLA_202109_fixed.xlsx"
                         :quarter "Jul-Sep"
                         :year 2021})

(def assessments-202106 {:resource-file-name "./homelessness-statistics/DetailedLA_202106.xlsx"
                         :quarter "Apr-Jun"
                         :year 2021})

(def assessments-202103 {:resource-file-name "./homelessness-statistics/DetailedLA_202103_revised.xlsx"
                         :quarter "Jan-Mar"
                         :year 2021})

(def assessments-202012 {:resource-file-name "./homelessness-statistics/DetailedLA_202012.xlsx"
                         :quarter "Oct-Dec"
                         :year 2020})

(def assessments-202009 {:resource-file-name "./homelessness-statistics/DetailedLA_202009_REVISED.xlsx"
                         :quarter "Jul-Sep"
                         :year 2020})
;; prior to this date the returns are less consistent in terms of the tables included
;; there was likely a development process as what was actually useful was ascertained

(def assessments-202006-file "./homelessness-statistics/DetailedLA_202006.xlsx")

(def assessments-202003-file "./homelessness-statistics/DetailedLA_202003.xlsx")

(def assessments-201912-file "./homelessness-statistics/DetailedLA_201912.xlsx")

(def assessments-201909-file "./homelessness-statistics/DetailedLA_201909_revised.xlsx")

(def assessments-201906-file "./homelessness-statistics/DetailedLA_201906_revised.xlsx")

(def assessments-201903-file "./homelessness-statistics/DetailedLA_201903_revised.xlsx")

(def assessments-201812-file "./homelessness-statistics/DetailedLA_201812.xlsx")

(def assessments-201809-file "./homelessness-statistics/DetailedLA_201809.xlsx")

(def assessments-201806-file "./homelessness-statistics/DetailedLA_201806.xlsx")

(def assessments-data
  "not including data prior to June 2020"
  [assessments-202503
   assessments-202412
   assessments-202409
   assessments-202406
   assessments-202403
   assessments-202312
   assessments-202309
   assessments-202306
   assessments-202303
   assessments-202212
   assessments-202209
   assessments-202206
   assessments-202203
   assessments-202112
   assessments-202109
   assessments-202106
   assessments-202103
   assessments-202012
   assessments-202009])

(defn ->map-of-datasets
  "Read homelessness statistics data from xlsx (converted from ods in
   LibreOffice Calc)"
  [& {:keys [resource-file-name file-path options]
      :or    {resource-file-name assessments-202503-file}}]
  (with-open [in (-> (or file-path (io/resource resource-file-name))
                     io/file
                     io/input-stream)]
    (as-> in $
      (fst/workbook->datasets $ (assoc options
                                       :header-row? false
                                       :key-fn      keyword))
      (reduce (fn [m coll] (assoc m (tc/dataset-name coll) coll)) {} $))))

(defn find-header-range [ds & {:keys [rows]
                               :or   {rows [0]}}]
  (range 1 (-> ds
               (tc/drop-rows rows)
               (tc/add-column :row (range))
               (tc/select-rows #(string? (:column-0 %)))
               (tc/select-rows 0)
               :row
               first
               inc)))

(defn sheet->ds
  "Load dataset using tab name from homelessness statistics data using the name
   of the tab and the spreadsheet row numbers where the headers exist between"
  [dataset-name & {:keys [resource-file-name file-path]
                   :or    {resource-file-name assessments-202503-file}}]
  (let [raw (if-let [ds (get (->map-of-datasets {:resource-file-name resource-file-name
                                                 :file-path file-path}) dataset-name)]
              ds (get (->map-of-datasets {:resource-file-name resource-file-name
                                          :file-path file-path}) (str dataset-name "_")))
        headers-range (if (= dataset-name "A13")
                        (find-header-range raw {:rows [0 1]})
                        (find-header-range raw))
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
                               ;;(s/replace #"\d$" "") ;; cuts out some numbered column headers
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
        (tc/drop-rows (range 0 (last headers-range)))
        (tc/select-columns cols)
        (tc/rename-columns (reduce #(assoc %1 (first %2) (second %2)) {} (map vector cols col-titles)))
        (tc/drop-missing :code)
        (tc/select-rows #(s/starts-with? (:code %) "E")))))


(def A1-data
  (->ds "A1"))

(def A2P-data
  (->ds "A2P"))

(def A2R-data
  (->ds "A2R"))

(def A3-data
  (->ds "A3"))

(def A4P-data
  (->ds "A4P"))

(def A4R-data
  (->ds "A4R"))

(def A5P-data
  (->ds "A5P"))

(def A5R-data
  (->ds "A5R"))

(def A6-data
  (->ds "A6"))

(def A7-data
  (->ds "A7"))

(def A8-data
  (->ds "A8"))

(def A10-data
  (->ds "A10"))

(def A12-data
  (->ds "A12"))

(def A13-data
  (->ds "A13"))

(def P1
  (->ds "P1"))

(def P2
  (->ds "P2"))

(def P3
  (->ds "P3"))

(def P5
  (->ds "P5"))

(def R1
  (->ds "R1"))

(def R2
  (->ds "R2"))

(def R3
  (->ds "R3"))

(def R5
  (->ds "R5"))

(def MD1
  (->ds "MD1"))

(def MD2
  (->ds "MD2"))

(def MD3
  (->ds "MD3"))

(def TA1
  (->ds "TA1"))

(def TA2
  (->ds "TA2"))

(def TA3
  (->ds "TA3"))
