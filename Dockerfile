FROM jenkins/ssh-slave

RUN /bin/bash -c 'echo Qadir check'
ENV customVar="Sample custom var"
