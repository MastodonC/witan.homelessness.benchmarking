^{:nextjournal.clerk/toc true}
(ns benchmarking.template
  {:nextjournal.clerk/visibility {:code   :hide
                                  :result :hide}}
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

(def number-homeless-per-000
  "relief duty owed per 000"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A1
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :households-assessed-as-homeless-per-1000]))))

(def number-threatened-w-homeless-per-000
  "prevention duty owed per 000"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A1
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :households-assessed-as-threatened-with-homelessness-per-1000]))))

(def total-homeless-end-of-ast
  "End of ensured shorthold private rented tenancy (AST)"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :total-end--of-ast]))))

(def total-homeless-end-of-non-ast
  "End of ensured non-AST private rented tenancy"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :end-of-non-ast-private-rented-tenancy]))))

(def total-homeless-family-friends
  "Family or friends no longer willing or able to accommodate"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :family-or-friends-no-longer-willing-or-able-to-accommodate]))))

(def total-homeless-non-violent-breakdown-w-partner
  "Non-violent relationship breakdown with partner"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :non-violent-relationship-breakdown-with-partner]))))

(def total-homeless-domestic-abuse
  "Total Domestic abuse"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :total-domestic-abuse]))))

(def total-homeless-violence-harassment
  "Other violence or harrassment"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :other-violence-or-harrassment]))))

(def total-homeless-end-of-social-tenancy
  "End of social rented tenancy"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :total-end-of-social-rented-tenancy]))))

(def total-homeless-eviction-supported-housing
  "Eviction from supported housing"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :total-evicted-from-supported-housing]))))

(def total-homeless-custody
  "Departure from custody/prison"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :custody]))))

(def total-homeless-psychiatric-hospital
  "Departure from psychiatric hospital"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :hospital-psychiatric]))))

(def total-homeless-general-hospital
  "Departure from general hospital"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :hospital-general]))))

(def total-homeless-lac
  "Departure from Looked After Child (LAC) placement"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :looked-after-child-placement]))))

(def total-homeless-asylum
  "Required to leave accommodation provided by Home Office as asylum support"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :required-to-leave-accommodation-provided-by-home-office-as-asylum-support]))))

(def total-homeless-disability-ill
  "Home no longer suitable - disability / ill health"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/map-columns :home-no-longer-suitable-disability-ill-health
                        [:home-no-longer-suitable-disability--ill-health-5
                         :home-no-longer-suitable-disability--ill-health-6]
                        (fn [h5 h6]
                          (cond
                            h6
                            h6
                            h5
                            h5
                            :else
                            nil)))
        (tc/select-columns [:date :name :quarter :year
                            :home-no-longer-suitable-disability-ill-health]))))

(def total-homeless-resettlement
  "Loss of placement or sponsorship provided through a resettlement scheme"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/map-columns :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                        [:loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                         :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme-6]
                        (fn [h5 h6]
                          (cond
                            h6
                            h6
                            h5
                            h5
                            :else
                            nil)))
        (tc/select-columns [:date :name :quarter :year
                            :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme]))))

(def total-homeless-other
  "Other reasons / not known5"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A2R
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/map-columns :other-reasons--not-known
                        [:other-reasons--not-known
                         :other-reasons--not-known5]
                        (fn [h5 h6]
                          (cond
                            h6
                            h6
                            h5
                            h5
                            :else
                            nil)))
        (tc/select-columns [:date :name :quarter :year
                            :other-reasons--not-known]))))

(
 ;; Charts
 )

(defn single-line-chart [{:keys [ds y-field y-title]}]
  {:data {:values (-> ds
                      (tc/select-rows #(#{la-name} (:name %)))
                      (tc/order-by :date)
                      (tc/rows :as-maps))}
   :mark {:type "line"}
   :encoding {:x {:field :date :type "temporal" :title "Quarter"}
              :y {:field y-field
                  :type "quantitative" :title y-title}
              :color {:field :name :type "nominal"}}})

(defn neighbour-comparison-boxplot
  [{:keys [neighbour-data la-name title y-field y-title x-field x-title max-y]
    :or {x-field :date
         x-title "Quarter"}}]
  (let [neighbours statistical-neighbours-pred
        la-data (-> neighbour-data
                    (tc/select-rows #(#{la-name} (:name %)))
                    (tc/order-by :date))
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
              :width 1000
              :showlegend false}
     :config {:displayModeBar false
              :displayLogo false}}))

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
           (clerk/vl (single-line-chart {:ds number-homeless
                                         :y-field :homeless-relief-duty-owed4
                                         :y-title "Count of homeless relief duty owed"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data number-homeless
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness w/Statistical Neighbours")
                                           :y-field :homeless-relief-duty-owed4
                                           :y-title "Count experiencing homelessness"})))

(mc-logo)

