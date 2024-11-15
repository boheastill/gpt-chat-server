FROM ghcr.io/graalvm/graalvm-community:23

# 设置工作目录
WORKDIR /app

# 将构建好的 JAR 文件复制到镜像中
COPY build/libs/gpt-chat-server-0.0.1-SNAPSHOT.jar gpt-chat-server.jar

# 暴露应用程序的端口（例如 Spring Boot 默认的 8080 端口）
EXPOSE 50000

# 设置运行时命令
CMD ["java", "-jar", "gpt-chat-server.jar"]
