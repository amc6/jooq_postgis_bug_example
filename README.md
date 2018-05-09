Requirements:
* Java 8
* postgres - `brew install postgresql`
* postgis - `brew install postgis`

To build run `./gradlew build`
The project can also be imported into intellij. And includes two run configs:

* GenerateJooq - Run this to generate jooq code.
* PersisterTest - Run this to run the persister tests.