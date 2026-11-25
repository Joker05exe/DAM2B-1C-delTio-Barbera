# Contactes — Pràctica 1C (DAM2B 0489)

**Autor:** Joan del Tio Barberà  
**Mòdul:** 0489 — Programació Multimèdia i Dispositius Mòbils (DAM2B)

## Descripció de l'aplicació

**Contactes** és una aplicació Android en Kotlin que mostra una agenda de contactes carregada des d'Internet (`jsonplaceholder.typicode.com/users`) en un fil de segon pla. Si no hi ha connexió a la xarxa, carrega automàticament la còpia local (`assets/contactes.json`) i mostra un avís a l'usuari.

Funcionalitats implementades:
- Llista de contactes amb `RecyclerView` (`ContacteAdapter`), monograma d'inicials, nom, ciutat, correu i casella de favorit.
- Filtratge en temps real per nom, ciutat o correu electrònic.
- Pantalla de detall (`ContacteDetallActivity`) amb dades compartides mitjançant el singleton `ContactesRepository` i pas d'`EXTRA_ID` per `Intent`.
- Descàrrega d'avatar en segon pla i selecció d'imatge pròpia des de la galeria (`ACTION_OPEN_DOCUMENT`).
- Sis accions amb `Intent` implícits: trucar (`ACTION_DIAL`), enviar correu (`ACTION_SENDTO`), obrir mapa (`geo:`), obrir web (`https:`), posar alarma a les 9:00 (`ACTION_SET_ALARM`) i triar foto de la galeria.
- Notificació en carregar els contactes amb gestió del permís `POST_NOTIFICATIONS` en temps d'execució (Android 13+).
- Suport per a català, castellà, anglès, tema clar, tema fosc i *edge-to-edge*.

## Com executar-la

1. Obre **Android Studio** → `File` → `Open` → selecciona la carpeta del projecte (`DAM2B-1C-delTio-Barbera/`).
2. Espera que acabi el **Gradle Sync**.
3. Selecciona un emulador o dispositiu físic (API 24+) i prem **Run (▶)**.
4. Per compilar des de la terminal:
   ```bash
   ./gradlew assembleDebug
   ```

## Proves i validació

- Compilació amb `./gradlew assembleDebug` verificada sense errors.
- *(Pendent d'afegir captures i resultats de les proves en emulador/dispositiu a `informe/informe-prac1c.pdf`).*
