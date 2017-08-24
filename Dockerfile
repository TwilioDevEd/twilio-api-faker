FROM gradle:alpine

WORKDIR /project

COPY . /project
USER root

RUN ["gradle", "build"]
CMD ["gradle", "run"]