# My Site
# Version: 1.0
FROM python:3.8
# Install Python and Package Libraries

RUN apt-get -y install curl
RUN apt-get update && apt-get upgrade -y  && apt-get autoremove && apt-get autoclean
RUN apt-get install -y \
    libffi-dev \
    libssl-dev \
    default-libmysqlclient-dev \
    libxml2-dev \
    libxslt-dev \
    libjpeg-dev \
    libfreetype6-dev \
    zlib1g-dev \
    net-tools \
    vim

# Project Files and Settings
ARG PROJECT=myproject
ARG PROJECT_DIR=/var/www/${PROJECT}

# run the commands
RUN mkdir -p $PROJECT_DIR
WORKDIR $PROJECT_DIR

# RUN pip install -U pipenv
# RUN pipenv install --system# Server
# run the requirements file
COPY requirements.txt ./
RUN pip3 install --no-cache-dir -r requirements.txt

# 8000 port pe daalo

EXPOSE 8000
STOPSIGNAL SIGINT
ENTRYPOINT ["python", "manage.py"]
CMD ["runserver", "0.0.0.0:8000"]

