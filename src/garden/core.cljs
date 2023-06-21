(ns garden.core
  (:require [cljsjs.pixi]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [cljsjs.howler]
            [clojure.string :as s]))


;; ------ Globals --------

(def tile-size {:w 128
                :h 64})

;; ----- mouse coordinates -------

(defn mouse->tile
  [mouse-x mouse-y]
  ;; square grid conversion
  #_(let [tile-x (/ mouse-x (:w tile-size))
        tile-y (/ mouse-y (:h tile-size))

        ;; for where the mouse is within the tile
        offset-x (mod mouse-x (:w tile-size))
        offset-y (mod mouse-y (:h tile-size))]
    [tile-x tile-y])

  ;; isometric grid conversion
  (let [half-tile-width (/ (:w tile-size) 2)
        half-tile-height (/ (:h tile-size) 2)
        
        tile-x (int (- (/ (+ (/ mouse-x half-tile-width)
                        (/ mouse-y half-tile-height))
                     2)
                  0.7))
        #_(- (* mouse-x half-tile-width)
             (* mouse-y half-tile-width))
        tile-y (+ (/ (- (/ mouse-y half-tile-height)
                        (/ mouse-x half-tile-width))
                     2)
                  0.4)
        #_(+ (* mouse-x half-tile-height)
           (* mouse-y half-tile-height))

        adjusted-tile-y (if (neg? tile-y)
                          (-> tile-y dec int)
                          (int tile-y))]
    
    [tile-x adjusted-tile-y]))


(defn tile->screen
  [tile-x tile-y]
  (let [half-tile-width (/ (:w tile-size) 2)
        half-tile-height (/ (:h tile-size) 2)

        screen-x (* (- tile-x tile-y) half-tile-width)
        screen-y (* (+ tile-x tile-y) half-tile-height)]
    
    [screen-x screen-y]))

;; --------------- PIXI ------------------

(defonce initial-state
  {:pixi
   {:app
    (js/PIXI.Application. #js{:width 2048 :height 2048
                              :backgroundColor #_"0xFFFFFF" #_"0x4C7C3E"
                              "0x8B7355"
                              #_"0x5A3A22"
                              #_"0xA08D89"})
    :sprites {:background {}
              :objects {}
              :overlays {:grid (js/PIXI.Sprite.from "images/Grid Style B - 2048x2048 - 128x64 - blue.png")}}
    :containers {:objects (js/PIXI.Container.)}}})


(defonce app-state
  (atom initial-state))


(defn reset-app-state!
  []
  (reset! app-state initial-state))


(defn create-herb-sprite
  [herb-key herb-id]
  (let [file-name (case herb-key
                    :basil "images/basil.png"
                    :sage "images/sage.png"
                    :parsley "images/parsley.png"
                    :thyme "images/thyme.png"
                    :oregano "images/oregano.png")
        herb-sprite (js/PIXI.Sprite.from file-name)]
    (swap! app-state
           assoc-in
           [:pixi :sprites :objects herb-id]
           herb-sprite)))


(defn add-herb-sprite!
  [herb-key]
  :TODO)


(def herb-templates
  {:basil {:offset [-6 -4]}
   :oregano {:offset [-7 -4]}
   :parsley {:offset [-7 -4]}
   :sage {:offset [-9 -6]}
   :thyme {:offset [-6 -2]}})

(defn add-sprite!
  "access-key is :objects, :background, or :overlay"
  [{:keys [app sprites containers] :as pixi-state} access-key sprite-id]
  (if-let [container (get containers access-key)]
    (.addChild container (get-in sprites [access-key sprite-id]))
    (.addChild (.-stage app) (get-in sprites [access-key sprite-id]))))

(defn create-herb
  "herb-key is any of #{:basil :thyme :oregano :sage :parsley}"
  [herb-key]
  (let [herb-id (-> herb-key
                    name
                    gensym
                    keyword)
        herb-sprite (create-herb-sprite herb-key herb-id)]
    
    (swap! app-state #(-> %
                          (assoc-in [:herbs herb-id]
                                    (get herb-templates herb-key))
                          (assoc :transplanting herb-id)))
    
    ;; just here temporarily
    (add-sprite! (:pixi @app-state) :objects herb-id)))


