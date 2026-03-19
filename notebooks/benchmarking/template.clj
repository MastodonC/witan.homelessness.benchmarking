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
            [witan.send.adroddiad.clerk.charting-v2 :as cv2]
            [witan.homelessness.benchmarking.regional-neighbours :as rn]
            [witan.homelessness.benchmarking.statistical-neighbours :as sn]
            [clojure.string :as s]))

(def la-name "Sheffield")

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

(def statistical-neighbours (let [lait-result (sn/neighbours la-name)]
                              (if (= 10 (tc/row-count lait-result))
                                lait-result
                                (sn/neighbours la-name sn/global-model))))
(def statistical-neighbours-pred (let [lait-result (sn/neighbours-name-pred la-name)]
                                   (if (= 10 (count lait-result))
                                     lait-result
                                     (sn/neighbours-name-pred la-name sn/global-model))))

(
 ;; Data
 )

(defn combine-columns [ds out-col col-1 col-2]
  (tc/map-columns ds out-col [col-1 col-2]
                  (fn [c-1 c-2]
                    (cond
                      c-2
                      c-2
                      c-1
                      c-1
                      :else
                      nil))))

(def number-homeless
  "relief duty owed"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A1
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :homeless-relief-duty-owed4
                            :threatened-with-homelessness-within-56-days-prevention-duty-owed])
        (tc/map-columns :total-homeless
                        [:homeless-relief-duty-owed4
                         :threatened-with-homelessness-within-56-days-prevention-duty-owed]
                        (fn [relief prevention] (if (some nil? [relief prevention])
                                                  nil
                                                  (+ relief prevention)))))))

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
                            :households-assessed-as-homeless-per-1000
                            :households-assessed-as-threatened-with-homelessness-per-1000])
        (tc/map-columns :total-homeless-per-000
                        [:households-assessed-as-homeless-per-1000
                         :households-assessed-as-threatened-with-homelessness-per-1000]
                        (fn [relief prevention] (if (some nil? [relief prevention])
                                                  nil
                                                  (+ relief prevention)))))))

