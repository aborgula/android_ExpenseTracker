# Projekt aplikacji mobilnej na zajęcia z testowania oprogramowania

## Autor
Alicja Borgula

## Temat projektu
Aplikacja do śledzenia wydatków TrackExpense

## Opis projektu
TrackExpense to aplikacja mobilna, która umożliwia użytkownikom zapisywanie i kontrolowanie codziennych wydatków. Po uruchomieniu aplikacji wymagane jest zarejestrowanie się lub zalogowanie przy użyciu nazwy użytkownika, adresu e-mail oraz hasła.
Aplikacja pozwala dodawać wydatki, kategoryzować je i analizować historię finansową użytkownika. Podczas dodawania wydatku należy podać jego nazwę, wybrać kategorię, określić datę oraz kwotę. Zapisane wydatki są wyświetlane w formie listy, którą można przefiltrować według kategorii i kwoty oraz posortować według ceny, nazwy lub daty.
Po przełączeniu się w dolnym menu na ekran statystyk, użytkownik może przeglądać wizualizacje wydatków na wykresach, prezentujące dane z ostatniego dnia, dwóch dni, tygodnia, miesiąca lub roku.
Celem projektu jest ułatwienie zarządzania budżetem osobistym oraz zastosowanie praktyk testowania i zapewniania jakości oprogramowania.

## Uruchomienie projektu
1. Otworzyć Android Studio.
2. Wybrać opcję **File → New → Project from Version Control**.
3. Wkleić adres URL repozytorium projektu i kliknąć **Clone**.
4. Kliknąć przycisk **Run**, aby uruchomić aplikację.

## Testy

### Testy jednostkowe

Testy jednostkowe zostały zaimplementowane w celu weryfikacji poprawności
logiki biznesowej związanej z obsługą wydatków. Testy zostały napisane z wykorzystaniem frameworka **JUnit 4** i obejmują
różne scenariusze działania metod, w tym przypadki brzegowe.

Początkowo testowana jest klasa `ExpenseService`, odpowiedzialna za sortowanie
oraz filtrowanie listy wydatków.

**Zakres testów jednostkowych dla klasy ExpenseService:**

1. Sortowanie wydatków według kwoty (rosnąco).
2. Sortowanie wydatków według daty (malejąco).
3. Sortowanie alfabetyczne według nazwy.
4. Obsługę pustej listy wydatków.
5. Filtrowanie wydatków według minimalnej i maksymalnej kwoty.
6. Filtrowanie według jednej kategorii.
7. Filtrowanie według wielu kategorii.
8. Łączenie filtrów (kwota i kategoria).
9. Brak filtrów – zwrócenie wszystkich wydatków.
10. Brak dopasowań – zwrócenie pustej listy.
11. Obsługę wydatków o wartości 0.
12. Poprawne działanie sortowania przy identycznych kwotach.


**Lokalizacja testów w projekcie:**  
[ExpenseServiceTest.java](app/src/test/java/com/example/expensetracker/service/ExpenseServiceTest.java)

---

Kolejnym testowanym elementem jest **model `Expense`**, który przechowuje dane pojedynczego wydatku.

**Zakres testów jednostkowych dla klasy Expense:**

13. Poprawność konstruktora oraz getterów (`Expense(...)` i `getId()`, `getName()`, `getDate()`, `getAmount()`, `getCategory()`, `getUserId()`).
14. Poprawne działanie settera `setId()`.
15. Poprawne ustawianie i zwracanie kwoty wydatku.
16. Sprawdzanie domyślnych wartości pól przy pustym konstruktorze.

**Lokalizacja testów jednostkowych dla klasy Expense:**  
[ExpenseTest.java](app/src/test/java/com/example/expensetracker/model/ExpenseTest.java)


---

### Testy integracyjne

Kolejnym testowanym elementem jest **integracja `StatsViewModel` z `ExpenseService` i `ExpenseRepository`**, sprawdzająca prawidłową komunikację pomiędzy warstwami logiki biznesowej i LiveData.

**Zakres testów integracyjnych:**

1. Sprawdzenie, czy filtr czasowy `TODAY` zwraca tylko wydatki z dzisiaj i prawidłowo oblicza sumę
2. Sprawdzenie, czy filtr czasowy `MONTH` zwraca wydatki z ostatniego miesiąca i prawidłowo oblicza sumę
3. Weryfikacja, czy po zmianie filtra czasowego w `ViewModel` suma wydatków jest poprawnie przeliczana
4. Sprawdzenie, czy po pojawieniu się nowych danych w LiveData suma wydatków zostaje zaktualizowana

**Lokalizacja testów integracyjnych:**  
[StatsViewModelIntegrationTest.java](app/src/test/java/com/example/expensetracker/integration/StatsViewModelIntegrationTest.java)

---

W ramach projektu zaimplementowano również testy, których celem jest weryfikacja poprawnej współpracy pomiędzy warstwami aplikacji:
**Fragment → ViewModel → Repository → Service → LiveData**.

