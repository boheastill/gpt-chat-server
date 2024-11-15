# 第一阶段：构建阶段
FROM ghcr.io/graalvm/graalvm-community:21 AS build

WORKDIR /app

# 设置环境变量
ENV GRAALVM_HOME=/usr/lib/graalvm
ENV PATH="$GRAALVM_HOME/bin:$PATH"
ENV GRADLE_USER_HOME=/root/.gradle

# 检查 native-image 工具，未安装则安装
RUN java -version && native-image --version || gu install native-image

# 复制 Gradle Wrapper 和项目配置文件
COPY gradlew /app/gradlew
COPY gradle /app/gradle
COPY build.gradle.kts settings.gradle.kts /app/

# 确保 Gradle Wrapper 可执行

RUN chmod +x /app/gradlew
# 构建时挂载 Gradle 缓存目录，避免重复下载分发包和依赖
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies --no-daemon

# 复制项目文件并构建
COPY . /app
# 再次确保 gradlew 可执行，防止被覆盖
RUN chmod +x /app/gradlew
RUN  --mount=type=cache,target=/root/.gradle ./gradlew clean build --no-daemon --parallel



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
