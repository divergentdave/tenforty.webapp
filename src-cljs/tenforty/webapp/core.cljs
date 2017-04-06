(ns tenforty.webapp.core
  (:require [tenforty.core :refer [calculate
                                   get-deps
                                   get-keyword
                                   get-group
                                   get-name
                                   ->MapTaxSituation
                                   NumberInputLine
                                   BooleanInputLine
                                   CodeInputLine
                                   FormulaLine
                                   reverse-deps
                                   make-context]]
            [tenforty.forms.ty2016 :refer [forms]]))

(def g (js/dagreD3.graphlib.Graph. (js-obj "compound" true)))

(defn nodelist-to-seq
  [nodelist]
  (doall (map
          #(.item nodelist %)
          (range (aget nodelist "length")))))

(defn exclusive-or [a b]
  (or
   (and a (not b))
   (and (not a) b)))

(defn parse-keyword [string]
  (keyword (subs string 1)))

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

(declare update-input)

(defn- boolean-control-handler [event]
  (let [target (aget event "target")
        kw-name (aget target "name")
        kw (parse-keyword kw-name)
        node (aget (aget g "_nodes") kw-name)
        value (exclusive-or (aget target "checked")
                            (= "false" (aget target "value")))]
    (when (aget target "checked")
      (.remove (aget (aget node "elem") "classList") "empty-input"))
    (update-input kw value)))

(defn- number-control-handler [event]
  (let [target (aget event "target")
        kw-name (aget target "name")
        kw (parse-keyword kw-name)
        node (aget (aget g "_nodes") kw-name)
        value (aget target "value")
        valid (and (not= "" value)
                   (aget (aget target "validity") "valid"))]
    (.toggle (aget (aget node "elem") "classList") "empty-input" (not valid))
    (if valid
      (update-input kw (js/parseFloat value))
      (update-input kw nil))))

(defn- enum-control-handler [event]
  (let [target (aget event "target")
        kw-name (aget target "name")
        kw (parse-keyword kw-name)
        node (aget (aget g "_nodes") kw-name)
        option (.item (aget target "selectedOptions") 0)
        value (aget option "value")
        valid (not= value "")]
    (.toggle (aget (aget node "elem") "classList") "empty-input" (not valid))
    (if valid
      (update-input kw (js/parseInt value))
      (update-input kw nil))))

(defn- boolean-controls [div]
  (let [label-true (.append div "label")
        radio-true (.append label-true "input")
        label-false (.append div "label")
        radio-false (.append label-false "input")]
    (.attr radio-true "type" "radio")
    (.attr radio-true "value" "true")
    (.addEventListener (.node radio-true) "change" boolean-control-handler)
    (.appendChild (.node label-true) (.createTextNode js/document "true"))
    (.attr radio-false "type" "radio")
    (.attr radio-false "value" "false")
    (.addEventListener (.node radio-false) "change" boolean-control-handler)
    (.appendChild (.node label-false) (.createTextNode js/document "false"))))

(defn- number-controls [div]
  (let [input (.append div "input")]
    (.attr input "type" "number")
    (.addEventListener (.node input) "change" number-control-handler)))

(defn- enum-controls [div]
  (let [input (.append div "select")
        option (.append input "option")]
    (.attr option "value" "")
    (.text option "")
    (.addEventListener (.node input) "change" enum-control-handler)))

(defn register-shapes
  [render]
  (let [shapes (.shapes render)]
    (aset shapes "rectRadioButtons"
          (shape-helper boolean-controls))
    (aset shapes "rectInput"
          (shape-helper number-controls))
    (aset shapes "rectSelect"
          (shape-helper enum-controls))
    (aset shapes "rectText"
          (shape-helper (fn [div])))))

