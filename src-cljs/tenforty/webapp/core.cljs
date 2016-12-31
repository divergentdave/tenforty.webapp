(ns tenforty.webapp.core
  (:require [tenforty.core :refer [calculate
                                   get-deps
                                   get-name]]
            [tenforty.forms.ty2015 :refer [forms]]))

(defn init
  []
  (let [g (js/dagreD3.graphlib.Graph.)
        svg (.select js/d3 "svg")
        svg-group (.append svg "g")
        render (js/dagreD3.render.)]
    (.setGraph g {})
    (.setDefaultEdgeLabel g #(js-obj))
    (dorun (map #(.setNode g (get-name %) (js-obj "label" (get-name %))) (vals (:lines forms))))
    (dorun (map (fn [dest] (dorun (map (fn [src] (.setEdge g (name src) (get-name dest)))
                                       (get-deps dest))))
                (vals (:lines forms))))
    (render (.select js/d3 "svg g") g)
    (let [graph (.graph g)
          width (aget graph "width")
          height (aget graph "height")]
      (.attr svg "width" (+ width 40))
      (.attr svg "height" (+ height 40)))
    (.attr svg-group "transform" "translate(20, 20)")))

(.addEventListener js/document "DOMContentLoaded" init)
