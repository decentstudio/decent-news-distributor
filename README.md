# decent-news-distributor

## Docker Usage (Development)
### Ubuntu 16.04 	

1. Navigate to the project directory

2. Create a `.env` file based on the `.env.example` file

3. Build the container:
   - `docker build -t decent-news-distributor:1.0 .`

4. Run the container:
   - `docker run --name=decent-news-distributor -d -p 80:80 --env-file=.env decent-news-distributor:1.0`

## Environment Variables Setup

Make a file called .env in the root of the project, if you have not already. Add the following required environment variables, along with their values. Follow the .env.example file if needed.

- RABBITMQ_HOST

- RABBITMQ_PORT

- RABBITMQ_USER

- RABBITMQ_PASS

- RABBITMQ_VHOST