(defn- empty-situation-factory
  ([forms]
   (empty-situation-factory forms nil))
  ([forms group-kw]
   (let [groups (:groups forms)
         child-groups (seq (get groups group-kw))]
     (->MapTaxSituation {} (zipmap
                            child-groups
                            (map #(vector (empty-situation-factory forms %))
                                 child-groups))))))

(defn- group-to-group-list-map
  ([forms] (group-to-group-list-map forms nil (list)))
  ([forms group-kw prefix]
   (let [groups (:groups forms)
         child-groups (seq (get groups group-kw))]
     (reduce merge (concat (map
                            #(sorted-map % (concat prefix [%]))
                            child-groups)
                           (map
                            #(group-to-group-list-map forms % (concat prefix [%]))
                            child-groups))))))

(let [situation (atom (empty-situation-factory forms))
      group-path-lookup (group-to-group-list-map forms)]
  (defn update-calculations [kws]
    (let [context (make-context forms @situation)]
      (dorun (map (fn [kw]
                    (let [node (aget (aget g "_nodes") (str kw))
                          div (.querySelector (aget node "elem") "div")]
                      (try
                        (aset div "textContent" (calculate context kw))
                        (catch js/Error e
                          (aset div "textContent" "")))))
                  kws))))

  (let [rdeps (reverse-deps forms)]
    (defn update-input [kw value]
      (let [line (kw (:lines forms))
            group-kw (get-group line)
            group-path (get group-path-lookup group-kw)
            assoc-path (concat (interleave (repeat (count group-path) :groups)
                                           group-path
                                           (repeat (count group-path) 0))
                               [:values kw])]
        (swap! situation assoc-in assoc-path value)
        (update-calculations (kw rdeps))))))

(defn init
  []
  (let [svg (.select js/d3 "svg")
        svg-group (.append svg "g")
        render (js/dagreD3.render.)]
    (register-shapes render)
    (.setGraph g {})
    (.setDefaultEdgeLabel g (fn [v w name] (js-obj)))
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
                    FormulaLine
                    (js-obj "label" (get-name %)
                            "shape" "rectText")))
                (vals (:lines forms))))
    (dorun (map #(when %
                   (.setNode
                    g
                    (str "group_" %)
                    (js-obj "label" (name %))))
                (keys (:groups forms))))
    (dorun (map #(when (get-group %)
                   (.setParent
                    g
                    (str (get-keyword %))
                    (str "group_" (get-group %))))
                (vals (:lines forms))))
    (dorun (map
            (fn [dest] (dorun (map
                               (fn [src] (.setEdge
                                          g
                                          (str src)
                                          (str (get-keyword dest))))
                               (get-deps dest))))
            (vals (:lines forms))))
    (render svg-group g)
    (let [_nodes (aget g "_nodes")]
      (dorun (map
              (fn [kw-string]
                (let [kw (parse-keyword kw-string)
                      line (kw (:lines forms))
                      node (aget _nodes kw-string)
                      elem (aget node "elem")]
                  (when elem
                    (let [inputs (.querySelectorAll elem "input, select")
                          inputs-seq (nodelist-to-seq inputs)]
                      (when (instance? CodeInputLine line)
                        (let [select (first inputs-seq)]
                          (dorun (map (fn [entry]
                                        (let [option (.createElement js/document "option")]
                                          (.setAttribute option "value" (val entry))
                                          (aset option "textContent" (key entry))
                                          (.appendChild select option)))
                                      (:options line)))))
                      (dorun (map
                              (fn [input] (.setAttribute input "name" kw-string))
                              inputs-seq))))))
              (.keys js/Object _nodes))))
    (let [graph (.graph g)
          width (aget graph "width")
          height (aget graph "height")]
      (.attr svg "width" (+ width 40))
      (.attr svg "height" (+ height 40)))
    (.attr svg-group "transform" "translate(20, 20)"))
  (update-calculations (keys (filter #(instance? FormulaLine (val %)) (:lines forms)))))

(.addEventListener js/document "DOMContentLoaded" init)
