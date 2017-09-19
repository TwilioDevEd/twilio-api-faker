# Transactions

Transactions are json files describing the expected behavior of an API endpoint. They contain a sample request to the public API, the expected downstream service request generated, a sample downstream response, and the expected public API response.

They are used in conjunction with base and cpr definitions to ensure the correctness of an API. There should be one transaction json file for each base definition. The directory structure and file names should be symmetrical to the `base`and `cpr` directories.

## Starting from scratch?

If you are creating a new resource and need to create a brand new transaction file you can use the helper script `scripts/generate_transaction.py` to generate a skeleton transaction file
based off of a base definition that you can then fill in.

Example usage:
```bash
python ./scripts/generate_transaction.py ./base/api/v2010/new_resource.json
```

## Transaction Format

Valid top level keys are:
- `create`
- `update`
- `fetch`
- `read`
- `delete`

These should match the actions defined in the corresponding resource's base definition (if there is a mismatch the unit tests will fail).

Each top level key should have an array of **transaction stanzas**.

### The Transaction Stanza

Each transaction stanza should define the following keys:

#### name (required)

A short description of the transaction, names should be unique within an action's transactions.

#### request (required)

Defines the public API request portion of the transaction. See the `Request Format` below.

#### downstream_request (required)

Defines the request the public API should make to the downstream service given the public request. See the `Request Format` below.

#### downstream_response (required)

Defines a sample response from the downstream service that would be returned if the public API made the above request to it. See the `Response Format` below.

#### response (required)

Defines the public API response portion of the transaction. See `Response Format` below.

### Request Format

The format that we define both the public api request and the downstream service api request. It should have the following properties:

#### account_sid (required)

The account sid of the request. Generally this is just a dummy account sid such as `ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa`. The account sids must match between all the requests and responses.

#### method (required)

The HTTP method of the request. Value should be a string.

#### url (required)

The url the request would be made to. Value should be a string.

*Note:* Query params should NOT be put into the url

#### form_params (either json_params or form_params must be present)

A map of form param names to values that should be in the request.

*Note:* These may have a slightly different format than they are defined in the base definition, they should be exactly what the service expects to receive. Ie they should be ProperCased for the public request and should be in whichever format the downstream is expecting them for the downstream request.

#### json_params (either json_params or form_params must be present)

A map of json param names to values that should be in the request.

*Note:* These may have a slightly different format than they are defined in the base definition, they should be exactly what the service expects to receive. Ie they should be ProperCased for the public request and should be in whichever format the downstream is expecting them for the downstream request.

#### query_params (required)

A map of query param names to values that should be in the request.

### Response Format

#### status_code (required)

The status code of the response. In the case of the public api response, this is the status code we expect to receive after making the `request` defined in the transaction. In the case of the downstream api response this is the status code we expect to receive after making the `downstream_request` to the downstream service.

#### content (required)

The json payload we expect to receive. Values are not important as long as they match values in the requests where applicable.
