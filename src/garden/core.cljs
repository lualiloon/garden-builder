(ns garden.core
  (:require [cljsjs.pixi]
            [cljsjs.howler]))

;; --------------- PIXI ------------------

(defonce initial-state {:pixi
                        {:app
                         (js/PIXI.Application. #js{:width 640 :height 360
                                                   :backgroundColor "0xA08D89"})
                         :sprite (js/PIXI.Sprite.from "images/beet.png")}})


(defonce state-atom
  (atom initial-state))


(defn reset-state-atom!
  []
  (reset! state-atom initial-state))



(defn add-pixi
  [{:keys [app] :as pixi-state}]
  (let [app-holder (js/document.getElementById "app-holder")]
    (.appendChild app-holder (.-view app))))


(defn add-sprite
  [{:keys [app sprite] :as pixi-state}]
  (.addChild (.-stage app) sprite))


(defn setup-pixi
  [pixi-state]
  (do (add-pixi pixi-state)
      (add-sprite pixi-state)))


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
