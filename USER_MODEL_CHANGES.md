# User Model Enhancement Summary

## Changes Made

### 1. **New Enums Created**

#### `SignupMethod.java`

- Location: `auth/enums/SignupMethod.java`
- Values:
  - `DEFAULT` - Normal email/password signup
  - `GOOGLE` - Google OAuth signup
  - `FACEBOOK` - Facebook OAuth (for future use)
  - `GITHUB` - GitHub OAuth (for future use)

#### `VerificationStatus.java`

- Location: `auth/enums/VerificationStatus.java`
- Values:
  - `UNVERIFIED` - User has not been verified yet
  - `PENDING` - Verification is in progress
  - `VERIFIED` - User is verified
  - `REJECTED` - Verification was rejected

#### `ProfileVisibility.java`

- Location: `auth/enums/ProfileVisibility.java`
- Values:
  - `PRIVATE` - Profile is only visible to the user
  - `PUBLIC` - Profile is visible to everyone

### 2. **User Model Updates**

Added the following fields to the `User` model:

```java
// Individual user fields
private String license;  // Professional license number or certification

@Column(length = 1000)
private String skills;  // Comma-separated list of skills

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private ProfileVisibility profileVisibility = ProfileVisibility.PUBLIC;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private SignupMethod signupMethod = SignupMethod.DEFAULT;
```

All fields include getters and setters.

### 3. **SignUpService Updates**

- Added import for `SignupMethod`
- Updated `createUser()` method to set `signupMethod` to `SignupMethod.DEFAULT` for normal signup flow
- This allows tracking how users registered (normal vs OAuth)

### 4. **DTOs Updated**

#### `UserProfileResponse.java`

Added new fields to the response:

- `String license`
- `String skills`
- `VerificationStatus verificationStatus`
- `ProfileVisibility profileVisibility`
- `SignupMethod signupMethod`

#### `UpdateUserProfileRequest.java`

Added new fields with validation:

- `@Size(max = 100) String license`
- `@Size(max = 1000) String skills`
- `VerificationStatus verificationStatus`
- `ProfileVisibility profileVisibility`

### 5. **UserService Updates**

#### `getMyProfile()` method

Updated to include new fields in the response:

```java
user.getLicense(),
user.getSkills(),
user.getVerificationStatus(),
user.getProfileVisibility(),
user.getSignupMethod()
```

#### `updateMyProfile()` method

Added update logic for new fields:

- License
- Skills
- Verification Status
- Profile Visibility

## Default Values

When a user is created via normal signup:

- `signupMethod`: `DEFAULT`
- `verificationStatus`: `UNVERIFIED`
- `profileVisibility`: `PUBLIC`
- `license`: `null`
- `skills`: `null`

## Next Steps for Google OAuth Integration

When implementing Google OAuth signup (future service), you should:

1. Set `user.setSignupMethod(SignupMethod.GOOGLE)`
2. Consider auto-verifying Google users: `user.setVerificationStatus(VerificationStatus.VERIFIED)`
3. Possibly skip email verification for OAuth users

## Database Migration

**Important:** After these changes, you need to run database migrations or let JPA auto-update the schema to add the new columns to the `users` table.

If using Flyway/Liquibase, create a migration script. If using JPA auto-update, the schema will be updated on next application start.