Testy te sprawdzają, czy dane są prawidłowo przekazywane pomiędzy komponentami,
czy logika biznesowa zawarta w serwisach jest poprawnie wykorzystywana przez ViewModel
oraz czy obserwatorzy otrzymują poprawne dane.

---

5. Sprawdzenie, czy logika sortowania w `ExpenseService` jest poprawnie stosowana,
   gdy wywoływana jest przez `ExpensesViewModel`, oraz czy dane poprawnie przepływają
   z repozytorium przez serwis do ViewModel.

6. Weryfikacja filtrowania wydatków przez `ExpensesViewModel` z wykorzystaniem
   logiki biznesowej `ExpenseService` (zakres kwot oraz kategorie).

7. Sprawdzenie interakcji pomiędzy `ExpensesViewModel` a `ExpenseRepository`
   podczas usuwania wydatku.

8. Sprawdzenie współpracy `ViewModel` i `Service` przy zastosowaniu złożonych filtrów
   (zakres kwot oraz wiele kategorii jednocześnie).

9. Weryfikacja, czy po zresetowaniu filtrów lista wydatków wraca do pierwotnego,
    niefiltrowanego stanu.

10. Sprawdzenie, czy po zastosowaniu filtrów możliwe jest sortowanie wyników
    bez utraty aktywnych filtrów.

**Lokalizacja testów integracyjnych komunikacji warstw:**  
[LayerCommunicationTest.java](app/src/test/java/com/example/expensetracker/integration/LayerCommunicationTest.java)


## Przypadki testowe dla testera manualnego


| ID   | Tytuł testu                                   | Cel testu                                     | Warunki początkowe                                           | Kroki                                                                                           | Dane wejściowe                          | Oczekiwany rezultat                                                                                  |
|------|------------------------------------------------|-----------------------------------------------|---------------------------------------------------------------|--------------------------------------------------------------------------------------------------|------------------------------------------|-------------------------------------------------------------------------------------------------------|
| TC01 | Sprawdzenie poprawnej rejestracji użytkownika  | Zweryfikować działanie procesu rejestracji    | Aplikacja otwarta, wyświetlany ekran rejestracji              | 1. Wpisz nazwę w pole "Name"<br>2. Wpisz email w pole "Email"<br>3. Wpisz hasło w pole "Password" | Name: user<br>Email: user@mail.com<br>Hasło: haslo2 | Wyświetlenie Toast “Registration successful!” i przejście do ekranu głównego z pustą listą wydatków |
| TC02 | Rejestracja ze zbyt krótkim hasłem             | Sprawdzić walidację długości hasła            | Aplikacja otwarta, wyświetlany ekran rejestracji              | 1. Wpisz nazwę w pole "Name"<br>2. Wpisz email w pole "Email"<br>3. Wpisz hasło w pole "Password" | Name: user<br>Email: user@mail.com<br>Hasło: haslo | Wyświetlenie Toast “Password must be at least 6 characters”                                          |
| TC03 | Pomyślne zalogowanie użytkownika               | Zweryfikować poprawne logowanie               | Aplikacja otwarta, ekran logowania; użytkownik istnieje w bazie | 1. Wpisz email w pole "Email"<br>2. Wpisz hasło w pole "Password"                                 | Email: user@mail.com<br>Hasło: haslo1    | Wyświetlenie Toast “Login successful” i przejście do ekranu głównego z listą wydatków                |
| TC04 | Pomyślne dodanie nowego wydatku                | Zweryfikować dodawanie wydatku                | Aplikacja otwarta, użytkownik zalogowany                      | 1. Naciśnij przycisk “+”<br>2. Wpisz tytuł wydatku<br>3. Wybierz kategorię<br>4. Wybierz datę<br>5. Wpisz kwotę<br>6. Kliknij “Save Expense” | Tytuł: kino<br>Kategoria: entertainment<br>Data: 29.11.2015<br>Kwota: 15 | Wyświetlenie Toast “Expense saved” i powrót do ekranu głównego z listą wydatków                       |
| TC05 | Próba dodania wydatku z pustymi polami         | Sprawdzić walidację wymaganych pól            | Aplikacja otwarta, użytkownik zalogowany                      | 1. Naciśnij przycisk “+”<br>2. Pozostaw pole “Name” puste<br>3. Wybierz kategorię<br>4. Wybierz datę<br>5. Wpisz kwotę<br>6. Kliknij “Save Expense” | Kategoria: entertainment<br>Data: 29.11.2015<br>Kwota: 15 | Wyświetlenie Toast “Please fill in all fields”                                                       |
| TC06 | Usunięcie wydatku poprzez przeciągnięcie    | Zweryfikować usuwanie wydatku z listy        | Aplikacja otwarta, użytkownik zalogowany, wyświetlana zakładka z listą wydatków | 1. Przeciągnij wiersz w liście z prawej do lewej<br>2. Potwierdź okno dialogowe klikając “yes” | — | Wyświetlenie Toast “Expense deleted”, lista automatycznie odświeżona - wydatek zniknął |
| TC07 | Anulowanie usunięcia wydatku               | Zweryfikować anulowanie usunięcia wydatku    | Aplikacja otwarta, użytkownik zalogowany, wyświetlana zakładka z listą wydatków | 1. Przeciągnij wiersz w liście z prawej do lewej<br>2. Kliknij “no” w oknie dialogowym | — | Okno potwierdzenia znika, wydatek wraca na swoje miejsce animacją z lewej do prawej |
| TC08 | Sortowanie wydatków przez kwotę malejąco    | Zweryfikować poprawność sortowania wydatków  | Aplikacja otwarta, użytkownik zalogowany, w liście minimum 3 wydatki o różnych kwotach | 1. Kliknij przycisk “Sort”<br>2. Wybierz opcję “Amount ↓” | — | Lista odświeżona, u góry wydatki większe, niżej mniejsze |
| TC09 | Filtrowanie wydatków poprzez zakres cen     | Zweryfikować poprawność filtrowania wydatków | Aplikacja otwarta, użytkownik zalogowany, w liście wydatki o kwotach 9, 12, 20 | 1. Kliknij przycisk “Filter”<br>2. Wpisz wartość w pole “Min amount”<br>3. Wpisz wartość w pole “Max amount”<br>4. Kliknij “Save” | Min: 10<br>Max: 15 | Pokazany komunikat “Filters applied”, lista odświeżona, wyświetlany tylko wydatek o kwocie 12 |
| TC10 | Filtrowanie wydatków przez kategorię        | Zweryfikować poprawność filtrowania po kategorii | Aplikacja otwarta, użytkownik zalogowany, w liście wydatki o kategoriach: Food, Entertainment, Health | 1. Kliknij przycisk “Filter”<br>2. Zaznacz tylko opcję “Health”<br>3. Kliknij “Save” | — | Pokazany komunikat “Filters applied”, lista odświeżona, wyświetlane tylko wydatki z kategorii “Health”, pozostałe ukryte |
| TC11 | Resetowanie wszystkich filtrów             | Zweryfikować poprawność resetowania filtrów    | Aplikacja otwarta, użytkownik zalogowany, zaaplikowano filtry cen i kategorii, niektóre wydatki ukryte | 1. Kliknij przycisk “Filter”<br>2. Kliknij przycisk “Reset” | — | Pokazany komunikat “Filters reset”, lista odświeżona, wszystkie wydatki pokazane, w oknie filtrów pola wyboru odznaczone, pola kwot puste |
| TC12 | Filtrowanie statystyk według tygodnia      | Zweryfikować poprawność filtrowania statystyk po tygodniu | Aplikacja otwarta, użytkownik zalogowany, ekran statystyk, w liście 3 wydatki z poprzedniego tygodnia i 2 sprzed 2 tygodni | 1. W poziomej liście kliknij przycisk “Week” | — | Przycisk “Week” zielony, “Today” biały; pole “Total” pokazuje sumę wydatków z ubiegłego tygodnia; wykres pokazuje tylko te wydatki |
| TC13 | Wyświetlenie dymku informacyjnego nad punktem na wykresie | Zweryfikować wyświetlanie szczegółów wydatku na wykresie | Aplikacja otwarta, użytkownik zalogowany, ekran statystyk, co najmniej jeden wydatek widoczny na wykresie | 1. Kliknij na punkt na wykresie | — | Nad punktem pojawia się dymek z datą dodania wydatku, kwotą i wartością, każda informacja w innym kolorze |


