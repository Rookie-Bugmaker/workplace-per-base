FROM swr.ckgf-global-1.cccar.com.cn/cloud_base/jdk_u:1.8_201_v1
# 环境变量配置
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    IBIZ_SLEEP=0 \
    TZ=Asia/Shanghai \
    JAVA_OPTS="-Xmx3700m -Xms3700m -javaagent:/usr/local/skywalking/skywalking-agent/skywalking-agent.jar -Duser.timezone=Asia/Shanghai"
#设置工作目录
WORKDIR /home/workplace/
# 拷贝文件夹下的文件到镜像中的app.jar
ADD @project.artifactId@-@project.version@-@env@.jar /home/workplace/app.jar
EXPOSE 11501
# 启动命令
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]