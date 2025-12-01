# Firebase Security Rules Setup Guide

## Issue: PERMISSION_DENIED Error on Account Creation

### Problem
When users try to create an account, they get a `PERMISSION_DENIED` error. However, sign-in works fine. This is because:

1. **Account Creation Process:**
   - Firebase Auth creates the user ✅ (Works)
   - App tries to save UserProfile to Firestore ❌ (PERMISSION_DENIED)

2. **Sign-In Process:**
   - Firebase Auth authenticates user ✅ (Works)
   - App only reads existing profile data ✅ (Works with current rules)

3. **Root Cause:**
   - Firestore security rules don't allow newly authenticated users to create documents in the `users` collection
   - Rules need to explicitly allow users to write their own profile

## Solution: Update Firestore Security Rules

### Current Rules (Problematic)
The current rules likely have one of these issues:
- No rules configured (defaults to deny all writes)
- Rules only allow reads, not writes
- Rules don't recognize newly authenticated users

### Recommended Rules (Fix)

Go to **Firebase Console** → **Firestore Database** → **Rules** tab and replace with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users collection - Allow users to read/write their own profile
    match /users/{userId} {
      // Allow user to read their own profile
      allow read: if request.auth.uid == userId;

      // Allow user to create/update their own profile
      allow create, update: if request.auth.uid == userId;

      // Deny delete (users can't delete their profile)
      allow delete: if false;
    }

    // Events collection - Allow everyone to read, authenticated users to create
    match /events/{eventId} {
      // Allow anyone to read events
      allow read: if true;

      // Allow authenticated users to create events
      allow create: if request.auth.uid != null;

      // Allow user to update/delete their own events
      allow update, delete: if request.auth.uid == resource.data.createdBy;
    }

    // Chat messages - Allow authenticated users to read/write
    match /events/{eventId}/chat/{messageId} {
      // Allow authenticated users to read messages
      allow read: if request.auth.uid != null;

      // Allow authenticated users to create messages
      allow create: if request.auth.uid != null && request.resource.data.senderId == request.auth.uid;

      // Deny updates and deletes
      allow update, delete: if false;
    }
  }
}
```

### Key Rules Explained

#### Users Collection
```javascript
match /users/{userId} {
  allow read: if request.auth.uid == userId;
  allow create, update: if request.auth.uid == userId;
}
```
- ✅ User can read their own profile (`uid` must match)
- ✅ User can create their profile on signup
- ✅ User can update their profile on edit
- ❌ User cannot read other users' profiles (privacy)
- ❌ User cannot delete their profile

#### Events Collection
```javascript
match /events/{eventId} {
  allow read: if true;
  allow create: if request.auth.uid != null;
  allow update, delete: if request.auth.uid == resource.data.createdBy;
}
```
- ✅ Anyone can read events (no login required to browse)
- ✅ Authenticated users can create events
- ✅ Only event creator can edit/delete their event

#### Chat Messages
```javascript
match /events/{eventId}/chat/{messageId} {
  allow read: if request.auth.uid != null;
  allow create: if request.auth.uid == request.resource.data.senderId;
}
```
- ✅ Authenticated users can read chat messages
- ✅ Users can only send messages as themselves
- ❌ Messages cannot be edited or deleted (immutable)

## Implementation Steps

### Step 1: Go to Firebase Console
1. Visit https://console.firebase.google.com/
2. Select your project
3. Go to **Firestore Database**

### Step 2: Open Security Rules
1. Click the **Rules** tab at the top
2. Click **Edit Rules**

### Step 3: Replace Rules
1. Select all current rules (Ctrl+A)
2. Delete them
3. Paste the recommended rules above
4. Click **Publish**

### Step 4: Test
1. Try creating a new account in the app
2. Verify no PERMISSION_DENIED error
3. Verify profile is saved to Firestore
4. Try signing in with the new account
5. Verify profile loads on Profile screen

## Testing Checklist

After applying rules:

- [ ] Create new account with signup form
  - [ ] No PERMISSION_DENIED error
  - [ ] Redirect to home/events screen
  - [ ] Profile visible in Firestore console

- [ ] Sign out and sign back in
  - [ ] Sign in works with new account
  - [ ] Profile loads on Profile screen
  - [ ] All user data is populated

- [ ] Edit profile
  - [ ] Changes save without error
  - [ ] Changes persist after sign out/in

- [ ] Create event
  - [ ] Event appears in Events tab
  - [ ] Event shows in Firestore
  - [ ] Only creator can edit/delete

- [ ] Join event
  - [ ] User added to participants list
  - [ ] Event appears in My Events tab

## Security Considerations

### What These Rules Protect:
- ✅ Users can only read/write their own profile
- ✅ Users cannot delete their profile
- ✅ Only event creators can modify their events
- ✅ Chat messages are immutable (can't be edited/deleted)
- ✅ Messages tied to sender identity

### What's Open:
- ⚠️ Anyone can read event listings (intentional - discovery feature)
- ⚠️ Authenticated users can create events (intentional - main feature)

### Future Enhancements:
1. **Rate limiting** - Prevent spam (requires Firestore extensions)
2. **Email verification** - Require verified UTA email
3. **Admin approval** - Moderate events before showing
4. **Reporting system** - Users can report inappropriate events/profiles
5. **Blocking users** - Users can block others from joining their events

## Troubleshooting

### Still Getting PERMISSION_DENIED?
1. **Check published status** - Make sure you clicked "Publish"
2. **Wait for propagation** - Changes can take up to 1 minute
3. **Check project** - Make sure rules are in correct Firebase project
4. **Check auth method** - User must be signed in (not just created account)
5. **Check user.uid** - Verify `request.auth.uid` is being passed correctly

### Check Firestore Console:
1. Go to Firestore → Data tab
2. Look for `users` collection
3. Check if document with user.uid exists
4. Verify profile data is complete

### Check Firebase Auth Console:
1. Go to Authentication → Users
2. Verify new user was created
3. Check email and UID match

## Firebase Console Access

**Need to update rules?**
1. Firebase Project: Campus Sports App
2. URL: `https://console.firebase.google.com/`
3. Project ID: (check google-services.json or Firebase config)
4. Navigate to: Firestore Database → Rules tab

## Notes

- Rules are case-sensitive
- `request.auth.uid` is the authenticated user's ID
- `resource.data.createdBy` assumes event has a `createdBy` field
- Rules are evaluated on both client and server (apply on server)
- Test rules in Firebase console before relying on them
