(ns tenforty.webapp.core
  (:require [tenforty.core :refer [calculate
                                   get-deps
                                   get-keyword
                                   get-name
                                   ->MapTaxSituation
                                   NumberInputLine
                                   BooleanInputLine
                                   CodeInputLine]]
            [tenforty.forms.ty2015 :refer [forms]]))

(defn- make-rect-intersect-callback [node]
  (fn [point] (.rect js/dagreD3.intersect node point)))

(defn- shape-helper [controls-fn]
  (fn [parent bbox node]
    (let [shape-svg (.insert parent "g" ":first-child")
          rect (.append shape-svg "rect")
          foreign-object (.append shape-svg "foreignObject")
          body (.append foreign-object "xhtml:body")
          div (.append body "div")]
      (.attr div "class" "controls")
      (let [bbox-width (aget bbox "width")
            bbox-height (aget bbox "height")
            width (max bbox-width 150)
            height (* 2 bbox-height)]
        (controls-fn div)
        (.attr rect "x" (- (/ width 2)))
        (.attr rect "y" (- (/ height 2)))
        (.attr rect "width" width)
        (.attr rect "height" height)
        (.attr foreign-object "x" (- (/ width 2)))
        (.attr foreign-object "y" 0)
        (.attr foreign-object "width" width)
        (.attr foreign-object "height" bbox-height)
        (.attr (.select parent ".label") "transform" (str "translate(0," (- (/ bbox-height 2)) ")")))
      (aset node "intersect" (make-rect-intersect-callback node))
      shape-svg)))

(defn- boolean-controls [div]
  (let [label-true (.append div "label")
        radio-true (.append label-true "input")
        label-false (.append div "label")
        radio-false (.append label-false "input")]
    (.attr radio-true "type" "radio")
    (.appendChild (.node label-true) (.createTextNode js/document "true"))
    (.attr radio-false "type" "radio")
    (.appendChild (.node label-false) (.createTextNode js/document "false"))))

(defn- number-controls [div]
  (let [input (.append div "input")]
    (.attr input "type" "number")))

(defn- enum-controls [div]
  (let [input (.append div "select")]
    (dorun (map
            #(let [option (.append input "option")]
               (.attr option "value" (val %))
               (.text option (key %)))
            (seq {"foo" 1 "bar" 2})))))

(defn register-shapes
  [render]
  (let [shapes (.shapes render)]
    (aset shapes "rectRadioButtons"
          (shape-helper boolean-controls))
    (aset shapes "rectInput"
          (shape-helper number-controls))
    (aset shapes "rectSelect"
          (shape-helper enum-controls))))

(defn init
  []
  (let [g (js/dagreD3.graphlib.Graph.)
        svg (.select js/d3 "svg")
        svg-group (.append svg "g")
        render (js/dagreD3.render.)]
    (register-shapes render)
    (.setGraph g {})
    (.setDefaultEdgeLabel g #(js-obj))
    (dorun (map #(.setNode
                  g
                  (str (get-keyword %))
                  (condp instance? %
                    NumberInputLine
                    (js-obj "label" (get-name %)
                            "shape" "rectInput"
                            "class" "input empty-input")
                    BooleanInputLine
                    (js-obj "label" (get-name %)
                            "shape" "rectRadioButtons"
                            "class" "input empty-input")
                    CodeInputLine
                    (js-obj "label" (get-name %)
                            "shape" "rectSelect"
                            "class" "input empty-input")
                    (js-obj "label" (get-name %))))
                (vals (:lines forms))))
    (dorun (map
            (fn [dest] (dorun (map
                               (fn [src] (.setEdge
                                          g
                                          (str src)
                                          (str (get-keyword dest))))
                               (get-deps dest))))
            (vals (:lines forms))))
    (render (.select js/d3 "svg g") g)
    (let [graph (.graph g)
          width (aget graph "width")
          height (aget graph "height")]
      (.attr svg "width" (+ width 40))
      (.attr svg "height" (+ height 40)))
    (.attr svg-group "transform" "translate(20, 20)")))

(let [situation (atom (->MapTaxSituation {} {}))])

(.addEventListener js/document "DOMContentLoaded" init)
