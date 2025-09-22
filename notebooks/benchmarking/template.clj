^{:nextjournal.clerk/toc true}
(ns benchmarking.template
  {:nextjournal.clerk/visibility {:code   :hide
                                  :result :hide}
   :nextjournal.clerk/page-size            nil
   :nextjournal.clerk/auto-expand-results? true
   :nextjournal.clerk/budget               nil}
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk-slideshow :as slideshow]
            [tablecloth.api :as tc]
            [witan.send.adroddiad.clerk.html :as chtml]
            [witan.homelessness.benchmarking.assessments :as bass]
            ))
