# Applicativo Java ToDo - Applicazione Desktop

## Descrizione del Progetto

L’applicazione desktop, sviluppata in **Java con Swing**, fornisce una gestione centralizzata di attività e To-Do. Permette agli utenti di organizzare i propri impegni in boards e di condividerle con altri utenti. Grazie a un database relazionale **PostgreSQL**, l’app consente di salvare e sincronizzare le attività tra diverse sessioni.

## Funzionalità Principali

  - **Autenticazione**
    Accesso tramite email e password; supporta la registrazione di nuovi utenti.

  - **Dashboard Utente**
    Una schermata principale che riassume tutte le boards create o condivise con l'utente, permettendo un rapido accesso.

  - **Gestione Bacheche (Boards)**
    Creazione, modifica ed eliminazione di bacheche per raggruppare logicamente le attività (es. "Lavoro", "Università", "Tempo Libero").

  - **Gestione To-Do**
    Inserimento, aggiornamento (es. cambio di stato) ed eliminazione di singole attività (To-Do) all'interno di una bacheca specifica.

  - **Condivisione**
    Funzionalità per condividere intere bacheche con altri utenti registrati.

## Requisiti

  - Java 23
  - PostgreSQL
  - Maven 23

## Installazione e Avvio

1.  **Clona il repository**

    ```bash
    git clone <https://github.com/v-oratore/Applicativo-Java-ToDo.git>
    cd Applicativo-Java-ToDo
    ```

2.  **Importa il database**

      - Apri pgAdmin.
      - Crea un nuovo database (es. `todo_app_db`).
      - Esegui lo script SQL per la creazione delle tabelle.

3.  **Configura la connessione**
    Apri il file:

    ```
    src/main/java/database/ConnessioneDatabase.java
    ```

    e aggiorna i campi `url`, `user` e `password` con le tue credenziali:

    ```java
    private String url = "jdbc:postgresql://localhost:5432/todo_app_db"; // Usa il nome del DB creato al punto 2
    private String user = "postgres";
    private String password = "<LA_TUA_PASSWORD>";
    ```

4.  **Compila e avvia**
    Utilizzando Maven:

    ```bash
    mvn clean package
    java -jar target/Applicativo-1.0-SNAPSHOT.jar
    ```

## Contribuire

Per bug, suggerimenti o contributi, apri un *issue* o invia una *pull request* su GitHub.
