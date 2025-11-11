# ParkWise-App

## Overview

**ParkWise-App** is an innovative, professional mobile application designed to streamline parking for urban drivers. Utilizing a modern Android tech stack (Kotlin, Jetpack Compose), integrated with payment solutions and real-time updates, ParkWise makes finding, reserving, and managing parking seamless and secure.

## Features

- **Nearby Parking Discovery**: Search for nearby parking spaces based on user’s current location.
- **Smart Booking**: Reserve parking slots in advance and get predictive price estimates for your stay.
- **Multiple Payment Options**: Secure and flexible payments via Credit Card, PayPal, Apple Pay, Google Pay, and Razorpay support.
- **Booking History**: View historical parking and payment records in a centralized profile.
- **Authentication**: Secure login and sign-up using Email/Password and Google Sign-In (Firebase Auth).
- **User Profile Management**: Edit profile and manage your vehicle(s).
- **3D Parking Visualizations**: Planned support for 3D viewing of parking areas and slots.
- **Real-time Updates**: WebSocket-powered, immediate sync of parking slot availability and status.
- **Car Image Generation**: Integration with Gemini AI for generating car images based on user or app input (for future/AI-based features).
- **Logout and Session Management**: Secure session handling with easy logout and confirmation dialogs.

## ScreenShots

<img width="427" height="914" alt="Screenshot 2025-11-11 at 2 16 32 PM" src="https://github.com/user-attachments/assets/50835e13-65a8-444f-bcf7-dd931348e22e" />
<img width="427" height="914" alt="Screenshot 2025-11-11 at 2 15 54 PM" src="https://github.com/user-attachments/assets/91f3db5a-6eb6-4e0b-85c7-6cc994b5b229" />
<img width="427" height="914" alt="Screenshot 2025-11-11 at 2 13 31 PM" src="https://github.com/user-attachments/assets/b588af74-6128-41f6-95f7-a794be3cfaaf" />
<img width="427" height="914" alt="Screenshot 2025-11-11 at 2 17 35 PM" src="https://github.com/user-attachments/assets/b7c7d47f-45c5-4dda-8fc8-d7092bd2a979" />
<img width="427" height="914" alt="Screenshot 2025-11-11 at 2 16 41 PM" src="https://github.com/user-attachments/assets/bc7d3b97-f0f9-41d5-b010-978a7069b71c" />
<img width="427" height="914" alt="Screenshot 2025-11-11 at 2 16 50 PM" src="https://github.com/user-attachments/assets/dbce4b56-c8d1-41f4-998f-65360c605df6" />
<img width="427" height="914" alt="Screenshot 2025-11-11 at 2 16 59 PM" src="https://github.com/user-attachments/assets/4cc9cd6c-3c26-4c46-a4cc-dee7b18e9f36" />
<img width="427" height="914" alt="Screenshot 2025-11-11 at 2 17 23 PM" src="https://github.com/user-attachments/assets/8295501a-4d73-4b47-af74-587f19e8d192" />

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose Material 3
- **Architecture**: MVVM (ViewModels, LiveData/StateFlow)
- **Networking**: Retrofit, WebSocket (OkHttp)
- **Authentication**: Firebase Auth (Email/Password, Google Sign-In)
- **Payments**: Razorpay, Stripe (Structure for multiple providers)
- **AI Integration**: Google Gemini API (for car image generation)
- **Other**: Gson, OkHttp, AndroidX ecosystem

## Key Modules

- `ui/screens/` — All major composable screens (Home, Authentication, Profile, Payment, History)
- `viewmodel/` — MVVM ViewModels handling UI logic and state
- `data/` — Repositories handling all data sources (network, local)
- `network/` — API and WebSocket communication logic
- `util/` — Utility classes, e.g., image generation, payment flows
- `components/` — Reusable UI and 3D visualization components (planned/experimental)

## Getting Started

### Prerequisites

- Android Studio (latest preferred)
- Minimum SDK: 21+
- Firebase project for authentication (provide your Web Client ID for Google sign-in)
- Set the Gemini API Key for car image generation if using AI features

### Installation

1. Clone the repo:
   ```bash
   git clone https://github.com/Aryan-Baglane/ParkWise-App.git
   cd ParkWise-App
   ```
2. Open in Android Studio.

3. Add your Firebase `google-services.json` and configure payment API keys as needed.

4. Build and run the app on your emulator or device.

### Configuration

- For Google Sign-In (Firebase):
  - Set your Web Client ID in `ActivityResultContract.kt` (see code comment for location).
- For the Gemini AI feature:
  - Insert your Gemini API key in `util/CarImageGeneration.kt`.

### Running

Use Android Studio’s standard run/debug flow. The app launches directly into authentication or the Home Screen depending on session state.

## Contributions

Contributions are welcome! Please open issues or submit pull requests for enhancements or bug fixes.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

## Contact

For questions, issues, or feature requests, open a GitHub issue or contact [Aryan Baglane](https://github.com/Aryan-Baglane).

---
*Empowering Drivers. Easing Parking. The Smart Way.*
