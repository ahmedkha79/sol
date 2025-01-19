FROM ubuntu
# TODO: Replace value of CI_PROJECT_NAME with team name
ENV CI_PROJECT_NAME='vs38'
ENV CI_PROJECT_NAMESPACE='ful'
ENV DEBIAN_FRONTEND='noninteractive'
ENV TZ=Europe/Berlin
LABEL authors='Michael.Deichen@haw-hamburg.de'

RUN apt-get update && apt-get install -y \
        curl \
        gettext \
        less \
        libintl-perl \
        lynx \
        nano \
        net-tools \
        openssh-server \
        tcpdump \
        telnet \
        tzdata \
        wget \
	openjdk-21-jdk \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH="$JAVA_HOME/bin_$PATH"



# locale
RUN ln -fs /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN dpkg-reconfigure --frontend noninteractive tzdata

RUN java -version

ENV NOTVISIBLE='in users profile'
RUN echo 'export VISIBLE=now' >> /etc/profile

COPY userdata /userdata
COPY app /app
# Add welcome message
RUN echo 'Welcome to our portal from '${CI_PROJECT_NAME}'.' > /etc/motd


RUN chmod +x /userdata/autorun.sh
# autorun
#RUN /usr/bin/bash /userdata/autorun.sh


# TODO: Port nach Aufgabenblatt anpassen
EXPOSE 8131

COPY entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh
CMD ["./entrypoint.sh"]