## Użyte technologie

Projekt został zrealizowany z wykorzystaniem następujących technologii, narzędzi oraz bibliotek:

### Platforma i język
- **Android SDK** – platforma do tworzenia aplikacji mobilnych na system Android.
- **Java 8** – język programowania użyty do implementacji logiki aplikacji.
- **Android Studio** – środowisko programistyczne (IDE) wykorzystywane do tworzenia, uruchamiania i testowania aplikacji.

### Architektura aplikacji
- **MVVM Model–View–ViewModel** – architektura aplikacji oddzielająca logikę biznesową od warstwy interfejsu użytkownika.
- **ViewModel** – komponent odpowiedzialny za zarządzanie danymi UI.
- **LiveData** – obserwowalne źródło danych, umożliwiające reagowanie interfejsu na zmiany danych.

### Interfejs użytkownika
- **ConstraintLayout** – elastyczny system tworzenia układów interfejsu.
- **MPAndroidChart** – biblioteka do wizualizacji danych w postaci wykresów (statystyki wydatków).

### Przechowywanie danych
- **Firebase Authentication** – obsługa rejestracji i logowania użytkowników.
- **Firebase Firestore** – chmurowa baza danych do przechowywania wydatków.
- **Firebase Realtime Database** – przechowywanie i synchronizacja danych w czasie rzeczywistym.
- **Android DataStore (Preferences)** – lokalne przechowywanie ustawień aplikacji.

### Testowanie
- **JUnit 4** – framework do tworzenia testów jednostkowych.
- **Mockito** – biblioteka do tworzenia mocków i testowania zależności pomiędzy komponentami.
