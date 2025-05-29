# 3D Solar System Simulator - JavaFX

This project is a 3D Solar System Simulator built with **JavaFX**. It provides an interactive and responsive environment to explore the universe based on fundamental Newton's Laws.

---

## Features

* **Physics Engine**: Developed a custom physics engine in Java, implementing **Newton's Law of Gravitation** ($F = G\frac{m_1m_2}{R^2}$) to calculate forces between celestial bodies.
* **Real-Life Scale**: The simulator uses a realistic scale for its units:
    * **1 distance unit = 1 AU (Astronomical Unit)**
    * **1 mass unit = 1 Solar Mass**
    * **1 time unit = 1 Earth Year**
* **Interactive 3D Environment**: All components, including the 3D elements, are interactive thanks to JavaFX.
* **Educational Tool**: An excellent resource for teaching children about our solar system and planets.
* **Responsive Simulation**: Users can immediately observe the impact of changing parameters (e.g., altering the Sun's mass) on the solar system's dynamics.

---

## Pre-requisites

* **Java 17 or newer**
* **Maven** or **Maven Daemon (mvnd)**

---

## How to Run

Follow these simple steps to get the simulator up and running on your machine:

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/NumeralTiger/JAVAFX
    ```
2.  **Navigate into the project directory:**
    ```bash
    cd JAVAFX
    ```
3.  **Open the project in your code editor:**
    ```bash
    code .
    ```
4.  **Run the application:**
    ```bash
    mvnd javafx:run
    ```
    Alternatively, if you don't have Maven Daemon installed, you can use:
    ```bash
    mvn javafx:run
    
