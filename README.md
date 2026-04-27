# Space Fight

A 2D space shooter game built with Java and JavaFX.

## Gameplay

- Pilot your ship through waves of enemy fighters and asteroids
- Auto-fire is always on — focus on dodging and positioning
- Enemies speed up as your level increases
- 3 lives; brief invincibility after each hit

## Controls

| Key | Action |
|-----|--------|
| `W` / `↑` | Move up |
| `S` / `↓` | Move down |
| `A` / `←` | Move left |
| `D` / `→` | Move right |
| `Enter` | Start / Restart |

## Scoring

| Event | Points |
|-------|--------|
| Destroy enemy | 100 × level |
| Destroy asteroid | 50 × level |

Level increases every 1000 points. Enemy spawn rate increases with each level.

## Requirements

- Java 21
- JavaFX 21

## Build & Run

```bash
./gradlew run
```

To build a distributable image:

```bash
./gradlew jlink
```
