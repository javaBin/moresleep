# What's this?

Session register which holds the talks for JavaZone.
Contains both a public API that can be consumed by anyone, and a private API used internally to submit talks and review/select talks.

Aiming to replace [Sleepingpill](https://github.com/javaBin/sleepingPillCore)

# Setup

## Database
Moresleep needs a database to run. A docker image is provided in the docker folder ([https://github.com/javaBin/moresleep/tree/master/docker/postgres](https://github.com/javaBin/moresleep/tree/master/docker/postgres)). 

Install docker desktop and then use the build.sh script to build the image and run.sh to run the image. You will then have a postgres installation running on port 5432.

## Running locally
Start the main method in the class no.java.moresleep.java.moresleep.Application. This will start moresleep locally on port 8082. By default you wont 
need to provide a basic auth when running locally.

## Usage
Moresleep is accessed through http calls (GET,POST,PUT and delete) with json payload and will return json as result.

# Public API

You can consume the published events from JavaZone by using the public JSON-api. There are no need for any authentication, just call the API using your favorite HTTP-client.

**API for talks in production:**
To fetch the conferences in the system use the following:
https://sleepingpill.javazone.no/public/allSessions

This will give the following json:
```javascript
 {
 "conferences": [
     {
     "name": "JavaZone 2016",
     "slug": "javazone_2016",
     "id": "3baa25d3-9cca-459a-90d7-9fc349209289"
     },
     {
     "name": "JavaZone 2017",
     "slug": "javazone_2017",
     "id": "30d5c2f1cb214fc8b0649a44fdf3b4bf"
     },
     ....
 ]}
```
To fetch the sessions for each conference add the conference slug to the url, for example:

```
https://sleepingpill.javazone.no/public/allSessions/javazone_2017
```
You can also use id if you want:
```
https://sleepingpill.javazone.no/public/conference/30d5c2f1cb214fc8b0649a44fdf3b4bf/session
```



The API returns a list of the published talks for the year. The following format are an example:

```javascript
{
  "sessions": [
    {
      "sessionId": "", // The ID of the session
      "title": "", // Title of the talk
      "abstract": "", // The full description of the talk
      "intendedAudience": "", // Who the speaker wants to come see their talk
      "language": "", // Language of the talk. Can be one of "no", "en"
      "format": "", // The format of the talk. Can be one of "presentation", "lightning-talk", "workshop"
      "level": "", // Difficulty level of the talk. Can be one of "beginner", "intermediate", "advanced"
      "keywords": ["list", "of", "keywords"], // Keywords classifying the talk
      "speakers": [ // A list of speakers for the talk
        {
          "name": "", // Name of the speaker
          "bio": "", // What the speaker said about him/herself
          "twitter": "", // The speaker's Twitter account
          "pictureUrl": "" // Url to a picture of the speaker
        },
        // ... more speakers here
      ]
    },
    // ... more talks here
  ]
}
```

**Important:** As there are some changes each year concerning which fields are included, the consumers of the API should handle any missing fields gracefully. For example, the field "twitter" on the speaker are new in 2017, and the field "level" is missing in 2016. The fields "title" and "abstract", and the field "name" on the speaker can be assumed to be present.

## What should you call the fields in your GUI?

The following table lists the different fields, and what name we are using to describe this. Feel free to change this if needed (due to short space etc.), but do it intentionally at least ;)

| Field | Name | Field limits | Description given to speaker |
| --- | --- | --- | --- |
| sessionId | _internal field, not for display_ | - |
| title | Title | freetext | Select an expressive and snappy title that captures the content of your talk without being too long. Remember that the title must be attractive and should make people curious. |
| abstract | Description | freetext | Give a concise description of the content and goals of your talk. Try not to exceed 300 words, as shorter and more to-the-point descriptions are more likely to be read by the participants. |
| intendedAudience  | Expected Audience and Code Level | freetext | Who should attend this session? How will the participants benefit from attending? Please indicate how code will factor into your presentation (for example "no code", "code in slides" or "live coding"). |
| language | Language | en / no | Which language will you be holding the talk in? It is permitted to use English in your slides, even though you may be talking in Norwegian, but you should write the rest of the abstract in the language you will speak in. We generally recommend that you hold the talk in the language you are most comfortable with. |
| format | Presentation format | presentation / lightning-talk / workshop | In which format are you presenting your talk? Presentation, lightning talk or workshop? |
| level | Experience level | beginner / intermediate / advanced | Who is your talk pitched at? Beginners, Experts or perhaps those in between? |
| keywords | Keywords | list of strings | Suggest up to five keywords that describe your talk. These will be used by the program committee to group the talks into categories. We reserve the right to edit these suggestions to make them fit into this years selected categories. |
| speaker -> name | Speakers name | freetext | Your name |
| speaker -> bio | Short description of the speaker | freetext | Short description of the speaker (try not to exceed 150 words) |
| speaker -> twitter | Twitter handle | freetext | Twitter handle, starting with an @ |
| speaker -> pictureUrl | Picture url | Url | An url to the picture of the speaker. |


# Private API

Check out the class `no.java.moresleep.HttpMethod` to see all the paths that can be called.

# Populate your local database with data from server (Optional)
If you want some data in your local database to play around with you can import them. To do this create a textfile on the following format
```
SLEEPINGPILL_AUTH=<Ask a friend to get password to sleepingpill>
LOAD_FROM_SLEEPINGPILL=true
```

Run the Application class. Provide a full path to the file above as program argument. If you start with your local database empty it will now be populated with data from the server.