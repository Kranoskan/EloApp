# EloBoard 🏆

[Español](#español) | [English](#english)

---

## Español

EloBoard es una aplicación Android diseñada para entusiastas de los juegos de mesa que desean llevar un registro detallado de sus partidas y medir el nivel de habilidad de los jugadores mediante un sistema de clasificación Elo dinámico.

### 🚀 Características Principales

*   **Gestión de Jugadores y Juegos**: Registra tu colección de juegos y a tu grupo de amigos.
*   **Registro Detallado de Partidas**: Soporta partidas por equipos y Todos Contra Todos (FFA).
*   **Sistema Elo Avanzado**: Calcula el nivel de habilidad no solo de los jugadores, sino también de elementos del juego (facciones, turnos de inicio, reglas especiales).
*   **Recálculo Histórico**: Permite recalcular todas las puntuaciones desde el principio de los tiempos si se ajustan las fórmulas.
*   **Respaldo Local y Compartición**: Genera archivos comprimidos (.zip) con la base de datos para transferir tus datos a otros dispositivos fácilmente o guardarlos externamente.
*   **Interfaz Moderna**: Diseño basado en Material Design con navegación fluida.

### 🛠️ Cómo está programada

La aplicación sigue las mejores prácticas de desarrollo Android moderno:

*   **Lenguaje**: 100% Kotlin.
*   **Arquitectura**: MVVM (Model-View-ViewModel) para una separación de conceptos clara.
*   **Persistencia**: **Room Database** para el almacenamiento local, manejando relaciones complejas entre jugadores, juegos y partidas.
*   **Asincronía**: Uso de **Coroutines** y **Flow** para operaciones fluidas en segundo plano.
*   **Navegación**: Implementación mediante Fragmentos y `BottomNavigationView`.
*   **Gestión de Archivos**: Uso de `FileProvider` y `ZipUtils` para la exportación e importación segura de copias de seguridad.
*   **Lógica de Cálculo**: Algoritmo Elo personalizado que utiliza medias cuadráticas para calcular la fuerza de los equipos y trata los atributos del juego como "jugadores virtuales" para medir su equilibrio.

---

## English

EloBoard is an Android application designed for board game enthusiasts who want to keep a detailed record of their matches and measure player skill levels using a dynamic Elo rating system.

### 🚀 Key Features

*   **Player and Game Management**: Register your game collection and your group of friends.
*   **Detailed Match Recording**: Supports team-based matches and Free-For-All (FFA).
*   **Advanced Elo System**: Calculates skill levels not only for players but also for game elements (factions, starting turns, special rules).
*   **Historical Recalculation**: Allows recalculating all scores from the beginning of time if formulas are adjusted.
*   **Local Backup & Sharing**: Generate compressed files (.zip) with the database to easily transfer your data to other devices or save them externally.
*   **Modern Interface**: Material Design-based UI with smooth navigation.

### 🛠️ Technical Implementation

The application follows modern Android development best practices:

*   **Language**: 100% Kotlin.
*   **Architecture**: MVVM (Model-View-ViewModel) for clear separation of concerns.
*   **Persistence**: **Room Database** for local storage, handling complex relationships between players, games, and matches.
*   **Asynchrony**: Use of **Coroutines** and **Flow** for smooth background operations.
*   **Navigation**: Implemented using Fragments and `BottomNavigationView`.
*   **File Management**: Use of `FileProvider` and `ZipUtils` for secure export and import of backups.
*   **Calculation Logic**: Custom Elo algorithm that uses quadratic means to calculate team strength and treats game attributes as "virtual players" to measure their balance.
