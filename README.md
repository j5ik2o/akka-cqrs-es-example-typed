# DDD CQRS Event Sourcing by using akka-typed

## Tools

- asdf
- java
- sbt
- terraform
- jq

## Set up

```sh
$ brew install asdf jq
```

- JDK

```sh
$ asdf plugin add java
$ asdf list all java
$ asdf instal java temurin-11.0.14+101
$ asdf global java temurin-11.0.14+101
```

- sbt

```sh
$ asdf plugin add sbt
$ asdf install sbt latest
$ asdf global sbt latest
```

- Terraform

```sh
$ asdf plugin add terraform
$ asdf install terraform latest
$ asdf global terraform latest
```

## 初期設定

```shell
$ cp env.sh.default env.sh
```

PREFIX, APPLICATION_NAMEを適宜修正する。
個人環境を作りたい場合は、PREFIXを変更するとよい。

### Debug by using IntelliJ IDEA

Edit Configurations

com.github.j5ik2o.api.write.Main

1. PORT=8081;AKKA_MANAGEMENT_HTTP_PORT=8558;JMX_PORT=8100
2. PORT=8082;AKKA_MANAGEMENT_HTTP_PORT=8559;JMX_PORT=8101
3. PORT=8083;AKKA_MANAGEMENT_HTTP_PORT=8560;JMX_PORT=8102

3つの分の設定を作り、IntelliJ IDEAで実行してください。デバッグしたい場合はいずれか1個のプロジェクトをデバッグで起動してください。