(def number-threatened-w-homeless-per-000
  "prevention duty owed per 000"
  (let [neighbours statistical-neighbours-pred]
    (-> @bass/A1
        (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
        (tc/select-columns [:date :name :quarter :year
                            :households-assessed-as-threatened-with-homelessness-per-1000]))))

(def number-household-in-area-000
  "for per 1000 calulcations"
  (-> @bass/A1
      (tc/select-rows #((conj (set statistical-neighbours-pred) la-name) (:name %)))
      (combine-columns :number-of-households-in-area-1000
                       :number-of-households--in-area-1000
                       :number-of-households--in-area4-1000)
      (tc/select-columns [:date :name :quarter :year
                          :number-of-households-in-area-1000])))

(defn calculate-per-000 [ds key]
  (tc/map-columns ds
                  (-> key
                      name
                      (str "-per-000")
                      keyword) [key :number-of-households-in-area-1000]
                  (fn [x y] (cond
                              (every? number? [x y])
                              (/ x y)
                              :else
                              nil))))

(def reason-for-homelessness-keys
  [:total-end--of-ast
   :end-of-non-ast-private-rented-tenancy
   :family-or-friends-no-longer-willing-or-able-to-accommodate
   :non-violent-relationship-breakdown-with-partner
   :total-domestic-abuse
   :other-violence-or-harrassment
   :total-end-of-social-rented-tenancy
   :total-evicted-from-supported-housing
   :custody
   :hospital-psychiatric
   :hospital-general
   :looked-after-child-placement
   :required-to-leave-accommodation-provided-by-home-office-as-asylum-support
   :home-no-longer-suitable-disability-ill-health
   :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
   :other-reasons--not-known])

(def A2R
  (as-> @bass/A2R $
    (tc/select-rows $ #((conj (set statistical-neighbours-pred) la-name) (:name %)))
    (tc/left-join $ number-household-in-area-000 [:date :name])
    (tc/drop-columns $ [:A1.date :A1.name :A1.quarter :A1.year])
    (combine-columns $ :home-no-longer-suitable-disability-ill-health
                     :home-no-longer-suitable-disability--ill-health-5
                     :home-no-longer-suitable-disability--ill-health-6)
    (combine-columns $ :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                     :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                     :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme-6)
    (combine-columns $ :other-reasons--not-known
                     :other-reasons--not-known
                     :other-reasons--not-known5)
    (reduce calculate-per-000 $ reason-for-homelessness-keys)))

(def A2P
  (as-> @bass/A2P $
    (tc/select-rows $ #((conj (set statistical-neighbours-pred) la-name) (:name %)))
    (tc/left-join $ number-household-in-area-000 [:date :name])
    (tc/drop-columns $ [:A1.date :A1.name :A1.quarter :A1.year])
    (combine-columns $ :home-no-longer-suitable-disability-ill-health
                     :home-no-longer-suitable-disability--ill-health-5
                     :home-no-longer-suitable-disability--ill-health-6)
    (combine-columns $ :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                     :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                     :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme-6)
    (combine-columns $ :other-reasons--not-known
                     :other-reasons--not-known
                     :other-reasons--not-known5)
    (reduce calculate-per-000 $ reason-for-homelessness-keys)))



(def A2P+A2R
  (as-> A2R $
    (tc/rename-columns $ (map (comp keyword #(str % "-per-000") name) reason-for-homelessness-keys) (comp keyword #(str % "-exp") name))
    (tc/left-join $
                  (tc/rename-columns A2P (map (comp keyword #(str % "-per-000") name) reason-for-homelessness-keys) (comp keyword #(str % "-thr") name))
                  [:code :date :name :quarter :year])
    (reduce (fn [ds k] (tc/map-columns ds k
                                       [((comp keyword #(str % "-exp") name) k)
                                        ((comp keyword #(str % "-thr") name) k)]
                                       (fn [relief prevention] (if (some nil? [relief prevention])
                                                                 nil
                                                                 (+ relief prevention)))))
            $ (map (comp keyword #(str % "-per-000") name) reason-for-homelessness-keys))))

(def la-reasons-for-homelessness
  (-> @bass/A2R
      (tc/select-rows #(#{la-name} (:name %)))
      (tc/order-by :date)
      (combine-columns :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                       :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                       :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme-6)
      (combine-columns :home-no-longer-suitable-disability-ill-health
                       :home-no-longer-suitable-disability--ill-health-5
                       :home-no-longer-suitable-disability--ill-health-6)
      (combine-columns :other-reasons--not-known
                       :other-reasons--not-known
                       :other-reasons--not-known5)
      (tc/pivot->longer (complement #{:code :name :quarter :year :date}))
      (tc/rename-columns {:$column :reason :$value :count})
      (tc/drop-rows #(#{:date :total-owed-a-relief-duty1} (:reason %)))))

(def la-reasons-for-threatened-w-homelessness
  (-> @bass/A2P
      (tc/select-rows #(#{la-name} (:name %)))
      (tc/order-by :date)
      (combine-columns :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                       :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                       :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme-6)
      (combine-columns :home-no-longer-suitable-disability-ill-health
                       :home-no-longer-suitable-disability--ill-health-5
                       :home-no-longer-suitable-disability--ill-health-6)
      (combine-columns :other-reasons--not-known
                       :other-reasons--not-known
                       :other-reasons--not-known5)
      (tc/pivot->longer (complement #{:code :name :quarter :year :date}))
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

(def summarised-reason-for-threatened-w-homelessness
  (-> la-reasons-for-threatened-w-homelessness
      (tc/select-rows #(summarised-reason-for-homelessness-keys (:reason %)))))

(def specific-reasons-for-homelessness
  (-> la-reasons-for-homelessness
      (tc/select-rows #((complement
                         (apply disj summarised-reason-for-homelessness-keys
                                [:end-of-non-ast-private-rented-tenancy
                                 :family-or-friends-no-longer-willing-or-able-to-accommodate
                                 :non-violent-relationship-breakdown-with-partner
                                 :other-violence-or-harrassment
                                 :required-to-leave-accommodation-provided-by-home-office-as-asylum-support
                                 :home-no-longer-suitable-disability--ill-health
                                 :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                                 :other-reasons--not-known])) (:reason %)))
      (tc/drop-rows #(#{:total-owed-a-relief-duty1 :date} (:reason %)))))

(def la-reasons-&-threatened-with-homelessness
  (-> @bass/A2P
      (tc/select-rows #(#{la-name} (:name %)))
      (tc/order-by :date)
      (combine-columns :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                       :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme
                       :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme-6)
      (combine-columns :home-no-longer-suitable-disability-ill-health
                       :home-no-longer-suitable-disability--ill-health-5
                       :home-no-longer-suitable-disability--ill-health-6)
      (combine-columns :other-reasons--not-known
                       :other-reasons--not-known
                       :other-reasons--not-known5)
      (tc/pivot->longer (complement #{:code :name :quarter :year :date}))
      (tc/rename-columns {:$column :reason :$value :count})
      (tc/drop-rows #(#{:date :total-owed-a-relief-duty1} (:reason %)))
      (tc/add-column :duty "prevention")
      (tc/concat (tc/add-column la-reasons-for-homelessness :duty "relief"))))

(
 ;; Charts
 )

(defn single-line-chart [{:keys [ds y-field y-title]}]
  {:data {:values (-> ds
                      (tc/select-rows #(#{la-name} (:name %)))
                      (tc/order-by :date)
                      (tc/rows :as-maps))}
   :mark {:type "line"}
   :encoding {:x {:field :date :type "temporal"
                  :axis {:title "Quarter" :titleFontSize 18.0
                         :labelFontSize 15.0}}
              :y {:field y-field
                  :type "quantitative"
                  :axis {:title y-title :titleFontSize 18.0
                         :labelFontSize 15.0}}
              :color {:field :name :type "nominal"
                      :legend nil}}})

(defn double-line-chart [{:keys [ds y-field-1 y-field-2 y-title-1 y-title-2]}]
  {:data {:values (-> ds
                      (tc/select-rows #(#{la-name} (:name %)))
                      (tc/order-by :date)
                      (tc/rows :as-maps))}
   :encoding {:x {:field :date :type "temporal"
                  :axis {:title "Quarter" :titleFontSize 18.0
                         :labelFontSize 15.0}}}
   :layer [{:mark {:type "line"}
            :encoding {:y {:field y-field-1
                           :type "quantitative"
                           :axis {:title y-title-1 :titleFontSize 18.0
                                  :labelFontSize 15.0}}}}
           {:mark {:type "line"}
            :encoding {:y {:field y-field-2
                           :type "quantitative"
                           :axis {:title y-title-2 :titleFontSize 18.0
                                  :labelFontSize 15.0 :titleAngle 270
                                  :titleX 60}}}}]
   :resolve {:scale {:y "independent"}}})

(defn stacked-area-chart [{:keys [ds y-field-1 y-field-2 y-title]}]
  ;; to work as expected :y-field-1 should always be those experiencing homelessness
  ;; and :y-field-2 should be those threatened with homelessness
  {:data {:values (-> ds
                      (tc/select-rows #(#{la-name} (:name %)))
                      (tc/pivot->longer [y-field-1 y-field-2]
                                        {:value-column-name :count
                                         :target-columns :homelessness-type})
                      (tc/map-columns :homelessness-type #(cond
                                                            (= y-field-1 %)
                                                            "Experiencing"
                                                            (= y-field-2 %)
                                                            "Threatened"))
                      (tc/order-by :date)
                      (tc/rows :as-maps))}
   :mark {:type "area"}
   :encoding {:x {:field :date :type "temporal"
                  :axis {:title "Quarter" :titleFontSize 18.0
                         :labelFontSize 15.0}}
              :y {:aggregate "sum"
                  :field :count
                  :type "quantitative"
                  :axis {:title y-title :titleFontSize 18.0
                         :labelFontSize 15.0}}
              :color {:field :homelessness-type :type "nominal"
                      :title "Homelessness Type"}}})

(defn normalised-stacked-area-chart [{:keys [ds y-field-1 y-field-2 y-title]}]
  (-> {:ds ds
       :y-field-1 y-field-1
       :y-field-2 y-field-2
       :y-title y-title}
      stacked-area-chart
      (assoc-in [:encoding :y :stack] "normalize")))

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
     :layout {:title title
              :font {:size 18}
              :scattermode "group"
              :scattergap 0.7
              :xaxis {:title x-title}
              :yaxis {:rangemode "tozero" :range (when max-y [0 max-y])
                      :title y-title}
              :yaxis2 {:rangemode "tozero" :range (when max-y [0 max-y])
                       :title y-title :overlaying "y" :side "right"}
              :height 400
              :width 1150
              :showlegend false}
     :config {:displayModeBar false
              :displayLogo false}}))

(defn neighbour-comparison-boxplot-2 ;; TODO would be nice to add oppisite values to tooltips
  [{:keys [neighbour-data la-name title y-field-1 y-field-2 y-title x-field x-title max-y]
    :or {x-field :date
         x-title "Quarter"}}]
  (let [neighbours statistical-neighbours-pred
        la-data (-> neighbour-data
                    (tc/select-rows #(#{la-name} (:name %)))
                    (tc/order-by :date))
        box-data (into (transduce
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
                               (update-in [(x-field x) :y] conj (y-field-1 x))
                               (update-in [(x-field x) :text] conj (:name x)))))
                        (-> neighbour-data
                            (tc/drop-rows #(#{la-name} (:name %)))
                            (tc/rows :as-maps)))
                       (transduce
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
                                         :opacity 0
                                         :boxpoints "all"
                                         :pointpos -1.8
                                         :jitter 0.3
                                         :hoverinfo "none"
                                         :type "box"
                                         :yaxis "y2"}))
                                 acc))
                          ([acc x]
                           (-> acc
                               (update-in [(x-field x) :y] conj (y-field-1 x))
                               (update-in [(x-field x) :text] conj (:name x)))))
                        (-> neighbour-data
                            (tc/drop-rows #(#{la-name} (:name %)))
                            (tc/rows :as-maps))))]
    {:data (conj
            box-data
            {:x (into [] (la-data x-field))
             :y (into [] (la-data y-field-1))
             :text (into [] (la-data :name))
             :name la-name
             :marker {:color "blue" :size 14 :symbol "star-diamond"}
             :mode "markers"
             :type "scatter"
             :zorder 1 ;; FIXME currently does nothing
             })
     :layout {:title title
              :font {:size 18}
              :scattermode "group"
              :scattergap 0.7
              :xaxis {:title x-title}
              :yaxis {:rangemode "tozero"
                      :range [0 100]
                      :title y-title}
              :yaxis2 {:showgrid false
                       :rangemode "tozero"
                       :tickmode "sync"
                       :tickvals [0 20 40 60 80 100]
                       :ticktext ["100" "80" "60" "40" "20" "0"]
                       :title "% Experiencing" :overlaying "y"
                       :side "right"}
              :height 400
              :width 1150
              :showlegend false}
     :config {:displayModeBar false
              :displayLogo false}}))

(defn duty-comparison-line-chart [reason-key]
  {:data {:values (-> la-reasons-&-threatened-with-homelessness
                      (tc/select-rows #(#{reason-key} (:reason %)))
                      (tc/rows :as-maps))}
   :mark {:type "line"}
   :encoding {:x {:field :date :type "temporal"
                  :axis {:title "Quarter" :titleFontSize 18.0
                         :labelFontSize 15.0}}
              :y {:field :count
                  :type "quantitative"
                  :axis {:title "Count" :titleFontSize 18.0
                         :labelFontSize 15.0}}
              :color {:field :duty :type "nominal"
                      :title "Duty"
                      :legend {:labelFontSize 18
                               :titleFontSize 18}}}
   :height 500
   :width 1000})

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
  (let [stat-neighbours (-> statistical-neighbours
                            (tc/select-columns [:sn :sn_name :sn_prox])
                            (tc/rename-columns {:sn_name "Neighbour Name"
                                                :sn "Neighbour Rank"
                                                :sn_prox "Statistical Proximity"}))]
    (if (every? nil? (-> stat-neighbours
                         (get "Statistical Proximity")
                         distinct))
      (tc/drop-columns stat-neighbours "Statistical Proximity")
      stat-neighbours))))

(mc-logo)

;; ---
;; ## We have taken
;; ## "relief duty owed" to mean a household experiencing homelessness
;; ## and
;; ## "prevention duty owed" to mean a household threatened with homelessness

(mc-logo)

;; ---
;; # Total counts of households experiencing or
;; # threatened with homelessness

(mc-logo)

;; ---
;; ## Total households homeless
(clerk/row {::clerk/width :full}
           (clerk/vl (normalised-stacked-area-chart {:ds number-homeless-per-000
                                                     :y-field-1 :households-assessed-as-homeless-per-1000
                                                     :y-field-2 :households-assessed-as-threatened-with-homelessness-per-1000
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (neighbour-comparison-boxplot {:neighbour-data number-homeless-per-000
                                           :la-name la-name
                                           :title (str la-name " Total Homeless per 1000 w/Statistical Neighbours")
                                           :y-field :total-homeless-per-000
                                           :y-title "Count per 1000"})))

(mc-logo)

;; ---
;; ## Benchmark proportions threatened vs experiencing
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> number-homeless-per-000
                                                                 (tc/map-columns :proportion-thr [:households-assessed-as-threatened-with-homelessness-per-1000
                                                                                                  :total-homeless-per-000]
                                                                                 (fn [thr total] (when (number? total) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:households-assessed-as-homeless-per-1000
                                                                                                  :total-homeless-per-000]
                                                                                 (fn [exp total] (when (number? total) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))

(mc-logo)

;; ---
;; ## Comparison of summarised reasons for homelessness
(let [raw-threatened-data (-> summarised-reason-for-threatened-w-homelessness
                              (tc/map-columns :reason #(-> %
                                                           name
                                                           (str/replace "-" " ")
                                                           str/capitalize)))
      color-map (cv2/color-map raw-threatened-data
                               :reason
                               (cv2/color-and-shape-lookup
                                (map #(-> %
                                          name
                                          (str/replace "-" " ")
                                          str/capitalize) summarised-reason-for-homelessness-keys)))]
  (clerk/row {::clerk/width :full}
             (clerk/vl {:hconcat [{:data {:values (tc/rows raw-threatened-data :as-maps)}
                                   :mark "bar"
                                   :title {:text "% Threatened w/Homelessness"
                                           :fontSize 18.0}
                                   :encoding {:y {:aggregate "sum" :field :count
                                                  :stack "normalize"
                                                  :axis {:title "% of households" :titleFontSize 18.0
                                                         :labelFontSize 15.0}}
                                              :x {:field :date
                                                  :axis {:title "Quarter" :titleFontSize 18.0
                                                         :labelFontSize 15.0}}
                                              :color (into {:title "Reason"
                                                            :legend {:labelFontSize 15
                                                                     :titleFontSize 15
                                                                     :labelLimit 0}}
                                                           color-map)}}
                                  {:data {:values (-> summarised-reason-for-homelessness
                                                      (tc/map-columns :reason #(-> %
                                                                                   name
                                                                                   (str/replace "-" " ")
                                                                                   str/capitalize))
                                                      (tc/rows :as-maps))}
                                   :mark "bar"
                                   :title {:text "% Experiencing Homelessness"
                                           :fontSize 18.0}
                                   :encoding {:y {:aggregate "sum" :field :count
                                                  :stack "normalize"
                                                  :axis {:title "" :labelFontSize 15.0}}
                                              :x {:field :date
                                                  :axis {:title "Quarter" :titleFontSize 18.0
                                                         :labelFontSize 15.0}}
                                              :color (into {:title "Reason"
                                                            :legend {:labelFontSize 15
                                                                     :titleFontSize 15
                                                                     :labelLimit 0}}
                                                           color-map)}}]})))

(mc-logo)

;; ---
;; # Households experiencing or threatened
;; # with homelessness by reason

(mc-logo)

;; ---
;; ## Total homeless due to end of AST
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :total-end--of-ast-per-000-exp
                                                     :y-field-2 :total-end--of-ast-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to end of AST w/Statistical Neighbours")
                                                     :y-field :total-end--of-ast-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))

(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to end of AST
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:total-end--of-ast-per-000-thr
                                                                                                  :total-end--of-ast-per-000]
                                                                                 (fn [thr total] (when (and (every? #(not= 0 %) [thr total])
                                                                                                            (number? total)) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:total-end--of-ast-per-000-exp
                                                                                                  :total-end--of-ast-per-000]
                                                                                 (fn [exp total] (when (and (every? #(not= 0 %) [exp total])
                                                                                                            (number? total)) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))

(mc-logo)

;; ---
;; ## Total homeless due to end of private non-AST
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :end-of-non-ast-private-rented-tenancy-per-000-exp
                                                     :y-field-2 :end-of-non-ast-private-rented-tenancy-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to end of non-AST w/Statistical Neighbours")
                                                     :y-field :end-of-non-ast-private-rented-tenancy-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))

(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to end of non-AST
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:end-of-non-ast-private-rented-tenancy-per-000-thr
                                                                                                  :end-of-non-ast-private-rented-tenancy-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:end-of-non-ast-private-rented-tenancy-per-000-exp
                                                                                                  :end-of-non-ast-private-rented-tenancy-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))

(mc-logo)

;; ---
;; ## Total homeless due to family or friends no longer willing or able to accomodate
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :family-or-friends-no-longer-willing-or-able-to-accommodate-per-000-exp
                                                     :y-field-2 :family-or-friends-no-longer-willing-or-able-to-accommodate-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to end of family or friends no longer willing or able to accomodate w/Statistical Neighbours")
                                                     :y-field :family-or-friends-no-longer-willing-or-able-to-accommodate-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))

(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to family or friends no longer willing or able to accomodate
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:family-or-friends-no-longer-willing-or-able-to-accommodate-per-000-thr
                                                                                                  :family-or-friends-no-longer-willing-or-able-to-accommodate-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:family-or-friends-no-longer-willing-or-able-to-accommodate-per-000-exp
                                                                                                  :family-or-friends-no-longer-willing-or-able-to-accommodate-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))

(mc-logo)

;; ---
;; ## Total homeless due to non-violent relationship breakdown with partner
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :non-violent-relationship-breakdown-with-partner-per-000-exp
                                                     :y-field-2 :non-violent-relationship-breakdown-with-partner-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to non-violent relationship breakdown with partner w/Statistical Neighbours")
                                                     :y-field :non-violent-relationship-breakdown-with-partner-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))
(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to family or friends no longer willing or able to accomodate
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:non-violent-relationship-breakdown-with-partner-per-000-thr
                                                                                                  :non-violent-relationship-breakdown-with-partner-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:non-violent-relationship-breakdown-with-partner-per-000-exp
                                                                                                  :non-violent-relationship-breakdown-with-partner-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))

(mc-logo)

;; ---
;; ## Total homeless due to domestic abuse
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :total-domestic-abuse-per-000-exp
                                                     :y-field-2 :total-domestic-abuse-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to domestic abuse w/Statistical Neighbours")
                                                     :y-field :total-domestic-abuse-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))

(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to domestic abuse
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:total-domestic-abuse-per-000-thr
                                                                                                  :total-domestic-abuse-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:total-domestic-abuse-per-000-exp
                                                                                                  :total-domestic-abuse-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))
(mc-logo)

;; ---
;; ## Total homeless due to violence or harassment
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :other-violence-or-harrassment-per-000-exp
                                                     :y-field-2 :other-violence-or-harrassment-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to violence or harrassment w/Statistical Neighbours")
                                                     :y-field :other-violence-or-harrassment-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))

(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to other violence or harrassment
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:other-violence-or-harrassment-per-000-thr
                                                                                                  :other-violence-or-harrassment-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:other-violence-or-harrassment-per-000-exp
                                                                                                  :other-violence-or-harrassment-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))
(mc-logo)

;; ---
;; ## Total homeless due to end of social rented tenancy
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :total-end-of-social-rented-tenancy-per-000-exp
                                                     :y-field-2 :total-end-of-social-rented-tenancy-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to violence or harrassment w/Statistical Neighbours")
                                                     :y-field :total-end-of-social-rented-tenancy-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))
(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to social rented tenancy
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:total-end-of-social-rented-tenancy-per-000-thr
                                                                                                  :total-end-of-social-rented-tenancy-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:total-end-of-social-rented-tenancy-per-000-exp
                                                                                                  :total-end-of-social-rented-tenancy-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))

(mc-logo)

;; ---
;; ## Total homeless due to eviction from supported housing
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :total-evicted-from-supported-housing-per-000-exp
                                                     :y-field-2 :total-evicted-from-supported-housing-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to eviction from supported housing w/Statistical Neighbours")
                                                     :y-field :total-evicted-from-supported-housing-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))
(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to eviction from supported housing
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:total-evicted-from-supported-housing-per-000-thr
                                                                                                  :total-evicted-from-supported-housing-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:total-evicted-from-supported-housing-per-000-exp
                                                                                                  :total-evicted-from-supported-housing-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))
(mc-logo)

;; ---
;; ## Total homeless due to departure from custody
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :custody-per-000-exp
                                                     :y-field-2 :custody-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to departure from custody w/Statistical Neighbours")
                                                     :y-field :custody-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))

(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to departure from custody
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:custody-per-000-thr
                                                                                                  :custody-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:custody-per-000-exp
                                                                                                  :custody-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))

(mc-logo)

;; ---
;; ## Total homeless due to departure from psychiatric hospital
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :hospital-psychiatric-per-000-exp
                                                     :y-field-2 :hospital-psychiatric-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to departure from psychiatric hospital w/Statistical Neighbours")
                                                     :y-field :custody-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))

(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to departure from psychiatric hospital
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:hospital-psychiatric-per-000-thr
                                                                                                  :hospital-psychiatric-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:hospital-psychiatric-per-000-exp
                                                                                                  :hospital-psychiatric-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))
(mc-logo)

;; ---
;; ## Total homeless due to departure from general hospital
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :hospital-general-per-000-exp
                                                     :y-field-2 :hospital-general-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to departure from general hospital w/Statistical Neighbours")
                                                     :y-field :hospital-general-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))

(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to departure from general hospital
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:hospital-general-per-000-thr
                                                                                                  :hospital-general-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:hospital-general-per-000-exp
                                                                                                  :hospital-general-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))

(mc-logo)

;; ---
;; ## Total homeless due to departure from LAC placement
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :looked-after-child-placement-per-000-exp
                                                     :y-field-2 :looked-after-child-placement-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to departure from LAC placement w/Statistical Neighbours")
                                                     :y-field :hospital-general-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))
(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to departure from LAC placement
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:looked-after-child-placement-per-000-thr
                                                                                                  :looked-after-child-placement-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:looked-after-child-placement-per-000-exp
                                                                                                  :looked-after-child-placement-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))

