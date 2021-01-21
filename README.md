# Digitaler Einkaufszettel Android (Java)
### Einleitung:

Ziel war es eine App für den privaten Gebrauch zu konzipieren, die das manuelle Schreiben von Einkaufszetteln ersetzt. Später wurden noch Funktionalitäten für den Ersatz eines Haushaltskontos hinzugefügt, die eine private Buchhaltung für den Haushalt ermöglichen. Außerdem erhöht die App den Spaß beim Einkaufen ungemein =) 

### Nutzung des Einkaufszettels:

Items werden per "Long Click" einer Kategorie per Googlespracherkennung hinzugefügt. Die hinzugefügten Einträge des Einkaufszettels werden aufgrund des Google-Cloud-Services Firebase in Echtzeit zwischen den Apps aller Nutzer synchronisiert. Fehlerhafte Items können "weggeswiped" werden. Items können per Click als grün markiert werden. Durch die permamente Synchronisation können somit mehrere Nutzer gemeinsam im Ladengeschäft die Liste abarbeiten. Der Einkaufsfortschritt in der Toolbar gibt dabei ständig Auskunft über den aktuellen Fortschritt des Einkaufes und spornt zu Höchstleistungen an =). Sind alle Items einer Kategorie grün markiert, wird auch die jeweilige Kategorie grün markiert.

![grab-landing-page](https://github.com/KarlEisenkolb/DigitalerEinkaufszettelAndroid/blob/githubUpload/gifs/hinzufuegen.gif)

### Nach dem Einkauf:

Nach einem erfolgreichen Einkauf können die grün markierten Items vom Einkaufszettel per FloatingButton entfernt werden. Dabei öffnet sich ein DialogFragment, welches die Eingabe der Kosten des Einkaufes ermöglicht. Der Einkauf wird dann der Kategorie "Haushalt" im Buchaltungsteil der Applikation hinzugefügt.  

In der Buchaltungs-Activity werden in den einzelnen Tabs (Rechnungskategorien) alle vorhandenen Rechnungen angezeigt. In den Einstellungen können Anteile hinterlegt werden, nach denen sich die Nutzer die anfallenden Kosten der Rechnungen aufteilen.

> Tätigt Person A einen Einkauf über 100€ und zahlt davon 60%, schuldet ihm Person B nun 40€ und könnte somit den nächsten Einkauf übernehmen.

Der aktuelle "Kontostand" über alle Rechnungen wird in der Toolbar angezeigt. Ist dieser negativ, erscheint die Zahl in roter Farbe. Für jede Rechnungskategorie wird zudem angezeigt, ob es sich um eine Solo- oder Gruppenliste handelt und wieviele Belege die Liste enthält. Bei Umfassung eines ausreichenden Zeitraumes werden die Gesamtkosten aller Rechnungen addiert, durch den Gesamtzeitrum dividiert und als Kosten/Monat in € ausgegeben. Bei der Betrachtung zu kurzer Zeiträume der Rechnungsliste erscheint der Hinweis "NO AVERAGE", da die errechneten Durchschnitte ansonsten zu groß sind.

Es kann ohne Passwort zwischen der Anzeige für beide Nutzer über einen FloatingButton jederzweit gewechselt werden. Der Kontostand von Person A ist dabei natürlich dann der negative Betrag des Kontostands von Person B. 

![grab-landing-page](https://github.com/KarlEisenkolb/DigitalerEinkaufszettelAndroid/blob/githubUpload/gifs/einkauffertig.gif)

### Erstellung neuer Kategorien in den Settings:

Es lassen sich jederzeit neue Listen zur weiteren Kategorisierung von Kosten wie beispielweise Nebenkosten, Versicherungen oder Reisen erstellen.

![grab-landing-page](https://github.com/KarlEisenkolb/DigitalerEinkaufszettelAndroid/blob/githubUpload/gifs/neueliste.gif)

### Hinzufügen/Updaten/Löschen von Rechnungen zu einer Kategorie:

In den einzelnen Tabs können Rechnungen hinzugefügt, geupdated oder gelöscht werden. Ein Algorithmus fasst die Gesamtkosten aller Rechnungen des jeweiligen Monats in einer Monatskarte zusammen. Diese wird bei jeder Aktion angepasst und entsprechend aktualisiert. 

![grab-landing-page](https://github.com/KarlEisenkolb/DigitalerEinkaufszettelAndroid/blob/githubUpload/gifs/rechnungadden.gif)
