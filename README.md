# DDD CQRS Event Sourcing by using akka-typed

## Tools

- awscli
- asdf
- jq
- java
- sbt
- terraform

## Set up

### asdf

```sh
$ brew install asdf
```

### jq

```shell
$ asdf plugin-add jq https://github.com/AZMCode/asdf-jq.git
$ asdf install jq 1.6
$ asdf local jq 1.6
```

### awscli

```shell
$ asdf plugin add awscli
$ asdf install awscli 2.7.6
$ asdf local  awscli 2.7.6
```

### Terraform

```sh
$ asdf plugin add terraform
$ asdf install terraform 1.2.1
$ asdf local terraform 1.2.1
```

### JDK

```sh
$ asdf plugin add java
$ asdf list all java
$ asdf instal java temurin-11.0.14+101
$ asdf local java temurin-11.0.14+101
```

### sbt

```sh
$ asdf plugin add sbt
$ asdf install sbt 1.6.2
$ asdf local sbt 1.6.2
```

### EKSを使う場合

#### kubectl

トラブルを避けるためサーバ側と同じバージョンのkubectlをインストールしてください

```shell
$ KUBECTL_VERSION=1.21.13
$ asdf plugin-add kubectl https://github.com/asdf-community/asdf-kubectl.git
$ asdf install kubectl $KUBECTL_VERSION
akka-ddd-cqrs-es-example-typed $ asdf local kubectl $KUBECTL_VERSION # 必ずプロジェクトルートで設定する
$ kubectl version
Client Version: version.Info{Major:"1", Minor:"21", GitVersion:"v1.21.13", GitCommit:"80ec6572b15ee0ed2e6efa97a4dcd30f57e68224", GitTreeState:"clean", BuildDate:"2022-05-24T12:40:44Z", GoVersion:"go1.16.15", Compiler:"gc", Platform:"darwin/arm64"}
Server Version: version.Info{Major:"1", Minor:"21+", GitVersion:"v1.21.12-eks-a64ea69", GitCommit:"d4336843ba36120e9ed1491fddff5f2fec33eb77", GitTreeState:"clean", BuildDate:"2022-05-12T18:29:27Z", GoVersion:"go1.16.15", Compiler:"gc", Platform:"linux/amd64"}
```

#### helm

```shell
$ asdf plugin-add helm https://github.com/Antiarchitect/asdf-helm.git
$ asdf install helm 3.9.0
$ asdf local helm 3.9.0
# helmfileで必要なプラグイン
$ helm plugin install https://github.com/databus23/helm-diff
```

#### helmfile

```shell
$ asdf plugin-add helmfile https://github.com/feniix/asdf-helmfile.git
$ asdf install helmfile 0.144.0
$ asdf local helmfile 0.144.0
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