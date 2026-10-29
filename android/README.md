# Appli Android (APK)

L'APK est construite automatiquement par GitHub Actions (`.github/workflows/apk.yml`) à chaque modification de `index.html`,
puis publiée dans les Releases du dépôt.

Dernière version : https://github.com/Nagromc86/carnet-de-voyage/releases/latest/download/carnet-de-voyage.apk

- `capacitor.config.json` et `package.json` : emballage Capacitor de la page `index.html`.
- `assets/` : icône et écran de démarrage.
- `carnet.jks` : clé de signature de l'appli. Elle doit rester la même pour que les mises à jour s'installent par-dessus
  l'ancienne version sans perdre les données. Elle est dans le dépôt (public) par simplicité : n'installe une APK de
  « Carnet de voyage » que depuis les Releases de ce dépôt.