;; ---
;; ## Total threatened with homelessness
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds number-threatened-w-homeless
                                         :y-field :threatened-with-homelessness-within-56-days-prevention-duty-owed
                                         :y-title "Count of homeless prevention owed"}))

           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data number-threatened-w-homeless
                                           :la-name la-name
                                           :title (str la-name " Total Threatened with Homelessness w/Statistical Neighbours")
                                           :y-field :threatened-with-homelessness-within-56-days-prevention-duty-owed
                                           :y-title "Count threatened w/homelessness"})))

(mc-logo)

;; ---
;; ## Total experiencing homelessness per 1000
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds number-homeless-per-000
                                         :y-field :households-assessed-as-homeless-per-1000
                                         :y-title "Count of homeless relief owed per 1000"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data number-homeless-per-000
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness per 1000 w/Statistical Neighbours")
                                           :y-field :households-assessed-as-homeless-per-1000
                                           :y-title "Count experiencing homelessness per 1000"})))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to end of AST
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-end-of-ast
                                         :y-field :total-end--of-ast
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-end-of-ast
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to end of AST w/Statistical Neighbours")
                                           :y-field :total-end--of-ast
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to end of private non-AST
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-end-of-non-ast
                                         :y-field :end-of-non-ast-private-rented-tenancy
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-end-of-non-ast
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to end of non-AST w/Statistical Neighbours")
                                           :y-field :end-of-non-ast-private-rented-tenancy
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to family or friends no longer willing or able to accomodate
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-family-friends
                                         :y-field :family-or-friends-no-longer-willing-or-able-to-accommodate
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-family-friends
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to family & friends no longer able or willing w/Statistical Neighbours")
                                           :y-field :family-or-friends-no-longer-willing-or-able-to-accommodate
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to non-violent relationshio breakdown with partner
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-non-violent-breakdown-w-partner
                                         :y-field :non-violent-relationship-breakdown-with-partner
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-non-violent-breakdown-w-partner
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to non-violent relationship breakdown with partner w/Statistical Neighbours")
                                           :y-field :non-violent-relationship-breakdown-with-partner
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to domestic abuse
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-domestic-abuse
                                         :y-field :total-domestic-abuse
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-domestic-abuse
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to domestic abuse w/Statistical Neighbours")
                                           :y-field :total-domestic-abuse
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to violence or harassment
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-violence-harassment
                                         :y-field :other-violence-or-harrassment
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-violence-harassment
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to violence or harassment w/Statistical Neighbours")
                                           :y-field :other-violence-or-harrassment
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to end of social rented tenancy
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-end-of-social-tenancy
                                         :y-field :total-end-of-social-rented-tenancy
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-end-of-social-tenancy
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to end of social rented tenancy w/Statistical Neighbours")
                                           :y-field :total-end-of-social-rented-tenancy
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to eviction from supported housing
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-eviction-supported-housing
                                         :y-field :total-evicted-from-supported-housing
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-eviction-supported-housing
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to eviction from supported housing w/Statistical Neighbours")
                                           :y-field :total-evicted-from-supported-housing
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to departure from custody
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-custody
                                         :y-field :custody
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-custody
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to departure from custody w/Statistical Neighbours")
                                           :y-field :custody
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to departure from psychiatric hospital
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-psychiatric-hospital
                                         :y-field :hospital-psychiatric
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-psychiatric-hospital
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to departure from psychiatric hospital w/Statistical Neighbours")
                                           :y-field :hospital-psychiatric
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to departure from general hospital
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-general-hospital
                                         :y-field :hospital-general
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-general-hospital
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to departure from geeneral hospital w/Statistical Neighbours")
                                           :y-field :hospital-general
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to departure from LAC placement
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-lac
                                         :y-field :looked-after-child-placement
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-lac
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to departure from LAC placement w/Statistical Neighbours")
                                           :y-field :looked-after-child-placement
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to being required to leave accomodation provided by HO as asylum support
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-asylum
                                         :y-field :required-to-leave-accommodation-provided-by-home-office-as-asylum-support
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-asylum
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to departure from HO asylum support w/Statistical Neighbours")
                                           :y-field :required-to-leave-accommodation-provided-by-home-office-as-asylum-support
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to disability/ill health
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-disability-ill
                                         :y-field :home-no-longer-suitable-disability-ill-health
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-disability-ill
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to disability/ill health w/Statistical Neighbours")
                                           :y-field :home-no-longer-suitable-disability-ill-health
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to loss of placement or sponsorship through a resettlement scheme
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-resettlement
                                         :y-field :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-resettlement
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to loss of placement in resettlement scheme w/Statistical Neighbours")
                                           :y-field :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to other/unknown reasons
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds total-homeless-other
                                         :y-field :other-reasons--not-known
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data total-homeless-other
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to other/unknown reason(s) w/Statistical Neighbours")
                                           :y-field :other-reasons--not-known
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

(comment
  ;; TODO
  ;; Reasons for threatened homelessness between LAs
  ;; how do those threatened with homelessness numbers compare to those who are experienceing homelessness within reason facing homelessness
  ;; Reasons for homelessness within an LA across time change
  )
