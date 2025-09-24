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

(def la-name "Waltham Forest")

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

(def A2R
  (let [raw @bass/A2R]
    {:total-homeless-end-of-ast
     {:title "End of ensured shorthold private rented tenancy (AST)"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :total-end--of-ast]))}
     :total-homeless-end-of-non-ast
     {:title "End of ensured non-AST private rented tenancy"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :end-of-non-ast-private-rented-tenancy]))}
     :total-homeless-family-friends
     {:title "Family or friends no longer willing or able to accommodate"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :family-or-friends-no-longer-willing-or-able-to-accommodate]))}
     :total-homeless-non-violent-breakdown-w-partner
     {:title "Non-violent relationship breakdown with partner"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :non-violent-relationship-breakdown-with-partner]))}
     :total-homeless-domestic-abuse
     {:title "Total Domestic abuse"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :total-domestic-abuse]))}
     :total-homeless-violence-harassment
     {:title "Other violence or harrassment"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :other-violence-or-harrassment]))}
     :total-homeless-end-of-social-tenancy
     {:title "End of social rented tenancy"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :total-end-of-social-rented-tenancy]))}
     :total-homeless-eviction-supported-housing
     {:title "Eviction from supported housing"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :total-evicted-from-supported-housing]))}
     :total-homeless-custody
     {:title "Departure from custody/prison"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :custody]))}
     :total-homeless-psychiatric-hospital
     {:title "Departure from psychiatric hospital"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :hospital-psychiatric]))}
     :total-homeless-general-hospital
     {:title "Departure from general hospital"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :hospital-general]))}
     :total-homeless-lac
     {:title "Departure from Looked After Child (LAC) placement"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :looked-after-child-placement]))}
     :total-homeless-asylum
     {:title "Required to leave accommodation provided by Home Office as asylum support"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :required-to-leave-accommodation-provided-by-home-office-as-asylum-support]))}
     :total-homeless-disability-ill
     {:title "Home no longer suitable - disability / ill health"
      :neighbours statistical-neighbours-pred
      :data (-> raw
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
                                    :home-no-longer-suitable-disability-ill-health]))}
     :total-homeless-resettlement
     {:title "Loss of placement or sponsorship provided through a resettlement scheme"
      :neighbours statistical-neighbours-pred
      :data (-> raw
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
                                    :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme]))}
     :total-homeless-other
     {:title "Other reasons / not known5"
      :neighbours statistical-neighbours-pred
      :data (-> raw
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
                                    :other-reasons--not-known]))}}))

(def A2P
  (let [raw @bass/A2P]
    {:total-homeless-end-of-ast
     {:title "End of ensured shorthold private rented tenancy (AST)"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :total-end--of-ast]))}
     :total-homeless-end-of-non-ast
     {:title "End of ensured non-AST private rented tenancy"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :end-of-non-ast-private-rented-tenancy]))}
     :total-homeless-family-friends
     {:title "Family or friends no longer willing or able to accommodate"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :family-or-friends-no-longer-willing-or-able-to-accommodate]))}
     :total-homeless-non-violent-breakdown-w-partner
     {:title "Non-violent relationship breakdown with partner"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :non-violent-relationship-breakdown-with-partner]))}
     :total-homeless-domestic-abuse
     {:title "Total Domestic abuse"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :total-domestic-abuse]))}
     :total-homeless-violence-harassment
     {:title "Other violence or harrassment"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :other-violence-or-harrassment]))}
     :total-homeless-end-of-social-tenancy
     {:title "End of social rented tenancy"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :total-end-of-social-rented-tenancy]))}
     :total-homeless-eviction-supported-housing
     {:title "Eviction from supported housing"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :total-evicted-from-supported-housing]))}
     :total-homeless-custody
     {:title "Departure from custody/prison"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :custody]))}
     :total-homeless-psychiatric-hospital
     {:title "Departure from psychiatric hospital"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :hospital-psychiatric]))}
     :total-homeless-general-hospital
     {:title "Departure from general hospital"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :hospital-general]))}
     :total-homeless-lac
     {:title "Departure from Looked After Child (LAC) placement"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :looked-after-child-placement]))}
     :total-homeless-asylum
     {:title "Required to leave accommodation provided by Home Office as asylum support"
      :neighbours statistical-neighbours-pred
      :data (-> raw
                (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
                (tc/select-columns [:date :name :quarter :year
                                    :required-to-leave-accommodation-provided-by-home-office-as-asylum-support]))}
     :total-homeless-disability-ill
     {:title "Home no longer suitable - disability / ill health"
      :neighbours statistical-neighbours-pred
      :data (-> raw
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
                                    :home-no-longer-suitable-disability-ill-health]))}
     :total-homeless-resettlement
     {:title "Loss of placement or sponsorship provided through a resettlement scheme"
      :neighbours statistical-neighbours-pred
      :data (-> raw
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
                                    :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme]))}
     :total-homeless-other
     {:title "Other reasons / not known5"
      :neighbours statistical-neighbours-pred
      :data (-> raw
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
                                    :other-reasons--not-known]))}}))

