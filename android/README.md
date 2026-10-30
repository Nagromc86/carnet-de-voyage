# Appli Android (APK)

L'APK est construite automatiquement par GitHub Actions (`.github/workflows/apk.yml`) à chaque modification de `index.html`,
puis publiée dans les Releases du dépôt.

Dernière version : https://github.com/Nagromc86/carnet-de-voyage/releases/latest/download/carnet-de-voyage.apk

- `capacitor.config.json` et `package.json` : emballage Capacitor de la page `index.html`.
- `assets/` : icône et écran de démarrage.
- Signature : la clé est rangée dans le coffre secret du dépôt (secrets `APK_KEYSTORE`, en base64, et `APK_KEYSTORE_PASSWORD`).
  Elle doit rester la même pour que les mises à jour s'installent par-dessus l'ancienne version sans perdre les données.
  L'ancienne clé (`carnet.jks`, utilisée pour la version 1.2) est abandonnée.