(mc-logo)

;; ---
;; ## Total homeless due to being required to leave accomodation provided by HO as asylum support
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :required-to-leave-accommodation-provided-by-home-office-as-asylum-support-per-000-exp
                                                     :y-field-2 :required-to-leave-accommodation-provided-by-home-office-as-asylum-support-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to departure from HO asylum support w/Statistical Neighbours")
                                                     :y-field :required-to-leave-accommodation-provided-by-home-office-as-asylum-support-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to being required to leave accomodation provided by HO as asylum support
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:required-to-leave-accommodation-provided-by-home-office-as-asylum-support-per-000-thr
                                                                                                  :required-to-leave-accommodation-provided-by-home-office-as-asylum-support-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:required-to-leave-accommodation-provided-by-home-office-as-asylum-support-per-000-exp
                                                                                                  :required-to-leave-accommodation-provided-by-home-office-as-asylum-support-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))

(mc-logo)

;; ---
;; ## Total homeless due to disability/ill health
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :home-no-longer-suitable-disability-ill-health-per-000-exp
                                                     :y-field-2 :home-no-longer-suitable-disability-ill-health-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to disability/ill health w/Statistical Neighbours")
                                                     :y-field :home-no-longer-suitable-disability-ill-health-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to disability/ill health
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:home-no-longer-suitable-disability-ill-health-per-000-thr
                                                                                                  :home-no-longer-suitable-disability-ill-health-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:home-no-longer-suitable-disability-ill-health-per-000-exp
                                                                                                  :home-no-longer-suitable-disability-ill-health-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))

