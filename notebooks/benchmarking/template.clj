^{:nextjournal.clerk/toc true}
(ns benchmarking.template
  {:nextjournal.clerk/visibility {:code   :hide
                                  :result :hide}
   :nextjournal.clerk/page-size            nil
   :nextjournal.clerk/auto-expand-results? true
   :nextjournal.clerk/budget               nil}
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk-slideshow :as slideshow]
            [tablecloth.api :as tc]
            [witan.send.adroddiad.clerk.html :as chtml]
            [witan.homelessness.benchmarking.assessments :as bass]
            [witan.homelessness.benchmarking.regional-neighbours :as rn]
            [witan.homelessness.benchmarking.statistical-neighbours :as sn]
            [clojure.string :as s]))

(def la-name "Camden")

(def out-dir "doc/")

(defn output-ns [ns]
  (let [ns-str (str ns)
        pathified-namepace (str/replace ns-str #"\.|-" {"." "/" "-" "_"})
        in-path (str "notebooks/" pathified-namepace ".clj")
        out-path (str out-dir
                      (-> la-name
                          (str/replace #"\.|-| |,"
                                       {"." "/"
                                        "-" "_"
                                        "," "_"
                                        " " "_"})
                          (str ".html")))
        index-out (str out-dir "index.html")]
    (clerk/build! {:paths    [in-path]
                   :ssr true
                   :bundle   true
                   :out-path out-dir})
    [(.renameTo (io/file index-out) (io/file out-path)) index-out out-path]))

(comment

  (output-ns *ns*)

  )

(def mc-logo-file (io/resource "logo_mastodonc.png"))

(defn mc-logo []
  (clerk/html
   {::clerk/width :full}
   [:div.h-full.max-h-full.bottom-0.-right-12.absolute (clerk/image mc-logo-file)]))

(clerk/add-viewers! [slideshow/viewer])

(def region (rn/region-name la-name))

(def regional-neighbours (rn/neighbours la-name))
(def regional-neighbours-pred (rn/neighbours-name-pred la-name))

(def statistical-neighbours (sn/neighbours la-name))
(def statistical-neighbours-pred (sn/neighbours-name-pred la-name))

(
 ;; Data
 )

(def number-homeless
  "relief duty owed"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A1
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :homeless-relief-duty-owed4]))))

(def number-threatened-w-homeless
  "prevention duty owed"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A1
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :threatened-with-homelessness-within-56-days-prevention-duty-owed]))))

