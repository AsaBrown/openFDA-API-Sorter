# openFDA-API-Sorter
A program written in Java to connect to the openFDA API and sort the provided data. The user selects which data is to be output and what the sorting criteria should be. 

The API provides the same functionality and more as this program does, but it limits your queries to a 100 data fields (actually JSON objects) at a time. With each file holding thousands of these fields, this program seeks to provide a more accomodating approach to sorting the large amounts of data within. 
Information on the openFDA API can be found at: https://open.fda.gov/apis/. 

At the moment, this program allows the user to do three things:
* Select which API field they want to pull the data from.
* Select which key/value pairs they want to be output from each JsonObject within that API field
* Select the sorting criteria among those key/value pairs. 

It accomplishes this through several functions. A few key points are listed here:
* Since we cannot interact with the API directly, we do so in a roundabout way in that we download a JSON file from openFDA that contains links to download every file under evey API field. This 'master' file is downloaded again every time the program is run, as openFDA updates it frequently. 
* Some of the key/value pairs within the JSON objects in each file are more nested JsonObjects to be traversed. When sorting and processing the data, it is important to distinguish whether or not these fields are JsonObjects or JsonValues. In order to do so, the program pulls from a sample JSON object, within the references/dataTypes.json file. This sample object contains a copy of every possible key within a JsonObject from that specific API field. If the value corresponding to that key is another JsonObject, the name of this sample key is "KeyJsonObject/JsonValue". Note that the '/' delimiter is used to denote that the key/value is another JsonObject that must be traversed to find the appropriate value. If the user wants to make the program support more API fields, a new sample JsonObject must be added for every API field. 


