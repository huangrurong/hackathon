FROM ubuntu:14.04

# basics
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    vim \
    apt-transport-https \
    && rm -rf /var/lib/apt/lists/*

# nodejs
RUN curl --silent https://deb.nodesource.com/gpgkey/nodesource.gpg.key | sudo apt-key add - \
    && echo "deb https://deb.nodesource.com/node_8.x trusty main" | tee /etc/apt/sources.list.d/nodesource.list \
    && echo "deb-src https://deb.nodesource.com/node_8.x trusty main" | tee -a /etc/apt/sources.list.d/nodesource.list \
    && apt-get update \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

# pm2
RUN npm install -g pm2