(defn neighbour-comparison-boxplot
  [{:keys [neighbour-data la-name title y-field y-title x-field x-title max-y]
    :or {x-field :date
         x-title "Quarter"}}]
  (let [la-data (-> neighbour-data
                    (tc/select-rows #(#{la-name} (:name %))))
        box-data (transduce
                  identity
                  (fn
                    ([] {})
                    ([acc]
                     (into []
                           (map (fn [[k v]]
                                  {:x k
                                   :y (:y v)
                                   :text (:text v)
                                   :name k
                                   :marker {:color "orange"}
                                   :boxpoints "all"
                                   :pointpos -1.8
                                   :jitter 0.3
                                   :type "box"}))
                           acc))
                    ([acc x]
                     (-> acc
                         (update-in [(x-field x) :y] conj (y-field x))
                         (update-in [(x-field x) :text] conj (:name x)))))
                  (-> neighbour-data
                      (tc/drop-rows #(#{la-name} (:name %)))
                      (tc/rows :as-maps)))]
    {:data (conj
            box-data
            {:x (into [] (la-data x-field))
             :y (into [] (la-data y-field))
             :text (into [] (la-data :name))
             :name la-name
             :marker {:color "blue" :size 14 :symbol "star-diamond"}
             :mode "markers"
             :type "scatter"})
     :layout {:title {:text title}
              :scattermode "group"
              :scattergap 0.7
              :xaxis {:title x-title}
              :yaxis {:rangemode "tozero" :title y-title :range (when max-y [0 max-y])}
              :height 600
              :width 1400
              :showlegend false}
     :config {:displayModeBar false
              :displayLogo false}}))

(defn plotly-total-homeless-neighbour-comparison
  [la-name neighbours]
  (-> (neighbour-comparison-boxplot
       (let [neighbours statistical-neighbours-pred]
         {:neighbour-data (-> number-homeless
                              (tc/select-rows #(#{2024 2025} (:year %)))
                              (tc/order-by :date))
          :la-name la-name
          :title (str la-name " Total Experiencing Homelessness w/Statistical Neighbours")
          :y-field :homeless-relief-duty-owed4
          :y-title "Count experiencing homelessness"
          }))
      (assoc-in [:layout :height] 375)
      (assoc-in [:layout :width] 500)))

(defn plotly-total-threatened-w-homeless-neighbour-comparison
  [la-name neighbours]
  (-> (neighbour-comparison-boxplot
       (let [neighbours statistical-neighbours-pred]
         {:neighbour-data (-> number-threatened-w-homeless
                              (tc/select-rows #(#{2024 2025} (:year %)))
                              (tc/order-by :date))
          :la-name la-name
          :title (str la-name " Total Threatened with Homelessness w/Statistical Neighbours")
          :y-field :threatened-with-homelessness-within-56-days-prevention-duty-owed
          :y-title "Count threatened w/homelessness"
          }))
      (assoc-in [:layout :height] 375)
      (assoc-in [:layout :width] 500)))

(
;;; Deck
 )
{::clerk/visibility {:result :show}}

(clerk/row
 {::clerk/width :full}
 (clerk/html
;;; Title Page
  {::clerk/width :full}
  [:div.max-w-screen-2xl.font-sans
   [:h1.text-6xl.font-extrabold.mb-12
    (format "Homelessness Benchmarking Results for %s" la-name)]
   [:p.text-4xl.font-bold.italic "Presented by Mastodon C"]
   [:p.text-3xl "Use ⬅️➡️ keys to navigate and ESC to see an overview."]]))

(mc-logo)

;; ---
;;; ## Statistical Nearest Neighbours
(clerk/row
 {::clerk/width :full}
 (clerk/table
  (-> statistical-neighbours
      (tc/select-columns [:sn :sn_name :sn_prox])
      (tc/rename-columns {:sn_name "Neighbour Name"
                          :sn "Neighbour Rank"
                          :sn_prox "Statistical Proximity"}))))

(mc-logo)

;; ---
;; ## Total experiencing homelessness
(clerk/row {::clerk/width :full}
           (clerk/vl {:data {:values (-> number-homeless
                                         (tc/select-rows #(#{la-name} (:name %)))
                                         (tc/order-by :date)
                                         (tc/rows :as-maps))}
                      :mark {:type "line"}
                      :encoding {:x {:field :date :type "temporal"}
                                 :y {:field :homeless-relief-duty-owed4 :type "quantitative"}
                                 :color {:field :name :type "nominal"}}})
           (clerk/plotly
            (plotly-total-homeless-neighbour-comparison
             la-name statistical-neighbours-pred)))

(mc-logo)

;; ---
;; ## Total threatened with homelessness
(clerk/row {::clerk/width :full}
           (clerk/vl {:data {:values (-> number-threatened-w-homeless
                                         (tc/select-rows #(#{la-name} (:name %)))
                                         (tc/order-by :date)
                                         (tc/rows :as-maps))}
                      :mark {:type "line"}
                      :encoding {:x {:field :date :type "temporal"}
                                 :y {:field :threatened-with-homelessness-within-56-days-prevention-duty-owed
                                     :type "quantitative" :title "Count threatened w/homelessness"}
                                 :color {:field :name :type "nominal"}}})
           (clerk/plotly
            (plotly-total-threatened-w-homeless-neighbour-comparison
             la-name statistical-neighbours-pred)))

(mc-logo)

(comment
  ;; TODO
  ;; Households assessed as threatened with homelessness per (000s)
  ;; Households assessed as homeless per (000s)
  )
