# Programmierung verteilter Systeme: Mandelbrot

Dieses Projekt berechnet und visualisiert die Mandelbrotmenge. Mit Hilfe von Java RMI wird die Arbeitslast auf mehrere Server(Worker) verteilt. Die Benutzeroberfläche basiert auf Java Swing, und der Client folgt dem Model-View-Presenter Architekturmuster.

## Inhaltsverzeichnis

- [Gruppenmitglieder](#gruppenmitglieder)
- [Projektstruktur](#projektstruktur)
- [Technologien](#technologien)
- [Starten des Programmes](#starten-des-programmes)
- [Alternative: Manuelle Ausführung](#alternative-manuelle-ausführung)
  - [1. Master starten](#1-master-starten)
  - [2. Worker starten](#2-worker-starten)
  - [3. Starten des Clients](#3-starten-des-clients)
- [Hinweise](#hinweise)

## Gruppenmitglieder

- Valine Richter s85875
- Anastasia Suglobow s84401
- Marlene Pannoscha s83814 (Gruppensprechen)

## Projektstruktur

```bash
Mandelbrot-9/
├── client/
│   ├── client.config                # Konfigurationsdatei für den Client
│   ├── Client.java                  # Einstiegspunkt für den Client
│   ├── ClientModel.java            # Model-Klasse (MVP)
│   ├── ClientPresenter.java        # Presenter-Klasse (MVP)
│   ├── ClientView.java             # View-Klasse (MVP)
│   ├── MasterInterface.java        # RMI-Interface zum Master
│   ├── WorkerInterface.java        # RMI-Interface zum Worker
│   └── start.bat                   # Startskript für den Client
│
├── master/
│   ├── Master.java                 # Einstiegspunkt für den Master-Server
│   ├── MasterInterface.java       # RMI-Interface für Clients/Worker
│   ├── WorkerInterface.java       # Interface-Replikat für Worker-Kommunikation
│   └── start.bat                  # Startskript für den Master
│
├── worker/
│   ├── MasterInterface.java       # Interface-Replikat für Verbindung zum Master
│   ├── Worker.java                # Einstiegspunkt für den Worker
│   ├── WorkerInterface.java       # RMI-Interface für Fraktalberechnung
│   └── start.bat                  # Startskript für den Worker
│
├── start_all.bat                  # Globales Startskript (z. B. Client starten)
├── README.md                      # Diese Dokumentation
└── .gitignore                     # Git-Ausschlussdateien
```

## Technologien

- Java 8+
- RMI
- Swing (BufferedImage)
- MVP-Muster (Model-View-Presenter)
- Visual Studio Code

## Starten des Programmes

Startskript ausführen:

```bash
./start_all.bat
```

Das Skript kompiliert alle Java-Dateien.

## Alternative: Manuelle Ausführung

## 1. Master starten

```bash
@echo off
javac Master.java
java Master 5000
```

```bash
java Master <Master Port>
```

## 2. Worker starten

Starte beliebig viele Worker, die sich bei dem Master registrieren. Jeder Worker benötigt die IP-Adresse und den Port des Masters.

```bash
javac Worker.java
java Worker localhost 5000
```

```bash
java Worker <Master IP> <Master Port>
```

## 3. Starten des Clients

Es können mehrere Clients gestartet werden. Der Client verbindet sich mit dem Master, öffnet die Benutzeroberfläche und startet die Berechnung.

```bash
javac Client.java
java Client localhost 5000
```

```bash
java Client <Master IP> <Master Port>
```

## Hinweise

- Die Anzahl der Client-Threads sollte mindestens so hoch sein wie die Gesamtanzahl der gestarteten Worker.
- Das Ergebnis des `yPixelsel/workerthread` sollte in Ganzzahlen vorliegen, um eine bessere Arbeitsverteilung und zu gewährleisten.
- Alle Parameter (z.B. Zoompunkt, Iterationsanzahl oder Auflösung) lassen sich mittels der Benutzeroberfläche oder über die Datei _client.config_ anpassen
