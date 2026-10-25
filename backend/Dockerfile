# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# 时区与中文字符集
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:MaxMetaspaceSize=256m"

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY --from=build /app/target/xinghai-backend.jar app.jar

# 上传目录与日志目录（建议挂载数据卷）
RUN mkdir -p /data/xinghai/uploads /app/logs

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
