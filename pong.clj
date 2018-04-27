(use '[quil.core])
(import 'java.awt.event.KeyEvent)

(def WIDTH 600)
(def HEIGHT 400)

;; We want a high frame rate so that the ball will travel fast.
(def FPS 100)

(def racket-width 10)
(def racket-height 70)

(def score-left (atom 0))
(def score-right (atom 0))

(defn reset-game!
  "Returns the game's internal state to it's original value."
  []
  (def game-paused? (atom false))
  ;; These are the ping pong rackets that the players will move.
  (def left-racket (atom {:x 10 :y 150 :w racket-width :h racket-height}))
  (def right-racket (atom {:x (- WIDTH 20) :y 150 :w racket-width :h racket-height}))

  ;; The ball that the players will play with.
  ;; Since the ball is just a square, we could say that it is a smaller racket.
  (def ball (atom {:x 225 :y 175 :w 10 :h 10}))

  ;; The ball's direction as two x,y coordinates.
  ;; Initially it flies to the right, hence why x=1 (meaning x will increase by 1 each tick) and y=0.
  (def ball-dir (atom [1 0])))

(defn draw-racket
  "Draws a pong racket on screen.
  The racket is just a map like this:
  {:x 0   ; x coordinate
   :y 0   ; y coordinate
   :w 10  ; width
   :h 20} ; height"
  [racket]
  (rect (:x racket) (:y racket) (:w racket) (:h racket)))

(defn move-ball
  "Moves the ball in the specified direction.
  The ball just a map describind a small square: {:x 225 :y 100 :w 10 :h 10}.
  The direction is a x,y vector like this [1 0].
  This means the ball flies to the right, hence why x=1 (meaning x will increase by 1 each tick) and y=0.
  If we had specified [-1 0], then the ball would fly to the left, because x will decrease by 1 each tick."
  [ball direction]
  (let [dx (first direction)
        dy (second direction)]
    (assoc
      ball
      :x (+ (:x ball) dx)
      :y (+ (:y ball) dy))))

(defn hit-location [racket ball]
  "calculate the 'location' where the ball hit the racket
  => it's 0.5 if hit at the top of the racket
  => it's 0 if hit at the middle of the racket
  => it's -0.5 if hit at the bottom of the racket
  The ball and racket are maps like this: {:x 225 :y 100 :w 10 :h 10}."
  (-
   (/ (- (:y ball) (:y racket)) (:h racket))
   0.5))

(defn rect-intersects?
  "Determines if two rectangles have hit each other (aka they intersect).
  The rectangles (a) and (b) are maps like this: {:x 225 :y 100 :w 10 :h 10}."
  [a b]
  (let [top-left-corner-a-x (:x a)
        top-left-corner-a-y (:y a)
        bottom-right-corner-a-x (+ (:x a) (:w a))
        bottom-right-corner-a-y (+ (:y a) (:h a))
        top-left-corner-b-x (:x b)
        top-left-corner-b-y (:y b)
        bottom-right-corner-b-x (+ (:x b) (:w b))
        bottom-right-corner-b-y (+ (:y b) (:h b))]
    (if (and
          (<= top-left-corner-a-x bottom-right-corner-b-x)
          (>= bottom-right-corner-a-x top-left-corner-b-x)
          (<= top-left-corner-a-y bottom-right-corner-b-y)
          (>= bottom-right-corner-a-y top-left-corner-b-y))
      true
      false)))

(defn pause-game []
  (background 0 0 0)
  (stroke 255 255 255)
  (fill 255 255 255)
  (text-size 20)
  (text-align :center)
  (text "Press R or CLICK with the mouse to resume." (/ WIDTH 2) 50)
  (text (str "Player 1 score: " @score-left) (/ WIDTH 2) 90)
  (text (str "Player 2 score: " @score-right) (/ WIDTH 2) 150))

(defn handle-racket-collision!
  "Handle the case where the ball collides with a racket."
  [racket ball]
  (when (rect-intersects? @racket @ball) ;; ball hit the racket?
    (let [l (hit-location @racket @ball)]
      ;; invert x direction, set y direction to follow the hit-direction
      (swap! ball-dir (fn [[x _]] [(- x) l])))))

(defn player1-scored?
  []
  (> (:x @ball) WIDTH))

(defn player2-scored?
  []
  (< (:x @ball) 0))
  
(defn update-state []
  ;; move the ball into its direction
  (swap! ball move-ball @ball-dir)
  (handle-racket-collision! left-racket ball)
  (handle-racket-collision! right-racket ball)
  ;; ball hit top or bottom border?
  (when (or (> (:y @ball) (- HEIGHT (:h @ball))) (< (:y @ball) 0))
    ; invert y direction
    (swap! ball-dir (fn [[x y]] [x (- y)])))
  (when (player1-scored?)
    (swap! score-left inc)
    (swap! game-paused? not))
  (when (player2-scored?)
    (swap! score-right inc)
    (swap! game-paused? not)))

(defn draw []
  ;; Set background color to dark gray, draw color to white.
  (background-float 0x20)
  (fill 0xff)
  ;; Draw rackets.
  (draw-racket @left-racket)
  (draw-racket @right-racket)
  ;; Draw the ball the same way we draw the rackets (since the ball is just a small square).
  (draw-racket @ball))

(defn key-pressed []
  (letfn [(move-up [x]
            (if (<= x 0)
              0
              (- x 10)))
          (move-down [x]
            (if (>= x (- HEIGHT racket-height))
              (- HEIGHT racket-height)
              (+ x 10)))]
    (cond
      ;; Left racket is controlled with W (up) and S (down).
      (= (key-code) KeyEvent/VK_W)    (swap! left-racket update-in [:y] move-up)
      (= (key-code) KeyEvent/VK_S)    (swap! left-racket update-in [:y] move-down)
      ;; Right racket is controlled with the arrow keys.
      (= (key-code) KeyEvent/VK_UP)   (swap! right-racket update-in [:y] move-up)
      (= (key-code) KeyEvent/VK_DOWN) (swap! right-racket update-in [:y] move-down)
      (= (key-code) KeyEvent/VK_R)    (reset-game!))))

(defn setup []
  (smooth)
  (no-stroke)
  (frame-rate FPS))

(defn draw-game []
  (if @game-paused? (pause-game)
    (do
      (update-state) ;; NOTE: First we update the state, then we draw.
      (draw))))

(reset-game!)

;; Start the game paused.
(swap! game-paused? not)

(defsketch pong
  :title "Pong"
  :features [:exit-on-close]
  :size [WIDTH HEIGHT]
  :setup setup
  :draw draw-game
  :mouse-clicked #(reset-game!)
  :key-pressed key-pressed)
