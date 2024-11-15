FROM ghcr.io/graalvm/graalvm-community:21

# 设置工作目录
WORKDIR /app

# 确保环境变量设置正确
ENV GRAALVM_HOME=/usr/lib/graalvm
ENV PATH="$GRAALVM_HOME/bin:$PATH"

# 检查 native-image 工具，未安装则安装
RUN java -version && native-image --version || gu install native-image

# 复制项目文件和 Gradle 缓存配置
COPY . /app
COPY gradle /root/.gradle
COPY gradlew /app/gradlew

# 确保 gradlew 可执行
RUN chmod +x ./gradlew

# 构建前，预下载依赖项
RUN ./gradlew dependencies --no-daemon

# 构建项目
RUN ./gradlew clean build --no-daemon

# 暴露端口
EXPOSE 50000

CMD ["java", "-jar", "build/libs/gpt-chat-server-0.0.1-SNAPSHOT.jar"]
