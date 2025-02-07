# Installation Guide
## Product DFT
Install from the command line:

docker container run -d --name [conatainer_name] ghcr.io/catenax-ng/tx-dft-backend:[tag]


It is necessary to inject the environment variables, credentials and URLs that can be found on application.properties file.
#### CatenaX variables
| Property       | Value          | Description        | Example |
|----------------|----------------|--------------------|---------|
| manufacturerId | MANUFACTURERID | Id of manufacturer | CatenaX |


#### Digital Twins variables:
| Property name                             | Environment Variable Name                 | Description                                    | Example Value                    |
|-------------------------------------------|-------------------------------------------|------------------------------------------------|----------------------------------|
| digital-twins.hostname                    | DIGITAL-TWINS_HOSTNAME                    | hostname for Digital Twins                     | https://                         |
| digital-twins.authentication.url          | DIGITAL-TWINS_AUTHENTICATION_URL          | authentication url for Digital Twins           | https://                         |
| digital-twins.authentication.clientId     | DIGITAL-TWINS_AUTHENTICATION_CLIENTID     | client ID authentication for Digital Twins     | sa-cl6-cx-4                      |
| digital-twins.authentication.clientSecret | DIGITAL-TWINS_AUTHENTICATION_CLIENTSECRET | client secret authentication for Digital Twins | VrL8uSG5Tn3NrFiY39vs0klTmlvsRRmo |

The values are on the [Vault].
*<i><b>Must create a GitHub token to access</b></i>

#### EDC variables:
| Property name    | Environment Variable Name | Description                                   | Example Value |
|------------------|---------------------------|-----------------------------------------------|---------------|
| edc.hostname     | EDC_HOSTNAME              | edc hostname                                  | https://      |
| edc.apiKeyHeader | EDC_APIKEYHEADER          | API KEY header for edc                        | X-Api_Key     |
| edc.apiKey       | EDC_APIKEY                | API KEY for edc                               | 123456        |
| dft.hostname     | DFT_HOSTNAME              | hostname for DFT                              | https://      |
| dft.apiKeyHeader | DFT_APIKEYHEADER          | url authentication key for edc asset payload  | Api-Key       |
| dft.apiKey       | DFT_APIKEY                | url authentication code for edc asset payload | someCode      |
| edc.enabled      | EDC_ENABLED               | enable / disable edc                          | true / false  |

#### Keycloak variables:
| Property name                     | Environment Variable Name | Description                  | Example Value |
|-----------------------------------|---------------------------|------------------------------|---------------|
| connector.discovery.token-url     | KEYCLOCK_HOSTNAME         | keyclock hostnam             | https://      |
| connector.discovery.clientId      | KEYCLOCK_CLIENTID         | keyclock clientId            | X-Api_Key     |
| connector.discovery.clientSecret  | KEYCLOCK_CLIENTSECRET     | keyclock clientse            | 123456        |
| portal.backend.hostname           | PORTAL_HOSTNAME           | portal hostname              | https://      |


The values are in the [Vault].
*<i><b>Must create a GitHub token to access</b></i> 

## Upload a file:
When a file .csv is uploaded, the program checks whether the file is a SerialPartTypization or an AssemblyPartRelationship and there is a pipeline for each one.
Apart from both upload Batch upload is additional feature were added into DFT.

<b>For Serial Part Typization:</b>

1. Maps the content of the line with an Serial Part Typization Aspect.
2. Generates the UUID if it does not contain a UUID.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For Assembly Part Relationship:</b>

1. Maps the content of the line with an Assembly Part Relationship Aspect Relationship.
2. checks if an Serial Part Typization Aspect exists so it can be related to that Aspect.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For Single Level Usage As Built:</b>

1. Maps the content of the line with an Single Level Usage As Built Aspect Relationship.
2. checks if an Serial Part Typization Aspect exists so it can be related to that Aspect.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For Batch Upload:</b>

1. Maps the content of the line with an Batch.
2. Generates the UUID if it does not contain a UUID.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For PartAsPlanned Upload:</b>

1. Maps the content of the line with an PartAsPlanned.
2. Generates the UUID if it does not contain a UUID.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For SingleLevelBoMAsPlanned Upload:</b>

1. Maps the content of the line with an SingleLevelBoMAsPlanned.
2. checks if an PartAsPlanned Aspect exists so it can be related to that Aspect.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For PartSiteInformationAsPlanned Upload:</b>

1. Maps the content of the line with an PartSiteInformationAsPlanned.
2. checks if an PartAsPlanned Aspect exists so it can be related to that Aspect.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

The file .csv is loaded in memory, the content is saved and then, the file is removed from memory.


If the file is not .csv, it is read, processed and is considered as FAILED



