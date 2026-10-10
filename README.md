# Brewkery ☕ – Android Coffee & Bakery Ordering App

A 4-screen ordering app built with Kotlin, Jetpack Compose and MVVM.
**Stack:** Kotlin · Jetpack Compose (Material 3) · MVVM · Retrofit + Gson · Coroutines/StateFlow · Coil · Navigation Compose

## Features
- **Menu:** store banner, category chips, search, item list (thumbnail, badge, rating, price), animated cart bar, active-order card after checkout
- **Item detail:** hero image, ingredients, size / milk / sugar options from the API, live price recalculation
- **Cart:** quantity controls, subtotal + $2.50 delivery + 8% tax, clear cart
- **Order status:** generated ticket ID, `PREPARING` status, cart cleared
- Loading state, error state with Retry, Coil image loading

## Architecture
```
data/    model (JSON classes) · remote (Retrofit) · repository (Menu, Cart)
domain/  CartItem, Order, PriceCalculator (plain Kotlin, unit tested)
ui/      one package per screen: Screen (UI only) + ViewModel (state)
```
- One-way data flow: Repository → ViewModel (`StateFlow`) → Screen. Taps go back up to the ViewModel.
- Menu state is a sealed interface (`Loading`, `Error`, `Success`), so the UI can never show an invalid combination.
- Screens are stateless: they take state and lambdas, which keeps them simple and previewable.
- The cart is an in-memory `StateFlow` in a shared repository, so it stays in sync across screens.
- All money maths is in `PriceCalculator`, with no Android imports, so it is easy to test.

## Run
Open in Android Studio and press Run, or `./gradlew assembleDebug` (APK in `app/build/outputs/apk/debug/`).
Tests: `./gradlew test`

## Tests
`PriceCalculatorTest` covers unit price, the cart totals ($9.40 + $2.50 + $0.75 = $12.65), an empty cart, merging identical cart lines, and clearing the cart when an order is placed.

## 🤖 How I used AI
**Tool:** Claude, throughout the project.

**How I worked with it:** I gave Claude the 4 design screenshots and the data source, then treated its answers as drafts to verify, not as final code. I cross-checked every screen against the design, ran the app after each change, and sent back anything that looked wrong or that I could not explain. Claude helped me with:
- **Colours:** matching the palette from the screenshots into one `BrewColors` object
- **Text layout:** fixing spacing so the text matches the design
- **Animations:** adding motion I could explain
- **Structure:** MVVM with a clear data / domain / ui split

**Prompts I sent (excerpts of the real messages):**
1. "I want to follow best MVVM and practices and can include basic animation, I have given you UI images."
2. "It's not smooth now. I want smooth sliding... where is the animation part, and it's not that beautiful. I shared the images and colors."
3. "I think you make it complex code." (this led to the simpler animation code below)

**What AI got right:**
- The architecture and the tax rule. Tax applies to the subtotal only, so the cart total matches the design exactly. I locked that in with a unit test.
- Driving the customisation options (size, milk, sugar) completely from the API data, so the detail screen needs no hard-coded options.

**What AI got wrong, and how I fixed it:**
| Problem | Fix |
|---|---|
| Several files showed icon errors | The project did not include the Material icons library. Added `material-icons-core` and synced Gradle. |
| Images failed to resolve | Coil was missing. Added `coil-compose:2.7.0` (must match the `coil.compose` import). |
| App installed but crashed on launch | The custom `Application` class was not declared in the manifest. Added `android:name=".BrewkeryApp"`. |
| App felt static and scrolling/navigation felt abrupt | Screen transitions were barely visible. Added longer slide + fade transitions in `NavHost`, and stable keys with `animateItem()` so the list animates smoothly when filtering. |
| Text looked loose compared to the design | Material's default letter spacing and line height were too large. Reset them in the theme and added Plus Jakarta Sans. |
| Animation code was over-engineered | Claude first gave custom `composed` modifiers (press-bounce, staggered entrance). I asked for something simpler and kept only what I can explain: `AnimatedVisibility`, `animateItem`, `animateColorAsState`, and `NavHost` transitions. |

## Modern API checks
I checked the code for deprecated APIs and used the current ones:
| Deprecated / discouraged | Used instead |
|---|---|
| `Icons.Default.ArrowBack` | `Icons.AutoMirrored.Filled.ArrowBack` (flips correctly for right-to-left languages) |
| `Divider` | `HorizontalDivider` |
| `Modifier.animateItemPlacement()` | `Modifier.animateItem()` |
| `collectAsState()` | `collectAsStateWithLifecycle()` (stops collecting when the app is in the background) |
| `Modifier.composed` | Removed; plain modifiers and `AnimatedVisibility` instead |
| `try/catch` inside ViewModels | Repository returns `Result`, and coroutine cancellation is re-thrown |

## Next steps
- **Payments:** integrate a payment SDK (e.g. Razorpay or Stripe) on the checkout button.
- **AI features:** personalised item suggestions and natural-language search ("something cold and not too sweet").
- **Hilt** for dependency injection, **Room** to save the cart, offline caching of the menu, and Compose UI tests.