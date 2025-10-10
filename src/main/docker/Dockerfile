FROM swr.ckgf-global-1.cccar.com.cn/cloud_base/jdk:1.8.0_201

ENV TZ="Asia/Shanghai" \
    SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    IBIZ_SLEEP=0 \
    JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

WORKDIR /home/workplace/

ADD *.jar /home/workplace/workplace-per-base.jar

EXPOSE 8864

CMD java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Duser.timezone=Asia/Shanghai -Djava.security.egd=file:/dev/./urandom -jar workplace-per-base.jar

