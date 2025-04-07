# 2024-4e-hermankova-syntezator


## Popis projektu
Tento projekt je interaktivní syntetizátor vytvořený v JavaFX, který simuluje digitální syntezátor. 
Uživatelé mohou upravovat parametry zvuku pomocí otočných knobů a sledovat změny na osciloskopu.

## Funkce
- **Rotující knoby** pro úpravu parametrů (volume, tune, width, color, depth, ADSR obálka).
- **Tlačítka pro výběr vlnového průběhu** (sinus, obdélník, pila).
- **Osciloskop** zobrazující zvukový signál v reálném čase.
- **Grafické vylepšení** pomocí CSS pro moderní vzhled.

## Instalace a spuštění

1. **Naklonujte repozitář**
    ```bash
    git clone https://github.com/gyarab/2024-4e-hermankova-syntezator.git
    ```

2. **Otevřete projekt v IDE**
    - Doporučeno: IntelliJ IDEA nebo Eclipse s podporou Maven.

3. **Zkontrolujte závislosti**
    - JDK 17+

4. **Spusťte aplikaci**
    - Hlavní třída: `MainApp.java`
    - Příkaz:
    ```bash
    mvn javafx:run
    ```