(def la-reasons-for-homelessness
  (-> @bass/A2R
      (tc/select-rows #(#{la-name} (:name %)))
      (tc/order-by :date)
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
      (tc/pivot->longer (complement #{:code :name :quarter :year}))
      (tc/rename-columns {:$column :reason :$value :count})
      (tc/drop-rows #(#{:date :total-owed-a-relief-duty1} (:reason %)))))

(def summarised-reason-for-homelessness-keys
  #{:total-end--of-ast
    :end-of-non-ast-private-rented-tenancy
    :family-or-friends-no-longer-willing-or-able-to-accommodate
    :non-violent-relationship-breakdown-with-partner
    :total-domestic-abuse
    :other-violence-or-harrassment
    :total-end-of-social-rented-tenancy
    :total-evicted-from-supported-housing
    :total-departure-from-institution
    :required-to-leave-accommodation-provided-by-home-office-as-asylum-support
    :home-no-longer-suitable-disability--ill-health
    :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
    :other-reasons--not-known})

(def summarised-reason-for-homelessness
  (-> la-reasons-for-homelessness
      (tc/select-rows #(summarised-reason-for-homelessness-keys (:reason %)))))

(def specific-reasons-for-homelessness
  (-> la-reasons-for-homelessness
      (tc/select-rows #((complement (apply disj summarised-reason-for-homelessness-keys [:end-of-non-ast-private-rented-tenancy
                                                                                         :family-or-friends-no-longer-willing-or-able-to-accommodate
                                                                                         :non-violent-relationship-breakdown-with-partner
                                                                                         :other-violence-or-harrassment
                                                                                         :required-to-leave-accommodation-provided-by-home-office-as-asylum-support
                                                                                         :home-no-longer-suitable-disability--ill-health
                                                                                         :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                                                                                         :other-reasons--not-known])) (:reason %)))
      (tc/drop-rows #(#{:total-owed-a-relief-duty1 :date} (:reason %)))))

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

(defn la-comparison-boxplot
  [{:keys [la-data title x-field x-title y-field y-title]
    :or {title ""}}]
  (let [box-data (transduce
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
                                   :jitter 0.3
                                   :type "box"}))
                           acc))
                    ([acc x]
                     (-> acc
                         (update-in [(x-field x) :y] conj (y-field x))
                         (update-in [(x-field x) :text] conj (:name x)))))
                  (tc/rows la-data :as-maps))]
    {:data box-data
     :layout {:title {:text title}
              :scattermode "group"
              :scattergap 0.7
              :xaxis {:title x-title}
              :yaxis {:rangemode "tozero" :title y-title}
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
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-end-of-ast :data)
                                         :y-field :total-end--of-ast
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-end-of-ast :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to end of AST w/Statistical Neighbours")
                                           :y-field :total-end--of-ast
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to end of private non-AST
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-end-of-non-ast :data)
                                         :y-field :end-of-non-ast-private-rented-tenancy
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-end-of-non-ast :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to end of non-AST w/Statistical Neighbours")
                                           :y-field :end-of-non-ast-private-rented-tenancy
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to family or friends no longer willing or able to accomodate
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-family-friends :data)
                                         :y-field :family-or-friends-no-longer-willing-or-able-to-accommodate
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-family-friends :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to family & friends no longer able or willing w/Statistical Neighbours")
                                           :y-field :family-or-friends-no-longer-willing-or-able-to-accommodate
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to non-violent relationship breakdown with partner
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-non-violent-breakdown-w-partner :data)
                                         :y-field :non-violent-relationship-breakdown-with-partner
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-non-violent-breakdown-w-partner :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to non-violent relationship breakdown with partner w/Statistical Neighbours")
                                           :y-field :non-violent-relationship-breakdown-with-partner
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to domestic abuse
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-domestic-abuse :data)
                                         :y-field :total-domestic-abuse
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-domestic-abuse :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to domestic abuse w/Statistical Neighbours")
                                           :y-field :total-domestic-abuse
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to violence or harassment
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-violence-harassment :data)
                                         :y-field :other-violence-or-harrassment
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-violence-harassment :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to violence or harassment w/Statistical Neighbours")
                                           :y-field :other-violence-or-harrassment
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to end of social rented tenancy
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-end-of-social-tenancy :data)
                                         :y-field :total-end-of-social-rented-tenancy
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-end-of-social-tenancy :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to end of social rented tenancy w/Statistical Neighbours")
                                           :y-field :total-end-of-social-rented-tenancy
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to eviction from supported housing
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-eviction-supported-housing :data)
                                         :y-field :total-evicted-from-supported-housing
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-eviction-supported-housing :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to eviction from supported housing w/Statistical Neighbours")
                                           :y-field :total-evicted-from-supported-housing
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to departure from custody
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-custody :data)
                                         :y-field :custody
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-custody :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to departure from custody w/Statistical Neighbours")
                                           :y-field :custody
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to departure from psychiatric hospital
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-psychiatric-hospital :data)
                                         :y-field :hospital-psychiatric
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-psychiatric-hospital :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to departure from psychiatric hospital w/Statistical Neighbours")
                                           :y-field :hospital-psychiatric
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to departure from general hospital
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-general-hospital :data)
                                         :y-field :hospital-general
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-general-hospital :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to departure from general hospital w/Statistical Neighbours")
                                           :y-field :hospital-general
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to departure from LAC placement
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-lac :data)
                                         :y-field :looked-after-child-placement
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-lac :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to departure from LAC placement w/Statistical Neighbours")
                                           :y-field :looked-after-child-placement
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to being required to leave accomodation provided by HO as asylum support
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-asylum :data)
                                         :y-field :required-to-leave-accommodation-provided-by-home-office-as-asylum-support
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-asylum :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to departure from HO asylum support w/Statistical Neighbours")
                                           :y-field :required-to-leave-accommodation-provided-by-home-office-as-asylum-support
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to disability/ill health
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-disability-ill :data)
                                         :y-field :home-no-longer-suitable-disability-ill-health
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-disability-ill :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to disability/ill health w/Statistical Neighbours")
                                           :y-field :home-no-longer-suitable-disability-ill-health
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to loss of placement or sponsorship through a resettlement scheme
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-resettlement :data)
                                         :y-field :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-resettlement :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to loss of placement in resettlement scheme w/Statistical Neighbours")
                                           :y-field :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total experiencing homelessness due to other/unknown reasons
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2R :total-homeless-other :data)
                                         :y-field :other-reasons--not-known
                                         :y-title "Count experiencing homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2R :total-homeless-other :data)
                                           :la-name la-name
                                           :title (str la-name " Total Experiencing Homelessness due to other/unknown reason(s) w/Statistical Neighbours")
                                           :y-field :other-reasons--not-known
                                           :y-title "Count experiencing homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened with homelessness due to end of AST
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-end-of-ast :data)
                                         :y-field :total-end--of-ast
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-end-of-ast :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened with Homelessness due to end of AST w/Statistical Neighbours")
                                           :y-field :total-end--of-ast
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to end of private non-AST
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-end-of-non-ast :data)
                                         :y-field :end-of-non-ast-private-rented-tenancy
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-end-of-non-ast :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to end of non-AST w/Statistical Neighbours")
                                           :y-field :end-of-non-ast-private-rented-tenancy
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to family or friends no longer willing or able to accomodate
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-family-friends :data)
                                         :y-field :family-or-friends-no-longer-willing-or-able-to-accommodate
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-family-friends :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to family & friends no longer able or willing w/Statistical Neighbours")
                                           :y-field :family-or-friends-no-longer-willing-or-able-to-accommodate
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to non-violent relationship breakdown with partner
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-non-violent-breakdown-w-partner :data)
                                         :y-field :non-violent-relationship-breakdown-with-partner
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-non-violent-breakdown-w-partner :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to non-violent relationship breakdown with partner w/Statistical Neighbours")
                                           :y-field :non-violent-relationship-breakdown-with-partner
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to domestic abuse
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-domestic-abuse :data)
                                         :y-field :total-domestic-abuse
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-domestic-abuse :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to domestic abuse w/Statistical Neighbours")
                                           :y-field :total-domestic-abuse
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to violence or harassment
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-violence-harassment :data)
                                         :y-field :other-violence-or-harrassment
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-violence-harassment :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to violence or harassment w/Statistical Neighbours")
                                           :y-field :other-violence-or-harrassment
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to end of social rented tenancy
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-end-of-social-tenancy :data)
                                         :y-field :total-end-of-social-rented-tenancy
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-end-of-social-tenancy :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to end of social rented tenancy w/Statistical Neighbours")
                                           :y-field :total-end-of-social-rented-tenancy
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to eviction from supported housing
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-eviction-supported-housing :data)
                                         :y-field :total-evicted-from-supported-housing
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-eviction-supported-housing :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to eviction from supported housing w/Statistical Neighbours")
                                           :y-field :total-evicted-from-supported-housing
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to departure from custody
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-custody :data)
                                         :y-field :custody
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-custody :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to departure from custody w/Statistical Neighbours")
                                           :y-field :custody
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to departure from psychiatric hospital
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-psychiatric-hospital :data)
                                         :y-field :hospital-psychiatric
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-psychiatric-hospital :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to departure from psychiatric hospital w/Statistical Neighbours")
                                           :y-field :hospital-psychiatric
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to departure from general hospital
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-general-hospital :data)
                                         :y-field :hospital-general
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-general-hospital :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to departure from general hospital w/Statistical Neighbours")
                                           :y-field :hospital-general
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to departure from LAC placement
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-lac :data)
                                         :y-field :looked-after-child-placement
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-lac :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to departure from LAC placement w/Statistical Neighbours")
                                           :y-field :looked-after-child-placement
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to being required to leave accomodation provided by HO as asylum support
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-asylum :data)
                                         :y-field :required-to-leave-accommodation-provided-by-home-office-as-asylum-support
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-asylum :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to departure from HO asylum support w/Statistical Neighbours")
                                           :y-field :required-to-leave-accommodation-provided-by-home-office-as-asylum-support
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to disability/ill health
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-disability-ill :data)
                                         :y-field :home-no-longer-suitable-disability-ill-health
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-disability-ill :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to disability/ill health w/Statistical Neighbours")
                                           :y-field :home-no-longer-suitable-disability-ill-health
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to loss of placement or sponsorship through a resettlement scheme
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-resettlement :data)
                                         :y-field :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-resettlement :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to loss of placement in resettlement scheme w/Statistical Neighbours")
                                           :y-field :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)

