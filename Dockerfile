FROM clojure:lein-alpine

LABEL maintainer="cjbarre@gmail.com" \
      org="DecentStudio"             \
      version="1.0"                  \
      app="decent-news-distributor"

RUN apk update && apk add git && apk add bash

RUN git clone https://github.com/vishnubob/wait-for-it.git \
    && chmod a+x wait-for-it/wait-for-it.sh                \
    && mv wait-for-it/wait-for-it.sh /usr/local/bin/wait-for-it

COPY . /app

WORKDIR /app

RUN lein deps

ENTRYPOINT wait-for-it -s --timeout=25 $RABBITMQ_HOST:$RABBITMQ_PORT -- lein run
