# UserProfile API Usage

This document details all the APIs provided by UserProfile, along with sample code snippets on how to properly use the APIs.

## extensionVersion:

The `extensionVersion()` API returns the version of the Profile extension.

### Syntax

```Java
public static String extensionVersion()
```

### Example

```Java
String extensionVersion = UserProfile.extensionVersion();
```

## getUserAttributes:

The `getUserAttributes()` API gets the user profile attributes with the given keys.

### Syntax

```Java
public static void getUserAttributes(List<String> keys, AdobeCallback<Map<String, Object>> callback)
```

- `callback` is invoked after the customer attributes are available.

### Example

A retail application wants to get the `itemsAddedToCart` user data when processing checkout.
When `AdobeCallbackWithError` is provided, if the operation times out (5s) or an unexpected error occurs, the fail method is called with the appropriate `AdobeError`.

```Java
UserProfile.getUserAttributes(Arrays.asList("itemsAddedToCart"), new AdobeCallbackWithError<Map<String, Object>>() {
    @Override
    public void fail(AdobeError adobeError) {
         // your customized code
    }
    @Override
    public void call(Map<String, Object> stringObjectMap) {
        // your customized code
    }
});
```

## removeUserAttributes

Removes the user profile attributes for the given keys.

### Syntax

```Java
public static void removeUserAttributes(List<String> attributeNames);

```

### Example

You want to remove `username`, `usertype` user data when session timeout occurs.

```Java
UserProfile.removeUserAttributes(Arrays.asList("username", "usertype"));
```

## updateUserAttributes

Sets the user profile attributes key and value.
Allows you to create/update a batch of user profile attributes:

- String, Integer, Boolean, Double, Array, Map are valid type of user profile attributes.
- Custom objects cannot be saved as a UserProfile attribute.
- If the attribute does not exist, it is created.
- If the attribute already exists, the value is updated.
- A null attribute value will remove the attribute.

### Syntax

```Java
public static void updateUserAttributes(Map<String, Object> attributeMap)
```

### Example

You want to update `username`, `usertype` of a user obtained in the log in page :

```Java
HashMap<String, Object> profileMap = new HashMap<>();
profileMap.put("username","Will Smith");
profileMap.put("usertype","Actor");
UserProfile.updateUserAttributes(profileMap);

```
