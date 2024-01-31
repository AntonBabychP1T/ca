![Main Info](https://i.imgur.com/EwfYTNR.png)
# City Car Sharing
## API Endpoints
### Authentication Controller

Endpoints available for all users without authentication.

- POST /api/auth/registration - Registers a new user.
  
*Example of request body*

```json
{
  "email": "email@mail.com",
  "password": "Min8Character",
  "repeatPassword": "Min8Character",
  "firstName": "Anton",
  "lastName": "Babych",
  "shippingAddress": "Kyiv 123 street"
}
```
- POST /api/auth/login - Authenticates a user and returns a JWT token.

*Example of request body*

```json 
{
  "email": "email@mail.com",
  "password": "Min8Character",
}
```

### Car Controller

Endpoints for managing cars. Requires authentication.


- POST /api/cars - Create a new car.
  
  *Example of request body*

```json
{
  "name": "CarName",
  "model": "CarModel",
  "brand": "CarBrand",
  "type": "SUV",
  "inventory": 2,
  "fee": 199.99
}
```

- GET /api/cars/{id} - Get car by id.

- PUT /api/cars/{id} - Update car by id.

- DELETE /api/cars/{id} - Delete car by id.

- GET /api/cars - Get all cars.

## Users Controller

Endpoints for user management. Requires authentication.

- PUT /api/users/{id}/role - Update user role.

- GET /api/users/me - Get info about user

- PATCH /api/users/me - Update current user info.

  *Example of request body*

```json
{
  "password": "NewPassword",
  "firstName": "NewFirstName",
  "lastName": "NewLastName"
}
```

# Rental Controller

Endpoints for managing rentals. Requires authentication.

- POST /api/rentals - Create a new rental

    *Example of request body*

```json
{
  "rentalDate": "2024-01-31",
  "returnDate": "2024-02-06",
  "carId": 1,
  "userId": 5
}
```

- POST /api/rentals/{id}/return - Return rental by id

- GET /api/rentals/{userId}/{isActive} - Get all active rentals for a user.

- GET /api/rentals/{id} - Get rental by id

# Payment Controller

Endpoints for managing payments. Requires authentication.

- POST /api/payments/{rentalId} - Create new payment for a rental.

- POST /api/payments/renew/{paymentId} - Renew an expired payment session.

- GET /api/payments/{userId} - Get payments for a specific user.

- GET /api/payments/success - Stripe redirection endpoint for successful payments.

- GET /api/payments/cancel - Stripe redirection endpoint for canceled payments.

## Database structure scheme 
![Database_structure.PNG](https://i.imgur.com/UaENXF4.png)

# How to test this application?
## Create Telegam bot
### Step 1: Create Bot in BotFather 
Go to Telegam and find [BotFather](https://t.me/BotFather)

Follow the instructions to create a new bot. Note down the token BotFather provides for HTTP API access.
## Step 2: Configure .env file
Add the bot token and bot name to the .env file in your project directory.

## Registration in Stripe 
### Step 1: Registration
Sing up at [Stripe](https://dashboard.stripe.com/register)
### Step 2: Get API token and configure .env
In the Stripe dashboard, navigate to the 'Developers' section to find your API token.

Add the Stripe API token to the .env file.

## Installing Postman and Importing the API Collection ## 
### Step 1: Download and Install Postman ###

Visit [Postman's official website](https://www.postman.com/) 
Download and install Postman for your operating system.

Install Postman: After downloading, run the installer and follow the on-screen instructions to complete the installation.

### Step 2: Launch Postman ###

Open Postman and sign in or continue as a guest.

### Step 3: Importing the API Collection ### 

Click 'Import' at the top left corner.

Drag and drop the .json file into the dialog or click 'Upload Files'. The .json file is available [here](https://drive.google.com/file/d/1fG0thEzODeqP7pla_S8oJA6yQ_8_g5OX/view?usp=sharing)

Click 'Import' to add the collection to Postman.

### Step 4: Using the Imported Collection ###

Find the imported collection in the 'Collections' sidebar.

Explore and send requests by clicking on them. Modify parameters or body as necessary.

Click 'Send' to execute and view responses.

### Step 5: Saving Changes (Optional) ###

Save any modifications to requests for future use.

Organize your collections and requests as needed.
