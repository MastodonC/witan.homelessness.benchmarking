(ns witan.homelessness.benchmarking.statistical-neighbours
  (:require
   [clojure.string :as s]
   [clojure.java.io :as io]
   [tablecloth.api :as tc]
   [witan.homelessness.benchmarking.assessments :as bass]))

(def lait-model-long-path "./lait-2025/sn-model-long.csv")

(def global-model-path "./Clustering-similar-local-authorities-in-the-UK/clusteringsimilarlocalauthoritiesandnearestneighbours.xlsx")

(def lait-model
  (with-open [in (-> (io/resource lait-model-long-path)
                     io/file
                     io/input-stream)]
    (-> in
        (tc/dataset {:key-fn (fn [k] (-> k s/lower-case keyword))
                     :file-type :csv}))))

(def global-model
  (let [global-neighbours-raw (-> {:resource-file-name global-model-path}
                                  bass/->map-of-datasets
                                  (get  "7.a"))
        headers (-> global-neighbours-raw
                    (tc/drop-rows (range 0 3))
                    (tc/select-rows 0)
                    (tc/rows :as-seq)
                    first)
        global-neighbours-ds (-> global-neighbours-raw
                                 (tc/drop-rows (range 0 4))
                                 (tc/rename-columns (->>  headers
                                                          (map (fn [x y] (assoc {} x y)) (keys global-neighbours-raw))
                                                          (reduce merge))))]
    (-> global-neighbours-ds
        (tc/select-columns (into ["Local Authority Code" "Local Authority Name"] (->> global-neighbours-ds keys (filter #(clojure.string/includes? % "code")))))
        (tc/pivot->longer (complement #{"Local Authority Code" "Local Authority Name"}) {:target-columns :sn
                                                                                         :splitter #"Neighbour (.*): code"
                                                                                         :value-column-name :sn_code})
        (tc/left-join (-> global-neighbours-ds
                          (tc/select-columns (into ["Local Authority Code" "Local Authority Name"] (->> global-neighbours-ds keys (filter #(clojure.string/includes? % "name")))))
                          (tc/pivot->longer (complement #{"Local Authority Code" "Local Authority Name"}) {:target-columns :sn
                                                                                                           :splitter #"Neighbour (.*): name"
                                                                                                           :value-column-name :sn_name})) ["Local Authority Code" "Local Authority Name" :sn])
        (tc/drop-columns ["7.a-right.Local Authority Code" "7.a-right.Local Authority Name" :7.a-right.sn])
        (tc/rename-columns {"Local Authority Code" :la_code
                            "Local Authority Name" :la_name})
        (tc/add-column :sn_prox nil)
        (tc/order-by [:la_code :sn]))))

(def unitary-authorities
  (delay
    (into (sorted-set) (:code @bass/A1))))

(defn neighbours
  ([la-name model]
   (let [result (-> model
                    (tc/select-rows #(@unitary-authorities (:sn_code %)))
                    (tc/select-rows #(@unitary-authorities (:la_code %)))
                    (tc/select-rows #(= la-name (:la_name %))))]
     (cond
       (< 10 (tc/row-count result))
       (tc/select-rows result (range 0 10))
       :else
       result)))
  ([la-name]
   (neighbours la-name lait-model)))

(defn neighbours-name-pred
  ([la-name model]
   (into (sorted-set) (-> la-name
                          (neighbours model)
                          :sn_name)))
  ([la-name]
   (neighbours-name-pred la-name lait-model)))

(comment

  (tc/info model)
  ;; => ./src-data/lait-2025/sn-model-long.csv: descriptive-stats [6 12]:
  ;;    | :col-name | :datatype | :n-valid | :n-missing | :min | :mean |           :mode | :max | :standard-deviation | :skew |               :first |           :last |
  ;;    |-----------|-----------|---------:|-----------:|-----:|------:|-----------------|-----:|--------------------:|------:|----------------------|-----------------|
  ;;    |  :la_code |   :string |     1530 |          0 |      |       |       E06000062 |      |                     |       |            E06000001 |       E10000034 |
  ;;    |  :la_name |   :string |     1530 |          0 |      |       |       St Helens |      |                     |       |           Hartlepool |  Worcestershire |
  ;;    |       :sn |    :int16 |     1530 |          0 |  1.0 |   5.5 |                 | 10.0 |          2.87322044 |   0.0 |                    1 |              10 |
  ;;    |  :sn_code |   :string |     1530 |          0 |      |       |       E10000013 |      |                     |       |            E06000003 |       E06000065 |
  ;;    |  :sn_name |   :string |     1530 |          0 |      |       | Gloucestershire |      |                     |       | Redcar and Cleveland | North Yorkshire |
  ;;    |  :sn_prox |   :string |     1530 |          0 |      |       |      Very close |      |                     |       |           Very close |      Very close |


  (neighbours "Surrey")
  ;; => ./src-data/lait-2025/sn-model-long.csv [10 6]:
  ;;    |  :la_code | :la_name | :sn |  :sn_code |               :sn_name |   :sn_prox |
  ;;    |-----------|----------|----:|-----------|------------------------|------------|
  ;;    | E10000030 |   Surrey |   1 | E06000040 | Windsor and Maidenhead | Very close |
  ;;    | E10000030 |   Surrey |   2 | E06000060 |        Buckinghamshire | Very close |
  ;;    | E10000030 |   Surrey |   3 | E08000009 |               Trafford | Very close |
  ;;    | E10000030 |   Surrey |   4 | E10000015 |          Hertfordshire | Very close |
  ;;    | E10000030 |   Surrey |   5 | E06000037 |         West Berkshire |      Close |
  ;;    | E10000030 |   Surrey |   6 | E06000056 |   Central Bedfordshire |      Close |
  ;;    | E10000030 |   Surrey |   7 | E06000041 |              Wokingham |      Close |
  ;;    | E10000030 |   Surrey |   8 | E10000025 |            Oxfordshire |      Close |
  ;;    | E10000030 |   Surrey |   9 | E06000036 |       Bracknell Forest |      Close |
  ;;    | E10000030 |   Surrey |  10 | E10000003 |         Cambridgeshire |      Close |

  (neighbours-name-pred "Surrey")
  ;; => #{"Bracknell Forest"
  ;;      "Buckinghamshire"
  ;;      "Cambridgeshire"
  ;;      "Central Bedfordshire"
  ;;      "Hertfordshire"
  ;;      "Oxfordshire"
  ;;      "Trafford"
  ;;      "West Berkshire"
  ;;      "Windsor and Maidenhead"
  ;;      "Wokingham"}

  )
