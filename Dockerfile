# 第一阶段：构建阶段
FROM ghcr.io/graalvm/graalvm-community:21 AS build

WORKDIR /app

# 设置环境变量
ENV GRAALVM_HOME=/usr/lib/graalvm
ENV PATH="$GRAALVM_HOME/bin:$PATH"
ENV GRADLE_USER_HOME=/root/.gradle

# 复制 Gradle Wrapper 和项目配置文件
COPY gradlew /app/gradlew
COPY gradle /app/gradle
COPY build.gradle.kts settings.gradle.kts /app/

# 确保 Gradle Wrapper 可执行
RUN chmod +x /app/gradlew

# 使用 Gradle 缓存依赖，防止每次重新下载
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies --no-daemon

# 复制项目代码（放在后面，减少变动影响缓存）
COPY . /app

# 再次确保 gradlew 可执行
RUN chmod +x /app/gradlew

# 构建项目
RUN --mount=type=cache,target=/root/.gradle ./gradlew clean build --no-daemon --parallel

# 第二阶段：运行时阶段
FROM openjdk:23-slim

WORKDIR /app

# 从构建阶段复制生成的 JAR 文件
COPY --from=build /app/build/libs/gpt-chat-server-0.0.1-SNAPSHOT.jar /app/

# 设置 JVM 优化参数（可选）
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 暴露端口
EXPOSE 50000

# 设置启动命令
CMD ["java", "-jar", "/app/gpt-chat-server-0.0.1-SNAPSHOT.jar"]
