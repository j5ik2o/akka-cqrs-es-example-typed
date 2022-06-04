# terraform

terraformの`tfstate`を管理するために専用のS3バケットと、
terraformのロックを実現するためのDynamoDBテーブルを作る必要があります。
以下のコマンドを実行する。

```shell
$ ./create-tf-bucket.sh
$ ./create-lock-table.sh
```

## 初期化

```shell
tools/terraform $ ./terraform-init.sh
```

## デプロイ

- インフラへの変更差分を確認する

```shell
tools/terraform $ ./terraform-plan.sh
```

- インフラへの変更を適用する

```shell
tools/terraform $ ./terraform-apply.sh
```

- インフラを破棄する

```shell
tools/terraform $ ./terraform-destroy.sh
```

aws --profile adceet eks --region us-east-1 update-kubeconfig --name 