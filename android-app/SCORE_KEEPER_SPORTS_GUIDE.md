# Score Keeper - Sport-Specific Scoring Guide

## Overview

The Score Keeper tool now features sport-specific scoring configurations. When users select a sport, the scoring interface adapts to show sport-appropriate buttons and point values.

## How It Works

### Setup Screen
1. **Enter Team Names**: Users enter both team names
2. **Select Sport from Dropdown**: Choose from 8 supported sports
3. **View Sport Guide**: A helpful card shows how to track that specific sport
4. **Start Scoring**: Begin the game

### Scoreboard Interface
- **Sport-Specific Buttons**: Each sport has custom point value buttons
- **Undo Button**: Gray button appears when score > 0, lets users undo last action
- **Live Score Display**: Large 80sp numbers showing current score
- **Sport Title**: Shows selected sport at top of scoreboard

## Supported Sports & Scoring

### Basketball
- **Free Throw**: +1 point
- **2-Pointer**: +2 points
- **3-Pointer**: +3 points
- **Description**: Track free throws, 2-pointers, and 3-pointers
- **Example**: User can press "+2 Points" to add 2 for a regular basket

### Soccer
- **Goal**: +1 point
- **Description**: Track goals
- **Note**: Simple goal counter - each goal is 1 point

### Volleyball
- **Point**: +1 point
- **Description**: Track points (first to 25)
- **Note**: Typical match format, track each point scored

### Tennis
- **Game Won**: +1 point (represents 1 game won, not individual points)
- **Description**: Track games won
- **Note**: Track games in the set, not individual rally points

### Badminton
- **Point**: +1 point
- **Description**: Track points (first to 21)
- **Note**: Modern badminton scoring, first to 21 wins

### Ping Pong (Table Tennis)
- **Point**: +1 point
- **Description**: Track points (first to 11)
- **Note**: Standard rally scoring, first to 11 wins

### Running
- **Lap**: +1 point (represents 1 lap)
- **Description**: Track laps completed
- **Note**: Count laps around a track or course

### Swimming
- **Lap**: +1 point (represents 1 lap)
- **Description**: Track laps completed
- **Note**: Count completed laps in pool

## UI Components

### Team Score Card
- **Color Coded**: Team 1 = Green (#4CAF50), Team 2 = Blue (#2196F3)
- **Bordered Card**: 2dp border with 16.dp radius
- **Large Score Display**: 80sp font for easy visibility
- **Custom Buttons**: Sport-appropriate point buttons

### Scoring Buttons

#### Undo Button
- **Appearance**: Gray button (#757575)
- **Height**: 40dp
- **Shows When**: Score > 0
- **Function**: Decreases score by 1 (reverses last action)

#### Point Buttons
- **Height**: 50dp
- **Border Radius**: 12dp
- **Number of Buttons**: Depends on sport (1-3 buttons)
- **Labels**: Descriptive text (e.g., "+2 Points", "+1 Goal")
- **Color**: Team 1 = Green, Team 2 = Blue
- **Action**: Adds configured points to score

### Reset Button
- **Full Width**: Spans entire bottom
- **Height**: 50dp
- **Style**: Outlined button
- **Function**: Resets both teams to 0

## Technical Implementation

### Sport Configuration Data Class
```kotlin
data class SportConfig(
    val name: String,
    val points: IntArray,           // Array of point values
    val pointLabels: List<String>,  // Button labels
    val description: String         // Guidance text
)
```

### SPORTS_CONFIG Map
- Centralized configuration for all sports
- Easy to add new sports
- Each sport has its own point structure
- Descriptions help users understand scoring

## Future Enhancements

1. **Point Limit Alerts**: Notify when team reaches winning score
2. **Game History**: Save game scores and statistics
3. **Custom Sports**: Allow users to define custom sports with custom point values
4. **Statistics**: Track win/loss records by sport
5. **Sound Effects**: Audio cues for points scored
6. **Voice Control**: "Add two points" voice commands

## User Experience Improvements

### Before (Generic Score Keeper)
- Single +/- buttons for all sports
- No guidance on sport-specific scoring
- Basketball and soccer scored identically
- Same UI for all sports

### After (Sport-Specific Score Keeper)
- ✅ Dropdown menu for sport selection
- ✅ Sport-specific buttons (Basketball: +1, +2, +3)
- ✅ Helpful descriptions for each sport
- ✅ Undo button for mistake recovery
- ✅ Color-coded teams for easy distinction
- ✅ Sport title displayed during scoring
- ✅ Intuitive button labels matching sport terminology

## Testing Checklist

- [ ] Dropdown shows all 8 sports
- [ ] Selecting sport updates description
- [ ] Basketball shows 3 buttons (+1, +2, +3)
- [ ] Soccer shows 1 button (+1 Goal)
- [ ] Undo button appears when score > 0
- [ ] Undo button hidden when score = 0
- [ ] Reset button clears both scores
- [ ] Team 1 buttons are green
- [ ] Team 2 buttons are blue
- [ ] Back button returns to setup screen
- [ ] Team names are retained on back (TODO: add state persistence)
- [ ] Large score display is readable
- [ ] All button labels are correct for each sport

## Files Modified

- `ScoreKeeperScreen.kt`: Complete redesign with sport-specific features

## Related Documentation

- See PRODUCTION_FINALIZATION_PLAN.md for overall app status
- See button styling standards for UI consistency guidelines
