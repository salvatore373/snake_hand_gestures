# Snake Hand Gestures and Tilt Control Game

Welcome to the reinvented version of the classic **Snake** game! This project takes the timeless gameplay of Snake and enhances it with innovative, interactive controls and a touch of modern technology. Dive in to collect prizes, grow your snake, and compete globally—all while controlling the game in groundbreaking ways.

---

## 🌟 **Features**

### **Innovative Controls**
Choose between two unique control modes for a personalized gaming experience:

1. **Hand Gesture Recognition**  
   Guide the snake by interacting with a **camera preview grid** displayed on top of the snake's grid. The rectangle is divided into four sections corresponding to the four directions of movement: up, down, left, and right.
    - To change direction, place your hand in the appropriate section of the rectangle.
    - When you're ready, close your hand to signal the snake to move in the chosen direction.  
      This mode combines computer vision and interactive input for a futuristic twist on gameplay.

2. **Device Tilting**  
   Control the snake by tilting (rolling or pitching) your phone. The game captures roll and pitch angles using device sensors to provide smooth and intuitive movement.

### **Global Leaderboard**
After the game ends, submit your score to a **Leaderboard hosted on Firestore**, allowing you to compete with players worldwide. Climb the ranks and showcase your skills to a global audience.

### **Customizable Game Parameters**
Tailor your gameplay:
- **Difficulty Levels**: Choose from three levels to match your skill, adjusting the snake’s speed for a tailored challenge.
- **Snake Skin Colors**: Personalize your snake's appearance with customizable skins.

### **Optimized Gameplay**
- **Real-Time Input Handling**: Smooth and responsive gameplay using sensor input and gesture detection.
- **Efficient Codebase**: Designed with Kotlin and Android best practices, leveraging coroutines to manage UI updates and game logic without delays or freezing.

---

## 🚀 **Technical Highlights**

### **Hand Gesture Recognition**
- Leverages computer vision to detect hand placement and gestures in a rectangle displayed above the snake grid.
- Translates gestures like **closing a hand in a specific rectangle section** into directional movement for the snake.

### **Device Tilt Control**
- Utilizes the **accelerometer** and **magnetometer** to calculate roll and pitch angles.
- Processes orientation data via the Android `SensorManager` to ensure precise snake movement.
- Efficiently handles sensor updates without overwhelming the UI thread, ensuring smooth gameplay.

### **Game Logic**
- Manages game states (grid occupation, prize location, collisions) in a well-optimized logic engine.
- Integrates **coroutines** to efficiently handle delays and smooth transitions between game states.

### **Cloud Integration**
- Incorporates **Firestore** for:
    - **Leaderboard**: Stores player scores and rankings.
    - **Authentication**: Secures player access.
    - **Cloud Functions**: Enhances interactions and engagement.

---

## 📜 **Conclusion**
This project showcases how classic games can evolve with cutting-edge technology. By utilizing hand gesture recognition and device tilt controls, we’ve created an engaging and immersive experience that combines nostalgic fun with modern innovation. Add a competitive edge by sharing your scores on the global leaderboard and aim for the top!

Thank you for exploring this journey with us—have fun playing Snake like never before!
