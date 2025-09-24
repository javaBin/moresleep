#!/bin/sh
# Build the Docker image
docker build -t moresleep-app:latest .

# Run the container with AWS credentials mounted
docker run --rm -it \
  -p 5000:5000 \
  -v $HOME/.aws:/root/.aws:ro \
  -e AWS_PROFILE=javabin \
  moresleep-app:latest