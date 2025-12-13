# Sử dụng base image (môi trường) chứa Java 17
FROM openjdk:17-jdk-slim AS build

# Đặt thư mục làm việc (working directory)
WORKDIR /app

# Sao chép các file cần thiết cho quá trình build Maven (pom.xml và mvnw)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Build dự án (chỉ build dependencies để tăng tốc độ cache Docker)
# Lệnh này sẽ chạy maven build
RUN ./mvnw dependency:go-offline

# Sao chép toàn bộ mã nguồn vào container
COPY src src

# Build file JAR cuối cùng (build command)
RUN ./mvnw clean install -DskipTests

# ----------------------------------------------------
# Giai đoạn 2: Tạo image nhẹ hơn để chạy (Runtime Image)
FROM openjdk:17-jre-slim

# Đặt biến môi trường cho Spring Boot để chạy file JAR
ENV JAR_FILE=quan_ly_kho-0.0.1-SNAPSHOT.jar
ENV PORT=8080

# Sao chép file JAR đã build từ giai đoạn 'build'
COPY --from=build /app/target/${JAR_FILE} /usr/app/

# Khai báo cổng mà ứng dụng lắng nghe
EXPOSE ${PORT}

# Lệnh chạy ứng dụng khi container khởi động (Start Command)
ENTRYPOINT ["java", "-jar", "/usr/app/quan_ly_kho-0.0.1-SNAPSHOT.jar"]