# OnMyWayServer

Fast implementation of Ktor framework to do a server for testing everlasting connections of TYPE CONNECT for mobile applications development

# Ktor Project Setup Guide

This guide will walk you through the necessary steps to set up and run your Ktor server. Follow these steps carefully to avoid common pitfalls.

## 1. Set Up JDK

- **Download a Recent JDK**: Use the Project Structure editor in your IDE to download and set the JDK. This project's setup requires a newer JDK; in my case, JDK 23 was used.
- **Update Build and Run Configuration**: Ensure both build and run configurations in your IDE are set to use this specific JDK version.

### Why a Recent JDK?
Newer JDKs are backward compatible with older Java code, meaning they can run applications built with earlier JDK versions. However, newer projects or libraries compiled with recent JDK versions may not run on older JDK versions. It's crucial to match your project and library requirements with the appropriate JDK to avoid runtime errors.

## 2. Troubleshooting

- **Invalidate Caches and Restart**: If you encounter issues, try invalidating caches and rebuilding the project. Sometimes, restarting your machine can also resolve unexplained issues.

## 3. Version Management

- **Follow Tutorials Cautiously**: When following a tutorial, take note of the version numbers of each plugin or dependency. Save the default versions generated by Ktor somewhere (like a notepad).
- **Experiment with Versions**: Start with the versions shown in the tutorial. Test the setup with those specific versions before gradually updating them. Monitor if any updates cause issues.
- **Researching Version Stability**: To find the latest versions, search the entire package name online. However, newer doesn't always mean better. Check the version history and reviews to choose the most stable release for your needs. Considering past experiences (e.g., with Python), it is advised to select versions that have proven reliability.

## Conclusion

By following these steps, you should be able to set up and run your Ktor server successfully. If you experience issues, refer back to these steps to ensure compliance and compatibility throughout the setup process.

---
# Environment Variables

Add your environment variables, I assume you have an empty mongoDB database, do not worry about crating the collections, the system will create them if they do not exist

```env
ANDROID_CLIENT_ID
DB_PASSWORD
DB_SERVER
DB_USER
WEB_APPLICATION_CLIENT_ID
PORT
```
Once you deploy it in your favorite platform, you do not need to add the PORT variable, in my case I used Heroku and, it assigns its own domain and port.

The code for the Android app that serves as client for this server is located at:

```env
https://github.com/Valentin387/OnMyWayApp
```
## Future Improvement: Remote AlarmManager Restart via FCM

To enhance the reliability of the AlarmManager responsible for periodic activity recognition, developers can implement a fail-safe mechanism using Firebase Cloud Messaging (FCM). This ensures that if the alarm stops running due to app termination, device restarts, or OS restrictions, it can be remotely checked and restarted.

### How It Works

- The server periodically sends an FCM silent push notification to the app (e.g., every hour).

- Upon receiving the notification, the app checks if AlarmManager is still active.

- If the alarm is missing or inactive, the app reschedules it to maintain tracking functionality.

### Implementation Overview

#### Server-Side Requirements

- Implement periodic silent push notifications via FCM.

- The notification payload should include a flag (e.g., { "check_alarm": true }) to trigger the verification logic.

#### Client-Side Implementation

- Implement an FCM service (FirebaseMessagingService) to listen for incoming messages.

- When a silent push notification is received, check the status of AlarmManager.

- If the alarm is not running, restart it to ensure uninterrupted activity recognition.

### Why This is Useful

✅ Ensures persistent tracking despite system constraints.

✅ Bypasses background restrictions imposed by certain Android devices.

✅ Minimal resource usage, as FCM push notifications do not require a persistent background service.

By incorporating this approach, developers can significantly improve the robustness of their tracking system. Future implementations should carefully handle push notification permissions and optimize server-side logic to avoid excessive network requests.

You can also go to the app's code and handle ON START intents to execute some payload if the device is restarted.
