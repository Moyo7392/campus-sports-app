# Profile Auto-Population Implementation Guide

## Overview
This document outlines the plan to auto-populate the Profile screen with data from the signup process, reducing friction for users and improving the onboarding experience.

## Current State
- ✅ Profile screen exists with editable fields
- ✅ Profile setup guidance message is displayed
- ✅ Users can manually edit all profile information
- ✅ Profile data is stored in Firestore UserProfile collection

## Fields to Auto-Populate from Signup

### From `ComprehensiveSignUpScreen.kt`:

| Signup Field | Profile Field | Status |
|---|---|---|
| Full Name | fullName | Ready to implement |
| Major | major | Ready to implement |
| Year | year | Ready to implement |
| Favorite Sports | favoritesSports | Ready to implement |
| Skill Level | skillLevel | Ready to implement |

### Fields NOT in signup (to implement later):
- Bio (leave empty initially)
- Profile Image URL (default avatar)

## Implementation Steps

### 1. Modify Signup Flow
**File**: `ComprehensiveSignUpScreen.kt`

Currently, after sign-up, the user's profile data needs to be captured and stored:

```kotlin
// After user completes sign-up form with:
// - fullName
// - email
// - major
// - year
// - favoritesSports (List<String>)
// - skillLevel

// Call authViewModel to create UserProfile with this data
authViewModel.createOrUpdateUserProfile(
    fullName = fullName,
    major = major,
    year = year,
    favoritesSports = favoritesSports,
    skillLevel = skillLevel,
    email = email
)
```

### 2. Update FirebaseAuthViewModel
**File**: `FirebaseAuthViewModel.kt`

Add or enhance `createOrUpdateUserProfile()` function to:
1. Create a UserProfile object with signup data
2. Save it to Firestore under `users/{uid}` collection
3. Trigger reload of userProfile StateFlow

```kotlin
suspend fun createOrUpdateUserProfile(
    fullName: String,
    major: String,
    year: String,
    favoritesSports: List<String>,
    skillLevel: String,
    email: String
) {
    // Create UserProfile with signup data
    val profile = UserProfile(
        uid = currentUser.value?.uid ?: return,
        fullName = fullName,
        email = email,
        major = major,
        year = year,
        favoritesSports = favoritesSports,
        skillLevel = skillLevel,
        bio = "", // Empty initially
        createdAt = Timestamp.now()
    )

    // Save to Firestore
    firestore.collection("users")
        .document(profile.uid)
        .set(profile)
        .addOnSuccess { /* Update StateFlow */ }
}
```

### 3. Update ProfileScreen (Already Done!)
**File**: `MainActivity.kt` - `ProfileScreen()` function

The ProfileScreen already:
- ✅ Listens to userProfile changes via authViewModel
- ✅ Auto-populates UI fields from userProfile data
- ✅ Displays "Complete Your Profile" guidance message

**No changes needed here!**

## Testing Checklist

- [ ] User completes signup with all fields
- [ ] User is redirected to home/events after signup
- [ ] User navigates to Profile tab
- [ ] All fields are pre-filled with signup data:
  - [ ] Full Name
  - [ ] Major & Year
  - [ ] Preferred Sports
  - [ ] Skill Level
- [ ] User can still edit these fields
- [ ] Changes are saved to Firestore
- [ ] Logging out and logging back in preserves auto-filled data

## Files to Modify

1. `auth/ComprehensiveSignUpScreen.kt`
   - Capture signup data
   - Call profile creation on successful signup

2. `auth/FirebaseAuthViewModel.kt`
   - Add `createOrUpdateUserProfile()` function
   - Save to Firestore users collection
   - Reload userProfile StateFlow after save

3. `MainActivity.kt` - ProfileScreen
   - ✅ Already supports auto-population (no changes needed)

## Timeline Notes

- **Estimated Effort**: 1-2 hours of development
- **Complexity**: Medium (Firebase integration already exists)
- **Priority**: Medium (nice-to-have, improves UX)
- **Dependencies**: None (all infrastructure is ready)

## Future Enhancements

1. **Profile Image Upload**
   - Allow users to upload/change profile picture
   - Store in Firebase Storage
   - Save URL to profileImageUrl field

2. **Bio Auto-Suggestion**
   - Generate initial bio from signup info
   - Let users edit and refine

3. **Skill Level Validation**
   - Show sport-specific skill levels
   - Different expectations for different sports

4. **Social Verification**
   - Verify email address
   - Confirm phone number
   - Add trust badges

## Notes

- The UserProfile data model already supports all signup fields
- The Firestore integration is already in place
- The ProfileScreen is already listening to profile changes
- This is primarily a **data flow** implementation, not UI redesign
- No database schema changes needed