(defn add-pixi
  [{:keys [app] :as pixi-state}]
  (let [app-holder (js/document.getElementById "app-holder")]
    (.appendChild app-holder (.-view app))))




(defn add-all-sprites
  [app {:keys [background objects overlays] :as all-sprites}]
  (doseq [sprite (concat (vals background)
                         (vals objects)
                         (vals overlays))]
    (.addChild (.-stage app) sprite)))


(defn add-all-containers
  [app {:keys [objects] :as all-containers}]
  (.addChild (.-stage app) objects))

(defn setup-pixi
  [{:keys [app sprites containers] :as pixi-state}]
  (do (add-pixi pixi-state)
      #_(add-sprite pixi-state)

      ;; So sprites' z-index will work properly
      (set! (.-sortableChildren (:objects containers)) true)      
      
      (add-all-sprites app sprites)
      (add-all-containers app containers)))


;; ---------------- mouse/input events --------------------

(defn dispatch-click
  [event]
  #_(js/console.log event)
  (let [mouse-x (.-pageX event)
        mouse-y (.-pageY event)]

    ;; TODO: when transplanting, plant it
    ;;       when not transplanting but over a selectable item, select item 
    
    (when-let [herb-id (:transplanting @app-state)]
      (swap! app-state dissoc :transplanting))
    (println mouse-x mouse-y)
    (println (mouse->tile mouse-x mouse-y))))

(defn dispatch-move
  [event]
  #_(js/console.log event)
  (let [mouse-x (.-pageX event)
        mouse-y (.-pageY event)

        [tile-x tile-y :as tile-xy] (mouse->tile mouse-x mouse-y)

        former-xy (:mouse-xy @app-state)]

    ;; set tile-x and tile-y in app-state, only when changed
    (when (not= tile-xy former-xy)
      (swap! app-state assoc :mouse-xy tile-xy)
      (when-let [herb-id (:transplanting @app-state)]        
        (let [herb-sprite (get-in @app-state [:pixi :sprites :objects herb-id])
              [offset-x offset-y] (get-in @app-state [:herbs herb-id :offset])
              
              adjusted-x (+ tile-x offset-x)
              adjusted-y (+ tile-y offset-y)

              [screen-x screen-y] (tile->screen adjusted-x adjusted-y)]
          (println screen-x screen-y)
          (println offset-x offset-y)
          (println "z-index: " (+ tile-x tile-y))
          (set! (.-x herb-sprite) screen-x)
          (set! (.-y herb-sprite) screen-y)
          (set! (.-zIndex herb-sprite) (+ tile-x tile-y)
                #_(+ adjusted-x adjusted-y))))
      #_(println "move: " tile-xy))))


(defn setup-inputs!
  [{:keys [app] :as pixi-state}]
  (.addEventListener (.. app -renderer -view)
                     "click"
                     #(dispatch-click %))
  (.addEventListener (.. app -renderer -view)
                     "mousemove"
                     #(dispatch-move %)))


;; ------------------ UI --------------------

(defn ui
  []
  [:div#ui {:style {:position "fixed"
                    :bottom 0
                    :margin 10}}
   (for [herb-key (keys herb-templates)]
     [:button {:style {:padding 20
                       :margin 10}
               :on-click #(create-herb herb-key)}
      (str "Add " (s/capitalize (name herb-key)))])])

(defn mount-ui
  []
  (rd/render [ui]
             (.getElementById js/document "ui-holder")) 
  #_(when-not (:loaded? @game-state)
      (init-game!)))

;; -------------------------------------------

(defonce _ (do (setup-pixi (:pixi @app-state))
               (setup-inputs! (:pixi @app-state))
               (mount-ui)))

(defonce elapsed (atom 0.0))

#_(.add (.-ticker pixi-app)
      (fn [delta]
        (swap! elapsed + delta)
        (set! (.-x pixi-sprite) @elapsed)))


;; --------------- Howler ------------------

(def sound (js/Howl. #js{:src "sounds/drop_001.ogg"}))

;; eval this to play the sound
#_(.play sound)
