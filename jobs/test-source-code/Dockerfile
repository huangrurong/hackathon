FROM hackathon/pipeline:latest

RUN mkdir /Hackathon
COPY ./build-deps /Hackathon/
COPY ./docker-entrypoint.sh /docker-entrypoint.sh

# Copy config file
# Expose port

ENTRYPOINT [ "/docker-entrypoint.sh" ]