;; ---
;; ## Total threatened w/homelessness due to other/unknown reasons
(clerk/row {::clerk/width :full}
           (clerk/vl (single-line-chart {:ds (-> A2P :total-homeless-other :data)
                                         :y-field :other-reasons--not-known
                                         :y-title "Count threatened w/homelessness"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data (-> A2P :total-homeless-other :data)
                                           :la-name la-name
                                           :title (str la-name " Total Threatened W/Homelessness due to other/unknown reason(s) w/Statistical Neighbours")
                                           :y-field :other-reasons--not-known
                                           :y-title "Count threatened w/homelessness"
                                           })))

(mc-logo)



;; ---
;; ## Comparison of all reasons for homelessness
(clerk/row
 {::clerk/width :full}
 (clerk/plotly
  (la-comparison-boxplot {:la-data la-reasons-for-homelessness
                          :title la-name
                          :x-field :reason
                          ;;:x-title "Reason for experiencing homelessness"
                          :y-field :count
                          :y-title "Number experiencing homelessness"})))

(mc-logo)

;; ---
;; ## Comparison of summarised reasons for homelessness
(clerk/row
 {::clerk/width :full}
 (clerk/plotly
  (la-comparison-boxplot {:la-data summarised-reason-for-homelessness
                          :title la-name
                          :x-field :reason
                          ;;:x-title "Reason for experiencing homelessness"
                          :y-field :count
                          :y-title "Number experiencing homelessness"})))

(mc-logo)

;; ---
;; ## Comparison of specific reasons for homelessness
(clerk/row
 {::clerk/width :full}
 (clerk/plotly
  (la-comparison-boxplot {:la-data specific-reasons-for-homelessness
                          :title la-name
                          :x-field :reason
                          ;;:x-title "Reason for experiencing homelessness"
                          :y-field :count
                          :y-title "Number experiencing homelessness"})))

(mc-logo)

{::clerk/visibility {:result :hide}}

(comment
  ;; # TODO
  ;; ## Analysis
  ;; Reasons for threatened homelessness between LAs
  ;; how do those threatened with homelessness numbers compare to those who are experienceing homelessness within reason facing homelessness
  ;; ## Processing
  ;; Read in files once, rather than reading each file every time a new ds is made (e.g. A1 ds)
  )
