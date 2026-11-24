# EvalúaApp (Android)

Aplicación Android nativa sencilla para registrar evaluaciones, consultar una lista ordenada por fecha, marcar pruebas realizadas y recibir un recordatorio local 24 horas antes. Los datos se guardan en el teléfono mediante SharedPreferences, por lo que consultar las evaluaciones no requiere internet.

## Requisitos
- Android Studio (versión reciente)
- JDK 17
- Android SDK 35 (Android Studio puede instalarlo desde SDK Manager)

## Abrir y ejecutar
1. Descomprime `EvaluaApp.zip`.
2. Abre Android Studio y selecciona **Open**.
3. Selecciona la carpeta `EvaluaApp` (la que contiene `settings.gradle`).
4. Espera a que termine la sincronización de Gradle.
5. Conecta un teléfono Android con depuración USB activada o crea un emulador.
6. Pulsa **Run**.
7. Acepta el permiso de notificaciones cuando la aplicación lo solicite.

## Crear un APK
En Android Studio: **Build > Build Bundle(s) / APK(s) > Build APK(s)**. El APK de prueba suele quedar en `app/build/outputs/apk/debug/app-debug.apk`.

## Uso
- Pulsa **Agregar evaluación**.
- Escribe la asignatura y, opcionalmente, los contenidos.
- Elige la fecha y guarda.
- La aplicación programa un aviso local para 24 horas antes (si la evaluación se registra con más de 24 horas de anticipación).
- Usa los filtros para ver todas, pendientes o realizadas.
- Marca una evaluación como realizada o elimínala.

## Notas y límites de esta primera versión
- No requiere internet para consultar y guardar las evaluaciones.
- Los datos quedan guardados solo en ese teléfono; todavía no hay sincronización entre estudiantes o profesores.
- El recordatorio se programa 24 horas antes. Android puede retrasar avisos por ahorro de batería; el permiso de notificaciones debe estar habilitado.
- Si el teléfono se reinicia, abre la aplicación para volver a programar los recordatorios.
- Esta entrega contiene el proyecto fuente Android Studio; no incluye un APK ya compilado.
