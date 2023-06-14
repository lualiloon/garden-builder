(ns garden.core
  (:require [cljsjs.pixi]
            [cljsjs.howler]))

;; --------------- PIXI ------------------

(defonce initial-state
  {:pixi
   {:app
    (js/PIXI.Application. #js{:width 2048 :height 2048
                              :backgroundColor "0x4C7C3E"
                              #_"0x5A3A22"
                              #_"0xA08D89"})
    :sprites {:background {}
              :objects {}
              :overlays {:grid (js/PIXI.Sprite.from "images/Grid Style B - 2048x2048 - 128x64 - blue.png")}}}})


(defonce state-atom
  (atom initial-state))


(defn reset-state-atom!
  []
  (reset! state-atom initial-state))


(defn create-herb-sprite
  [herb-key]
  (let [file-name (case herb-key
                    :basil "images/basil.png"
                    :sage "images/sage.png")]
    (swap! state-atom
           assoc-in
           [:pixi :sprites-objects herb-key]
           (js/PIXI.Sprite.from file-name))))


(defn add-herb-sprite!
  [herb-key]
  :TODO)


(defn add-pixi
  [{:keys [app] :as pixi-state}]
  (let [app-holder (js/document.getElementById "app-holder")]
    (.appendChild app-holder (.-view app))))


(defn add-sprite
  [{:keys [app sprite] :as pixi-state}]
  (.addChild (.-stage app) sprite))


(defn add-all-sprites
  [app {:keys [background objects overlays] :as all-sprites}]
  (doseq [sprite (concat (vals background)
                         (vals objects)
                         (vals overlays))]
    (.addChild (.-stage app) sprite)))


(defn setup-pixi
  [{:keys [app sprites] :as pixi-state}]
  (do (add-pixi pixi-state)
      #_(add-sprite pixi-state)
      (add-all-sprites app sprites)))


(defonce _ (setup-pixi (:pixi @state-atom)))

(defonce elapsed (atom 0.0))

#_(.add (.-ticker pixi-app)
      (fn [delta]
        (swap! elapsed + delta)
        (set! (.-x pixi-sprite) @elapsed)))


;; --------------- Howler ------------------

(def sound (js/Howl. #js{:src "sounds/drop_001.ogg"}))

;; eval this to play the sound
#_(.play sound)