(mc-logo)

;; ---
;; ## Total homeless due to loss of placement or sponsorship through a resettlement scheme
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme-per-000-exp
                                                     :y-field-2 :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to loss in resettlement scheme w/Statistical Neighbours")
                                                     :y-field :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))

(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to loss of placement or sponsorship through a resettlement scheme
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme-per-000-thr
                                                                                                  :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme-per-000-exp
                                                                                                  :loss-of-placement-or-sponsorship-provided-through-a-resettlement-scheme-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))

(mc-logo)

;; ---
;; ## Total homeless due to other/unknown reasons
(clerk/row {::clerk/width :full}

           (clerk/vl (normalised-stacked-area-chart {:ds A2P+A2R
                                                     :y-field-1 :other-reasons--not-known-per-000-exp
                                                     :y-field-2 :other-reasons--not-known-per-000-thr
                                                     :y-title "% homeless - threatened vs experiencing"}))
           (clerk/plotly
            (assoc-in (neighbour-comparison-boxplot {:neighbour-data A2P+A2R
                                                     :la-name la-name
                                                     :title (str la-name " Total Homeless per 1000 due to loss in resettlement scheme w/Statistical Neighbours")
                                                     :y-field :other-reasons--not-known-per-000
                                                     :y-title "Count per 1000"}) [:layout :height] 400)))

(mc-logo)

;; ---
;; ## Benchmark proportions threatened with vs experiencing homelessness due to other/unknown reasons
(clerk/row {::clerk/width :full}
           (clerk/plotly
            (neighbour-comparison-boxplot-2 {:neighbour-data (-> A2P+A2R
                                                                 (tc/map-columns :proportion-thr [:other-reasons--not-known-per-000-thr
                                                                                                  :other-reasons--not-known-per-000]
                                                                                 (fn [thr total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [thr total])) (int (* 100 (/ thr total))))))
                                                                 (tc/map-columns :proportion-exp [:other-reasons--not-known-per-000-exp
                                                                                                  :other-reasons--not-known-per-000]
                                                                                 (fn [exp total] (when (and (number? total)
                                                                                                            (every? (complement zero?) [exp total])) (int (* 100 (/ exp total)))))))
                                             :la-name la-name
                                             :title (str la-name " % Threatened with or Experiencing Homelessness w/Statistical Neighbours")
                                             :y-field-1 :proportion-thr
                                             :y-field-2 :proportion-exp
                                             :y-title "% Threatened"})))

(mc-logo)

{::clerk/visibility {:result :hide}}
